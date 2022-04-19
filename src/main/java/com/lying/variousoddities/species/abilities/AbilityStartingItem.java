package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.api.event.SpeciesEvent;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.templates.AbilityOperation;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.templates.TemplateOperation.Operation;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityStartingItem extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "starting_item");
	
	private ItemStack[] itemStacks = new ItemStack[] {ItemStack.EMPTY};
	
	protected AbilityStartingItem()
	{
		super(REGISTRY_NAME);
	}
	
	public AbilityStartingItem(ListNBT stackList)
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
	
	public ResourceLocation getMapName() { return new ResourceLocation(Reference.ModInfo.MOD_ID, "starts_with_"+namingStack().getItem().getRegistryName().getPath()); }
	
	public ItemStack namingStack() { return this.itemStacks[0]; }
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability." + Reference.ModInfo.MOD_ID + ".starting_item", namingStack().getDisplayName());
	}
	
	public ITextComponent description()
	{
		return new TranslationTextComponent("ability."+Reference.ModInfo.MOD_ID+":starting_item.desc", stacksToList());
	}
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		ListNBT items = new ListNBT();
		for(ItemStack stack : this.itemStacks)
			items.add(stack.write(new CompoundNBT()));
		compound.put("Items", items);
		
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		if(compound.contains("Items", 9))
			this.itemStacks = listToStacks(compound.getList("Items", 10));
	}
	
	private static ItemStack[] listToStacks(ListNBT listIn)
	{
		if(listIn == null || listIn.size() == 0)
			return new ItemStack[] {ItemStack.EMPTY};
		
		ItemStack[] itemStacks = new ItemStack[listIn.size()];
		for(int i=0; i<listIn.size(); i++)
			itemStacks[i] = ItemStack.read(listIn.getCompound(i)).copy();
		
		return itemStacks;
	}
	
	private ITextComponent stacksToList()
	{
		IFormattableTextComponent list = new StringTextComponent("[");
		for(int i=0; i<this.itemStacks.length; i++)
		{
			ItemStack stack = this.itemStacks[i];
			if(stack.getCount() > 1)
				list.appendString(String.valueOf(stack.getCount()) + "x ");
			list.append(this.itemStacks[i].getDisplayName());
			if(i < this.itemStacks.length - 1)
				list.appendString(", ");
		}
		list.appendString("]");
		return list;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::onSpeciesSelected);
		bus.addListener(this::onTemplateAdded);
	}
	
	public void onSpeciesSelected(SpeciesEvent.SpeciesSelected event)
	{
		PlayerEntity player = (PlayerEntity)event.getEntityLiving();
		if(player.getEntityWorld().isRemote)
			return;
		
		Abilities abilities = LivingData.forEntity(player).getAbilities();
		for(Ability ability : abilities.getEntityAbilities(player).values())
			if(ability.getRegistryName().equals(REGISTRY_NAME))
				addItemsFromAbility((AbilityStartingItem)ability, player);
	}
	
	public void onTemplateAdded(SpeciesEvent.TemplateApplied event)
	{
		PlayerEntity player = (PlayerEntity)event.getEntityLiving();
		if(player.getEntityWorld().isRemote)
			return;
		
		Template template = VORegistries.TEMPLATES.get(event.getTemplate());
		if(template != null)
		{
			for(TemplateOperation operation : template.getOperations())
				if(operation.getRegistryName() == AbilityOperation.REGISTRY_NAME && operation.action() == Operation.ADD)
				{
					AbilityOperation addAbility = (AbilityOperation)operation;
					Ability ability = addAbility.getAbility();
					if(ability != null && ability.getRegistryName() == AbilityStartingItem.REGISTRY_NAME)
						addItemsFromAbility((AbilityStartingItem)ability, player);
				}
		}
	}
	
	private static void addItemsFromAbility(AbilityStartingItem ability, PlayerEntity player)
	{
		for(ItemStack stack : ability.itemStacks)
			player.addItemStackToInventory(stack.copy());
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return new AbilityStartingItem(compound.getList("Items", 10));
		}
	}
}
