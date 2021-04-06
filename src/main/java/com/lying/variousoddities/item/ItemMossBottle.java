package com.lying.variousoddities.item;

import com.lying.variousoddities.entity.passive.EntityMarimo;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ItemMossBottle extends VOItem
{
	public ItemMossBottle(Item.Properties properties)
	{
		super(properties.maxStackSize(1));
	}
	
	public static DyeColor getColor(ItemStack stack)
	{
		if(stack.hasTag() && stack.getTag().contains("Color", 3))
			return DyeColor.byId(stack.getTag().getInt("Color"));
		return DyeColor.GREEN;
	}
	
	public static ItemStack setColor(ItemStack stack, int color)
	{
		CompoundNBT stackData = stack.hasTag() ? stack.getTag() : new CompoundNBT();
		stackData.putInt("Color", color);
		stack.setTag(stackData);
		return stack;
	}
	
	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
	 * {@link #onItemUse}.
	 */
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
	{
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		RayTraceResult raytraceresult = rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.SOURCE_ONLY);
		if(raytraceresult.getType() != RayTraceResult.Type.BLOCK)
			return ActionResult.resultPass(itemstack);
		else if(!(worldIn instanceof ServerWorld))
			return ActionResult.resultSuccess(itemstack);
		else
		{
			BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult)raytraceresult;
			BlockPos blockpos = blockraytraceresult.getPos();
			if((worldIn.getBlockState(blockpos).getBlock() instanceof FlowingFluidBlock))
			{
				if(worldIn.isBlockModifiable(playerIn, blockpos) && playerIn.canPlayerEdit(blockpos, blockraytraceresult.getFace(), itemstack))
				{
					EntityMarimo marimo = (EntityMarimo)VOEntities.MARIMO.spawn((ServerWorld)worldIn, itemstack, playerIn, blockpos, SpawnReason.SPAWN_EGG, false, false); 
					if(marimo == null)
						return ActionResult.resultPass(itemstack);
					else
					{
						marimo.setColor(getColor(itemstack));
						if(!playerIn.abilities.isCreativeMode)
							itemstack.shrink(1);
						playerIn.addStat(Stats.ITEM_USED.get(this));
						return ActionResult.resultConsume(itemstack);
					}
				}
			}
			else
				return ActionResult.resultFail(itemstack);
		}
		return ActionResult.resultPass(itemstack);
	}
}
