package com.lying.variousoddities.magic.trigger;

import java.util.ArrayList;
import java.util.Collection;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
		
		public ITextComponent getTranslated(boolean inverted){ return new TranslationTextComponent("trigger."+Reference.ModInfo.MOD_PREFIX+"audible_chat" + (inverted ? "_inverted" : ""), message); }
		
		public boolean applyToChat(String chatMessage)
		{
			return (chatMessage.length() > 0 && message.length() == 0) || chatMessage.equals(message);
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			compound.putString("Message", message);
			return compound;
		}
		
		public void readFromNBT(CompoundNBT compound)
		{
			message = compound.getString("Message");
		}
	}
	
	public static class TriggerAudibleSound extends TriggerAudible
	{
		private ResourceLocation sound = null;
		
		public String type(){ return "sound"; }
		
		public TriggerAudibleSound(){ }
		public TriggerAudibleSound(SoundEvent soundIn)
		{
			sound = soundIn.getRegistryName();
		}
		
		public ITextComponent getTranslated(boolean inverted){ return new TranslationTextComponent("trigger."+Reference.ModInfo.MOD_PREFIX+"audible_sound" + (inverted ? "_inverted" : ""), sound == null ? "" : sound.getNamespace()); }
		
		public boolean applyToSound(SoundEvent heardSound)
		{
			return sound == null || heardSound.getRegistryName().equals(sound);
		}
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			compound.putString("Sound", sound.toString());
			return compound;
		}
		
		public void readFromNBT(CompoundNBT compound)
		{
			sound = new ResourceLocation(compound.getString("Sound"));
		}
	}
}
