package com.lying.variousoddities.species.abilities;

import java.util.EnumSet;

import com.google.common.base.Predicate;
import com.lying.variousoddities.api.event.DamageTypesEvent;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.init.VOEnchantments;

import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;

/** A predefined set of damage types, used when applying damage resistance or damage reduction. */
public enum DamageType implements StringRepresentable
{
	MUNDANE(false, (source) -> { return false; }),
	@SuppressWarnings("deprecation")
	SILVER(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> 
		{
			Item held = itemstack.getItem();
			Tier itemTier = held instanceof TieredItem ? ((TieredItem)held).getTier() : null;
			return (itemTier != null && itemTier.toString().toLowerCase().contains("silver")) || EnchantmentHelper.getItemEnchantmentLevel(VOEnchantments.SILVERSHEEN.get(), itemstack) > 0;
		} ); }),
	WOOD(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, Tiers.WOOD); } ); }),
	STONE(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, Tiers.STONE); } ); }),
	GOLD(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, Tiers.GOLD); } ); }),
	IRON(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, Tiers.IRON); } ); }),
	DIAMOND(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, Tiers.DIAMOND); } ); }),
	NETHERITE(false, (source) -> { return VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return VODamageSource.isItemTier(itemstack, Tiers.NETHERITE); } ); }),
	FALLING(false, (source) -> { return source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL; } ),
	POISON(false, (source) -> { return source == VODamageSource.POISON; }),
	ACID(true, (source) -> { return source == VODamageSource.ACID; }),
	FIRE(true, (source) -> { return source.isFire(); } ),
	COLD(true, (source) -> VODamageSource.isOrSynonym(source, VODamageSource.COLD)),
	HOLY(true, (source) -> VODamageSource.isOrSynonym(source, VODamageSource.HOLY)),
	EVIL(true, (source) -> VODamageSource.isOrSynonym(source, VODamageSource.EVIL)),
	LIGHTNING(true, (source) -> { return source == DamageSource.LIGHTNING_BOLT; } ),
	MAGIC(false, (source) -> { return source == DamageSource.MAGIC || source.isMagic() || VODamageSource.applyHeldItemPredicate(source, (itemstack) -> { return itemstack.isEnchanted(); } ); } ),
	SONIC(true, (source) -> { return source == VODamageSource.SONIC; }),
	FORCE(false, (source) -> { return source == VODamageSource.FORCE; }),
	PSYCHIC(false, (source) -> { return source == VODamageSource.PSYCHIC; }),
	NONLETHAL(false, (source) -> { return source == VODamageSource.BLUDGEON; });
	
	private final boolean isEnergy;
	private final Predicate<DamageSource> identifier;
	
	private DamageType(boolean energy, Predicate<DamageSource> identifierIn)
	{
		isEnergy = energy;
		identifier = identifierIn;
	}
	
	public String getSerializedName()
	{
		return this.name().toLowerCase();
	}
	
	public Component getTranslated()
	{
		return Component.translatable("enum.varodd.damage_type."+getSerializedName());
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
    		if(val.getSerializedName().equalsIgnoreCase(str))
    			return val;
    	return null;
	}
}