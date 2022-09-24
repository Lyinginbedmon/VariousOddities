package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelMindFlayer;
import com.lying.variousoddities.entity.hostile.EntityMindFlayer;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class EntityMindFlayerRenderer extends MobRenderer<EntityMindFlayer, ModelMindFlayer>
{
	public EntityMindFlayerRenderer(EntityRendererProvider.Context context)
	{
		super(context, new ModelMindFlayer(context.bakeLayer(VOModelLayers.MIND_FLAYER)), 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<EntityMindFlayer>(context.bakeLayer(VOModelLayers.MIND_FLAYER_ARMOR_INNER)), new HumanoidModel<EntityMindFlayer>(context.bakeLayer(VOModelLayers.MIND_FLAYER_ARMOR_OUTER))));
	}
	
	public ResourceLocation getTextureLocation(EntityMindFlayer entity)
	{
		return new ResourceLocation(Reference.ModInfo.MOD_ID,"textures/entity/mind_flayer/mind_flayer.png");
	}
}
