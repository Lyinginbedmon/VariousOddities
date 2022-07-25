package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Predicate;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class VODamageSource extends DamageSource
{
	public static final Map<DamageSource, String[]> DAMAGE_SYNONYMS = new HashMap<>();
	
	public static final DamageSource ACID = new VODamageSource("acid");
	public static final DamageSource COLD = new VODamageSource("cold").bypassArmor();
	public static final DamageSource EVIL = new VODamageSource("evil").bypassArmor();
	public static final DamageSource FORCE = new VODamageSource("force").bypassMagic();
	public static final DamageSource HOLY = new VODamageSource("good").bypassArmor();
	public static final DamageSource POISON = new VODamageSource("poison").bypassArmor();
	public static final DamageSource PSYCHIC = new VODamageSource("psychic").bypassArmor().bypassMagic();
	public static final DamageSource SONIC = new VODamageSource("sonic");
	public static final DamageSource BLUDGEON = new VODamageSource("bludgeoning");
	
	/** Damage caused by the owner exploding */
	public static final DamageSource EXPLOSION = new VODamageSource("explosion").bypassArmor().setExplosion();
	
	/** Damage caused by resigning to some form of paralysis */
	public static final DamageSource PARALYSIS = new VODamageSource("paralysis").bypassArmor().bypassMagic().bypassInvul();
	
	private VODamageSource(String nameIn)
	{
		super(nameIn);
	}
	
	public Component getDeathMessage(LivingEntity victim)
	{
		LivingEntity attacker = victim.getLastHurtByMob();
		String s = "death."+Reference.ModInfo.MOD_ID+".attack." + this.msgId;
		String s1 = s + ".player";
		return attacker != null ? Component.translatable(s1, victim.getDisplayName(), attacker.getDisplayName()) : Component.translatable(s, victim.getDisplayName());
	}
	
	public static boolean isFalling(DamageSource source)
	{
		return source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL;
	}
	
	public static boolean isFire(DamageSource source)
	{
		return source.isFire();
	}
	
	public static boolean isOrSynonym(DamageSource sourceA, DamageSource sourceB)
	{
		if(sourceA == sourceB) return true;
		else if(DAMAGE_SYNONYMS.containsKey(sourceB))
		{
			String type = sourceA.msgId.toLowerCase();
			for(String synonym : DAMAGE_SYNONYMS.get(sourceB))
				if(type.contains(synonym))
					return true;
		}
		return false;
	}
	
	public static boolean isCold(DamageSource source){ return isOrSynonym(source, COLD); }
	
	public static boolean isEvil(DamageSource source){ return isOrSynonym(source, EVIL); }
	
	public static boolean isHoly(DamageSource source){ return isOrSynonym(source, HOLY); }
	
	public static boolean applyHeldItemPredicate(DamageSource source, Predicate<ItemStack> predicate)
	{
		if(source instanceof EntityDamageSource)
		{
			Entity attacker = source.getEntity();
			if(attacker != null && attacker instanceof LivingEntity)
			{
				ItemStack heldItem = ((LivingEntity)attacker).getMainHandItem();
				if(!heldItem.isEmpty())
					return predicate.apply(heldItem);
			}
		}
		return false;
	}
	
	public static boolean isItemTier(ItemStack stack, Tiers tier)
	{
		Item held = stack.getItem();
		Tier itemTier = held instanceof TieredItem ? ((TieredItem)held).getTier() : null;
		return itemTier != null && itemTier == tier;
	}
	
	/** Applies special conditions and effects to the victim from some damage types */
	public static void livingHurtEvent(LivingHurtEvent event)
	{
		LivingEntity living = event.getEntity();
		if(living == null || !living.isAlive())
			return;
		
		DamageSource source = event.getSource();
		RandomSource rand = living.getRandom();
		if(source == ACID)
		{
			// Damage worn and held equipment
			if(event.getAmount() > 0F)
				applyAcidDamage(living, event.getAmount(), rand);
		}
		else if(source == COLD)
		{
			// Occasionally slow
			if(event.getAmount() >= 2F)
				applyColdDamage(living, event.getAmount(), rand);
		}
		else if(source == POISON)
		{
			if(!living.canBeAffected(new MobEffectInstance(MobEffects.POISON)))
				event.setCanceled(true);
		}
		else if(source == SONIC)
		{
			// TODO Sonic damage occasionally deafens
		}
	}
	
	public static void applyAcidDamage(LivingEntity living, float amount, RandomSource rand)
	{
		for(EquipmentSlot slot : EquipmentSlot.values())
		{
			ItemStack gear = living.getItemBySlot(slot);
			if(!gear.isEmpty())
				damageItem(gear, rand, amount, living, slot);
		}
	}
	
	public static void applyColdDamage(LivingEntity living, float amount, RandomSource rand)
	{
		float odds = 1F - Math.min(1F, amount / 20F);
		if(rand.nextFloat() > odds)
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Reference.Values.TICKS_PER_SECOND * 15, (int)Math.floorDiv((int)amount, 5)));
	}
	
	private static void damageItem(ItemStack item, RandomSource rand, float sourceAmount, LivingEntity entity, EquipmentSlot slot)
	{
		if(!item.isEmpty() && item.getItem().isDamageable(item))
		{
			float amount = Math.max(0, (rand.nextFloat() * sourceAmount * 10F));
			item.hurtAndBreak((int)amount, entity, (player) -> { player.broadcastBreakEvent(slot); });
		}
	}
	
	static
	{
		DAMAGE_SYNONYMS.put(COLD, new String[]{"cold", "frost", "ice", "chill"});
		DAMAGE_SYNONYMS.put(EVIL, new String[]{"evil", "vile", "demon", "profane"});
		DAMAGE_SYNONYMS.put(HOLY, new String[]{"holy", "good", "angel", "divine"});
	}
}
