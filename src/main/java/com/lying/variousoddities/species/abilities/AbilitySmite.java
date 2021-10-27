package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilitySmite extends ToggledAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "smite");
	
	private EnumCreatureType targetType;
	
	public AbilitySmite(EnumCreatureType targetIn)
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_DAY);
		this.targetType = targetIn;
	}
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "smite"+(targetType == null ? "" : "_" + targetType.getString())); }
	
	public ITextComponent translatedName()
	{
		return targetType == null ? super.translatedName() : new TranslationTextComponent("ability.varodd.smite_type", targetType.getTranslated(false));
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		if(targetType != null)
			compound.putString("Type", targetType.getString());
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.targetType = EnumCreatureType.fromName(compound.getString("Type"));
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onAttack);
	}
	
	public void onAttack(LivingHurtEvent event)
	{
		LivingEntity victim = event.getEntityLiving();
		DamageSource source = event.getSource();
		if(source instanceof EntityDamageSource && source.getImmediateSource() instanceof LivingEntity)
		{
			LivingEntity attacker = (LivingEntity)source.getImmediateSource();
			for(AbilitySmite smite : AbilityRegistry.getAbilitiesOfType(attacker, AbilitySmite.class))
			{
				if(!smite.isActive() || !smite.appliesTo(victim))
					continue;
				
				smite.isActive = false;
				smite.putOnCooldown(attacker);
				
				victim.attackEntityFrom(DamageSource.causeMobDamage(attacker), Math.min(20F, attacker.getHealth()));
			}
		}
	}
	
	private boolean appliesTo(LivingEntity entity)
	{
		return targetType == null || EnumCreatureType.getTypes(entity).includesType(targetType);
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public ToggledAbility createAbility(CompoundNBT compound)
		{
			return new AbilitySmite(EnumCreatureType.fromName(compound.getString("Type")));
		}
	}
}
