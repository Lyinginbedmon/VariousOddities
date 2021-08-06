package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.entity.EntityCorpse;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityCorpseRenderer extends LivingRenderer<EntityCorpse, BipedModel<EntityCorpse>>
{
	private final EntityRendererManager manager;
	
	public EntityCorpseRenderer(EntityRendererManager rendererManager)
	{
		super(rendererManager, new BipedModel<EntityCorpse>(0F), 0.5F);
		this.manager = rendererManager;
	}
	
	public ResourceLocation getEntityTexture(EntityCorpse entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/corpse.png");
	}
	
//	@SuppressWarnings("unchecked")
//	public void render(EntityCorpse entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
//	{
//		LivingEntity body = entityIn.hasBody() ? entityIn.getBody() : EntityType.BLAZE.create(entityIn.getEntityWorld());
//		EntityRenderer<LivingEntity> renderer = (EntityRenderer<LivingEntity>)manager.getRenderer(body);
//		if(renderer == null)
//			return;
//		
//		renderer.render(body, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
//	}
	
	public void render(EntityCorpse entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		System.out.println("Rendering corpse"); // Never called?
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	public static class RenderFactory implements IRenderFactory<EntityCorpse>
	{
		public EntityRenderer<? super EntityCorpse> createRenderFor(EntityRendererManager manager) 
		{
			System.out.println("Registering corpse renderer");
			return new EntityCorpseRenderer(manager);
		}
	}
}
