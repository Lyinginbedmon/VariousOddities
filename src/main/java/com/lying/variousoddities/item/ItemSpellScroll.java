package com.lying.variousoddities.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemSpellScroll extends ItemSpellContainer
{
	private final String itemName;
	
	public ItemSpellScroll(Item.Properties properties)
	{
		this(properties, "spell_scroll");
	}
	
	public ItemSpellScroll(Item.Properties properties, String nameIn)
	{
		super(properties.maxStackSize(8).maxDamage(0));
		this.itemName = nameIn;
	}
	
//    /**
//     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
//     */
//    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
//    {
//        if(!this.isInCreativeTab(tab) || MagicEffects.getTotalSpells() == 0) return;
//        items.addAll(getVariants());
//    }
//    
//    public static List<ItemStack> getVariants()
//    {
//    	List<ItemStack> items = new ArrayList<ItemStack>();
//        for(IMagicEffect spell : MagicEffects.getAllSpells()) items.add(setSpell(new ItemStack(VOItems.SPELL_SCROLL), spell));
//        return items;
//    }
//    
//	public EnumLootType getLootType(ItemStack stack)
//	{
//		IMagicEffect spell = ItemSpellContainer.getSpell(stack);
//		if(spell != null)
//		{
//			switch(spell.getLevel())
//			{
//				case 0:
//				case 1:
//				case 2:
//					return EnumLootType.MINOR_MAGIC;
//				case 3:
//				case 4:
//					return EnumLootType.MEDIUM_MAGIC;
//				default:
//					return EnumLootType.MAJOR_MAGIC;
//			}
//		}
//		
//		return EnumLootType.getLootType(stack.getItem());
//	}
	
	public ITextComponent getDisplayName(ItemStack stack)
    {
		String prefix = this.itemName;
		if(this.itemName.equals("spell_scroll") && getContainedSpells(stack).length > 1) prefix = "compound_spell_scroll";
		return new TranslationTextComponent("item.varodd:"+prefix+".name", getLocalisedSpellName(stack));
    }
    
//    @SideOnly(Side.CLIENT)
//	public void addInformation(ItemStack par1ItemStack, World par2World, List<String> par3List, ITooltipFlag par4Flags)
//	{
//    	appendSpellData(par1ItemStack, par2World, par3List, par4Flags);
//		
//		if(getContainedSpells(par1ItemStack) != null && getContainedSpells(par1ItemStack).length > 1)
//			par3List.add(I18n.translateToLocalFormatted("info.varodd:spell_scroll.compound", getContainedSpells(par1ItemStack).length));
//	}
}
