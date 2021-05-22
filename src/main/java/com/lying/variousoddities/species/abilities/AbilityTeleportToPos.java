package com.lying.variousoddities.species.abilities;

import java.util.Random;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;

public class AbilityTeleportToPos extends ActivatedAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "teleport_to_position");
	
	private double maxRange;
	
	public AbilityTeleportToPos(double range)
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_SECOND * 6);
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
	
	protected Nature getDefaultNature(){ return Nature.SPELL_LIKE; }
	
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
			World world = entity.getEntityWorld();
			
			// True if no area at or in the vicinity of the target point was found to be safe to teleport to
			boolean failed = false;
			
			Vector3d hitVec = trace.getHitVec();
			Vector3d destination = hitVec;
			if(!isValidTeleportTarget(destination, world, entity))
			{
				// If the target point is invalid, search for a valid point in its vicinity, getting progressively further out
				Random rand = entity.getRNG();
				int range = 1;
				int attempts = 20;
				while(!isValidTeleportTarget(destination, world, entity) && attempts-- > 0)
				{
					double modX = (rand.nextDouble() * range * 2) - range;
					double modY = (rand.nextDouble() * range * 2) - range;
					double modZ = (rand.nextDouble() * range * 2) - range;
					destination = hitVec.add(modX, modY, modZ);
					range = 1 + (int)((1D - ((double)attempts / 20D)) * 5);
				}
				
				if(!isValidTeleportTarget(destination, world, entity))
					failed = true;
			}
			
			world.playSound(null, entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1F, 0.2F + entity.getRNG().nextFloat() * 0.8F);
			if(!failed)
			{
				entity.setPositionAndUpdate(destination.getX(), destination.getY(), destination.getZ());
				entity.fallDistance = 0F;
				world.playSound(null, destination.getX(), destination.getY(), destination.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1F, 0.2F + entity.getRNG().nextFloat() * 0.8F);
			}
		}
		
		if(side != Dist.CLIENT)
			putOnCooldown(entity);
	}
	
	private boolean isValidTeleportTarget(Vector3d vec, World world, LivingEntity entity)
	{
		AxisAlignedBB boundsAt = entity.getBoundingBox().offset(vec);
		BlockPos blockPos = new BlockPos(vec.x, vec.y, vec.z);
		return world.hasNoCollisions(entity, boundsAt) && !world.getBlockState(blockPos).getMaterial().blocksMovement() && world.getBlockState(blockPos.down()).getMaterial().blocksMovement();
	}
	
	public int getCooldown(){ return Reference.Values.TICKS_PER_SECOND * 6; }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityTeleportToPos(compound.contains("Range", 6) ? compound.getDouble("Range") : 16D);
		}
	}
}
