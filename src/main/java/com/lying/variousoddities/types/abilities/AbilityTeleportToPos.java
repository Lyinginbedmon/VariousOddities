package com.lying.variousoddities.types.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;

public class AbilityTeleportToPos extends ActivatedAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "teleport_to_position");
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	private double maxRange;
	
	protected AbilityTeleportToPos(double range)
	{
		super(Reference.Values.TICKS_PER_SECOND * 6);
		this.maxRange = range;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putDouble("Range", this.maxRange);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.maxRange = compound.contains("Range", 6) ? compound.getDouble("Range") : 16D;
	}
	
	public Ability.Type getType(){ return Ability.Type.UTILITY; }
	
	public boolean canTrigger(LivingEntity entity)
	{
		RayTraceResult trace = entity.pick(maxRange, 0F, true);
		return super.canTrigger(entity) && trace.getType() == RayTraceResult.Type.BLOCK;
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		RayTraceResult trace = entity.pick(maxRange, 0F, true);
		if(trace.getType() == RayTraceResult.Type.BLOCK)
		{
			Vector3d hitVec = trace.getHitVec();
			World world = entity.getEntityWorld();
			world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1F, 0.2F + entity.getRNG().nextFloat() * 0.8F);
				entity.setPositionAndUpdate(hitVec.getX(), hitVec.getY(), hitVec.getZ());
			world.playSound(null, hitVec.getX(), hitVec.getY(), hitVec.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1F, 0.2F + entity.getRNG().nextFloat() * 0.8F);
		}
		
		if(side != Dist.CLIENT)
			putOnCooldown(entity);
	}
	
	public int getCooldown(){ return Reference.Values.TICKS_PER_SECOND * 6; }
	
	public static class Builder extends Ability.Builder
	{
		public Ability create(CompoundNBT compound)
		{
			return new AbilityTeleportToPos(compound.contains("Range", 6) ? compound.getDouble("Range") : 16D);
		}
	}
}
