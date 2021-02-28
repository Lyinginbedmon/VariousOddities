package com.lying.variousoddities.types;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.types.EnumCreatureType.Action;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemTier;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
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
	private boolean canCriticalHit = true;
	private boolean canPoison = true;
	private boolean canParalysis = true;
	private Map<DamageSource, EnumDamageResist> resistances = new HashMap<>();
	private EnumDamageResist fireResist = EnumDamageResist.NORMAL;
	
	/** Returns a default instance */
	public static final TypeHandler get(){ return new TypeHandler(); }
	
	public boolean canCriticalHit(){ return this.canCriticalHit; }
	public boolean canPoison(){ return this.canPoison; }
	public boolean canParalysis(){ return this.canParalysis; }
	
	/** Prevents this type from receiving critical hits */
	public TypeHandler noCriticalHit(){ this.canCriticalHit = false; return this; }
	/** Prevents this type from being affected by poison */
	public TypeHandler noPoison(){ this.canPoison = false; return this; }
	/** Prevents this type from being affected by paralysis effects */
	public TypeHandler noParalysis(){ this.canParalysis = false; return this; }
	
	/**
	 * Sets how this type is affected by the given type of damage.<br>
	 * Also affects synonym damage types.
	 */
	public TypeHandler addResistance(DamageSource source, EnumDamageResist resist){ resistances.put(source, resist); return this;}
	/** Sets how this type is affected by fire-type damage */
	public TypeHandler setFireResist(EnumDamageResist resist){ this.fireResist = resist; return this; }
	
	/** Controls how critical hits affect this type */
	public  void onCriticalEvent(CriticalHitEvent event){ };
	
	/** Used to determine if the mob should take more, less, none, or normal damage from the given source */
	public EnumDamageResist getDamageResist(DamageSource source)
	{
		if(source.isFireDamage())
			return fireResist;
		
		for(DamageSource resistance : resistances.keySet())
			if(VODamageSource.isOrSynonym(source, resistance))
				return resistances.get(resistance);
		return EnumDamageResist.NORMAL;
	}
	/** Applies immunity to certain types of damage, to prevent damage that might be applied */
	public  void onDamageEventPre(LivingAttackEvent event){ };
	/** Modifies damage received according to configured creature types */
	public  void onDamageEventPost(LivingHurtEvent event){ };
	
	/** Returns a filtered list of active effects, removing any this type is unaffected by */
	public  List<Effect> getInvalidPotions(List<EffectInstance> activePotions){ return TypeUtils.EMPTY_POTIONS; };
	
	/** Applies additional effects specific to this type */
	public  void onMobUpdateEvent(LivingEntity entity){ };
	
	public  boolean canSpellAffect(IMagicEffect spellIn){ return true; }
	
	/** Used by subtypes to modifer the action set from supertypes */
	public  EnumSet<Action> applyActions(EnumSet<Action> actions){ return actions; }
	
	/** Used in commands to determine if a given type can be added to an existing set */
	public  boolean canApplyTo(List<EnumCreatureType> types){ return true; }
	
	public static enum EnumDamageResist
	{
		NORMAL(0F),
		VULNERABLE(0.5F),
		RESISTANT(-0.5F),
		IMMUNE(-1F);
		
		private final float mult;
		
		private EnumDamageResist(float multIn)
		{
			this.mult = multIn;
		}
		
		public EnumDamageResist add(EnumDamageResist resistA)
		{
			if(resistA == IMMUNE || this == IMMUNE)
				return IMMUNE;
			
			float add = resistA.mult + this.mult;
			return add > 0 ? VULNERABLE : add < 0 ? RESISTANT : NORMAL;
		}
		
		public float apply(float amount){ return amount * (1F + mult); }
	}
	
	/** Common operations between types */
	public static class TypeUtils
	{
		public static final List<Effect> EMPTY_POTIONS = new ArrayList<Effect>();
		
		public static void preventNaturalRegen(LivingEntity living)
		{
			if(living != null && living instanceof PlayerEntity)
			{
				PlayerEntity player = (PlayerEntity)living;
				if(player.getFoodStats().getFoodLevel() > 17) player.getFoodStats().setFoodLevel(17);
			}
		}
		
		public static ItemTier getMaterial(String name)
		{
			for(ItemTier material : ItemTier.values())
				if(material.name().equals(name))
					return material;
			return null;
		}
	}
}