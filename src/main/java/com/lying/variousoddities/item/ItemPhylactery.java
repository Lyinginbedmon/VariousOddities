package com.lying.variousoddities.item;

import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemPhylactery extends BlockItem
{
	public ItemPhylactery(Properties builder)
	{
		super(VOBlocks.PHYLACTERY, builder.isImmuneToFire());
	}
	
	public ITextComponent getDisplayName(ItemStack stack)
	{
		CompoundNBT data = stack.hasTag() ? stack.getTag() : new CompoundNBT();
		CompoundNBT tileData = data.contains("BlockEntityTag", 10) ? data.getCompound("BlockEntityTag") : new CompoundNBT();
		if(tileData.contains("OwnerName", 8))
			return new TranslationTextComponent("block."+Reference.ModInfo.MOD_ID+".phylactery_bound", ITextComponent.Serializer.getComponentFromJson(tileData.getString("OwnerName")));
		
		return super.getDisplayName(stack);
	}
	
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if(!(entityIn instanceof MobEntity || entityIn.getType() == EntityType.PLAYER))
			return;
		
		// If an unbound phylactery is held by a mob or player, bind to it
		CompoundNBT data = stack.hasTag() ? stack.getTag() : new CompoundNBT();
		CompoundNBT tileData = data.contains("BlockEntityTag", 10) ? data.getCompound("BlockEntityTag") : new CompoundNBT();
		if(!tileData.contains("OwnerUUID", 11))
		{
			tileData.putString("OwnerName", ITextComponent.Serializer.toJson(entityIn.getDisplayName()));
			tileData.putUniqueId("OwnerUUID", entityIn.getUniqueID());
			
			tileData.putBoolean("IsPlayer", entityIn.getType() == EntityType.PLAYER);
			
			data.put("BlockEntityTag", tileData);
			stack.setTag(data);
		}
	}
}
