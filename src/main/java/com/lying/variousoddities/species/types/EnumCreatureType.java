package com.lying.variousoddities.species.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.lying.variousoddities.api.event.CreatureTypeEvent.GetEntityTypesEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.GetTypeActionsEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.AbilityAmphibious;
import com.lying.variousoddities.species.abilities.AbilityBlind;
import com.lying.variousoddities.species.abilities.AbilityBlindsight;
import com.lying.variousoddities.species.abilities.AbilityBurrow;
import com.lying.variousoddities.species.abilities.AbilityDamageReduction;
import com.lying.variousoddities.species.abilities.AbilityDamageResistance;
import com.lying.variousoddities.species.abilities.AbilityDarkvision;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityFlight.Grade;
import com.lying.variousoddities.species.abilities.AbilityImmunityCrits;
import com.lying.variousoddities.species.abilities.AbilityIncorporeality;
import com.lying.variousoddities.species.abilities.AbilityResistanceSpell;
import com.lying.variousoddities.species.abilities.AbilityStatusImmunity;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.MinecraftForge;

public enum EnumCreatureType implements StringRepresentable
{
	ABERRATION(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("2145fe0a-c00c-405c-9ecf-a5d3e636834c"))
		.addAbility(new AbilityDarkvision()), Action.STANDARD, 8),
	AIR(null, new TypeHandler(UUID.fromString("6c8fc47e-8485-465a-a1a9-c79f125c9286"))
		.addAbility(new AbilityFlight(Grade.PERFECT, 0.7D))),
	AMPHIBIOUS(null, new TypeHandler(UUID.fromString("b4728431-bea6-4d37-894c-d45a629a1109"))
		{
			public boolean canApplyTo(Collection<EnumCreatureType> types){ return types.contains(AQUATIC); }
		}.addAbility(new AbilityAmphibious())),
	ANIMAL(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("af347e37-0c1b-4e89-aaaf-de9f62ee1db2")), Action.STANDARD, 8),
	AQUATIC(null, new TypeHandlerAquatic(UUID.fromString("be78378d-5aeb-43ea-bce7-f1ade0dea14b"), false)),
	AUGMENTED(UUID.fromString("a7d80091-f713-470f-9878-7d2479cf7a2a")),
	COLD(null, new TypeHandler(UUID.fromString("9bef87fa-7f6e-47f0-b227-a9d3a7b7fed1"))
		.addAbility(new AbilityDamageResistance(DamageType.COLD, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.FIRE, DamageResist.VULNERABLE))),
	CONSTRUCT(MobType.UNDEFINED, new TypeHandler(UUID.fromString("423958a4-5b86-4389-990c-f1899925f47a"))
		.addAbility(new AbilityDarkvision())
		.addAbility(new AbilityDamageResistance(DamageType.FALLING, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.NONLETHAL, DamageResist.IMMUNE))
		.addAbility(new AbilityResistanceSpell(MagicSchool.ENCHANTMENT))
		.addAbility(new AbilityResistanceSpell(MagicSchool.NECROMANCY))
		.addAbility(new AbilityResistanceSpell(MagicSubType.DEATH))
		.addAbility(new AbilityStatusImmunity.Poison())
		.addAbility(new AbilityStatusImmunity.Paralysis())
		.addAbility(new AbilityImmunityCrits()), Action.NONE, 10),
	DRAGON(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("cb4c0178-0fa0-44c0-b891-341b7874707e"))
		.addAbility(new AbilityDarkvision())
		.addAbility(new AbilityStatusImmunity.Paralysis()), Action.STANDARD, 12),
	EARTH(null, new TypeHandler(UUID.fromString("c1f5d866-365d-495c-9aa6-c5adaef000de"))
		.addAbility(new AbilityBurrow(true, false))),
	ELEMENTAL(MobType.UNDEFINED, new TypeHandler(UUID.fromString("b0047670-88a2-41a0-aaae-7c9da5a79a4b"))
		.addAbility(new AbilityDarkvision())
		.addAbility(new AbilityStatusImmunity.Poison())
		.addAbility(new AbilityStatusImmunity.Paralysis())
		.addAbility(new AbilityImmunityCrits()), Action.REGEN_ONLY, 8),
	EXTRAPLANAR(UUID.fromString("27f5fb29-f904-4af6-8fcd-1ab39b385813")),
	EVIL(null, new TypeHandler(UUID.fromString("c03386d7-96e2-487c-905d-974777a889ea"))
		.addAbility(new AbilityDamageResistance(DamageType.EVIL, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.HOLY, DamageResist.VULNERABLE))),
	FEY(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("07c3a904-0cc5-43a5-b801-d67445175d4d"))
		.addAbility(new AbilityDamageReduction(4, DamageType.SILVER)), Action.STANDARD, 6),
	FIRE(null, new TypeHandler(UUID.fromString("3c798e03-0af6-4a02-8297-d70eb012427d"))
		.addAbility(new AbilityDamageResistance(DamageType.FIRE, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.COLD, DamageResist.VULNERABLE))),
	GIANT(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("1278a0dc-ba70-4641-b6af-24ba2a815cab")), Action.STANDARD, 8),
	GOBLIN(UUID.fromString("6bc4dbf3-fc00-4419-8a91-5ff4f9a59703")),
	HOLY(null, new TypeHandler(UUID.fromString("7096bdb3-2ab4-4094-9c66-3367d13b9065"))
		.addAbility(new AbilityDamageResistance(DamageType.HOLY, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.EVIL, DamageResist.VULNERABLE))),
	HUMANOID(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("77bc6296-eab6-4ccd-963a-84caeb703e4c")), Action.STANDARD, 8),
	INCORPOREAL(null, new TypeHandler(UUID.fromString("aba0ffd1-cf26-4363-85bd-0866a5fdea10"))
		.addAbility(new AbilityIncorporeality())
		.addAbility(new AbilityDamageResistance(DamageType.FALLING, DamageResist.IMMUNE))),
	MAGICAL_BEAST(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("4e2f8461-9965-43db-ab1b-8de4ce8dcd30"))
		.addAbility(new AbilityDarkvision()), Action.STANDARD, 10),
	MONSTROUS_HUMANOID(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("6f5802ec-231c-48f4-a535-08c64e5aaf0f"))
		.addAbility(new AbilityDarkvision()), Action.STANDARD, 8),
	OUTSIDER(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("b71f6adc-179b-4f13-b1fd-d7f2372f7ad0"))
		.addAbility(new AbilityDarkvision()), EnumSet.of(Action.BREATHES, Action.REGENERATE), 8),
	NATIVE(null, new TypeHandler(UUID.fromString("d7dc8434-e8f3-454d-b51c-2f6c9a24589b"))
		{
			public EnumSet<Action> applyActions(EnumSet<Action> actions, Collection<EnumCreatureType> types){ actions.addAll(Arrays.asList(Action.SLEEP, Action.EAT)); return actions; }
			public boolean canApplyTo(Collection<EnumCreatureType> types){ return types.contains(OUTSIDER); }
		}),
	PLANT(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("a696c570-84ce-46aa-859b-46c4b2c1fa85"))
		.addAbility(new AbilityDamageResistance(DamageType.FIRE, DamageResist.VULNERABLE))
		.addAbility(new AbilityResistanceSpell(MagicSchool.ENCHANTMENT))
		.addAbility(new AbilityResistanceSpell(MagicSchool.TRANSMUTATION))
		.addAbility(new AbilityStatusImmunity.Poison())
		.addAbility(new AbilityStatusImmunity.Paralysis())
		.addAbility(new AbilityImmunityCrits()), EnumSet.of(Action.EAT, Action.REGENERATE), 8),
	REPTILE(UUID.fromString("b7b6e1bc-d1c5-4c6c-af9d-994b73bf73d8")),
	SHAPECHANGER(UUID.fromString("b9956455-2bd2-456d-8e3e-976864fb23e6")),
	OOZE(MobType.UNDEFINED, TypeHandler.getBreathesAir(UUID.fromString("38debdac-03a3-432f-8f77-66a09417e44b"))
		.addAbility(new AbilityResistanceSpell(MagicSchool.TRANSMUTATION))
		.addAbility(new AbilityBlind())
		.addAbility(new AbilityBlindsight(16D))
		.addAbility(new AbilityStatusImmunity.Poison())
		.addAbility(new AbilityStatusImmunity.Paralysis())
		.addAbility(new AbilityImmunityCrits()), EnumSet.of(Action.EAT, Action.BREATHES, Action.REGENERATE), 10),
	UNDEAD(MobType.UNDEAD, new TypeHandler(UUID.fromString("1e5ae3b8-b509-447f-b790-51ed71d090b7"))
		.addAbility(new AbilityDarkvision())
		.addAbility(new AbilityDamageResistance(DamageType.HOLY, DamageResist.VULNERABLE))
		.addAbility(new AbilityDamageResistance(DamageType.NONLETHAL, DamageResist.IMMUNE))
		.addAbility(new AbilityResistanceSpell(MagicSchool.ENCHANTMENT))
		.addAbility(new AbilityResistanceSpell(MagicSubType.DEATH))
		.addAbility(new AbilityResistanceSpell(MagicSubType.FEAR))
		.addAbility(new AbilityStatusImmunity.Poison())
		.addAbility(new AbilityStatusImmunity.Paralysis())
		.addAbility(new AbilityImmunityCrits()), Action.NONE, 12),
	VERMIN(MobType.ARTHROPOD, TypeHandler.getBreathesAir(UUID.fromString("fb82a222-6b50-4d48-b6aa-5ea1875008ef"))
		.addAbility(new AbilityDarkvision()), Action.STANDARD, 8),
	WATER(MobType.WATER, new TypeHandlerAquatic(UUID.fromString("98c06c81-ed84-4974-bb47-e5d728ce83b8"), true));
	
	public static final Predicate<EnumCreatureType> IS_SUPERTYPE = new Predicate<EnumCreatureType>()
		{
			public boolean apply(EnumCreatureType input){ return input.isSupertype(); }
		};
	public static final Predicate<EnumCreatureType> IS_SUBTYPE = new Predicate<EnumCreatureType>()
		{
			public boolean apply(EnumCreatureType input){ return !input.isSupertype(); }
		};
	
	public static final EnumSet<EnumCreatureType> SUPERTYPES = EnumSet.of(ABERRATION, ANIMAL, CONSTRUCT, DRAGON, ELEMENTAL, FEY, GIANT, HUMANOID, MAGICAL_BEAST, MONSTROUS_HUMANOID, OUTSIDER, PLANT, OOZE, UNDEAD, VERMIN);
	public static final EnumSet<EnumCreatureType> SUBTYPES = EnumSet.complementOf(SUPERTYPES);
	
	public static final String translationBase = "enum.varodd.creature_type.";
	private final MobType parentAttribute;
	private final boolean supertype;
	private final TypeHandler handler;
	private final EnumSet<Action> actions;
	private final int hitDie;
	
	private EnumCreatureType(MobType attribute, TypeHandler handlerIn, EnumSet<Action> actionsIn, int hitDieIn)
	{
		parentAttribute = attribute;
		supertype = true;
		handler = handlerIn;
		actions = actionsIn.clone();
		hitDie = hitDieIn;
	}
	
	private EnumCreatureType(UUID idIn)
	{
		this(null, TypeHandler.get(idIn));
	}
	
	private EnumCreatureType(MobType attribute, TypeHandler handlerIn)
	{
		parentAttribute = attribute;
		supertype = false;
		handler = handlerIn;
		actions = Action.NONE;
		hitDie = 8;
	}
	
	public static List<String> getSupertypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : SUPERTYPES)
			names.add(type.getSerializedName());
		return names;
	}
	
	public static List<String> getSubtypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : SUBTYPES)
			names.add(type.getSerializedName());
		return names;
	}
	
	public static List<String> getTypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : values())
			names.add(type.getSerializedName());
		return names;
	}
	
	public boolean hasParentAttribute(){ return parentAttribute != null; }
	public MobType getParentAttribute(){ return parentAttribute; }
	
	public String getName(){ return translationBase+getSerializedName(); }
	public String getSerializedName(){ return this.name().toLowerCase(); }
	public String getDefinition(){ return translationBase+getSerializedName()+".definition"; }
	public String getProperties(){ return translationBase+getSerializedName()+".properties"; }
	public String getType(){ return translationBase + (supertype ? "supertype" : "subtype"); }
	public boolean canApplyTo(Collection<EnumCreatureType> types){ return getHandler().canApplyTo(types); }
	public TypeHandler getHandler(){ return this.handler; }
	public boolean isSupertype(){ return this.supertype; }
	
	public EnumSet<Action> actions(){ return actions; }
	
	public double healthModForPlayer()
	{
		if(!isSupertype()) return 0D;
		return (((double)getHitDie() / (double)HUMANOID.getHitDie()) * Attributes.MAX_HEALTH.getDefaultValue()) - Attributes.MAX_HEALTH.getDefaultValue();
	}
	public int getHitDie(){ return hitDie; }
	
	/** Returns the translated name of the given creature type, with a hover event displaying its definition and optionally its abilities */
	public MutableComponent getTranslated(boolean details)
	{
		MutableComponent displayText = Component.translatable(getDefinition());
		if(details)
			displayText.append(this.handler.getDetails());
		
		return Component.translatable(getName()).withStyle((style) -> 
		{
			return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, displayText));
		});
	}
	
	public static EnumCreatureType fromName(String nameIn)
	{
		for(EnumCreatureType type : EnumCreatureType.values())
			if(type.getSerializedName().equalsIgnoreCase(nameIn))
				return type;
		return null;
	}
	
	public static List<EnumCreatureType> stringToTypes(String[] namesIn)
	{
		List<EnumCreatureType> types = new ArrayList<EnumCreatureType>();
		for(String name : namesIn)
			if(fromName(name) != null)
				types.add(fromName(name));
		java.util.Collections.sort(types);
		return types;
	}
	
	public static List<EnumCreatureType> getCreatureTypes(@Nullable LivingEntity entity)
	{
		List<EnumCreatureType> types = Lists.newArrayList();
		if(entity == null || !TypeBus.shouldFire())
			return types;
		
		LivingData data = LivingData.getCapability(entity);
		if(data != null)
		{
			// If creature has custom types, use those
			if(data.hasCustomTypes())
				types.addAll(data.getCustomTypes());
			// If creature has species, use its types
			else if(data.hasSpecies())
				types.addAll(data.getTypesFromSpecies());
			
			if(data.hasTemplates())
			{
				for(Template template : data.getTemplates())
					template.applyTypeOperations(types);
			}
		}
		
		if(types.isEmpty())
		{
			// Otherwise, default to world settings
			TypesManager manager = TypesManager.get(entity.getLevel());
			if(manager != null)
				types.addAll(manager.getMobTypes(entity.getType()));
			
			if(types.isEmpty())
			{
				MobType attribute = entity.getMobType();
				if(attribute == MobType.UNDEAD)
					types.add(EnumCreatureType.UNDEAD);
				else if(attribute == MobType.ARTHROPOD)
					types.add(EnumCreatureType.VERMIN);
				else
					types.add(EnumCreatureType.HUMANOID);
				
				if(attribute == MobType.WATER)
					types.add(EnumCreatureType.AQUATIC);
			}
		}
		
		// Apply contextual type effects, eg. native vs extraplanar
		GetEntityTypesEvent event = new GetEntityTypesEvent(entity.getLevel(), entity, types);
		MinecraftForge.EVENT_BUS.post(event);
		
		// Final QA: Ensure each entry is unique (usually true, but sometimes broken through GetEntityTypesEvent.set etc.)
		List<EnumCreatureType> typesFinal = Lists.newArrayList();
		event.getTypes().forEach((type) -> { if(!typesFinal.contains(type)) typesFinal.add(type); });
		
		return typesFinal;
	}
	
	public static Types getTypes(@Nullable LivingEntity entity)
	{
		return new Types(getCreatureTypes(entity));
	}
	
	public static Types getCustomTypes(@Nullable LivingEntity entity)
	{
		List<EnumCreatureType> types = Lists.newArrayList();
		
		if(entity == null || !TypeBus.shouldFire())
			return new Types(types);
		
		LivingData data = LivingData.getCapability(entity);
		if(data != null)
		{
			// If creature has custom types, use those
			if(data.hasCustomTypes())
				types.addAll(data.getCustomTypes());
		}
		
		return new Types(types);
	}
	
	public static class ActionSet
	{
		private EnumSet<Action> actions = Action.NONE.clone();
		
		public ActionSet(){ }
		public ActionSet(EnumSet<Action> actions)
		{
			super();
			add(actions);
		}
		
		public boolean isEmpty() { return this.actions.isEmpty(); }
		public int size() { return this.actions.size(); }
		
		public String toString(){ return actions.toString(); }
		
		@Nullable
		public Component translated()
		{
			MutableComponent translated = null;
			if(!actions.isEmpty())
				translated = Component.translatable("enum.varodd.type_action.does", actionsToList(actions));
			
			if(EnumSet.complementOf(actions).size() > 0)
			{
				MutableComponent doesnt = Component.translatable("enum.varodd.type_action.doesnt", actionsToList(EnumSet.complementOf(actions)));
				if(translated == null)
					translated = doesnt;
				else
				{
					translated.append("\n");
					translated.append(doesnt);
				}
			}
			
			return translated;
		}
		
		@Nullable
		private static Component actionsToList(EnumSet<Action> actions)
		{
			MutableComponent actionSet = null;
			for(Action action : actions)
			{
				MutableComponent name = Component.literal(action.translated().getString().toLowerCase());
				if(actionSet != null)
					actionSet.append(", ");
				else
					actionSet = Component.literal("");
				actionSet.append(name);
			}
			return actionSet;
		}
		
		public void applyType(TypeHandler handler, Collection<EnumCreatureType> types)
		{
			actions = handler.applyActions(actions, types);
		}
		
		public boolean contains(Action actionIn){ return this.actions.contains(actionIn); }
		
		public void add(Action actionIn)
		{
			if(!actions.contains(actionIn))
				actions.add(actionIn);
		}
		
		public void add(EnumSet<Action> actionsIn)
		{
			for(Action action : actionsIn)
				add(action);
		}
		
		public void remove(Action actionIn)
		{
			if(actions.contains(actionIn))
				actions.remove(actionIn);
		}
		
		public void remove(EnumSet<Action> actionsIn)
		{
			for(Action action : actionsIn)
				remove(action);
		}
		
		public boolean eats(){ return actions.contains(Action.EAT); }
		public boolean sleeps(){ return actions.contains(Action.SLEEP); }
		
		public boolean breathes(){ return actions.contains(Action.BREATHES); }
		
		public boolean regenerates(){ return actions.contains(Action.REGENERATE); }
		
		public static ActionSet fromSupertypes(Collection<EnumCreatureType> types)
		{
			ActionSet set = new ActionSet();
			types.removeIf(IS_SUBTYPE);
			
			// Actions shared by all supertypes
			EnumSet<Action> actions = EnumSet.allOf(Action.class);
			for(EnumCreatureType type : types)
			{
				EnumSet<Action> typeActions = type.actions();
				EnumSet<Action> remove = EnumSet.noneOf(Action.class);
				for(Action action : actions)
					if(!typeActions.contains(action))
						remove.add(action);
				actions.removeAll(remove);
			}
			set.add(actions);
			
			return set;
		}
		
		public static ActionSet fromTypes(@Nullable LivingEntity entity, Collection<EnumCreatureType> types)
		{
			if(types.isEmpty())
				return new ActionSet(Action.STANDARD);
			
			List<EnumCreatureType> supertypes = new ArrayList<>();
			supertypes.addAll(types);
			supertypes.removeIf(IS_SUBTYPE);
			
			ActionSet set = fromSupertypes(supertypes);
			
			List<EnumCreatureType> subtypes = new ArrayList<>();
			subtypes.addAll(types);
			subtypes.removeIf(IS_SUPERTYPE);
			// Modifiers to actions from subtypes
			subtypes.forEach((type) -> 
				{
					set.applyType(type.getHandler(), types);
				});
			
			GetTypeActionsEvent event = new GetTypeActionsEvent(entity, types, set.actions);
			if(entity != null)
				MinecraftForge.EVENT_BUS.post(event);
			
			return event.getActions();
		}
	}
	
	/** Common meaningful properties of creatures that must be acted upon to survive */
	public enum Action
	{
		/** Eating food and starving to death without it */
		EAT(0),
		/** Sleeping in a bed and spawning phantoms without sleep */
		SLEEP(1),
		/** Breathing at least one fluid (or air) */
		BREATHES(3),
		/** Regain health naturally over time */
		REGENERATE(2);
		
		private final int iconIndex;
		
		private Action(int index)
		{
			this.iconIndex = index;
		}
		
		/** The most common array. */
		public static final EnumSet<Action> STANDARD = EnumSet.of(Action.EAT, Action.SLEEP, Action.BREATHES, REGENERATE);
		/** Creatures that do not need to eat, sleep, or breathe, but can regenerate health. */
		public static final EnumSet<Action> REGEN_ONLY = EnumSet.of(REGENERATE);
		/** Creatures that have no natural needs, but also cannot regenerate health. */
		public static final EnumSet<Action> NONE = EnumSet.noneOf(Action.class);
		public static final EnumSet<Action> ALL = EnumSet.allOf(Action.class);
		
		public Component translated() { return Component.translatable("enum.varodd.type_action."+name().toLowerCase()); }
		
		public int index(){ return this.iconIndex; }
	}
}