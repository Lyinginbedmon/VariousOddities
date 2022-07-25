package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;

public class AbilityTeleportToPos extends ActivatedAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "teleport_to_position");
	
	private double maxRange;
	
	public AbilityTeleportToPos(double range)
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_SECOND * 6);
		this.maxRange = range;
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		compound.putDouble("Range", this.maxRange);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		super.readFromNBT(compound);
		this.maxRange = compound.contains("Range", 6) ? compound.getDouble("Range") : 16D;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityTeleportToPos teleport = (AbilityTeleportToPos)abilityIn;
		return teleport.maxRange < maxRange ? 1 : teleport.maxRange > maxRange ? -1 : 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.SPELL_LIKE; }
	
	public Ability.Type getType(){ return Ability.Type.UTILITY; }
	
	public boolean canTrigger(LivingEntity entity)
	{
		HitResult trace = entity.pick(maxRange, 0F, true);
		return super.canTrigger(entity) && trace.getType() == HitResult.Type.BLOCK;
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		HitResult trace = entity.pick(maxRange, 0F, true);
		if(trace.getType() == HitResult.Type.BLOCK)
		{
			Level world = entity.getLevel();
			
			Vec3 hitVec = trace.getLocation();
			Vec3 destination = hitVec;
			if(!isValidTeleportTarget(destination, world, entity))
			{
				// If the target point is invalid, search for a valid point in its vicinity, getting progressively further out
				RandomSource rand = entity.getRandom();
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
			}
			
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 0.2F + entity.getRandom().nextFloat() * 0.8F);
			
			EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(entity, destination.x, destination.y, destination.z);
			if(MinecraftForge.EVENT_BUS.post(event))
			{
				putOnCooldown(entity);
				return;
			}
			
			entity.setPos(destination.x, destination.y, destination.z);
			entity.fallDistance = 0F;
			world.playSound(null, destination.x, destination.y, destination.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 0.2F + entity.getRandom().nextFloat() * 0.8F);
		}
		
		if(side != Dist.CLIENT)
			putOnCooldown(entity);
	}
	
	private boolean isValidTeleportTarget(Vec3 vec, Level world, LivingEntity entity)
	{
		AABB boundsAt = entity.getBoundingBox().move(vec);
		BlockPos blockPos = new BlockPos(vec.x, vec.y, vec.z);
		return world.noCollision(entity, boundsAt) && !world.getBlockState(blockPos).getMaterial().blocksMotion() && world.getBlockState(blockPos.below()).getMaterial().blocksMotion();
	}
	
	public int getCooldown(){ return Reference.Values.TICKS_PER_SECOND * 6; }
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityTeleportToPos(compound.contains("Range", 6) ? compound.getDouble("Range") : 16D);
		}
	}
}
