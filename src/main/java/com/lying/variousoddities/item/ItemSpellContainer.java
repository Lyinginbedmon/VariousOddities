package com.lying.variousoddities.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.MagicEffects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Hand;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class ItemSpellContainer extends VOItem
{
	public ItemSpellContainer(Properties properties)
	{
		super(properties);
	}
	
	public static void registerSubItems(IForgeRegistry<Item> registry)
	{
		if(MagicEffects.getTotalSpells() > 0)
		{
			VOItems.register("spell_list", new ItemSpellList(new Properties().group(VOItemGroup.LOOT)));
			for(IMagicEffect spell : MagicEffects.getAllSpells())
			{
				VOItems.register("scroll_"+spell.getName(), new ItemSpellScroll(new Properties().group(VOItemGroup.LOOT)));
				
				// If spell is level 4 or less, also create wand
			}
		}
	}
    
    public void onSpellCast(String spellID, ItemStack stack, LivingEntity caster)
    {
    	if(caster instanceof PlayerEntity)
		{
    		PlayerEntity player = (PlayerEntity)caster;
//    		handlePlayerStats(player, MagicEffects.getSpellFromName(spellID));
    		if(player.isCreative()) return;
		}
    	
    	if(stack.isDamageable())
    		stack.damageItem(1, caster, (player) -> {
                     player.sendBreakAnimation(Hand.MAIN_HAND);
                  });
    }
    
    public static IMagicEffect getSpell(ItemStack stack)
    {
    	if(stack.getItem() instanceof ItemSpellList)
    		return MagicEffects.getSpellFromName(ItemSpellList.getCurrentSpell(stack));
    	else if(stack.hasTag())
    		return MagicEffects.getSpellFromName(getSpellName(stack));
    	return null;
    }
    
    public static String getSpellName(ItemStack stack)
    {
    	if(stack.getItem() instanceof ItemSpellList)
    	{
    		return ItemSpellList.getCurrentSpell(stack);
    	}
    	else if(stack.hasTag())
    	{
    		CompoundNBT stackData = stack.getTag();
    		if(stackData.contains("Spell")) return stack.getTag().getString("Spell");
    		else if(stackData.contains("ContainedSpells"))
    		{
    			String[] containedSpells = getContainedSpells(stack);
    			if(containedSpells.length > 0)
    			{
    				setSpell(stack, containedSpells[0]);
    				return containedSpells[0];
    			}
    			return null;
    		}
    		else if(stackData.contains("RandomSpell")) return generateRandomSpells(stack, stackData);
    	}
		return null;
    }
	
	public static String getLocalisedSpellName(ItemStack stackIn)
	{
    	if(getSpell(stackIn) != null) return getSpell(stackIn).getTranslatedName();
    	return "";
	}
    
    public static boolean isInverted(ItemStack stack)
    {
    	if(stack.hasTag() && stack.getTag().contains("Inverted")) return stack.getTag().getBoolean("Inverted");
    	return false;
    }
    
    public static void setInverted(ItemStack stack, boolean inverted)
    {
    	CompoundNBT stackData = stack.hasTag() ? stack.getTag() : new CompoundNBT();
    	stackData.putBoolean("Inverted", inverted);
    	stack.setTag(stackData);
    }
    
    public static String[] getContainedSpells(ItemStack stack)
    {
    	return getContainedSpells(stack, null);
    }
    
    public static String[] getContainedSpells(ItemStack stack, PlayerEntity player)
    {
    	if(stack.getItem() instanceof ItemSpellList)
    	{
//    		VOPlayerData playerData = VOPlayerData.getPlayerData(player);
//    		if(playerData != null)
//    			return playerData.getActiveList().toArray(new String[0]);
    	}
    	else if(stack.hasTag())
    	{
    		CompoundNBT stackData = stack.getTag();
    		if(stackData.contains("ContainedSpells"))
    		{
    			ListNBT spells = stackData.getList("ContainedSpells", 8);
    			String[] spellList = new String[spells.size()];
    			for(int i=0; i<spells.size(); i++) spellList[i] = spells.getString(i);
    			return spellList;
    		}
    	}
    	return new String[0];
    }
    
    public static String generateRandomSpells(ItemStack stack, CompoundNBT stackData)
    {
		Random rand = new Random();
		CompoundNBT randStats = stackData.getCompound("RandomSpell");
		
		// How many spells to generate
		int spellCount = 1;
		int minCount = randStats.contains("MinCount") ? Math.max(1, randStats.getInt("MinCount")) : 1;
		int maxCount = randStats.contains("MaxCount") ? Math.max(1, randStats.getInt("MaxCount")) : 1;
		if(minCount != maxCount) spellCount = Math.min(minCount, maxCount) + (rand.nextInt(Math.max(minCount, maxCount) - Math.min(minCount, maxCount)));
		else spellCount = minCount;
		spellCount = Math.max(1, Math.min(6, spellCount));
		
		// The level boundaries to generate within
		int minLevel = randStats.contains("MinLevel") ? Math.max(0, randStats.getInt("MinLevel")) : 0;
		int maxLevel = randStats.contains("MaxLevel") ? Math.min(MagicEffects.getMaxLevel(), randStats.getInt("MaxLevel")) : MagicEffects.getMaxLevel();
		
		stackData.remove("RandomSpell");
		stack.setTag(stackData);
		
		if(spellCount == 1)
		{
			IMagicEffect spell = MagicEffects.getRandomSpell(minLevel, maxLevel);
			setSpell(stack, spell);
			return spell.getSimpleName();
		}
		else
		{
			String firstName = null;
			List<Integer> spellIDs = new ArrayList<Integer>();
			List<String> spellNames = new ArrayList<String>();
			for(int i=0; i<spellCount; i++)
			{
				int ID = MagicEffects.getRandomSpellID(minLevel, maxLevel);
				while(spellIDs.contains(ID)) ID = MagicEffects.getRandomSpellID(minLevel, maxLevel);
				spellIDs.add(ID);
				spellNames.add(MagicEffects.getSpellFromID(ID).getSimpleName());
				if(firstName == null) firstName = MagicEffects.getSpellFromID(ID).getSimpleName();
			}
			
			setContainedSpells(stack, spellNames);
			return firstName;
		}
    }
    
    public static ItemStack setSpell(ItemStack stack, IMagicEffect spell)
    {
    	return setSpell(stack, spell.getSimpleName());
    }
    
    public static ItemStack setSpell(ItemStack stack, String spellName)
    {
    	CompoundNBT data = stack.hasTag() ? stack.getTag() : new CompoundNBT();
    	if(spellName != null) data.putString("Spell", spellName);
    	else if(data.contains("Spell")) data.remove("Spell");
    	stack.setTag(data);
    	return stack;
    }
    
    public static void setContainedSpells(ItemStack stack, List<String> spells)
    {
    	CompoundNBT stackData = stack.hasTag() ? stack.getTag() : new CompoundNBT();
    	if(spells.isEmpty())
    	{
    		if(stackData.contains("ContainedSpells")) stackData.remove("ContainedSpells");
    		if(stackData.contains("SpellID")) stackData.remove("SpellID");
    	}
    	else
    	{
    		spells = validateSpellNames(spells);
	    	ListNBT spellList = new ListNBT();
	    	for(String spell : spells)
    		{
	    		if(MagicEffects.getSpellFromName(spell) == null) continue;
	    		spellList.add(StringNBT.valueOf(spell));
    		}
	    	stackData.put("ContainedSpells", spellList);
			
	    	if(!stackData.contains("Spell") || !spells.contains(getSpellName(stack))) stackData.putString("Spell", spells.get(0));
    	}
		stack.setTag(stackData);
    }
    
    public static List<String> validateSpellNames(List<String> spells)
    {
    	List<String> validated = new ArrayList<String>();
    	for(String spell : spells) if(MagicEffects.getSpellFromName(spell) != null) validated.add(spell);
    	return validated;
    }
}
