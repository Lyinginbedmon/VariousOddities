package com.lying.variousoddities.client.model.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelPatronKirin extends BipedModel<EntityPatronKirin>
{
	ModelRenderer coatTail;
	
	ModelRenderer tail;
	List<ModelRenderer> tailSegments = Lists.newArrayList();
	
	public ModelPatronKirin()
	{
		super(0F, 0F, 64, 128);
		
		this.bipedHead.setTextureOffset(32, 0).addBox(-4F, -8F, -4F, 8, 8, 8, 0.25F);
		
		this.bipedHeadwear = ModelUtils.freshRenderer(this);
		this.bipedHeadwear.setTextureOffset(0, 86).addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
		
		this.bipedBody.setTextureOffset(16, 45).addBox(-4F, 0F, -2F, 8, 4, 4, 0.5F);
		
		this.coatTail = ModelUtils.freshRenderer(this);
		this.coatTail.setTextureOffset(16, 32).addBox(-4F, 0F, -2F, 8, 9, 4, 0.5F);
		this.coatTail.setRotationPoint(0F, 9.5F, 0.5F);
		this.coatTail.rotateAngleX = ModelUtils.toRadians(25.5D);
		this.bipedBody.addChild(this.coatTail);
		
			this.tail = ModelUtils.freshRenderer(this);
			this.tail.setRotationPoint(0F, 12F, 2F);
			this.tail.setTextureOffset(26, 53).addBox(-1F, -1F, -1F, 2, 6, 2, -0.2F);
			tailSegments.add(tail);
			
			for(int i=0; i<3; i++)
			{
				ModelRenderer segment = makeTailSegment(i);
				tailSegments.get(i).addChild(segment);
				tailSegments.add(segment);
			}
			tailSegments.get(tailSegments.size() - 2).setTextureOffset(34, 53).addBox(0F, 1F, 0F, 0, 5, 2);
			tailSegments.get(tailSegments.size() - 1).setTextureOffset(26, 64).addBox(-1.5F, 1.5F, -1.5F, 3, 7, 3, -0.25F);
			tailSegments.get(tailSegments.size() - 1).setTextureOffset(34, 60).addBox(0F, 1F, 0F, 0, 2, 2);
		this.bipedBody.addChild(tail);
		
		this.bipedRightArm.setTextureOffset(40, 32).addBox(-3F, -2F, -2F, 4, 4, 4, 0.5F);
		this.bipedLeftArm.setTextureOffset(0, 32).addBox(-1F, -2F, -2F, 4, 4, 4, 0.5F);
		
		this.bipedLeftLeg = ModelUtils.freshRenderer(this);
		this.bipedLeftLeg.setRotationPoint(2.4F, 12F, 0F);
			ModelRenderer thigh = ModelUtils.freshRenderer(this);
			thigh.mirror = true;
			thigh.setTextureOffset(0, 53).addBox(-2F, -2F, -6.5F, 4, 4, 9);
			thigh.rotateAngleX = ModelUtils.toRadians(35D);
			ModelRenderer ankle = ModelUtils.freshRenderer(this);
			ankle.mirror = true;
			ankle.setTextureOffset(0, 66).addBox(-1F, 3.5F, -2F, 2, 2, 7);
			ankle.rotateAngleX = ModelUtils.toRadians(-30D);
			ModelRenderer foot = ModelUtils.freshRenderer(this);
			foot.mirror = true;
			foot.setTextureOffset(0, 75).addBox(-1.5F, 2F, -11.8F, 3, 3, 8);
			foot.setTextureOffset(22, 76).addBox(-2.5F, 1.0F, -13F, 5, 5, 5, -0.5F);
			foot.rotateAngleX = ModelUtils.toRadians(70D);
		this.bipedLeftLeg.addChild(thigh);
		this.bipedLeftLeg.addChild(ankle);
		this.bipedLeftLeg.addChild(foot);
		
		this.bipedRightLeg = ModelUtils.freshRenderer(this);
		this.bipedRightLeg.setRotationPoint(-2.4F, 12F, 0F);
			thigh = ModelUtils.freshRenderer(this);
			thigh.setTextureOffset(0, 53).addBox(-2F, -2F, -6.5F, 4, 4, 9);
			thigh.rotateAngleX = ModelUtils.toRadians(35D);
			ankle = ModelUtils.freshRenderer(this);
			ankle.setTextureOffset(0, 66).addBox(-1F, 3.5F, -2F, 2, 2, 7);
			ankle.rotateAngleX = ModelUtils.toRadians(-30D);
			foot = ModelUtils.freshRenderer(this);
			foot.setTextureOffset(0, 75).addBox(-1.5F, 2F, -11.8F, 3, 3, 8);
			foot.setTextureOffset(22, 76).addBox(-2.5F, 1.0F, -13F, 5, 5, 5, -0.5F);
			foot.rotateAngleX = ModelUtils.toRadians(70D);
		this.bipedRightLeg.addChild(thigh);
		this.bipedRightLeg.addChild(ankle);
		this.bipedRightLeg.addChild(foot);
	}
    
    private ModelRenderer makeTailSegment(int tailPosition)
    {
    	ModelRenderer segment = ModelUtils.freshRenderer(this);
    	segment.mirror = (tailPosition % 2) == 0;
    	segment.setTextureOffset(26, 53).addBox(-1F, 0F, -1F, 2, 6, 2, -0.2F);
    	segment.rotationPointY = 3.5F + (1F*tailPosition);
    	segment.rotateAngleX = (1+tailPosition) * ModelUtils.degree10;
    	
    	return segment;
    }
    
    public void setRotationAngles(EntityPatronKirin entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        
    	this.coatTail.rotateAngleX = ModelUtils.toRadians(25.5D) + Math.max(this.bipedLeftLeg.rotateAngleX, this.bipedRightLeg.rotateAngleX);
    	
    	float time = ((float)Math.sin(ageInTicks / 10)) * 0.5F;
    	int i = 0;
    	for(ModelRenderer segment : tailSegments)
    	{
    		segment.rotateAngleY = time / 1.5F;
    		segment.rotateAngleX = (time * Math.signum(time)) / 8 + (ModelUtils.degree10 * i++);
    	}
    	this.tail.rotateAngleX += ModelUtils.toRadians(45D);
    }
}
