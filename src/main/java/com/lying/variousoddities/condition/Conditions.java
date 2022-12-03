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
	public static final RegistryObject<Condition> AFRAID = VORegistries.CONDITIONS.register("afraid", () -> new ConditionMindAffecting(MagicSubType.FEAR)
			{
				public ResourceLocation getIconTexture(boolean affecting)
				{
					return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/condition/afraid"+(affecting ? "" : "_source")+".png");
				}
			});
	public static final RegistryObject<Condition> CHARMED = VORegistries.CONDITIONS.register("charmed", () -> new ConditionMindAffecting(MagicSchool.ENCHANTMENT));
	public static final RegistryObject<Condition> DOMINATED = VORegistries.CONDITIONS.register("dominated", () -> new ConditionMindAffecting(MagicSchool.ENCHANTMENT)
			{
				public ResourceLocation getIconTexture(boolean affecting)
				{
					return new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/condition/dominated"+(affecting ? "" : "_source")+".png");
				}
			});
	
	public static void init() { }
	
	public static Condition getByRegistryName(@Nonnull ResourceLocation registryName)
	{
		return VORegistries.CONDITIONS_REGISTRY.get().getValue(registryName);
	}
}
