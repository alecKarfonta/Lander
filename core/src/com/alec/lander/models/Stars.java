package com.alec.lander.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Stars {
	int width, height;
	PolygonSprite sprite;
	TextureRegion texreg;
	
	public Stars (int width, int height) {
		this.width = width;
		this.height = height;
		
		Texture texture = new Texture(Gdx.files.internal("images/itsFullOfStars.png"));
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        texreg = new TextureRegion(texture,0,0,width,height);
        texreg.setTexture(texture);
//		sprite = new PolygonSprite(texreg);
//		PolygonRegion polyRegion = new PolygonRegion(texreg);
		
	}

	public void render(SpriteBatch batch) {
		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
	    batch.draw(texreg, 
	    		0,0,
	    		width, height);
	}
}
