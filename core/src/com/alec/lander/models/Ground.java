package com.alec.lander.models;

import com.alec.lander.Constants;
import com.alec.lander.MyMath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Ground {
	private Body body;
	private Array<Vector2> points;
	private int pointCount = 1000; 
	
	public Ground(World world) {
		// body definition
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		// ground shape
		Shape shape = new ChainShape();

		points = new Array<Vector2>();
		float groundHeight = 0;
		// add a steep cliff at the edge
		points.add(new Vector2(-1000, - 1000));
		for (int x = 0; x < pointCount; x += (5 + (20 * Math.random()))) {
			points.add(new Vector2(x, groundHeight += 20 * Math.random()
					* MyMath.randomSignChange()));
		}
		points.add(new Vector2(points.peek().x + 1000,
				groundHeight - 1000));

		((ChainShape) shape).createChain((Vector2[]) points.toArray(Vector2.class));

		// fixture definition
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.friction = .5f;
		fixtureDef.restitution = 0;
		fixtureDef.filter.categoryBits = Constants.FILTER_GROUND;
		fixtureDef.filter.maskBits = Constants.FILTER_LANDER
				| Constants.FILTER_LIGHT;

		// add the floor to the world
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setUserData(this);

	}
	
	public int getPointCount() {
		return points.size;
	}
	
	public Vector2 getPoint(int index) {
		return points.get(index);
	}
	
}
