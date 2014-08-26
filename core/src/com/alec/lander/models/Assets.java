package com.alec.lander.models;

import com.alec.lander.Constants;
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
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
	public AssetLander lander;
	public AssetLevelDecorations levelDecorations;
	public AssetUI ui;
	
	
	public void init( AssetManager assetManager) {
		this.assetManager = assetManager;
		assetManager.setErrorListener(this);		
		assetManager.load(Constants.TEXTURE_ATLAS_OBJECTS, TextureAtlas.class);
		assetManager.load("sounds/mainExhaust.mp3", Sound.class);
		assetManager.load("sounds/sideExhaust.wav", Sound.class);
		assetManager.load("sounds/playerDeath.wav", Sound.class);
		assetManager.load("sounds/applause.mp3", Sound.class);
		assetManager.load("sounds/firework.wav", Sound.class);
		assetManager.load("music/intro.ogg", Music.class);
		assetManager.finishLoading();
		

		// load the texture atlas
		atlas = assetManager.get(Constants.TEXTURE_ATLAS_OBJECTS);
		
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
		lander = new AssetLander(atlas);
		levelDecorations = new AssetLevelDecorations(atlas);
		ui = new AssetUI(atlas);

		music = new AssetMusic(assetManager);
	}
	
	public class AssetLander {
		public AtlasRegion chassis, piece1, piece2, piece3, rightLeg, leftLeg, rocket;
		
		public AssetLander (TextureAtlas atlas) {
			chassis = atlas.findRegion("lander/lander");
			piece1 = atlas.findRegion("lander/landerPiece1");
			piece2 = atlas.findRegion("lander/landerPiece2");
			piece3 = atlas.findRegion("lander/landerPiece3");
			rightLeg = atlas.findRegion("lander/rightLeg");
			leftLeg = atlas.findRegion("lander/leftLeg");
			rocket = atlas.findRegion("lander/rocket");
		}
	}
	
	public class AssetUI {
		public AtlasRegion gaugeFill, gaugeBorder, glassPanel, glassPanel_corners, metalPanel_blue, metalPanel_red, metalPanel_yellow;
		
		public AssetUI (TextureAtlas atlas) {
			gaugeFill = atlas.findRegion("ui/fill");
			gaugeBorder = atlas.findRegion("ui/border");
			glassPanel = atlas.findRegion("ui/glassPanel");
			glassPanel_corners = atlas.findRegion("ui/glassPanel_corners");
			metalPanel_blue = atlas.findRegion("ui/metaPanel_blue");
			metalPanel_red = atlas.findRegion("ui/metalPanel_red");;
			metalPanel_yellow = atlas.findRegion("ui/metalPanel_yellow");
		}
	}
	
	public class AssetLevelDecorations {
		public AtlasRegion background, surface;
		
		public  AssetLevelDecorations (TextureAtlas atlas) {
			background = atlas.findRegion("itsFullOfStars");
			surface = atlas.findRegion("surface/surface");
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
		public final Sound mainExhaust, sideExhaust, playerDeath, applause, firework;
		
		public AssetSounds (AssetManager am) {
			mainExhaust = am.get("sounds/mainExhaust.mp3", Sound.class);
			sideExhaust = am.get("sounds/sideExhaust.wav", Sound.class);
			playerDeath = am.get("sounds/playerDeath.wav", Sound.class);
			applause = am.get("sounds/applause.mp3", Sound.class);
			firework = am.get("sounds/firework.wav", Sound.class);
		}
	}
		
	public class AssetFonts {
		public final BitmapFont defaultSmall;
		public final BitmapFont defaultNormal;
		public final BitmapFont defaultBig;
		
		public AssetFonts () {
			FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/reg.ttf"));
			FreeTypeFontParameter params = new FreeTypeFontParameter();
			params.size = 12;
			defaultSmall = generator.generateFont(params);
			params.size = 22;
			defaultNormal = generator.generateFont(params);
			params.size = 32;
			defaultBig = generator.generateFont(params);
			
			generator.dispose();
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
