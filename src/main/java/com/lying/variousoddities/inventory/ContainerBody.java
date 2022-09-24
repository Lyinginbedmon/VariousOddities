package com.lying.variousoddities.inventory;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOItems;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

public class ContainerBody extends AbstractContainerMenu
{
	public static ContainerBody fromNetwork(int windowId, Inventory inv)
	{
		Player player = inv.player;
		double range = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vec3 eyeStart = player.getEyePosition(0F);
		Vec3 eyeEnd = eyeStart.add(player.getLookAngle().multiply(range, range, range));
		
		AbstractBody likelyBody = null;
		double minDist = Double.MAX_VALUE;
		for(AbstractBody body : player.getLevel().getEntitiesOfClass(AbstractBody.class, player.getBoundingBox().inflate(range)))
		{
			double dist = body.distanceTo(player);
			if(body.getBoundingBox().intersects(eyeStart, eyeEnd))
				if(dist < minDist)
				{
					likelyBody = body;
					minDist = dist;
				}
		}
		
		if(likelyBody != null)
			return new ContainerBody(windowId, inv, likelyBody.getInventory(), likelyBody);
		return null;
	}
	
	public final AbstractBody theBody;
	public final boolean isCorpse;
	private final Container bodyInventory;
	
	public ContainerBody(int windowId, Inventory playerInventory, Container bodyInventory, final AbstractBody bodyIn)
	{
		super(VOItems.CONTAINER_BODY, windowId);
		this.theBody = bodyIn;
		this.isCorpse = bodyIn.getType() == VOEntities.CORPSE.get();
		this.bodyInventory = bodyInventory;
		
		Player player = playerInventory.player;
		bodyInventory.startOpen(player);
		
		// Body inventory
		this.addSlot(new Slot(bodyInventory, 3, 62, 0)
				{
					public int getMaxStackSize(){ return 1; }
					public boolean mayPlace(ItemStack stack){ return stack.canEquip(EquipmentSlot.HEAD, player); }
					public boolean mayPickup(Player playerIn)
					{
						ItemStack stack = getItem();
						return !stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack) ? false : super.mayPickup(playerIn);
					}
					
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
					}
				});
		this.addSlot(new Slot(bodyInventory, 2, 62, 18)
				{
					public int getMaxStackSize(){ return 1; }
					public boolean mayPlace(ItemStack stack){ return stack.canEquip(EquipmentSlot.CHEST, player); }
					public boolean mayPickup(Player playerIn)
					{
						ItemStack stack = getItem();
						return !stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack) ? false : super.mayPickup(playerIn);
					}
					
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
					}
				});
		this.addSlot(new Slot(bodyInventory, 1, 62, 36)
				{
					public int getMaxStackSize(){ return 1; }
					public boolean mayPlace(ItemStack stack){ return stack.canEquip(EquipmentSlot.LEGS, player); }
					public boolean mayPickup(Player playerIn)
					{
						ItemStack stack = getItem();
						return !stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack) ? false : super.mayPickup(playerIn);
					}
					
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
					}
				});
		this.addSlot(new Slot(bodyInventory, 0, 62, 54)
				{
					public int getMaxStackSize(){ return 1; }
					public boolean mayPlace(ItemStack stack){ return stack.canEquip(EquipmentSlot.FEET, player); }
					public boolean mayPickup(Player playerIn)
					{
						ItemStack stack = getItem();
						return !stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack) ? false : super.mayPickup(playerIn);
					}
					
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
					}
				});
		int slot = 4;
		for(int k=0; k<2; k++)
			for(int l=0; l<4; l++)
			{
				int slotX = 80 + k * 18;
				int slotY = l * 18;
				if(slot == 5)
					this.addSlot(new Slot(bodyInventory, slot, slotX, slotY)
					{
						@OnlyIn(Dist.CLIENT)
						public Pair<ResourceLocation, ResourceLocation> getBackground()
						{
							return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
						}
					});
				else
					this.addSlot(new Slot(bodyInventory, slot, slotX, slotY));
				
				slot++;
			}
		
		// Player inventory
		for(int i1 = 0; i1 < 3; ++i1)
			for(int k1 = 0; k1 < 9; ++k1)
				this.addSlot(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
		
		// Player hotbar
		for(int j1 = 0; j1 < 9; ++j1)
			this.addSlot(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
	}
	
	public boolean canInteractWith(Player playerIn)
	{
		return theBody.isAlive() && playerIn.isAlive() && playerIn.distanceTo(theBody) <= playerIn.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
	}
	
	public ItemStack quickMoveStack(Player playerIn, int index)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if(slot != null && slot.hasItem())
		{
			ItemStack stackInSlot = slot.getItem();
			itemStack = stackInSlot.copy();
			
			int slots = this.bodyInventory.getContainerSize();
			// Transfer from body slots to main inventory
			if(index < slots)
			{
				if(!this.moveItemStackTo(stackInSlot, slots, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			// Equipment slots
			else if(getSlot(0).mayPlace(stackInSlot) && !getSlot(0).hasItem())
			{
				if(!moveItemStackTo(stackInSlot, 0, 1, false))
					return ItemStack.EMPTY;
			}
			else if(getSlot(1).mayPlace(stackInSlot) && !getSlot(1).hasItem())
			{
				if(!moveItemStackTo(stackInSlot, 1, 2, false))
					return ItemStack.EMPTY;
			}
			else if(getSlot(2).mayPlace(stackInSlot) && !getSlot(2).hasItem())
			{
				if(!moveItemStackTo(stackInSlot, 2, 3, false))
					return ItemStack.EMPTY;
			}
			else if(getSlot(3).mayPlace(stackInSlot) && !getSlot(3).hasItem())
			{
				if(!moveItemStackTo(stackInSlot, 3, 4, false))
					return ItemStack.EMPTY;
			}
			// Body inventory
			else if(!moveItemStackTo(stackInSlot, 4, 12, true))
				return ItemStack.EMPTY;
			else
			{
				int invStart = slots + 27;
				int hotStart = invStart + 9;
				
				if(index >= invStart && index < hotStart)
				{
					if(!this.moveItemStackTo(stackInSlot, slots, invStart, false))
						return ItemStack.EMPTY;
				}
				else if(index >= slots && index < hotStart)
				{
					if(!this.moveItemStackTo(stackInSlot, invStart, hotStart, false))
						return ItemStack.EMPTY;
				}
				else if(!this.moveItemStackTo(stackInSlot, invStart, invStart, false))
					return ItemStack.EMPTY;
				
				return ItemStack.EMPTY;
			}
			
			if(stackInSlot.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
		}
		return itemStack;
	}
	
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		this.bodyInventory.stopOpen(playerIn);
	}
	
	public boolean stillValid(Player playerIn) { return this.bodyInventory.stillValid(playerIn); }
}
