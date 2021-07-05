package com.lying.variousoddities.species.types;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.species.abilities.Ability;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Convenient holder object for accessing information about a creature's types
 * @author Lying
 */
public class Types
{
	private final List<EnumCreatureType> types = Lists.newArrayList();
	
	public Types(Collection<EnumCreatureType> typesIn)
	{
		this.types.addAll(typesIn);
	}
	
	public boolean hasCustomAttributes(){ return !getAttributes().isEmpty(); }
	
	public List<CreatureAttribute> getAttributes()
	{
		List<CreatureAttribute> attributes = Lists.newArrayList();
		this.types.forEach((type) -> { if(type.hasParentAttribute() && !attributes.contains(type.getParentAttribute())) attributes.add(type.getParentAttribute()); });
		return attributes;
	}
	
	public EnumSet<EnumCreatureType> supertypes()
	{
		EnumSet<EnumCreatureType> supertypes = EnumSet.noneOf(EnumCreatureType.class);
		this.types.forEach((type) -> { if(type.isSupertype()) supertypes.add(type); });
		return supertypes;
	}
	
	public EnumSet<EnumCreatureType> subtypes()
	{
		EnumSet<EnumCreatureType> supertypes = EnumSet.noneOf(EnumCreatureType.class);
		this.types.forEach((type) -> { if(!type.isSupertype()) supertypes.add(type); });
		return supertypes;
	}
	
	public boolean includesType(EnumCreatureType typeIn){ return this.types.contains(typeIn); }
	
	public boolean isEvil()		{ return includesType(EnumCreatureType.EVIL); }
	public boolean isGolem()	{ return includesType(EnumCreatureType.CONSTRUCT); }
	public boolean isHoly()		{ return includesType(EnumCreatureType.HOLY); }
	public boolean isUndead()	{ return includesType(EnumCreatureType.UNDEAD); }
	
	/** Returns how much health a player with these types would have */
	public double getPlayerHealth()
	{
		EnumSet<EnumCreatureType> supertypes = supertypes();
		double hitDieModifier = 0D;
		if(!supertypes.isEmpty())
		{
			for(EnumCreatureType type : supertypes)
			{
				double health = ((double)type.getHitDie() / (double)EnumCreatureType.HUMANOID.getHitDie()) * 20D;
				hitDieModifier += health - 20D;
			}
			hitDieModifier /= Math.max(1, supertypes.size());
		}
		return 20D + hitDieModifier;
	}
	
	public Map<ResourceLocation, Ability> addAbilitiesToMap(Map<ResourceLocation, Ability> map)
	{
		this.types.forEach((type) -> 
			{
				type.getHandler().getAbilities().forEach((ability) -> 
					{
						map.put(ability.getMapName(), ability);
					}); 
			});
		return map;
	}
	
	/** Converts a list of assorted creature types into a type entry, as in a stat block. */
	public ITextComponent toHeader()
	{
		EnumSet<EnumCreatureType> supertypes = supertypes();
		EnumSet<EnumCreatureType> subtypes = subtypes();
		
		StringTextComponent supertype = new StringTextComponent("");
		if(supertypes.isEmpty())
			supertype.append(new TranslationTextComponent(EnumCreatureType.translationBase+"no_supertype"));
		else
			for(EnumCreatureType sup : supertypes)
			{
				if(supertype.getSiblings().size() > 0)
					supertype.append(new StringTextComponent(" "));
				supertype.append(sup.getTranslated(true));
			}
		
		StringTextComponent subtype = new StringTextComponent("");
		if(!subtypes.isEmpty())
		{
			subtype = new StringTextComponent(" (");
			for(EnumCreatureType sup : subtypes)
			{
				if(subtype.getSiblings().size() > 0)
					subtype.append(new StringTextComponent(", "));
				subtype.append(sup.getTranslated(true));
			}
			subtype.append(new StringTextComponent(")"));
		}
		
		return supertype.append(subtype);
	}
}
