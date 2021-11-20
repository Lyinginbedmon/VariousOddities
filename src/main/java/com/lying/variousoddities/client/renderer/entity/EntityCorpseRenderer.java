package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;

import com.lying.variousoddities.entity.EntityBodyCorpse;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityCorpseRenderer extends LivingRenderer<EntityBodyCorpse, BipedModel<EntityBodyCorpse>>
{
	private final EntityRendererManager manager;
	
	public EntityCorpseRenderer(EntityRendererManager rendererManager)
	{
		super(rendererManager, new BipedModel<EntityBodyCorpse>(0F), 0.5F);
		this.manager = rendererManager;
	}
	
	public ResourceLocation getEntityTexture(EntityBodyCorpse entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/corpse.png");
	}
	
	@SuppressWarnings("unchecked")
	public void render(EntityBodyCorpse entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		if(!entityIn.hasBody())
		{
			super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
			return;
		}
		
		LivingEntity body = entityIn.getBody();
		EntityRenderer<LivingEntity> renderer = (EntityRenderer<LivingEntity>)manager.getRenderer(body);
		if(renderer == null)
			return;
		
		Random rand = new Random(entityIn.getUniqueID().getLeastSignificantBits());
		body.setHealth(1F);
		body.deathTime = 20;
		
		body.limbSwing = rand.nextFloat();
		body.limbSwingAmount = (rand.nextFloat() - 0.5F) * 1.5F;
		body.prevLimbSwingAmount = body.limbSwingAmount + (rand.nextFloat() - 0.5F) * 0.01F;
		
		body.isSwingInProgress = true;
		body.swingProgressInt = rand.nextInt(6);
		body.swingProgress = rand.nextFloat();
		body.prevSwingProgress = body.swingProgress + (rand.nextFloat() - 0.5F) * 0.01F;
		
		renderer.render(body, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	public static class RenderFactory implements IRenderFactory<EntityBodyCorpse>
	{
		public EntityRenderer<? super EntityBodyCorpse> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityCorpseRenderer(manager);
		}
	}
}
