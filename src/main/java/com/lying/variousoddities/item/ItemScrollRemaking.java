package com.lying.variousoddities.item;

import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesOpenScreen;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ItemScrollRemaking extends Item
{
	private final boolean randomise;
	
	public ItemScrollRemaking(boolean random, Properties properties)
	{
		super(properties.stacksTo(1));
		this.randomise = random;
	}
	
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
	{
		playerIn.startUsingItem(handIn);
		return InteractionResultHolder.consume(playerIn.getItemInHand(handIn));
	}
	
	public UseAnim getUseAction(ItemStack stack) { return UseAnim.SPEAR; }
	
	public int getUseDuration(ItemStack stack) { return Reference.Values.TICKS_PER_SECOND * 5; }
	
	public ItemStack onItemUseFinish(ItemStack stack, Level worldIn, LivingEntity entityLiving)
	{
		if(entityLiving.getType() == EntityType.PLAYER && !worldIn.isClientSide)
			PacketHandler.sendTo((ServerPlayer)entityLiving, new PacketSpeciesOpenScreen(ConfigVO.MOBS.powerLevel.get(), randomise));
		stack.shrink(1);
		return stack;
	}
}
