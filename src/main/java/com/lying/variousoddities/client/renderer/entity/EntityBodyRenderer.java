package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.EntityBodyUnconscious;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityBodyRenderer extends AbstractBodyRenderer
{
	public EntityBodyRenderer(EntityRendererManager rendererManager)
	{
		super(rendererManager, new BipedModel<AbstractBody>(0F), 0.5F);
	}
	
	protected void poseEntity(LivingEntity body, Random rand)
	{
		super.poseEntity(body, rand);
		body.setHealth(body.getMaxHealth());
		body.deathTime = 0;
		body.hurtTime = 0;
		body.hurtResistantTime = 0;
		if(rand.nextBoolean())
		{
			body.setPose(Pose.FALL_FLYING);
			body.rotationPitch = (float)Math.toRadians(90D * (rand.nextBoolean() ? 1D : -1D));
		}
		else
			body.setPose(Pose.SLEEPING);
	}
	
	public ResourceLocation getEntityTexture(AbstractBody entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/unconscious.png");
	}
	
	public static class RenderFactory implements IRenderFactory<EntityBodyUnconscious>
	{
		public EntityRenderer<? super EntityBodyUnconscious> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityBodyRenderer(manager);
		}
	}
}
