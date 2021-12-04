package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.item.IBludgeoningItem;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityUnarmedStrike extends ToggledAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "unarmed_strike");
	
	public AbilityUnarmedStrike()
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_SECOND);
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
			LivingEntity trueSource = (LivingEntity)event.getSource().getTrueSource();
			if(isValidItem(trueSource.getHeldItemMainhand(), AbilityRegistry.hasAbility(trueSource, REGISTRY_NAME) && ((AbilityUnarmedStrike)AbilityRegistry.getAbilityByName(trueSource, REGISTRY_NAME)).isActive()))
			{
				// Replace melee damage with bludgeoning damage
				event.setCanceled(true);
				event.getEntityLiving().attackEntityFrom(VODamageSource.BLUDGEON, event.getAmount());
			}
		}
	}
	
	protected boolean isValidItem(ItemStack stackIn, boolean hasActiveAbility)
	{
		if(stackIn.getItem() instanceof IBludgeoningItem)
			return true;
		else if(Block.getBlockFromItem(stackIn.getItem()) instanceof AnvilBlock && stackIn.getDisplayName().getString().equalsIgnoreCase("ACME"))
			return true;
		return stackIn.isEmpty() && hasActiveAbility;
	}
	
	protected boolean isValidDamageSource(DamageSource source)
	{
		if(source != VODamageSource.BLUDGEON && source instanceof EntityDamageSource && !((EntityDamageSource)source).getIsThornsDamage())
		{
			Entity trueSource = source.getTrueSource();
			if(source.getImmediateSource() == trueSource && trueSource != null && trueSource instanceof LivingEntity && trueSource.isAlive())
			{
				ItemStack weapon = ((LivingEntity)trueSource).getHeldItemMainhand();
				return isValidItem(weapon, true);
			}
		}
		return false;
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public ToggledAbility createAbility(CompoundNBT compound)
		{
			return new AbilityUnarmedStrike();
		}
	}
}
