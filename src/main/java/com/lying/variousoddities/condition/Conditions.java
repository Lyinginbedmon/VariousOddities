package com.lying.variousoddities.condition;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;

public class Conditions
{
	private static final Map<ResourceLocation, Condition> CONDITION_MAP = new HashMap<>();
	
	public static final Condition AFRAID = register(new ResourceLocation(Reference.ModInfo.MOD_ID, "afraid"), new ConditionMindAffecting(MagicSubType.FEAR)
			{
				public ResourceLocation getIconTexture(boolean affecting)
				{
					return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/condition/afraid"+(affecting ? "" : "_source")+".png");
				}
			});
	public static final Condition CHARMED = register(new ResourceLocation(Reference.ModInfo.MOD_ID, "charmed"), new ConditionMindAffecting(MagicSchool.ENCHANTMENT));
	public static final Condition DOMINATED = register(new ResourceLocation(Reference.ModInfo.MOD_ID, "dominated"), new ConditionMindAffecting(MagicSchool.ENCHANTMENT)
			{
				public ResourceLocation getIconTexture(boolean affecting)
				{
					return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/condition/dominated"+(affecting ? "" : "_source")+".png");
				}
			});
	
	private static Condition register(ResourceLocation nameIn, Condition conditionIn)
	{
		VORegistries.CONDITIONS.register(nameIn.toString(), () -> conditionIn);
		CONDITION_MAP.put(nameIn, conditionIn);
		return conditionIn;
	}
	
	public static void init() { }
	
	public static Condition getByRegistryName(@Nonnull ResourceLocation registryName)
	{
		return CONDITION_MAP.containsKey(registryName) ? CONDITION_MAP.get(registryName) : null;
	}
}
