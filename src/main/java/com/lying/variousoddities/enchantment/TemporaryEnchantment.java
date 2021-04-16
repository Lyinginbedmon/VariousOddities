package com.lying.variousoddities.enchantment;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;

public abstract class TemporaryEnchantment extends Enchantment
{
	protected TemporaryEnchantment(Rarity rarityIn, EnchantmentType typeIn, EquipmentSlotType... slots)
	{
		super(rarityIn, typeIn, slots);
	}
	
	/** The duration of this enchantment on an item, measured in seconds */
	public abstract int getDuration();
	
	public void onExpire(World worldIn, LivingEntity entityIn, ItemStack stackIn){ }
	
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		if(worldIn.isRemote || (worldIn.getGameTime() % Reference.Values.TICKS_PER_SECOND) != 0)
			return;
		
		long time = worldIn.getGameTime();
		long expiry = getOrCreateTimer(stack, time);
		if(expiry <= time)
		{
			if(entityIn instanceof LivingEntity)
			{
				onExpire(worldIn, (LivingEntity)entityIn, stack);
		        worldIn.playEvent((PlayerEntity)null, 1027, entityIn.getPosition(), 0);
			}
			removeEnchantment(stack);
		}
	}
	
	public long getOrCreateTimer(ItemStack stack, long gameTime)
	{
		ListNBT enchList = stack.getEnchantmentTagList();
		CompoundNBT ench = null;
		int index = -1;
		for(int i=0; i<enchList.size(); i++)
		{
			CompoundNBT entry = enchList.getCompound(i);
			if(entry.getString("id").equalsIgnoreCase(this.getRegistryName().toString()))
			{
				index = i;
				ench = entry;
				break;
			}
		}
		
		if(index >= 0)
		{
			if(ench.contains("time", 4))
				return ench.getLong("time");
			
			long time = gameTime + getDuration() * Reference.Values.TICKS_PER_SECOND;
			ench.putLong("time", time);
			enchList.set(index, ench);
			CompoundNBT data = stack.getTag();
			data.put("Enchantments", enchList);
			stack.setTag(data);
			
			return time;
		}
		return -1;
	}
	
	public void removeEnchantment(ItemStack stack)
	{
		ListNBT enchList = stack.getEnchantmentTagList();
		int index = -1;
		for(int i=0; i<enchList.size(); i++)
		{
			CompoundNBT entry = enchList.getCompound(i);
			if(entry.getString("id").equalsIgnoreCase(this.getRegistryName().toString()))
			{
				index = i;
				break;
			}
		}
		enchList.remove(index);
		
		CompoundNBT data = stack.getTag();
		if(enchList.isEmpty())
			data.remove("Enchantments");
		else
			data.put("Enchantments", enchList);
		stack.setTag(data);
	}
}
