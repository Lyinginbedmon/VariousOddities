package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.entity.mount.EntityWarg;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class ModelWargChest extends ModelMountChest<EntityWarg>
{
	public ModelWargChest(ModelPart partsIn)
	{
		super(partsIn);
		
		this.chestL.z = this.chestR.z = 9F;
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation, float sizeIn)
	{
		return ModelMountChest.createBodyLayer(deformation, sizeIn);
	}
	
	public void prepareMobModel(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTickTime);
		if(entityIn.isOrderedToSit())
		{
			body.setPos(0F, 33F, -1F);
			body.xRot = -((float)Math.PI / 4F);
			this.chestL.y = this.chestR.y = -7F;
		}
        else
        {
            body.setPos(0F, 26F, 2F);
            body.xRot = 0F;
    		this.chestL.y = this.chestR.y = -7F;
        }
		
        body.zRot = entityIn.getShakeAngle(partialTickTime, -0.16F);
	}
}
