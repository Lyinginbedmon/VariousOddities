package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.passive.EntityWorg;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EntityAIWorgFetch extends Goal
{
	private final EntityWorg theWorg;
	private final World theWorld;
	private final PathNavigator theNavigator;
	
	private LivingEntity theOwner;
	private ItemEntity theBone;
	private final Predicate<ItemEntity> searchPredicate = new Predicate<ItemEntity>()
			{
				public boolean apply(ItemEntity input)
				{
					ItemStack itemstack = input.getItem();
					Item item = itemstack.getItem();
					return !input.cannotPickup() && itemstack.getCount() >= 1 && (item == Items.BONE || EntityAIWorgFetch.isItemFeetArmor(itemstack));
				}
			};
			
	private final double searchRange;
	
	public EntityAIWorgFetch(EntityWorg worgIn, double range)
	{
		theWorg = worgIn;
		theWorld = worgIn.getEntityWorld();
		theNavigator = worgIn.getNavigator();
		searchRange = range;
		setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	public boolean shouldExecute()
	{
		if(theWorg.isSitting() || !theWorg.isTamed()|| theWorg.getAttackTarget() != null || !theWorg.getHeldItemMainhand().isEmpty())
			return false;
		
		theOwner = theWorg.getOwner();
		if(theOwner == null)
			return false;
		
		List<ItemEntity> bones = theWorld.getEntitiesWithinAABB(ItemEntity.class, theWorg.getBoundingBox().grow(searchRange, 2D, searchRange), searchPredicate);
		if(bones.isEmpty())
			return false;
		
		for(ItemEntity bone : bones)
			if(theNavigator.getPathToEntity(bone, (int)searchRange) != null && theWorg.getDistance(bone) > searchRange)
				theBone = bone;
		
		return theBone != null && theWorg.getRNG().nextInt(30) == 0;
	}
	
	public boolean shouldContinueExecuting()
	{
		return theBone != null && theBone.isAlive() && theWorg.getAttackTarget() == null && !theWorg.isSitting();
	}
	
	public void startExecuting()
	{
		theWorg.getLookController().setLookPositionWithEntity(theBone, (float)(theWorg.getHorizontalFaceSpeed() + 20), (float)theWorg.getVerticalFaceSpeed());
	}
	
	public void resetTask()
	{
		theBone = null;
	}
	
	public void tick()
	{
		theWorg.getLookController().setLookPositionWithEntity(theBone, (float)(theWorg.getHorizontalFaceSpeed() + 20), (float)theWorg.getVerticalFaceSpeed());
		if(theWorg.getDistance(theBone) >= 1D)
		{
			if(theNavigator.noPath())
				theNavigator.tryMoveToEntityLiving(theBone, 1.0D);
		}
		else
		{
			theNavigator.clearPath();
			ItemStack heldStack = theBone.getItem();
			if(heldStack.isDamageable() && (heldStack.getMaxDamage() - heldStack.getDamage()) > 1 && theWorg.getRNG().nextInt(4) == 0)
				heldStack.damageItem(1, theWorg, (player) -> {});
			
			theWorg.setHeldItem(Hand.MAIN_HAND, heldStack);
			theBone.remove();
			theNavigator.tryMoveToEntityLiving(theOwner, 1.0D);
		}
	}
	
	public void dropHeldItem()
	{
		if(theWorg.getHeldItemMainhand().isEmpty()) return;
		theWorg.entityDropItem(theWorg.getHeldItemMainhand().getItem(), 1);
		theWorg.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
	}
	
	public static boolean isItemFeetArmor(ItemStack itemstack)
	{
		Item item = itemstack.getItem();
		return 
				item.getEquipmentSlot(itemstack) == EquipmentSlotType.FEET ||
				(item instanceof ArmorItem && ((ArmorItem)item).getEquipmentSlot() == EquipmentSlotType.FEET);
	}
}
