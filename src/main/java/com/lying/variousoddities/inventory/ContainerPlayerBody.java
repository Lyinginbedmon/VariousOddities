package com.lying.variousoddities.inventory;

import com.lying.variousoddities.entity.AbstractBody;
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

public class ContainerPlayerBody extends AbstractContainerMenu
{
	private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{
			InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, 
			InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, 
			InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, 
			InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};
	private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
	
	public static ContainerPlayerBody fromNetwork(int windowId, Inventory inv)
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
		
		if(likelyBody != null && likelyBody.isPlayer() && likelyBody.getSoul() != null)
			return new ContainerPlayerBody(windowId, inv, ((Player)likelyBody.getSoul()).getInventory(), likelyBody);
		return null;
	}
	
	public final AbstractBody theBody;
	private final Container bodyInventory;
	
	public ContainerPlayerBody(int windowId, Inventory playerInventory, Inventory bodyInventory, final AbstractBody bodyIn)
	{
		super(VOItems.CONTAINER_PLAYER_BODY, windowId);
		this.theBody = bodyIn;
		this.bodyInventory = bodyInventory;
		
		Player player = playerInventory.player;
		Player player2 = bodyInventory.player;
		bodyInventory.startOpen(player);
		
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
						return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
					}
				});
		
		// Body armour
		for(int k = 0; k < 4; ++k)
		{
			final EquipmentSlot equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
			this.addSlot(new Slot(bodyInventory, 39 - k, -10, k * 18)
			{
				/**
				 * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in
				 * the case of armor slots)
				 */
				public int getMaxStackSize(){ return 1; }
				
				/**
				 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
				 */
				public boolean mayPlace(ItemStack stack)
				{
					return stack.canEquip(equipmentslottype, player2);
				}
				
				/**
				 * Return whether this slot's stack can be taken from this slot.
				 */
				public boolean mayPickup(Player playerIn)
				{
					ItemStack itemstack = this.getItem();
					return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.mayPickup(playerIn);
				}
				
				@OnlyIn(Dist.CLIENT)
				public Pair<ResourceLocation, ResourceLocation> getBackground()
				{
					return Pair.of(InventoryMenu.BLOCK_ATLAS, ARMOR_SLOT_TEXTURES[equipmentslottype.getIndex()]);
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
