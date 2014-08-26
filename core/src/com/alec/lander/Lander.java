package com.alec.lander;

import com.alec.lander.controllers.AudioManager;
import com.alec.lander.models.Assets;
import com.alec.lander.views.Play;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;

public class Lander extends Game {
	
	@Override
	public void create() {		
		
		Assets.instance.init(new AssetManager());
		GamePreferences.instance.load();
		AudioManager.instance.play(Assets.instance.music.intro);
		
		setScreen(new Play(1));
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void render() {		
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}
}
