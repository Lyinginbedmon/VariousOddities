package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelPatronKirin;
import com.lying.variousoddities.client.model.entity.ModelPatronKirinHorns;
import com.lying.variousoddities.client.renderer.entity.EntityPatronKirinRenderer;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;

import net.minecraft.client.renderer.entity.layers.EnergyLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class LayerPatronKirinHorns extends EnergyLayer<EntityPatronKirin, ModelPatronKirin>
{
    public static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation(EntityPatronKirinRenderer.RESOURCE_BASE+"lightning.png");
	private static final ModelPatronKirinHorns hornModel = new ModelPatronKirinHorns();
	
	public LayerPatronKirinHorns(EntityPatronKirinRenderer rendererIn)
	{
		super(rendererIn);
	}
	
	protected float func_225634_a_(float p_225634_1_){ return MathHelper.cos(p_225634_1_ * 0.004F) * 3.0F; }
	
	protected ResourceLocation func_225633_a_(){ return LIGHTNING_TEXTURE; }
	
	protected EntityModel<EntityPatronKirin> func_225635_b_(){ return hornModel; }
}
