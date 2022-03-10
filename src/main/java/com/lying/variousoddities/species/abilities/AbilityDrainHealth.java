package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

public class AbilityDrainHealth extends ActivatedAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "drain_health");

	public AbilityDrainHealth()
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_SECOND * 6);
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature() { return Nature.EXTRAORDINARY; }
	
	public boolean canTrigger(LivingEntity entity)
	{
		return super.canTrigger(entity) && VOHelper.getEntityLookTarget(entity, 1D) != null;
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		LivingEntity victim = VOHelper.getEntityLookTarget(entity, 1D);
		if(victim == null || !EnumCreatureType.getCustomTypes(victim).isLiving())
			return;
		
		switch(side)
		{
			case CLIENT:
				break;
			default:
				if(victim.attackEntityFrom(DamageSource.GENERIC, 2F) && victim.addPotionEffect(new EffectInstance(VOPotions.HEALTH_DRAIN, Integer.MAX_VALUE, 1, true, false)))
				{
					entity.addPotionEffect(new EffectInstance(VOPotions.TEMP_HP, Integer.MAX_VALUE, 4, true, false));
					putOnCooldown(entity);
				}
				break;
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			AbilityDrainHealth drain = new AbilityDrainHealth();
			CompoundNBT nbt = drain.writeToNBT(new CompoundNBT());
			nbt.merge(compound);
			drain.readFromNBT(nbt);
			return drain;
		}
	}
}
