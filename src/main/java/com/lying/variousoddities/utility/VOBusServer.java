package com.lying.variousoddities.utility;

import java.util.List;

import com.lying.variousoddities.api.event.AbilityEvent.AbilityAffectEntityEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.GetEntityTypesEvent;
import com.lying.variousoddities.api.event.FireworkExplosionEvent;
import com.lying.variousoddities.api.event.LivingWakeUpEvent;
import com.lying.variousoddities.api.event.PlayerChangeConditionEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.condition.Conditions;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.EntityBodyCorpse;
import com.lying.variousoddities.entity.EntityBodyUnconscious;
import com.lying.variousoddities.entity.ai.EntityAIFrightened;
import com.lying.variousoddities.entity.hostile.EntityGoblin;
import com.lying.variousoddities.entity.hostile.EntityRatGiant;
import com.lying.variousoddities.entity.passive.EntityGhastling;
import com.lying.variousoddities.entity.passive.EntityRat;
import com.lying.variousoddities.entity.passive.EntityWorg;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesOpenScreen;
import com.lying.variousoddities.network.PacketSyncAir;
import com.lying.variousoddities.network.PacketSyncLivingData;
import com.lying.variousoddities.network.PacketSyncPlayerData;
import com.lying.variousoddities.network.PacketSyncSpecies;
import com.lying.variousoddities.potion.PotionSleep;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilitySize;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VOBusServer
{
	@SubscribeEvent
	public static void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof LivingEntity && !(event.getObject() instanceof AbstractBody))
		{
			LivingData dataLiving = new LivingData();
			dataLiving.getAbilities().markForRecache();
			event.addCapability(LivingData.IDENTIFIER, dataLiving);
			event.addListener(dataLiving.handler()::invalidate);
			
			if(event.getObject().getType() == EntityType.PLAYER)
			{
				PlayerData dataPlayer = new PlayerData();
				event.addCapability(PlayerData.IDENTIFIER, dataPlayer);
				event.addListener(dataPlayer.handler()::invalidate);
			}
		}
	}
	
	@SubscribeEvent
	public static void onChangeDimensionEvent(EntityTravelToDimensionEvent event)
	{
		Entity entity = event.getEntity();
		if(!entity.getLevel().isClientSide && entity.getType() == EntityType.PLAYER)
		{
			Player player = (Player)entity;
			LivingData data = LivingData.forEntity(player);
			if(data != null)
			{
				PacketHandler.sendTo((ServerPlayer)player, new PacketSyncAir(data.getAir()));
				data.getAbilities().markDirty();
			}
		}
		
		if(entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(VOPotions.ANCHORED))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onPlayerLogInEvent(PlayerLoggedInEvent event)
	{
		Player player = event.getEntity();
		PacketHandler.sendTo((ServerPlayer)player, new PacketSyncSpecies(VORegistries.SPECIES));
		
		LivingData livingData = LivingData.forEntity(player);
		if(livingData != null)
		{
			PacketHandler.sendToAll((ServerLevel)player.getLevel(), new PacketSyncLivingData(player.getUUID(), livingData));
			livingData.getAbilities().markDirty();
			
			if(!livingData.hasSelectedSpecies() && ConfigVO.MOBS.createCharacterOnLogin.get())
			{
				if(!player.getLevel().isClientSide)
					PacketHandler.sendTo((ServerPlayer)player, new PacketSpeciesOpenScreen(ConfigVO.MOBS.powerLevel.get(), ConfigVO.MOBS.randomCharacters.get()));
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Reference.Values.TICKS_PER_MINUTE * 15, 15, true, false));
			}
		}
		
		PlayerData playerData = PlayerData.forPlayer(player);
		if(playerData != null)
			PacketHandler.sendToAll((ServerLevel)player.getLevel(), new PacketSyncPlayerData(player.getUUID(), playerData));
	}
	
	@SubscribeEvent
	public static void onPlayerCloneEvent(PlayerEvent.Clone event)
	{
		LivingData oldLiving = LivingData.forEntity(event.getOriginal());
		LivingData newLiving = LivingData.forEntity(event.getEntity());
		if(oldLiving != null && newLiving != null)
		{
			newLiving.setCustomTypes(oldLiving.getCustomTypes());
			newLiving.setSpecies(oldLiving.getSpecies());
			newLiving.setSelectedSpecies(oldLiving.hasSelectedSpecies());
			newLiving.setTemplates(oldLiving.getTemplates());
			newLiving.getAbilities().copy(oldLiving.getAbilities());
			newLiving.getAbilities().markDirty();
		}
		
		PlayerData oldPlayer = PlayerData.forPlayer(event.getOriginal());
		PlayerData newPlayer = PlayerData.forPlayer(event.getEntity());
		if(oldPlayer != null && newPlayer != null)
		{
			if(!event.isWasDeath())
			{
				newPlayer.setBodyCondition(oldPlayer.getBodyCondition());
				newPlayer.setBodyUUID(oldPlayer.getBodyUUID());
			}
			newPlayer.reputation.deserializeNBT(oldPlayer.reputation.serializeNBT(new CompoundTag()));
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawnEvent(PlayerRespawnEvent event)
	{
		LivingData livingData = LivingData.forEntity(event.getEntity());
		if(livingData != null)
			livingData.getAbilities().markDirty();
		
		if(ConfigVO.MOBS.newCharacterOnDeath.get())
			livingData.setSelectedSpecies(false);
		
		if(AbilityRegistry.hasAbility(event.getEntity(), AbilitySize.REGISTRY_NAME))
			event.getEntity().recalculateSize();
		
		if(PlayerData.isPlayerBodyDead(event.getEntity()))
		{
			PlayerData playerData = PlayerData.forPlayer(event.getEntity());
			playerData.setBodyCondition(BodyCondition.ALIVE);
			playerData.setSoulCondition(SoulCondition.ALIVE);
		}
	}
	
	@SubscribeEvent
	public static void addEntityBehaviours(EntityJoinLevelEvent event)
	{
		Entity theEntity = event.getEntity();
		if(theEntity instanceof LivingEntity && !theEntity.getLevel().isClientSide)
		{
			LivingData data = LivingData.forEntity((LivingEntity)theEntity);
			if(data != null && !theEntity.getLevel().isClientSide)
				PacketHandler.sendToAll((ServerLevel)theEntity.getLevel(), new PacketSyncLivingData(theEntity.getUUID(), data));
		}
		
		if(theEntity.getType() == EntityType.CAT || theEntity.getType() == EntityType.OCELOT)
		{
			Monster feline = (Monster)theEntity;
			
			if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.RAT))
				feline.targetSelector.addGoal(1, new NearestAttackableTargetGoal<EntityRat>(feline, EntityRat.class, true));
			
			if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.RAT_GIANT))
				feline.targetSelector.addGoal(1, new NearestAttackableTargetGoal<EntityRatGiant>(feline, EntityRatGiant.class, true));
		}
		
		// Add special AI to mobs
		if(theEntity instanceof PathfinderMob)
		{
			PathfinderMob living = (PathfinderMob)theEntity;
			living.goalSelector.addGoal(1, new EntityAIFrightened(living));
		}
		
		// Spook worgs
		if(event.getEntity().getType() == EntityType.LIGHTNING_BOLT)
		{
			BlockPos pos = event.getEntity().blockPosition();
			AABB bounds = new AABB(0, 0, 0, 1, 256, 1).move(pos.getX(), 0, pos.getZ()).inflate(128, 0, 128);
			for(EntityWorg worg : event.getEntity().getLevel().getEntitiesOfClass(EntityWorg.class, bounds))
				worg.spook();
		}
	}
	
	@SubscribeEvent
	public static void onDeathNearGoblinEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntity();
		DamageSource cause = event.getSource();
		Level world = victim.getLevel();
		
		// Reduce refractory period of nearby goblins when a. goblin is slain or b. goblin slays any mob (esp. players)
		if(victim.getType() == VOEntities.GOBLIN)
			reduceRefractory(victim, 1000);
		else if(cause instanceof EntityDamageSource && ((EntityDamageSource)cause).getEntity() instanceof EntityGoblin)
			reduceRefractory(victim, victim instanceof Player ? 4000 : 500);
		
		// Occasionally spawn ghastlings when a ghast dies to a reflected fireball
		if(victim.getType() == EntityType.GHAST)
			if(cause.getDirectEntity() instanceof Fireball && cause.getEntity() instanceof Player)
			{
				RandomSource rand = victim.getRandom();
				if(rand.nextInt(15) == 0)
					for(int i=0; i<rand.nextInt(3); i++)
					{
						EntityGhastling ghastling = VOEntities.GHASTLING.create(world);
						ghastling.setPos(victim.getX(), victim.getY(), victim.getZ());
						ghastling.setYRot(rand.nextFloat() * 360F);
						ghastling.setXRot(0F);
						world.addFreshEntity(ghastling);
					}
			}
		
		// Heal worgs and wargs when they kill something
		if(cause instanceof EntityDamageSource && ((EntityDamageSource)cause).getEntity() instanceof AbstractGoblinWolf)
			((AbstractGoblinWolf)cause.getEntity()).heal(2F + victim.getRandom().nextFloat() * 3F);
		
	}
	
	@SubscribeEvent(priority=EventPriority.LOW)
	public static void unconsciousDeathEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntity();
		EntityBodyUnconscious body = EntityBodyUnconscious.getBodyFromEntity(victim);
		if(body != null)
			victim.copyPosition(body);
	}
	
	/** Spawn a corpse when a Needled creature dies */
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void corpseSpawnEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntity();
		Level world = victim.getLevel();
		if(event.getSource() == DamageSource.OUT_OF_WORLD || event.isCanceled())
			return;
		
		if(!(victim instanceof Monster || victim instanceof Player))
			return;
		
		boolean spawnCorpse = false;
		switch(ConfigVO.GENERAL.corpseSpawnRule())
		{
			case PLAYERS_ONLY:
				spawnCorpse = victim.getType() == EntityType.PLAYER;
				break;
			case NEEDLED_ONLY:
				spawnCorpse = victim.hasEffect(VOPotions.NEEDLED);
				break;
			case PLAYERS_AND_NEEDLED:
				spawnCorpse = victim.getType() == EntityType.PLAYER || victim.hasEffect(VOPotions.NEEDLED);
				break;
			case ALWAYS:
				spawnCorpse = true;
				break;
			default:
				spawnCorpse = false;
				break;
		}
		
		if(spawnCorpse)
		{
			AbstractBody.clearNearbyAttackTargetsOf(victim);
			victim.removeEffect(VOPotions.NEEDLED);
			EntityBodyCorpse corpse = EntityBodyCorpse.createCorpseFrom(victim);
			
			if(victim.getType() == EntityType.PLAYER)
			{
				PlayerData playerData = PlayerData.forPlayer((Player)victim);
				
				// If player is already dead, let them die as normal
				if(PlayerData.isPlayerBodyDead((Player)victim))
					return;
				// Otherwise, cancel the event and set them to be dead
				else if(playerData.setConditionIsDead(corpse.getUUID()))
				{
					event.setCanceled(true);
					world.players().forEach((player) -> { player.sendSystemMessage(event.getSource().getLocalizedDeathMessage(victim)); });
					return;
				}
			}
			else if(corpse != null && !world.isClientSide)
			{
				corpse.setPocketInventory(LivingData.forEntity(victim).getPocketInventory());
				world.addFreshEntity(corpse);
			}
		}
		else if(!world.isClientSide)
		{
			LivingData livingData = LivingData.forEntity(victim);
			for(ItemStack stack : livingData.getPocketInventory())
				if(!stack.isEmpty())
					victim.spawnAtLocation(stack, victim.getRandom().nextFloat());
		}
	}
	
	/**
	 * Spawns an appropriate body (if any) in response to a player's change in condition.
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onPlayerConditionChange(PlayerChangeConditionEvent event)
	{
		if(event.bodyChange())
		{
			Player player = event.getEntity();
			PlayerData data = PlayerData.forPlayer(player);
			Level world = player.getLevel();
			
			if(!(event.getNewBody() == BodyCondition.ALIVE && event.getNewSoul() == SoulCondition.ALIVE))
				AbstractBody.clearNearbyAttackTargetsOf(player);
			
			switch(event.getNewBody())
			{
				case DEAD:
					player.removeEffect(VOPotions.NEEDLED);
					EntityBodyCorpse corpse = EntityBodyCorpse.createCorpseFrom(player);
					data.setBodyUUID(corpse.getUUID());
					player.setHealth(player.getMaxHealth());
					if(!world.isClientSide)
						world.addFreshEntity(corpse);
					break;
				case UNCONSCIOUS:
					LivingEntity body = EntityBodyUnconscious.createBodyFrom(player);
					data.setBodyUUID(body.getUUID());
					if(!world.isClientSide)
						world.addFreshEntity(body);
					break;
				case ALIVE:
				default:
					break;
			}
		}
	}
	
	@SubscribeEvent
	public static void onFireworkBlastEvent(FireworkExplosionEvent event)
	{
		ListTag explosions = event.fireworkData().getList("Explosions", 10);
		if(!explosions.isEmpty())
		{
			AABB bounds = new AABB(0, 0, 0, 1, 1, 1).move(event.position()).inflate(16 * explosions.size());
			for(EntityWorg worg : event.world().getEntitiesOfClass(EntityWorg.class, bounds))
				worg.spook();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void applyNativeExtraplanar(GetEntityTypesEvent event)
	{
		LivingEntity entity = event.getEntity();
		LivingData data = LivingData.forEntity(entity);
		if(data == null)
			return;
		
		if(data.getHomeDimension() != null)
		{
			List<EnumCreatureType> types = event.getTypes();
			if(types.contains(EnumCreatureType.EXTRAPLANAR) || types.contains(EnumCreatureType.NATIVE))
				return;
			
			ResourceLocation currentDim = new ResourceLocation(entity.getLevel().dimensionType().toString());
			if(currentDim.equals(data.getHomeDimension()))
			{
				if(!types.contains(EnumCreatureType.EXTRAPLANAR) && EnumCreatureType.NATIVE.canApplyTo(types))
					event.getTypes().add(EnumCreatureType.NATIVE);
			}
			else
			{
				if(!types.contains(EnumCreatureType.NATIVE) && EnumCreatureType.EXTRAPLANAR.canApplyTo(types))
					event.getTypes().add(EnumCreatureType.EXTRAPLANAR);
			}
		}
	}
	
	/**
	 * If a sleeping mob is harmed, wake them up.
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onSleepingHurtEvent(LivingHurtEvent event)
	{
		if(event.getAmount() > 0F && !event.isCanceled())
		{
			LivingEntity hurtEntity = event.getEntity();
			if(VOPotions.isSilenced(hurtEntity))
				return;
			
			wakeupEntitiesAround(hurtEntity);
			
			MobEffectInstance sleepEffect = hurtEntity.getEffect(VOPotions.SLEEP);
			int tier = (sleepEffect == null || sleepEffect.getDuration() <= 0) ? -1 : sleepEffect.getAmplifier();
			
			if(PotionSleep.isSleeping(hurtEntity) && tier < 1)
				if(!MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(hurtEntity, true)))
					hurtEntity.removeEffect(VOPotions.SLEEP);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onCharmedHurtEvent(LivingHurtEvent event)
	{
		if(event.getAmount() > 0F && !event.isCanceled())
		{
			DamageSource source = event.getSource();
			
			LivingData data = LivingData.forEntity(event.getEntity());
			if(data == null)
				return;
			
			Entity immediate = source.getDirectEntity();
			Entity distant = source.getEntity();
			if(immediate != null && immediate instanceof LivingEntity && data.hasCondition(Conditions.CHARMED, (LivingEntity)immediate))
				data.clearCondition((LivingEntity)immediate, Conditions.CHARMED);
			if(distant != null && distant instanceof LivingEntity && data.hasCondition(Conditions.CHARMED, (LivingEntity)distant))
				data.clearCondition((LivingEntity)distant, Conditions.CHARMED);
		}
	}
	
	/**
	 * Prevents bludgeoning damage from affecting the entity's health instead of their bludgeoning value.
	 * @param event
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBludgeonedEvent(LivingHurtEvent event)
	{
		if(!event.isCanceled() && event.getSource() == VODamageSource.BLUDGEON)
		{
			LivingEntity hurtEntity = event.getEntity();
			LivingData data = LivingData.forEntity(hurtEntity);
			
			if(data != null)
				data.addBludgeoning(event.getAmount());
			
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onAnchoredTeleport(EntityTeleportEvent.EnderEntity event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(entity.hasEffect(VOPotions.ANCHORED))
			event.setCanceled(true);
	}
	
	/**
	 * Prevents players not currently in their physical bodies from being affected by abilities.<br>
	 * Does not prevent their physical bodies from being affected.
	 */
	@SubscribeEvent
	public static void onAbilityAffectPlayer(AbilityAffectEntityEvent event)
	{
		if(event.getEntity() != null && event.getEntity().getType() == EntityType.PLAYER)
			if(!PlayerData.isPlayerNormalFunction((Player)event.getEntity()))
				event.setCanceled(true);
	}
	
	public static void wakeupEntitiesAround(Entity source, double rangeXZ, double rangeY)
	{
		for(LivingEntity entity : source.getLevel().getEntitiesOfClass(LivingEntity.class, source.getBoundingBox().inflate(rangeXZ, rangeY, rangeXZ)))
		{
			if(entity == source || !PotionSleep.isSleeping(entity) || PotionSleep.hasSleepEffect(entity))
				continue;
			
			if(entity instanceof LivingEntity)
			{
				double wakeupChance = (RandomSource.create(entity.getUUID().getLeastSignificantBits())).nextDouble();
				if(entity.getRandom().nextDouble() < wakeupChance && !MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(entity, true)))
					entity.removeEffect(VOPotions.SLEEP);
			}
			else if(entity instanceof Player)
			{
				Player player = (Player)entity;
				if(!MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(entity, true)))
					player.stopSleeping();
			}
		}
	}
	
	public static void wakeupEntitiesAround(Entity source)
	{
		wakeupEntitiesAround(source, 6D, 2D);
	}
	
	private static void reduceRefractory(LivingEntity goblinIn, int amount)
	{
		List<EntityGoblin> nearbyGoblins = goblinIn.getLevel().getEntitiesOfClass(EntityGoblin.class, goblinIn.getBoundingBox().inflate(10));
		for(EntityGoblin goblin : nearbyGoblins)
			if(goblin.isAlive() && goblin.getAge() > 0)
				goblin.setAge(Math.max(0, goblin.getAge() - amount));
	}
}
