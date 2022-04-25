package com.lying.variousoddities.utility;

import java.util.List;
import java.util.Random;

import com.lying.variousoddities.api.event.CreatureTypeEvent.GetEntityTypesEvent;
import com.lying.variousoddities.api.event.FireworkExplosionEvent;
import com.lying.variousoddities.api.event.LivingWakeUpEvent;
import com.lying.variousoddities.api.event.PlayerChangeConditionEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.LivingData.MindControl;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.EntityBodyCorpse;
import com.lying.variousoddities.entity.EntityBodyUnconscious;
import com.lying.variousoddities.entity.ai.EntityAIFrightened;
import com.lying.variousoddities.entity.ai.EntityAISleep;
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
import com.lying.variousoddities.network.PacketSyncBludgeoning;
import com.lying.variousoddities.network.PacketSyncLivingData;
import com.lying.variousoddities.network.PacketSyncSpecies;
import com.lying.variousoddities.potion.PotionSleep;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilitySize;
import com.lying.variousoddities.species.types.EnumCreatureType;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
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
		if(event.getObject() instanceof LivingEntity)
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
		if(!entity.getEntityWorld().isRemote && entity.getType() == EntityType.PLAYER)
		{
			PlayerEntity player = (PlayerEntity)entity;
			LivingData data = LivingData.forEntity(player);
			if(data != null)
			{
				PacketHandler.sendTo((ServerPlayerEntity)player, new PacketSyncAir(data.getAir()));
				data.getAbilities().markDirty();
			}
		}
		
		if(entity instanceof LivingEntity && ((LivingEntity)entity).isPotionActive(VOPotions.ANCHORED))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onPlayerLogInEvent(PlayerLoggedInEvent event)
	{
		PlayerEntity player = event.getPlayer();
		PacketHandler.sendTo((ServerPlayerEntity)player, new PacketSyncSpecies(VORegistries.SPECIES));
		
		LivingData data = LivingData.forEntity(player);
		if(data != null)
		{
			PacketHandler.sendToAll((ServerWorld)player.getEntityWorld(), new PacketSyncLivingData(player.getUniqueID(), data));
			data.getAbilities().markDirty();
			
			if(!data.hasSelectedSpecies() && ConfigVO.MOBS.selectSpeciesOnLogin.get())
			{
				if(!player.getEntityWorld().isRemote)
					PacketHandler.sendTo((ServerPlayerEntity)player, new PacketSpeciesOpenScreen());
				player.addPotionEffect(new EffectInstance(Effects.RESISTANCE, Reference.Values.TICKS_PER_MINUTE * 15, 15, true, false));
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerCloneEvent(PlayerEvent.Clone event)
	{
		LivingData oldLiving = LivingData.forEntity(event.getOriginal());
		LivingData newLiving = LivingData.forEntity(event.getPlayer());
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
		PlayerData newPlayer = PlayerData.forPlayer(event.getPlayer());
		if(oldPlayer != null && newPlayer != null)
		{
			if(!event.isWasDeath())
			{
				newPlayer.setBodyCondition(oldPlayer.getBodyCondition());
				newPlayer.setBodyUUID(oldPlayer.getBodyUUID());
			}
			newPlayer.reputation.deserializeNBT(oldPlayer.reputation.serializeNBT(new CompoundNBT()));
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawnEvent(PlayerRespawnEvent event)
	{
		LivingData livingData = LivingData.forEntity(event.getPlayer());
		if(livingData != null)
			livingData.getAbilities().markDirty();
		
		if(AbilityRegistry.hasAbility(event.getPlayer(), AbilitySize.REGISTRY_NAME))
			event.getPlayer().recalculateSize();
		
		if(PlayerData.isPlayerBodyDead(event.getPlayer()))
		{
			PlayerData playerData = PlayerData.forPlayer(event.getPlayer());
			playerData.setBodyCondition(BodyCondition.ALIVE);
			playerData.setSoulCondition(SoulCondition.ALIVE);
		}
	}
	
	@SubscribeEvent
	public static void addEntityBehaviours(EntityJoinWorldEvent event)
	{
		Entity theEntity = event.getEntity();
		if(theEntity instanceof LivingEntity && !theEntity.getEntityWorld().isRemote)
		{
			LivingData data = LivingData.forEntity((LivingEntity)theEntity);
			if(data != null && !theEntity.getEntityWorld().isRemote)
				PacketHandler.sendToAll((ServerWorld)theEntity.getEntityWorld(), new PacketSyncLivingData(theEntity.getUniqueID(), data));
		}
		
		if(theEntity.getType() == EntityType.CAT || theEntity.getType() == EntityType.OCELOT)
		{
			MobEntity feline = (MobEntity)theEntity;
			
			if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.RAT))
				feline.targetSelector.addGoal(1, new NearestAttackableTargetGoal<EntityRat>(feline, EntityRat.class, true));
			
			if(ConfigVO.MOBS.aiSettings.isOddityAIEnabled(VOEntities.RAT_GIANT))
				feline.targetSelector.addGoal(1, new NearestAttackableTargetGoal<EntityRatGiant>(feline, EntityRatGiant.class, true));
		}
		
		// Add sleep AI to mobs
		if(theEntity instanceof MobEntity)
		{
			MobEntity living = (MobEntity)theEntity;
			living.goalSelector.addGoal(1, new EntityAISleep(living));
			
			if(living instanceof CreatureEntity)
				living.goalSelector.addGoal(1, new EntityAIFrightened((CreatureEntity)living));
		}
		
		// Spook worgs
		if(event.getEntity().getType() == EntityType.LIGHTNING_BOLT)
		{
			BlockPos pos = event.getEntity().getPosition();
			AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 256, 1).offset(pos.getX(), 0, pos.getZ()).grow(128, 0, 128);
			for(EntityWorg worg : event.getEntity().getEntityWorld().getEntitiesWithinAABB(EntityWorg.class, bounds))
				worg.spook();
		}
	}
	
	@SubscribeEvent
	public static void onDeathNearGoblinEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntityLiving();
		DamageSource cause = event.getSource();
		World world = victim.getEntityWorld();
		
		// Reduce refractory period of nearby goblins when a. goblin is slain or b. goblin slays any mob (esp. players)
		if(victim instanceof EntityGoblin)
			reduceRefractory(victim, 1000);
		else if(cause instanceof EntityDamageSource && ((EntityDamageSource)cause).getTrueSource() instanceof EntityGoblin)
			reduceRefractory(victim, victim instanceof PlayerEntity ? 4000 : 500);
		
		// Occasionally spawn ghastlings when a ghast dies to a reflected fireball
		if(victim.getType() == EntityType.GHAST)
			if(cause.getImmediateSource() instanceof FireballEntity && cause.getTrueSource() instanceof PlayerEntity)
			{
				Random rand = victim.getRNG();
				if(rand.nextInt(15) == 0)
					for(int i=0; i<rand.nextInt(3); i++)
					{
						EntityGhastling ghastling = VOEntities.GHASTLING.create(world);
						ghastling.setLocationAndAngles(victim.getPosX(), victim.getPosY(), victim.getPosZ(), rand.nextFloat() * 360F, 0F);
						world.addEntity(ghastling);
					}
			}
		
		// Heal worgs and wargs when they kill something
		if(cause instanceof EntityDamageSource && ((EntityDamageSource)cause).getTrueSource() instanceof AbstractGoblinWolf)
			((AbstractGoblinWolf)cause.getTrueSource()).heal(2F + victim.getRNG().nextFloat() * 3F);
		
	}
	
	@SubscribeEvent(priority=EventPriority.LOW)
	public static void unconsciousDeathEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntityLiving();
		EntityBodyUnconscious body = EntityBodyUnconscious.getBodyFromEntity(victim);
		if(body != null)
			victim.copyLocationAndAnglesFrom(body);
	}
	
	/** Spawn a corpse when a Needled creature dies */
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void corpseSpawnEvent(LivingDeathEvent event)
	{
		LivingEntity victim = event.getEntityLiving();
		World world = victim.getEntityWorld();
		if(event.getSource() == DamageSource.OUT_OF_WORLD || event.isCanceled())
			return;
		
		if(!(victim instanceof MobEntity || victim instanceof PlayerEntity))
			return;
		
		boolean spawnCorpse = false;
		switch(ConfigVO.GENERAL.corpseSpawnRule())
		{
			case PLAYERS_ONLY:
				spawnCorpse = victim.getType() == EntityType.PLAYER;
				break;
			case NEEDLED_ONLY:
				spawnCorpse = victim.isPotionActive(VOPotions.NEEDLED);
				break;
			case PLAYERS_AND_NEEDLED:
				spawnCorpse = victim.getType() == EntityType.PLAYER || victim.isPotionActive(VOPotions.NEEDLED);
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
			victim.removeActivePotionEffect(VOPotions.NEEDLED);
			EntityBodyCorpse corpse = EntityBodyCorpse.createCorpseFrom(victim);
			
			if(victim.getType() == EntityType.PLAYER)
			{
				PlayerData playerData = PlayerData.forPlayer((PlayerEntity)victim);
				
				// If player is already dead, let them die as normal
				if(PlayerData.isPlayerBodyDead((PlayerEntity)victim))
					return;
				// Otherwise, cancel the event and set them to be dead
				else if(playerData.setConditionIsDead(corpse.getUniqueID()))
				{
					event.setCanceled(true);
					world.getPlayers().forEach((player) -> { player.sendMessage(event.getSource().getDeathMessage(victim), victim.getUniqueID()); });
					return;
				}
			}
			else if(corpse != null && !world.isRemote)
			{
				corpse.setPocketInventory(LivingData.forEntity(victim).getPocketInventory());
				world.addEntity(corpse);
			}
		}
		else if(!world.isRemote)
		{
			LivingData livingData = LivingData.forEntity(victim);
			for(ItemStack stack : livingData.getPocketInventory())
				if(!stack.isEmpty())
					victim.entityDropItem(stack, victim.getRNG().nextFloat());
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
			PlayerEntity player = event.getPlayer();
			PlayerData data = PlayerData.forPlayer(player);
			World world = player.getEntityWorld();
			
			if(!(event.getNewBody() == BodyCondition.ALIVE && event.getNewSoul() == SoulCondition.ALIVE))
				AbstractBody.clearNearbyAttackTargetsOf(player);
			
			switch(event.getNewBody())
			{
				case DEAD:
					player.removeActivePotionEffect(VOPotions.NEEDLED);
					EntityBodyCorpse corpse = EntityBodyCorpse.createCorpseFrom(player);
					data.setBodyUUID(corpse.getUniqueID());
					player.setHealth(player.getMaxHealth());
					if(!world.isRemote)
						world.addEntity(corpse);
					break;
				case UNCONSCIOUS:
					LivingEntity body = EntityBodyUnconscious.createBodyFrom(player);
					data.setBodyUUID(body.getUniqueID());
					if(!world.isRemote)
						world.addEntity(body);
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
		ListNBT explosions = event.fireworkData().getList("Explosions", 10);
		if(!explosions.isEmpty())
		{
			AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(event.position()).grow(16 * explosions.size());
			for(EntityWorg worg : event.world().getEntitiesWithinAABB(EntityWorg.class, bounds))
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
			
			ResourceLocation currentDim = entity.getEntityWorld().getDimensionKey().getLocation();
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
			LivingEntity hurtEntity = event.getEntityLiving();
			if(VOPotions.isSilenced(hurtEntity))
				return;
			
			wakeupEntitiesAround(hurtEntity);
			
			EffectInstance sleepEffect = hurtEntity.getActivePotionEffect(VOPotions.SLEEP);
			int tier = (sleepEffect == null || sleepEffect.getDuration() <= 0) ? -1 : sleepEffect.getAmplifier();
			
			if(PotionSleep.isSleeping(hurtEntity) && tier < 1)
				if(!MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(hurtEntity, true)))
					hurtEntity.removePotionEffect(VOPotions.SLEEP);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onCharmedHurtEvent(LivingHurtEvent event)
	{
		if(event.getAmount() > 0F && !event.isCanceled())
		{
			DamageSource source = event.getSource();
			
			LivingData data = LivingData.forEntity(event.getEntityLiving());
			if(data == null)
				return;
			
			Entity immediate = source.getImmediateSource();
			Entity distant = source.getTrueSource();
			if(immediate != null && immediate instanceof LivingEntity && data.isCharmedBy((LivingEntity)immediate))
				data.clearMindControlled((LivingEntity)immediate, MindControl.CHARMED);
			if(distant != null && distant instanceof LivingEntity && data.isCharmedBy((LivingEntity)distant))
				data.clearMindControlled((LivingEntity)distant, MindControl.CHARMED);
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
			LivingEntity hurtEntity = event.getEntityLiving();
			LivingData data = LivingData.forEntity(hurtEntity);
			
			if(data != null)
			{
				data.setBludgeoning(data.getBludgeoning() + event.getAmount());
				if(hurtEntity.getType() == EntityType.PLAYER && !hurtEntity.getEntityWorld().isRemote)
					PacketHandler.sendTo((ServerPlayerEntity)hurtEntity, new PacketSyncBludgeoning(data.getBludgeoning()));
			}
			
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onAnchoredTeleport(EntityTeleportEvent.EnderEntity event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(entity.isPotionActive(VOPotions.ANCHORED))
			event.setCanceled(true);
	}
	
	public static void wakeupEntitiesAround(Entity source, double rangeXZ, double rangeY)
	{
		for(LivingEntity entity : source.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, source.getBoundingBox().grow(rangeXZ, rangeY, rangeXZ)))
		{
			if(entity == source || !PotionSleep.isSleeping(entity) || PotionSleep.hasSleepEffect(entity))
				continue;
			
			if(entity instanceof LivingEntity)
			{
				double wakeupChance = (new Random(entity.getUniqueID().getLeastSignificantBits())).nextDouble();
				if(entity.getRNG().nextDouble() < wakeupChance && !MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(entity, true)))
					entity.removeActivePotionEffect(VOPotions.SLEEP);
			}
			else if(entity instanceof PlayerEntity)
			{
				PlayerEntity player = (PlayerEntity)entity;
				if(!MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(entity, true)))
					player.wakeUp();
			}
		}
	}
	
	public static void wakeupEntitiesAround(Entity source)
	{
		wakeupEntitiesAround(source, 6D, 2D);
	}
	
	private static void reduceRefractory(LivingEntity goblinIn, int amount)
	{
		List<EntityGoblin> nearbyGoblins = goblinIn.getEntityWorld().getEntitiesWithinAABB(EntityGoblin.class, goblinIn.getBoundingBox().grow(10));
		for(EntityGoblin goblin : nearbyGoblins)
			if(goblin.isAlive() && goblin.getGrowingAge() > 0)
				goblin.setGrowingAge(Math.max(0, goblin.getGrowingAge() - amount));
	}
}
