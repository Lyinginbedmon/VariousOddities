package com.lying.variousoddities.init;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VOSoundEvents
{
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Reference.ModInfo.MOD_ID);
	
		// EntityGoblin
	public static final RegistryObject<SoundEvent> ENTITY_GOBLIN_AMBIENT = createSound("entity_goblin_ambient");
	public static final RegistryObject<SoundEvent> ENTITY_GOBLIN_ATTACK = createSound("entity_goblin_attack");
	public static final RegistryObject<SoundEvent> ENTITY_GOBLIN_DEATH = createSound("entity_goblin_death");
	public static final RegistryObject<SoundEvent> ENTITY_GOBLIN_HURT = createSound("entity_goblin_hurt");
	
		// EntityKobold
	public static final RegistryObject<SoundEvent> ENTITY_KOBOLD_AMBIENT = createSound("entity_kobold_ambient");
	public static final RegistryObject<SoundEvent> ENTITY_KOBOLD_HURT = createSound("entity_kobold_hurt");
	public static final RegistryObject<SoundEvent> ENTITY_KOBOLD_DEATH = createSound("entity_kobold_death");
	
	private static RegistryObject<SoundEvent> createSound(String name)
	{
		return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(Reference.ModInfo.MOD_ID, name)));
	}
}
