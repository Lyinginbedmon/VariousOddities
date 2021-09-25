package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;

import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelPatronWitchChangeling extends ModelChangeling<EntityPatronWitch>
{
	ModelRenderer[] tail = new ModelRenderer[4];
	ModelRenderer miniArmLeft, miniArmRight;
	ModelRenderer miniHandLeft, miniHandRight;
	
	public ModelPatronWitchChangeling()
	{
		super();
		this.textureHeight = 32;
		this.textureWidth = 64;
		
        this.bipedHead = ModelUtils.freshRenderer(this);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        	ModelRenderer head1 = ModelUtils.freshRenderer(this);
        	head1.rotateAngleX = ModelUtils.toRadians(17D);
        	head1.setTextureOffset(0, 0).addBox(-3F, -8.5F, 0F, 6, 5, 10, 0.1F);
        	head1.setTextureOffset(0, 15).addBox(-2F, -8F, -1.5F, 4, 1, 2);
        	head1.setTextureOffset(-7, 18).addBox(-6F, -6F, 2.5F, 12, 0, 7);
        	this.bipedHead.addChild(head1);
        
        this.bipedBody = ModelUtils.freshRenderer(this);
        for(int i=0; i<4; i++)
        {
            ModelRenderer tailPart = ModelUtils.freshRenderer(this);
            tailPart.setRotationPoint(0F, 7F, 0F);
            tailPart.setTextureOffset(32, 8*i).addBox(-0.5F, 0F, -0.5F, 1, 7, 1);
            tail[i] = tailPart;
            if(i>0) tail[i-1].addChild(tailPart);
        }
        tail[0].setRotationPoint(0F, 11F, 1.5F);
        this.bipedBody.addChild(tail[0]);
        tail[tail.length - 1].setTextureOffset(22, -5).addBox(0F, 4F, -1.5F, 0, 7, 5);
        
        miniArmRight = ModelUtils.freshRenderer(this);
        miniArmRight.setRotationPoint(-1.5F, 6F, -2F);
        miniArmRight.rotateAngleZ = -ModelUtils.toRadians(5D);
        miniArmRight.setTextureOffset(26, 15).addBox(-1F, -3F, -0.5F, 2, 3, 1);
        this.bipedBody.addChild(miniArmRight);
        
        miniHandRight = ModelUtils.freshRenderer(this);
        miniHandRight.setRotationPoint(0F, -3F, 0F);
        miniHandRight.setTextureOffset(20, 15).addBox(-1F, 0F, -0.5F, 2, 2, 1, 0.1F);
        miniArmRight.addChild(miniHandRight);
        
        miniArmLeft = ModelUtils.freshRenderer(this);
        miniArmLeft.setRotationPoint(1.5F, 6F, -2F);
        miniArmLeft.mirror = true;
        miniArmLeft.rotateAngleZ = ModelUtils.toRadians(5D);
        miniArmLeft.setTextureOffset(26, 15).addBox(-1F, -3F, -0.5F, 2, 3, 1);
        this.bipedBody.addChild(miniArmLeft);
        
        miniHandLeft = ModelUtils.freshRenderer(this);
        miniHandLeft.setRotationPoint(0F, -3F, 0F);
        miniHandLeft.mirror = true;
        miniHandLeft.setTextureOffset(20, 15).addBox(-1F, 0F, -0.5F, 2, 2, 1, 0.1F);
        miniArmLeft.addChild(miniHandLeft);
	}
    
	public void setRotationAngles(EntityPatronWitch entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
		super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	tail[0].rotateAngleZ = ModelUtils.toRadians(45D * Math.sin(ageInTicks / 29));
    	for(ModelRenderer tailPart : tail)
    		tailPart.rotateAngleX = ModelUtils.toRadians(25D + (1D + Math.sin(ageInTicks / 12)) * 20D);
    	
    	miniArmRight.rotateAngleX = ModelUtils.toRadians(35D + 10D * (1D + Math.sin(ageInTicks / 23)));
    	miniHandRight.rotateAngleX = -miniArmRight.rotateAngleX;
    	
    	miniArmLeft.rotateAngleX = ModelUtils.toRadians(35D + 10D * (1D + Math.sin(ageInTicks / 19)));
    	miniHandLeft.rotateAngleX = -miniArmLeft.rotateAngleX;
    }
}
