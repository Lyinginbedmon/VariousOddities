package com.lying.variousoddities.client;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.world.savedata.SpellManager;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpellManagerClient extends SpellManager
{
	public SpellManagerClient()
	{
		this(DATA_NAME);
	}
	
	public SpellManagerClient(String name)
	{
		super(name);
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		return compound;
	}
	
	public void read(CompoundNBT compound)
	{
		
	}
	
	public boolean removeSpell(int index)
	{
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
		{
			List<SpellData> spells = DIM_TO_SPELLS.get(dim);
			SpellData foundSpell = null;
			for(SpellData spell : spells)
				if(spell.getID() == index)
				{
					foundSpell = spell;
					break;
				}
			if(foundSpell != null)
			{
				spells.remove(foundSpell);
				DIM_TO_SPELLS.put(dim, spells);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Registers the given spell data and returns its unique ID.
	 * @param spell
	 * @return
	 */
	public int registerNewSpell(SpellData spell, World world)
	{
		spell.setID(nextID);
		
		ResourceLocation dim = world.getDimensionKey().getRegistryName();
		spell.setDim(dim);
		List<SpellData> spells = DIM_TO_SPELLS.containsKey(dim) ? DIM_TO_SPELLS.get(dim) : new ArrayList<SpellData>();
		spells.add(spell);
		DIM_TO_SPELLS.put(dim, spells);
		
		return nextID++;
	}
}
