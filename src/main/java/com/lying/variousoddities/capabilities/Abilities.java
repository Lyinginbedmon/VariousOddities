package com.lying.variousoddities.capabilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.api.event.AbilityEvent.AbilityAddEvent;
import com.lying.variousoddities.api.event.AbilityEvent.AbilityRemoveEvent;
import com.lying.variousoddities.api.event.AbilityEvent.AbilityUpdateEvent;
import com.lying.variousoddities.api.event.GatherAbilitiesEvent;
import com.lying.variousoddities.network.PacketAbilityCooldown;
import com.lying.variousoddities.network.PacketAbilityRemove;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncAbilities;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilitySwim;
import com.lying.variousoddities.species.abilities.IBonusJumpAbility;
import com.lying.variousoddities.species.abilities.IBonusJumpAbility.JumpType;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;

public class Abilities
{
	private static final UUID UUID_ABILITIES = UUID.fromString("f7bc7eeb-69ea-43c7-8b3a-e85f1abbc817");
	
	public static int FAVOURITE_SLOTS = 5;
	
	protected Map<ResourceLocation, Ability> customAbilities = new HashMap<>();
	protected Map<ResourceLocation, Integer> cooldowns = new HashMap<>();
	protected ResourceLocation[] favourites = new ResourceLocation[FAVOURITE_SLOTS];
	
	private Map<ResourceLocation, Ability> cachedAbilities = new HashMap<>();
	private boolean cacheDirty = false;
	
	public boolean canBonusJump = false;
	public int bonusJumpTimer = 0;
	private JumpType currentJumpType = null;
	
	public LivingEntity entity = null;
	
	/** Synchronise this object with surrounding entities */
	public void markDirty()
	{
		if(this.entity != null && !this.entity.getEntityWorld().isRemote)
		{
			PacketSyncAbilities packet = new PacketSyncAbilities(this.entity.getUniqueID(), serializeNBT());
			PacketHandler.sendToNearby(entity.getEntityWorld(), entity, packet);
		}
	}
	
	public void markForRecache(){ this.cacheDirty = true; }
	
	public CompoundNBT serializeNBT()
	{
		CompoundNBT compound = new CompoundNBT();
		if(!customAbilities.isEmpty())
		{
			ListNBT abilityList = new ListNBT();
			for(Ability ability : customAbilities.values())
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
		if(!cachedAbilities.isEmpty())
		{
			ListNBT abilityList = new ListNBT();
			for(Ability ability : cachedAbilities.values())
			{
				CompoundNBT abilityData = ability.writeAtomically(new CompoundNBT());
				ResourceLocation mapName = ability.getMapName();
				
				if(isAbilityOnCooldown(mapName))
					abilityData.putInt("Cooldown", this.cooldowns.get(mapName));
				
				if(isFavourite(mapName))
					abilityData.putInt("Favourite", favouriteIndex(mapName));
				
				abilityList.add(abilityData);
			}
			compound.put("CachedAbilities", abilityList);
		}
		compound.putBoolean("CanJump", this.canBonusJump);
		compound.putInt("JumpTimer", this.bonusJumpTimer);
		return compound;
	}
	
	public void deserializeNBT(CompoundNBT nbt)
	{
		this.customAbilities.clear();
		this.cachedAbilities.clear();
		this.cooldowns.clear();
		if(nbt.contains("Abilities", 9))
		{
			ListNBT abilityList = nbt.getList("Abilities", 10);
			this.favourites = new ResourceLocation[this.favourites.length];
			for(int i=0; i<abilityList.size(); i++)
			{
				CompoundNBT abilityData = abilityList.getCompound(i);
				Ability ability = AbilityRegistry.getAbility(abilityData);
				if(ability != null)
				{
					addCustomAbility(ability);
					
					ResourceLocation mapName = ability.getMapName();
					if(abilityData.contains("Cooldown", 3))
						this.cooldowns.put(mapName, abilityData.getInt("Cooldown"));
					
					if(abilityData.contains("Favourite", 3))
						this.favourites[abilityData.getInt("Favourite") % this.favourites.length] = mapName;
				}
			}
		}
		if(nbt.contains("CachedAbilities", 9))
		{
			ListNBT abilityList = nbt.getList("CachedAbilities", 10);
			this.favourites = new ResourceLocation[this.favourites.length];
			for(int i=0; i<abilityList.size(); i++)
			{
				CompoundNBT abilityData = abilityList.getCompound(i);
				Ability ability = AbilityRegistry.getAbility(abilityData);
				if(ability != null)
				{
					cacheAbility(ability);
					
					ResourceLocation mapName = ability.getMapName();
					if(abilityData.contains("Cooldown", 3))
						this.cooldowns.put(mapName, abilityData.getInt("Cooldown"));
					
					if(abilityData.contains("Favourite", 3))
						this.favourites[abilityData.getInt("Favourite") % this.favourites.length] = mapName;
				}
			}
			markForRecache();
			markDirty();
		}
		
		this.canBonusJump = nbt.getBoolean("CanJump");
		this.bonusJumpTimer = nbt.getInt("JumpTimer");
	}
	
	public int size(){ return this.customAbilities.size(); }
	
	public void cacheAbility(@Nonnull Ability ability)
	{
		try
		{
			this.cachedAbilities.put(ability.getMapName(), ability);
			markDirty();
		}
		catch(Exception e){ }
	}
	
	public void uncacheAbility(@Nonnull ResourceLocation mapName)
	{
		try
		{
			this.cachedAbilities.remove(mapName);
			markDirty();
		}
		catch(Exception e){ }
	}
	
	public void addCustomAbility(@Nonnull Ability ability)
	{
		try
		{
			this.customAbilities.put(ability.getMapName(), ability.setSourceId(UUID_ABILITIES));
			if(this.entity != null)
				ability.onAbilityAdded(this.entity);
			markForRecache();
			markDirty();
		}
		catch(Exception e){ }
	}
	
	public void removeCustomAbility(ResourceLocation mapName)
	{
		Ability ability = this.customAbilities.get(mapName);
		if(ability != null && this.entity != null)
		{
			ability.onAbilityRemoved(this.entity);
			MinecraftForge.EVENT_BUS.post(new AbilityRemoveEvent(this.entity, ability, this));
			if(this.entity.getType() == EntityType.PLAYER && !this.entity.getEntityWorld().isRemote)
				PacketHandler.sendTo((ServerPlayerEntity)this.entity, new PacketAbilityRemove(ability.getMapName()));
		}
		this.customAbilities.remove(mapName);
		markForRecache();
		markDirty();
	}
	
	public void removeCustomAbility(Ability ability){ removeCustomAbility(ability.getMapName()); uncacheAbility(ability.getMapName()); };
	
	public void clearCustomAbilities(){ this.customAbilities.clear(); markForRecache(); markDirty(); }
	
	public Map<ResourceLocation, Ability> addCustomToMap(Map<ResourceLocation, Ability> abilityMap)
	{
		for(Ability ability : customAbilities.values())
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
			// Refresh cached abilities
			if(this.cacheDirty)
				updateAbilityCache();
			
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
			
			updateBonusJumpAbilities();
			
			MinecraftForge.EVENT_BUS.post(new AbilityUpdateEvent(this.entity, this));
		}
		
		if(dirty)
			markDirty();
	}
	
	public Map<ResourceLocation, Ability> getCachedAbilities()
	{
		return this.cachedAbilities;
	}
	
	private Map<ResourceLocation, Ability> getCurrentAbilities()
	{
		Map<ResourceLocation, Ability> abilityMap = new HashMap<>();
		if(this.entity != null)
		{
			// Collect abilities from creature's types
			EnumCreatureType.getTypes(this.entity).addAbilitiesToMap(abilityMap);
			
			// Collect abilities from creature's LivingData
			LivingData data = LivingData.forEntity(this.entity);
			if(data != null)
			{
				if(data.hasSpecies())
					abilityMap = data.getSpecies().addToMap(abilityMap);
				abilityMap = data.getAbilities().addCustomToMap(abilityMap);
				
				if(data.hasTemplates())
					for(Template template : data.getTemplates())
						template.applyAbilityOperations(abilityMap);
			}
			
			GatherAbilitiesEvent event = new GatherAbilitiesEvent(this.entity, abilityMap);
			MinecraftForge.EVENT_BUS.post(event);
			
			abilityMap = event.getAbilityMap();
		}
		
		// Remove any abilities not possessing a source ID
		List<ResourceLocation> invalid = Lists.newArrayList();
		abilityMap.forEach((mapName, ability) -> { if(ability.getSourceId() == null) invalid.add(mapName); });
		for(ResourceLocation mapName : invalid)
			abilityMap.remove(mapName);
		
		return abilityMap;
	}
	
	public void updateAbilityCache()
	{
		if(this.entity == null)
			return;
		
		boolean dirty = false;
		
		Map<ResourceLocation, Ability> currentAbilities = getCurrentAbilities();
		// If a map name in cachedAbilities doesn't exist in currentAbilities, remove it from cachedAbilities
		List<ResourceLocation> removedAbilities = Lists.newArrayList();
		cachedAbilities.keySet().forEach((mapname) -> { if(!currentAbilities.containsKey(mapname)) removedAbilities.add(mapname); });
		if(!removedAbilities.isEmpty())
			dirty = true;
		removedAbilities.forEach((mapname) -> 
		{
			Ability ability = cachedAbilities.get(mapname);
			ability.onAbilityRemoved(this.entity);
			MinecraftForge.EVENT_BUS.post(new AbilityRemoveEvent(this.entity, ability, this));
			if(this.entity.getType() == EntityType.PLAYER)
				PacketHandler.sendTo((ServerPlayerEntity)this.entity, new PacketAbilityRemove(mapname));
			uncacheAbility(mapname);
		});
		
		List<ResourceLocation> overrides = Lists.newArrayList();
		currentAbilities.forEach((mapname,ability) -> 
		{
			// If a map name exists in currentAbilities that isn't in cachedAbilities, add it to cachedAbilities
			// If the source ID of an ability in currentAbilities doesn't match its counterpart in cachedAbilities, overwrite it in cachedAbilities
			if(!cachedAbilities.containsKey(mapname) || !ability.getSourceId().equals(cachedAbilities.get(mapname).getSourceId()))
				overrides.add(mapname);
		});
		if(!overrides.isEmpty())
			dirty = true;
		overrides.forEach((mapname) -> 
		{
			Ability ability = currentAbilities.get(mapname);
			ability.onAbilityAdded(this.entity);
			MinecraftForge.EVENT_BUS.post(new AbilityAddEvent(this.entity, ability, this));
			cacheAbility(ability);
		});
		
		if(dirty)
			markDirty();
		this.cacheDirty = false;
	}
	
	public void updateBonusJumpAbilities()
	{
		if(this.entity == null || !this.entity.isAlive())
			return;
		
		List<Ability> bonusJumps = Lists.newArrayList();
		this.cachedAbilities.values().forEach((ability) -> { if(ability instanceof IBonusJumpAbility) bonusJumps.add(ability); });
		
		boolean noneValid = true;
		for(Ability ability : bonusJumps)
		{
			if(!ability.passive() && !ability.isActive())
				continue;
			
			IBonusJumpAbility jump = (IBonusJumpAbility)ability;
			// Start and/or increment jump timer
			if(jump.isValid(this.entity, this.entity.getEntityWorld()) && (this.currentJumpType == null || this.currentJumpType == jump.jumpType()))
			{
				this.currentJumpType = jump.jumpType();
				if(!canBonusJump)
					if(bonusJumpTimer++ >= jump.getRate())
					{
						canBonusJump = true;
						bonusJumpTimer = 0;
						markDirty();
					}
				
				noneValid = false;
				break;
			}
		}
		
		if(bonusJumps.isEmpty() || noneValid)
		{
			canBonusJump = false;
			bonusJumpTimer = -(Reference.Values.TICKS_PER_SECOND / 2);
			currentJumpType = null;
			markDirty();
			return;
		}
	}
	
	/** Returns true if the entity is alive, not mounted, and not sleeping */
	public static boolean canBonusJump(@Nullable LivingEntity entity)
	{
		return entity != null && entity.isAlive() && entity.getRidingEntity() == null && !entity.isSleeping();
	}
	
	public void doAirJump()
	{
		if(!canBonusJump(this.entity) || this.entity.isOnGround())
			return;
		Map<ResourceLocation, Ability> abilities = AbilityRegistry.getCreatureAbilities(this.entity);
		if(!abilities.containsKey(AbilityFlight.REGISTRY_NAME))
			return;
		
		AbilityFlight flight = (AbilityFlight)abilities.get(AbilityFlight.REGISTRY_NAME);
		if(!flight.isActive())
			return;
		
		double scale = flight.flySpeed();
		Vector3d motion = entity.getLookVec();
		entity.setMotion(motion.x * scale, motion.y * scale, motion.z * scale);
		
		if(entity.getRNG().nextInt(4) == 0)
			entity.getEntityWorld().playSound(entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, entity.getSoundCategory(), 5.0F, 0.8F + entity.getRNG().nextFloat() * 0.3F, false);
		
		resetBonusJump();
	}
	
	public void doWaterJump()
	{
		if(!canBonusJump(this.entity) || !AbilitySwim.isEntitySwimming(this.entity))
			return;
		Map<ResourceLocation, Ability> abilities = AbilityRegistry.getCreatureAbilities(this.entity);
		if(!abilities.containsKey(AbilitySwim.REGISTRY_NAME))
			return;
		
		double scale = 0.7D;
		Vector3d motion = entity.getLookVec();
		entity.addVelocity(motion.x * scale, motion.y * scale, motion.z * scale);
		
		entity.getEntityWorld().playSound(entity.getPosX(), entity.getPosY(), entity.getPosZ(), SoundEvents.ITEM_TRIDENT_RIPTIDE_1, entity.getSoundCategory(), 5.0F, 0.8F + entity.getRNG().nextFloat() * 0.3F, false);
		
		resetBonusJump();
	}
	
	private void resetBonusJump()
	{
		this.canBonusJump = false;
		this.bonusJumpTimer = 0;
		markDirty();
	}
	
	public void copy(Abilities data)
	{
		this.customAbilities.clear();
		for(Ability ability : data.customAbilities.values())
			this.customAbilities.put(ability.getMapName(), ability);
		
		this.cooldowns.clear();
		for(ResourceLocation mapName : data.cooldowns.keySet())
			this.cooldowns.put(mapName, data.cooldowns.get(mapName));
		
		for(int i=0; i<this.favourites.length; i++)
			this.favourites[i] = data.favourites[i];
	}
}