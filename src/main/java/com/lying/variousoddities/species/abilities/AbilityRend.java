package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
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
	
	public int compare(Ability abilityIn)
	{
		AbilityRend rend = (AbilityRend)abilityIn;
		return rend.dmgAmount < dmgAmount ? 1 : rend.dmgAmount > dmgAmount ? -1 : 0;
	}
	
	public Type getType(){ return Type.ATTACK; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		compound.putFloat("Dmg", this.dmgAmount);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
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
			LivingEntity trueSource = (LivingEntity)event.getSource().getEntity();
			if(AbilityRegistry.hasAbility(trueSource, REGISTRY_NAME))
			{
				AbilityRend rend = (AbilityRend)AbilityRegistry.getAbilityByName(trueSource, REGISTRY_NAME);
				
				LivingEntity victim = event.getEntity();
				if(!canAbilityAffectEntity(victim, trueSource))
					return;
				
				int slotIndex = victim.getLevel().random.nextInt(4);
				EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slotIndex);
				ItemStack armor = victim.getItemBySlot(slot);
				if(armor != null && !armor.isEmpty())
				{
					boolean ripOffArmor = !armor.isDamageableItem();
					if(!ripOffArmor)
					{
						int damage = armor.getDamageValue();
						int rendAmount = (int)Math.ceil(armor.getMaxDamage() * rend.dmgAmount);
						armor.hurtAndBreak(rendAmount, trueSource, (entity) -> {});
						if(victim instanceof Player && armor.getItem() instanceof ArmorItem)
						{
							ArmorItem armorItem = (ArmorItem)armor.getItem();
							armorItem.onArmorTick(armor, victim.getLevel(), (Player)victim);
						}
						ripOffArmor = armor.getDamageValue() <= damage;
					}
					
					if(ripOffArmor)
					{
						victim.setItemSlot(slot, ItemStack.EMPTY);
						ItemEntity droppedItem = victim.spawnAtLocation(armor, 1.0F);
						if(droppedItem != null)
							droppedItem.setPickUpDelay(Reference.Values.TICKS_PER_SECOND * 5);
					}
				}
			}
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityRend(compound.contains("Dmg", 5) ? compound.getFloat("Dmg") : 0.25F);
		}
	}
}
