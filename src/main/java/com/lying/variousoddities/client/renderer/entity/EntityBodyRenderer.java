package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.EntityBodyUnconscious;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityBodyRenderer extends AbstractBodyRenderer
{
	public EntityBodyRenderer(EntityRendererProvider.Context rendererManager)
	{
		super(rendererManager, new HumanoidModel<AbstractBody>(0F), 0.5F);
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
	
	public ResourceLocation getTextureLocation(AbstractBody entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/unconscious.png");
	}
	
	public static class RenderFactory implements IRenderFactory<EntityBodyUnconscious>
	{
		public EntityRenderer<? super EntityBodyUnconscious> createRenderFor(EntityRendererProvider.Context manager) 
		{
			return new EntityBodyRenderer(manager);
		}
	}
}
