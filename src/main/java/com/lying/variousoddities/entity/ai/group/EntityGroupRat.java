package com.lying.variousoddities.entity.ai.group;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.entity.passive.EntityRat;

import net.minecraft.entity.MobEntity;

public class EntityGroupRat extends EntityGroup
{
	private final AbstractRat leader;
	
	public EntityGroupRat(AbstractRat leaderIn)
	{
		addMember(leader = leaderIn);
	}
	
	public void tick()
	{
		super.tick();
		
		if(leader.isAlive() && leader.getAttackTarget() != null)
		{
			for(EntityRat rat : leader.getEntityWorld().getEntitiesWithinAABB(EntityRat.class, leader.getBoundingBox().grow(6, 2, 6), new Predicate<EntityRat>()
			{
				public boolean apply(EntityRat input)
				{
					return input.isAlive() && !input.isChild() && isObserved(input) && GroupHandler.getEntityMemberGroup(input) == null && input.getRatBreed() == leader.getRatBreed();
				}
			}))
				addMember(rat);
			
			for(MobEntity entity : members.keySet())
				if(entity.isAlive() && entity.getAttackTarget() != leader.getAttackTarget())
					entity.setAttackTarget(leader.getAttackTarget());
		}
		else
			GroupHandler.removeGroup(this);
	}
}
