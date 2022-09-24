package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelPatronKirin;
import com.lying.variousoddities.client.renderer.entity.layer.LayerPatronKirinEye;
import com.lying.variousoddities.client.renderer.entity.layer.LayerPatronKirinHorns;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EntityPatronKirinRenderer extends MobRenderer<EntityPatronKirin, ModelPatronKirin>
{
	public static final String RESOURCE_BASE = Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_kirin/";
	private static final ResourceLocation kirinTexture = new ResourceLocation(RESOURCE_BASE+"patron_kirin.png");
	
	public static final float SCALE = 1.05F;
	
	public EntityPatronKirinRenderer(EntityRendererProvider.Context rendererManager)
	{
		super(rendererManager, new ModelPatronKirin(rendererManager.bakeLayer(VOModelLayers.PATRON_KIRIN)), 0.5F);
		
		this.addLayer(new LayerPatronKirinHorns(this, rendererManager.getModelSet()));
		this.addLayer(new LayerPatronKirinEye(this));
	}
	
	public ResourceLocation getTextureLocation(EntityPatronKirin entity){ return kirinTexture; }
	
    protected void preRenderCallback(EntityPatronKirin entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime)
    {
    	matrixStackIn.scale(SCALE, SCALE, SCALE);
    }
}
