package com.lying.variousoddities.species.abilities;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityGaseous extends AbilityPhasing implements ICompoundAbility
{
	public static final AbilityDamageReduction DAMAGE_REDUCTION = new AbilityDamageReduction(10, DamageType.MAGIC);
	
	public AbilityGaseous()
	{
		super();
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public List<Ability> getSubAbilities()
	{
		return Lists.newArrayList(new AbilityDamageReduction(DAMAGE_REDUCTION.getAmount(), DamageType.MAGIC));
	}
	
	public boolean ignoresNonMagicDamage(){ return false; }
	
	public boolean isPhaseable(BlockGetter worldIn, BlockPos pos, LivingEntity entity)
	{
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void addListeners(IEventBus bus)
	{
		super.addListeners(bus);
		bus.addListener(this::tick);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void tick(RenderLivingEvent.Post<?,?> event)
	{
		LivingEntity living = event.getEntity();
		Level world = living.getLevel();
		if(world.isClientSide && AbilityRegistry.hasAbilityOfMapName(living, getRegistryName()))
		{
			RandomSource rand = world.random;
			world.addParticle(ParticleTypes.SMOKE, 
					living.getX(0.5D), living.getY(1D) - 0.25D, living.getZ(0.5D), 
					(rand.nextDouble() - 0.5D) * 0.125D, rand.nextDouble() * 0.0125D, (rand.nextDouble() - 0.5D) * 0.125D);
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityGaseous();
		}
	}
}
