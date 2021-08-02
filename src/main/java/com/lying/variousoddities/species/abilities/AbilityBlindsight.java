package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AbilityBlindsight extends AbilityVision
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "blindsight");
	
	public AbilityBlindsight(double rangeIn)
	{
		super(REGISTRY_NAME, Math.max(4D, rangeIn));
	}
	
	public AbilityBlindsight(double rangeIn, double rangeMinIn)
	{
		this(rangeIn);
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityBlindsight sight = (AbilityBlindsight)abilityIn;
		return sight.range < this.range ? 1 : sight.range > this.range ? -1 : 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability."+Reference.ModInfo.MOD_ID+".blindsight", (int)range); }
	
	public boolean testEntity(Entity entity, LivingEntity player)
	{
		Vector3d eyePos = new Vector3d(player.getPosX(), player.getPosYEye(), player.getPosZ());
		for(int i=5; i>0; i--)
		{
			Vector3d pos = new Vector3d(entity.getPosX(), entity.getPosY() + (double)i / 5 * entity.getHeight(), entity.getPosZ());
			if(player.getEntityWorld().rayTraceBlocks(new RayTraceContext(eyePos, pos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, player)).getType() == BlockRayTraceResult.Type.MISS)
				return true;
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			double range = compound.contains("Max", 6) ? compound.getDouble("Max") : 16;
			return new AbilityBlindsight(range, compound.getDouble("Min"));
		}
	}
}
