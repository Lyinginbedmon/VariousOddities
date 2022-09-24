package com.lying.variousoddities.item;

import com.lying.variousoddities.entity.passive.EntityMarimo;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ItemMossBottle extends VOItem
{
	public ItemMossBottle(Item.Properties properties)
	{
		super(properties.stacksTo(1));
	}
	
	public static DyeColor getColor(ItemStack stack)
	{
		if(stack.hasTag() && stack.getTag().contains("Color", 3))
			return DyeColor.byId(stack.getTag().getInt("Color"));
		return DyeColor.GREEN;
	}
	
	public static ItemStack setColor(ItemStack stack, int color)
	{
		CompoundTag stackData = stack.hasTag() ? stack.getTag() : new CompoundTag();
		stackData.putInt("Color", color);
		stack.setTag(stackData);
		return stack;
	}
	
	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
	 * {@link #onItemUse}.
	 */
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
	{
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		HitResult raytraceresult = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.SOURCE_ONLY);
		if(raytraceresult.getType() != HitResult.Type.BLOCK)
			return InteractionResultHolder.pass(itemstack);
		else if(!(worldIn instanceof ServerLevel))
			return InteractionResultHolder.success(itemstack);
		else
		{
			BlockHitResult blockraytraceresult = (BlockHitResult)raytraceresult;
			BlockPos blockpos = blockraytraceresult.getBlockPos();
			if((worldIn.getBlockState(blockpos).getBlock() instanceof LiquidBlock))
			{
				if(worldIn.mayInteract(playerIn, blockpos) && playerIn.mayUseItemAt(blockpos, blockraytraceresult.getDirection(), itemstack))
				{
					EntityMarimo marimo = (EntityMarimo)VOEntities.MARIMO.get().spawn((ServerLevel)worldIn, itemstack, playerIn, blockpos, MobSpawnType.SPAWN_EGG, false, false); 
					if(marimo == null)
						return InteractionResultHolder.pass(itemstack);
					else
					{
						marimo.setColor(getColor(itemstack));
						if(!playerIn.isCreative())
							itemstack.shrink(1);
						playerIn.awardStat(Stats.ITEM_USED.get(this));
						return InteractionResultHolder.consume(itemstack);
					}
				}
			}
			else
				return InteractionResultHolder.fail(itemstack);
		}
		return InteractionResultHolder.pass(itemstack);
	}
}
