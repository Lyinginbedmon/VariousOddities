package com.lying.variousoddities.entity.ai.group;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
	protected Map<Mob, Sighting> members = new HashMap<>();
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
	
	public void addMember(Mob entity)
	{
		if(entity == null || !entity.isAlive()) return;
		members.put(entity, new Sighting(entity));
	}
	
	public void removeMember(Mob entity)
	{
		members.remove(entity);
	}
	
	public final boolean isMember(Mob entity){ return members.containsKey(entity); }
	public final boolean isTarget(LivingEntity entity){ return targets.containsKey(entity); }
	public boolean isTracking(LivingEntity entity){ return (entity instanceof Mob && isMember((Mob)entity)) || isTarget(entity); }
	
	/** Returns true if any member is in a loaded chunk */
	@SuppressWarnings("deprecation")
	public boolean isLoaded()
	{
		for(Mob entity : members.keySet())
			if(entity.isAlive() && entity.getLevel().isAreaLoaded(entity.blockPosition(), 1))
				return true;
		return false;
	}
	
	/**
	 * Returns true if any living member of this group can see the given entity.
	 */
	public boolean isObserved(LivingEntity entity)
	{
		for(Mob member : members.keySet())
			if(member != entity && member.isAlive() && member.hasLineOfSight(entity))
				return true;
		return false;
	}
	
	/**
	 * Returns true if any living member of this group can see the given position
	 */
	public boolean isObserved(BlockPos pos)
	{
		for(Mob member : members.keySet())
			if(member.isAlive())
			{
			    Vec3 vector3d = new Vec3(member.getX(), member.getEyeY(), member.getZ());
			    Vec3 vector3d1 = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
			    if(member.getLevel().clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, member)).getType() == HitResult.Type.MISS)
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
	public void onMemberHarmed(LivingHurtEvent event, Mob victim, LivingEntity attacker)
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
		LivingEntity living = event.getEntity();
		if(isTarget(living))
			removeTarget(living);
		else if(living instanceof Mob && isMember((Mob)living))
			removeMember((Mob)living);
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
			time = entity.getLevel().getGameTime();
			location = entity.blockPosition();
		}
		
		public long age(long gameTime){ return gameTime - time; }
		public BlockPos location(){ return location; }
		
		public void update(LivingEntity target)
		{
			time = target.getLevel().getGameTime();
			location = target.blockPosition();
		}
		
		public void updateWith(LivingEntity target, List<Mob> observers)
		{
			for(Mob observer : observers)
			{
				if(observer.hasLineOfSight(target))
				{
					update(target);
					return;
				}
			}
		}
	}
}
