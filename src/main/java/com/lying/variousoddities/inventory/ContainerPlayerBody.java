package com.lying.variousoddities.inventory;

import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.init.VOItems;
import com.mojang.datafixers.util.Pair;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
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

public class ContainerPlayerBody extends Container
{
	private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{
			PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS, 
			PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS, 
			PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE, 
			PlayerContainer.EMPTY_ARMOR_SLOT_HELMET};
	private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
	
	public static ContainerPlayerBody fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf)
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
		
		if(likelyBody != null && likelyBody.isPlayer() && likelyBody.getSoul() != null)
			return new ContainerPlayerBody(windowId, inv, ((PlayerEntity)likelyBody.getSoul()).inventory, likelyBody);
		return null;
	}
	
	public final AbstractBody theBody;
	private final PlayerInventory bodyInventory;
	
	public ContainerPlayerBody(int windowId, PlayerInventory playerInventory, PlayerInventory bodyInventory, final AbstractBody bodyIn)
	{
		super(VOItems.CONTAINER_PLAYER_BODY, windowId);
		this.theBody = bodyIn;
		this.bodyInventory = bodyInventory;
		
		PlayerEntity player = playerInventory.player;
		PlayerEntity player2 = bodyInventory.player;
		bodyInventory.openInventory(player);
		
		// Body inventory
		for(int i1 = 0; i1 < 3; ++i1)
			for(int k1 = 0; k1 < 9; ++k1)
				this.addSlot(new Slot(bodyInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, i1 * 18));
		
		// Body hotbar
		for(int j1 = 0; j1 < 9; ++j1)
			this.addSlot(new Slot(bodyInventory, j1, 8 + j1 * 18, 54));
		
		// Body off-hand slot
		this.addSlot(new Slot(bodyInventory, 40, -28, 18)
				{
					@OnlyIn(Dist.CLIENT)
					public Pair<ResourceLocation, ResourceLocation> getBackground()
					{
						return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
					}
				});
		
		// Body armour
		for(int k = 0; k < 4; ++k)
		{
			final EquipmentSlotType equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(new Slot(bodyInventory, 39 - k, -10, k * 18)
			{
				/**
				 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in
				 * the case of armor slots)
				 */
				public int getSlotStackLimit(){ return 1; }
				
				/**
				 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
				 */
				public boolean isItemValid(ItemStack stack)
				{
					return stack.canEquip(equipmentslottype, player2);
				}
				
				/**
				 * Return whether this slot's stack can be taken from this slot.
				 */
				public boolean canTakeStack(PlayerEntity playerIn)
				{
					ItemStack itemstack = this.getStack();
					return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
				}
				
				@OnlyIn(Dist.CLIENT)
				public Pair<ResourceLocation, ResourceLocation> getBackground()
				{
					return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, ARMOR_SLOT_TEXTURES[equipmentslottype.getIndex()]);
				}
			});
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
