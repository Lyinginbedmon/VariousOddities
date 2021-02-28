package com.lying.variousoddities.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.SpellRegisterEvent;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.SpellManager.SpellData;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class MagicEffects
{
	private static final Random rand = new Random();
	private static int MAX_LEVEL = Integer.MIN_VALUE;
	private static int MIN_LEVEL = Integer.MAX_VALUE;
	private static final Map<String, IMagicEffect> EFFECTS_MAP = new HashMap<>();
	public static List<IMagicEffect> allowedInnateSpells = new ArrayList<>();
	
	public static IMagicEffect ANTIMAGIC = null;
	
	public static int getTotalSpells(){ return getAllSpells().size(); }
	public static Collection<IMagicEffect> getAllSpells(){ return EFFECTS_MAP.values(); }
	
	public static Collection<String> getAllSpellNames()
	{
		return EFFECTS_MAP.keySet();
	}
	
	public static void reportKnownSpells()
	{
		if(getTotalSpells() > 0 && VariousOddities.log != null && ConfigVO.GENERAL.verboseLogs())
		{
			VariousOddities.log.info("Initialised with "+getTotalSpells()+" known spells:");
			
			List<String> spellNames = new ArrayList<String>();
			for(IMagicEffect spell : getAllSpells()) spellNames.add(spell.getSimpleName());
			
			Collections.sort(spellNames);
			int index = 1;
			for(String name : spellNames) VariousOddities.log.info((index++)+" - "+name);
		}
	}
	
	public static void initSpells()
	{
		MinecraftForge.EVENT_BUS.post(new SpellRegisterEvent());
		
		reportKnownSpells();
	}
	
	/** Registers the given spell to be included in scrolls, wands, etc. and useable in-game */
	public static IMagicEffect registerSpell(IMagicEffect effectIn)
	{
		return registerSpell(effectIn, false);
	}
	
	/** Registers the given spell to be included in scrolls, wands, etc. and useable in-game */
	public static IMagicEffect registerSpell(IMagicEffect effectIn, boolean overwrite)
	{
		if(ConfigVO.Magic.maxSpellLevel < 0) return null;
		int minLevel = Math.min(ConfigVO.Magic.minSpellLevel, ConfigVO.Magic.maxSpellLevel);
		int maxLevel = Math.max(ConfigVO.Magic.minSpellLevel, ConfigVO.Magic.maxSpellLevel);
		
		String spellName = effectIn.getSimpleName();
		if(EFFECTS_MAP.containsKey(spellName) && !overwrite)
		{
			VariousOddities.log.warn("Duplicate spell registered for name "+spellName);
			VariousOddities.log.warn("#  Class of conflicting spell: "+effectIn.getClass().getName());
		}
		else
		{
			List<EnumSpellProperty> properties = effectIn.getSpellProperties();
			if(properties == null)
			{
				VariousOddities.log.error("Voided spell "+spellName+" due to no spell properties, this usually only happens during development.");
				return null;
			}
			
			List<IMagicEffect> spells = EnumSpellProperty.getSpellsWithProperties(properties.toArray(new EnumSpellProperty[0]));
			if(!spells.isEmpty())
			{
				VariousOddities.log.warn("Duplicate spell properties for spell "+spellName);
				for(IMagicEffect spell : spells)
					VariousOddities.log.warn("#  Conflicting spell: "+spell.getSimpleName());
			}
			else if(ConfigVO.Magic.isSpellForbidden(spellName) || effectIn.getLevel() > maxLevel || effectIn.getLevel() < minLevel)
			{
				VariousOddities.log.info("Prevented spell registering due to config: "+spellName);
			}
			else
			{
				EFFECTS_MAP.put(spellName, effectIn);
				if(effectIn.getLevel() < 2)
					allowedInnateSpells.add(effectIn);
				if(effectIn.getLevel() > MAX_LEVEL) MAX_LEVEL = effectIn.getLevel();
				if(effectIn.getLevel() < MIN_LEVEL) MIN_LEVEL = effectIn.getLevel();
				return effectIn;
			}
		}
		return null;
	}
	
	/**
	 * Retrieves a spell via its numerical position in the greater spell list.<br>
	 * This is in general less reliable than using its name and getSpellFromName.<br>
	 */
	public static IMagicEffect getSpellFromID(int par1Int)
	{
		if(par1Int < 0 || par1Int >= EFFECTS_MAP.size()) return null;
		List<IMagicEffect> spells = new ArrayList<IMagicEffect>();
		spells.addAll(EFFECTS_MAP.values());
		return spells.get(par1Int);
	}
	
	/**
	 * Retrieves a spell via its internal name in the greater spell list.<br>
	 * The internal name is the name used in NBT data for container items.<br>
	 */
	public static IMagicEffect getSpellFromName(String par1String)
	{
		return EFFECTS_MAP.containsKey(par1String) ? EFFECTS_MAP.get(par1String) : null;
	}
	
	public static boolean spellIsRegistered(IMagicEffect par1Effect)
	{
		return EFFECTS_MAP.containsValue(par1Effect);
	}
	
	public static String getNameFromSpell(IMagicEffect par1Effect)
	{
		if(EFFECTS_MAP.containsValue(par1Effect))
		{
			for(String key : EFFECTS_MAP.keySet()) if(EFFECTS_MAP.get(key) == par1Effect) return key;
		}
		return "";
	}
	
	/** Returns the highest spell level currently registered. */
	public static int getMaxLevel(){ return MAX_LEVEL; }
	
	/** Returns the lowest spell level currently registered. */
	public static int getMinLevel(){ return MIN_LEVEL; }
	
	public static int getSpellLevel(String spellName)
	{
		return getSpellFromName(spellName) != null ? getSpellFromName(spellName).getLevel() : 0;
	}
	
	private static int getRandomSpellID()
	{
		return rand.nextInt(EFFECTS_MAP.size());
	}
	
	public static int getRandomSpellID(int minLevel, int maxLevel)
	{
		int baseMin = Math.max(0, minLevel);
		int baseMax = Math.min(9, maxLevel);
		
		minLevel = Math.min(baseMin, baseMax);
		maxLevel = Math.max(baseMin, baseMax);
		
		int ID = getRandomSpellID();
		while(getSpellFromID(ID).getLevel() > maxLevel || getSpellFromID(ID).getLevel() < minLevel) ID = getRandomSpellID();
		return ID;
	}
	
	public static IMagicEffect getRandomSpell(int minLevel, int maxLevel)
	{
		return getSpellFromID(getRandomSpellID(minLevel, maxLevel));
	}
	
    public static boolean isInsideAntiMagic(Entity entity)
    {
    	if(entity == null || entity.getEntityWorld() == null) return false;
    	return isInsideAntiMagic(entity.getEntityWorld(), entity.getPosX(), entity.getPosY(), entity.getPosZ());
    }
    
    private static boolean inRangeOfAntiMagic(SpellData spell, World world, double posX, double posY, double posZ)
    {
    	return spell.getCaster(world) != null && MathHelper.sqrt(spell.getCaster(world).getDistanceSq(posX, posY, posZ)) < Spell.feetToMetres(10D);
    }
    
    public static boolean isInsideAntiMagic(SpellData data, World world)
    {
    	if(data == null || world == null) return false;
    	for(SpellData spell : SpellManager.get(world).getSpellsOfTypeInDimension(MagicEffects.ANTIMAGIC, world.getDimensionType()))
    		if(data != spell && inRangeOfAntiMagic(spell, world, data.posX, data.posY, data.posZ))
    			return true;
    	
    	return false;
    }
    
    public static boolean isInsideAntiMagic(World world, double posX, double posY, double posZ)
    {
    	if(world == null) return false;
    	
    	for(SpellData spell : SpellManager.get(world).getSpellsOfTypeInDimension(MagicEffects.ANTIMAGIC, world.getDimensionType()))
    		if(inRangeOfAntiMagic(spell, world, posX, posY, posZ))
    			return true;
    	
    	return false;
    }
}
