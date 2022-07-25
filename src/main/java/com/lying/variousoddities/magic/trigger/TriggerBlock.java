package com.lying.variousoddities.magic.trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

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
	
	public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"block" + (inverted ? "_inverted" : "")); }
	
	public Collection<? extends Trigger> possibleVariables(){ return possibleVariables; }
	
	public Trigger addVariable(Trigger triggerIn){ variables.add((TriggerBlock)triggerIn); return this; }
	
	public List<? extends Trigger> getVariables(){ return variables; }
	
	public boolean applyToBlock(Level world)
	{
		if(!world.isEmptyBlock(pos))
		{
			for(TriggerBlock variable : variables)
				if(variable.applyToBlock(world) == variable.inverted())
					return false;
			return true;
		}
		return false;
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.put("Pos", NbtUtils.writeBlockPos(pos));
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		pos = NbtUtils.readBlockPos(compound.getCompound("Pos"));
	}
	
	public static class TriggerBlockType extends TriggerBlock
	{
		private ResourceLocation block = null;
		
		public String type(){ return "block_type"; }
		
		public TriggerBlockType(){ }
		@SuppressWarnings("deprecation")
		public TriggerBlockType(Block blockIn)
		{
			block = Registry.BLOCK.getKey(blockIn);
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"block_type" + (inverted ? "_inverted" : ""), block == null ? "anything" : block); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		@SuppressWarnings("deprecation")
		public boolean applyToBlock(Level world)
		{
			return block == null || Registry.BLOCK.getKey(world.getBlockState(pos).getBlock()).equals(block);
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			super.writeToNBT(compound);
			compound.putString("Block", block.toString());
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
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
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"block_state" + (inverted ? "_inverted" : ""), propertyName, propertyValue); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public boolean applyToBlock(Level world)
		{
			if(propertyName.length() == 0 || propertyValue.length() == 0)
				return true;
			
			BlockState state = world.getBlockState(pos);
			for(Property property : state.getProperties())
				if(property.getName().equals(propertyName))
				{
					Comparable<?> stateValue = state.getValue(property);
					return stateValue.equals(property.getValue(propertyValue));
				}
			
			return false;
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			super.writeToNBT(compound);
			compound.putString("Name", propertyName);
			compound.putString("Value", propertyValue);
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
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
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"block_powered" + (inverted ? "_inverted" : "")); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		public boolean applyToBlock(Level world)
		{
			return world.hasNeighborSignal(pos);
		}
	}
}
