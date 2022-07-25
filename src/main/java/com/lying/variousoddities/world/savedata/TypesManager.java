package com.lying.variousoddities.world.savedata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.entity.IDefaultSpecies;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketTypesData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TypesManager extends WorldSavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_types";
	
	private Map<EnumCreatureType, List<ResourceLocation>> typeToMob = new HashMap<>();
	private Map<ResourceLocation, List<EnumCreatureType>> mobTypeCache = new HashMap<>();
	
	private ServerLevel world = null;
	
	public TypesManager()
	{
		this(DATA_NAME);
	}
	
	public TypesManager(String nameIn)
	{
		super(nameIn);
	}
	
	public void read(CompoundTag compound)
	{
		typeToMob.clear();
		ListTag mobs = compound.getList("Mobs", 10);
		for(int i=0; i<mobs.size(); i++)
		{
			CompoundTag typ = mobs.getCompound(i);
			EnumCreatureType type = EnumCreatureType.fromName(typ.getString("Type"));
			
			ListTag entr = typ.getList("Entries", 8);
			List<ResourceLocation> entries = new ArrayList<>();
			for(int j=0; j<entr.size(); j++)
				entries.add(new ResourceLocation(entr.getString(j)));
			
			typeToMob.put(type, entries);
		}
	}
	
	public CompoundTag write(CompoundTag compound)
	{
		ListTag mobs = new ListTag();
		for(EnumCreatureType type : typeToMob.keySet())
		{
			CompoundTag typ = new CompoundTag();
			typ.putString("Type", type.getSerializedName());
			ListTag entries = new ListTag();
			for(ResourceLocation entry : typeToMob.get(type))
				entries.add(StringTag.valueOf(entry.toString()));
			typ.put("Entries", entries);
			
			mobs.add(typ);
		}
		compound.put("Mobs", mobs);
		return compound;
	}
	
	@Nullable
	public static TypesManager get(@Nonnull Level worldIn)
	{
		if(worldIn == null)
			return null;
		else if(worldIn.isClientSide)
			return VariousOddities.proxy.getTypesManager();
		else
		{
			ServerLevel world = (ServerLevel)worldIn;
			MinecraftServer server = world.getServer();
			ServerLevel overWorld = server.getLevel(Level.OVERWORLD);
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
	
	public void notifyPlayer(Player player)
	{
		PacketHandler.sendTo((ServerPlayer)player, new PacketTypesData(write(new CompoundTag())));
	}
	
	public void notifyPlayers(ServerLevel world)
	{
		PacketHandler.sendToAll(world, new PacketTypesData(write(new CompoundTag())));
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
		if(entity == null || !(entity instanceof LivingEntity))
			return Collections.emptyList();
		return EnumCreatureType.getCreatureTypes((LivingEntity)entity);
	}
	
	public List<EnumCreatureType> getMobTypes(EntityType<?> typeIn)
	{
		return getMobTypes(new ResourceLocation(typeIn.toString()));
	}
	public List<EnumCreatureType> getMobTypes(ResourceLocation registryName)
	{
		if(mobTypeCache.containsKey(registryName))
			return mobTypeCache.get(registryName);

		List<EnumCreatureType> types = new ArrayList<>();
		
		Optional<EntityType<?>> entityType = EntityType.byString(registryName.toString());
		if(entityType.isPresent() && world != null)
		{
			Entity object = entityType.get().create(world);
			if(object != null && object instanceof IDefaultSpecies)
				types.addAll(((IDefaultSpecies)object).defaultCreatureTypes());
		}
		
		if(types.isEmpty())
			for(EnumCreatureType type : typeToMob.keySet())
				for(ResourceLocation entry : typeToMob.getOrDefault(type, Collections.emptyList()))
					if(entry.equals(registryName))
					{
						types.add(type);
						break;
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
