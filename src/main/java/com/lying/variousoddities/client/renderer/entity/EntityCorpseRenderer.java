package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;

import com.lying.variousoddities.entity.EntityBodyCorpse;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class EntityCorpseRenderer extends AbstractBodyRenderer<EntityBodyCorpse>
{
	public EntityCorpseRenderer(EntityRendererProvider.Context manager)
	{
		super(manager, new HumanoidModel<EntityBodyCorpse>(manager.bakeLayer(ModelLayers.PLAYER)), 0.5F);
	}
	
	protected void poseEntity(LivingEntity body, Random rand)
	{
		super.poseEntity(body, rand);
		body.setHealth(1F);
		body.deathTime = 20;
	}
	
	public ResourceLocation getTextureLocation(EntityBodyCorpse entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/corpse.png");
	}
}
