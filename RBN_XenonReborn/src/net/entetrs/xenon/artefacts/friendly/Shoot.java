package net.entetrs.xenon.artefacts.friendly;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import net.entetrs.xenon.artefacts.AbstractArtefact;
import net.entetrs.xenon.commons.displays.AnimatedSprite;

/**
 * représente un tir quel qu'il soit.
 * 
 * @author robin
 *
 */
public class Shoot extends AbstractArtefact
{
	private AnimatedSprite animatedSprite;

	public Shoot(AnimatedSprite animatedSprite, int lifeForce, int impactForce, float vX, float vY)
	{
		super(vX, vY, lifeForce, impactForce);
		this.animatedSprite = animatedSprite;
	}

	@Override
	public void render(SpriteBatch batch, float delta)
	{
		this.act(delta);
		animatedSprite.render(batch, delta);
	}

	@Override
	public Sprite getSprite()
	{
		return animatedSprite;
	}

}