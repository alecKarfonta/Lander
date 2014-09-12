package com.alec.lander.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Button {
	Rectangle bounds;
	TextureRegion texture;
	
	public Button(Rectangle bounds, TextureRegion texture) {
		super();
		this.bounds = bounds;
		this.texture = texture;
	}

	public void render(SpriteBatch batch) {
		batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	public TextureRegion getTexture() {
		return texture;
	}

	public void setTexture(TextureRegion texture) {
		this.texture = texture;
	}
	
	
	
}
