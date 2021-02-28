package com.lying.variousoddities.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.hostile.EntityScorpionGiant;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.entity.passive.EntityScorpion;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOSoundEvents
{
	private static final List<SoundEvent> REGISTRY = new ArrayList<>();
	private static final Map<Class<? extends LivingEntity>, SoundSet> ENTITY_SOUNDS = new HashMap<>();
	
		// EntityGoblin
	public static final SoundEvent ENTITY_GOBLIN_AMBIENT = createSound("entity_goblin_ambient");
	public static final SoundEvent ENTITY_GOBLIN_ATTACK = createSound("entity_goblin_attack");
	public static final SoundEvent ENTITY_GOBLIN_DEATH = createSound("entity_goblin_death");
	public static final SoundEvent ENTITY_GOBLIN_HURT = createSound("entity_goblin_hurt");
	
		// EntityKobold
	public static final SoundEvent ENTITY_KOBOLD_AMBIENT = createSound("entity_kobold_ambient");
	public static final SoundEvent ENTITY_KOBOLD_HURT = createSound("entity_kobold_hurt");
	public static final SoundEvent ENTITY_KOBOLD_DEATH = createSound("entity_kobold_death");
	
	public static final SoundSet DEFAULT_SOUNDS	 = new SoundSet(SoundEvents.ENTITY_VEX_AMBIENT, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, SoundEvents.ENTITY_PLAYER_DEATH, SoundEvents.ENTITY_PLAYER_HURT, SoundEvents.ENTITY_ZOMBIE_STEP);
	
	private static SoundEvent createSound(String name)
	{
		ResourceLocation loc = new ResourceLocation(Reference.ModInfo.MOD_ID, name);
		SoundEvent sound = new SoundEvent(loc);
		sound.setRegistryName(loc);
		REGISTRY.add(sound);
		return sound;
	}
	
    @SubscribeEvent
    public static void registerSound(RegistryEvent.Register<SoundEvent> event)
    {
    	IForgeRegistry<SoundEvent> registry = event.getRegistry();
    	for(SoundEvent sound : REGISTRY)
    		registry.register(sound);
    }
	
	public static SoundSet getSounds(LivingEntity entityIn){ return getSounds(entityIn.getClass()); }
	public static SoundSet getSounds(Class<? extends LivingEntity> classIn)
	{
		return ENTITY_SOUNDS.containsKey(classIn) ? ENTITY_SOUNDS.get(classIn) : DEFAULT_SOUNDS;
	}
	
	private static void addSounds(Class<? extends LivingEntity> classIn, SoundEvent ambientIn, SoundEvent attackIn, SoundEvent deathIn, SoundEvent hurtIn, SoundEvent stepIn)
	{
		ambientIn	= ambientIn == null	? DEFAULT_SOUNDS.AMBIENT	: ambientIn;
		attackIn	= attackIn == null	? DEFAULT_SOUNDS.ATTACK		: attackIn;
		deathIn		= deathIn == null	? DEFAULT_SOUNDS.DEATH		: deathIn;
		hurtIn		= hurtIn == null	? DEFAULT_SOUNDS.HURT		: hurtIn;
		stepIn		= stepIn == null	? DEFAULT_SOUNDS.STEP		: stepIn;
		ENTITY_SOUNDS.put(classIn,	new SoundSet(ambientIn, attackIn, deathIn, hurtIn, stepIn));
	}
	
	public static class SoundSet
	{
		public final SoundEvent AMBIENT;
		public final SoundEvent ATTACK;
		public final SoundEvent DEATH;
		public final SoundEvent HURT;
		public final SoundEvent STEP;
		
		public SoundSet(SoundEvent ambientIn, SoundEvent attackIn, SoundEvent deathIn, SoundEvent hurtIn, SoundEvent stepIn)
		{
			AMBIENT = ambientIn;
			ATTACK = attackIn;
			DEATH = deathIn;
			HURT = hurtIn;
			STEP = stepIn;
		}
	}
	static
	{
			// Mobs using fully vanilla sounds
		addSounds(EntityRat.class,				SoundEvents.ENTITY_SILVERFISH_AMBIENT, null, SoundEvents.ENTITY_SILVERFISH_DEATH, SoundEvents.ENTITY_SILVERFISH_HURT, SoundEvents.ENTITY_PIG_STEP);
		addSounds(EntityRatGiant.class,			SoundEvents.ENTITY_SILVERFISH_AMBIENT, null, SoundEvents.ENTITY_SILVERFISH_DEATH, SoundEvents.ENTITY_SILVERFISH_HURT, SoundEvents.ENTITY_PIG_STEP);
		
//		addSounds(EntityCrab.class,				SoundEvents.ENTITY_SILVERFISH_AMBIENT, null, SoundEvents.ENTITY_SILVERFISH_DEATH, SoundEvents.ENTITY_SILVERFISH_HURT, SoundEvents.ENTITY_SPIDER_STEP);
//		addSounds(EntityCrabGiant.class,		SoundEvents.ENTITY_SILVERFISH_AMBIENT, null, SoundEvents.ENTITY_SILVERFISH_DEATH, SoundEvents.ENTITY_SILVERFISH_HURT, SoundEvents.ENTITY_SPIDER_STEP);
		addSounds(EntityScorpion.class,			SoundEvents.ENTITY_SILVERFISH_AMBIENT, null, SoundEvents.ENTITY_SILVERFISH_DEATH, SoundEvents.ENTITY_SILVERFISH_HURT, SoundEvents.ENTITY_SILVERFISH_STEP);
		addSounds(EntityScorpionGiant.class,	SoundEvents.ENTITY_SILVERFISH_AMBIENT, null, SoundEvents.ENTITY_SILVERFISH_DEATH, SoundEvents.ENTITY_SILVERFISH_HURT, SoundEvents.ENTITY_SILVERFISH_STEP);
		addSounds(EntityGoblin.class,			ENTITY_GOBLIN_AMBIENT, ENTITY_GOBLIN_ATTACK, ENTITY_GOBLIN_DEATH, ENTITY_GOBLIN_HURT, SoundEvents.ENTITY_ZOMBIE_STEP);
		addSounds(EntityKobold.class,			ENTITY_KOBOLD_AMBIENT, null, ENTITY_KOBOLD_DEATH, ENTITY_KOBOLD_HURT, SoundEvents.ENTITY_ZOMBIE_STEP);
//		addSounds(EntityZombieKobold.class,		SoundEvents.ENTITY_ZOMBIE_AMBIENT, null, SoundEvents.ENTITY_ZOMBIE_DEATH, SoundEvents.ENTITY_ZOMBIE_HURT, SoundEvents.ENTITY_ZOMBIE_STEP);
	}
}
