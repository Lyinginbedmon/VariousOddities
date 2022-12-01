package com.lying.variousoddities.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.entity.EntityBodyCorpse;
import com.lying.variousoddities.entity.EntityBodyUnconscious;
import com.lying.variousoddities.entity.EntityDummyBiped;
import com.lying.variousoddities.entity.EntitySpell;
import com.lying.variousoddities.entity.hostile.EntityCrabGiant;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.hostile.EntityMindFlayer;
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
import com.lying.variousoddities.entity.projectile.EntityFireballGhastling;
import com.lying.variousoddities.entity.wip.EntityChangeling;
import com.lying.variousoddities.entity.wip.EntityPatronKirin;
import com.lying.variousoddities.entity.wip.EntityPatronWitch;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class VOEntities
{
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Reference.ModInfo.MOD_ID);
	
    public static final List<EntityType<?>> ENTITIES_AI = Lists.newArrayList();
    public static final Map<EntityType<?>, Item> SPAWN_EGGS = new HashMap<>();
	
    public static final RegistryObject<EntityType<EntitySpell>> SPELL			= register("spell", EntityType.Builder.<EntitySpell>of(EntitySpell::new, MobCategory.MISC).sized(0.5F, 0.5F));//SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING
    public static final RegistryObject<EntityType<EntityBodyCorpse>> CORPSE		= register("corpse", EntityType.Builder.<EntityBodyCorpse>of(EntityBodyCorpse::new, MobCategory.MISC).sized(0.75F, 0.5F));//SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING
    public static final RegistryObject<EntityType<EntityBodyUnconscious>> BODY	= register("body", EntityType.Builder.<EntityBodyUnconscious>of(EntityBodyUnconscious::new, MobCategory.MISC).sized(0.75F, 0.5F));//SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING
    
    // First release
	public static final RegistryObject<EntityType<EntityKobold>> KOBOLD						= register("kobold",			EntityType.Builder.<EntityKobold>of(EntityKobold::new, MobCategory.CREATURE).sized(0.6F, 1.6F).clientTrackingRange(10), 16167425, 15826224);
	public static final RegistryObject<EntityType<EntityGoblin>> GOBLIN						= register("goblin",			EntityType.Builder.<EntityGoblin>of(EntityGoblin::new, MobCategory.CREATURE).sized(0.6F, 1.6F).clientTrackingRange(10), 5349438, 8306542);
	public static final RegistryObject<EntityType<EntityRat>> RAT							= register("rat",				EntityType.Builder.<EntityRat>of(EntityRat::new, MobCategory.CREATURE).sized(0.3F, 0.2F).clientTrackingRange(10), 6043662, 3679244);
	public static final RegistryObject<EntityType<EntityRatGiant>> RAT_GIANT				= register("giant_rat",			EntityType.Builder.<EntityRatGiant>of(EntityRatGiant::new, MobCategory.MONSTER).sized(0.9F, 0.5F).clientTrackingRange(10), 6043662, 3679244);
	public static final RegistryObject<EntityType<EntityScorpion>> SCORPION					= register("scorpion",			EntityType.Builder.<EntityScorpion>of(EntityScorpion::new, MobCategory.CREATURE).sized(0.8F, 0.45F).clientTrackingRange(10), 14704695, 14696759);
	public static final RegistryObject<EntityType<EntityScorpionGiant>> SCORPION_GIANT		= register("giant_scorpion",	EntityType.Builder.<EntityScorpionGiant>of(EntityScorpionGiant::new, MobCategory.MONSTER).sized(1.8F, 1.85F).clientTrackingRange(10), 14704695, 6366997);
	
	// Second release
	public static final RegistryObject<EntityType<EntityCrab>> CRAB						= register("crab",				EntityType.Builder.<EntityCrab>of(EntityCrab::new, MobCategory.WATER_CREATURE).sized(0.6F, 0.5F).clientTrackingRange(10), 10489616, 16775294);//SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.OCEAN_FLOOR
	public static final RegistryObject<EntityType<EntityCrabGiant>> CRAB_GIANT			= register("giant_crab",		EntityType.Builder.<EntityCrabGiant>of(EntityCrabGiant::new, MobCategory.MONSTER).sized(1.9F, 1.5F).clientTrackingRange(10), 10489616, 16775294);//SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.OCEAN_FLOOR
	public static final RegistryObject<EntityType<EntityWorg>> WORG						= register("worg",				EntityType.Builder.<EntityWorg>of(EntityWorg::new, MobCategory.CREATURE).sized(0.7F, 1.0F).clientTrackingRange(10), 14670297, 3749941);
	public static final RegistryObject<EntityType<EntityWarg>> WARG						= register("warg",				EntityType.Builder.<EntityWarg>of(EntityWarg::new, MobCategory.CREATURE).sized(0.85F, 1.35F).clientTrackingRange(10), 6898719, 1248261);
	public static final RegistryObject<EntityType<EntityGhastling>> GHASTLING			= register("ghastling",			EntityType.Builder.<EntityGhastling>of(EntityGhastling::new, MobCategory.CREATURE).sized(0.95F, 0.95F).clientTrackingRange(10).fireImmune(), 16382457, 12369084);
	
	// WIP mobs to be fleshed out at a later date
	public static final RegistryObject<EntityType<EntityPatronKirin>> PATRON_KIRIN			= register("patron_kirin",		EntityType.Builder.<EntityPatronKirin>of(EntityPatronKirin::new, MobCategory.CREATURE).sized(0.6F, 1.999F).clientTrackingRange(10), -1, 1);
	public static final RegistryObject<EntityType<EntityPatronWitch>> PATRON_WITCH			= register("patron_witch",		EntityType.Builder.<EntityPatronWitch>of(EntityPatronWitch::new, MobCategory.CREATURE).sized(0.6F, 1.8F).clientTrackingRange(10), -1, 1);
	public static final RegistryObject<EntityType<EntityChangeling>> CHANGELING				= register("changeling",		EntityType.Builder.<EntityChangeling>of(EntityChangeling::new, MobCategory.CREATURE).sized(0.6F, 1.8F).clientTrackingRange(10), -1, 1);
	public static final RegistryObject<EntityType<EntityMindFlayer>> MIND_FLAYER			= register("mind_flayer",		EntityType.Builder.<EntityMindFlayer>of(EntityMindFlayer::new, MobCategory.MONSTER).sized(0.6F, 1.8F).clientTrackingRange(10), -1, -1);
	
	public static final RegistryObject<EntityType<EntityMarimo>> MARIMO	= register("marimo", EntityType.Builder.<EntityMarimo>of(EntityMarimo::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(8));//SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
	
	// Utility entities
	public static final RegistryObject<EntityType<EntityFireballGhastling>> GHASTLING_FIREBALL	= register("ghastling_fireball", EntityType.Builder.<EntityFireballGhastling>of(EntityFireballGhastling::new, MobCategory.MISC).sized(0.3125F, 0.3125F).noSummon().fireImmune().clientTrackingRange(4));
	public static final RegistryObject<EntityType<EntityDummyBiped>> DUMMY_BIPED				= register("dummy_biped", EntityType.Builder.<EntityDummyBiped>of(EntityDummyBiped::new, MobCategory.MISC).sized(0.6F, 1.8F).noSummon().clientTrackingRange(4));
    
	private static <T extends Mob> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder, int primaryColor, int secondaryColor)
	{
		return register(name, builder);
	}
	
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder)
	{
		return ENTITIES.register(name, () -> builder.build(Reference.ModInfo.MOD_PREFIX + name));
	}
	
	public static void init() {}
    
    public static List<ResourceLocation> getEntityNameList()
    {
    	List<ResourceLocation> names = new ArrayList<>();
    	for(RegistryObject<EntityType<?>> type : ENTITIES.getEntries())
    		if(type.isPresent())
    			names.add(EntityType.getKey(type.get()));
    	return names;
    }
    
    public static List<ResourceLocation> getEntityAINameList()
    {
    	List<ResourceLocation> names = new ArrayList<>();
    	for(EntityType<?> type : ENTITIES_AI)
    		names.add(EntityType.getKey(type));
    	return names;
    }
    
    public static EntityType<?> getEntityTypeByName(String nameIn)
    {
    	for(RegistryObject<EntityType<?>> type : ENTITIES.getEntries())
    		if(EntityType.getKey(type.get()).getPath().equalsIgnoreCase(nameIn))
    			return type.get();
    	return null;
    }
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event)
    {
    	event.put(DUMMY_BIPED.get(), EntityDummyBiped.createAttributes().build());
    	event.put(CORPSE.get(), EntityBodyCorpse.createAttributes().build());
    	event.put(BODY.get(), EntityBodyCorpse.createAttributes().build());
    	event.put(KOBOLD.get(), EntityKobold.createAttributes().build());
    	event.put(GOBLIN.get(), EntityGoblin.createAttributes().build());
    	event.put(MARIMO.get(), EntityMarimo.createAttributes().build());
    	event.put(RAT.get(), EntityRat.createAttributes().build());
    	event.put(RAT_GIANT.get(), EntityRatGiant.createAttributes().build());
    	event.put(SCORPION.get(), EntityRat.createAttributes().build());
    	event.put(SCORPION_GIANT.get(), EntityRatGiant.createAttributes().build());
    	event.put(CRAB.get(), EntityCrab.createAttributes().build());
    	event.put(CRAB_GIANT.get(), EntityCrabGiant.createAttributes().build());
    	event.put(WORG.get(), EntityWorg.createAttributes().build());
    	event.put(WARG.get(), EntityWarg.createAttributes().build());
    	event.put(GHASTLING.get(), EntityGhastling.createAttributes().build());
    	
    	event.put(PATRON_KIRIN.get(), EntityPatronKirin.createAttributes().build());
    	event.put(PATRON_WITCH.get(), EntityPatronWitch.createAttributes().build());
    	event.put(CHANGELING.get(), EntityChangeling.createAttributes().build());
    	event.put(MIND_FLAYER.get(), Mob.createMobAttributes().build());
    }
}
