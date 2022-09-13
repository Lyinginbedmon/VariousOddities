package com.lying.variousoddities.client.renderer.entity;

import com.google.common.base.Predicate;
import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelChangeling;
import com.lying.variousoddities.entity.passive.IChangeling;
import com.lying.variousoddities.entity.wip.EntityChangeling;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityChangelingRenderer extends MobRenderer<EntityChangeling, ModelChangeling<EntityChangeling>>
{
	@SuppressWarnings("unused")
	private final ModelChangeling<EntityChangeling> changelingModel;
//	private final ModelChangeling<EntityChangeling> changelingElfModel = new ModelChangelingElf();
	
	public static final ResourceLocation changelingTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/changeling/changeling.png");
	public static final ResourceLocation changelingTextureGlow = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/changeling/changeling_glow.png");
	
	public static final ResourceLocation changelingElfTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/changeling/changeling_elf.png");
	
    public static final Predicate<LivingEntity> SHOULD_REVEAL = new Predicate<LivingEntity>()
    		{
    			public boolean apply(LivingEntity input)
    			{
    				return IChangeling.shouldReveal(Minecraft.getInstance().player, input);
    			}
    		};
    
	public EntityChangelingRenderer(EntityRendererProvider.Context rendererManager)
	{
		super(rendererManager, new ModelChangeling<EntityChangeling>(rendererManager.bakeLayer(VOModelLayers.CHANGELING)), 0.5F);
		this.changelingModel = this.model;
//		this.changelingElfModel = new ModelChangelingElf(rendererManager.bakeLayer(VOModelLayers.CHANGELING_ELF));
	}
	
	public ResourceLocation getTextureLocation(EntityChangeling entity){ return changelingTexture; }
	
	public static class RenderFactory implements IRenderFactory<EntityChangeling>
	{
		public EntityRenderer<? super EntityChangeling> createRenderFor(EntityRendererProvider.Context manager) 
		{
			return new EntityChangelingRenderer(manager);
		}
	}
}
