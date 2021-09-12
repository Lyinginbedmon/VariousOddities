package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.model.entity.ModelPatronKirin;
import com.lying.variousoddities.client.renderer.entity.layer.LayerPatronKirinEye;
import com.lying.variousoddities.client.renderer.entity.layer.LayerPatronKirinHorns;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityPatronKirinRenderer extends LivingRenderer<EntityPatronKirin, ModelPatronKirin>
{
	public static final String RESOURCE_BASE = Reference.ModInfo.MOD_PREFIX+"textures/entity/patron_kirin/";
	private static final ResourceLocation kirinTexture = new ResourceLocation(RESOURCE_BASE+"patron_kirin.png");
	
	public static final float SCALE = 1.05F;
	
	public EntityPatronKirinRenderer(EntityRendererManager rendererManager)
	{
		super(rendererManager, new ModelPatronKirin(), 0.5F);
		
		this.addLayer(new LayerPatronKirinHorns(this));
		this.addLayer(new LayerPatronKirinEye(this));
	}
	
	public ResourceLocation getEntityTexture(EntityPatronKirin entity){ return kirinTexture; }
	
    protected void preRenderCallback(EntityPatronKirin entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime)
    {
    	matrixStackIn.scale(SCALE, SCALE, SCALE);
    }
	
	public static class RenderFactory implements IRenderFactory<EntityPatronKirin>
	{
		public EntityRenderer<? super EntityPatronKirin> createRenderFor(EntityRendererManager manager) 
		{
			return new EntityPatronKirinRenderer(manager);
		}
	}
}
