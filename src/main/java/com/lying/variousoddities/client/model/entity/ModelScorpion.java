package com.lying.variousoddities.client.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.ImmutableList;
import com.lying.variousoddities.client.model.EnumLimbPosition;
import com.lying.variousoddities.client.model.ModelUtils;
import com.lying.variousoddities.entity.AbstractScorpion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class ModelScorpion<T extends AbstractScorpion> extends AgeableListModel<T>
{
	ModelPart body;
	ClawHandler rightClaw, leftClaw;
	TailHandler tail;
	List<LegHandler> leftLegs = new ArrayList<LegHandler>();
	List<LegHandler> rightLegs = new ArrayList<LegHandler>();
	
	private static final float legOffset = 5F;
	
	public ModelScorpion(ModelPart partsIn)
	{
		this.body = partsIn.getChild("body");
		
		this.tail = new TailHandler(partsIn.getChild("tail"));
		
		this.rightClaw = new ClawHandler(EnumLimbPosition.RIGHT, partsIn.getChild("right_claw"));
		this.leftClaw = new ClawHandler(EnumLimbPosition.LEFT, partsIn.getChild("left_claw"));
		
		for(int i=-2; i<2; i++)
		{
			leftLegs.add(new LegHandler(i, EnumLimbPosition.LEFT, partsIn.getChild("leg_"+i+"_left")));
			rightLegs.add(new LegHandler(i, EnumLimbPosition.RIGHT, partsIn.getChild("leg_"+i+"_right")));
		}
	}
	
	public static LayerDefinition createBodyLayer(CubeDeformation deformation)
	{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		
		part.addOrReplaceChild("body", CubeListBuilder.create()
			.texOffs(14, 16).addBox(-5F, -3F, -9F, 2, 2, 1, deformation.extend(0.2F))
			.texOffs(14, 13).addBox(3.5F, -3F, -9F, 2, 2, 1, deformation.extend(0.2F)), PartPose.offset(0F, 18F, 0F))
			.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-3F, -13F, -1F, 6, 18, 1, deformation.extend(3.5F)), PartPose.rotation(ModelUtils.toRadians(-90D), 0F, 0F));
		
		TailHandler.addTail(part, deformation);
		
		ClawHandler.addClaw(EnumLimbPosition.RIGHT, part, deformation);
		ClawHandler.addClaw(EnumLimbPosition.LEFT, part, deformation);
		
		for(int i=-2; i<2; i++)
		{
			LegHandler.addLeg(i, EnumLimbPosition.LEFT, part, deformation);
			LegHandler.addLeg(i, EnumLimbPosition.RIGHT, part, deformation);
		}
		
		return LayerDefinition.create(mesh, 64, 32);
	}
    
    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setupAnim(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
    	rightClaw.swingProgress = leftClaw.swingProgress = tail.swingProgress = this.attackTime;
    	
    	tail.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	rightClaw.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	leftClaw.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	for(int i=0; i<leftLegs.size(); i++)
    	{
    		leftLegs.get(i).setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    		rightLegs.get(i).setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    	}
    }
    
	protected Iterable<ModelPart> headParts(){ return Collections.emptyList(); }
	
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(
				this.body, this.tail.getRoot(), 
				this.rightClaw.getClaw(), this.leftClaw.getClaw(),
				rightLegs.get(0).getLeg(), leftLegs.get(0).getLeg(), rightLegs.get(1).getLeg(), leftLegs.get(1).getLeg(), rightLegs.get(2).getLeg(), leftLegs.get(2).getLeg(), rightLegs.get(3).getLeg(), leftLegs.get(3).getLeg());
	}
    
    public static class ClawHandler
    {
    	public float swingProgress;
    	
    	private final EnumLimbPosition side;
    	private final boolean isLeft;
    	
    	private final ModelPart theArm;
    	private final ModelPart theClaw;
    	
    	public ClawHandler(EnumLimbPosition sideIn, ModelPart armIn)
    	{
    		this.side = sideIn;
    		this.isLeft = sideIn == EnumLimbPosition.LEFT;
    		this.theArm = armIn;
    		this.theClaw = armIn.getChild("claw");
    	}
    	
    	public static void addClaw(EnumLimbPosition sideIn, PartDefinition part, CubeDeformation deformation)
    	{
    		boolean isLeft = sideIn == EnumLimbPosition.LEFT;
    		
    		PartDefinition theArm = part.addOrReplaceChild(sideIn.name().toLowerCase()+"_arm", CubeListBuilder.create(), PartPose.offset(8F * (isLeft ? -1F : 1F), 16.5F, -7.5F));
    			CubeListBuilder armCubes = CubeListBuilder.create();
    			if(isLeft)
    				armCubes.mirror();
    			armCubes.texOffs(12, 19).addBox(-1.5F, -2.5F, -1.5F, 3, 7, 3);
    			theArm.addOrReplaceChild("child", armCubes, PartPose.ZERO);
    			CubeListBuilder clawCubes = CubeListBuilder.create();
    			if(isLeft)
    				clawCubes.mirror();
        		clawCubes.texOffs(12, 29).addBox(-2F, 0F, -2F, 4, 6, 4, deformation.extend(1F))
        			.texOffs(24, 24).addBox(2F * (isLeft ? 1F : -1.6F), -0.5F, -1F, 1, 3, 2, deformation.extend(0.75F));
    			theArm.addOrReplaceChild("claw", clawCubes, PartPose.offset(0F, 4F, 0F));
    	}
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
        public void setupAnim(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
            if(swingProgress > 0.0F && swingSideMatches(this.getMainHand(entityIn)))
        	{
            	theArm.xRot = -ModelUtils.degree90;
            	theArm.yRot = 0F;
            	theArm.zRot = 0F;
            	
	                float f1 = 1.0F - swingProgress;
	                f1 = f1 * f1;
	                f1 = f1 * f1;
	                f1 = 1.0F - f1;
	                float f2 = Mth.sin(f1 * (float)Math.PI);
	                float f3 = Mth.sin(swingProgress * (float)Math.PI) * 0.7F * 0.75F;
	                theArm.yRot = (float)((double)theArm.yRot - ((double)f2 * 1.2D + (double)f3)) * (isLeft ? -1F : 1F);
        	}
            else
            {
            	theClaw.zRot = ModelUtils.degree10 * 4F * (isLeft ? -1F : 1F);
            	
            	theArm.xRot = -ModelUtils.degree180 * 0.5F;
            	theArm.xRot += (float)Math.cos(entityIn.tickCount / 20F) * ModelUtils.degree10 * 0.5F;
            	theArm.yRot = ModelUtils.degree90 * 0.5F * (isLeft ? 1F : -1F);
            	theArm.zRot = 0F;
            }
        }
    	
    	public boolean swingSideMatches(HumanoidArm handIn)
    	{
    		if(handIn == HumanoidArm.LEFT && this.side == EnumLimbPosition.RIGHT){ return true; }
    		if(handIn == HumanoidArm.RIGHT && this.side == EnumLimbPosition.LEFT){ return true; }
    		return false;
    	}
    	
        protected HumanoidArm getMainHand(Entity entityIn)
        {
            if (entityIn instanceof LivingEntity)
            {
                LivingEntity entitylivingbase = (LivingEntity)entityIn;
                HumanoidArm enumhandside = entitylivingbase.getMainArm();
                return entitylivingbase.swingingArm == InteractionHand.MAIN_HAND ? enumhandside : enumhandside.getOpposite();
            }
            else
                return HumanoidArm.RIGHT;
        }
        
        public ModelPart getClaw(){ return this.theArm; }
    }
    
    public static class TailHandler
    {
    	public float swingProgress;
    	
    	List<ModelPart> tailSegments = Lists.newArrayList();
    	ModelPart tailRoot;
    	ModelPart tailStinger;
    	
    	public static void addTail(PartDefinition part, CubeDeformation deformation)
    	{
    		List<PartDefinition> segments = Lists.newArrayList();
    		PartDefinition tailRoot = part.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(16, 0).addBox(-2.5F, -1F, -2F, 5, 2, 3, deformation.extend(2F)), PartPose.offset(0F, 14.5F, 19F));
    		segments.add(tailRoot);
    		
    		for(int i=0; i<3; i++)
    			segments.add(
    				segments.get(i).addOrReplaceChild("child", 
    					CubeListBuilder.create().texOffs(14, 5).addBox(-2F, -0.5F, 0F, 4, 1, 5, deformation.extend(2F - (i * 0.6F))), 
    					PartPose.offset(0F, 0F, 8F - (i * 0.3F) - (i==0 ? 4F : 0F))));
    		
    		segments.get(2).addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(24, 11).addBox(-1F, 0F, -1F, 2, 4, 2, deformation.extend(0.25F)), PartPose.offset(0F, 0F, 6.5F));
    	}
    	
    	public TailHandler(ModelPart tailIn)
    	{
    		this.tailRoot = tailIn;
    		this.tailSegments.add(tailIn);
    		
    		for(int i=0; i<3; i++)
    			this.tailSegments.add(this.tailSegments.get(this.tailSegments.size()-1).getChild("child"));
    		this.tailStinger = this.tailSegments.get(this.tailSegments.size()-1).getChild("stinger");
    	}
    	
    	public ModelPart getRoot() { return this.tailRoot; }
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
        public void setupAnim(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        	if(this.swingProgress > 0.0F && !entityIn.isBaby())
        	{
        		this.swingProgress = 1 - (Math.abs(this.swingProgress-0.5F)*2F);
        		
        		float tailStart = ModelUtils.toRadians(48D);
        		float tailEnd = ModelUtils.toRadians(-5D);
        		for(ModelPart segment : tailSegments)
        		{
        			segment.xRot = tailStart + (tailEnd - tailStart)*this.swingProgress;
        		}

        		float rootStart = ModelUtils.toRadians(58D);
        		float rootEnd = ModelUtils.toRadians(180D);
        		tailRoot.xRot = rootStart + (rootEnd - rootStart)*this.swingProgress;
        		
        		tailRoot.y = 14.5F - (4F * this.swingProgress);
        		tailRoot.z = 19F - (6F * this.swingProgress);
        	}
        	else
        	{
        		this.tailRoot.setPos(0F, 14.5F, 19F);
        		
	        	float rotation = (float)Math.sin(ageInTicks / 20);
	        	rotation *= Math.signum(rotation);
	        	rotation = rotation*ModelUtils.toRadians(10D) + ModelUtils.toRadians(38D);
	        	for(ModelPart segment : tailSegments)
	        	{
	        		segment.xRot = rotation;
	        	}
        	}
        	tailStinger.xRot = ModelUtils.toRadians(180D);
        }
    	
    	public void render(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    	{
    		tailRoot.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    	}
    }
    
    public static class LegHandler
    {
    	private final int index;
    	private final EnumLimbPosition side;
    	private final boolean isLeft;
    	
    	private ModelPart theLegUpper;
    	private ModelPart theLegLower;
    	
    	public static void addLeg(int index, EnumLimbPosition side, PartDefinition part, CubeDeformation deformation)
    	{
    		boolean isLeft = side == EnumLimbPosition.LEFT;
    		float pol = (isLeft ? 1F : -1F);
    		
    		PartDefinition legUpper = part.addOrReplaceChild("leg_"+index+"_"+side.name().toLowerCase(), CubeListBuilder.create(), PartPose.offsetAndRotation((legOffset + (index%2 == 0 ? 1.5F : 0F)) * pol, 18F, 8F + index * 5.5F, 0F, -ModelUtils.toRadians(10D) * index * pol + (isLeft ? ModelUtils.toRadians(180D) : 0F), -ModelUtils.degree10 * pol));
    			legUpper.addOrReplaceChild("child", CubeListBuilder.create().texOffs(0, 19).addBox(0-1.5F, 0F, -1.5F, 3, 6, 3), PartPose.rotation(0F, 0F, -ModelUtils.degree90));
			
    		legUpper.addOrReplaceChild("lower", CubeListBuilder.create(), PartPose.offsetAndRotation(5F, 0F, 0F, 0F, 0F, ModelUtils.degree90 / 1.5F))
    			.addOrReplaceChild("child", CubeListBuilder.create().texOffs(0, 28).addBox(-1F, 0F, -1F, 2, 8, 2, deformation.extend(0.2F)), PartPose.rotation(0F, 0F, -ModelUtils.degree90));
    	}
    	
    	public LegHandler(int index, EnumLimbPosition side, ModelPart legUpper)
    	{
    		this.index = index;
    		this.side = side;
    		this.isLeft = this.side == EnumLimbPosition.LEFT;
    		this.theLegUpper = legUpper;
    		this.theLegLower = legUpper.getChild("lower");
    	}
    	
    	public ModelPart getLeg() { return this.theLegUpper; }
        
        /**
         * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
         * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
         * "far" arms and legs can swing at most.
         */
        public void setupAnim(AbstractScorpion entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
        {
        	float legBase = Mth.cos(limbSwing * 1.5F + (float)Math.PI) * 2F * limbSwingAmount;
        	boolean limbPolarity = (isLeft ? index%2 == 0 : index%2 != 0);
        	
    		theLegUpper.zRot = Math.min(-ModelUtils.degree10, (legBase * (limbPolarity ? 1 : -1)) - ModelUtils.degree10) * (isLeft ? 1F : -1F);
    		
    		theLegUpper.yRot = !isLeft ? ModelUtils.degree180 : 0F;
    		theLegUpper.yRot += (Mth.cos(limbSwing * 0.66682F + (float)Math.PI) * 0.8F * limbSwingAmount) * (limbPolarity ? 1F : -1F);
    		
    		theLegLower.zRot = ModelUtils.degree90 / 1.5F;
    		theLegLower.zRot -= ((((isLeft ? 1F : -1F )*ModelUtils.degree10) + theLegUpper.zRot) * 1F) * (isLeft ? 1F : -1F);
        }
    }
}
