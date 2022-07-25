package com.lying.variousoddities.magic.trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;

public class TriggerItem extends Trigger
{
	private static final List<Trigger> possibleVariables = Arrays.asList(new Trigger[]{new TriggerItemName(), new TriggerItemEnchanted()});
	
	List<TriggerItem> variables = new ArrayList<>();
	ItemStack itemStack = ItemStack.EMPTY;
	
	public String type(){ return "item"; }
	
	public TriggerItem(){ }
	public TriggerItem(ItemStack stackIn)
	{
		itemStack = stackIn;
	}
	
	public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"item" + (inverted ? "_inverted" : ""), itemStack.getDisplayName()); }
	
	public boolean applyToItem(ItemStack stack)
	{
		if(stack.getItem() == itemStack.getItem())
		{
			for(TriggerItem variable : variables)
				if(variable.applyToItem(stack) == variable.inverted())
					return false;
			
			return true;
		}
		return false;
	}
	
	public Collection<? extends Trigger> possibleVariables(){ return possibleVariables; }
	
	public Trigger addVariable(Trigger triggerIn){ variables.add((TriggerItem)triggerIn); return this; }
	
	public List<? extends Trigger> getVariables(){ return variables; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.put("Item", itemStack.save(new CompoundTag()));
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		itemStack = ItemStack.of(compound.getCompound("Item"));
	}
	
	public static class TriggerItemName extends TriggerItem
	{
		private String name = "";
		
		public String type(){ return "item_name"; }
		
		public TriggerItemName(){ }
		public TriggerItemName(String nameIn)
		{
			name = nameIn;
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"item_name" + (inverted ? "_inverted" : "")); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		public boolean applyToItem(ItemStack stack)
		{
			return name.length() == 0 || stack.hasCustomHoverName() && stack.getDisplayName().equals(name);
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putString("Name", name);
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			name = compound.getString("Name");
		}
	}
	
	public static class TriggerItemEnchanted extends TriggerItem
	{
		public String type(){ return "item_enchanted"; }
		
		public TriggerItemEnchanted(){ }
		public TriggerItemEnchanted(boolean boolIn)
		{
			setInverted(!boolIn);
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"item_enchanted" + (inverted ? "_inverted" : "")); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		public boolean applyToItem(ItemStack stack)
		{
			return stack.isEnchanted();
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			
		}
	}
	
	public static class TriggerItemColor extends TriggerItem
	{
		private static final float RANGE = 0.1F; 
		private int color = -1;
		
		public String type(){ return "item_color"; }
		
		public TriggerItemColor(){ }
		public TriggerItemColor(DyeColor colorIn)
		{
			this(colorIn.getFireworkColor());
		}
		public TriggerItemColor(String colorIn)
		{
			this(hexToDec(colorIn));
		}
		public TriggerItemColor(int colorIn)
		{
			color = colorIn;
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"item_color" + (inverted ? "_inverted" : "")); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		public boolean applyToItem(ItemStack stack)
		{
			if(stack.getItem() instanceof DyeableArmorItem)
			{
				DyeableArmorItem armor = (DyeableArmorItem)stack.getItem();
				if(!armor.hasCustomColor(stack))
					return false;
				
				if(color < 0)
					return true;
				
                float colorR = (float)(color >> 16 & 255) / 255.0F;
                float colorG = (float)(color >> 8 & 255) / 255.0F;
                float colorB = (float)(color & 255) / 255.0F;
				
				int armorColor = armor.getColor(stack);
                float armorR = (float)(armorColor >> 16 & 255) / 255.0F;
                float armorG = (float)(armorColor >> 8 & 255) / 255.0F;
                float armorB = (float)(armorColor & 255) / 255.0F;
                
                if(Math.abs(colorR - armorR) > RANGE)
                	return false;
                if(Math.abs(colorG - armorG) > RANGE)
                	return false;
                if(Math.abs(colorB - armorB) > RANGE)
                	return false;
                
                return true;
			}
			return false;
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putInt("Color", color);
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			if(compound.contains("Color", 8))
				color = hexToDec(compound.getString("Color"));
			if(compound.contains("Color", 99))
				color = compound.getInt("Color");
		}
		
		public static int hexToDec(String hexIn)
		{
			return -1;
		}
	}
}
