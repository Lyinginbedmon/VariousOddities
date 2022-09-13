package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelGhastling;
import com.lying.variousoddities.entity.passive.EntityGhastling;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@OnlyIn(Dist.CLIENT)
public class EntityGhastlingRenderer extends MobRenderer<EntityGhastling, ModelGhastling>
{
	public EntityGhastlingRenderer(EntityRendererProvider.Context renderManagerIn)
	{
		super(renderManagerIn, new ModelGhastling(renderManagerIn.bakeLayer(VOModelLayers.GHASTLING)), 0F);
	}
	
	public ResourceLocation getTextureLocation(EntityGhastling entity)
	{
		return entity.getEmotion().texture();
	}
	
	public static class RenderFactory implements IRenderFactory<EntityGhastling>
	{
		public EntityRenderer<? super EntityGhastling> createRenderFor(EntityRendererProvider.Context manager) 
		{
			return new EntityGhastlingRenderer(manager);
		}
	}
}
