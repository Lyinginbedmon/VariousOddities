package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.client.ClientPlayerDummy;
import com.lying.variousoddities.entity.EntityDummyBiped;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class EntityDummyBipedRenderer extends LivingEntityRenderer<EntityDummyBiped, PlayerModel<EntityDummyBiped>>
{
	@SuppressWarnings("rawtypes")
	public EntityDummyBipedRenderer(EntityRendererProvider.Context context, boolean useSmallArms)
	{
		super(context, new PlayerModel<EntityDummyBiped>(context.bakeLayer(useSmallArms ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), useSmallArms), 0.5F);
		this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel(0.5F), new HumanoidModel(1.0F)));
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
	    this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
	    this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
	}
	
	protected boolean shouldShowName(EntityDummyBiped entityIn)
    {
		return super.shouldShowName(entityIn) && (entityIn.shouldShowName() || entityIn.hasCustomName() && entityIn == this.entityRenderDispatcher.crosshairPickEntity);
    }
	
	public ResourceLocation getTextureLocation(EntityDummyBiped entity)
	{
		if(!entity.hasGameProfile())
			return DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
		ClientPlayerDummy player = new ClientPlayerDummy((ClientLevel)entity.getLevel(), entity.getGameProfile());
		return player.getSkinTextureLocation();
	}
	
	protected void scale(LocalPlayer entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime)
	{
		float scale = 0.9375F;
		matrixStackIn.scale(scale, scale, scale);
	}
	
	public void render(EntityDummyBiped entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		this.setModelVisibilities(entityIn);
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}
	
	public Vec3 getRenderOffset(EntityDummyBiped entityIn, float partialTicks)
	{
		return entityIn.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : super.getRenderOffset(entityIn, partialTicks);
	}
	
	private void setModelVisibilities(EntityDummyBiped clientPlayer)
	{
		PlayerModel<EntityDummyBiped> model = this.getModel();
		ClientPlayerDummy player = new ClientPlayerDummy((ClientLevel)clientPlayer.getLevel(), clientPlayer.getGameProfile());
		if(clientPlayer.isSpectator())
		{
			model.setAllVisible(false);
			model.head.visible = true;
			model.hat.visible = true;
		}
		else
		{
			model.setAllVisible(true);
			model.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
			model.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
			model.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
			model.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
			model.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
			model.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
			model.crouching = clientPlayer.isCrouching();
			HumanoidModel.ArmPose armPoseMain = getPoseForHand(clientPlayer, InteractionHand.MAIN_HAND);
			HumanoidModel.ArmPose armPoseOff = getPoseForHand(clientPlayer, InteractionHand.OFF_HAND);
			if(armPoseMain.isTwoHanded())
				armPoseOff = clientPlayer.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
			
			if (clientPlayer.isLeftHanded())
			{
				model.rightArmPose = armPoseOff;
				model.leftArmPose = armPoseMain;
			}
			else
			{
				model.rightArmPose = armPoseMain;
				model.leftArmPose = armPoseOff;
			}
		}
	}
	
	private static HumanoidModel.ArmPose getPoseForHand(EntityDummyBiped entityIn, InteractionHand handIn)
	{
		ItemStack heldItem = entityIn.getItemInHand(handIn);
		if(heldItem.isEmpty())
			return HumanoidModel.ArmPose.EMPTY;
		
		if(entityIn.getUsedItemHand() == handIn && entityIn.getTicksUsingItem() > 0)
		{
			switch(heldItem.getUseAnimation())
			{
				case BLOCK:
					return HumanoidModel.ArmPose.BLOCK;
				case BOW:
					return HumanoidModel.ArmPose.BOW_AND_ARROW;
				case CROSSBOW:
					if(handIn == entityIn.getUsedItemHand())
						return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
					break;
				case SPEAR:
					return HumanoidModel.ArmPose.THROW_SPEAR;
				default:
					break;
			
			}
		}
		else if(!entityIn.swinging && heldItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(heldItem))
			return HumanoidModel.ArmPose.CROSSBOW_HOLD;
		
		return HumanoidModel.ArmPose.ITEM;
	}
}
