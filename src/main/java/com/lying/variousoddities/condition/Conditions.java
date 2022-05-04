package com.lying.variousoddities.condition;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class Conditions
{
	private static final List<Condition> conditions = Lists.newArrayList();
	
	public static final Condition AFRAID = register(new ResourceLocation(Reference.ModInfo.MOD_ID, "afraid"), new ConditionMindAffecting(MagicSubType.FEAR));
	public static final Condition CHARMED = register(new ResourceLocation(Reference.ModInfo.MOD_ID, "charmed"), new ConditionMindAffecting(MagicSchool.ENCHANTMENT));
	public static final Condition DOMINATED = register(new ResourceLocation(Reference.ModInfo.MOD_ID, "dominated"), new ConditionMindAffecting(MagicSchool.ENCHANTMENT));
	
	private static Condition register(ResourceLocation nameIn, Condition conditionIn)
	{
		conditionIn.setRegistryName(nameIn);
		conditions.add(conditionIn);
		return conditionIn;
	}
	
	public static void onRegisterConditions(RegistryEvent.Register<Condition> event)
	{
		IForgeRegistry<Condition> registry = event.getRegistry();
		
		conditions.forEach((condition) -> { registry.register(condition); });
		
		VariousOddities.log.info("Initialised "+registry.getEntries().size()+" conditions");
		if(ConfigVO.GENERAL.verboseLogs())
			for(ResourceLocation name : registry.getKeys())
				VariousOddities.log.info("#   "+name.toString());
	}
	
	public static Condition getByRegistryName(@Nonnull ResourceLocation registryName)
	{
		return VORegistries.CONDITIONS.containsKey(registryName) ? VORegistries.CONDITIONS.getValue(registryName) : null;
	}
	
	public static Collection<Condition> getAllConditions(){ return VORegistries.CONDITIONS.getValues(); }
}
