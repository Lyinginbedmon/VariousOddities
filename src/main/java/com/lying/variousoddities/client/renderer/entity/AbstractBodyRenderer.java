package com.lying.variousoddities.client.renderer.entity;

import java.util.Random;
import java.util.UUID;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.EntityDummyBiped;
import com.lying.variousoddities.init.VOEntities;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3f;

public abstract class AbstractBodyRenderer extends LivingRenderer<AbstractBody, BipedModel<AbstractBody>>
{
	private final EntityDummyBipedRenderer playerRendererThin;
	private final EntityDummyBipedRenderer playerRendererThick;
	
	public AbstractBodyRenderer(EntityRendererManager rendererManager, BipedModel<AbstractBody> modelIn, float shadowSize)
	{
		super(rendererManager, modelIn, shadowSize);
		this.playerRendererThin = new EntityDummyBipedRenderer(rendererManager, true);
		this.playerRendererThick = new EntityDummyBipedRenderer(rendererManager, false);
	}
	
	protected boolean canRenderName(AbstractBody entityIn)
    {
		return super.canRenderName(entityIn) && (entityIn.getAlwaysRenderNameTagForRender() || entityIn.hasCustomName() && entityIn == this.renderManager.pointedEntity);
    }
	
	@SuppressWarnings({ "unchecked" })
	public void render(AbstractBody entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		if(!entityIn.hasBody())
		{
			super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
			return;
		}
		
		LivingEntity body = entityIn.getBodyForRender();
		poseEntity(body, new Random(entityIn.getUniqueID().getLeastSignificantBits()));
		
		matrixStackIn.push();
			matrixStackIn.rotate(Vector3f.YP.rotation(entityIn.rotationYaw));
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
					matrixStackIn.rotate(Vector3f.XP.rotation(body.rotationPitch));
					matrixStackIn.translate(0D, body.getType().getHeight() * -0.5D, 0D);
					break;
				case SLEEPING:
					matrixStackIn.translate(body.getType().getHeight() * 0.5D, 0D, 0D);
					break;
				default:
					break;
			}
			matrixStackIn.push();
				EntityRenderer<LivingEntity> renderer = (EntityRenderer<LivingEntity>)Minecraft.getInstance().getRenderManager().getRenderer(body);
				if(entityIn.isPlayer())
				{
					try
					{
						EntityDummyBiped dummy = (EntityDummyBiped)body;
						dummy.setGameProfile(entityIn.getGameProfile());
						UUID playerID = entityIn.getGameProfile().getId();
						NetworkPlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(playerID);
						String skinType = playerInfo == null ? DefaultPlayerSkin.getSkinType(playerID) : playerInfo.getSkinType();
						
						if(skinType.equalsIgnoreCase("slim"))
							this.playerRendererThin.render(dummy, entityIn.rotationYaw, 0F, matrixStackIn, bufferIn, packedLightIn);
						else
							this.playerRendererThick.render(dummy, entityIn.rotationYaw, 0F, matrixStackIn, bufferIn, packedLightIn);
					}
					catch(Exception e) { }
				}
				else if(renderer != null)
					renderer.render(body, entityYaw, 0F, matrixStackIn, bufferIn, packedLightIn);
			matrixStackIn.pop();
		matrixStackIn.pop();
	}
	
	protected void poseEntity(LivingEntity body, Random rand)
	{
//		body.rotationYawHead = rand.nextFloat();
//		body.rotationPitch = rand.nextFloat();
		
		body.limbSwing = rand.nextFloat();
		body.limbSwingAmount = (rand.nextFloat() - 0.5F) * 1.5F;
		body.prevLimbSwingAmount = body.limbSwingAmount + (rand.nextFloat() - 0.5F) * 0.01F;
		
		body.isSwingInProgress = true;
		body.swingProgressInt = rand.nextInt(6);
		body.swingProgress = rand.nextFloat();
		body.prevSwingProgress = body.swingProgress + (rand.nextFloat() - 0.5F) * 0.01F;
	}
}
