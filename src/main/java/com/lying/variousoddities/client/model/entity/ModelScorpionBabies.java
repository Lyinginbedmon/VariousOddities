package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractScorpion;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelScorpionBabies extends EntityModel<AbstractScorpion>
{
	List<ModelRenderer> babies = new ArrayList<ModelRenderer>();
	
	public ModelScorpionBabies()
	{
		this.textureHeight = 16;
		this.textureWidth = 16;
		
		babies.add(makeBaby(this, -3F, 0F, 4.5F));
		babies.add(makeBaby(this, 4.5F, 0F, 8F));
		babies.add(makeBaby(this, -1F, 0F, 12F));
		babies.add(makeBaby(this, 2.5F, 0F, 0F));
		babies.add(makeBaby(this, -1.2F, 0F, -4F));
		babies.add(makeBaby(this, 0.5F, 0F, 6F));
		babies.add(makeBaby(this, 3F, 0F, 13F));
		babies.add(makeBaby(this, -4.5F, 0F, 12.5F));
		babies.add(makeBaby(this, -4.5F, 0F, -1F));
	}
	
    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
    	for(ModelRenderer baby : babies)
    		baby.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
    
    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	float rot = (float)Math.sin(entityIn.ticksExisted / 10F) * ModelUtils.toRadians(4D);
    	int index = 0;
    	for(ModelRenderer baby : babies)
    	{
    		float polarity = (++index%2 == 0 ? 1F : -1F);
    		baby.rotateAngleY = rot * polarity * (index%3 == 0 ? 1 : 2);
    		baby.rotateAngleY += ModelUtils.toRadians(5D) * index * polarity;
    		baby.rotateAngleY += ModelUtils.toRadians(140D) * (index%4 == 0 ? 1F : 0F);
    	}
    }
    
    private ModelRenderer makeBaby(Model model, float rpointX, float rpointY, float rpointZ)
    {
    	rpointX = Math.max(-4.5F, Math.min(4.5F, rpointX));
    	rpointZ = Math.max(-4.5F, Math.min(13F, rpointZ));
    	
    	ModelRenderer baby = ModelUtils.freshRenderer(model);
    	baby.setRotationPoint(rpointX, 13.9F, rpointZ);
    	baby.setTextureOffset(0, 0).addBox(-1.5F, -1F, -2.5F, 3, 1, 4, -0.3F);
    	return baby;
    }
}
