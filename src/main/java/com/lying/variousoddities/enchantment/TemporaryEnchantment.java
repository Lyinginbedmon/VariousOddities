package com.lying.variousoddities.enchantment;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;

public abstract class TemporaryEnchantment extends Enchantment
{
	protected TemporaryEnchantment(Rarity rarityIn, EnchantmentCategory typeIn, EquipmentSlot... slots)
	{
		super(rarityIn, typeIn, slots);
	}
	
	/** The duration of this enchantment on an item, measured in seconds */
	public abstract int getDuration();
	
	public void onExpire(Level worldIn, LivingEntity entityIn, ItemStack stackIn){ }
	
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		if(worldIn.isClientSide || (worldIn.getGameTime() % Reference.Values.TICKS_PER_SECOND) != 0)
			return;
		
		long time = worldIn.getGameTime();
		long expiry = getOrCreateTimer(stack, time);
		if(expiry <= time)
		{
			if(entityIn instanceof LivingEntity)
			{
				onExpire(worldIn, (LivingEntity)entityIn, stack);
		        worldIn.levelEvent((Player)null, 1027, entityIn.blockPosition(), 0);
			}
			removeEnchantment(stack);
		}
	}
	
	@SuppressWarnings("deprecation")
	public long getOrCreateTimer(ItemStack stack, long gameTime)
	{
		ListTag enchList = stack.getEnchantmentTags();
		CompoundTag ench = null;
		int index = -1;
		for(int i=0; i<enchList.size(); i++)
		{
			CompoundTag entry = enchList.getCompound(i);
			if(entry.getString("id").equalsIgnoreCase(Registry.ENCHANTMENT.getKey(this).toString()))
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
			CompoundTag data = stack.getTag();
			data.put("Enchantments", enchList);
			stack.setTag(data);
			
			return time;
		}
		return -1;
	}
	
	@SuppressWarnings("deprecation")
	public void removeEnchantment(ItemStack stack)
	{
		ListTag enchList = stack.getEnchantmentTags();
		int index = -1;
		for(int i=0; i<enchList.size(); i++)
		{
			CompoundTag entry = enchList.getCompound(i);
			if(entry.getString("id").equalsIgnoreCase(Registry.ENCHANTMENT.getKey(this).toString()))
			{
				index = i;
				break;
			}
		}
		enchList.remove(index);
		
		CompoundTag data = stack.getTag();
		if(enchList.isEmpty())
			data.remove("Enchantments");
		else
			data.put("Enchantments", enchList);
		stack.setTag(data);
	}
}
