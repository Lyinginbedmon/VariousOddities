package com.lying.variousoddities.magic.trigger;

import java.util.ArrayList;
import java.util.Collection;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public abstract class TriggerAudible extends Trigger
{
	public Collection<? extends Trigger> possibleVariables(){ return new ArrayList<>(); }
	
	public static class TriggerAudibleChat extends TriggerAudible
	{
		private String message = "";
		
		public String type(){ return "chat"; }
		
		public TriggerAudibleChat(){ }
		public TriggerAudibleChat(String messageIn)
		{
			message = messageIn;
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"audible_chat" + (inverted ? "_inverted" : ""), message); }
		
		public boolean applyToChat(String chatMessage)
		{
			return (chatMessage.length() > 0 && message.length() == 0) || chatMessage.equals(message);
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putString("Message", message);
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			message = compound.getString("Message");
		}
	}
	
	public static class TriggerAudibleSound extends TriggerAudible
	{
		private ResourceLocation sound = null;
		
		public String type(){ return "sound"; }
		
		public TriggerAudibleSound(){ }
		@SuppressWarnings("deprecation")
		public TriggerAudibleSound(SoundEvent soundIn)
		{
			sound = Registry.SOUND_EVENT.getKey(soundIn);
		}
		
		public Component getTranslated(boolean inverted){ return Component.translatable("trigger."+Reference.ModInfo.MOD_PREFIX+"audible_sound" + (inverted ? "_inverted" : ""), sound == null ? "" : sound.getNamespace()); }
		
		@SuppressWarnings("deprecation")
		public boolean applyToSound(SoundEvent heardSound)
		{
			return sound == null || Registry.SOUND_EVENT.getKey(heardSound).equals(sound);
		}
		
		public CompoundTag writeToNBT(CompoundTag compound)
		{
			compound.putString("Sound", sound.toString());
			return compound;
		}
		
		public void readFromNBT(CompoundTag compound)
		{
			sound = new ResourceLocation(compound.getString("Sound"));
		}
	}
}
