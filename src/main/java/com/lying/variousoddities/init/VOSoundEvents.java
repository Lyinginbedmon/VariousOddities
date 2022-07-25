package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;

import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.hostile.EntityScorpionGiant;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.entity.passive.EntityScorpion;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class VOSoundEvents
{
	private static final Map<Class<? extends LivingEntity>, SoundSet> SOUNDS = new HashMap<>();
	
		// EntityGoblin
	public static final SoundEvent ENTITY_GOBLIN_AMBIENT = createSound("entity_goblin_ambient");
	public static final SoundEvent ENTITY_GOBLIN_ATTACK = createSound("entity_goblin_attack");
	public static final SoundEvent ENTITY_GOBLIN_DEATH = createSound("entity_goblin_death");
	public static final SoundEvent ENTITY_GOBLIN_HURT = createSound("entity_goblin_hurt");
	
		// EntityKobold
	public static final SoundEvent ENTITY_KOBOLD_AMBIENT = createSound("entity_kobold_ambient");
	public static final SoundEvent ENTITY_KOBOLD_HURT = createSound("entity_kobold_hurt");
	public static final SoundEvent ENTITY_KOBOLD_DEATH = createSound("entity_kobold_death");
	
	public static final SoundSet DEFAULT_SOUNDS	 = new SoundSet(SoundEvents.VEX_AMBIENT, SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundEvents.PLAYER_DEATH, SoundEvents.PLAYER_HURT, SoundEvents.ZOMBIE_STEP);
	
	private static SoundEvent createSound(String name)
	{
		ResourceLocation loc = new ResourceLocation(Reference.ModInfo.MOD_ID, name);
		SoundEvent sound = new SoundEvent(loc);
		ForgeRegistries.SOUND_EVENTS.register(loc, sound);
		return sound;
	}
	
	public static SoundSet getSounds(LivingEntity entityIn){ return getSounds(entityIn.getClass()); }
	public static SoundSet getSounds(Class<? extends LivingEntity> classIn)
	{
		return SOUNDS.containsKey(classIn) ? SOUNDS.get(classIn) : DEFAULT_SOUNDS;
	}
	
	private static void addSounds(Class<? extends LivingEntity> classIn, SoundEvent ambientIn, SoundEvent attackIn, SoundEvent deathIn, SoundEvent hurtIn, SoundEvent stepIn)
	{
		ambientIn	= ambientIn == null	? DEFAULT_SOUNDS.AMBIENT	: ambientIn;
		attackIn	= attackIn == null	? DEFAULT_SOUNDS.ATTACK		: attackIn;
		deathIn		= deathIn == null	? DEFAULT_SOUNDS.DEATH		: deathIn;
		hurtIn		= hurtIn == null	? DEFAULT_SOUNDS.HURT		: hurtIn;
		stepIn		= stepIn == null	? DEFAULT_SOUNDS.STEP		: stepIn;
		SOUNDS.put(classIn,	new SoundSet(ambientIn, attackIn, deathIn, hurtIn, stepIn));
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
		addSounds(EntityRat.class,				SoundEvents.SILVERFISH_AMBIENT, null, SoundEvents.SILVERFISH_DEATH, SoundEvents.SILVERFISH_HURT, SoundEvents.PIG_STEP);
		addSounds(EntityRatGiant.class,			SoundEvents.SILVERFISH_AMBIENT, null, SoundEvents.SILVERFISH_DEATH, SoundEvents.SILVERFISH_HURT, SoundEvents.PIG_STEP);
		
//		addSounds(EntityCrab.class,				SoundEvents.SILVERFISH_AMBIENT, null, SoundEvents.SILVERFISH_DEATH, SoundEvents.SILVERFISH_HURT, SoundEvents.SPIDER_STEP);
//		addSounds(EntityCrabGiant.class,		SoundEvents.SILVERFISH_AMBIENT, null, SoundEvents.SILVERFISH_DEATH, SoundEvents.SILVERFISH_HURT, SoundEvents.SPIDER_STEP);
		addSounds(EntityScorpion.class,			SoundEvents.SILVERFISH_AMBIENT, null, SoundEvents.SILVERFISH_DEATH, SoundEvents.SILVERFISH_HURT, SoundEvents.SILVERFISH_STEP);
		addSounds(EntityScorpionGiant.class,	SoundEvents.SILVERFISH_AMBIENT, null, SoundEvents.SILVERFISH_DEATH, SoundEvents.SILVERFISH_HURT, SoundEvents.SILVERFISH_STEP);
		addSounds(EntityGoblin.class,			ENTITY_GOBLIN_AMBIENT, ENTITY_GOBLIN_ATTACK, ENTITY_GOBLIN_DEATH, ENTITY_GOBLIN_HURT, SoundEvents.ZOMBIE_STEP);
		addSounds(EntityKobold.class,			ENTITY_KOBOLD_AMBIENT, null, ENTITY_KOBOLD_DEATH, ENTITY_KOBOLD_HURT, SoundEvents.ZOMBIE_STEP);
//		addSounds(EntityZombieKobold.class,		SoundEvents.ZOMBIE_AMBIENT, null, SoundEvents.ZOMBIE_DEATH, SoundEvents.ZOMBIE_HURT, SoundEvents.ZOMBIE_STEP);
	}
}
