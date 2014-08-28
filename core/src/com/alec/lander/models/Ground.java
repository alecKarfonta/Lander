package com.alec.lander.models;

import com.alec.lander.Constants;
import com.alec.lander.MyMath;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Ground {
	private Body body;
	private Array<Vector2> points;
	private ChainShape shape;
	private PolygonSprite polySprite;
	private PolygonRegion region;
	private Vector2 pos;
	
	// settings
	private int xPoints, dx, randomDx, randomDy;
	private int leftEdgeX = -1000, 
			rightEdgeX = 100,
			depth = 1000;
	// stats
	private int peek;
	
	public Ground (World world) {
		this(world, 	// defaults
				0, 0,
				4000,	// point count
				 2,	// dx min
				 70,	// randomDx
				50 );	// randomDy
	}
	
	public Ground(World world, int initX, int initY, int xPoints, int dx, int randomDx, int randomDy) {
		this.xPoints = xPoints;
		this.dx = dx;
		this.randomDx = randomDx;
		this.randomDy = randomDy;
		this.pos = new Vector2(initX, initY);
		// body definition
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		// ground shape
		shape = new ChainShape();

		points = new Array<Vector2>(xPoints + 3);
		int groundHeight = 0;
		peek = groundHeight;
		// add a steep cliff at the edge
		points.add(new Vector2(leftEdgeX, -depth));
		
		for (int x = 0; x < xPoints; x += (dx + (randomDx * Math.random()))) {
			int y = groundHeight += randomDy * Math.random()
					* MyMath.randomSignChange();
			points.add(new Vector2(x, y));
			if (y > peek) {
				peek = y;
			}
		}
		points.add(new Vector2(points.peek().x + rightEdgeX,
				- depth));
		
		points.add(new Vector2(leftEdgeX, - depth));

		((ChainShape) shape).createChain((Vector2[]) points.toArray(Vector2.class));

		
		// build the texture region
		Vector2 mTmp = new Vector2();
		int vertexCount = shape.getVertexCount();
        float[] vertices = new float[vertexCount * 2];
        for (int k = 0; k < vertexCount; k++) {
            shape.getVertex(k, mTmp);
            vertices[k * 2] = ( mTmp.x );
            vertices[k * 2 + 1] =  ( mTmp.y );
        }
        short triangles[] = new EarClippingTriangulator()
                .computeTriangles(vertices)
                .toArray();
        
        Texture texture = new Texture(Gdx.files.internal("images/surface_alt.png"));
        texture.setWrap(TextureWrap.MirroredRepeat, TextureWrap.MirroredRepeat);
        System.out.println("Width: " + points.get(0).x + points.get(points.size-1).x);
        TextureRegion texreg = new TextureRegion(texture,0,0,points.get(0).x + points.get(points.size-2).x,300);
        texreg.setTexture(texture);
        region = new PolygonRegion(texreg, vertices, triangles);
        
		// fixture definition
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.friction = .5f;
		fixtureDef.restitution = 0;
		fixtureDef.filter.categoryBits = Constants.FILTER_GROUND;
		fixtureDef.filter.maskBits = Constants.FILTER_LANDER;
//				| Constants.FILTER_LIGHT;

		// add the floor to the world
		body = world.createBody(bodyDef);
		body.createFixture(fixtureDef);
		String userData = "ground";
		body.setUserData(userData);
//		polySprite.setPosition(body.getPosition().x, body.getPosition().y);
	}
	
	public int getPointCount() {
		return points.size;
	}
	
	public Vector2 getPoint(int index) {
		return points.get(index);
	}

	public int getDx() {
		return dx;
	}

	public void setDx(int dx) {
		this.dx = dx;
	}

	public int getRandomDx() {
		return randomDx;
	}

	public void setRandomDx(int randomDx) {
		this.randomDx = randomDx;
	}

	public int getRandomDy() {
		return randomDy;
	}

	public void setRandomDy(int randomDy) {
		this.randomDy = randomDy;
	}

	public int getPeek() {
		return peek;
	}

	public void update(float delta) {
		
	}
	
	public void render(PolygonSpriteBatch batch) {

//		polySprite.setPosition(body.getPosition().x, body.getPosition().y);
//		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
//		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl.glEnable(GL20.GL_TEXTURE_WRAP_S);
	    batch.draw(region, pos.x,pos.y, region.getRegion().getRegionWidth(), region.getRegion().getRegionHeight());
	}


	public Vector2 getPos() {
		return pos;
	
	}


	public void setPos(Vector2 pos) {
		this.pos = pos;
	}
}
