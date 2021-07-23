package com.lying.variousoddities.species.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.types.EnumCreatureType.Action;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemTier;
import net.minecraft.potion.Effect;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * A handler class that applies and defines the properties of different creature types
 * @author Lying
 *
 */
public class TypeHandler
{
	public final UUID sourceID;
	
	private final List<Ability> abilities = Lists.newArrayList();
	
	public TypeHandler(UUID idIn)
	{
		this.sourceID = idIn;
	}
	
	/** Returns a default instance */
	public static final TypeHandler get(UUID idIn){ return new TypeHandler(idIn); }
	
	/** Called when the type is first applied in LivingData */
	public void onApply(LivingEntity entity){ }
	/** Called when the type is removed in LivingData */
	public void onRemove(LivingEntity entity){ }
	
	public TypeHandler addAbility(Ability abilityIn)
	{
		this.abilities.add(abilityIn.setSourceId(this.sourceID));
		return this;
	}
	
	public List<Ability> getAbilities(){ return this.abilities; }
	
	public ITextComponent getDetails()
	{
		if(abilities.isEmpty())
			return new StringTextComponent("");
		
		Collections.sort(abilities, Ability.SORT_ABILITY);
		IFormattableTextComponent details = new StringTextComponent("\n");
		for(int i=0; i<abilities.size(); i++)
		{
			Ability ability = abilities.get(i);
			details.append(new StringTextComponent("  ").append(ability.getDisplayName()));
			if(i < abilities.size() - 1)
				details.append(new StringTextComponent("\n"));
		}
		return details;
	}
	
	/** Used by subtypes to modifer the action set from supertypes */
	public  EnumSet<Action> applyActions(EnumSet<Action> actions, Collection<EnumCreatureType> types){ return actions; }
	
	/** Used in commands to determine if a given type can be added to an existing set */
	public  boolean canApplyTo(Collection<EnumCreatureType> types){ return true; }
	
	public static enum DamageResist implements IStringSerializable
	{
		NORMAL(0F),
		VULNERABLE(0.5F),
		RESISTANT(-0.5F),
		IMMUNE(-1F);
		
		private final float mult;
		
		private DamageResist(float multIn)
		{
			this.mult = multIn;
		}
		
		public DamageResist add(DamageResist resistA)
		{
			if(resistA == IMMUNE || this == IMMUNE)
				return IMMUNE;
			
			float add = resistA.mult + this.mult;
			return add > 0 ? VULNERABLE : add < 0 ? RESISTANT : NORMAL;
		}
		
		public float apply(float amount){ return amount * (1F + mult); }
		
		public float val(){ return mult; }
		
		public String getString(){ return name().toLowerCase(); }
		
		public ITextComponent getTranslated(DamageType typeIn)
		{
			return new TranslationTextComponent("enum.varodd.damage_resist."+getString(), typeIn.getTranslated());
		}
		
		public static DamageResist fromString(String str)
		{
	    	for(DamageResist val : values())
	    		if(val.getString().equalsIgnoreCase(str))
	    			return val;
	    	return null;
		}
	}
	
	/** Common operations between types */
	public static class TypeUtils
	{
		public static final List<Effect> EMPTY_POTIONS = new ArrayList<Effect>();
		
		public static ItemTier getMaterial(String name)
		{
			for(ItemTier material : ItemTier.values())
				if(material.name().equals(name))
					return material;
			return null;
		}
	}
}