package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilitySunBurn extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "sunburn");
	
	private boolean helmetProtects = true;
	
	public AbilitySunBurn()
	{
		super(REGISTRY_NAME);
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
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putBoolean("HelmetProtects", this.helmetProtects);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.helmetProtects = compound.getBoolean("HelmetProtects");
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingUpdate);
	}
	
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(entity, getMapName()) && !(entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.ZOMBIE))
		{
			AbilitySunBurn sunburn = (AbilitySunBurn)AbilityRegistry.getAbilityByName(entity, getMapName());
			boolean shouldBurn = isInDaylight(entity) && !(entity.isPotionActive(Effects.FIRE_RESISTANCE) || entity.isInvulnerableTo(DamageSource.ON_FIRE));
			if(shouldBurn)
			{
				ItemStack helmet = entity.getItemStackFromSlot(EquipmentSlotType.HEAD);
				if(sunburn.helmetProtects && !helmet.isEmpty())
				{
					if(helmet.isDamageable())
					{
						helmet.setDamage(helmet.getDamage() + entity.getRNG().nextInt(2));
						if(helmet.getDamage() >= helmet.getMaxDamage())
						{
							entity.sendBreakAnimation(EquipmentSlotType.HEAD);
							entity.setItemStackToSlot(EquipmentSlotType.HEAD, ItemStack.EMPTY);
						}
					}
					
					shouldBurn = false;
				}
				
				if(shouldBurn)
				  	 entity.setFire(8);
			}
		}
	}
	
	private static boolean isInDaylight(LivingEntity entity)
	{
		World world = entity.getEntityWorld();
		if(world.isDaytime() && !world.isRemote)
		{
			float light = entity.getBrightness();
			BlockPos position = 
					(entity.getRidingEntity() != null && entity.getRidingEntity().getType() == EntityType.BOAT) ? 
							(new BlockPos(entity.getPosX(), (double)Math.round(entity.getPosY()), entity.getPosZ())).up() : 
							new BlockPos(entity.getPosX(), (double)Math.round(entity.getPosY()), entity.getPosZ());
			return light > 0.5F && entity.getRNG().nextFloat() * 30F < (light - 0.4F) * 2F && world.canSeeSky(position);
		}
		return false;
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound){ return new AbilitySunBurn(compound.getBoolean("HelmetProtects")); }
	}
}
