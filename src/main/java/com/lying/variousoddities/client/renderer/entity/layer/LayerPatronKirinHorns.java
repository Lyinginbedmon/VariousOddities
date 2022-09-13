package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelPatronKirin;
import com.lying.variousoddities.client.model.entity.ModelPatronKirinHorns;
import com.lying.variousoddities.client.renderer.entity.EntityPatronKirinRenderer;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LayerPatronKirinHorns extends EnergySwirlLayer<EntityPatronKirin, ModelPatronKirin>
{
    public static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation(EntityPatronKirinRenderer.RESOURCE_BASE+"lightning.png");
	private final ModelPatronKirinHorns hornModel;
	
	public LayerPatronKirinHorns(RenderLayerParent<EntityPatronKirin, ModelPatronKirin> rendererIn, EntityModelSet modelsIn)
	{
		super(rendererIn);
		this.hornModel = new ModelPatronKirinHorns(modelsIn.bakeLayer(VOModelLayers.PATRON_KIRIN_HORNS));
	}
	
	protected float xOffset(float p_225634_1_){ return Mth.cos(p_225634_1_ * 0.004F) * 3.0F; }
	
	protected ResourceLocation getTextureLocation(){ return LIGHTNING_TEXTURE; }
	
	protected EntityModel<EntityPatronKirin> model(){ return hornModel; }
}
