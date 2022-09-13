package com.lying.variousoddities.client.model.entity;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelPatronWitchChangeling extends ModelChangeling<EntityPatronWitch>
{
	private ModelPart[] tail = new ModelPart[4];
	private ModelPart miniArmLeft, miniArmRight;
	private ModelPart miniHandLeft, miniHandRight;
	
	public ModelPatronWitchChangeling(ModelPart partsIn)
	{
		super(partsIn);
        
		this.tail = new ModelPart[]
				{
					this.body.getChild("tail"),
					this.body.getChild("tail").getChild("child"),
					this.body.getChild("tail").getChild("child").getChild("child"),
					this.body.getChild("tail").getChild("child").getChild("child").getChild("child")
				};
        
        this.miniArmRight = this.body.getChild("right_mini_arm");
        this.miniHandRight = this.miniArmRight.getChild("hand");
        
        this.miniArmLeft = this.body.getChild("left_mini_arm");
        this.miniHandLeft = this.miniArmLeft.getChild("hand");
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = ModelChangeling.createMesh(deformation);
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO)
			.addOrReplaceChild("main", CubeListBuilder.create()
	        	.texOffs(0, 0).addBox(-3F, -8.5F, 0F, 6, 5, 10, deformation.extend(0.1F))
	        	.texOffs(0, 15).addBox(-2F, -8F, -1.5F, 4, 1, 2)
	        	.texOffs(-7, 18).addBox(-6F, -6F, 2.5F, 12, 0, 7), PartPose.rotation(ModelUtils.toRadians(17D), 0F, 0F));
		
		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
		
		PartDefinition miniArmRight = body.addOrReplaceChild("right_mini_arm", CubeListBuilder.create().texOffs(26, 15).addBox(-1F, -3F, -0.5F, 2, 3, 1), PartPose.offsetAndRotation(-1.5F, 6F, -2F, 0F, 0F, ModelUtils.toRadians(-5D)));
        	miniArmRight.addOrReplaceChild("hand", CubeListBuilder.create().texOffs(20, 15).addBox(-1F, 0F, -0.5F, 2, 2, 1, deformation.extend(0.1F)), PartPose.offset(0F, -3F, 0F));
        
        PartDefinition miniArmLeft = body.addOrReplaceChild("left_mini_arm", CubeListBuilder.create().mirror().texOffs(26, 15).addBox(-1F, -3F, -0.5F, 2, 3, 1), PartPose.offsetAndRotation(1.5F, 6F, -2F, 0F, 0F, ModelUtils.toRadians(5D)));
        	miniArmLeft.addOrReplaceChild("hand", CubeListBuilder.create().mirror().texOffs(20, 15).addBox(-1F, 0F, -0.5F, 2, 2, 1, deformation.extend(0.1F)), PartPose.offset(0F, -3F, 0F));
        
        PartDefinition tail0 = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(32, 0).addBox(-0.5F, 0F, -0.5F, 1, 7, 1), PartPose.offset(0F, 11F, 1.5F));
        PartDefinition tail1 = tail0.addOrReplaceChild("child", CubeListBuilder.create().texOffs(32, 8).addBox(-0.5F, 0F, -0.5F, 1, 7, 1), PartPose.offset(0F, 7F, 0F));
        PartDefinition tail2 = tail1.addOrReplaceChild("child", CubeListBuilder.create().texOffs(32, 16).addBox(-0.5F, 0F, -0.5F, 1, 7, 1), PartPose.offset(0F, 7F, 0F));
        	tail2.addOrReplaceChild("child", CubeListBuilder.create().texOffs(32, 24).addBox(-0.5F, 0F, -0.5F, 1, 7, 1).texOffs(22, -5).addBox(0F, 4F, -1.5F, 0, 7, 5), PartPose.offset(0F, 7F, 0F));
        
		return LayerDefinition.create(mesh, 32, 64);
	}
    
	public void setupAnim(EntityPatronWitch entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
		super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	
    	tail[0].zRot = ModelUtils.toRadians(45D * Math.sin(ageInTicks / 29));
    	for(ModelPart tailPart : tail)
    		tailPart.xRot = ModelUtils.toRadians(25D + (1D + Math.sin(ageInTicks / 12)) * 20D);
    	
    	miniArmRight.xRot = ModelUtils.toRadians(35D + 10D * (1D + Math.sin(ageInTicks / 23)));
    	miniHandRight.xRot = -miniArmRight.xRot;
    	
    	miniArmLeft.xRot = ModelUtils.toRadians(35D + 10D * (1D + Math.sin(ageInTicks / 19)));
    	miniHandLeft.xRot = -miniArmLeft.xRot;
    }
}
