package com.lying.variousoddities.entity.ai.passive;

import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.entity.passive.EntityWorg;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityAIWorgFetch extends Goal
{
	private final EntityWorg theWorg;
	private final Level theWorld;
	private final PathNavigation theNavigator;
	
	private LivingEntity theOwner;
	private ItemEntity theBone;
	private final Predicate<ItemEntity> searchPredicate = new Predicate<ItemEntity>()
			{
				public boolean apply(ItemEntity input)
				{
					ItemStack itemstack = input.getItem();
					Item item = itemstack.getItem();
					return input.isPickable() && itemstack.getCount() >= 1 && (item == Items.BONE || EntityAIWorgFetch.isItemFeetArmor(itemstack));
				}
			};
	
	private final double searchRange;
	
	public EntityAIWorgFetch(EntityWorg worgIn, double range)
	{
		theWorg = worgIn;
		theWorld = worgIn.getLevel();
		theNavigator = worgIn.getNavigation();
		searchRange = range;
		setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	public boolean canUse()
	{
		if(theWorg.isOrderedToSit() || !theWorg.isTame()|| theWorg.getTarget() != null || !theWorg.getMainHandItem().isEmpty())
			return false;
		
		theOwner = theWorg.getOwner();
		if(theOwner == null)
			return false;
		
		List<ItemEntity> bones = theWorld.getEntitiesOfClass(ItemEntity.class, theWorg.getBoundingBox().inflate(searchRange, 2D, searchRange), searchPredicate);
		if(bones.isEmpty())
			return false;
		
		for(ItemEntity bone : bones)
			if(theNavigator.createPath(bone, (int)searchRange) != null && theWorg.distanceToSqr(bone) > searchRange)
				theBone = bone;
		
		return theBone != null && theWorg.getRandom().nextInt(30) == 0;
	}
	
	public boolean canContinueToUse()
	{
		return theBone != null && theBone.isAlive() && theWorg.getTarget() == null && !theWorg.isOrderedToSit();
	}
	
	public void startExecuting()
	{
		theWorg.getLookControl().setLookPositionWithEntity(theBone, (float)(theWorg.getHorizontalFaceSpeed() + 20), (float)theWorg.getVerticalFaceSpeed());
	}
	
	public void stop()
	{
		theBone = null;
	}
	
	public void tick()
	{
		theWorg.getLookControl().setLookPositionWithEntity(theBone, (float)(theWorg.getHorizontalFaceSpeed() + 20), (float)theWorg.getVerticalFaceSpeed());
		if(theWorg.distanceToSqr(theBone) >= 1D)
		{
			if(theNavigator.isDone())
				theNavigator.moveTo(theBone, 1.0D);
		}
		else
		{
			theNavigator.stop();
			ItemStack heldStack = theBone.getItem();
			if(heldStack.isDamageableItem() && (heldStack.getMaxDamage() - heldStack.getDamageValue()) > 1 && theWorg.getRandom().nextInt(4) == 0)
				heldStack.hurtAndBreak(1, theWorg, (player) -> {});
			
			theWorg.setItemInHand(InteractionHand.MAIN_HAND, heldStack);
			theBone.remove();
			theNavigator.moveTo(theOwner, 1.0D);
		}
	}
	
	public void dropHeldItem()
	{
		if(theWorg.getMainHandItem().isEmpty()) return;
		theWorg.spawnAtLocation(theWorg.getMainHandItem().getItem(), 1);
		theWorg.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}
	
	public static boolean isItemFeetArmor(ItemStack itemstack)
	{
		Item item = itemstack.getItem();
		return 
				item.getEquipmentSlot(itemstack) == EquipmentSlot.FEET ||
				(item instanceof ArmorItem && ((ArmorItem)item).getEquipmentSlot(itemstack) == EquipmentSlot.FEET);
	}
}
