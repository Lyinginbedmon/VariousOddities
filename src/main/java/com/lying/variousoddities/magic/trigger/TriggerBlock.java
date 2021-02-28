package com.lying.variousoddities.magic.trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TriggerBlock extends Trigger
{
	private static final List<Trigger> possibleVariables = Arrays.asList(new Trigger[]{new TriggerBlockType(), new TriggerBlockState(), new TriggerBlockPowered()});
	protected BlockPos pos = BlockPos.ZERO;
	
	private List<TriggerBlock> variables = new ArrayList<>();
	
	public String type(){ return "block"; }
	
	public TriggerBlock(){ }
	public TriggerBlock(BlockPos posIn)
	{
		pos = posIn;
	}
	
	public ITextComponent getTranslated(boolean inverted){ return new TranslationTextComponent("trigger."+Reference.ModInfo.MOD_PREFIX+"block" + (inverted ? "_inverted" : "")); }
	
	public Collection<? extends Trigger> possibleVariables(){ return possibleVariables; }
	
	public Trigger addVariable(Trigger triggerIn){ variables.add((TriggerBlock)triggerIn); return this; }
	
	public List<? extends Trigger> getVariables(){ return variables; }
	
	public boolean applyToBlock(World world)
	{
		if(!world.isAirBlock(pos))
		{
			for(TriggerBlock variable : variables)
				if(variable.applyToBlock(world) == variable.inverted())
					return false;
			return true;
		}
		return false;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.put("Pos", NBTUtil.writeBlockPos(pos));
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		pos = NBTUtil.readBlockPos(compound.getCompound("Pos"));
	}
	
	public static class TriggerBlockType extends TriggerBlock
	{
		private ResourceLocation block = null;
		
		public String type(){ return "block_type"; }
		
		public TriggerBlockType(){ }
		public TriggerBlockType(Block blockIn)
		{
			block = blockIn.getRegistryName();
		}
		
		public ITextComponent getTranslated(boolean inverted){ return new TranslationTextComponent("trigger."+Reference.ModInfo.MOD_PREFIX+"block_type" + (inverted ? "_inverted" : ""), block == null ? "anything" : block); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		public boolean applyToBlock(World world)
		{
			return block == null || world.getBlockState(pos).getBlock().getRegistryName().equals(block);
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			super.writeToNBT(compound);
			compound.putString("Block", block.toString());
			return compound;
		}
		
		public void readFromNBT(CompoundNBT compound)
		{
			super.readFromNBT(compound);
			block = new ResourceLocation(compound.getString("Block"));
		}
	}
	
	public static class TriggerBlockState extends TriggerBlock
	{
		private String propertyName = "";
		private String propertyValue = "";
		
		public String type(){ return "block_state"; }
		
		public TriggerBlockState(){ }
		public TriggerBlockState(String nameIn, String valIn)
		{
			propertyName = nameIn;
			propertyValue = valIn;
		}
		
		public ITextComponent getTranslated(boolean inverted){ return new TranslationTextComponent("trigger."+Reference.ModInfo.MOD_PREFIX+"block_state" + (inverted ? "_inverted" : ""), propertyName, propertyValue); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public boolean applyToBlock(World world)
		{
			if(propertyName.length() == 0 || propertyValue.length() == 0)
				return true;
			
			BlockState state = world.getBlockState(pos);
			for(Property property : state.getProperties())
				if(property.getName().equals(propertyName))
				{
					Comparable<?> stateValue = state.get(property);
					return stateValue.equals(property.parseValue(propertyValue));
				}
			
			return false;
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			super.writeToNBT(compound);
			compound.putString("Name", propertyName);
			compound.putString("Value", propertyValue);
			return compound;
		}
		
		public void readFromNBT(CompoundNBT compound)
		{
			super.readFromNBT(compound);
			propertyName = compound.getString("Name");
			propertyValue = compound.getString("Value");
		}
	}
	
	public static class TriggerBlockPowered extends TriggerBlock
	{
		public String type(){ return "block_powered"; }
		
		public TriggerBlockPowered(){ }
		public TriggerBlockPowered(boolean boolIn)
		{
			setInverted(!boolIn);
		}
		
		public ITextComponent getTranslated(boolean inverted){ return new TranslationTextComponent("trigger."+Reference.ModInfo.MOD_PREFIX+"block_powered" + (inverted ? "_inverted" : "")); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		public boolean applyToBlock(World world)
		{
			return world.isBlockPowered(pos);
		}
	}
}
