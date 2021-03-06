package com.alec.lander;

public class Constants {
	public final static String GameName = "Lunar Lander";

	public final static float TIMESTEP = 1 / 60f;
	public final static int VELOCITYITERATIONS = 8;
	public final static int POSITIONITERATONS = 3;
	
	public static final float VIEWPORT_WIDTH = 128f;
	public static final float VIEWPORT_HEIGHT = 72f;
	public static final float VIEWPORT_GUI_WIDTH = 1280.0f;
	public static final float VIEWPORT_GUI_HEIGHT = 720.0f;

	public static final String TEXTURE_ATLAS_OBJECTS = "images/lander.pack";
	public static final String TEXTURE_ATLAS_MENU_UI = "ui/uiskin.atlas";
	public static final String SKIN_MENU_UI = "ui/uiskin.json";
	
	public final static short FILTER_NONE = 0x0000;
	public final static short FILTER_GROUND = 0x0001;
	public final static short FILTER_LANDER = 0x0002;
	public final static short FILTER_LIGHT = 0x0003;
	public final static short FILTER_EXPLOSION = 0x0004;
	public final static short FILTER_COMET = 0x0005;
	public final static short FILTER_ALIENSHIP = 0x0006;
	

	public static final String PREFERENCES = "default.prefs";

	
}
