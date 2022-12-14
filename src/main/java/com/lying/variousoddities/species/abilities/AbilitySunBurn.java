package com.lying.variousoddities.species.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilitySunBurn extends Ability
{
	private boolean helmetProtects = true;
	
	public AbilitySunBurn()
	{
		super();
	}
	
	public AbilitySunBurn(boolean helmet)
	{
		this();
		this.helmetProtects = helmet;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilitySunBurn sunburn = (AbilitySunBurn)abilityIn;
		return sunburn.helmetProtects == false && helmetProtects ? -1 : sunburn.helmetProtects == true && !helmetProtects ? 1 : 0;
	}
	
	public Type getType() { return Type.WEAKNESS; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		compound.putBoolean("HelmetProtects", this.helmetProtects);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		this.helmetProtects = compound.getBoolean("HelmetProtects");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingUpdate);
	}
	
	public void onLivingUpdate(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		AbilitySunBurn sunburn = (AbilitySunBurn)AbilityRegistry.getAbilityByMapName(entity, getMapName());
		if(sunburn != null && !(entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.ZOMBIE))
		{
			boolean shouldBurn = isInDaylight(entity) && !(entity.hasEffect(MobEffects.FIRE_RESISTANCE) || entity.isInvulnerableTo(DamageSource.ON_FIRE));
			if(shouldBurn)
			{
				ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
				if(sunburn.helmetProtects && !helmet.isEmpty())
				{
					if(helmet.isDamageableItem())
					{
						helmet.setDamageValue(helmet.getDamageValue() + entity.getRandom().nextInt(2));
						if(helmet.getDamageValue() >= helmet.getMaxDamage())
						{
							entity.broadcastBreakEvent(EquipmentSlot.HEAD);
							entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
						}
					}
					
					shouldBurn = false;
				}
				
				if(shouldBurn)
				  	 entity.setSecondsOnFire(8);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private static boolean isInDaylight(LivingEntity entity)
	{
		Level world = entity.getLevel();
		if(world.isDay() && !world.isClientSide)
		{
			float light = entity.getLightLevelDependentMagicValue();
			BlockPos position = 
					(entity.getVehicle() != null && entity.getVehicle().getType() == EntityType.BOAT) ? 
							(new BlockPos(entity.getX(), (double)Math.round(entity.getY()), entity.getZ())).above() : 
							new BlockPos(entity.getX(), (double)Math.round(entity.getY()), entity.getZ());
			return light > 0.5F && entity.getRandom().nextFloat() * 30F < (light - 0.4F) * 2F && world.canSeeSky(position);
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound){ return new AbilitySunBurn(compound.getBoolean("HelmetProtects")); }
	}
}
