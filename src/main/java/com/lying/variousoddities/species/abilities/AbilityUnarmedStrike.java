package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.item.IBludgeoningItem;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityUnarmedStrike extends ToggledAbility
{
	public AbilityUnarmedStrike()
	{
		super(Reference.Values.TICKS_PER_SECOND);
	}
	
	public Type getType(){ return Type.ATTACK; }
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onLivingAttackEvent);
	}
	
	public void onLivingAttackEvent(LivingAttackEvent event)
	{
		if(isValidDamageSource(event.getSource()))
		{
			LivingEntity trueSource = (LivingEntity)event.getSource().getEntity();
			
			AbilityUnarmedStrike strike = (AbilityUnarmedStrike)AbilityRegistry.getAbilityByMapName(trueSource, getRegistryName());
			if(strike == null)
				return;
			
			if(isValidItem(trueSource.getMainHandItem(), strike.isActive()))
			{
				// Replace melee damage with bludgeoning damage
				event.setCanceled(true);
				event.getEntity().hurt(VODamageSource.BLUDGEON, event.getAmount());
			}
		}
	}
	
	protected boolean isValidItem(ItemStack stackIn, boolean hasActiveAbility)
	{
		if(stackIn.getItem() instanceof IBludgeoningItem)
			return true;
		else if(Block.byItem(stackIn.getItem()) instanceof AnvilBlock && stackIn.getDisplayName().getString().equalsIgnoreCase("ACME"))
			return true;
		return stackIn.isEmpty() && hasActiveAbility;
	}
	
	protected boolean isValidDamageSource(DamageSource source)
	{
		if(source != VODamageSource.BLUDGEON && source instanceof EntityDamageSource && !((EntityDamageSource)source).isThorns())
		{
			Entity trueSource = source.getEntity();
			if(source.getDirectEntity() == trueSource && trueSource != null && trueSource instanceof LivingEntity && trueSource.isAlive())
			{
				ItemStack weapon = ((LivingEntity)trueSource).getMainHandItem();
				return isValidItem(weapon, true);
			}
		}
		return false;
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(); }
		
		public ToggledAbility createAbility(CompoundTag compound)
		{
			return new AbilityUnarmedStrike();
		}
	}
}
