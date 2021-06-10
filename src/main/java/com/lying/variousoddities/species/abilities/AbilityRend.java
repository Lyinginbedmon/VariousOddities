package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityRend extends AbilityMeleeDamage
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "rend");
	
	private float dmgAmount = 0.25F;
	
	public AbilityRend(float amount)
	{
		super(REGISTRY_NAME);
		this.dmgAmount = amount;
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putFloat("Dmg", this.dmgAmount);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.dmgAmount = compound.contains("Dmg", 5) ? compound.getFloat("Dmg") : 0.25F;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::rendOnDamage);
	}
	
	public void rendOnDamage(LivingHurtEvent event)
	{
		if(isValidDamageSource(event.getSource()))
		{
			LivingEntity trueSource = (LivingEntity)event.getSource().getTrueSource();
			if(AbilityRegistry.hasAbility(trueSource, REGISTRY_NAME))
			{
				AbilityRend rend = (AbilityRend)AbilityRegistry.getAbilityByName(trueSource, REGISTRY_NAME);
				
				LivingEntity victim = event.getEntityLiving();
				int slotIndex = victim.getEntityWorld().rand.nextInt(4);
				EquipmentSlotType slot = EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR, slotIndex);
				ItemStack armor = victim.getItemStackFromSlot(slot);
				if(armor != null && !armor.isEmpty())
				{
					boolean ripOffArmor = !armor.isDamageable();
					if(!ripOffArmor)
					{
						int damage = armor.getDamage();
						int rendAmount = (int)Math.ceil(armor.getMaxDamage() * rend.dmgAmount);
						armor.damageItem(rendAmount, trueSource, (entity) -> {});
						if(victim instanceof PlayerEntity && armor.getItem() instanceof ArmorItem)
						{
							ArmorItem armorItem = (ArmorItem)armor.getItem();
							armorItem.onArmorTick(armor, victim.getEntityWorld(), (PlayerEntity)victim);
						}
						ripOffArmor = armor.getDamage() <= damage;
					}
					
					if(ripOffArmor)
					{
						victim.setItemStackToSlot(slot, ItemStack.EMPTY);
						ItemEntity droppedItem = victim.entityDropItem(armor, 1.0F);
						if(droppedItem != null)
							droppedItem.setPickupDelay(Reference.Values.TICKS_PER_SECOND * 5);
					}
				}
			}
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityRend(compound.contains("Dmg", 5) ? compound.getFloat("Dmg") : 0.25F);
		}
	}
}
