package com.lying.variousoddities.inventory;

import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.init.VOItems;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

public class ContainerWarg extends AbstractContainerMenu
{
	public static ContainerWarg fromNetwork(int windowId, Inventory inv)
	{
		Entity mount = inv.player.getVehicle();
		if(mount instanceof EntityWarg)
			return new ContainerWarg(windowId, inv, ((EntityWarg)mount).wargChest, ((EntityWarg)mount));
		return null;
	}
	
	public final EntityWarg theWarg;
	private final Container wargInventory;
	
	public ContainerWarg(int windowId, Inventory playerInventory, Container wargInventory, final EntityWarg wargIn)
	{
		super(VOItems.CONTAINER_WARG.get(), windowId);
		this.theWarg = wargIn;
		this.wargInventory = wargInventory;
		wargInventory.startOpen(playerInventory.player);
		// Warg saddle
		this.addSlot(new Slot(wargInventory, 0, 8, 18)
		{
			public boolean mayPlace(ItemStack stack){ return stack.is(Items.SADDLE) && !this.hasItem(); }
			
			@OnlyIn(Dist.CLIENT)
			public boolean isEnabled(){ return theWarg.isTamed(); }
		});
		
		// Warg carpet
		this.addSlot(new Slot(wargInventory, 1, 8, 36)
		{
			public boolean mayPlace(ItemStack stack)
			{
				return stack.is(ItemTags.WOOL_CARPETS) && !this.hasItem();
			}
			
			@OnlyIn(Dist.CLIENT)
			public boolean isEnabled(){ return theWarg.isTamed(); }
			
			public int getMaxStackSize(){ return 1; }
		});
		
		// Warg armour
		this.addSlot(new Slot(wargInventory, 2, 8, 54)
		{
			public boolean mayPlace(ItemStack stack){ return stack.getItem() instanceof HorseArmorItem && !this.hasItem(); }
			
			@OnlyIn(Dist.CLIENT)
			public boolean isEnabled(){ return theWarg.isTamed(); }
		});
		
		// Warg saddle bags
		if(theWarg.hasChest())
			for(int k = 0; k < 3; ++k)
	            for(int l = 0; l < wargIn.inventoryColumns(); ++l)
	               this.addSlot(new Slot(wargInventory, 3 + l + (k * wargIn.inventoryColumns()), 80 + l * 18, 18 + k * 18));
		
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
		return theWarg.isAlive() && playerIn.isAlive() && (playerIn.getVehicle() == theWarg || playerIn.distanceTo(theWarg) <= playerIn.getAttributeValue(ForgeMod.REACH_DISTANCE.get()));
	}
	
	public ItemStack quickMoveStack(Player playerIn, int index)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if(slot != null && slot.hasItem())
		{
			ItemStack stackInSlot = slot.getItem();
			itemStack = stackInSlot.copy();
			
			int slots = this.theWarg.getContainerSize();
			// Transfer from warg slots to main inventory
			if(index < slots)
			{
				if(!this.moveItemStackTo(stackInSlot, slots, this.slots.size(), true))
					return ItemStack.EMPTY;
			}
			// Saddle slot
			else if(this.getSlot(0).mayPlace(stackInSlot) && !this.getSlot(0).hasItem())
			{
				if(!this.moveItemStackTo(stackInSlot, 0, 1, false))
					return ItemStack.EMPTY;
			}
			// Carpet slot
			else if(this.getSlot(1).mayPlace(stackInSlot) && !this.getSlot(1).hasItem())
			{
				if(!this.moveItemStackTo(stackInSlot, 1, 2, false))
					return ItemStack.EMPTY;
			}
			// Armour slot
			else if(this.getSlot(2).mayPlace(stackInSlot) && !this.getSlot(2).hasItem())
			{
				if(!this.moveItemStackTo(stackInSlot, 2, 3, false))
					return ItemStack.EMPTY;
			}
			// Warg inventory
			else if(slots > 3)
			{
				if(!this.moveItemStackTo(stackInSlot, 3, slots, false))
					return ItemStack.EMPTY;
			}
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
		this.wargInventory.stopOpen(playerIn);
	}
	
	public boolean stillValid(Player playerIn) { return this.wargInventory.stillValid(playerIn); }
}
