package com.lying.variousoddities.species.abilities;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.api.event.DamageTypesEvent;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.init.VOEnchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;
import net.minecraft.item.TieredItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;

/** A predefined set of damage types, used when applying damage resistance or damage reduction. */
public enum DamageType implements IStringSerializable
{
	MUNDANE(false, (source) -> { return false; }),
	SILVER(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> 
		{
			Item held = itemstack.getItem();
			IItemTier itemTier = held instanceof TieredItem ? ((TieredItem)held).getTier() : null;
			return (itemTier != null && itemTier.toString().toLowerCase().contains("silver")) || EnchantmentHelper.getEnchantmentLevel(VOEnchantments.SILVERSHEEN, itemstack) > 0;
		} ); }),
	WOOD(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, ItemTier.WOOD); } ); }),
	STONE(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, ItemTier.STONE); } ); }),
	GOLD(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, ItemTier.GOLD); } ); }),
	IRON(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, ItemTier.IRON); } ); }),
	DIAMOND(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, ItemTier.DIAMOND); } ); }),
	NETHERITE(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, ItemTier.NETHERITE); } ); }),
	FALLING(false, (source) -> { return source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL; } ),
	POISON(false, (source) -> { return source == VODamageSource.POISON; }),
	ACID(true, (source) -> { return source == VODamageSource.ACID; }),
	FIRE(true, (source) -> { return source.isFireDamage(); } ),
	COLD(true, (source) -> VODamageSource.isOrSynonym(source, VODamageSource.COLD)),
	HOLY(true, (source) -> VODamageSource.isOrSynonym(source, VODamageSource.HOLY)),
	EVIL(true, (source) -> VODamageSource.isOrSynonym(source, VODamageSource.EVIL)),
	LIGHTNING(true, (source) -> { return source == DamageSource.LIGHTNING_BOLT; } ),
	MAGIC(false, (source) -> { return source == DamageSource.MAGIC || source.isMagicDamage() || VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return itemstack.isEnchanted(); } ); } ),
	SONIC(true, (source) -> { return source == VODamageSource.SONIC; }),
	FORCE(false, (source) -> { return source == VODamageSource.FORCE; }),
	PSYCHIC(false, (source) -> { return source == VODamageSource.PSYCHIC; });
	
	private final boolean isEnergy;
	private final Predicate<DamageSource> identifier;
	
	private DamageType(boolean energy, Predicate<DamageSource> identifierIn)
	{
		isEnergy = energy;
		identifier = identifierIn;
	}
	
	public String getString()
	{
		return this.name().toLowerCase();
	}
	
	public ITextComponent getTranslated()
	{
		return new TranslationTextComponent("enum.varodd.damage_type."+getString());
	}
	
	public boolean isDamageType(DamageSource source)
	{
		return this.identifier.apply(source);
	}
	
	public boolean isEnergyDamage(){ return this.isEnergy; }
	
	public static EnumSet<DamageType> getDamageTypes(DamageSource source)
	{
		EnumSet<DamageType> set = EnumSet.noneOf(DamageType.class);
		for(DamageType type : values())
			if(type.isDamageType(source))
				set.add(type);
		
		DamageTypesEvent event = new DamageTypesEvent(source, set);
		MinecraftForge.EVENT_BUS.post(event);
		
		return event.getTypes();
	}
	
	public static DamageType fromString(String str)
	{
    	for(DamageType val : values())
    		if(val.getString().equalsIgnoreCase(str))
    			return val;
    	return null;
	}
}