package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelBase;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityWorgRenderer extends MobRenderer<EntityWorg, ModelBase<EntityWorg>>
{
	public EntityWorgRenderer(EntityRendererManager p_i50961_1)
	{
		super(p_i50961_1, new ModelBase<EntityWorg>(), 0.5F);
	}
	
	public ResourceLocation getEntityTexture(EntityWorg entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/entity/worg/worg.png");
	}
	
	public static class RenderFactory implements IRenderFactory<EntityWorg>
	{
		public EntityRenderer<? super EntityWorg> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityWorgRenderer(manager);
		}
	}
}
