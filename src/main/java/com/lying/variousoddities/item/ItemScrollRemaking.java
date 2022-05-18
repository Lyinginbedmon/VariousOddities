package com.lying.variousoddities.item;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesOpenScreen;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemScrollRemaking extends Item
{
	private final boolean randomise;
	
	public ItemScrollRemaking(boolean random, Properties properties)
	{
		super(properties.maxStackSize(1));
		this.randomise = random;
	}
	
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
	{
		playerIn.setActiveHand(handIn);
		return ActionResult.resultConsume(playerIn.getHeldItem(handIn));
	}
	
	public UseAction getUseAction(ItemStack stack) { return UseAction.SPEAR; }
	
	public int getUseDuration(ItemStack stack) { return Reference.Values.TICKS_PER_SECOND * 5; }
	
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving)
	{
		if(entityLiving.getType() == EntityType.PLAYER && !worldIn.isRemote)
			PacketHandler.sendTo((ServerPlayerEntity)entityLiving, new PacketSpeciesOpenScreen(ConfigVO.MOBS.powerLevel.get(), randomise));
		stack.shrink(1);
		return stack;
	}
}
