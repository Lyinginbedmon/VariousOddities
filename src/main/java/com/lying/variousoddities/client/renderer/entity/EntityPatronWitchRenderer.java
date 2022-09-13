package com.lying.variousoddities.client.renderer.entity;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchChangeling;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchCrone;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchElf;
import com.lying.variousoddities.client.model.entity.ModelPatronWitchHuman;
import com.lying.variousoddities.client.renderer.entity.layer.LayerConditional;
import com.lying.variousoddities.client.renderer.entity.layer.LayerOddityGlow;
import com.lying.variousoddities.client.renderer.entity.layer.LayerPatronWitchPonytail;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityPatronWitchRenderer extends MobRenderer<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>
{
	private static long lastRenderTime = -1;
	private static EnumAppearance appearance = EnumAppearance.HUMAN;
	
	/** Redmaker-esque */
	private final HumanoidModel<EntityPatronWitch> humanWitch;
	private static final ResourceLocation humanWitchTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch.png");
	
	/** Elven */
	private final HumanoidModel<EntityPatronWitch> elfWitch;
	private static final ResourceLocation elfWitchTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_elf.png");
    
    /** Crone */
    private final HumanoidModel<EntityPatronWitch> croneWitch;
    private static final ResourceLocation croneWitchTexture = new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_crone.png");
    
    /** Changeling */
    private final HumanoidModel<EntityPatronWitch> changelingWitch;
	private static final ResourceLocation changelingWitchTexture = EntityChangelingRenderer.changelingTexture;
	
	public EntityPatronWitchRenderer(EntityRendererProvider.Context rendererManager)
	{
		super(rendererManager, new ModelPatronWitchHuman(rendererManager.bakeLayer(VOModelLayers.PATRON_WITCH_HUMAN)), 0.5F);
		
		this.humanWitch = this.model;
		this.elfWitch = new ModelPatronWitchElf(rendererManager.bakeLayer(VOModelLayers.PATRON_WITCH_ELF));
	    this.croneWitch = new ModelPatronWitchCrone(rendererManager.bakeLayer(VOModelLayers.PATRON_WITCH_CRONE));
	    this.changelingWitch = new ModelPatronWitchChangeling(rendererManager.bakeLayer(VOModelLayers.PATRON_WITCH_CHANGELING));
		
//		this.addLayer(new LayerPatronWitchHat(this));	// Needs injection by VE
		this.addLayer(new LayerPatronWitchPonytail(this, EnumAppearance.HUMAN::isActive, new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_ponytail.png")));
		this.addLayer(new LayerConditional<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(this, new LayerOddityGlow<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(this, new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_glow.png")), EnumAppearance.HUMAN::isActive));
		
		this.addLayer(new LayerPatronWitchPonytail(this, EnumAppearance.CRONE::isActive, new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_crone_ponytail.png")));
		this.addLayer(new LayerConditional<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(this, new LayerOddityGlow<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(this, new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_crone_glow.png")), EnumAppearance.CRONE::isActive));
		
//		this.addLayer(new LayerPatronWitchCrown(this));	// Needs injection by VE
		this.addLayer(new LayerPatronWitchPonytail(this, EnumAppearance.ELF::isActive, new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_elf_ponytail.png")));
		this.addLayer(new LayerConditional<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(this, new LayerOddityGlow<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(this, new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_witch/patron_witch_elf_glow.png")), EnumAppearance.ELF::isActive));
		
//		this.addLayer(new LayerConditional<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(new LayerPatronWitchChangeling(this), EnumAppearance.CHANGELING::isActive));
		this.addLayer(new LayerConditional<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(this, new LayerOddityGlow<EntityPatronWitch, HumanoidModel<EntityPatronWitch>>(this, EntityChangelingRenderer.changelingTextureGlow), EnumAppearance.CHANGELING::isActive));
	}
    
    public static EnumAppearance getCurrentAppearance()
    {
    	return appearance;
    }
	
	public ResourceLocation getTextureLocation(EntityPatronWitch entity)
	{
		switch(appearance)
		{
			case CHANGELING:
				return changelingWitchTexture;
			case CRONE:
				return croneWitchTexture;
			case ELF:
				return elfWitchTexture;
			case HUMAN:
			default:
				return humanWitchTexture;
		}
	}
    
    public HumanoidModel<EntityPatronWitch> getEntityModel()
    {
		switch(appearance)
		{
			case ELF:
				return elfWitch;
			case CRONE:
				return croneWitch;
			case CHANGELING:
				return changelingWitch;
			default:
				return humanWitch;
		}
    }
    
    public void render(EntityPatronWitch entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
    {
    	long time = System.currentTimeMillis();
    	if(lastRenderTime > 0 && (time - lastRenderTime) / 1000 > 5)
    	{
    		// Randomly select different appearance
    		EnumAppearance lastAppearance = appearance;
    		Random rand = new Random(time);
    		while(appearance == lastAppearance)
    			appearance = EnumAppearance.getRandom(rand);
    		
    		this.model = getEntityModel();
    	}
    	lastRenderTime = time;
    	
    	switch(appearance)
    	{
    		case FOX:
    			Fox fox = EntityType.FOX.create(entityIn.getLevel());
    			fox.read(entityIn.writeWithoutTypeId(new CompoundTag()));
    			Minecraft.getInstance().getRenderManager().renderEntityStatic(fox, 0, 0, 0, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    			break;
    		default:
    	    	super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    	    	break;
    	}
    }
	
    /**
     * Allows the render to do state modifications necessary before the model is rendered.
     */
    protected void preRenderCallback(EntityPatronWitch koboldIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	float totalScale = appearance.scale;
    	if(EnumAppearance.FOX.isActive(koboldIn))
    		this.shadowRadius = 0F;
    	else
    		this.shadowRadius = 0.5F * totalScale;
    	matrixStackIn.scale(totalScale, totalScale, totalScale);
    }
	
	public static enum EnumAppearance
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
		
		public boolean isActive(LivingEntity par1Entity)
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
		public EntityRenderer<? super EntityPatronWitch> createRenderFor(EntityRendererProvider.Context manager) 
		{
			return new EntityPatronWitchRenderer(manager);
		}
	}
}
