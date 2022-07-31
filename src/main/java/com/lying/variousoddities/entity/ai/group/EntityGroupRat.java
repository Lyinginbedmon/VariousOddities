package com.lying.variousoddities.entity.ai.group;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.entity.passive.EntityRat;

import net.minecraft.world.entity.Mob;

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
		
		if(leader.isAlive() && leader.getTarget() != null)
		{
			for(EntityRat rat : leader.getLevel().getEntitiesOfClass(EntityRat.class, leader.getBoundingBox().inflate(6, 2, 6), new Predicate<EntityRat>()
			{
				public boolean apply(EntityRat input)
				{
					return input.isAlive() && !input.isBaby() && isObserved(input) && GroupHandler.getEntityMemberGroup(input) == null && input.getRatBreed() == leader.getRatBreed();
				}
			}))
				addMember(rat);
			
			for(Mob entity : members.keySet())
				if(entity.isAlive() && entity.getTarget() != leader.getTarget())
					entity.setTarget(leader.getTarget());
		}
		else
			GroupHandler.removeGroup(this);
	}
}
