package com.lying.variousoddities.init;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.DamageSource;

public class VODamageSource
{
	public static final Map<DamageSource, String[]> DAMAGE_SYNONYMS = new HashMap<>();
	
	public static final DamageSource COLD = new DamageSource("cold").setDamageBypassesArmor();
	public static final DamageSource EVIL = new DamageSource("evil").setDamageBypassesArmor();
	public static final DamageSource HOLY = new DamageSource("good").setDamageBypassesArmor();
	
	public static boolean isFalling(DamageSource source)
	{
		return source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL;
	}
	
	public static boolean isFire(DamageSource source)
	{
		return source.isFireDamage();
	}
	
	public static boolean isOrSynonym(DamageSource sourceA, DamageSource sourceB)
	{
		if(sourceA == sourceB) return true;
		else if(DAMAGE_SYNONYMS.containsKey(sourceB))
		{
			String type = sourceA.damageType.toLowerCase();
			for(String synonym : DAMAGE_SYNONYMS.get(sourceB))
				if(type.contains(synonym))
					return true;
		}
		return false;
	}
	
	public static boolean isCold(DamageSource source){ return isOrSynonym(source, COLD); }
	
	public static boolean isEvil(DamageSource source){ return isOrSynonym(source, EVIL); }
	
	public static boolean isHoly(DamageSource source){ return isOrSynonym(source, HOLY); }
	
	static
	{
		DAMAGE_SYNONYMS.put(COLD, new String[]{"cold", "frost", "ice", "chill"});
		DAMAGE_SYNONYMS.put(EVIL, new String[]{"evil", "vile", "demon", "profane"});
		DAMAGE_SYNONYMS.put(HOLY, new String[]{"holy", "good", "angel", "divine"});
	}
}
