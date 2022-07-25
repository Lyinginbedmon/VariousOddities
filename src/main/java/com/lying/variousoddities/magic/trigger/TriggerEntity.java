package com.lying.variousoddities.magic.trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Triggers corresponding to a given visible entity
 */
public class TriggerEntity extends Trigger
{
	private static final List<Trigger> possibleVariables = Arrays.asList(new Trigger[]{new TriggerEntityType(), new TriggerEntityName(), new TriggerEntityEquipment(), new TriggerEntityHeldItem()});
	
	List<TriggerEntity> variables = new ArrayList<>();
	
	public String type(){ return "entity"; }
	
	public boolean applyToEntity(Entity entity)
	{
		if(entity == null)
			return false;
		
		for(TriggerEntity variable : variables)
			if(variable.applyToEntity(entity) == variable.inverted())
				return false;
		
		return true;
	}
	
	public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"entity" + (inverted ? "_inverted" : "")); }
	
	public Collection<? extends Trigger> possibleVariables(){ return possibleVariables; }
	
	public Trigger addVariable(Trigger triggerIn){ variables.add((TriggerEntity)triggerIn); return this; }
	
	public List<? extends Trigger> getVariables(){ return variables; }
	
	/**
	 * Check for a specific type of entity
	 */
	public static class TriggerEntityType extends TriggerEntity
	{
		ResourceLocation entityType = null;
		
		public String type(){ return "entity_type"; }
		
		public TriggerEntityType(){ }
		public TriggerEntityType(ResourceLocation typeIn)
		{
			entityType = typeIn;
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"entity_type" + (inverted ? "_inverted" : ""), entityType == null ? "anything" : entityType.toString()); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		public boolean applyToEntity(Entity visibleEntity)
		{
			if(entityType == null)
				return true;
			return EntityType.getKey(visibleEntity.getType()).equals(entityType);
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putString("Type", entityType.toString());
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			entityType = new ResourceLocation(compound.getString("Type"));
		}
	}
	
	/**
	 * Check for an entity with a specific name
	 */
	public static class TriggerEntityName extends TriggerEntity
	{
		String entityName = "";
		
		public String type(){ return "entity_name"; }
		
		public TriggerEntityName(){ }
		public TriggerEntityName(String nameIn)
		{
			entityName = nameIn;
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"entity_name" + (inverted ? "_inverted" : ""), entityName); }
		
		public Collection<? extends Trigger> possibleVariables(){ return NO_VARIABLES; }
		
		public boolean applyToEntity(Entity visibleEntity)
		{
			if(visibleEntity instanceof LivingEntity)
			{
//				LivingEntity entity = (LivingEntity)visibleEntity;
//				ItemStack helmet = entity.getItemStackFromSlot(EquipmentSlot.HEAD); 
//				if(!helmet.isEmpty() && helmet.getItem() == VOItems.HOOD && ItemHatHood.getIsUp(helmet))
//					return false;
				
				if(visibleEntity instanceof LivingEntity)
				{
					LivingEntity living = (LivingEntity)visibleEntity;
					return living.hasCustomName() && living.getCustomName().equals(entityName);
				}
				else if(visibleEntity.getType() == EntityType.PLAYER)
					return visibleEntity.getName().equals(entityName);
			}
			return false;
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putString("Name", entityName);
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			entityName = compound.getString("Name");
		}
	}
	
	/**
	 * Check for an entity wearing or carrying equipment in a specific slot
	 */
	public static class TriggerEntityEquipment extends TriggerEntity
	{
		private static final List<Trigger> possibleVariables = Arrays.asList(new Trigger[]{new TriggerItem()});
		
		List<TriggerItem> variables = new ArrayList<>();
		EquipmentSlot slot = EquipmentSlot.HEAD;
		
		public String type(){ return "entity_equipment"; }
		
		public TriggerEntityEquipment(){ }
		public TriggerEntityEquipment(EquipmentSlot slotIn)
		{
			slot = slotIn;
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"entity_equipment" + (inverted ? "_inverted" : ""), slot.name()); }
		
		public Collection<? extends Trigger> possibleVariables(){ return possibleVariables; }
		
		public Trigger addVariable(Trigger triggerIn){ variables.add((TriggerItem)triggerIn); return this; }
		
		public List<? extends Trigger> getVariables(){ return variables; }
		
		public boolean applyToEntity(Entity visibleEntity)
		{
			if(!(visibleEntity instanceof LivingEntity))
				return false;
			
			LivingEntity living = (LivingEntity)visibleEntity;
			return testItem(living.getItemBySlot(slot));
		}
		
		protected boolean testItem(ItemStack stack)
		{
			if(!stack.isEmpty())
			{
				for(TriggerItem variable : variables)
					if(!variable.applyToItem(stack))
						return false;
				return true;
			}
			return false;
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putString("Slot", slot.getName());
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			slot = EquipmentSlot.valueOf(compound.getString("Slot"));
		}
	}
	
	/**
	 * Check for an entity holding a specific item.
	 */
	public static class TriggerEntityHeldItem extends TriggerEntityEquipment
	{
		public String type(){ return "entity_held"; }
		
		public TriggerEntityHeldItem(){ }
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"entity_held_item" + (inverted ? "_inverted" : "")); }
		
		public boolean applyToEntity(Entity visibleEntity)
		{
			if(!(visibleEntity instanceof LivingEntity))
				return false;
			
			LivingEntity living = (LivingEntity)visibleEntity;
			return testItem(living.getItemBySlot(EquipmentSlot.MAINHAND)) || testItem(living.getItemBySlot(EquipmentSlot.OFFHAND));
		}
	}
	
	/**
	 * Check for an entity riding a specific entity.
	 */
	public static class TriggerEntityMount extends TriggerEntity
	{
		public String type(){ return "entity_mount"; }
		
		public TriggerEntityMount(){ }
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"entity_mount" + (inverted ? "_inverted" : "")); }
		
		public boolean applyToEntity(Entity visibleEntity)
		{
			if(visibleEntity.getVehicle() != null)
			{
				Entity mount = visibleEntity.getVehicle();
				for(TriggerEntity variable : variables)
					if(variable.applyToEntity(mount) == variable.inverted())
						return false;
				return true;
			}
			return false;
		}
	}
}
