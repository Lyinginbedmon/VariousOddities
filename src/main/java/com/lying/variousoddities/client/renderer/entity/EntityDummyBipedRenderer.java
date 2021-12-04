package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.ClientPlayerDummy;
import com.lying.variousoddities.entity.EntityDummyBiped;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

public class EntityDummyBipedRenderer extends LivingRenderer<EntityDummyBiped, PlayerModel<EntityDummyBiped>>
{
	@SuppressWarnings("rawtypes")
	public EntityDummyBipedRenderer(EntityRendererManager renderManagerIn, boolean useSmallArms)
	{
		super(renderManagerIn, new PlayerModel<EntityDummyBiped>(0F, useSmallArms), 0.5F);
		this.addLayer(new BipedArmorLayer<>(this, new BipedModel(0.5F), new BipedModel(1.0F)));
		this.addLayer(new HeldItemLayer<>(this));
	    this.addLayer(new HeadLayer<>(this));
	    this.addLayer(new ElytraLayer<>(this));
	}
	
	protected boolean canRenderName(EntityDummyBiped entityIn)
    {
		return super.canRenderName(entityIn) && (entityIn.getAlwaysRenderNameTagForRender() || entityIn.hasCustomName() && entityIn == this.renderManager.pointedEntity);
    }
	
	public ResourceLocation getEntityTexture(EntityDummyBiped entity)
	{
		if(!entity.hasGameProfile())
			return DefaultPlayerSkin.getDefaultSkin(entity.getUniqueID());
		ClientPlayerDummy player = new ClientPlayerDummy((ClientWorld)entity.getEntityWorld(), entity.getGameProfile());
		return player.getLocationSkin();
	}
	
	protected void preRenderCallback(AbstractClientPlayerEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime)
	{
		float scale = 0.9375F;
		matrixStackIn.scale(scale, scale, scale);
	}
	
	public void render(EntityDummyBiped entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		this.setModelVisibilities(entityIn);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	public Vector3d getRenderOffset(EntityDummyBiped entityIn, float partialTicks)
	{
		return entityIn.isCrouching() ? new Vector3d(0.0D, -0.125D, 0.0D) : super.getRenderOffset(entityIn, partialTicks);
	}
	
	private void setModelVisibilities(EntityDummyBiped clientPlayer)
	{
		PlayerModel<EntityDummyBiped> model = this.getEntityModel();
		ClientPlayerDummy player = new ClientPlayerDummy((ClientWorld)clientPlayer.getEntityWorld(), clientPlayer.getGameProfile());
		if(clientPlayer.isSpectator())
		{
			model.setVisible(false);
			model.bipedHead.showModel = true;
			model.bipedHeadwear.showModel = true;
		}
		else
		{
			model.setVisible(true);
			model.bipedHeadwear.showModel = player.isWearing(PlayerModelPart.HAT);
			model.bipedBodyWear.showModel = player.isWearing(PlayerModelPart.JACKET);
			model.bipedLeftLegwear.showModel = player.isWearing(PlayerModelPart.LEFT_PANTS_LEG);
			model.bipedRightLegwear.showModel = player.isWearing(PlayerModelPart.RIGHT_PANTS_LEG);
			model.bipedLeftArmwear.showModel = player.isWearing(PlayerModelPart.LEFT_SLEEVE);
			model.bipedRightArmwear.showModel = player.isWearing(PlayerModelPart.RIGHT_SLEEVE);
			model.isSneak = clientPlayer.isCrouching();
			BipedModel.ArmPose armPoseMain = getPoseForHand(clientPlayer, Hand.MAIN_HAND);
			BipedModel.ArmPose armPoseOff = getPoseForHand(clientPlayer, Hand.OFF_HAND);
			if(armPoseMain.func_241657_a_())
				armPoseOff = clientPlayer.getHeldItemOffhand().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
		
			if (clientPlayer.getPrimaryHand() == HandSide.RIGHT)
			{
				model.rightArmPose = armPoseMain;
				model.leftArmPose = armPoseOff;
			}
			else
			{
				model.rightArmPose = armPoseOff;
				model.leftArmPose = armPoseMain;
			}
		}
	}
	
	private static BipedModel.ArmPose getPoseForHand(EntityDummyBiped entityIn, Hand handIn)
	{
		ItemStack heldItem = entityIn.getHeldItem(handIn);
		if(heldItem.isEmpty())
			return BipedModel.ArmPose.EMPTY;
		
		if(entityIn.getActiveHand() == handIn && entityIn.getItemInUseCount() > 0)
		{
			switch(heldItem.getUseAction())
			{
				case BLOCK:
					return BipedModel.ArmPose.BLOCK;
				case BOW:
					return BipedModel.ArmPose.BOW_AND_ARROW;
				case CROSSBOW:
					if(handIn == entityIn.getActiveHand())
						return BipedModel.ArmPose.CROSSBOW_CHARGE;
					break;
				case SPEAR:
					return BipedModel.ArmPose.THROW_SPEAR;
				default:
					break;
			
			}
		}
		else if(!entityIn.isSwingInProgress && heldItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(heldItem))
			return BipedModel.ArmPose.CROSSBOW_HOLD;
		
		return BipedModel.ArmPose.ITEM;
	}
}
