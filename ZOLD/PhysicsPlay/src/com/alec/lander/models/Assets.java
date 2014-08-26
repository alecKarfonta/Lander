package com.alec.lander.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Disposable;


public class Assets implements Disposable, AssetErrorListener {

	public static final String TAG = Assets.class.getName();
	public static final Assets instance = new Assets();	// singleton
	private AssetManager assetManager;
	private TextureAtlas atlas;
	
	private Assets() {}
	
	public AssetFonts fonts;
	public AssetSounds sounds;
	public AssetMusic music;
	public AssetLevelDecorations levelDecorations;
	
	
	public void init( AssetManager assetManager) {
		this.assetManager = assetManager;
		assetManager.setErrorListener(this);		
		assetManager.load("sounds/mainExhaust.mp3", Sound.class);
		assetManager.load("sounds/sideExhaust.wav", Sound.class);
		assetManager.load("sounds/playerDeath.wav", Sound.class);
		assetManager.load("music/intro.ogg", Music.class);
		assetManager.finishLoading();
		

		// load the texture atlas
		atlas = assetManager.get("images/");
		
		// enable texture filtering
		for (Texture texture : atlas.getTextures()) {
			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}		
		// log all the assets there were loaded
		Gdx.app.debug(TAG, "# of assets loaded: " + assetManager.getAssetNames().size);
		for (String asset : assetManager.getAssetNames()) {
			Gdx.app.debug(TAG, "asset: " + asset);
		}
		
		
		// create the game resources (inner Asset~ classes)
		fonts = new AssetFonts();
		sounds = new AssetSounds(assetManager);
		levelDecorations = new AssetLevelDecorations(atlas);

		music = new AssetMusic(assetManager);
	}
	
	public class AssetLevelDecorations {
		public AtlasRegion background;
		
		public  AssetLevelDecorations (TextureAtlas atlas) {
			background = atlas.findRegion("itsFullOfStars");
		}
	}
	
	public class AssetMusic {
		public final Music intro;
		
		public AssetMusic (AssetManager am) {
			intro = am.get("music/intro.ogg", Music.class);
			intro.setLooping(true);
			
		}
	}
	
	public class AssetSounds {
		public final Sound mainExhaust, sideExhaust, playerDeath;
		
		public AssetSounds (AssetManager am) {
			mainExhaust = am.get("sounds/mainExhaust.mp3", Sound.class);
			sideExhaust = am.get("sounds/sideExhaust.wav", Sound.class);
			playerDeath = am.get("sounds/playerDeath.wav", Sound.class);
		}
	}
		
	public class AssetFonts {
		public final BitmapFont defaultSmall;
		public final BitmapFont defaultNormal;
		public final BitmapFont defaultBig;
		
		public AssetFonts () {
			defaultSmall = new BitmapFont(Gdx.files.internal("fonts/white16.fnt"), true);
			defaultNormal = new BitmapFont(Gdx.files.internal("fonts/white16.fnt"), true);
			defaultBig = new BitmapFont(Gdx.files.internal("fonts/white16.fnt"), true);
			
			defaultSmall.setScale(.1f);
			defaultNormal.setScale(1.0f);
			defaultBig.setScale(2.0f);
			
			defaultSmall.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			defaultNormal.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
			defaultBig.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		}
		
	}

	@Override
	public void error(AssetDescriptor asset, Throwable throwable) {
		Gdx.app.error(TAG, "Couldn't load asset: '" + asset.fileName + "' " + (Exception)throwable);
	}

	@Override
	public void dispose() {
		assetManager.dispose();
	}
	
}
