package com.lying.variousoddities.species.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.lying.variousoddities.api.event.CreatureTypeEvent.GetEntityTypesEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.GetTypeActionsEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.species.abilities.AbilityDamageReduction;
import com.lying.variousoddities.species.abilities.AbilityDamageResistance;
import com.lying.variousoddities.species.abilities.AbilityIncorporeality;
import com.lying.variousoddities.species.abilities.AbilityResistanceSpell;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;

public enum EnumCreatureType implements IStringSerializable
{
	ABERRATION(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 8),
	AIR(null, new TypeHandler()
		{
			// TODO Convert flight into an ability rather than just adding it as a property of the AIR subtype
			public void onLivingTick(LivingEntity living)
			{
				if(living.getType() == EntityType.PLAYER)
				{
					PlayerEntity player = (PlayerEntity)living;
					if(!VOPotions.isParalysed(player))
					{
						if(!(player.isCreative() || player.isSpectator()) && !player.abilities.allowFlying)
						{
							player.abilities.allowFlying = true;
							player.sendPlayerAbilities();
						}
					}
				}
			}
			
			public void onRemove(LivingEntity living)
			{
				if(living.getType() == EntityType.PLAYER)
				{
					PlayerEntity player = (PlayerEntity)living;
					if(!player.isCreative() && !player.isSpectator())
					{
						player.abilities.allowFlying = false;
						player.sendPlayerAbilities();
					}
				}
			}
		}),
	AMPHIBIOUS(null, new TypeHandler()
		{
			public boolean canApplyTo(Collection<EnumCreatureType> types){ return types.contains(AQUATIC); }
		}),
	ANIMAL(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 8),
	AQUATIC(null, new TypeHandlerAquatic(false)),
	AUGMENTED(),
	COLD(null, new TypeHandler().addAbility(new AbilityDamageResistance(DamageType.COLD, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.FIRE, DamageResist.VULNERABLE))),
	CONSTRUCT(CreatureAttribute.UNDEFINED, new TypeHandler().noCriticalHit().noParalysis().noPoison()
		.addAbility(new AbilityDamageResistance(DamageType.FALLING, DamageResist.IMMUNE))
		.addAbility(new AbilityResistanceSpell(MagicSchool.ENCHANTMENT))
		.addAbility(new AbilityResistanceSpell(MagicSchool.NECROMANCY))
		.addAbility(new AbilityResistanceSpell(MagicSubType.DEATH)), Action.NONE, 10),
	DRAGON(CreatureAttribute.UNDEFINED, new TypeHandler().noParalysis(), Action.STANDARD, 12),
	EARTH(),
	ELEMENTAL(CreatureAttribute.UNDEFINED, new TypeHandler().noCriticalHit().noParalysis().noPoison(), Action.REGEN_ONLY, 8),
	EXTRAPLANAR(),
	EVIL(null, new TypeHandler()
		.addAbility(new AbilityDamageResistance(DamageType.EVIL, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.HOLY, DamageResist.VULNERABLE))),
	FEY(CreatureAttribute.UNDEFINED, new TypeHandler()
		.addAbility(new AbilityDamageReduction(4, DamageType.SILVER)), Action.STANDARD, 6),
	FIRE(null, new TypeHandler()
		.addAbility(new AbilityDamageResistance(DamageType.FIRE, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.COLD, DamageResist.VULNERABLE))),
	GIANT(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 8),
	GOBLIN(),
	HOLY(null, new TypeHandler()
		.addAbility(new AbilityDamageResistance(DamageType.HOLY, DamageResist.IMMUNE))
		.addAbility(new AbilityDamageResistance(DamageType.EVIL, DamageResist.VULNERABLE))),
	HUMANOID(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 8),
	INCORPOREAL(null, new TypeHandler().addAbility(new AbilityIncorporeality())
		.addAbility(new AbilityDamageResistance(DamageType.FALLING, DamageResist.IMMUNE))),
	MAGICAL_BEAST(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 10),
	MONSTROUS_HUMANOID(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 8),
	OUTSIDER(CreatureAttribute.UNDEFINED, new TypeHandler(), EnumSet.of(Action.BREATHE_AIR, Action.REGENERATE), 8),
	NATIVE(null, new TypeHandler()
		{
			public EnumSet<Action> applyActions(EnumSet<Action> actions, Collection<EnumCreatureType> types){ actions.addAll(Arrays.asList(Action.SLEEP, Action.EAT)); return actions; }
			public boolean canApplyTo(Collection<EnumCreatureType> types){ return types.contains(OUTSIDER); }
		}),
	PLANT(CreatureAttribute.UNDEFINED, new TypeHandler().noCriticalHit().noParalysis().noPoison()
		.addAbility(new AbilityDamageResistance(DamageType.FIRE, DamageResist.VULNERABLE))
		.addAbility(new AbilityResistanceSpell(MagicSchool.ENCHANTMENT))
		.addAbility(new AbilityResistanceSpell(MagicSchool.TRANSMUTATION)), EnumSet.of(Action.BREATHE_AIR, Action.EAT, Action.REGENERATE), 8),
	REPTILE(),
	SHAPECHANGER(),
	OOZE(CreatureAttribute.UNDEFINED, new TypeHandler().noCriticalHit().noParalysis().noPoison()
		.addAbility(new AbilityResistanceSpell(MagicSchool.TRANSMUTATION)), EnumSet.of(Action.BREATHE_AIR, Action.EAT, Action.REGENERATE), 10),
	UNDEAD(CreatureAttribute.UNDEAD, new TypeHandler().noCriticalHit().noParalysis().noPoison()
		.addAbility(new AbilityDamageResistance(DamageType.HOLY, DamageResist.VULNERABLE))
		.addAbility(new AbilityResistanceSpell(MagicSchool.ENCHANTMENT))
		.addAbility(new AbilityResistanceSpell(MagicSubType.DEATH)), Action.NONE, 12),
	VERMIN(CreatureAttribute.ARTHROPOD, TypeHandler.get(), Action.STANDARD, 8),
	WATER(CreatureAttribute.WATER, new TypeHandlerAquatic(true));
	
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
	private final CreatureAttribute parentAttribute;
	private final boolean supertype;
	private final TypeHandler handler;
	private final EnumSet<Action> actions;
	private final int hitDie;
	
	private EnumCreatureType(CreatureAttribute attribute, TypeHandler handlerIn, EnumSet<Action> actionsIn, int hitDieIn)
	{
		parentAttribute = attribute;
		supertype = true;
		handler = handlerIn;
		actions = actionsIn.clone();
		hitDie = hitDieIn;
	}
	
	private EnumCreatureType()
	{
		this(null, TypeHandler.get());
	}
	
	private EnumCreatureType(CreatureAttribute attribute, TypeHandler handlerIn)
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
			names.add(type.getString());
		return names;
	}
	
	public static List<String> getSubtypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : SUBTYPES)
			names.add(type.getString());
		return names;
	}
	
	public static List<String> getTypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : values())
			names.add(type.getString());
		return names;
	}
	
	public boolean hasParentAttribute(){ return parentAttribute != null; }
	public CreatureAttribute getParentAttribute(){ return parentAttribute; }
	
	public String getName(){ return translationBase+getString(); }
	public String getString(){ return this.name().toLowerCase(); }
	public String getDefinition(){ return translationBase+getString()+".definition"; }
	public String getProperties(){ return translationBase+getString()+".properties"; }
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
	
	/** Returns the translated name of the given creature type, with a hover event displaying its definition */
	public IFormattableTextComponent getTranslated()
	{
		return new TranslationTextComponent(getName()).modifyStyle((style) -> { return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(getDefinition()))); });
	}
	
	public static EnumCreatureType fromName(String nameIn)
	{
		for(EnumCreatureType type : EnumCreatureType.values())
			if(type.getString().equalsIgnoreCase(nameIn))
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
		
		LivingData data = LivingData.forEntity(entity);
		if(data != null)
		{
			// If creature has custom types, use those
			if(data.hasCustomTypes())
				types.addAll(data.getCustomTypes());
			// If creature has species, use its types
			else if(data.hasSpecies())
				types.addAll(data.getTypesFromSpecies());
		}
		
		if(types.isEmpty())
		{
			// Otherwise, default to world settings
			TypesManager manager = TypesManager.get(entity.getEntityWorld());
			if(manager != null)
				types.addAll(manager.getMobTypes(entity.getType()));
		}
		
		// Apply contextual type effects, eg. native vs extraplanar
		GetEntityTypesEvent event = new GetEntityTypesEvent(entity.getEntityWorld(), entity, types);
		MinecraftForge.EVENT_BUS.post(event);
		
		// Final QA: Ensure each entry is unique (usually true, but sometimes broken through GetEntityTypesEvent.set etc.)
		List<EnumCreatureType> typesFinal = Lists.newArrayList();
		event.getTypes().forEach((type) -> { if(!typesFinal.contains(type)) typesFinal.add(type); });
		
		return typesFinal;
	}
	
	public static Types getTypes(LivingEntity entity)
	{
		return new Types(getCreatureTypes(entity));
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
		
		public String toString(){ return actions.toString(); }
		
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
		public boolean breathes(){ return breathesAir() || breathesWater(); }
		public boolean breathesAir(){ return actions.contains(Action.BREATHE_AIR); }
		public boolean breathesWater(){ return actions.contains(Action.BREATHE_WATER); }
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
		
		public static ActionSet fromTypes(LivingEntity entity, Collection<EnumCreatureType> types)
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
			MinecraftForge.EVENT_BUS.post(event);
			
			return event.getActions();
		}
	}
	
	/** Common meaningful properties of creatures that must be acted upon to survive */
	public enum Action
	{
		/** Eating food and starving to death without it */
		EAT,
		/** Sleeping in a bed and spawning phantoms without sleep */
		SLEEP,
		/** Breathing air and drowning without it */
		BREATHE_AIR,
		/** Breathing water and suffocating without it */
		BREATHE_WATER,
		/** Regain health naturally over time */
		REGENERATE;
		
		/** The most common array. */
		public static final EnumSet<Action> STANDARD = EnumSet.of(Action.EAT, Action.SLEEP, Action.BREATHE_AIR, REGENERATE);
		/** Creatures that do not need to eat, sleep, or breathe, but can regenerate health. */
		public static final EnumSet<Action> REGEN_ONLY = EnumSet.of(REGENERATE);
		/** Creatures that have no natural needs, but also cannot regenerate health. */
		public static final EnumSet<Action> NONE = EnumSet.noneOf(Action.class);
		public static final EnumSet<Action> ALL = EnumSet.allOf(Action.class);
	}
}