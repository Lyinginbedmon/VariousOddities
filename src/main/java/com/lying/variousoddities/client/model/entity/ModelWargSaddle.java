package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;
import java.util.Arrays;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.mount.EntityWarg;

import net.minecraft.client.renderer.entity.model.TintedAgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelWargSaddle extends TintedAgeableModel<EntityWarg>
{
    public ModelRenderer body;
    public ModelRenderer mane;
    public ModelRenderer mane2;
    
    public ModelRenderer saddle;
    
    public ModelWargSaddle()
    {
    	this(0F);
    }
    
    public ModelWargSaddle(float scaleIn)
    {
    	this.textureWidth = 64;
    	this.textureHeight = 32;
    	
    	this.saddle = ModelUtils.freshRenderer(this);
    	this.saddle.setRotationPoint(0F, 11F, 2F);
    	this.saddle.setTextureOffset(0, 0).addBox(-3F, 0F, -1F, 6, 6, 6, scaleIn);
    	this.saddle.setTextureOffset(24, 0).addBox(-3.5F, -1F, -2F, 7, 6, 1, scaleIn);
    }
    
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return Arrays.asList(this.saddle);
	}
	
	protected Iterable<ModelRenderer> getHeadParts(){ return new ArrayList<ModelRenderer>(); }
	
	public void setLivingAnimations(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.setLivingAnimations(entityIn, limbSwing, limbSwingAmount, partialTickTime);
		
        if(entityIn.isEntitySleeping())
        {
            this.saddle.setRotationPoint(0.0F, 15.0F, 2.0F);
            this.saddle.rotateAngleX = -((float)Math.PI / 4F);
        }
        else
        {
        	this.saddle.setRotationPoint(0.0F, 11.0F, 2.0F);
            this.saddle.rotateAngleX = 0F;
        }
        
        this.saddle.rotateAngleZ = entityIn.getShakeAngle(partialTickTime, -0.16F);
	}
	
	public void setRotationAngles(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		
	}
}
