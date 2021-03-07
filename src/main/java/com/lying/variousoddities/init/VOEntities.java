package com.lying.variousoddities.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.lying.variousoddities.entity.EntitySpell;
import com.lying.variousoddities.entity.hostile.EntityCrabGiant;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.hostile.EntityScorpionGiant;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.entity.passive.EntityCrab;
import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityKobold;
import com.lying.variousoddities.entity.passive.EntityMarimo;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.entity.passive.EntityScorpion;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.item.ItemOddEgg;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntitySpawnPlacementRegistry.IPlacementPredicate;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOEntities
{
	private static final Map<EntityType<?>, EntityRegistry> TYPE_PROPERTIES_MAP = new HashMap<>();
	
    public static final List<EntityType<?>> ENTITIES = Lists.newArrayList();
    public static final List<EntityType<?>> ENTITIES_AI = Lists.newArrayList();
    public static final Map<EntityType<?>, Item> SPAWN_EGGS = new HashMap<>();
    
//    public static final ITag.INamedTag<EntityType<?>> CRABS = EntityTypeTags.getTagById(Reference.ModInfo.MOD_PREFIX+"crabs");
    public static final ITag.INamedTag<EntityType<?>> RATS = EntityTypeTags.getTagById(Reference.ModInfo.MOD_PREFIX+"rats");
    public static final ITag.INamedTag<EntityType<?>> SCORPIONS = EntityTypeTags.getTagById(Reference.ModInfo.MOD_PREFIX+"scorpions");
	
    public static final EntityType<EntitySpell> SPELL	= register("spell", EntitySpell::new, EntityClassification.MISC, 0.5F, 0.5F, PlacementType.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING, EntitySpell::canSpawnAt);
    
    // First release
	public static final EntityType<EntityKobold> KOBOLD						= register("kobold",			EntityKobold::new, EntityClassification.CREATURE, 0.6F, 1.6F, EntityKobold::canSpawnAt, 16167425, 15826224);
	public static final EntityType<EntityGoblin> GOBLIN						= register("goblin",			EntityGoblin::new, EntityClassification.MONSTER, 0.6F, 1.6F, EntityGoblin::canSpawnAt, 5349438, 8306542);
	public static final EntityType<EntityRat> RAT							= register("rat",				EntityRat::new, EntityClassification.CREATURE, 0.3F, 0.2F, EntityRat::canSpawnAt, 6043662, 3679244);
	public static final EntityType<EntityRatGiant> RAT_GIANT				= register("giant_rat",			EntityRatGiant::new, EntityClassification.MONSTER, 0.9F, 0.5F, EntityRatGiant::canSpawnAt, 6043662, 3679244);
	public static final EntityType<EntityScorpion> SCORPION					= register("scorpion",			EntityScorpion::new, EntityClassification.CREATURE, 0.8F, 0.45F, EntityScorpion::canSpawnAt, 14704695, 14696759);
	public static final EntityType<EntityScorpionGiant> SCORPION_GIANT		= register("giant_scorpion",	EntityScorpionGiant::new, EntityClassification.MONSTER, 1.8F, 1.85F, EntityScorpionGiant::canSpawnAt, 14704695, 6366997);
	
	// Second release
	public static final EntityType<EntityCrab> CRAB							= register("crab",				EntityCrab::new, EntityClassification.WATER_CREATURE, 0.6F, 0.5F, PlacementType.NO_RESTRICTIONS, Heightmap.Type.OCEAN_FLOOR, EntityCrab::canSpawnAt, 10489616, 16775294);
	public static final EntityType<EntityCrabGiant> CRAB_GIANT				= register("giant_crab",		EntityCrabGiant::new, EntityClassification.MONSTER, 1.9F, 1.5F, PlacementType.NO_RESTRICTIONS, Heightmap.Type.OCEAN_FLOOR, EntityCrabGiant::canSpawnAt, 10489616, 16775294);
	public static final EntityType<EntityWorg> WORG							= register("worg",				EntityWorg::new, EntityClassification.CREATURE, 0.7F, 1.0F, EntityWorg::canSpawnAt, 14670297, 3749941);
	public static final EntityType<EntityWarg> WARG							= register("warg",				EntityWarg::new, EntityClassification.CREATURE, 0.85F, 1.35F, EntityWarg::canSpawnAt, 6898719, 1248261);
	public static final EntityType<EntityGhastling> GHASTLING				= register("ghastling",			EntityGhastling::new, EntityClassification.CREATURE, 0.95F, 0.95F, EntityGhastling::canSpawnAt, 0, 0);
	
	public static final EntityType<EntityMarimo> MARIMO	= register("marimo", EntityMarimo::new, EntityClassification.MISC, 0.5F, 0.5F, PlacementType.IN_WATER, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EntityMarimo::canSpawnAt);
    
    private static <T extends Entity> EntityType<T> register(String name, EntityType.IFactory<T> factory, EntityClassification type, float width, float height, PlacementType placeType1, Heightmap.Type placeType2, IPlacementPredicate<T> predicate)
    {
        ResourceLocation location = new ResourceLocation(Reference.ModInfo.MOD_ID, name);
        EntityType<T> entity = EntityType.Builder.create(factory, type).size(width, height).setTrackingRange(64).setUpdateInterval(1).build(location.toString());
        entity.setRegistryName(location);
        ENTITIES.add(entity);
        
        TYPE_PROPERTIES_MAP.put(entity, new EntityRegistry(placeType1, placeType2, predicate));
        
        return entity;
    }
    
    private static <T extends Entity> EntityType<T> register(String name, EntityType.IFactory<T> factory, EntityClassification type, float width, float height, IPlacementPredicate<T> predicate, int eggPrimary, int eggSecondary)
    {
    	return register(name, factory, type, width, height, PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, predicate, eggPrimary, eggSecondary);
    }
    
    private static <T extends Entity> EntityType<T> register(String name, EntityType.IFactory<T> factory, EntityClassification type, float width, float height, PlacementType placeType1, Heightmap.Type placeType2, IPlacementPredicate<T> predicate, int eggPrimary, int eggSecondary)
    {
    	EntityType<T> entity = register(name, factory, type, width, height, placeType1, placeType2, predicate);
        Item spawnEgg = new ItemOddEgg(entity, eggPrimary, eggSecondary, (new Item.Properties()));
        spawnEgg.setRegistryName(new ResourceLocation(Reference.ModInfo.MOD_ID, name + "_spawn_egg"));
        SPAWN_EGGS.put(entity, spawnEgg);
        ENTITIES_AI.add(entity);
        return entity;
    }
    
    public static List<ResourceLocation> getEntityNameList()
    {
    	List<ResourceLocation> names = new ArrayList<>();
    	for(EntityType<?> type : ENTITIES)
    		names.add(type.getRegistryName());
    	return names;
    }
    
    public static List<ResourceLocation> getEntityAINameList()
    {
    	List<ResourceLocation> names = new ArrayList<>();
    	for(EntityType<?> type : ENTITIES_AI)
    		names.add(type.getRegistryName());
    	return names;
    }
    
    public static EntityType<?> getEntityTypeByName(String nameIn)
    {
    	for(EntityType<?> type : ENTITIES)
    		if(type.getRegistryName().getPath().equalsIgnoreCase(nameIn))
    			return type;
    	return null;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event)
    {
    	for(EntityType entity : TYPE_PROPERTIES_MAP.keySet())
    	{
            Preconditions.checkNotNull(entity.getRegistryName(), "registryName");
            event.getRegistry().register(entity);
            
            EntityRegistry registry = TYPE_PROPERTIES_MAP.get(entity);
            EntitySpawnPlacementRegistry.register(entity, registry.placementType, registry.heightType, registry.placementPredicate);
    	}
    }
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event)
    {
    	event.put(KOBOLD, EntityKobold.getAttributes().create());
    	event.put(GOBLIN, EntityGoblin.getAttributes().create());
    	event.put(MARIMO, EntityMarimo.getAttributes().create());
    	event.put(RAT, EntityRat.getAttributes().create());
    	event.put(RAT_GIANT, EntityRatGiant.getAttributes().create());
    	event.put(SCORPION, EntityRat.getAttributes().create());
    	event.put(SCORPION_GIANT, EntityRatGiant.getAttributes().create());
    	event.put(CRAB, EntityCrab.getAttributes().create());
    	event.put(CRAB_GIANT, EntityCrabGiant.getAttributes().create());
    	event.put(WORG, EntityWorg.getAttributes().create());
    	event.put(WARG, EntityWarg.getAttributes().create());
    	event.put(GHASTLING, EntityGhastling.getAttributes().create());
    }
    
    @SubscribeEvent
    public static void registerSpawnEggs(RegistryEvent.Register<Item> event)
    {
        for (Item spawnEgg : SPAWN_EGGS.values())
        {
            Preconditions.checkNotNull(spawnEgg.getRegistryName(), "registryName");
            event.getRegistry().register(spawnEgg);
        }
    }
    
    private static class EntityRegistry
    {
    	@SuppressWarnings("rawtypes")
		IPlacementPredicate placementPredicate;
    	PlacementType placementType;
    	Heightmap.Type heightType;
    	
    	@SuppressWarnings("rawtypes")
		public EntityRegistry(PlacementType placeType1, Heightmap.Type placeType2, IPlacementPredicate predicate)
    	{
    		this.placementType = placeType1;
    		this.heightType = placeType2;
    		this.placementPredicate = predicate;
    	}
    }
}
