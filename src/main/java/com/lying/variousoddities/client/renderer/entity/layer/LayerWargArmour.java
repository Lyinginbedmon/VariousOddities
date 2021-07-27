package com.lying.variousoddities.client.renderer.entity.layer;

import com.lying.variousoddities.client.model.entity.ModelWarg;
import com.lying.variousoddities.client.renderer.entity.EntityWargRenderer;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeableHorseArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

public class LayerWargArmour extends LayerRenderer<EntityWarg, ModelWarg>
{
	private final ModelWarg MODEL = new ModelWarg(0.2F);
	private final ModelWarg MODEL2 = new ModelWarg(0.225F);
	private static final ResourceLocation IRON = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/iron.png");
	private static final ResourceLocation GOLD = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/gold.png");
	private static final ResourceLocation DIAMOND = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/diamond.png");
	private static final ResourceLocation LEATHER = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/leather.png");
	private static final ResourceLocation LEATHER_OVERLAY = new ResourceLocation(EntityWargRenderer.resourceBase+"armor/leather_overlay.png");
	
	public LayerWargArmour(IEntityRenderer<EntityWarg, ModelWarg> entityRendererIn)
	{
		super(entityRendererIn);
	}
	
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityWarg entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!entitylivingbaseIn.hasItemInSlot(EquipmentSlotType.CHEST))
			return;
		
		ItemStack stack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.CHEST);
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
		
        this.getEntityModel().copyModelAttributesTo(this.MODEL);
        MODEL.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
        MODEL.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        IVertexBuilder vertexBuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(texture));
        MODEL.render(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
        
        if(armour == Items.LEATHER_HORSE_ARMOR)
        {
			float f, f1, f2;
			int i = ((DyeableHorseArmorItem)armour).getColor(stack);
			f = (float)(i >> 16 & 255) / 255.0F;
			f1 = (float)(i >> 8 & 255) / 255.0F;
			f2 = (float)(i & 255) / 255.0F;
			this.getEntityModel().copyModelAttributesTo(this.MODEL2);
			MODEL2.setLivingAnimations(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks);
			MODEL2.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			vertexBuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(LEATHER_OVERLAY));
			MODEL2.render(matrixStackIn, vertexBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, f, f1, f2, 1.0F);
        }
	}
}
