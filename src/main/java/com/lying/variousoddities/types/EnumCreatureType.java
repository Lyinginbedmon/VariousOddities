package com.lying.variousoddities.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.types.TypeHandler.EnumDamageResist;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.TieredItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;

public enum EnumCreatureType
{
	ABERRATION(null, TypeHandler.get(), Action.ALL),
	AIR(),
	AMPHIBIOUS(null, new TypeHandler()
		{
			public boolean canApplyTo(List<EnumCreatureType> types){ return types.contains(AQUATIC); }
		}),
	ANIMAL(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.ALL),
	AQUATIC(null, new TypeHandlerAquatic()),
	AUGMENTED(),
	COLD(null, new TypeHandler().addResistance(VODamageSource.COLD, EnumDamageResist.IMMUNE).setFireResist(EnumDamageResist.VULNERABLE)),
	CONSTRUCT(null, new TypeHandler()
	{
		public EnumDamageResist getDamageResist(DamageSource source)
		{
			return VODamageSource.isFalling(source) ? EnumDamageResist.IMMUNE : super.getDamageResist(source);
		}
		public void onMobUpdateEvent(LivingEntity living){ TypeUtils.preventNaturalRegen(living); }
		public boolean canSpellAffect(IMagicEffect spellIn)
		{
			if(spellIn.getSchool() == MagicSchool.ENCHANTMENT) return false;
			if(spellIn.getSchool() == MagicSchool.NECROMANCY || spellIn.getDescriptors().contains(MagicSubType.DEATH)) return false;
			return true;
		}
	}.noCriticalHit().noParalysis().noPoison(), Action.NONE),
	DRAGON(null, new TypeHandler().noParalysis(), Action.ALL),
	EARTH(),
	ELEMENTAL(null, new TypeHandler().noCriticalHit().noParalysis().noPoison(), Action.NONE),
	EXTRAPLANAR(),
	EVIL(null, new TypeHandler().addResistance(VODamageSource.EVIL, EnumDamageResist.IMMUNE).addResistance(VODamageSource.HOLY, EnumDamageResist.VULNERABLE)),
	FEY(null, new TypeHandler()
		{
			public EnumDamageResist getDamageResist(DamageSource source)
			{
				if(source instanceof EntityDamageSource)
				{
					Entity attacker = source.getTrueSource();
					if(attacker != null && attacker instanceof LivingEntity)
					{
						ItemStack heldItem = ((LivingEntity)attacker).getHeldItemMainhand();
						if(!heldItem.isEmpty())
						{
							Item held = heldItem.getItem();
							IItemTier itemTier = held instanceof TieredItem ? ((TieredItem)held).getTier() : null;
							if(itemTier != null && (itemTier == ItemTier.IRON || itemTier.toString().toLowerCase().contains("silver")))
								return EnumDamageResist.VULNERABLE;
						}
					}
				}
				return super.getDamageResist(source);
			}
		}, Action.ALL),
	FIRE(null, new TypeHandler().setFireResist(EnumDamageResist.IMMUNE).addResistance(VODamageSource.COLD, EnumDamageResist.VULNERABLE)),
	GIANT(null, TypeHandler.get(), Action.ALL),
	GOBLIN(),
	HOLY(null, new TypeHandler().addResistance(VODamageSource.HOLY, EnumDamageResist.IMMUNE).addResistance(VODamageSource.EVIL, EnumDamageResist.VULNERABLE)),
	HUMANOID(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.ALL),
	MAGICAL_BEAST(null, TypeHandler.get(), Action.ALL),
	MONSTROUS_HUMANOID(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.ALL),
	OUTSIDER(null, new TypeHandler(), EnumSet.of(Action.BREATHE)),
	NATIVE(null, new TypeHandler()
		{
			public EnumSet<Action> applyActions(EnumSet<Action> actions){ actions.addAll(Arrays.asList(Action.SLEEP, Action.EAT)); return actions; }
			public boolean canApplyTo(List<EnumCreatureType> types){ return types.contains(OUTSIDER); }
		}),
	PLANT(null, new TypeHandler()
		{
			public boolean canSpellAffect(IMagicEffect spellIn)
			{
				MagicSchool school = spellIn.getSchool();
				return !(school == MagicSchool.ENCHANTMENT || school == MagicSchool.TRANSMUTATION);
			}
		}.noCriticalHit().noParalysis().noPoison().setFireResist(EnumDamageResist.VULNERABLE), EnumSet.of(Action.BREATHE, Action.EAT)),
	REPTILE(),
	SHAPECHANGER(),
	OOZE(null, new TypeHandler()
		{
			public boolean canSpellAffect(IMagicEffect spellIn)
			{
				return spellIn.getSchool() != MagicSchool.TRANSMUTATION;
			}
		}.noCriticalHit().noParalysis().noPoison(), EnumSet.of(Action.BREATHE, Action.EAT)),
	UNDEAD(CreatureAttribute.UNDEAD, new TypeHandler()
		{
			public void onMobUpdateEvent(LivingEntity event){ TypeUtils.preventNaturalRegen(event); }
			public boolean canSpellAffect(IMagicEffect spellIn)
			{
				return !(spellIn.getSchool() == MagicSchool.ENCHANTMENT || spellIn.getDescriptors().contains(MagicSubType.DEATH));
			}
		}.noCriticalHit().noParalysis().noPoison().addResistance(VODamageSource.HOLY, EnumDamageResist.VULNERABLE), Action.NONE),
	VERMIN(CreatureAttribute.ARTHROPOD, TypeHandler.get(), Action.ALL),
	WATER(CreatureAttribute.WATER, new TypeHandlerAquatic());
	
	private static final List<EnumCreatureType> SUPERTYPES = Arrays.asList(ABERRATION, ANIMAL, CONSTRUCT, DRAGON, ELEMENTAL, FEY, GIANT, HUMANOID, MAGICAL_BEAST, MONSTROUS_HUMANOID, OUTSIDER, PLANT, OOZE, UNDEAD, VERMIN);
	private static final List<EnumCreatureType> SUBTYPES = Arrays.asList(AIR, AQUATIC, AUGMENTED, COLD, EARTH, EXTRAPLANAR, EVIL, FIRE, GOBLIN, HOLY, NATIVE, REPTILE, SHAPECHANGER, WATER);
	
	private static final String translationBase = "enum.varodd.creature_type.";
	private final CreatureAttribute parentAttribute;
	private final boolean supertype;
	private final TypeHandler handler;
	private final EnumSet<Action> actions;
	
	private EnumCreatureType(CreatureAttribute attribute, TypeHandler handlerIn, EnumSet<Action> actionsIn)
	{
		parentAttribute = attribute;
		supertype = true;
		handler = handlerIn;
		actions = actionsIn;
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
	}
	
	public static List<String> getSupertypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : SUPERTYPES)
			names.add(type.getSimpleName().toLowerCase());
		return names;
	}
	
	public static List<String> getSubtypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : SUBTYPES)
			names.add(type.getSimpleName().toLowerCase());
		return names;
	}
	
	public static List<String> getTypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : values())
			names.add(type.getSimpleName().toLowerCase());
		return names;
	}
	
	public boolean hasParentAttribute(){ return parentAttribute != null; }
	public CreatureAttribute getParentAttribute(){ return parentAttribute; }
	
	public String getName(){ return translationBase+getSimpleName(); }
	public String getSimpleName(){ return this.name().toLowerCase(); }
	public String getDefinition(){ return translationBase+getSimpleName()+".definition"; }
	public String getProperties(){ return translationBase+getSimpleName()+".properties"; }
	public String getType(){ return translationBase + (supertype ? "supertype" : "subtype"); }
	public TypeHandler getHandler(){ return this.handler; }
	public boolean isSupertype(){ return this.supertype; }
	
	public EnumSet<Action> actions(){ return actions; }
	
	/** Returns the translated name of the given creature type, with a hover event displaying its definition */
	public IFormattableTextComponent getTranslated()
	{
		return new TranslationTextComponent(getName()).modifyStyle((style) -> { return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(getDefinition()))); });
	}
	
	public static EnumCreatureType fromName(String nameIn)
	{
		for(EnumCreatureType type : EnumCreatureType.values())
			if(type.getSimpleName().equalsIgnoreCase(nameIn))
				return type;
		return null;
	}
	
	public static List<String> names()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : values())
			names.add(type.getSimpleName());
		java.util.Collections.sort(names);
		return names;
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
	
	public static EnumCreatureType getTypeFromAttribute(CreatureAttribute attribute)
	{
		for(EnumCreatureType type : values())
			if(type.getParentAttribute() == attribute)
				return type;
		return null;
	}
	
	/** Converts a list of assorted creature types into a type entry, as in a stat block. */
	public static ITextComponent typesToHeader(Collection<EnumCreatureType> types)
	{
		List<EnumCreatureType> supertypes = new ArrayList<>();
		List<EnumCreatureType> subtypes = new ArrayList<>();
		
		for(EnumCreatureType type : types)
			if(type.isSupertype())
				supertypes.add(type);
			else
				subtypes.add(type);
		java.util.Collections.sort(supertypes);
		java.util.Collections.sort(subtypes);
		
		StringTextComponent supertype = new StringTextComponent("");
		if(supertypes.isEmpty())
			supertype.append(new TranslationTextComponent(translationBase+"no_supertype"));
		else
			for(EnumCreatureType sup : supertypes)
			{
				if(supertype.getSiblings().size() > 0)
					supertype.append(new StringTextComponent(" "));
				supertype.append(sup.getTranslated());
			}
		
		StringTextComponent subtype = new StringTextComponent("");
		if(!subtypes.isEmpty())
		{
			subtype = new StringTextComponent(" (");
			for(EnumCreatureType sup : subtypes)
			{
				if(subtype.getSiblings().size() > 0)
					subtype.append(new StringTextComponent(", "));
				subtype.append(sup.getTranslated());
			}
			subtype.append(new StringTextComponent(")"));
		}
		
		return supertype.append(subtype);
	}
	
	public static class ActionSet
	{
		EnumSet<Action> actions = Action.NONE.clone();
		
		public void applyType(TypeHandler handler)
		{
			actions = handler.applyActions(actions);
		}
		
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
		
		public boolean eats(){ return actions.contains(Action.EAT); }
		public boolean sleeps(){ return actions.contains(Action.SLEEP); }
		public boolean breathes(){ return actions.contains(Action.BREATHE); }
		
		public static ActionSet fromTypes(List<EnumCreatureType> types)
		{
			ActionSet set = new ActionSet();
			for(EnumCreatureType type : types)
				if(type.isSupertype())
					set.add(type.actions());
			
			for(EnumCreatureType type : types)
				if(!type.isSupertype())
					set.applyType(type.getHandler());
			
			return set;
		}
	}
	
	public enum Action
	{
		/** Eating food and starving to death without it */
		EAT,
		/** Sleeping in a bed and spawning phantoms without sleep */
		SLEEP,
		/** Breathing air and drowning without it */
		BREATHE;
		
		private static final EnumSet<Action> ALL = EnumSet.of(Action.EAT, Action.SLEEP, Action.BREATHE);
		private static final EnumSet<Action> NONE = EnumSet.noneOf(Action.class);
	}
}