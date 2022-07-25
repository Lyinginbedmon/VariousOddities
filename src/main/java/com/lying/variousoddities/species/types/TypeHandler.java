package com.lying.variousoddities.species.types;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityBreatheFluid;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.types.EnumCreatureType.Action;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

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
	
	public static final TypeHandler getBreathesAir(UUID idIn)
	{
		return get(idIn).addAbility(new AbilityBreatheFluid(null));
	}
	
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
	
	/** Adds the abilities of this type to the given ability map. */
	public void addAbilitiesToMap(Map<ResourceLocation, Ability> abilityMap)
	{
		this.abilities.forEach((ability) -> { abilityMap.put(ability.getMapName(), ability); });
	}
	
	public Component getDetails()
	{
		if(abilities.isEmpty())
			return Component.literal("");
		
		Collections.sort(abilities, Ability.SORT_ABILITY);
		MutableComponent details = Component.literal("\n");
		for(int i=0; i<abilities.size(); i++)
		{
			Ability ability = abilities.get(i);
			details.append(Component.literal("  ").append(ability.getDisplayName()));
			if(i < abilities.size() - 1)
				details.append(Component.literal("\n"));
		}
		return details;
	}
	
	/** Used by subtypes to modifer the action set from supertypes */
	public  EnumSet<Action> applyActions(EnumSet<Action> actions, Collection<EnumCreatureType> types){ return actions; }
	
	/** Used in commands to determine if a given type can be added to an existing set */
	public  boolean canApplyTo(Collection<EnumCreatureType> types){ return true; }
	
	public static enum DamageResist implements StringRepresentable
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
		
		public String getSerializedName(){ return name().toLowerCase(); }
		
		public Component getTranslated(DamageType typeIn)
		{
			return Component.translatable("enum.varodd.damage_resist."+getSerializedName(), typeIn.getTranslated());
		}
		
		public static DamageResist fromString(String str)
		{
	    	for(DamageResist val : values())
	    		if(val.getSerializedName().equalsIgnoreCase(str))
	    			return val;
	    	return null;
		}
	}
}