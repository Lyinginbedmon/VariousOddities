package com.lying.variousoddities.item;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.lying.variousoddities.init.VOItems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class ItemHeldFlag extends VOItem
{
	private static final Map<EnumPrideType, ItemHeldFlag> PRIDE_TYPE_ITEM_MAP = Maps.newEnumMap(EnumPrideType.class);
	private final EnumPrideType prideType;
	private final Multimap<Attribute, AttributeModifier> attributeModifiers;
	
	public ItemHeldFlag(EnumPrideType typeIn, Properties properties)
	{
		super(properties);
		this.prideType = typeIn;
		
	    Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
	    builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", -1D, AttributeModifier.Operation.MULTIPLY_TOTAL));
	    this.attributeModifiers = builder.build();
	    
	    PRIDE_TYPE_ITEM_MAP.put(typeIn, this);
	}
	
	public static void registerSubItems()
	{
		for(EnumPrideType type : EnumPrideType.values())
			VOItems.register("held_flag_"+type.name().toLowerCase(), new ItemHeldFlag(type, new Properties().tab(VOItemGroup.LOOT)));
	}
	
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot)
	{
		return equipmentSlot == EquipmentSlot.MAINHAND ? this.attributeModifiers : HashMultimap.create();
	}
    
    public EnumPrideType getType(ItemStack stack)
    {
    	return this.prideType;
    }
    
    public static ItemHeldFlag getItem(EnumPrideType color)
    {
        return PRIDE_TYPE_ITEM_MAP.get(color);
    }
	
	public enum EnumPrideType
	{
		AGENDER,
		AROMANTIC,
		ASEXUAL,
		BISEXUAL,
		GAY,
		GENDERFLUID,
		GENDERQUEER,
		INTERSEX,
		LESBIAN,
		NONBINARY,
		PANSEXUAL,
		POLYSEXUAL,
		QUEER,
		TRANSGENDER;
		
		public static EnumPrideType getRandomType(UUID idIn)
		{
			return values()[new Random(idIn.getLeastSignificantBits()).nextInt(values().length)];
		}
	}
}
