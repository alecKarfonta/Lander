package com.alec.lander.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;

public class FuelGauge {
	private Sprite border;
	private Texture fill;
	private float displayFuel = 1.0f;
	private float width, height;
	private float x, y;
	
	public FuelGauge (int x , int y) {
		fill = new Texture(Gdx.files.internal("images/fill.png"));
		border = new Sprite(Assets.instance.ui.gaugeBorder);
		this.width = border.getWidth();
		this.height = border.getHeight();

		this.x = x ;
		this.y = y - height;
	}
	
	public void render(SpriteBatch spriteBatch, float fuel) {
		displayFuel = Interpolation.linear.apply(displayFuel, fuel, .25f);
		spriteBatch.draw(border, 
				x, y, 		
				width, height);	
//		spriteBatch.draw(fill, 
//				x, y, 		
//				width, height);	
		spriteBatch.draw(fill, 
				x, y, 		
				width * displayFuel, height,
				(float)0, (float)0,
				displayFuel, 1f);	
		
	}
	
	public void dispose() {
	}
	
}
