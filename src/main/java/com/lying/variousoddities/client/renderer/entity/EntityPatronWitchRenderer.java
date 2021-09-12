package com.lying.variousoddities.client.renderer.entity;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchElf;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityPatronWitchRenderer extends LivingRenderer<EntityPatronWitch, ModelPatronWitchElf>
{
	/**
	 * Multiple models and textures
	 * When the entity ceases to be rendered, mark the last time it was viewed by the client
	 * If the time since last viewing is sufficient, randomly select a different model to render
	 */
	private static long lastRenderTime = -1;
	private static EnumAppearance appearance = EnumAppearance.HUMAN;
	
	/** Redmaker-esque */
//	private static final ModelPatronWitchHuman humanWitch = new ModelPatronWitchHuman();
//	private static final ResourceLocation humanWitchTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch.png");
//	public static final Predicate<EntityLivingBase> isHuman = new IsAppearance(EnumAppearance.HUMAN);
	
	/** Elven */
//	private static final ModelPatronWitchElf elfWitch = new ModelPatronWitchElf();
	private static final ResourceLocation elfWitchTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_elf.png");
//	public static final Predicate<EntityLivingBase> isElf = new IsAppearance(EnumAppearance.ELF);
    
    /** Crone */
//    private static final ModelPatronWitchCrone croneWitch = new ModelPatronWitchCrone();
//    private static final ResourceLocation croneWitchTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_crone.png");
//	public static final Predicate<EntityLivingBase> isCrone = new IsAppearance(EnumAppearance.CRONE);
    
    /** Fox */
//    private static final ModelPatronWitchFox foxWitch = new ModelPatronWitchFox();
//	private static final ResourceLocation foxWitchTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_fox.png");
//    public static final Predicate<EntityLivingBase> isFox = new IsAppearance(EnumAppearance.FOX);
    
    /** Changeling */
//    private static final ModelChangelingBiped changelingWitch = new ModelChangelingBiped();
//	private static final ResourceLocation changelingWitchTexture = RenderChangeling.changelingTexture;
//	public static final Predicate<EntityLivingBase> isChangeling = new IsAppearance(EnumAppearance.CHANGELING);
	
	public EntityPatronWitchRenderer(EntityRendererManager rendererManager)
	{
		super(rendererManager, new ModelPatronWitchElf(), 0.5F);
	}
	
	public ResourceLocation getEntityTexture(EntityPatronWitch entity){ return elfWitchTexture; }
    
    public static EnumAppearance getCurrentAppearance()
    {
    	return appearance;
    }
	
	public enum EnumAppearance
	{
		HUMAN(0.9375F, 49),
		ELF(1F, 16),
		CRONE(0.9375F, 24),
		FOX(1F, 8),
		CHANGELING(1.25F, 3);
		
		private final float scale;
		private final int weight;
		
		private EnumAppearance(float sizeIn, int weightIn)
		{
			scale = Math.max(0.2F, sizeIn);
			weight = Math.max(1, weightIn);
		}
		
		public boolean isActive()
		{
			return EntityPatronWitchRenderer.getCurrentAppearance() == this;
		}
		
		public static EnumAppearance getRandom(Random rand)
		{
			List<EnumAppearance> weightedList = Lists.newArrayList();
			for(EnumAppearance appearance : values())
				for(int i=0; i<appearance.weight; i++) weightedList.add(appearance);
			
			return weightedList.get(rand.nextInt(weightedList.size()));
		}
	}
	
	public static class RenderFactory implements IRenderFactory<EntityPatronWitch>
	{
		public EntityRenderer<? super EntityPatronWitch> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityPatronWitchRenderer(manager);
		}
	}
}
