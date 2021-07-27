package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Predicate;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class VODamageSource extends DamageSource
{
	public static final Map<DamageSource, String[]> DAMAGE_SYNONYMS = new HashMap<>();
	
	public static final DamageSource ACID = new VODamageSource("acid");
	public static final DamageSource COLD = new VODamageSource("cold").setDamageBypassesArmor();
	public static final DamageSource EVIL = new VODamageSource("evil").setDamageBypassesArmor();
	public static final DamageSource FORCE = new VODamageSource("force").setDamageIsAbsolute();
	public static final DamageSource HOLY = new VODamageSource("good").setDamageBypassesArmor();
	public static final DamageSource POISON = new VODamageSource("poison").setDamageBypassesArmor();
	public static final DamageSource PSYCHIC = new VODamageSource("psychic").setDamageBypassesArmor().setDamageIsAbsolute();
	public static final DamageSource SONIC = new VODamageSource("sonic");
	public static final DamageSource BLUDGEON = new VODamageSource("bludgeoning");
	
	/** Damage caused by the owner exploding */
	public static final DamageSource EXPLOSION = new VODamageSource("explosion").setDamageBypassesArmor().setExplosion();
	
	/** Damage caused by resigning to some form of paralysis */
	public static final DamageSource PARALYSIS = new VODamageSource("paralysis").setDamageBypassesArmor().setDamageIsAbsolute().setDamageAllowedInCreativeMode();
	
	private VODamageSource(String nameIn)
	{
		super(nameIn);
	}
	
	public ITextComponent getDeathMessage(LivingEntity victim)
	{
		LivingEntity attacker = victim.getAttackingEntity();
		String s = "death."+Reference.ModInfo.MOD_ID+".attack." + this.damageType;
		String s1 = s + ".player";
		return attacker != null ? new TranslationTextComponent(s1, victim.getDisplayName(), attacker.getDisplayName()) : new TranslationTextComponent(s, victim.getDisplayName());
	}
	
	public static boolean isFalling(DamageSource source)
	{
		return source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL;
	}
	
	public static boolean isFire(DamageSource source)
	{
		return source.isFireDamage();
	}
	
	public static boolean isOrSynonym(DamageSource sourceA, DamageSource sourceB)
	{
		if(sourceA == sourceB) return true;
		else if(DAMAGE_SYNONYMS.containsKey(sourceB))
		{
			String type = sourceA.damageType.toLowerCase();
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
			Entity attacker = source.getTrueSource();
			if(attacker != null && attacker instanceof LivingEntity)
			{
				ItemStack heldItem = ((LivingEntity)attacker).getHeldItemMainhand();
				if(!heldItem.isEmpty())
					return predicate.apply(heldItem);
			}
		}
		return false;
	}
	
	public static boolean isItemTier(ItemStack stack, IItemTier tier)
	{
		Item held = stack.getItem();
		IItemTier itemTier = held instanceof TieredItem ? ((TieredItem)held).getTier() : null;
		return itemTier != null && itemTier == tier;
	}
	
	/** Applies special conditions and effects to the victim from some damage types */
	public static void livingHurtEvent(LivingHurtEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		if(living == null || !living.isAlive())
			return;
		
		DamageSource source = event.getSource();
		Random rand = living.getRNG();
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
			if(!living.isPotionApplicable(new EffectInstance(Effects.POISON)))
				event.setCanceled(true);
		}
		else if(source == SONIC)
		{
			// Occasionally deafen
		}
	}
	
	public static void applyAcidDamage(LivingEntity living, float amount, Random rand)
	{
		for(EquipmentSlotType slot : EquipmentSlotType.values())
		{
			ItemStack gear = living.getItemStackFromSlot(slot);
			if(!gear.isEmpty())
				damageItem(gear, rand, amount, living, slot);
		}
	}
	
	public static void applyColdDamage(LivingEntity living, float amount, Random rand)
	{
		float odds = 1F - Math.min(1F, amount / 20F);
		if(rand.nextFloat() > odds)
			living.addPotionEffect(new EffectInstance(Effects.SLOWNESS, Reference.Values.TICKS_PER_SECOND * 15, (int)Math.floorDiv((int)amount, 5)));
	}
	
	private static void damageItem(ItemStack item, Random rand, float sourceAmount, LivingEntity entity, EquipmentSlotType slot)
	{
		if(!item.isEmpty() && item.getItem().isDamageable())
		{
			float amount = Math.max(0, (rand.nextFloat() * sourceAmount * 10F));
			item.damageItem((int)amount, entity, (player) -> { player.sendBreakAnimation(slot); });
		}
	}
	
	static
	{
		DAMAGE_SYNONYMS.put(COLD, new String[]{"cold", "frost", "ice", "chill"});
		DAMAGE_SYNONYMS.put(EVIL, new String[]{"evil", "vile", "demon", "profane"});
		DAMAGE_SYNONYMS.put(HOLY, new String[]{"holy", "good", "angel", "divine"});
	}
}
