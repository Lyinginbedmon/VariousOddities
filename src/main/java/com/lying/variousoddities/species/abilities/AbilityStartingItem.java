package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.SpeciesEvent;
import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.templates.AbilityOperation;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.templates.TemplateOperation.Operation;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityStartingItem extends Ability
{
	private ItemStack[] itemStacks = new ItemStack[] {ItemStack.EMPTY};
	
	protected AbilityStartingItem()
	{
		super();
	}
	
	public AbilityStartingItem(ListTag stackList)
	{
		this(listToStacks(stackList));
	}
	
	public AbilityStartingItem(ItemStack... stackIn)
	{
		this();
		
		this.itemStacks = new ItemStack[stackIn.length];
		for(int i=0; i<this.itemStacks.length; i++)
			this.itemStacks[i] = stackIn[i].copy();
	}
	
	public Type getType() { return Type.UTILITY; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	@SuppressWarnings("deprecation")
	public ResourceLocation getMapName() 
	{
		ResourceLocation itemName = Registry.ITEM.getKey(namingStack().getItem());
		return new ResourceLocation(Reference.ModInfo.MOD_ID, "starts_with_"+itemName.getPath());
	}
	
	public ItemStack namingStack() { return this.itemStacks[0]; }
	
	public Component translatedName()
	{
		return Component.translatable("ability." + Reference.ModInfo.MOD_ID + ".starting_item", namingStack().getDisplayName());
	}
	
	public Component description()
	{
		return Component.translatable("ability."+Reference.ModInfo.MOD_ID+":starting_item.desc", stacksToList());
	}
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		ListTag items = new ListTag();
		for(ItemStack stack : this.itemStacks)
			items.add(stack.save(new CompoundTag()));
		compound.put("Items", items);
		
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		if(compound.contains("Items", 9))
			this.itemStacks = listToStacks(compound.getList("Items", 10));
	}
	
	private static ItemStack[] listToStacks(ListTag listIn)
	{
		if(listIn == null || listIn.size() == 0)
			return new ItemStack[] {ItemStack.EMPTY};
		
		ItemStack[] itemStacks = new ItemStack[listIn.size()];
		for(int i=0; i<listIn.size(); i++)
			itemStacks[i] = ItemStack.of(listIn.getCompound(i)).copy();
		
		return itemStacks;
	}
	
	private Component stacksToList()
	{
		MutableComponent list = Component.literal("[");
		for(int i=0; i<this.itemStacks.length; i++)
		{
			ItemStack stack = this.itemStacks[i];
			if(stack.getCount() > 1)
				list.append(String.valueOf(stack.getCount()) + "x ");
			list.append(this.itemStacks[i].getDisplayName());
			if(i < this.itemStacks.length - 1)
				list.append(", ");
		}
		list.append("]");
		return list;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onSpeciesSelected);
		bus.addListener(this::onTemplateAdded);
	}
	
	public void onSpeciesSelected(SpeciesEvent.SpeciesSelected event)
	{
		Player player = (Player)event.getEntity();
		if(player.getLevel().isClientSide)
			return;
		
		AbilityData abilities = AbilityData.forEntity(player);
		for(Ability ability : abilities.getEntityAbilities(player).values())
			if(ability.getRegistryName().equals(getRegistryName()))
				addItemsFromAbility((AbilityStartingItem)ability, player);
	}
	
	public void onTemplateAdded(SpeciesEvent.TemplateApplied event)
	{
		Player player = (Player)event.getEntity();
		if(player.getLevel().isClientSide)
			return;
		
		Template template = VORegistries.TEMPLATES.get(event.getTemplate());
		if(template != null)
		{
			for(TemplateOperation operation : template.getOperations())
				if(operation.getRegistryName() == AbilityOperation.REGISTRY_NAME && operation.action() == Operation.ADD)
				{
					AbilityOperation addAbility = (AbilityOperation)operation;
					Ability ability = addAbility.getAbility();
					if(ability != null && ability.getRegistryName() == getRegistryName())
						addItemsFromAbility((AbilityStartingItem)ability, player);
				}
		}
	}
	
	private static void addItemsFromAbility(AbilityStartingItem ability, Player player)
	{
		for(ItemStack stack : ability.itemStacks)
			player.addItem(stack.copy());
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			return new AbilityStartingItem(compound.getList("Items", 10));
		}
	}
}
