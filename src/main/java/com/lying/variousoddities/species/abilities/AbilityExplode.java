package com.lying.variousoddities.species.abilities;

import java.util.Collection;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityExplode extends ActivatedAbility
{
	private boolean ignited = false;
	private int fuse = 30;
	private int radius = 3;
	private boolean charged = false;
	
	public AbilityExplode()
	{
		super(Reference.Values.TICKS_PER_MINUTE);
	}
	
	public Component translatedName(){ return this.charged ? Component.translatable("ability."+getMapName()+"_charged") : super.translatedName(); }
	
	public int compare(Ability abilityIn)
	{
		AbilityExplode explode = (AbilityExplode)abilityIn;
		if(explode.radius != radius)
			return explode.radius < radius ? 1 : -1;
		return explode.fuse > fuse ? 1 : explode.fuse < fuse ? -1 : 0;
	}
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public Type getType(){ return Ability.Type.ATTACK; }
	
	public void trigger(LivingEntity entity, Dist side)
	{
		switch(side)
		{
			case CLIENT:
				break;
			default:
				this.ignited = true;
				this.activeTicks = this.fuse;
				entity.getLevel().playSound(null, entity.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundSource.HOSTILE, 1.0F, 0.5F);
				putOnCooldown(entity);
				break;
		}
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		compound.putBoolean("Ignited", this.ignited);
		compound.putBoolean("Charged", this.charged);
		compound.putInt("Fuse", this.fuse);
		compound.putInt("Radius", this.radius);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		super.readFromNBT(compound);
		this.ignited = compound.getBoolean("Ignited");
		this.charged = compound.getBoolean("Charged");
		this.fuse = compound.getInt("Fuse");
		this.radius = compound.getInt("Radius");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::doCharging);
		bus.addListener(this::doExplosion);
	}
	
	public void doCharging(EntityStruckByLightningEvent event)
	{
		Entity entity = event.getEntity();
		if(entity instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)entity;
			AbilityExplode ability = (AbilityExplode)AbilityRegistry.getAbilityByMapName(living, getRegistryName());
			if(ability != null && ability.canTrigger(living))
			{
				ability.charged = true;
				ability.markForUpdate(living);
			}
		}
	}
	
	public void doExplosion(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		AbilityExplode ability = (AbilityExplode)AbilityRegistry.getAbilityByMapName(entity, getRegistryName());
		if(ability != null)
			if(ability.ignited)
			{
				Level world = entity.getLevel();
				if(world.isClientSide)
				{
					// Do particles
					for(int i=0; i<2; i++)
						world.addParticle(ParticleTypes.SMOKE, entity.getRandomX(0.5D), entity.getRandomY() - 0.25D, entity.getRandomZ(0.5D), 0D, entity.getRandom().nextDouble() * 0.5D, 0D);
				}
				else
				{
					if(--ability.activeTicks < 0)
					{
						// Do Explosion
						Explosion.BlockInteraction mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, entity) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;
						float f = ability.charged ? 2.0F : 1.0F;
						world.explode(entity, entity.getX(), entity.getY(), entity.getZ(), (float)ability.radius * f, mode);
						entity.hurt(VODamageSource.EXPLOSION, Float.MAX_VALUE);
						spawnLingeringCloud(entity, world);
						
						ability.ignited = false;
						ability.charged = false;
						ability.activeTicks = 0;
					}
					markForUpdate(entity);
				}
			}
	}
	
	private void spawnLingeringCloud(LivingEntity entity, Level world)
	{
		Collection<MobEffectInstance> collection = entity.getActiveEffects();
		if(!collection.isEmpty())
		{
			AreaEffectCloud lingeringCloud = new AreaEffectCloud(world, entity.getX(), entity.getY(), entity.getZ());
			lingeringCloud.setRadius(2.5F);
			lingeringCloud.setRadiusOnUse(-0.5F);
			lingeringCloud.setWaitTime(10);
			lingeringCloud.setDuration(lingeringCloud.getDuration() / 2);
			lingeringCloud.setRadiusPerTick(-lingeringCloud.getRadius() / (float)lingeringCloud.getDuration());
			
			for(MobEffectInstance effectinstance : collection)
				lingeringCloud.addEffect(new MobEffectInstance(effectinstance));
		
			world.addFreshEntity(lingeringCloud);
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			AbilityExplode explode = new AbilityExplode();
			CompoundTag nbt = explode.writeToNBT(new CompoundTag());
			nbt.merge(compound);
			explode.readFromNBT(nbt);
			return explode;
		}
	}
}
