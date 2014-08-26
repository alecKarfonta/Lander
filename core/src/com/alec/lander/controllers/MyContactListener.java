package com.alec.lander.controllers;

import com.alec.lander.views.Play;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class MyContactListener implements ContactListener {
	private static final String TAG = MyContactListener.class.getName();
	
	private Play play; // reference play so you can call functions
	
	public MyContactListener(Play play) {
		this.play = play;
	}
	
	@Override
	public void beginContact(Contact contact) {
		
	}

	@Override
	public void endContact(Contact contact) {
			play.lander.endContact();
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	
		
	}
	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		if (contact.getFixtureA().getBody().getUserData() instanceof String) {
			postSolver(contact.getFixtureA(), impulse.getNormalImpulses()[0]);
		}
		
		if (contact.getFixtureB().getBody().getUserData() instanceof String) {
			postSolver(contact.getFixtureB(), impulse.getNormalImpulses()[0]);
		}
	}
	
	public void postSolver(Fixture fixture, float impulse) {
		if (fixture.getBody().getUserData() instanceof String) {
			String data = (String) fixture.getBody().getUserData();
			if ( data != null && data.contains("lander")) {
				// if the force is over the threshold for death
				if (impulse > 200) {
					// if the impact was with a leg
					if (data.contains("leg")) {
						if (data.contains("left")) {
							if (!play.lander.isDead) {
								play.lander.breakLeg(0);
							}
						} else if (data.contains("right")) {
							if (!play.lander.isDead) {
								play.lander.breakLeg(1);
							}
						}
					}   
					// if the impact was with the chassis
					if (data.contains("chassis")) {
						play.destroyLander();
					}
				// else the lander impacted, but did not explode	
				} else {
					play.lander.beginContact();
				}
			}
		}
	}

}
