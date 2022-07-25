package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AbilityBlindsight extends AbilityVision
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "blindsight");
	
	private AbilityBlindsight()
	{
		this(0D);
	}
	
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
	
	public Component translatedName(){ return Component.translatable("ability."+Reference.ModInfo.MOD_ID+".blindsight", (int)range); }
	
	public boolean testEntity(Entity entity, LivingEntity player)
	{
		if(canAbilityAffectEntity(entity, player))
			return false;
		
		Vec3 eyePos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
		for(int i=5; i>0; i--)
		{
			Vec3 pos = new Vec3(entity.getX(), entity.getY() + (double)i / 5 * entity.getBbHeight(), entity.getZ());
			if(player.getLevel().clip(new ClipContext(eyePos, pos, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player)).getType() == BlockHitResult.Type.MISS)
				return true;
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			Ability ability = new AbilityBlindsight();
			ability.readFromNBT(compound);
			return ability;
		}
	}
}
