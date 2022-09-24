package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;

import com.lying.variousoddities.entity.EntityBodyUnconscious;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public class EntityBodyRenderer extends AbstractBodyRenderer<EntityBodyUnconscious>
{
	public EntityBodyRenderer(EntityRendererProvider.Context manager)
	{
		super(manager, new HumanoidModel<EntityBodyUnconscious>(manager.bakeLayer(ModelLayers.PLAYER)), 0.5F);
	}
	
	protected void poseEntity(LivingEntity body, Random rand)
	{
		super.poseEntity(body, rand);
		body.setHealth(body.getMaxHealth());
		body.deathTime = 0;
		body.hurtTime = 0;
		body.invulnerableTime = 0;
		if(rand.nextBoolean())
		{
			body.setPose(Pose.FALL_FLYING);
			body.setXRot((float)Math.toRadians(90D * (rand.nextBoolean() ? 1D : -1D)));
		}
		else
			body.setPose(Pose.SLEEPING);
	}
	
	public ResourceLocation getTextureLocation(EntityBodyUnconscious entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/unconscious.png");
	}
}
