package net.entetrs.xenon.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class AbstractArtefact implements Artefact {
	
	private int lifePoint;
	private int impactForce;
	
	public void translateX(float dx) {
		this.getSprite().translateX(dx);
	}
	
	public void translateY(float dy) {
		this.getSprite().translateY(dy);
	}
	

	@Override
	public void decreaseLife(int force) {
		this.lifePoint -= force;
	}

	@Override
	public int getImpactForce() {
		return impactForce;
	}

	@Override
	public boolean isAlive() {
		return lifePoint > 0;
	}

	@Override
	public abstract Sprite getSprite();

}