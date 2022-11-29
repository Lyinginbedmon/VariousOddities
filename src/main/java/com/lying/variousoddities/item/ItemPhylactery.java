package com.lying.variousoddities.item;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemPhylactery extends BlockItem
{
	public ItemPhylactery(Properties builder)
	{
		super(VOBlocks.PHYLACTERY.get(), builder.fireResistant());
	}
	
	public Component getName(ItemStack stack)
	{
		CompoundTag data = stack.hasTag() ? stack.getTag() : new CompoundTag();
		CompoundTag tileData = data.contains("BlockEntityTag", 10) ? data.getCompound("BlockEntityTag") : new CompoundTag();
		if(tileData.contains("OwnerName", 8))
			return Component.translatable("block."+Reference.ModInfo.MOD_ID+".phylactery_bound", Component.Serializer.fromJson(tileData.getString("OwnerName")));
		
		return super.getName(stack);
	}
	
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if(!(entityIn instanceof Mob || entityIn.getType() == EntityType.PLAYER))
			return;
		
		// If an unbound phylactery is held by a mob or player, bind to it
		CompoundTag data = stack.hasTag() ? stack.getTag() : new CompoundTag();
		CompoundTag tileData = data.contains("BlockEntityTag", 10) ? data.getCompound("BlockEntityTag") : new CompoundTag();
		if(!tileData.contains("OwnerUUID", 11))
		{
			tileData.putString("OwnerName", Component.Serializer.toJson(entityIn.getDisplayName()));
			tileData.putUUID("OwnerUUID", entityIn.getUUID());
			
			tileData.putBoolean("IsPlayer", entityIn.getType() == EntityType.PLAYER);
			
			data.put("BlockEntityTag", tileData);
			stack.setTag(data);
		}
	}
}
