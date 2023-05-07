package com.lying.variousoddities.species.types;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.species.abilities.Ability;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobType;

/**
 * Convenient holder object for accessing information about a creature's types
 * @author Lying
 */
public class Types
{
	private static final EnumSet<EnumCreatureType> DIMENSIONAL = EnumSet.of(EnumCreatureType.NATIVE, EnumCreatureType.EXTRAPLANAR);
	private final List<EnumCreatureType> types = Lists.newArrayList();
	
	public Types(Collection<EnumCreatureType> typesIn)
	{
		this.types.addAll(typesIn);
		
		if(types.containsAll(DIMENSIONAL))
		{
			int indexNative = types.indexOf(EnumCreatureType.NATIVE);
			int indexExtraplanar = types.indexOf(EnumCreatureType.EXTRAPLANAR);
			
			if(indexNative > indexExtraplanar)
				types.remove(indexExtraplanar);
			else
				types.remove(indexNative);
		}
	}
	
	public static Types empty() { return new Types(EnumSet.noneOf(EnumCreatureType.class)); }
	
	public boolean isEmpty() { return types.isEmpty(); }
	
	public void clear() { this.types.clear(); }
	
	public boolean hasCustomAttributes() { return !getAttributes().isEmpty(); }
	
	public List<MobType> getAttributes()
	{
		List<MobType> attributes = Lists.newArrayList();
		this.types.forEach((type) -> { if(type.hasParentAttribute() && !attributes.contains(type.getParentAttribute())) attributes.add(type.getParentAttribute()); });
		return attributes;
	}
	
	public EnumSet<EnumCreatureType> asSet(){ return EnumSet.copyOf(types); }
	
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
	public boolean isLiving()	{ return !(isGolem() || isUndead()); }
	
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
		this.types.forEach((type) -> { type.getHandler().addAbilitiesToMap(map); });
		return map;
	}
	
	/** Converts a list of assorted creature types into a type entry, as in a stat block. */
	public Component toHeader()
	{
		EnumSet<EnumCreatureType> supertypes = supertypes();
		EnumSet<EnumCreatureType> subtypes = subtypes();
		
		MutableComponent supertype = Component.literal("");
		if(supertypes.isEmpty())
			supertype.append(Component.translatable(EnumCreatureType.translationBase+"no_supertype"));
		else
			for(EnumCreatureType sup : supertypes)
			{
				if(supertype.getSiblings().size() > 0)
					supertype.append(Component.literal(" "));
				supertype.append(sup.getTranslated(true));
			}
		
		MutableComponent subtype = Component.literal("");
		if(!subtypes.isEmpty())
		{
			subtype = Component.literal(" (");
			for(EnumCreatureType sup : subtypes)
			{
				if(subtype.getSiblings().size() > 0)
					subtype.append(Component.literal(", "));
				subtype.append(sup.getTranslated(true));
			}
			subtype.append(Component.literal(")"));
		}
		
		return supertype.append(subtype);
	}
}
