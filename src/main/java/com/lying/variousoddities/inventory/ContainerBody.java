package com.lying.variousoddities.inventory;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.init.VOItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.ForgeMod;

public class ContainerBody extends Container
{
	public static ContainerBody fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf)
	{
		Entity entity = inv.player.getEntityWorld().getEntityByID(buf.readInt());
		if(entity != null && entity instanceof AbstractBody)
		{
			AbstractBody body = (AbstractBody)entity;
			return new ContainerBody(windowId, inv, body.getInventory(), body);
		}
		return null;
	}
	
	public final AbstractBody theBody;
	private final IInventory bodyInventory;
	
	public ContainerBody(int windowId, PlayerInventory playerInventory, IInventory bodyInventory, final AbstractBody bodyIn)
	{
		super(VOItems.CONTAINER_BODY, windowId);
		this.theBody = bodyIn;
		this.bodyInventory = bodyInventory;
		bodyInventory.openInventory(playerInventory.player);
		// Body inventory
		for(int k = 0; k < 3; ++k)
            for(int l = 0; l < 4; ++l)
               this.addSlot(new Slot(bodyInventory, l + (k * 3), 80 + l * 18, 18 + k * 18));
		
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
		return theBody.isAlive() && playerIn.isAlive() && playerIn.getDistance(theBody) <= playerIn.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
	}
	
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if(slot != null && slot.getHasStack())
		{
			ItemStack stackInSlot = slot.getStack();
			itemStack = stackInSlot.copy();
			
			int slots = this.bodyInventory.getSizeInventory();
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
	
	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		this.bodyInventory.closeInventory(playerIn);
	}
}
