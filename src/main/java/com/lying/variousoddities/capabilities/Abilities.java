package com.lying.variousoddities.capabilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.network.PacketAbilitiesSync;
import com.lying.variousoddities.network.PacketAbilityCooldown;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

public class Abilities
{
	public static int FAVOURITE_SLOTS = 5;
	
	protected Map<ResourceLocation, Ability> abilities = new HashMap<>();
	protected Map<ResourceLocation, Integer> cooldowns = new HashMap<>();
	protected ResourceLocation[] favourites = new ResourceLocation[FAVOURITE_SLOTS];
	
	public LivingEntity entity = null;
	
	/** Synchronise this object with surrounding entities */
	public void markDirty()
	{
		if(this.entity != null && !this.entity.getEntityWorld().isRemote)
		{
			PacketAbilitiesSync packet = new PacketAbilitiesSync(this.entity.getUniqueID(), serializeNBT());
			PacketHandler.sendToNearby(entity.getEntityWorld(), entity, packet);
		}
	}
	
	public CompoundNBT serializeNBT()
	{
		CompoundNBT compound = new CompoundNBT();
		if(!abilities.isEmpty())
		{
			ListNBT abilityList = new ListNBT();
			for(Ability ability : abilities.values())
			{
				CompoundNBT abilityData = ability.writeAtomically(new CompoundNBT());
				ResourceLocation mapName = ability.getMapName();
				
				if(isAbilityOnCooldown(mapName))
					abilityData.putInt("Cooldown", this.cooldowns.get(mapName));
				
				if(isFavourite(mapName))
					abilityData.putInt("Favourite", favouriteIndex(mapName));
				
				abilityList.add(abilityData);
			}
			compound.put("Abilities", abilityList);
		}
		return compound;
	}
	
	public void deserializeNBT(CompoundNBT nbt)
	{
		if(nbt.contains("Abilities", 9))
		{
			ListNBT abilityList = nbt.getList("Abilities", 10);
			this.abilities.clear();
			this.cooldowns.clear();
			this.favourites = new ResourceLocation[this.favourites.length];
			for(int i=0; i<abilityList.size(); i++)
			{
				CompoundNBT abilityData = abilityList.getCompound(i);
				Ability ability = AbilityRegistry.getAbility(abilityData);
				if(ability != null)
				{
					add(ability);
					
					ResourceLocation mapName = ability.getMapName();
					if(abilityData.contains("Cooldown", 3))
						this.cooldowns.put(mapName, abilityData.getInt("Cooldown"));
					
					if(abilityData.contains("Favourite", 3))
						this.favourites[abilityData.getInt("Favourite") % this.favourites.length] = mapName;
				}
			}
		}
	}
	
	public Collection<ResourceLocation> names(){ return this.abilities.keySet(); }
	
	public int size(){ return this.abilities.size(); }
	
	public void add(@Nonnull Ability ability)
	{
		try
		{
			this.abilities.put(ability.getMapName(), ability);
			markDirty();
		}
		catch(Exception e){ }
	}
	
	public void remove(ResourceLocation mapName)
	{
		this.abilities.remove(mapName);
		markDirty();
	}
	
	public void remove(Ability ability){ remove(ability.getMapName()); };
	
	public void clear(){ this.abilities.clear(); markDirty(); }
	
	public Map<ResourceLocation, Ability> addToMap(Map<ResourceLocation, Ability> abilityMap)
	{
		for(Ability ability : abilities.values())
			abilityMap.put(ability.getMapName(), ability);
		return abilityMap;
	}
	
	public void putOnCooldown(@Nonnull ResourceLocation mapName, int cooldown)
	{
		this.cooldowns.put(mapName, cooldown);
		markDirty();
	}
	
	public void putOnCooldown(@Nonnull Ability ability, int cooldown)
	{
		putOnCooldown(ability.getMapName(), cooldown);
	}
	
	public boolean isAbilityOnCooldown(ResourceLocation mapName)
	{
		return this.cooldowns.containsKey(mapName) && this.cooldowns.get(mapName) != 0;
	}
	
	@Nullable
	public ResourceLocation getFavourite(int index)
	{
		return this.favourites[index % FAVOURITE_SLOTS];
	}
	
	public boolean hasEmptyFavourites(){ return nextEmptyFavourite() >= 0; }
	
	public int nextEmptyFavourite()
	{
		for(int i=0; i<this.favourites.length; i++)
			if(this.favourites[i] == null)
				return i;
		return -1;
	}
	
	public void favourite(ResourceLocation mapName)
	{
		int index = nextEmptyFavourite();
		if(index >= 0)
		{
			this.favourites[index] = mapName;
			markDirty();
		}
	}
	
	public void unfavourite(ResourceLocation mapName)
	{
		for(int i=0; i<this.favourites.length; i++)
		{
			ResourceLocation name = this.favourites[i];
			if(name != null && name.equals(mapName))
			{
				this.favourites[i] = null;
				markDirty();
				return;
			}
		}
	}
	
	public boolean isFavourite(ResourceLocation mapName)
	{
		return favouriteIndex(mapName) >= 0;
	}
	
	public int favouriteIndex(ResourceLocation mapName)
	{
		for(int i=0; i<this.favourites.length; i++)
		{
			ResourceLocation favourite = this.favourites[i];
			if(favourite != null && favourite.toString().equalsIgnoreCase(mapName.toString()))
				return i;
		}
		return -1;
	}
	
	/** Called by LivingData to update cooldowns and remove favourited abilities the entity does not have */
	public void tick()
	{
		boolean dirty = false;
		if(this.entity != null && !this.entity.getEntityWorld().isRemote)
		{
			// Manage cooldowns
			List<ResourceLocation> finishedCooldowns = Lists.newArrayList();
			for(ResourceLocation mapName : cooldowns.keySet())
			{
				int cooldown = cooldowns.get(mapName);
				cooldown -= Math.signum(cooldown);
				cooldowns.put(mapName, cooldown);
				if(cooldown == 0)
					finishedCooldowns.add(mapName);
			}
			finishedCooldowns.forEach((mapName) -> { cooldowns.remove(mapName); });
			if(!finishedCooldowns.isEmpty())
			{
				if(this.entity.getType() == EntityType.PLAYER)
					PacketHandler.sendTo((ServerPlayerEntity)this.entity, new PacketAbilityCooldown());
				dirty = true;
			}
			
			/*
			 * Remove favourited abilities that the entity does not have.
			 * Note: Non-permanent effects should NOT remove activated abilities, just prevent them from triggering whilst active.
			 */
			for(ResourceLocation favourite : this.favourites)
				if(!AbilityRegistry.hasAbility(entity, favourite))
				{
					unfavourite(favourite);
					dirty = true;
				}
		}
		
		if(dirty)
			markDirty();
	}
	
	public void copy(Abilities data)
	{
		this.abilities.clear();
		for(Ability ability : data.abilities.values())
			this.abilities.put(ability.getMapName(), ability);
		
		this.cooldowns.clear();
		for(ResourceLocation mapName : data.cooldowns.keySet())
			this.cooldowns.put(mapName, data.cooldowns.get(mapName));
		
		for(int i=0; i<this.favourites.length; i++)
			this.favourites[i] = data.favourites[i];
	}
}