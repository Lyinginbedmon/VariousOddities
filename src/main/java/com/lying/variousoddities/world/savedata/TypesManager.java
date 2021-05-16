package com.lying.variousoddities.world.savedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketTypesData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

public class TypesManager extends WorldSavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_types";
	
	private Map<EnumCreatureType, List<ResourceLocation>> typeToMob = new HashMap<>();
	private Map<ResourceLocation, List<EnumCreatureType>> mobTypeCache = new HashMap<>();
	
	private ServerWorld world = null;
	
	public TypesManager()
	{
		this(DATA_NAME);
	}
	
	public TypesManager(String nameIn)
	{
		super(nameIn);
	}
	
	public void read(CompoundNBT compound)
	{
		typeToMob.clear();
		ListNBT mobs = compound.getList("Mobs", 10);
		for(int i=0; i<mobs.size(); i++)
		{
			CompoundNBT typ = mobs.getCompound(i);
			EnumCreatureType type = EnumCreatureType.fromName(typ.getString("Type"));
			
			ListNBT entr = typ.getList("Entries", 8);
			List<ResourceLocation> entries = new ArrayList<>();
			for(int j=0; j<entr.size(); j++)
				entries.add(new ResourceLocation(entr.getString(j)));
			
			typeToMob.put(type, entries);
		}
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		ListNBT mobs = new ListNBT();
		for(EnumCreatureType type : typeToMob.keySet())
		{
			CompoundNBT typ = new CompoundNBT();
			typ.putString("Type", type.getString());
			ListNBT entries = new ListNBT();
			for(ResourceLocation entry : typeToMob.get(type))
				entries.add(StringNBT.valueOf(entry.toString()));
			typ.put("Entries", entries);
			
			mobs.add(typ);
		}
		compound.put("Mobs", mobs);
		return compound;
	}
	
	public static TypesManager get(World worldIn)
	{
		if(worldIn.isRemote)
			return VariousOddities.proxy.getTypesManager();
		else
		{
			ServerWorld world = (ServerWorld)worldIn;
			MinecraftServer server = world.getServer();
			ServerWorld overWorld = server.getWorld(World.OVERWORLD);
			TypesManager manager = (TypesManager)overWorld.getSavedData().get(TypesManager::new, DATA_NAME);
			if(manager == null)
			{
				manager = (TypesManager)overWorld.getSavedData().getOrCreate(TypesManager::new, DATA_NAME);
				manager.resetMobs();
			}
			manager.world = world;
			return manager;
		}
	}
	
	public void notifyPlayer(PlayerEntity player)
	{
		PacketHandler.sendTo((ServerPlayerEntity)player, new PacketTypesData(write(new CompoundNBT())));
	}
	
	public void notifyPlayers(ServerWorld world)
	{
		PacketHandler.sendToAll(world, new PacketTypesData(write(new CompoundNBT())));
	}
	
	public void clearCaches()
	{
		mobTypeCache.clear();
	}
	
	public void resetMobs()
	{
		typeToMob.clear();
		mobTypeCache.clear();
		
		Map<EnumCreatureType, String[]> configuredMobs = ConfigVO.MOBS.typeSettings.getMobTypes();
		for(EnumCreatureType type : configuredMobs.keySet())
			if(configuredMobs.get(type).length > 0)
				for(String entry : configuredMobs.get(type))
					if(entry != null && entry.length() > 0 && entry.contains(":"))
						addToEntity(new ResourceLocation(entry), type, false);
		
		markDirty();
	}
	
	public List<ResourceLocation> mobsOfType(EnumCreatureType type)
	{
		List<ResourceLocation> mobs = typeToMob.getOrDefault(type, Collections.emptyList());
		java.util.Collections.sort(mobs);
		return mobs;
	}
	
	public List<EnumCreatureType> getMobTypes(Entity entity)
	{
		return entity != null && entity instanceof LivingEntity ? EnumCreatureType.getCreatureTypes((LivingEntity)entity) : Collections.emptyList();
	}
	
	public List<EnumCreatureType> getMobTypes(EntityType<?> typeIn)
	{
		return getMobTypes(typeIn.getRegistryName());
	}
	public List<EnumCreatureType> getMobTypes(ResourceLocation registryName)
	{
		if(mobTypeCache.containsKey(registryName))
			return mobTypeCache.get(registryName);
		
		List<EnumCreatureType> types = new ArrayList<>();
		for(EnumCreatureType type : typeToMob.keySet())
			for(ResourceLocation entry : typeToMob.getOrDefault(type, Collections.emptyList()))
				if(entry.equals(registryName))
				{
					types.add(type);
					break;
				}
		
		if(types.isEmpty())
		{
			EntityType<?> ent = EntityType.byKey(registryName.toString()).get();
			if(ent != null && ent.create(this.world) instanceof LivingEntity)
			{
				LivingEntity entity = (LivingEntity)ent.create(this.world);
				CreatureAttribute attribute = entity.getCreatureAttribute();
				if(attribute == CreatureAttribute.UNDEAD)
					types.add(EnumCreatureType.UNDEAD);
				else if(attribute == CreatureAttribute.ARTHROPOD)
					types.add(EnumCreatureType.VERMIN);
				else
					types.add(EnumCreatureType.HUMANOID);
				
				if(attribute == CreatureAttribute.WATER)
					types.add(EnumCreatureType.AQUATIC);
			}
		}
		
		mobTypeCache.put(registryName, types);
		return types;
	}
	
	public boolean isMobOfType(ResourceLocation input, EnumCreatureType group)
	{
		if(group == null) return false;
		return getMobTypes(input).contains(group);
	}
	
	public static boolean isMobOfType(LivingEntity input, EnumCreatureType group)
	{
		if(group == null) return false;
		return EnumCreatureType.getCreatureTypes(input).contains(group);
	}
	
	public void addToEntity(ResourceLocation entity, EnumCreatureType type, boolean notify)
	{
		if(entity == null || type == null || isMobOfType(entity, type))
			return;
		
		List<ResourceLocation> entries = new ArrayList<ResourceLocation>();
			entries.addAll(mobsOfType(type));
			entries.add(entity);
		typeToMob.put(type, entries);
		mobTypeCache.remove(entity.toString());
		
		if(notify)
			markDirty();
	}
	
	public void removeFromEntity(ResourceLocation entity, EnumCreatureType type, boolean notify)
	{
		if(entity == null || type == null || !isMobOfType(entity, type))
			return;
		List<ResourceLocation> entries = new ArrayList<ResourceLocation>();
			entries.addAll(typeToMob.getOrDefault(type, Collections.emptyList()));
			entries.remove(entity);
		typeToMob.put(type, entries);
		mobTypeCache.remove(entity.toString());
		
		if(notify)
			markDirty();
	}
	
	public void markDirty()
	{
		super.markDirty();
		if(this.world != null)
			notifyPlayers(this.world);
	}
}
