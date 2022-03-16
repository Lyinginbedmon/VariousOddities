package com.lying.variousoddities.species.abilities;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityGaseous extends AbilityPhasing implements ICompoundAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "gaseous_form");
	
	public static final AbilityDamageReduction DAMAGE_REDUCTION = new AbilityDamageReduction(10, DamageType.MAGIC);
	
	public AbilityGaseous()
	{
		super(REGISTRY_NAME);
	}
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public List<Ability> getSubAbilities()
	{
		return Lists.newArrayList(new AbilityDamageReduction(DAMAGE_REDUCTION.getAmount(), DamageType.MAGIC));
	}
	
	public boolean ignoresNonMagicDamage(){ return false; }
	
	public boolean isPhaseable(IBlockReader worldIn, BlockPos pos, LivingEntity entity)
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
		World world = living.getEntityWorld();
		if(world.isRemote && AbilityRegistry.hasAbility(living, REGISTRY_NAME))
		{
			Random rand = world.rand;
			world.addParticle(ParticleTypes.SMOKE, 
					living.getPosXRandom(0.5D), living.getPosYRandom() - 0.25D, living.getPosZRandom(0.5D), 
					(rand.nextDouble() - 0.5D) * 0.125D, rand.nextDouble() * 0.0125D, (rand.nextDouble() - 0.5D) * 0.125D);
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityGaseous();
		}
	}
}
