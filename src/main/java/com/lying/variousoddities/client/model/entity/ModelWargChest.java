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
		if(entityIn.isOrderedToSit())
		{
			body.setPos(0.0F, 33.0F, -1.0F);
			body.xRot = -((float)Math.PI / 4F);
			this.chestL.y = this.chestR.y = -7F;
		}
        else
        {
            body.setPos(0.0F, 26.0F, 2.0F);
            body.xRot = 0F;
    		this.chestL.y = this.chestR.y = -7F;
        }
		
        body.zRot = entityIn.getShakeAngle(partialTickTime, -0.16F);
	}
}
