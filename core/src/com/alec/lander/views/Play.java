package com.alec.lander.views;

import java.util.ArrayList;

import javax.media.j3d.AmbientLight;

import box2dLight.DirectionalLight;
import box2dLight.Light;
import box2dLight.RayHandler;

import com.alec.lander.Constants;
import com.alec.lander.MyMath;
import com.alec.lander.controllers.AudioManager;
import com.alec.lander.controllers.CameraController;
import com.alec.lander.controllers.MyContactListener;
import com.alec.lander.models.Assets;
import com.alec.lander.models.Firework;
import com.alec.lander.models.FireworkExplosion;
import com.alec.lander.models.FuelGauge;
import com.alec.lander.models.Ground;
import com.alec.lander.models.Lander;
import com.alec.lander.models.LanderDeath;
import com.alec.lander.models.Stars;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class Play implements Screen {
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera cameraLighted, cameraUI, cameraBackground;
	private CameraController cameraController;
	private SpriteBatch lightedBatch, hudBatch, backgroundBatch;
	private PolygonSpriteBatch polySpriteBatch;
	private ShapeRenderer shapeRenderer;
	private RayHandler rayHandler;
	private int rayCastDistance = 315;
	// private Sprite background;
	private Stars stars;
	private FuelGauge fuelGauge;

	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITYITERATIONS = 8;
	private final int POSITIONITERATONS = 3;
	private final float gravityFactor = 3;
	private int width = Gdx.graphics.getWidth();
	private int height = Gdx.graphics.getHeight();

	public Lander lander;
	private Vector2 rayCastCollision, normal;
	private RayCastCallback callback;
	private LanderDeath landerDeath;
	private Ground ground;
	private boolean shouldDestroyLander = false;
	private ArrayList<Body> destroyQueue = new ArrayList<Body>();
	private ArrayList<Joint> destroyJointQueue = new ArrayList<Joint>();
	private Array<Firework> fireworks;
	private Array<FireworkExplosion> fireworkExplosions;

	// game stats
	private int currentLevel;
	
	private boolean isDarkSide = true;
	private boolean isWon = false;

	// tweak settings
	private float nextGameDelay = 7.0f;
	private int initVelocity = 70;

	public Play(int level) {
		System.out.println("Play( " + level + ")");
		currentLevel = level;
	}

	// level 1 - start with no initial velocity
	public void startLevel1() {
		currentLevel = 1;
		// spawn in the middle
		int initY = (int) ground.getPoint(ground.getPointCount() / 2).y + 350;
		int initX = (int) ground.getPoint(ground.getPointCount() / 2).x;
		// spawn with no initial velocity
		int initDY = 0;
		int initDX = 0;
		// start at 0 degrees with no angular velocity
		float initAngle = (float) Math.toRadians(0);
		float initAngularVeleoctity = 0;
		Vector2 initPos = new Vector2(initX, initY);
		Vector2 initVel = new Vector2(initDX, initDY);
		createLander(initPos, initVel, initAngle, initAngularVeleoctity);
	}

	// level 2 - start off to one side with an initial horizontal vector
	public void startLevel2() {
		currentLevel = 2;
		// start on either side of the map
		boolean isLeftSide = MathUtils.randomBoolean();
		// start at the end
		int initX = (int) ground.getPoint((isLeftSide ? 2 : ground
				.getPointCount() - 3)).x;
		int initY = (int) ground.getPoint((isLeftSide ? 2 : ground
				.getPointCount() - 3)).y + 300;
		// with some initial horizontal velocity
		int initDX = (isLeftSide ? initVelocity : -initVelocity);
		int initDY = 0;
		// 0 degrees, no angular velocity
		float initAngle = (float) Math.toRadians(90 * (isLeftSide ? 1 : -1));
		float initAngularVeleoctity = 0;
		Vector2 initPos = new Vector2(initX, initY);
		Vector2 initVel = new Vector2(initDX, initDY);
		createLander(initPos, initVel, initAngle, initAngularVeleoctity);
	}

	// level 3 - start off to one side with an initial horizontal velocity and
	// facing the wrong way
	public void startLevel3() {
		currentLevel = 3;
		// start on either side of the map
		boolean isLeftSide = MathUtils.randomBoolean();
		// start at the end
		int initX = (int) ground.getPoint((isLeftSide ? 2 : ground
				.getPointCount() - 3)).x;
		int initY = (int) ground.getPoint((isLeftSide ? 2 : ground
				.getPointCount() - 3)).y + 300;
		// with some initial horizontal velocity
		int initDX = (isLeftSide ? initVelocity : -initVelocity);
		int initDY = 0;
		// facing the wrong way, no angular velocity
		float initAngle = (float) Math.toRadians(90 * (isLeftSide ? -1 : 1));
		float initAngularVeleoctity = 0;
		Vector2 initPos = new Vector2(initX, initY);
		Vector2 initVel = new Vector2(initDX, initDY);
		createLander(initPos, initVel, initAngle, initAngularVeleoctity);
	}

	// level 4 - start off to one side with an initial horizontal velocity
	// spinning
	public void startLevel4() {
		currentLevel = 4;
		boolean isLeftSide = MathUtils.randomBoolean();
		// start at the end
		int initX = (int) ground.getPoint((isLeftSide ? 2 : ground
				.getPointCount() - 3)).x;
		int initY = (int) ground.getPoint((isLeftSide ? 2 : ground
				.getPointCount() - 3)).y + 300;
		// with some initial horizontal velocity
		int initDX = (isLeftSide ? initVelocity : -initVelocity);
		int initDY = 0;
		// spinning
		float initAngle = (float) Math.toRadians(90 * (MathUtils
				.randomBoolean() ? 1 : -1));
		float initAngularVeleoctity = 5 * (MathUtils.randomBoolean() ? 1 : -1);
		Vector2 initPos = new Vector2(initX, initY);
		Vector2 initVel = new Vector2(initDX, initDY);
		createLander(initPos, initVel, initAngle, initAngularVeleoctity);

	}

	// level 5 - break a leg
	public void startLevel5() {
		currentLevel = 5;
		int initY = (int) ground.getPoint(ground.getPointCount() / 2).y + 400;
		int initX = (int) ground.getPoint(ground.getPointCount() / 2).x;
		int initDY = -3;
		int initDX = 0;
		float initAngle = (float) Math.toRadians(0);
		float initAngularVeleoctity = 0;
		Vector2 initPos = new Vector2(initX, initY);
		Vector2 initVel = new Vector2(initDX, initDY);
		createLander(initPos, initVel, initAngle, initAngularVeleoctity);

		// after a timer, break off the leg, so the player sees whats happened
		Timer.schedule(new Task() {
			@Override
			public void run() {
				lander.breakLeg(0);
			}
		}, 2.0f);

	}

	public void changeLevel(int level) {
		// dispose of current lander

		lander.isHidden = true;
		destroy(lander.getChassis());
		destroy(lander.getLeftLeg());
		destroy(lander.getRightLeg());
		destroy(lander.getRocket());
		fireworks.clear();
		fireworkExplosions.clear();
		
		lander.isHidden = true;
		lander.isContactingGround = false;
		switch (level) {
		case 1: 
			startLevel1();
			break;
		case 2: 
			startLevel2();
			break;
		case 3: 
			startLevel3();
			break;
		case 4: 
			startLevel4();
			break;
		case 5: 
			startLevel5();
			break;
		}
		lander.isHidden = false;

		lander.isContactingGround = false;
		isWon = false;
	}
	
	public void win() {
		System.out.println("win( " + currentLevel + ")");
		if (isWon != true) {
			AudioManager.instance.play(Assets.instance.sounds.applause);
			isWon = true;
			System.out.println("win() - level " + currentLevel);
			switch (currentLevel) {
			case 1:
				createFireworks(1);
				Timer.schedule(new Task() {
					@Override
					public void run() {
						changeLevel(2);
					}
				}, nextGameDelay);

				break;
			case 2:
				createFireworks(5);
				Timer.schedule(new Task() {
					@Override
					public void run() {
						changeLevel(3);
					}
				}, nextGameDelay);

				break;
			case 3:
				createFireworks(15);
				Timer.schedule(new Task() {
					@Override
					public void run() {
						changeLevel(4);
					}
				}, nextGameDelay);

				break;
			case 4:
				createFireworks(30);
				Timer.schedule(new Task() {
					@Override
					public void run() {
						changeLevel(5);
					}
				}, nextGameDelay);

				break;
			case 5:
				System.out.println("final win");
				createFireworks(100);
				break;
				
			}
			
		}
	}

	// create
	@Override
	public void show() {
		// create each part of the screen
		createWorld();
		createLights();
		createUI();
		switch (currentLevel) {
		case 1:
			startLevel1();
			break;
		case 2:
			startLevel2();
			break;
		case 3:
			startLevel3();
			break;
		case 4:
			startLevel4();
			break;
		case 5:
			startLevel5();
			break;
		}

		shapeRenderer = new ShapeRenderer();
		rayCastCollision = new Vector2();
		normal = new Vector2();
		callback = new RayCastCallback() {
			@Override
			public float reportRayFixture(Fixture fixture, Vector2 point,
					Vector2 normal, float fraction) {
				float distance = rayCastCollision.dst(lander.getChassis()
						.getPosition());
				if (distance > 10) {
					rayCastCollision.set(point);
					cameraController.setTargetZoom(MyMath.convertRanges(
							distance, 10, rayCastDistance,
							cameraController.MAX_ZOOM_IN,
							cameraController.MAX_ZOOM_OUT));
				}
				return 1;
			}

		};
		// set up the input listener
		Gdx.input.setInputProcessor(
		// anonymous inner class for screen specific input
				new InputAdapter() {
					// Handle keyboard input
					@Override
					public boolean keyDown(int keycode) {
						switch (keycode) {
						case Keys.A:
							lander.fireLeftRocket();
							break;
						case Keys.D:
							lander.fireRightRocket();
							break;
						case Keys.SPACE:
							lander.fireMainRocket();
							break;
						case Keys.ESCAPE:
							((Game) Gdx.app.getApplicationListener())
									.setScreen(new Play(1));
							break;

						case Keys.X:
							shouldDestroyLander = true;

							break;
						default:
							break;
						}
						return false;
					}

					@Override
					public boolean keyUp(int keycode) {
						switch (keycode) {
						case Keys.A:
							lander.stopLeftRocket();
							break;
						case Keys.D:
							lander.stopRightRocket();
							break;
						case Keys.SPACE:
							lander.stopMainRocket();
							break;
						default:
							break;
						}
						return false;
					}

					// zoom
					@Override
					public boolean scrolled(int amount) {
						if (amount == 1) {
							cameraController.addZoom(cameraLighted.zoom * .25f);
						} else if (amount == -1) {
							cameraController
									.addZoom(-cameraLighted.zoom * .25f);
						}
						return true;
					}

					// click or touch
					@Override
					public boolean touchDown(int screenX, int screenY,
							int pointer, int button) {

						return false;
					}

					@Override
					public boolean touchUp(int x, int y, int pointer, int button) {

						return false;
					}

					@Override
					public boolean touchDragged(int x, int y, int pointer) {

						return false;
					}
				}); // second input adapter for the input multiplexer

		Timer.schedule(new Task() {
			@Override
			public void run() {
				if (cameraController.getZoom() == cameraController.MAX_ZOOM_IN) {
					cameraController
							.setTargetZoom(cameraController.MAX_ZOOM_OUT);
				}
			}
		}, 4);
	}

	// draw
	@Override
	public void render(float delta) {
		GL20 gl = Gdx.gl;
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		

		// draw the background
		backgroundBatch.setProjectionMatrix(cameraBackground.combined);
		backgroundBatch.begin();
		stars.render(backgroundBatch);
		backgroundBatch.end();
		
		// draw the lighted batch
		lightedBatch.setProjectionMatrix(cameraLighted.combined);
		lightedBatch.enableBlending();
		lightedBatch.begin();
//		stars.render(lightedBatch);
		
		if (lander != null) {
			lander.render(lightedBatch, delta);
		}
		for (Firework firework : fireworks) {
			firework.render(lightedBatch, delta);
		}
		for (FireworkExplosion fireworkExplosion : fireworkExplosions) {
			fireworkExplosion.render(lightedBatch, delta);
		}
		
		if (landerDeath != null) {
			landerDeath.render(lightedBatch, delta);
		}
		// debugRenderer.render(world, cameraLighted.combined);
		lightedBatch.end();

		// draw the ground
		polySpriteBatch.setProjectionMatrix(cameraLighted.combined);
		polySpriteBatch.enableBlending();
		polySpriteBatch.begin();
		ground.render(polySpriteBatch);
		polySpriteBatch.end();
		

		
				// update and draw the lights
				rayHandler.setCombinedMatrix(cameraLighted.combined);
				rayHandler.updateAndRender();
				

				update(delta);
	}

	// update
	public void update(float delta) {
		Vector2 landerPos = lander.getChassis().getPosition().cpy();

		cameraController.setTarget(landerPos);
		cameraController.update(delta);
		cameraController.applyTo(cameraLighted);
		cameraController.applyTo(cameraBackground);

		world.rayCast(callback, landerPos.add(0, -15),
				landerPos.cpy().add(0, -rayCastDistance));
		
			lander.update(delta);
			
			if (shouldDestroyLander) {
				shouldDestroyLander = false;
				lander.breakApart(world);
			}

			// fireworks
		for (Firework firework : fireworks) {
			firework.update(delta);
			// if the firework is dead, make a firework explosion
			if (firework.isDead) {
				// play the sound
				AudioManager.instance.play(Assets.instance.sounds.firework);
				// remove from fireworks list
				fireworks.removeValue(firework, true);
				// add a new fireworks explosion
				fireworkExplosions.add(new FireworkExplosion(firework.getBody()
						.getPosition(), firework.getColor()));
			}
		}

		destroyQueues();

		world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATONS);

	}

	public void createUI() {
		fuelGauge = new FuelGauge(-(width / 2), (height / 2));
	}

	@Override
	public void resize(int width, int height) {
		// reset the camera size to the width of the window scaled to the zoom
		// level
		this.width = width;
		this.height = height;
	}

	public void createWorld() {
		// create the world with surface gravity
		world = new World(new Vector2(0f, -1.6249f * gravityFactor), true);
		world.setContactListener(new MyContactListener(this));
		debugRenderer = new Box2DDebugRenderer();
		ground = new Ground(world);

		lightedBatch = new SpriteBatch();
		hudBatch = new SpriteBatch();
		backgroundBatch = new SpriteBatch();
		polySpriteBatch = new PolygonSpriteBatch();
		// setup a camera with a 1:1 ratio to the screen contents
		cameraLighted = new OrthographicCamera(width, height);
		cameraUI = new OrthographicCamera(width, height);
		cameraBackground = new OrthographicCamera(width, height);

		cameraController = new CameraController();

		fireworks = new Array<Firework>();
		fireworkExplosions = new Array<FireworkExplosion>();

		stars = new Stars(1000, 1000);
	}

	public void createLights() {
		// RayHandler.useDiffuseLight(true);
		RayHandler.setGammaCorrection(true);
		rayHandler = new RayHandler(world);

		rayHandler.setCulling(true);

		Color lightColor = Color.WHITE;
		lightColor.a = (isDarkSide ? .034f : .12f);
		Light light = new DirectionalLight(rayHandler, 500, lightColor, -40);
		Filter lightFilter = new Filter();
		lightFilter.categoryBits = Constants.FILTER_LIGHT;
		lightFilter.maskBits = Constants.FILTER_NONE;
		Light.setContactFilter(lightFilter);

//		lightColor.a = .0005f;
//		rayHandler.setAmbientLight(lightColor);
	}

	public void createFireworks(int number) {
		// fire off some fireworks
		for (int index = 0; index < number; index++) {
			Firework firework = new Firework(world, new Vector2(lander
					.getChassis().getPosition()), new Color(
					(float) Math.random(), (float) Math.random(),
					(float) Math.random(), (float) Math.random()),
					(float) (2 + (3 * Math.random())));
			firework.getBody().applyLinearImpulse(
					MyMath.getRectCoords(new Vector2((float) (15 + (10 * Math.random())),
							(float) (90 + ((45 * Math.random()))
									* (MathUtils.randomBoolean() ? 1 : -1)))),
					firework.getBody().getWorldCenter(), false);
			fireworks.add(firework);
		}
	}

	public void createLander(Vector2 initPos, Vector2 initVelocity,
			float initAngle, float initAngularVeloctity) {
		cameraController.setPosition(initPos);
		cameraController.applyTo(cameraLighted);
		lander = new Lander(this, world, initPos);
		lander.getChassis().setTransform(initPos, initAngle);
		lander.getChassis().setLinearVelocity(initVelocity);
		lander.getLeftLeg().setTransform(initPos, initAngle);
		lander.getLeftLeg().setLinearVelocity(initVelocity);
		lander.getRightLeg().setTransform(initPos, initAngle);
		lander.getRightLeg().setLinearVelocity(initVelocity);
		lander.getRocket().setTransform(initPos, initAngle);
		lander.getRocket().setLinearVelocity(initVelocity);

		lander.getChassis().setAngularVelocity(initAngularVeloctity);

		cameraController.setPosition(new Vector2(lander.getChassis()
				.getPosition()));

		rayHandler.removeAll();
		rayHandler.dispose();
		createLights();
		lander.createLights(rayHandler);
		
	}

	
	public void destroyLander() {
		if (!lander.isDead) {
			System.out.println("die");
			lander.die();
			shouldDestroyLander = true;
			lander.isDead = true;
			landerDeath = new LanderDeath(lander.getChassis().getPosition(),
					rayHandler);
			if (lander.jointChassisRocket != null
					&& lander.jointChassisRocket.isActive()) {
				destroyJoint(lander.jointChassisRocket);
			}
			if (lander.jointLeftLeg != null && lander.jointLeftLeg.isActive()) {
				destroyJoint(lander.jointLeftLeg);
			}
			if (lander.jointRightLeg != null && lander.jointRightLeg.isActive()) {
				destroyJoint(lander.jointRightLeg);
			}
		}
	}

	@Override
	public void hide() {
//		dispose();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		world.dispose();
		debugRenderer.dispose();
		shapeRenderer.dispose();
//		rayHandler.dispose();
//		lightedBatch.dispose();
		backgroundBatch.dispose();
		hudBatch.dispose();
		fuelGauge.dispose();
	}

	public void destroy(Body body) {
		if (!destroyQueue.contains(body)) {
			destroyQueue.add(body);
		}
	}

	public void destroyJoint(Joint joint) {
		if (!destroyJointQueue.contains(joint)) {
			destroyJointQueue.add(joint);
		}
	}

	private void destroyQueues() {
		if (!destroyQueue.isEmpty()) {
			for (Body body : destroyQueue) {
				world.destroyBody(body);
			}
			destroyQueue.clear();
		}
		if (!destroyJointQueue.isEmpty()) {
			for (Joint joint : destroyJointQueue) {
				world.destroyJoint(joint);
			}
			destroyJointQueue.clear();
		}
	}
}