package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.LivingEntity;
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
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "smite"+(targetType == null ? "" : "_" + targetType.getSerializedName())); }
	
	public Component translatedName()
	{
		return targetType == null ? super.translatedName() : Component.translatable("ability.varodd.smite_type", targetType.getTranslated(false));
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		if(targetType != null)
			compound.putString("Type", targetType.getSerializedName());
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
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
		LivingEntity victim = event.getEntity();
		DamageSource source = event.getSource();
		if(source instanceof EntityDamageSource && source.getDirectEntity() instanceof LivingEntity)
		{
			LivingEntity attacker = (LivingEntity)source.getDirectEntity();
			for(AbilitySmite smite : AbilityRegistry.getAbilitiesOfType(attacker, AbilitySmite.class))
			{
				if(!smite.isActive() || !smite.appliesTo(victim) || !smite.canAbilityAffectEntity(victim, attacker))
					continue;
				
				smite.isActive = false;
				smite.putOnCooldown(attacker);
				
				victim.hurt(DamageSource.mobAttack(attacker), Math.min(20F, attacker.getHealth()));
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
		
		public ToggledAbility createAbility(CompoundTag compound)
		{
			return new AbilitySmite(EnumCreatureType.fromName(compound.getString("Type")));
		}
	}
}
