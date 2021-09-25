package com.lying.variousoddities.client.renderer.entity;

import com.google.common.base.Predicate;
import com.lying.variousoddities.client.model.entity.ModelChangeling;
import com.lying.variousoddities.entity.passive.IChangeling;
import com.lying.variousoddities.entity.wip.EntityChangeling;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityChangelingRenderer extends LivingRenderer<EntityChangeling, ModelChangeling<EntityChangeling>>
{
//	private static final ModelChangelingElf changelingElfModel = new ModelChangelingElf();
	private static final ModelChangeling<EntityChangeling> changelingModel = new ModelChangeling<EntityChangeling>();
	
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

	public EntityChangelingRenderer(EntityRendererManager rendererManager)
	{
		super(rendererManager, new ModelChangeling<EntityChangeling>(), 0.5F);
	}
	
	public ResourceLocation getEntityTexture(EntityChangeling entity){ return changelingTexture; }
	
	public static class RenderFactory implements IRenderFactory<EntityChangeling>
	{
		public EntityRenderer<? super EntityChangeling> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityChangelingRenderer(manager);
		}
	}
}
