package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.SpellEvent.SpellAffectEntityEvent;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "resistance_spell_"+(descriptor == null ? school.getString() : descriptor.getString())); }
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability.varodd.resistance_spell", (descriptor == null ? school.translatedName() : descriptor.translatedName()));
	}
	
	public ITextComponent description()
	{
		return new TranslationTextComponent("ability.varodd:resistance_spell.desc", (descriptor == null ? school.translatedName() : descriptor.translatedName()));
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::spellAffectEntity);
	}
	
	public void spellAffectEntity(SpellAffectEntityEvent event)
	{
		if(event.getTarget() != null && event.getTarget() instanceof LivingEntity)
		{
			IMagicEffect spell = event.getSpellData().getSpell();
			for(Ability ability : AbilityRegistry.getAbilitiesOfType((LivingEntity)event.getTarget(), REGISTRY_NAME))
			{
				AbilityResistanceSpell resist = (AbilityResistanceSpell)ability;
				if(resist.applies(spell))
				{
					event.setCanceled(true);
					return;
				}
			}
		}
	}
	
	private boolean applies(IMagicEffect spell)
	{
		if(this.school != null && spell.getSchool() == this.school)
			return true;
		else if(this.descriptor != null && spell.getDescriptors().contains(this.descriptor))
			return true;
		return false;
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		if(this.school != null)
			compound.putString("School", this.school.getString());
		else if(this.descriptor != null)
			compound.putString("Descriptor", this.descriptor.getString());
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		if(compound.contains("School", 8))
			this.school = MagicSchool.fromString(compound.getString("School"));
		else if(compound.contains("Descriptor", 8))
			this.descriptor = MagicSubType.fromString(compound.getString("Descriptor"));
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			if(compound.contains("School", 8))
				return new AbilityResistanceSpell(MagicSchool.fromString(compound.getString("School")));
			else if(compound.contains("Descriptor", 8))
				return new AbilityResistanceSpell(MagicSubType.fromString(compound.getString("Descriptor")));
			return new AbilityResistanceSpell();
		}
	}
}
