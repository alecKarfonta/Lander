package com.alec.lander.models;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

public class LanderDeath {
	public ParticleEffect particles;
	private Light light;
	private float lightTimer = 1f;

	public LanderDeath(Vector2 initPos, RayHandler rayHandler) {
		particles = new ParticleEffect();
		particles.load(Gdx.files.internal("landerDeath.pfx"),
				Gdx.files.internal("images/"));
		particles.setPosition(initPos.x, initPos.y);
		particles.start();
		
		int distance = 1;
		int rays = 10;
		light = new PointLight(rayHandler, rays, Color.RED, distance, initPos.x, initPos.y);
		
		//AudioManager.instance.play(Assets.instance.sounds.asteroidDeath);
	}


	public boolean shouldDestroy() {
		return particles.isComplete();
	}

	public void render(SpriteBatch batch, float deltaTime) {
		lightTimer -= deltaTime;
		light.setDistance(50 * Interpolation.bounce.apply(lightTimer));
		particles.draw(batch, deltaTime);
	}
}