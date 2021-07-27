package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.entity.mount.EntityWarg;

public class ModelWargChest extends ModelMountChest<EntityWarg>
{
	public ModelWargChest(float scaleIn)
	{
		super(scaleIn);
		
		this.chestL.rotationPointZ = this.chestR.rotationPointZ = 9F;
	}
	
	public void setLivingAnimations(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTickTime);
		if(entityIn.isEntitySleeping())
		{
			body.setRotationPoint(0.0F, 33.0F, -1.0F);
			body.rotateAngleX = -((float)Math.PI / 4F);
			this.chestL.rotationPointY = this.chestR.rotationPointY = -7F;
		}
        else
        {
            body.setRotationPoint(0.0F, 26.0F, 2.0F);
            body.rotateAngleX = 0F;
    		this.chestL.rotationPointY = this.chestR.rotationPointY = -7F;
        }
		
        body.rotateAngleZ = entityIn.getShakeAngle(partialTickTime, -0.16F);
	}
}
