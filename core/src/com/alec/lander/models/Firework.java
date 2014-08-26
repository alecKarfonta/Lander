package com.alec.lander.models;

import com.alec.lander.Constants;
import com.alec.lander.MyMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Firework {
	public Body body;
	public boolean isDead = false;
	private Color color;
	private float timer;

	public ParticleEffect particles = new ParticleEffect();

	public Firework(World world, Vector2 initPos, Color color, float timer) {
		this.color = color;
		this.timer = timer;
		// define body
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(initPos); // initially place it off screen
		bodyDef.bullet = true;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 2;
		fixtureDef.friction = .01f;
		fixtureDef.restitution = 0;
		fixtureDef.filter.categoryBits = Constants.FILTER_COMET;
		fixtureDef.filter.maskBits = Constants.FILTER_NONE;

		CircleShape shape = new CircleShape();
		shape.setRadius(.5f);
		fixtureDef.shape = shape;

		// create body
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);

		// model data
		body.setUserData(this);

		particles.load(Gdx.files.internal("particles/comet.pfx"),
				Gdx.files.internal("particles"));
		particles.setPosition(body.getPosition().x, body.getPosition().y);
		particles.start();

		particles.getEmitters().get(0).getTint()
				.setColors(new float[] { color.r, color.g, color.b });
	}

	public void render(SpriteBatch batch, float deltaTime) {
		particles.draw(batch, deltaTime);
	}

	public void update(float deltaTime) {
		timer -= deltaTime;
		if (timer <= 0) {
			isDead = true;
			return;
		}
		// apply gravity
//		Vector2 forceVectorPolar = new Vector2();
//		forceVectorPolar.x = (body.getMass() * 10000)
//				/ body.getPosition().dst(0,0);
//		forceVectorPolar.y = MyMath.getAngleBetween(body.getPosition(),
//				new Vector2(0, 0));
//		body.applyForceToCenter(MyMath.getRectCoords(forceVectorPolar), false);
		
		// update particles position
		particles.setPosition(body.getPosition().x, body.getPosition().y);
	}

	public Color getColor() {
		return color;
	}

	public void reset() {
		Filter filter = this.body.getFixtureList().get(0).getFilterData();
		filter.categoryBits = Constants.FILTER_NONE;
		body.getFixtureList().get(0).setFilterData(filter);
		// body.setTransform(new Vector2(100,0), 0);
	}
	
	public Body getBody() {
		return body;
	}

}
