package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.VOModelLayers;
import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.renderer.entity.EntityWargRenderer;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LayerWargArmour extends RenderLayer<EntityWarg, ModelWarg>
{
	private final ModelWarg inner;
	private final ModelWarg outer;
	private static final ResourceLocation IRON = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/iron.png");
	private static final ResourceLocation GOLD = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/golden.png");
	private static final ResourceLocation DIAMOND = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/diamond.png");
	private static final ResourceLocation LEATHER = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/leather.png");
	private static final ResourceLocation LEATHER_OVERLAY = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/leather_overlay.png");
	
	public LayerWargArmour(RenderLayerParent<EntityWarg, ModelWarg> entityRendererIn, EntityModelSet modelsIn)
	{
		super(entityRendererIn);
		this.inner = new ModelWarg(modelsIn.bakeLayer(VOModelLayers.WARG_ARMOR_INNER));
		this.outer = new ModelWarg(modelsIn.bakeLayer(VOModelLayers.WARG_ARMOR_OUTER));
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, EntityWarg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!entitylivingbaseIn.hasItemInSlot(EquipmentSlot.CHEST))
			return;
		
		ItemStack stack = entitylivingbaseIn.getItemBySlot(EquipmentSlot.CHEST);
		Item armour = stack.getItem();
		ResourceLocation texture = null;
		if(armour == Items.LEATHER_HORSE_ARMOR)
			texture = LEATHER;
		else if(armour == Items.IRON_HORSE_ARMOR)
			texture = IRON;
		else if(armour == Items.GOLDEN_HORSE_ARMOR)
			texture = GOLD;
		else if(armour == Items.DIAMOND_HORSE_ARMOR)
			texture = DIAMOND;
		else
			return;
		
        this.getParentModel().copyPropertiesTo(this.inner);
        inner.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
        inner.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vertexBuilder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(texture));
        inner.renderToBuffer(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
        
        if(armour == Items.LEATHER_HORSE_ARMOR)
        {
			float f, f1, f2;
			int i = ((DyeableHorseArmorItem)armour).getColor(stack);
			f = (float)(i >> 16 & 255) / 255.0F;
			f1 = (float)(i >> 8 & 255) / 255.0F;
			f2 = (float)(i & 255) / 255.0F;
			this.getParentModel().copyPropertiesTo(this.outer);
			outer.prepareMobModel(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
			outer.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			vertexBuilder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(LEATHER_OVERLAY));
			outer.renderToBuffer(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, f, f1, f2, 1.0F);
        }
	}
}
