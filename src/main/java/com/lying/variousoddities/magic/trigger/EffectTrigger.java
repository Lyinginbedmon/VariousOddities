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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
	
	public EffectTrigger(CompoundTag compound)
	{
		if(compound.contains("Time"))
			variableTime = (TriggerTime)Trigger.createTriggerFromNBT(compound.getCompound("Time"));
		
		if(compound.contains("Chat"))
			variableChat = (TriggerAudibleChat)Trigger.createTriggerFromNBT(compound.getCompound("Chat"));
		
		if(compound.contains("Sound"))
			variableSound = (TriggerAudibleSound)Trigger.createTriggerFromNBT(compound.getCompound("Sound"));
		
		if(compound.contains("Entities"))
		{
			ListTag entities = compound.getList("Entities", 10);
			for(int i=0; i<entities.size(); i++)
			{
				CompoundTag triggerData = entities.getCompound(i);
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
			ListTag blocks = compound.getList("Blocks", 10);
			for(int i=0; i<blocks.size(); i++)
			{
				CompoundTag triggerData = blocks.getCompound(i);
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
		VariousOddities.log.info(Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"base").getString());
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
		String name = Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"base").getString();
		
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
	public boolean applyToContext(Collection<Entity> visibleEntities, SoundEvent heardSound, String chatMessage, Level world)
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
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		if(variableTime != null)
			compound.put("Time", Trigger.writeTriggerToNBT(variableTime, new CompoundTag()));
		
		if(variableChat != null)
			compound.put("Chat", Trigger.writeTriggerToNBT(variableChat, new CompoundTag()));
		
		if(variableSound != null)
			compound.put("Sound", Trigger.writeTriggerToNBT(variableSound, new CompoundTag()));
		
		if(!variableEntities.isEmpty())
		{
			ListTag entities = new ListTag();
			for(TriggerEntity trigger : variableEntities)
				entities.add(Trigger.writeTriggerToNBT(trigger, new CompoundTag()));
			compound.put("Entities", entities);
		}
		
		if(!variableBlocks.isEmpty())
		{
			ListTag blocks = new ListTag();
			for(TriggerBlock trigger : variableBlocks)
				blocks.add(Trigger.writeTriggerToNBT(trigger, new CompoundTag()));
			compound.put("Blocks", blocks);
		}
		
		return compound;
	}
	
	public static class TriggerContext
	{
		private Level world;
		private Collection<Entity> visibleEntities = new ArrayList<>();
		private SoundEvent heardSound;
		private String chatMessage;
		
		public static TriggerContext buildContextForEntity(Entity ent, double range)
		{
			Vec3 eyePos = new Vec3(ent.getX(), ent.getY() + (ent instanceof LivingEntity ? ent.getEyeHeight() : ent.getBbHeight() / 2F), ent.getZ());
			return buildContextForPosition(eyePos, ent.getLevel(), range);
		}
		
		public static TriggerContext buildContextForPosition(BlockPos pos, Level world, double range)
		{
			return buildContextForPosition(new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D), world, range);
		}
		
		public static TriggerContext buildContextForPosition(Vec3 pos, Level world, double range)
		{
			TriggerContext context = new TriggerContext();
			context.world = world;
			AABB bounds = new AABB(pos.x - range, pos.y - range, pos.z - range, pos.x + range, pos.y + range, pos.z + range);
			for(Entity ent : world.getEntitiesOfClass(Entity.class, bounds))
			{
				if(ent.isInvisible())
					continue;
				
				Vec3 entCore = new Vec3(ent.getX(), ent.getY() + ent.getBbHeight() / 2D, ent.getZ());
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
