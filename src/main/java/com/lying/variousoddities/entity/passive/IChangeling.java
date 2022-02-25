package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.api.entity.IFactionMob;
import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.faction.FactionReputation;
import com.lying.variousoddities.faction.FactionReputation.EnumAttitude;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;

public interface IChangeling extends IFactionMob
{
	public default String getFactionName()
	{
		return "changeling";
	}
	
	public default boolean hasParentHive(){ return getParentHivePos() != null; }
	public BlockPos getParentHivePos();
	public void setParentHive(BlockPos hivePos);
	
	public default EnumRoomFunction getHomeRoom()
	{
		return null;
	}
	
	public default boolean isFlapping()
	{
		return getFlappingTime() > 0;
	}
	
	public default int getFlappingTime()
	{
		return 0;
	}
	
	/**
	 * Returns true if any slot in the changeling's inventory of carried items contains an item.
	 * @return
	 */
	public default boolean hasCarriedItems()
	{
		for(ItemStack stack : getCarriedItems())
			if(!stack.isEmpty())
				return true;
		return false;
	}
	
	/**
	 * Returns true if any slot in the changeling's inventory of carried items could hold the given item.
	 * @param stackIn
	 * @return
	 */
	public default boolean canCarryItem(ItemStack stackIn)
	{
		return false;
	}
	
	/**
	 * Returns a non-null list of itemstacks representing the changeling's carried items.
	 * @return
	 */
	public default NonNullList<ItemStack> getCarriedItems()
	{
		return NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
	}
	
	/**
	 * Voids the contents of the changeling's carried items.
	 */
	public default void emptyCarriedItems(){ }
	
	public default void setItemInSlot(int index, ItemStack stackIn){ }
	
	/**
	 * Adds the given itemstack to the changeling's carried items and returns any remainder.
	 * @param stackIn
	 * @return
	 */
	public default ItemStack addCarriedItem(ItemStack stackIn)
	{
		return stackIn;
	}
	
    public default boolean canCombine(ItemStack stack1, ItemStack stack2)
    {
        if(stack1.getItem() != stack2.getItem())
            return false;
//        if(stack1.getMetadata() != stack2.getMetadata())
//            return false;
        if (stack1.getCount() > stack1.getMaxStackSize())
            return false;
        
        return ItemStack.areItemStackTagsEqual(stack1, stack2);
    }
	
	public static EnumAttitude getChangelingAttitude(PlayerEntity par1Player, LivingEntity par2Entity)
	{
		return FactionReputation.getPlayerAttitude(par1Player, ((IFactionMob)par2Entity).getFactionName(), par2Entity);
	}
	
	public static boolean shouldReveal(PlayerEntity par1Player, LivingEntity par2Entity)
	{
		return getChangelingAttitude(par1Player, par2Entity) == EnumAttitude.HELPFUL;
	}
	
	public static void addChangelingBehaviours(CreatureEntity creatureIn)
	{
		if(!(creatureIn instanceof IChangeling)) return;
//		creatureIn.tasks.addTask(3, new EntityAIChangelingVisitHive(creatureIn));
//		creatureIn.tasks.addTask(3, new EntityAIOperateRoomChangeling(creatureIn));
//		creatureIn.tasks.addTask(3, new EntityAIChangelingGrabItem(creatureIn));
//		creatureIn.tasks.addTask(3, new EntityAIChangelingStoreItem(creatureIn));
	}
	
	/**
	 * Returns the first behaviour of the given class (if any) in the creature's AI tasks
	 * @param behaviourIn
	 * @param creatureIn
	 * @return
	 */
	public static Goal getChangelingBehaviour(Class<? extends Goal> behaviourIn, LivingEntity creatureIn)
	{
//		for(EntityAITaskEntry taskEntry : creatureIn.tasks.taskEntries)
//		{
//			Goal task = taskEntry.action;
//			if(behaviourIn.isAssignableFrom(task.getClass()))
//				return task;
//		}
		return null;
	}
	
	public static void setChangelingAttributes(CreatureEntity creatureIn)
	{
        creatureIn.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(64.0D);
	}
	
	public static boolean teleportTo(CreatureEntity theMob, double x, double y, double z)
	{
		EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(theMob, x, y, z);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return false;
        boolean success = false;//theMob.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        if(success)
        {
            theMob.getEntityWorld().playSound((PlayerEntity)null, theMob.prevPosX, theMob.prevPosY, theMob.prevPosZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, theMob.getSoundCategory(), 1.0F, 1.0F);
            theMob.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
        return success;
	}
}
