package com.lying.variousoddities.magic.trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.magic.trigger.TriggerAudible.TriggerAudibleChat;
import com.lying.variousoddities.magic.trigger.TriggerAudible.TriggerAudibleSound;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class EffectTrigger
{
	private static final List<Trigger> possibleVariables = Arrays.asList(new Trigger[]{new TriggerTime(), new TriggerBlock(), new TriggerEntity(), new TriggerAudibleChat(), new TriggerAudibleSound()});
	
	private TriggerTime variableTime = null;
	private TriggerAudibleChat variableChat = null;
	private TriggerAudibleSound variableSound = null;
	private List<TriggerEntity> variableEntities = new ArrayList<>();
	private List<TriggerBlock> variableBlocks = new ArrayList<>();
	
	private Comparator<Trigger> SIZE_SORT = new Comparator<Trigger>()
			{
				public int compare(Trigger o1, Trigger o2)
				{
					int size1 = o1.totalVariables();
					int size2 = o2.totalVariables();
					return size1 > size2 ? -1 : size1 < size2 ? 1 : 0;
				}
			};
	
	public EffectTrigger()
	{
		
	}
	
	public EffectTrigger(CompoundNBT compound)
	{
		if(compound.contains("Time"))
			variableTime = (TriggerTime)Trigger.createTriggerFromNBT(compound.getCompound("Time"));
		
		if(compound.contains("Chat"))
			variableChat = (TriggerAudibleChat)Trigger.createTriggerFromNBT(compound.getCompound("Chat"));
		
		if(compound.contains("Sound"))
			variableSound = (TriggerAudibleSound)Trigger.createTriggerFromNBT(compound.getCompound("Sound"));
		
		if(compound.contains("Entities"))
		{
			ListNBT entities = compound.getList("Entities", 10);
			for(int i=0; i<entities.size(); i++)
			{
				CompoundNBT triggerData = entities.getCompound(i);
				TriggerEntity trigger = (TriggerEntity)Trigger.createTriggerFromNBT(triggerData);
				if(trigger != null)
					variableEntities.add(trigger);
				else
					VariousOddities.log.warn("Malformed NBT data in trigger: "+triggerData);
			}
			variableEntities.sort(SIZE_SORT);
		}
		
		if(compound.contains("Blocks"))
		{
			ListNBT blocks = compound.getList("Blocks", 10);
			for(int i=0; i<blocks.size(); i++)
			{
				CompoundNBT triggerData = blocks.getCompound(i);
				TriggerBlock trigger = (TriggerBlock)Trigger.createTriggerFromNBT(triggerData);
				if(trigger != null)
					variableBlocks.add(trigger);
				else
					VariousOddities.log.warn("Malformed NBT data in trigger: "+triggerData);
			}
			variableBlocks.sort(SIZE_SORT);
		}
	}
	
	public void print()
	{
		VariousOddities.log.info(new TranslationTextComponent("trigger."+Reference.ModInfo.MOD_PREFIX+"base").getUnformattedComponentText());
		if(variableTime != null)
			variableTime.print();
		if(variableChat != null)
			variableChat.print();
		if(variableSound != null)
			variableSound.print();
		if(!variableEntities.isEmpty())
			for(TriggerEntity trigger : variableEntities)
				trigger.print();
		if(!variableBlocks.isEmpty())
			for(TriggerBlock trigger : variableBlocks)
				trigger.print();
	}
	
	public String toString()
	{
		String name = new TranslationTextComponent("trigger."+Reference.ModInfo.MOD_PREFIX+"base").getUnformattedComponentText();
		
		List<String> variables = new ArrayList<>();
		if(variableTime != null)
			variables.add(variableTime.toString());
		if(variableChat != null)
			variables.add(variableChat.toString());
		if(variableSound != null)
			variables.add(variableSound.toString());
		if(!variableEntities.isEmpty())
			for(TriggerEntity trigger : variableEntities)
				variables.add(trigger.toString());
		if(!variableBlocks.isEmpty())
			for(TriggerBlock trigger : variableBlocks)
				variables.add(trigger.toString());
		
		for(int i=0; i<variables.size(); i++)
			name += (i>0 ? ", " : "") + variables.get(i);
		
		return name;
	}
	
	public boolean applyToContext(TriggerContext context)
	{
		return applyToContext(context.visibleEntities, context.heardSound, context.chatMessage, context.world);
	}
	
	/**
	 * Applies the given context to this trigger's variables.<br>
	 * Returns true if all variables are met.
	 */
	public boolean applyToContext(Collection<Entity> visibleEntities, SoundEvent heardSound, String chatMessage, World world)
	{
		if(variableTime != null && variableTime.applyToTime(world.getGameTime()) == variableTime.inverted())
			return false;
		
		if(variableChat != null && variableChat.applyToChat(chatMessage) == variableTime.inverted())
			return false;
		
		if(variableSound != null && variableSound.applyToSound(heardSound) == variableTime.inverted())
			return false;
		
		for(TriggerBlock block : variableBlocks)
			if(block.applyToBlock(world) == block.inverted())
				return false;
		
		for(TriggerEntity variable : variableEntities)
		{
			Entity validatedWith = null;
			for(Entity ent : visibleEntities)
				if(variable.applyToEntity(ent))
				{
					validatedWith = ent;
					break;
				}
			
			boolean validated = validatedWith != null;
			if(validated == variable.inverted())
				return false;
			
			if(validatedWith != null)
				visibleEntities.remove(validatedWith);
		}
		
		return true;
	}
	
	public EffectTrigger addVariable(Trigger variableIn)
	{
		switch(variableIn.type())
		{
			case "entity":
				variableEntities.add((TriggerEntity)variableIn);
				variableEntities.sort(SIZE_SORT);
				break;
			case "block":
				variableBlocks.add((TriggerBlock)variableIn);
				variableBlocks.sort(SIZE_SORT);
				break;
			case "time":
				variableTime = (TriggerTime)variableIn;
				break;
			case "chat":
				variableChat = (TriggerAudibleChat)variableChat;
				break;
			case "sound":
				variableSound = (TriggerAudibleSound)variableSound;
				break;
			default:
				break;
		}
		
		return this;
	}
	
	public Collection<? extends Trigger> possibleVariables(){ return possibleVariables; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		if(variableTime != null)
			compound.put("Time", Trigger.writeTriggerToNBT(variableTime, new CompoundNBT()));
		
		if(variableChat != null)
			compound.put("Chat", Trigger.writeTriggerToNBT(variableChat, new CompoundNBT()));
		
		if(variableSound != null)
			compound.put("Sound", Trigger.writeTriggerToNBT(variableSound, new CompoundNBT()));
		
		if(!variableEntities.isEmpty())
		{
			ListNBT entities = new ListNBT();
			for(TriggerEntity trigger : variableEntities)
				entities.add(Trigger.writeTriggerToNBT(trigger, new CompoundNBT()));
			compound.put("Entities", entities);
		}
		
		if(!variableBlocks.isEmpty())
		{
			ListNBT blocks = new ListNBT();
			for(TriggerBlock trigger : variableBlocks)
				blocks.add(Trigger.writeTriggerToNBT(trigger, new CompoundNBT()));
			compound.put("Blocks", blocks);
		}
		
		return compound;
	}
	
	public static class TriggerContext
	{
		private World world;
		private Collection<Entity> visibleEntities = new ArrayList<>();
		private SoundEvent heardSound;
		private String chatMessage;
		
		public static TriggerContext buildContextForEntity(Entity ent, double range)
		{
			Vector3d eyePos = new Vector3d(ent.getPosX(), ent.getPosY() + (ent instanceof LivingEntity ? ent.getEyeHeight() : ent.getHeight() / 2F), ent.getPosZ());
			return buildContextForPosition(eyePos, ent.getEntityWorld(), range);
		}
		
		public static TriggerContext buildContextForPosition(BlockPos pos, World world, double range)
		{
			return buildContextForPosition(new Vector3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D), world, range);
		}
		
		public static TriggerContext buildContextForPosition(Vector3d pos, World world, double range)
		{
			TriggerContext context = new TriggerContext();
			context.world = world;
			AxisAlignedBB bounds = new AxisAlignedBB(pos.x - range, pos.y - range, pos.z - range, pos.x + range, pos.y + range, pos.z + range);
			for(Entity ent : world.getEntitiesWithinAABB(Entity.class, bounds))
			{
				if(ent.isInvisible())
					continue;
				
				Vector3d entCore = new Vector3d(ent.getPosX(), ent.getPosY() + ent.getHeight() / 2D, ent.getPosZ());
				if(pos.distanceTo(entCore) > range)
					continue;
				
//				RayTraceResult result = world.rayTraceBlocks(pos, entCore);
//				if(result == null || result.typeOfHit == Type.ENTITY && result.entityHit == ent)
					context.visibleEntities.add(ent);
			}
			
			return context;
		}
		
		public void setChatMessage(String message)
		{
			chatMessage = message;
		}
		
		public void setSoundEvent(SoundEvent event)
		{
			heardSound = event;
		}
	}
}
