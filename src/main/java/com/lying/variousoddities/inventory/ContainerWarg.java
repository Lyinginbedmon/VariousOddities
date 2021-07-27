package com.lying.variousoddities.inventory;

import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.init.VOItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

public class ContainerWarg extends Container
{
	public static ContainerWarg fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf)
	{
		Entity mount = inv.player.getRidingEntity();
		if(mount instanceof EntityWarg)
			return new ContainerWarg(windowId, inv, ((EntityWarg)mount));
		return null;
	}
	
	public final EntityWarg theWarg;
	
	public ContainerWarg(int windowId, PlayerInventory playerInventory, EntityWarg wargIn)
	{
		super(VOItems.CONTAINER_WARG, windowId);
		theWarg = wargIn;
		
		Inventory wargInventory = wargIn.wargChest;
		
		// Warg saddle
		this.addSlot(new Slot(wargInventory, 0, 8, 18)
		{
			public boolean isItemValid(ItemStack stack){ return stack.getItem() == Items.SADDLE && !this.getHasStack(); }
			
			@OnlyIn(Dist.CLIENT)
			public boolean isEnabled(){ return theWarg.isTamed(); }
		});
		
		// Warg carpet
		this.addSlot(new Slot(wargInventory, 1, 8, 36)
		{
			public boolean isItemValid(ItemStack stack)
			{
				return ItemTags.CARPETS.contains(stack.getItem()) && !this.getHasStack();
			}
			
			@OnlyIn(Dist.CLIENT)
			public boolean isEnabled(){ return theWarg.isTamed(); }
			
			public int getSlotStackLimit(){ return 1; }
		});
		
		// Warg armour
		this.addSlot(new Slot(wargInventory, 2, 8, 54)
		{
			public boolean isItemValid(ItemStack stack){ return stack.getItem() instanceof HorseArmorItem && !this.getHasStack(); }
			
			@OnlyIn(Dist.CLIENT)
			public boolean isEnabled(){ return theWarg.isTamed(); }
		});
		
		// Warg saddle bags
		if(theWarg.hasChest())
			for(int k = 0; k < 3; ++k)
	            for(int l = 0; l < wargIn.inventoryColumns(); ++l)
	               this.addSlot(new Slot(wargInventory, 3 + l + k * wargIn.inventoryColumns(), 80 + l * 18, 18 + k * 18));
		
		// Player inventory
		for(int i1 = 0; i1 < 3; ++i1)
			for(int k1 = 0; k1 < 9; ++k1)
				this.addSlot(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
		
		// Player hotbar
		for(int j1 = 0; j1 < 9; ++j1)
			this.addSlot(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
	}
	
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return theWarg.isAlive() && playerIn.isAlive() && (playerIn.getRidingEntity() == theWarg || playerIn.getDistance(theWarg) < playerIn.getAttributeValue(ForgeMod.REACH_DISTANCE.get()));
	}
	
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		System.out.println("Transferring from slot "+index);
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot != null && slot.getHasStack())
		{
			ItemStack stackInSlot = slot.getStack();
			itemStack = stackInSlot.copy();
			
			int slots = this.theWarg.getSizeInventory();
			// Transfer from warg slots to main inventory
			if(index < slots)
			{
				if(!this.mergeItemStack(stackInSlot, slots, this.inventorySlots.size(), true))
					return ItemStack.EMPTY;
			}
			// Saddle slot
			else if(this.getSlot(0).isItemValid(stackInSlot) && !this.getSlot(0).getHasStack())
			{
				if(!this.mergeItemStack(stackInSlot, 0, 1, false))
					return ItemStack.EMPTY;
			}
			// Carpet slot
			else if(this.getSlot(1).isItemValid(stackInSlot) && !this.getSlot(1).getHasStack())
			{
				if(!this.mergeItemStack(stackInSlot, 1, 2, false))
					return ItemStack.EMPTY;
			}
			// Armour slot
			else if(this.getSlot(2).isItemValid(stackInSlot) && !this.getSlot(2).getHasStack())
			{
				if(!this.mergeItemStack(stackInSlot, 2, 3, false))
					return ItemStack.EMPTY;
			}
			// Warg inventory
			else if(slots > 3)
			{
				if(!this.mergeItemStack(stackInSlot, 3, slots, false))
					return ItemStack.EMPTY;
			}
			else
			{
				int invStart = slots + 27;
				int hotStart = invStart + 9;
				
				if(index >= invStart && index < hotStart)
				{
					if(!this.mergeItemStack(stackInSlot, slots, invStart, false))
						return ItemStack.EMPTY;
				}
				else if(index >= slots && index < hotStart)
				{
					if(!this.mergeItemStack(stackInSlot, invStart, hotStart, false))
						return ItemStack.EMPTY;
				}
				else if(!this.mergeItemStack(stackInSlot, invStart, invStart, false))
					return ItemStack.EMPTY;
				
				return ItemStack.EMPTY;
			}
			
			if(stackInSlot.isEmpty())
				slot.putStack(ItemStack.EMPTY);
			else
				slot.onSlotChanged();
		}
		return itemStack;
	}
}
