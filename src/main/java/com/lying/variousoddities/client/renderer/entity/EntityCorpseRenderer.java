package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.EntityBodyCorpse;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityCorpseRenderer extends AbstractBodyRenderer
{
	public EntityCorpseRenderer(EntityRendererManager rendererManager)
	{
		super(rendererManager, new BipedModel<AbstractBody>(0F), 0.5F);
	}
	
	protected void poseEntity(LivingEntity body, Random rand)
	{
		super.poseEntity(body, rand);
		body.setHealth(1F);
		body.deathTime = 20;
	}
	
	public ResourceLocation getEntityTexture(AbstractBody entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/corpse.png");
	}
	
	public static class RenderFactory implements IRenderFactory<EntityBodyCorpse>
	{
		public EntityRenderer<? super EntityBodyCorpse> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityCorpseRenderer(manager);
		}
	}
}
