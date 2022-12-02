package com.lying.variousoddities.condition;

import javax.annotation.Nonnull;

import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public class Conditions
{
	public static final RegistryObject<Condition> AFRAID = register("afraid", new ConditionMindAffecting(MagicSubType.FEAR)
			{
				public ResourceLocation getIconTexture(boolean affecting)
				{
					return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/condition/afraid"+(affecting ? "" : "_source")+".png");
				}
			});
	public static final RegistryObject<Condition> CHARMED = register("charmed", new ConditionMindAffecting(MagicSchool.ENCHANTMENT));
	public static final RegistryObject<Condition> DOMINATED = register("dominated", new ConditionMindAffecting(MagicSchool.ENCHANTMENT)
			{
				public ResourceLocation getIconTexture(boolean affecting)
				{
					return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/condition/dominated"+(affecting ? "" : "_source")+".png");
				}
			});
	
	private static RegistryObject<Condition> register(String nameIn, Condition conditionIn)
	{
		return VORegistries.CONDITIONS.register(nameIn, () -> conditionIn);
	}
	
	public static void init() { }
	
	public static Condition getByRegistryName(@Nonnull ResourceLocation registryName)
	{
		for(RegistryObject<Condition> entry : VORegistries.CONDITIONS.getEntries())
			if(entry.getKey().equals(registryName) && entry.isPresent())
				return entry.get();
		return null;
	}
}
