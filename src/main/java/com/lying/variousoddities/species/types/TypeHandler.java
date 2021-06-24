package com.lying.variousoddities.species.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.types.EnumCreatureType.Action;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemTier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

/**
 * A handler class that applies and defines the properties of different creature types
 * @author Lying
 *
 */
public class TypeHandler
{
	public final UUID sourceID;
	
	private boolean canBreatheAir = true;
	private boolean canCriticalHit = true;
	private boolean canPoison = true;
	private boolean canParalysis = true;
	
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
	
	public boolean canBreatheAir(){ return this.canBreatheAir; }
	public boolean canCriticalHit(){ return this.canCriticalHit; }
	public boolean canBePoisoned(){ return this.canPoison; }
	public boolean canBeParalysed(){ return this.canParalysis; }
	
	/** Prevents this type from breathing outside of water */
	public TypeHandler noBreatheAir(){ this.canBreatheAir = false; return this; }
	/** Prevents this type from receiving critical hits */
	public TypeHandler noCriticalHit(){ this.canCriticalHit = false; return this; }
	/** Prevents this type from being affected by poison */
	public TypeHandler noPoison(){ this.canPoison = false; return this; }
	/** Prevents this type from being affected by paralysis effects */
	public TypeHandler noParalysis(){ this.canParalysis = false; return this; }
	
	public TypeHandler addAbility(Ability abilityIn)
	{
		this.abilities.add(abilityIn.setSourceId(this.sourceID));
		return this;
	}
	
	public List<Ability> getAbilities(){ return this.abilities; }
	
	/** Controls how critical hits affect this type */
	public  void onCriticalEvent(CriticalHitEvent event){ };
	
	/** Applies immunity to certain types of damage, to prevent damage that might be applied */
	public  void onDamageEventPre(LivingAttackEvent event){ };
	/** Modifies damage received according to configured creature types */
	public  void onDamageEventPost(LivingHurtEvent event){ };
	
	/** Returns a filtered list of active effects, removing any this type is unaffected by */
	public  List<Effect> getInvalidPotions(List<EffectInstance> activePotions){ return TypeUtils.EMPTY_POTIONS; };
	
	/** Applies additional effects specific to this type */
	public  void onLivingTick(LivingEntity entity){ };
	
	public  boolean canSpellAffect(IMagicEffect spellIn){ return true; }
	
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