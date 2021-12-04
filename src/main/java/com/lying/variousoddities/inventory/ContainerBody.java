package com.lying.variousoddities.inventory;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.init.VOItems;
import com.mojang.datafixers.util.Pair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;

public class ContainerBody extends Container
{
	public static ContainerBody fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf)
	{
		PlayerEntity player = inv.player;
		double range = player.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
		Vector3d eyeStart = player.getEyePosition(0F);
		Vector3d eyeEnd = eyeStart.add(player.getLook(0F).mul(range, range, range));
		
		AbstractBody likelyBody = null;
		double minDist = Double.MAX_VALUE;
		for(AbstractBody body : player.getEntityWorld().getEntitiesWithinAABB(AbstractBody.class, player.getBoundingBox().grow(range)))
		{
			double dist = body.getDistance(player);
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
	private final IInventory bodyInventory;
	
	public ContainerBody(int windowId, PlayerInventory playerInventory, IInventory bodyInventory, final AbstractBody bodyIn)
	{
		super(VOItems.CONTAINER_BODY, windowId);
		this.theBody = bodyIn;
		this.bodyInventory = bodyInventory;
		
		PlayerEntity player = playerInventory.player;
		bodyInventory.openInventory(player);
		
		// Body inventory
		this.addSlot(new Slot(bodyInventory, 3, 62, 0)
				{
					public int getSlotStackLimit(){ return 1; }
					public boolean isItemValid(ItemStack stack){ return stack.canEquip(EquipmentSlotType.HEAD, player); }
					public boolean canTakeStack(PlayerEntity playerIn)
					{
						ItemStack stack = getStack();
						return !stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack) ? false : super.canTakeStack(playerIn);
					}
					
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET);
					}
				});
		this.addSlot(new Slot(bodyInventory, 2, 62, 18)
				{
					public int getSlotStackLimit(){ return 1; }
					public boolean isItemValid(ItemStack stack){ return stack.canEquip(EquipmentSlotType.CHEST, player); }
					public boolean canTakeStack(PlayerEntity playerIn)
					{
						ItemStack stack = getStack();
						return !stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack) ? false : super.canTakeStack(playerIn);
					}
					
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE);
					}
				});
		this.addSlot(new Slot(bodyInventory, 1, 62, 36)
				{
					public int getSlotStackLimit(){ return 1; }
					public boolean isItemValid(ItemStack stack){ return stack.canEquip(EquipmentSlotType.LEGS, player); }
					public boolean canTakeStack(PlayerEntity playerIn)
					{
						ItemStack stack = getStack();
						return !stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack) ? false : super.canTakeStack(playerIn);
					}
					
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS);
					}
				});
		this.addSlot(new Slot(bodyInventory, 0, 62, 54)
				{
					public int getSlotStackLimit(){ return 1; }
					public boolean isItemValid(ItemStack stack){ return stack.canEquip(EquipmentSlotType.FEET, player); }
					public boolean canTakeStack(PlayerEntity playerIn)
					{
						ItemStack stack = getStack();
						return !stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack) ? false : super.canTakeStack(playerIn);
					}
					
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS);
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
							return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
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
			// Transfer from body slots to main inventory
			if(index < slots)
			{
				if(!this.mergeItemStack(stackInSlot, slots, this.inventorySlots.size(), true))
					return ItemStack.EMPTY;
			}
			// Equipment slots
			else if(getSlot(0).isItemValid(stackInSlot) && !getSlot(0).getHasStack())
			{
				if(!mergeItemStack(stackInSlot, 0, 1, false))
					return ItemStack.EMPTY;
			}
			else if(getSlot(1).isItemValid(stackInSlot) && !getSlot(1).getHasStack())
			{
				if(!mergeItemStack(stackInSlot, 1, 2, false))
					return ItemStack.EMPTY;
			}
			else if(getSlot(2).isItemValid(stackInSlot) && !getSlot(2).getHasStack())
			{
				if(!mergeItemStack(stackInSlot, 2, 3, false))
					return ItemStack.EMPTY;
			}
			else if(getSlot(3).isItemValid(stackInSlot) && !getSlot(3).getHasStack())
			{
				if(!mergeItemStack(stackInSlot, 3, 4, false))
					return ItemStack.EMPTY;
			}
			// Body inventory
			else if(!mergeItemStack(stackInSlot, 4, 12, true))
				return ItemStack.EMPTY;
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
