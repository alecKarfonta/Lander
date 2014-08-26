package com.alec.lander.views;

import java.util.ArrayList;

import box2dLight.DirectionalLight;
import box2dLight.Light;
import box2dLight.RayHandler;

import com.alec.lander.Constants;
import com.alec.lander.MyMath;
import com.alec.lander.controllers.CameraController;
import com.alec.lander.controllers.MyContactListener;
import com.alec.lander.models.FuelGauge;
import com.alec.lander.models.Ground;
import com.alec.lander.models.Lander;
import com.alec.lander.models.LanderDeath;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Play implements Screen {
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera cameraGame, cameraUI;
	private CameraController cameraController;
	private SpriteBatch spriteBatch, hudBatch;
	private ShapeRenderer shapeRenderer;
	private RayHandler rayHandler;
	private Sprite background;
	private FuelGauge fuelGauge;

	private final float TIMESTEP = 1 / 60f;
	private final int VELOCITYITERATIONS = 8;
	private final int POSITIONITERATONS = 3;
	private int width = Gdx.graphics.getWidth();
	private int height = Gdx.graphics.getHeight();
	private int bottom = -(height / 2);

	public Lander lander;
	private Vector2 startPos, rayCastCollision, normal;
	private RayCastCallback callback;
	private LanderDeath landerDeath;
	private Ground ground;
	private boolean shouldDestroyLander = false;
	private ArrayList<Body> destroyQueue = new ArrayList<Body>();
	private ArrayList<Joint> destroyJointQueue = new ArrayList<Joint>();

	@Override
	public void show() {
		// create each part of the screen
		createWorld();
		createUI();
		createGround();
		createLander();
		createLights();

		shapeRenderer = new ShapeRenderer();
		rayCastCollision = new Vector2();
		normal = new Vector2();
		callback = new RayCastCallback() {
			@Override
			public float reportRayFixture(Fixture fixture, Vector2 point,
					Vector2 normal, float fraction) {
				rayCastCollision.set(point);
				if (rayCastCollision.y < 100 && rayCastCollision.y > 50) {
					cameraController.setTargetZoom(MyMath.convertRanges(
							rayCastCollision.dst(lander.getChassis().getPosition()), 
							0, 100,
							cameraController.MAX_ZOOM_IN + .05f,
							cameraController.MAX_ZOOM_OUT));
					System.out.println(rayCastCollision.dst(lander.getChassis().getPosition()));
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
									.setScreen(new Play());
							break;

						case Keys.X:
							shouldDestroyLander = true;

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
							cameraController.addZoom(cameraGame.zoom * .25f);
						} else if (amount == -1) {
							cameraController.addZoom(-cameraGame.zoom * .25f);
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
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		// increment the world
		world.step(TIMESTEP, VELOCITYITERATIONS, POSITIONITERATONS);

		cameraController.update(delta);
		cameraController.applyTo(cameraGame);

		// add each sprite
		spriteBatch.setProjectionMatrix(cameraGame.combined);
		spriteBatch.begin();

		background.draw(spriteBatch);

		// render the car's effects
		lander.render(spriteBatch, Gdx.graphics.getDeltaTime());
		cameraController.setTarget(lander.getChassis().getPosition());

		if (landerDeath != null) {
			landerDeath.render(spriteBatch, delta);
		}

		// debugRenderer.render(world, cameraGame.combined);

		spriteBatch.end();

		rayHandler.setCombinedMatrix(cameraGame.combined);
		rayHandler.updateAndRender();

		hudBatch.begin();
		hudBatch.setProjectionMatrix(cameraUI.combined);
		fuelGauge.render(hudBatch, lander.getFuel());
		hudBatch.end();

		destroyQueues();

		update(delta);

		/** /
		Vector2 rayCast = new Vector2();
		rayCast.add(lander.getChassis().getPosition());
		rayCast.add(0, -100);
		world.rayCast(callback, lander.getChassis().getPosition(), rayCast);

		shapeRenderer.setProjectionMatrix(cameraGame.combined);
		shapeRenderer.begin(ShapeType.Point);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.point(rayCastCollision.x, rayCastCollision.y, 0);
		shapeRenderer.end();
		/**/
	}

	public void createUI() {
		fuelGauge = new FuelGauge(-(width / 2), (height / 2));
	}

	public void update(float delta) {

		lander.update(delta);
		if (shouldDestroyLander) {
			shouldDestroyLander = false;
			lander.breakApart(world);
		}
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
		world = new World(new Vector2(0f, -1.6249f), true);
		world.setContactListener(new MyContactListener(this));
		debugRenderer = new Box2DDebugRenderer();

		spriteBatch = new SpriteBatch();
		hudBatch = new SpriteBatch();

		// setup a camera with a 1:1 ratio to the screen contents
		cameraGame = new OrthographicCamera(width, height);
		cameraUI = new OrthographicCamera(width, height);

		cameraController = new CameraController();

		background = new Sprite(new Texture(
				Gdx.files.internal("images/itsFullOfStars.png")));

		background.setBounds(0, 0, width, height);
		background.setOrigin(-width / 2, height);
	}

	public void createGround() {
		ground = new Ground(world);
	}

	public void createLights() {
		// RayHandler.useDiffuseLight(true);
		RayHandler.setGammaCorrection(true);
		rayHandler = new RayHandler(world);

		rayHandler.setCulling(true);

		Color lightColor = Color.WHITE;
		lightColor.a = .07f;
		Light light = new DirectionalLight(rayHandler, 1000, lightColor, -40);
		Filter lightFilter = new Filter();
		lightFilter.categoryBits = Constants.FILTER_LIGHT;
		lightFilter.maskBits = Constants.FILTER_GROUND;
		Light.setContactFilter(lightFilter);

		lightColor.a = .1f;
		rayHandler.setAmbientLight(lightColor);

		lander.createLights(rayHandler);
	}

	public void createLander() {
		int startHeight = 100;
		startPos = ground.getPoint(ground.getPointCount() / 2).add(0,
				startHeight);

		lander = new Lander(world, startPos);

		cameraController.setPosition(new Vector2(lander.getChassis()
				.getPosition()));
	}

	public void destroyLander() {
		if (!lander.isDead) {
			lander.die();
			shouldDestroyLander = true;
			lander.isDead = true;
			landerDeath = new LanderDeath(lander.getChassis().getPosition(),
					rayHandler);
			destroyJointQueue.add(lander.jointChassisRocket);
			destroyJointQueue.add(lander.jointLeftLeg);
			destroyJointQueue.add(lander.jointRightLeg);
		}
	}

	@Override
	public void hide() {
		dispose();
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
		rayHandler.dispose();
		hudBatch.dispose();
		fuelGauge.dispose();
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