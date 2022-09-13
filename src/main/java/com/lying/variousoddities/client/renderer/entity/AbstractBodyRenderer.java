package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;
import java.util.UUID;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.EntityDummyBiped;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.LivingEntity;

public abstract class AbstractBodyRenderer extends LivingEntityRenderer<AbstractBody, HumanoidModel<AbstractBody>>
{
	private final EntityDummyBipedRenderer playerRendererThin;
	private final EntityDummyBipedRenderer playerRendererThick;
	
	public AbstractBodyRenderer(EntityRendererProvider.Context rendererManager, HumanoidModel<AbstractBody> modelIn, float shadowSize)
	{
		super(rendererManager, modelIn, shadowSize);
		this.playerRendererThin = new EntityDummyBipedRenderer(rendererManager, true);
		this.playerRendererThick = new EntityDummyBipedRenderer(rendererManager, false);
	}
	
	protected boolean shouldShowName(AbstractBody entityIn)
    {
		return super.shouldShowName(entityIn) && (entityIn.shouldShowName() || entityIn.hasCustomName() && entityIn == this.entityRenderDispatcher.crosshairPickEntity);
    }
	
	@SuppressWarnings({ "unchecked" })
	public void render(AbstractBody entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		if(!entityIn.hasBody())
		{
			super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
			return;
		}
		
		LivingEntity body = entityIn.getBodyForRender();
		poseEntity(body, new Random(entityIn.getUUID().getLeastSignificantBits()));
		
		matrixStackIn.pushPose();
			matrixStackIn.mulPose(Vector3f.YP.rotation(entityIn.yBodyRot));
			switch(body.getPose())
			{
				case CROUCHING:
				case DYING:
				case SPIN_ATTACK:
				case STANDING:
					if(body.deathTime > 0)
						matrixStackIn.translate(body.getType().getHeight() * -0.5D, 0D, 0D);
					break;
				case SWIMMING:
				case FALL_FLYING:
					matrixStackIn.mulPose(Vector3f.XP.rotation(body.xRotO));
					matrixStackIn.translate(0D, body.getType().getHeight() * -0.5D, 0D);
					break;
				case SLEEPING:
					matrixStackIn.translate(body.getType().getHeight() * 0.5D, 0D, 0D);
					break;
				default:
					break;
			}
			matrixStackIn.pushPose();
				EntityRenderer<LivingEntity> renderer = (EntityRenderer<LivingEntity>)Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(body);
				if(entityIn.isPlayer())
				{
					try
					{
						EntityDummyBiped dummy = (EntityDummyBiped)body;
						dummy.setGameProfile(entityIn.getGameProfile());
						UUID playerID = entityIn.getGameProfile().getId();
						PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(playerID);
						String skinType = playerInfo == null ? DefaultPlayerSkin.getSkinModelName(playerID) : playerInfo.getModelName();
						
						if(skinType.equalsIgnoreCase("slim"))
							this.playerRendererThin.render(dummy, entityIn.yBodyRot, 0F, matrixStackIn, bufferIn, packedLightIn);
						else
							this.playerRendererThick.render(dummy, entityIn.yBodyRot, 0F, matrixStackIn, bufferIn, packedLightIn);
					}
					catch(Exception e) { }
				}
				else if(renderer != null)
					renderer.render(body, entityYaw, 0F, matrixStackIn, bufferIn, packedLightIn);
			matrixStackIn.popPose();
		matrixStackIn.popPose();
	}
	
	protected void poseEntity(LivingEntity body, Random rand)
	{
//		body.rotationYawHead = rand.nextFloat();
//		body.rotationPitch = rand.nextFloat();
		
		body.animationPosition = rand.nextFloat();
		body.animationSpeed = (rand.nextFloat() - 0.5F) * 1.5F;
		body.animationSpeedOld = body.animationSpeed + (rand.nextFloat() - 0.5F) * 0.01F;
		
		body.swinging = true;
		body.swingTime = rand.nextInt(6);
		body.attackAnim = rand.nextFloat();
		body.oAttackAnim = body.attackAnim + (rand.nextFloat() - 0.5F) * 0.01F;
	}
}
