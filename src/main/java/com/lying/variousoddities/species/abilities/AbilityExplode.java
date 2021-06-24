package com.lying.variousoddities.species.abilities;

import java.util.Collection;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityExplode extends ActivatedAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "explode");
	
	private boolean ignited = false;
	private int fuse = 30;
	private int radius = 3;
	private boolean charged = false;
	
	public AbilityExplode()
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_MINUTE);
	}
	
	public ITextComponent translatedName(){ return this.charged ? new TranslationTextComponent("ability."+getMapName()+"_charged") : super.translatedName(); }
	
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
				entity.getEntityWorld().playSound(null, entity.getPosition(), SoundEvents.ENTITY_CREEPER_PRIMED, SoundCategory.HOSTILE, 1.0F, 0.5F);
				putOnCooldown(entity);
				break;
		}
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putBoolean("Ignited", this.ignited);
		compound.putBoolean("Charged", this.charged);
		compound.putInt("Fuse", this.fuse);
		compound.putInt("Radius", this.radius);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
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
		if(entity instanceof LivingEntity && AbilityRegistry.hasAbility((LivingEntity)entity, REGISTRY_NAME))
		{
			LivingEntity living = (LivingEntity)entity;
			AbilityExplode ability = (AbilityExplode)AbilityRegistry.getAbilityByName(living, REGISTRY_NAME);
			if(ability.canTrigger(living))
			{
				ability.charged = true;
				ability.markForUpdate(living);
			}
		}
	}
	
	public void doExplosion(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(entity, REGISTRY_NAME))
		{
			AbilityExplode ability = (AbilityExplode)AbilityRegistry.getAbilityByName(entity, REGISTRY_NAME);
			if(ability.ignited)
			{
				World world = entity.getEntityWorld();
				if(world.isRemote)
				{
					// Do particles
					for(int i=0; i<2; i++)
						world.addParticle(ParticleTypes.SMOKE, entity.getPosXRandom(0.5D), entity.getPosYRandom() - 0.25D, entity.getPosZRandom(0.5D), 0D, entity.getRNG().nextDouble() * 0.5D, 0D);
				}
				else
				{
					if(--ability.activeTicks < 0)
					{
						// Do Explosion
						Explosion.Mode mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, entity) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
						float f = ability.charged ? 2.0F : 1.0F;
						world.createExplosion(entity, entity.getPosX(), entity.getPosY(), entity.getPosZ(), (float)ability.radius * f, mode);
						entity.attackEntityFrom(VODamageSource.EXPLOSION, Float.MAX_VALUE);
						spawnLingeringCloud(entity, world);
						
						ability.ignited = false;
						ability.charged = false;
						ability.activeTicks = 0;
					}
					markForUpdate(entity);
				}
			}
		}
	}
	
	private void spawnLingeringCloud(LivingEntity entity, World world)
	{
		Collection<EffectInstance> collection = entity.getActivePotionEffects();
		if(!collection.isEmpty())
		{
			AreaEffectCloudEntity lingeringCloud = new AreaEffectCloudEntity(world, entity.getPosX(), entity.getPosY(), entity.getPosZ());
			lingeringCloud.setRadius(2.5F);
			lingeringCloud.setRadiusOnUse(-0.5F);
			lingeringCloud.setWaitTime(10);
			lingeringCloud.setDuration(lingeringCloud.getDuration() / 2);
			lingeringCloud.setRadiusPerTick(-lingeringCloud.getRadius() / (float)lingeringCloud.getDuration());
			
			for(EffectInstance effectinstance : collection)
				lingeringCloud.addEffect(new EffectInstance(effectinstance));
		
			world.addEntity(lingeringCloud);
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			AbilityExplode explode = new AbilityExplode();
			CompoundNBT nbt = explode.writeToNBT(new CompoundNBT());
			nbt.merge(compound);
			explode.readFromNBT(nbt);
			return explode;
		}
	}
}
