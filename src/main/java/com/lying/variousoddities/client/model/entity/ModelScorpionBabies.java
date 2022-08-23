package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractScorpion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ModelScorpionBabies extends EntityModel<AbstractScorpion>
{
	List<ModelPart> babies = new ArrayList<ModelPart>();
	
	public ModelScorpionBabies(ModelPart partsIn)
	{
		for(int i=0; i<9; i++)
			babies.add(partsIn.getChild("baby_"+i));
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		makeBaby(0, part, deformation, -3F, 0F, 4.5F);
		makeBaby(1, part, deformation, 4.5F, 0F, 8F);
		makeBaby(2, part, deformation, -1F, 0F, 12F);
		makeBaby(3, part, deformation, 2.5F, 0F, 0F);
		makeBaby(4, part, deformation, -1.2F, 0F, -4F);
		makeBaby(5, part, deformation, 0.5F, 0F, 6F);
		makeBaby(6, part, deformation, 3F, 0F, 13F);
		makeBaby(7, part, deformation, -4.5F, 0F, 12.5F);
		makeBaby(8, part, deformation, -4.5F, 0F, -1F);
		
		return LayerDefinition.create(mesh, 16, 16);
	}
	
    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
    	for(ModelPart baby : babies)
    		baby.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
    
    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setupAnim(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	float rot = (float)Math.sin(entityIn.tickCount / 10F) * ModelUtils.toRadians(4D);
    	int index = 0;
    	for(ModelPart baby : babies)
    	{
    		float polarity = (++index%2 == 0 ? 1F : -1F);
    		baby.yRot = rot * polarity * (index%3 == 0 ? 1 : 2);
    		baby.yRot += ModelUtils.toRadians(5D) * index * polarity;
    		baby.yRot += ModelUtils.toRadians(140D) * (index%4 == 0 ? 1F : 0F);
    	}
    }
    
    private static PartDefinition makeBaby(int index, PartDefinition model, CubeDeformation deformation, float rpointX, float rpointY, float rpointZ)
    {
    	rpointX = Math.max(-4.5F, Math.min(4.5F, rpointX));
    	rpointZ = Math.max(-4.5F, Math.min(13F, rpointZ));
    	return model.addOrReplaceChild("baby_"+index, CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1F, -2.5F, 3, 1, 4, deformation.extend(-0.3F)), PartPose.offset(rpointX, 13.9F, rpointZ));
    }
}
