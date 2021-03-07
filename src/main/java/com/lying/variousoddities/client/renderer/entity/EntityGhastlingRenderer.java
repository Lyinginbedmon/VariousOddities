package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityGhastlingRenderer extends MobRenderer<EntityGhastling, ModelGhastling>
{
	public EntityGhastlingRenderer(EntityRendererManager renderManagerIn)
	{
		super(renderManagerIn, new ModelGhastling(), 0F);
	}
	
	public ResourceLocation getEntityTexture(EntityGhastling entity)
	{
		return entity.getEmotion().texture();
	}
	
	public static class RenderFactory implements IRenderFactory<EntityGhastling>
	{
		public EntityRenderer<? super EntityGhastling> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityGhastlingRenderer(manager);
		}
	}
}
