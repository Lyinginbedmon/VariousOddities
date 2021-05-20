package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.GatherAbilitiesEvent;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityGaseousForm extends AbilityPhasing
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "gaseous_form");
	
	public static final AbilityDamageReduction DAMAGE_REDUCTION = new AbilityDamageReduction(10, DamageType.MAGIC);
	
	public AbilityGaseousForm()
	{
		super(REGISTRY_NAME);
	}
	
	public void addListeners(IEventBus bus)
	{
		super.addListeners(bus);
		bus.addListener(this::gatherAbilities);
	}
	
	public void gatherAbilities(GatherAbilitiesEvent event)
	{
		if(event.hasAbility(getRegistryName()) && !event.hasAbility(DAMAGE_REDUCTION.getMapName()))
			event.addAbility(new AbilityDamageReduction(DAMAGE_REDUCTION.getAmount(), DamageType.MAGIC));
	}
	
	public boolean ignoresNonMagicDamage(){ return false; }
	
	protected boolean isPhaseable(IBlockReader worldIn, BlockPos pos, LivingEntity entity)
	{
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityGaseousForm();
		}
	}
}
