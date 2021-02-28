package com.lying.variousoddities.entity.ai.group;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Basic group controller.<br>
 * Tracks members and aggressors as well as the last time and place they were seen.<br>
 * Serves to enable group action with limited world information.
 * @author Lying
 */
public class EntityGroup
{
	/** Map of members to their last sighting */
	protected Map<MobEntity, Sighting> members = new HashMap<>();
	/** Map of targets to their last sighting */
	protected Map<LivingEntity, Sighting> targets = new HashMap<>();
	
	public void addTarget(LivingEntity entity)
	{
		if(entity == null || !entity.isAlive()) return;
		targets.put(entity, new Sighting(entity));
	}
	
	public void removeTarget(LivingEntity entity)
	{
		targets.remove(entity);
	}
	
	public void addMember(MobEntity entity)
	{
		if(entity == null || !entity.isAlive()) return;
		members.put(entity, new Sighting(entity));
	}
	
	public void removeMember(MobEntity entity)
	{
		members.remove(entity);
	}
	
	public final boolean isMember(MobEntity entity){ return members.containsKey(entity); }
	public final boolean isTarget(LivingEntity entity){ return targets.containsKey(entity); }
	public boolean isTracking(LivingEntity entity){ return (entity instanceof MobEntity && isMember((MobEntity)entity)) || isTarget(entity); }
	
	/** Returns true if any member is in a loaded chunk */
	public boolean isLoaded()
	{
		for(MobEntity entity : members.keySet())
			if(entity.isAlive() && entity.getEntityWorld().isAreaLoaded(entity.getPosition(), 1))
				return true;
		return false;
	}
	
	/**
	 * Returns true if any living member of this group can see the given entity.
	 */
	public boolean isObserved(LivingEntity entity)
	{
		for(MobEntity member : members.keySet())
			if(member != entity && member.isAlive() && member.getEntitySenses().canSee(entity))
				return true;
		return false;
	}
	
	/**
	 * Returns true if any living member of this group can see the given position
	 */
	public boolean isObserved(BlockPos pos)
	{
		for(MobEntity member : members.keySet())
			if(member.isAlive())
			{
			    Vector3d vector3d = new Vector3d(member.getPosX(), member.getPosYEye(), member.getPosZ());
			    Vector3d vector3d1 = new Vector3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
			    if(member.getEntityWorld().rayTraceBlocks(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, member)).getType() == RayTraceResult.Type.MISS)
			    	return true;
			}
		return false;
	}
	
	/** Called frequently manage the sightings of members and targets */
	public void updateObservations()
	{
		for(LivingEntity target : targets.keySet())
			if(isObserved(target))
				targets.get(target).update(target);
		
		for(LivingEntity member : members.keySet())
			if(isObserved(member))
				members.get(member).update(member);
	}
	
	/** Called every tick by the world to update group logic */
	public void tick()
	{
		if(members.isEmpty() || targets.isEmpty())
			GroupHandler.removeGroup(this);
		else
			updateObservations();
	}
	
	/** Called whenever a member is injured, such as to add new targets */
	public void onMemberHarmed(LivingHurtEvent event, MobEntity victim, LivingEntity attacker)
	{
		if(isObserved(victim))
			addTarget(attacker);
	}
	
	/**
	 * Called when an entity relevant to this group is killed (usually a member or aggressor).<br>
	 * Typically this is done to drop members/targets that have died.
	 */
	public void onEntityKilled(LivingDeathEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		if(isTarget(living))
			removeTarget(living);
		else if(living instanceof MobEntity && isMember((MobEntity)living))
			removeMember((MobEntity)living);
	}
	
	/**
	 * Holder class for group awareness of a given entity.<br>
	 * Used to identify missing members and aggressors that might be sneaking around.
	 * @author Lying
	 */
	public class Sighting
	{
		private long time = 0;
		private BlockPos location = BlockPos.ZERO;
		
		public Sighting(LivingEntity entity)
		{
			time = entity.getEntityWorld().getGameTime();
			location = entity.getPosition();
		}
		
		public long age(long gameTime){ return gameTime - time; }
		public BlockPos location(){ return location; }
		
		public void update(LivingEntity target)
		{
			time = target.getEntityWorld().getGameTime();
			location = target.getPosition();
		}
		
		public void updateWith(LivingEntity target, List<MobEntity> observers)
		{
			for(MobEntity observer : observers)
			{
				if(observer.getEntitySenses().canSee(target))
				{
					update(target);
					return;
				}
			}
		}
	}
}
