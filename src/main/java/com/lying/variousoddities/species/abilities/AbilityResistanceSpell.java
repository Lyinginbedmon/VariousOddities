package com.lying.variousoddities.species.abilities;

import javax.annotation.Nullable;

import com.lying.variousoddities.api.event.SpellEvent.SpellAffectEntityEvent;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityResistanceSpell extends Ability
{
	public static final ResourceLocation REGISTRY_NAME	= new ResourceLocation(Reference.ModInfo.MOD_ID, "resistance_spell");
	
	private MagicSchool school;
	private MagicSubType descriptor;
	
	public AbilityResistanceSpell()
	{
		super(REGISTRY_NAME);
		this.school = MagicSchool.TRANSMUTATION;
		this.descriptor = null;
	}
	
	public AbilityResistanceSpell(MagicSchool school)
	{
		this();
		this.school = school;
		this.descriptor = null;
	}
	
	public AbilityResistanceSpell(MagicSubType descriptor)
	{
		this();
		this.descriptor = descriptor;
		this.school = null;
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Type.DEFENSE; }
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "resistance_spell_"+(descriptor == null ? school.getSerializedName() : descriptor.getSerializedName())); }
	
	public Component translatedName()
	{
		return Component.translatable("ability.varodd.resistance_spell", (descriptor == null ? school.translatedName() : descriptor.translatedName()));
	}
	
	public Component description()
	{
		return Component.translatable("ability.varodd:resistance_spell.desc", (descriptor == null ? school.translatedName() : descriptor.translatedName()));
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::spellAffectEntity);
	}
	
	public void spellAffectEntity(SpellAffectEntityEvent event)
	{
		if(event.getTarget() != null && event.getTarget() instanceof LivingEntity)
			if(!canSpellAffectMob((LivingEntity)event.getTarget(), event.getSpellData().getSpell()))
			{
				event.setCanceled(true);
				return;
			}
	}
	
	public static boolean canSpellAffectMob(@Nullable LivingEntity living, IMagicEffect spell)
	{
		return canSpellAffectMob(living, spell.getSchool(), spell.getDescriptors().toArray(new MagicSubType[0]));
	}
	
	public static boolean canSpellAffectMob(@Nullable LivingEntity living, MagicSchool school, MagicSubType... subtypes)
	{
		if(living == null)
			return false;
		
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(living, REGISTRY_NAME))
		{
			AbilityResistanceSpell resist = (AbilityResistanceSpell)ability;
			if(resist.effectiveAgainst(school, subtypes))
				return false;
		}
		
		return true;
	}
	
	private boolean effectiveAgainst(MagicSchool school, MagicSubType... subtypes)
	{
		if(this.school != null && school == this.school)
			return true;
		else if(subtypes != null && subtypes.length > 0)
			for(MagicSubType subtype : subtypes)
				if(subtype == this.descriptor)
					return true;
		
		return false;
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		if(this.school != null)
			compound.putString("School", this.school.getSerializedName());
		else if(this.descriptor != null)
			compound.putString("Descriptor", this.descriptor.getSerializedName());
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		if(compound.contains("School", 8))
			this.school = MagicSchool.fromString(compound.getString("School"));
		else if(compound.contains("Descriptor", 8))
			this.descriptor = MagicSubType.fromString(compound.getString("Descriptor"));
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			if(compound.contains("School", 8))
				return new AbilityResistanceSpell(MagicSchool.fromString(compound.getString("School")));
			else if(compound.contains("Descriptor", 8))
				return new AbilityResistanceSpell(MagicSubType.fromString(compound.getString("Descriptor")));
			return new AbilityResistanceSpell();
		}
	}
}
