package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.EntityBodyCorpse;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityCorpseRenderer extends AbstractBodyRenderer
{
	public EntityCorpseRenderer(EntityRendererProvider.Context rendererManager)
	{
		super(rendererManager, new HumanoidModel<AbstractBody>(0F), 0.5F);
	}
	
	protected void poseEntity(LivingEntity body, Random rand)
	{
		super.poseEntity(body, rand);
		body.setHealth(1F);
		body.deathTime = 20;
	}
	
	public ResourceLocation getTextureLocation(AbstractBody entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/corpse.png");
	}
	
	public static class RenderFactory implements IRenderFactory<EntityBodyCorpse>
	{
		public EntityRenderer<? super EntityBodyCorpse> createRenderFor(EntityRendererProvider.Context manager) 
		{
			return new EntityCorpseRenderer(manager);
		}
	}
}
