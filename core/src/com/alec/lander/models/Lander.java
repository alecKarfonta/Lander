package com.alec.lander.models;

import box2dLight.ConeLight;
import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.alec.lander.Constants;
import com.alec.lander.MyMath;
import com.alec.lander.controllers.AudioManager;
import com.alec.lander.views.Play;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class Lander {
	private static final String TAG = Lander.class.getName();
	private Body chassis, rocket, leftLeg, rightLeg;
	private Body piece1, piece2, piece3;
	public RevoluteJoint jointChassisRocket;
	public PrismaticJoint jointLeftLeg, jointRightLeg;
	private ParticleEmitter mainExhaust, sideExhaust;
	private Light spotLight, smallLight;
	private Sprite chassisSprite, rocketSprite, leftLegSprite, rightLegSprite;
	private Sprite piece1Sprite, piece2Sprite, piece3Sprite;
	private float x, y;
	private float fuel;
	private float smallLightPulseTimer = 0.0f;
	private float width, height, rocketWidth, rocketHeight, legWidth,
			legHeight;
	public boolean isFiringMainRocket = false, isFiringLeftRocket = false,
			isFiringRightRocket = false, isDead = false, isMissingLeftLeg = false, isMissingRightLeg = false,
			isHidden = false;
	private float winTimer = 7.0f, winTimerInit = winTimer;
	public boolean isContactingGround = false;
	private Play play; // keep reference to play
	// settings
	private float mainThruster = .75f,
			fuelCost = .0005f, 		// cost of fuel per frame
			sideThruster = .55f;	// * chassis mass
	private int smallLightDistance = 120, 
				spotLightDistance = 800;
	private float smallLightIntensity = .30f,
				spotLightIntensity = .15f;
	
	public Lander(Play play, World world, Vector2 initPos) {
		this.play = play;
		this.x = initPos.x;
		this.y = initPos.y;
		this.width = 10;
		this.height = 10;
		fuel = 1.0f;
		rocketWidth = width * .4f;
		rocketHeight = height * .2f;
		legWidth = width * .4f;
		legHeight = height * .75f;

		String name = "lander";	// user data
		
		FixtureDef chassisFixtureDef = new FixtureDef();
		chassisFixtureDef.density = .9f;
		chassisFixtureDef.friction = .32f;
		chassisFixtureDef.restitution = .1f;
		chassisFixtureDef.filter.categoryBits = Constants.FILTER_LANDER;
		chassisFixtureDef.filter.maskBits = Constants.FILTER_GROUND;

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		bodyDef.angularDamping = 0.0f;
		bodyDef.linearDamping = 0.0f;
		bodyDef.allowSleep = false;

		// create the chassis
		PolygonShape chassisShape = new PolygonShape();
		chassisShape.setAsBox(width / 2, height / 2);
		chassisFixtureDef.shape = chassisShape;

		chassis = world.createBody(bodyDef);
		chassis.createFixture(chassisFixtureDef);
		chassis.setUserData(name + "_chassis");
		
		chassisSprite = new Sprite(Assets.instance.lander.chassis);
		chassisSprite.setSize(width, height);
		chassisSprite.setOrigin(width / 2, height / 2);

		// left leg
		// create the chassis
		PolygonShape legShape = new PolygonShape();
		legShape.setAsBox(legWidth / 2, legHeight / 2);

		rightLegSprite = new Sprite(Assets.instance.lander.rightLeg);

		rightLegSprite.setSize(legWidth, legHeight);
		rightLegSprite.setOrigin(legWidth / 2, legHeight / 2);

		leftLegSprite = new Sprite(Assets.instance.lander.leftLeg);
		leftLegSprite.setSize(legWidth, legHeight);
		leftLegSprite.setOrigin(legWidth / 2, legHeight / 2);

		FixtureDef legFixture = new FixtureDef();
		legFixture.density = .9f;
		legFixture.friction = .32f;
		legFixture.restitution = .1f;
		legFixture.shape = legShape;
		legFixture.filter.categoryBits = Constants.FILTER_LANDER;
		legFixture.filter.maskBits = Constants.FILTER_GROUND;

		leftLeg = world.createBody(bodyDef);
		leftLeg.createFixture(legFixture);
		leftLeg.setUserData(name + "_leg_left");

		rightLeg = world.createBody(bodyDef);
		rightLeg.createFixture(legFixture);
		rightLeg.setUserData(name + "_leg_right");

		PrismaticJointDef legJointDef = new PrismaticJointDef();
		legJointDef.bodyA = chassis;
		legJointDef.bodyB = leftLeg;
		legJointDef.localAnchorA.set(-width * .4f, -2);
		legJointDef.localAnchorB.set(legWidth / 2, legHeight / 2);
		legJointDef.localAxisA.set(0, 1);
		legJointDef.collideConnected = false;

		legJointDef.lowerTranslation = -.1f;
		legJointDef.upperTranslation = .1f;
		legJointDef.enableLimit = true;
		legJointDef.maxMotorForce = 10.0f;
		legJointDef.motorSpeed = 0.0f;
		legJointDef.enableMotor = true;

		jointLeftLeg = (PrismaticJoint) world.createJoint(legJointDef);

		// now set up def for right leg
		legJointDef.bodyB = rightLeg;
		legJointDef.localAnchorA.set(width * .4f, -2);
		legJointDef.localAnchorB.set(-legWidth / 2, legHeight / 2);

		jointRightLeg = (PrismaticJoint) world.createJoint(legJointDef);

		// rocket
		PolygonShape rocketShape = new PolygonShape();
		rocketShape.setAsBox(rocketWidth / 2, rocketHeight / 2);

		rocketSprite = new Sprite(Assets.instance.lander.rocket);
		rocketSprite.setSize(rocketWidth, rocketHeight);
		rocketSprite.setOrigin(rocketWidth / 2, rocketHeight / 2);

		FixtureDef rocketFixture = new FixtureDef();
		rocketFixture.density = .9f;
		rocketFixture.friction = .32f;
		rocketFixture.restitution = .1f;
		rocketFixture.shape = rocketShape;
		rocketFixture.filter.categoryBits = Constants.FILTER_LANDER;
		rocketFixture.filter.maskBits = Constants.FILTER_GROUND;

		rocket = world.createBody(bodyDef);
		rocket.createFixture(rocketFixture);
		rocket.setUserData(name);

		// joint - chassis : rocket
		RevoluteJointDef jointChassisRocketDef = new RevoluteJointDef();
		jointChassisRocketDef.bodyA = chassis;
		jointChassisRocketDef.bodyB = rocket;
		jointChassisRocketDef.localAnchorA.set(0, -height / 2);
		jointChassisRocketDef.collideConnected = false;
		jointChassisRocketDef.enableLimit = true;
		// jointChassisRocketDef.referenceAngle = 270;
		jointChassisRocketDef.lowerAngle = (float) Math.toRadians(-8);
		jointChassisRocketDef.upperAngle = (float) Math.toRadians(8);

		jointChassisRocket = (RevoluteJoint) world
				.createJoint(jointChassisRocketDef);

		// exhaust
		mainExhaust = new ParticleEmitter();
		try {
			mainExhaust.load(Gdx.files.internal("exhaust.pfx").reader(2024));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Sprite particle = new Sprite(new Texture("images/particle.png"));
		mainExhaust.setSprite(particle);

		sideExhaust = new ParticleEmitter();
		try {
			sideExhaust
					.load(Gdx.files.internal("sideExhaust.pfx").reader(2024));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		sideExhaust.setSprite(particle);
	}

	// Update
	public void update(float delta) {
		if (isContactingGround) {
			winTimer -= delta;
			// if the contact timer ends and the player is not dead
			if (winTimer < 0 && !isDead) {
				// win 
				// TODO: fire works, "houston the eagle has landed", a guy hops out
				play.win();
			}
		}
		
		if (isFiringMainRocket) {
				chassis.applyLinearImpulse(MyMath.getRectCoords(
						mainThruster * chassis.getMass(),
						(float) (Math.toDegrees(chassis.getAngle()) + -270)),
						chassis.getPosition(), false);
				fuel -= fuelCost;
				if (fuel < 0.0f) {
					stopMainRocket();
				}
		}
		if (isFiringLeftRocket) {
			chassis.applyAngularImpulse(sideThruster * chassis.getMass(), false);
		}
		if (isFiringRightRocket) {
			chassis.applyAngularImpulse(-sideThruster * chassis.getMass(), false);
		}

		// update lights
		smallLightPulseTimer += delta / 2;
		if (smallLightPulseTimer > 1) {
			smallLightPulseTimer = 0.0f;
		}
		spotLight.setDirection((float) (270 + Math.toDegrees(chassis.getAngle())));
		Vector2 posOffset = MyMath.getRectCoords(new Vector2(5, (float) (270 + Math.toDegrees(chassis.getAngle()))));
		Vector2 pos = new Vector2(chassis.getPosition().x + posOffset.x, chassis.getPosition().y + posOffset.y);
		spotLight.setPosition(pos);
	}

	public void render(SpriteBatch spriteBatch, float delta) {
		if (!isHidden) {
	 		x = chassis.getPosition().x;
			y = chassis.getPosition().y;
	
			if (!mainExhaust.isComplete()) {
				Vector2 posOffset = MyMath.getRectCoords(new Vector2(3, (float) (270 + Math.toDegrees(chassis.getAngle()))));
				Vector2 pos = new Vector2(chassis.getPosition().x + posOffset.x, chassis.getPosition().y + posOffset.y);
				pos.add(MyMath.getRectCoords(2.5f,
								(float) (Math.toDegrees(chassis.getAngle()) + 270)));
				mainExhaust.setPosition(pos.x, pos.y);
				setMainExhaustRotation();
				mainExhaust.draw(spriteBatch, delta);
			}
	
			if (!sideExhaust.isComplete()) {
				Vector2 pos = chassis
						.getPosition()
						.add(MyMath.getRectCoords(
								width * .4f,
								(float) (Math.toDegrees(chassis.getAngle()) + (isFiringLeftRocket ? 45
										: 135))));
				sideExhaust.setPosition(pos.x, pos.y);
				setSideExhaustRotation(isFiringLeftRocket);
				sideExhaust.draw(spriteBatch, delta);
			}
	
			rocketSprite.setPosition(rocket.getPosition().x - rocketWidth / 2,
					rocket.getPosition().y - rocketHeight / 2);
			rocketSprite.setRotation((float) Math.toDegrees(rocket.getAngle()));
			rocketSprite.draw(spriteBatch);
	
			if (leftLeg != null) {
				leftLegSprite.setPosition(leftLeg.getPosition().x - legWidth / 2,
						leftLeg.getPosition().y - legHeight / 2);
				leftLegSprite.setRotation((float) Math.toDegrees(leftLeg.getAngle()));
				leftLegSprite.draw(spriteBatch);
			}
	
			if (rightLeg != null) {
				rightLegSprite.setPosition(rightLeg.getPosition().x - legWidth / 2,
						rightLeg.getPosition().y - legHeight / 2);
				rightLegSprite.setRotation((float) Math.toDegrees(rightLeg.getAngle()));
				rightLegSprite.draw(spriteBatch);
			}
			
			if (!isDead) {
				chassisSprite.setPosition(x - width / 2, y - height / 2);
				chassisSprite
						.setRotation((float) Math.toDegrees(chassis.getAngle()));
				chassisSprite.draw(spriteBatch);
	
			} else if (piece1Sprite != null) {
				piece1Sprite.setPosition(piece1.getPosition().x,
						piece1.getPosition().y);
				piece1Sprite.setRotation((float) Math.toDegrees(piece1.getAngle()));
				piece1Sprite.draw(spriteBatch);
				piece2Sprite.setPosition(piece2.getPosition().x,
						piece2.getPosition().y);
				piece2Sprite.setRotation((float) Math.toDegrees(piece2.getAngle()));
				piece2Sprite.draw(spriteBatch);
				piece3Sprite.setPosition(piece3.getPosition().x,
						piece3.getPosition().y);
				piece3Sprite.setRotation((float) Math.toDegrees(piece3.getAngle()));
				piece3Sprite.draw(spriteBatch);
			}
		}
	}

	public void createLights(RayHandler rayHandler) {
		// lander lights
		Color lightColor = Color.WHITE;
		lightColor.a = spotLightIntensity;
		spotLight = new ConeLight(rayHandler, 60, lightColor,
				spotLightDistance, 0, 0, 0, 55);
		lightColor.a = smallLightIntensity;
		smallLight = new PointLight(rayHandler, 50, lightColor,
				smallLightDistance, 0, 0);
		smallLight.attachToBody(chassis, 0, 0);
	}

	public void flash() {
		// TODO : flash lights really bright
	}

	private void setMainExhaustRotation() {
		float angle = (float) Math.toDegrees(chassis.getAngle());
		mainExhaust.getAngle().setLow(angle + 270);
		mainExhaust.getAngle().setHighMin(angle + 240);
		mainExhaust.getAngle().setHighMax(angle + 300);
	}

	private void setSideExhaustRotation(boolean isLeft) {
		float angle = (float) Math.toDegrees(chassis.getAngle());
		sideExhaust.getAngle().setLow(angle + (isLeft ? 15 : 180));
		sideExhaust.getAngle().setHighMin(angle + (isLeft ? 0 : 170));
		sideExhaust.getAngle().setHighMax(angle + (isLeft ? -20 : 190));
	}
	
	public void die() {
		if (!isDead) {
			isDead = true;
			isFiringMainRocket = false;
			isFiringLeftRocket = false;
			isFiringRightRocket = false;
			AudioManager.instance.stopSound(Assets.instance.sounds.mainExhaust);
			AudioManager.instance.stopSound(Assets.instance.sounds.sideExhaust);
			AudioManager.instance.play(Assets.instance.sounds.playerDeath);
			mainExhaust.allowCompletion();
			sideExhaust.allowCompletion();
			destroyLights();
			
			(leftLeg.getFixtureList().get(0)).setUserData("borken");
			(rightLeg.getFixtureList().get(0)).setUserData("borken");
			(rocket.getFixtureList().get(0)).setUserData("borken");
			(chassis.getFixtureList().get(0)).setUserData("borken");
		}
	}

	public void destroyLights() {
		spotLight.setActive(false);
		smallLight.setActive(false);
	}
	
	public void breakLeg(int leg) {
		System.out.println("breakleg" + leg);
		if (leg == 0 && !isMissingLeftLeg) {
			if (jointLeftLeg != null && leftLeg != null) {
				AudioManager.instance.play(
						Assets.instance.sounds.legBreak);
				play.destroyJoint(jointLeftLeg);
				leftLeg.setLinearVelocity(-5, 0);
				leftLeg.setAngularVelocity(-.05f);
				String userData = "brokenLeg";
				leftLeg.setUserData(userData);
				jointLeftLeg = null;
				isContactingGround = false;
			}
		} else if (!isMissingRightLeg){
			if (jointRightLeg != null && rightLeg != null) {
				AudioManager.instance.play(
						Assets.instance.sounds.legBreak);
				play.destroyJoint(jointRightLeg);
				rightLeg.setLinearVelocity(5, 0);
				rightLeg.setAngularVelocity(.05f);
				String userData = "brokenLeg";
				rightLeg.setUserData(userData);
				jointRightLeg = null;
				isContactingGround = false;
			}
		}
	}
	
	public void breakApart(World world) {
		Filter filter = chassis.getFixtureList().get(0).getFilterData();
		filter.categoryBits = 0x0000;
		chassis.getFixtureList().get(0).setFilterData(filter);
		chassis.setLinearVelocity(0, 0);
		chassis.setGravityScale(0);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = .9f;
		fixtureDef.friction = .32f;
		fixtureDef.restitution = .1f;
		fixtureDef.filter.categoryBits = Constants.FILTER_LANDER;
		fixtureDef.filter.maskBits = Constants.FILTER_GROUND;

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;

		PolygonShape shape = new PolygonShape();

		// piece 1
		bodyDef.position.set(chassis.getPosition().x - width * .5f,
				chassis.getPosition().y + height * .5f);
		shape.setAsBox(width * .5f * .5f, height * .5f * .5f);

		fixtureDef.shape = shape;

		piece1 = world.createBody(bodyDef);
		piece1.createFixture(fixtureDef);
		piece1.setLinearVelocity(MyMath.getRectCoords(new Vector2(10, MyMath
				.getAngleBetween(chassis.getPosition(), piece1.getPosition()))));

		piece1.setAngularVelocity((float) (2 * Math.random()));

		piece1Sprite = new Sprite(Assets.instance.lander.piece1);
		piece1Sprite.setSize(width * .5f, height * .5f);
		piece1Sprite.setOrigin(width * .5f, height * .5f);

		// piece 2
		bodyDef.position.set(chassis.getPosition().x + width * .5f,
				chassis.getPosition().y + height * .5f);
		shape.setAsBox(width * .5f * .5f, height * .5f * .5f);

		fixtureDef.shape = shape;

		piece2 = world.createBody(bodyDef);
		piece2.createFixture(fixtureDef);
		piece2.setLinearVelocity(MyMath.getRectCoords(new Vector2(10, MyMath
				.getAngleBetween(chassis.getPosition(), piece2.getPosition()))));

		piece2.setAngularVelocity((float) (2 * Math.random()));

		piece2Sprite = new Sprite(Assets.instance.lander.piece2);
		piece2Sprite.setSize(width * .5f, height * .5f);
		piece2Sprite.setOrigin(width * .5f, height * .5f);

		// piece 3
		bodyDef.position.set(chassis.getPosition().x, chassis.getPosition().y);
		shape.setAsBox(width * .25f, height * .25f * .5f);

		fixtureDef.shape = shape;

		piece3 = world.createBody(bodyDef);
		piece3.createFixture(fixtureDef);
		piece3.setLinearVelocity(MyMath.getRectCoords(new Vector2(10, MyMath
				.getAngleBetween(chassis.getPosition(), piece3.getPosition()))));
		piece3.setAngularVelocity((float) (2 * Math.random()));

		piece3Sprite = new Sprite(Assets.instance.lander.piece3);
		piece3Sprite.setSize(width, height * .5f);
		piece3Sprite.setOrigin(width * .5f, height * .5f * .5f);
		
		if (leftLeg != null) {
			leftLeg.setLinearVelocity(MyMath.getRectCoords(new Vector2(15, MyMath
					.getAngleBetween(chassis.getPosition(), leftLeg.getPosition()))));
			leftLeg.setAngularVelocity((float) (-2 * Math.random()));
		}
		if (rightLeg != null) {
			rightLeg.setLinearVelocity(MyMath.getRectCoords(new Vector2(15, MyMath
					.getAngleBetween(chassis.getPosition(), rightLeg.getPosition()))));
			rightLeg.setAngularVelocity((float) (2 * Math.random()));
		}
		rocket.setLinearVelocity(MyMath.getRectCoords(new Vector2(15, MyMath
				.getAngleBetween(chassis.getPosition(), rocket.getPosition()))));
		rocket.setAngularVelocity((float) (2 * Math.random()));
	}
	
	public void fireLeftRocket() {
		if (!isDead && fuel > 0) {
			sideExhaust.start();
			AudioManager.instance.play(Assets.instance.sounds.sideExhaust);
			isFiringLeftRocket = true;
		}
	}
	
	public void fireRightRocket() {
		if (!isDead && fuel > 0) {
			sideExhaust.start();
			AudioManager.instance.play(Assets.instance.sounds.sideExhaust);
			isFiringRightRocket = true;
		}
	}
	
	public void fireMainRocket() {
		if (!isDead && fuel > 0) {
			mainExhaust.start();
			AudioManager.instance.play(Assets.instance.sounds.mainExhaust);
			isFiringMainRocket = true;
		}
	}
	
	public void stopMainRocket () {
		mainExhaust.allowCompletion();
		isFiringMainRocket = false;
		AudioManager.instance.stopSound(Assets.instance.sounds.mainExhaust);
	}
	
	public void stopLeftRocket () {
		sideExhaust.allowCompletion();
		isFiringLeftRocket = false;
		AudioManager.instance.stopSound(Assets.instance.sounds.sideExhaust);
	}
	
	public void stopRightRocket () {
		sideExhaust.allowCompletion();
		isFiringRightRocket = false;
		AudioManager.instance.stopSound(Assets.instance.sounds.sideExhaust);
	}
	
	public Body getChassis() {
		return chassis;
	}

	public float getFuel() {
		return fuel;
	}

	public Body getLeftLeg() {
		return leftLeg;
	}
	
	public Body getRightLeg() {
		return rightLeg;
	}
	
	public Body getRocket() {
		return rocket;
	}

	public void beginContact() {
//		System.out.println("lander.beginContact()");
		
		// if was not previously contacting, play impact sound
		if (isContactingGround == false) {
			isContactingGround = true;
		}
	}

	public void endContact() {
		System.out.println("lander.endContact()");
		isContactingGround = false;
		winTimer = winTimerInit;
	}

	public float getWinTimer() {
		return winTimer;
	}

	public void setWinTimer(float winTimer) {
		this.winTimer = winTimer;
	}
	
	
	
}