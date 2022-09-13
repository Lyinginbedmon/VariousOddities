package com.lying.variousoddities.client.model.entity;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.entity.mount.EntityWarg;

import net.minecraft.client.model.ColorableAgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelWargSaddle extends ColorableAgeableListModel<EntityWarg>
{
    public ModelPart saddle;
    
    public ModelWargSaddle(ModelPart partsIn)
    {
    	this.saddle = partsIn.getChild("saddle");
    }
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation, float scaleIn)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild("saddle", CubeListBuilder.create()
	    	.texOffs(0, 0).addBox(-3F, 0F, -1F, 6, 6, 6, deformation.extend(scaleIn))
	    	.texOffs(24, 0).addBox(-3.5F, -1F, -2F, 7, 6, 1, deformation.extend(scaleIn)), PartPose.offset(0F, 11F, 2F));
		
		return LayerDefinition.create(mesh, 64, 32);
	}
    
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.saddle);
	}
	
	protected Iterable<ModelPart> headParts(){ return ImmutableList.of(); }
	
	public void prepareMobModel(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float partialTickTime)
	{
		super.prepareMobModel(entityIn, limbSwing, limbSwingAmount, partialTickTime);
		
        if(entityIn.isSleeping())
        {
            this.saddle.setPos(0.0F, 15.0F, 2.0F);
            this.saddle.xRot = -((float)Math.PI / 4F);
        }
        else
        {
        	this.saddle.setPos(0.0F, 11.0F, 2.0F);
            this.saddle.xRot = 0F;
        }
        
        this.saddle.zRot = entityIn.getShakeAngle(partialTickTime, -0.16F);
	}
	
	public void setupAnim(EntityWarg entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){ }
}
