package com.lying.variousoddities.capabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.entity.IDefaultSpecies;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeApplyEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeRemoveEvent;
import com.lying.variousoddities.api.event.SpeciesEvent;
import com.lying.variousoddities.api.event.SpeciesEvent.TemplateApplied;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.condition.Condition;
import com.lying.variousoddities.condition.ConditionInstance;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.EntityBodyUnconscious;
import com.lying.variousoddities.init.VOCapabilities;
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.network.PacketBludgeoned;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncLivingData;
import com.lying.variousoddities.network.PacketSyncVisualPotions;
import com.lying.variousoddities.network.PacketVisualPotion;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Species.SpeciesInstance;
import com.lying.variousoddities.species.SpeciesRegistry;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.CreatureTypeDefaults;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.species.types.TypeBus;
import com.lying.variousoddities.species.types.Types;
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Data manipulated by the types system for some special effects.<br>
 * Used predominantly by players, but is not exclusive to them.<br>
 * @author Lying
 */
public class LivingData implements ICapabilitySerializable<CompoundTag>
{
	private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("1f1a65b2-2041-44d9-af77-e13166a2a5b3");
	
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "living_data");
	
	private final LazyOptional<LivingData> handler;
	
	private LivingEntity entity = null;
	private boolean isPlayer = false;
	
	private boolean initialised = false;
	private List<EnumCreatureType> customTypes = Lists.newArrayList();
	private List<EnumCreatureType> prevTypes = Lists.newArrayList();
	private ResourceLocation originDimension = null;
	
	private boolean selectedSpecies = false;
	private Species.SpeciesInstance species = null;
	private Map<ResourceLocation, Template> templates = new HashMap<>();
	
	private byte visualPotions = (byte)0;
	private int potionSyncTimer = 0;
	
	private int air = Reference.Values.TICKS_PER_DAY;
	
	private float bludgeoning = 0F;
	private boolean isUnconscious = false;
	private int recoveryTimer = ConfigVO.GENERAL.bludgeoningRecoveryRate();
	private NonNullList<ItemStack> pockets = NonNullList.withSize(6, ItemStack.EMPTY);
	
	/** Complex status effects */
	private List<ConditionInstance> conditions = Lists.newArrayList();
	
	public boolean checkingFoodRegen = false;
	
	private boolean dirty = false;
	
	public LivingData()
	{
		this.handler = LazyOptional.of(() -> this);
	}
	
	public LazyOptional<LivingData> handler(){ return this.handler; }
	
	@Nullable
	public static LivingData forEntity(LivingEntity entity)
	{
		if(entity == null || entity instanceof AbstractBody)
			return null;
		
		LivingData data = entity.getCapability(VOCapabilities.LIVING_DATA).orElse(null);
		if(data != null)
			data.setEntity(entity);
		return data;
	}
	
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return VOCapabilities.LIVING_DATA.orEmpty(cap, this.handler);
	}
	
	private void setEntity(LivingEntity entityIn)
	{
		this.entity = entityIn;
		this.isPlayer = entityIn.getType() == EntityType.PLAYER;
	}
	
	public CompoundTag serializeNBT()
	{
		CompoundTag compound = new CompoundTag();
			compound.putBoolean("Initialised", this.initialised);
			
			if(this.originDimension != null)
				compound.putString("HomeDim", this.originDimension.toString());
			
			compound.putInt("Air", this.air);
			compound.putFloat("Bludgeoning", getBludgeoning());
			compound.putInt("Recovery", this.recoveryTimer);
			compound.putBoolean("Unconscious", isActuallyUnconscious());
			
			if(this.species != null)
				compound.put("Species", this.species.writeToNBT(new CompoundTag()));
			compound.putBoolean("SelectedSpecies", this.selectedSpecies);
			
			if(!this.templates.isEmpty())
			{
				ListTag templateList = new ListTag();
				for(ResourceLocation template : templates.keySet())
					templateList.add(StringTag.valueOf(template.toString()));
				compound.put("Templates", templateList);
			}
			
			ListTag types = new ListTag();
			for(EnumCreatureType type : prevTypes)
				types.add(StringTag.valueOf(type.getSerializedName()));
			compound.put("Types", types);
			
			if(!this.customTypes.isEmpty())
			{
				ListTag customTypes = new ListTag();
				for(EnumCreatureType type : this.customTypes)
					customTypes.add(StringTag.valueOf(type.getSerializedName()));
				compound.put("CustomTypes", customTypes);
			}
			
			if(!this.conditions.isEmpty())
			{
				ListTag conditionList = new ListTag();
				this.conditions.forEach((instance) -> { conditionList.add(instance.write(new CompoundTag())); });
				compound.put("Conditions", conditionList);
			}
			
			compound.putByte("Potions", this.visualPotions);
			
			if(!isPlayer)
			{
				ListTag pocketItems = new ListTag();
				for(ItemStack stack : pockets)
					pocketItems.add(stack.save(new CompoundTag()));
				
				compound.put("Pockets", pocketItems);
			}
		return compound;
	}
	
	public void deserializeNBT(CompoundTag nbt)
	{
		if(nbt.isEmpty())
		{
			resetLivingData();
			return;
		}
		this.initialised = nbt.getBoolean("Initialised");
		
		if(nbt.contains("HomeDim", 8))
			this.originDimension = new ResourceLocation(nbt.getString("HomeDim"));
		
		this.air = nbt.getInt("Air");
		this.bludgeoning = nbt.getFloat("Bludgeoning");
		this.recoveryTimer = nbt.getInt("Recovery");
		this.isUnconscious = nbt.getBoolean("Unconscious");
		
		this.species = null;
		if(nbt.contains("Species", 10))
		{
			CompoundTag speciesData = nbt.getCompound("Species");
			this.species = SpeciesRegistry.instanceFromNBT(speciesData);
		}
		this.selectedSpecies = nbt.getBoolean("SelectedSpecies");
		
		this.templates.clear();
		if(nbt.contains("Templates", 9))
		{
			ListTag templateList = nbt.getList("Templates", 8);
			for(int i=0; i<templateList.size(); i++)
			{
				ResourceLocation registryName = new ResourceLocation(templateList.getString(i));
				if(VORegistries.TEMPLATES.containsKey(registryName))
					this.templates.put(registryName, VORegistries.TEMPLATES.get(registryName));
			}
		}
		
		ListTag types = nbt.getList("Types", 8);
		prevTypes.clear();
		for(int i=0; i<types.size(); i++)
			prevTypes.add(EnumCreatureType.fromName(types.getString(i)));
		
		this.customTypes.clear();
		if(nbt.contains("CustomTypes", 9))
		{
			ListTag customTypes = nbt.getList("CustomTypes", 8);
			for(int i=0; i<customTypes.size(); i++)
				this.customTypes.add(EnumCreatureType.fromName(types.getString(i)));
		}
		
		this.conditions.clear();
		if(nbt.contains("Conditions", 9))
		{
			ListTag conditionList = nbt.getList("Conditions", 10);
			for(int i=0; i<conditionList.size(); i++)
			{
				ConditionInstance instance = ConditionInstance.read(conditionList.getCompound(i));
				if(instance != null)
					this.conditions.add(instance);
			}
		}
		
		this.visualPotions = nbt.getByte("Potions");
		
		if(nbt.contains("Pockets", 10))
		{
			ListTag pocketItems = nbt.getList("Pockets", 10);
			for(int i=0; i<6; i++)
			{
				CompoundTag stackData = pocketItems.getCompound(i);
				this.pockets.set(i, ItemStack.of(stackData));
			}
		}
	}
	
	private void resetLivingData()
	{
		LivingData fresh = new LivingData();
		CompoundTag freshData = fresh.serializeNBT();
		this.deserializeNBT(freshData);
	}
	
	public NonNullList<ItemStack> getPocketInventory(){ return this.pockets; }
	public void setPocketInventory(NonNullList<ItemStack> inventory)
	{
		if(isPlayer)
			return;
		
		for(int i=0; i<6; i++)
			this.pockets.set(i, inventory.get(i));
		markDirty();
	}
	
	public ResourceLocation getHomeDimension(){ return this.originDimension; }
	public void setHomeDimension(ResourceLocation dimension){ this.originDimension = dimension; markDirty(); }
	
	@Nullable
	private AbilityData getAbilities()
	{
		return this.entity == null ? null : AbilityData.forEntity(this.entity);
	}
	
	public void tryMarkAbilitiesToRecache()
	{
		AbilityData abilities = getAbilities();
		if(abilities != null)
			abilities.markForRecache();
	}
	
	public byte getVisualPotions(){ return this.visualPotions; }
	public boolean getVisualPotion(@Nullable MobEffect potion){ return potion == null ? false : getVisualPotion(VOMobEffects.getVisualPotionIndex(potion)); }
	public boolean getVisualPotion(int index)
	{
		if(index < 0) return false;
		
		if(visualPotions == 0)
			return false;
		return DataHelper.Bytes.getBit(visualPotions, index);
	}
	public void setVisualPotion(int index, boolean bool)
	{
		if(index < 0)
			return;
		
		boolean initial = getVisualPotion(index);
		if(initial == bool)
			return;
		
		// Set bit in visualPotions
		this.visualPotions = (byte)DataHelper.Bytes.setBit(visualPotions, index, bool);
		
		// Packet to nearby players to sync
		if(this.entity != null && !this.entity.getLevel().isClientSide)
			PacketHandler.sendToNearby(this.entity.getLevel(), this.entity, new PacketVisualPotion(this.entity.getUUID(), index, bool));
	}
	
	public void setVisualPotions(byte value)
	{
		this.visualPotions = (byte)Math.max(0, value);
	}
	
	public boolean hasSpecies(){ return this.species != null; }
	public SpeciesInstance getSpecies(){ return this.species; }
	public void setSpecies(SpeciesInstance speciesIn)
	{
		AbilityData abilities = getAbilities();
		this.species = null;
		if(abilities != null)
			abilities.updateAbilityCache();
		
		this.species = speciesIn;
		if(abilities != null)
			abilities.updateAbilityCache();
		
		markDirty();
	}
	public void setSpecies(Species speciesIn){ setSpecies(speciesIn.createInstance()); }
	
	public boolean hasSelectedSpecies(){ return this.selectedSpecies; }
	public void setSelectedSpecies(boolean bool)
	{
		if(bool != this.selectedSpecies)
			markDirty();
		this.selectedSpecies = bool;
	}
	
	public boolean addTemplateInitial(Template templateIn)
	{
		if(addTemplate(templateIn))
		{
			if(this.entity != null && this.entity.getType() == EntityType.PLAYER)
			{
				SpeciesEvent.TemplateApplied event = new TemplateApplied((Player)this.entity, templateIn.getRegistryName());
				MinecraftForge.EVENT_BUS.post(event);
			}
			return true;
		}
		return false;
	}
	
	public boolean addTemplate(Template templateIn)
	{
		if(this.templates.containsKey(templateIn.getRegistryName()))
			return false;
		
		this.templates.put(templateIn.getRegistryName(), templateIn);
		tryMarkAbilitiesToRecache();
		this.markDirty();
		return true;
	}
	
	public void removeTemplate(Template templateIn)
	{
		removeTemplate(templateIn.getRegistryName());
	}
	
	public void removeTemplate(ResourceLocation registryName)
	{
		if(!hasTemplate(registryName))
			return;
		
		this.templates.remove(registryName);
		tryMarkAbilitiesToRecache();
		this.markDirty();
	}
	
	public void clearTemplates()
	{
		this.templates.clear();
		tryMarkAbilitiesToRecache();
		this.markDirty();
	}
	
	public void setTemplates(Collection<Template> templatesIn)
	{
		clearTemplates();
		templatesIn.forEach((template) -> { addTemplate(template); });
	}
	
	public Collection<Template> getTemplates(){ return this.templates.values(); }
	
	public boolean hasTemplates(){ return !this.templates.isEmpty(); }
	public boolean hasTemplate(ResourceLocation registryName){ return this.templates.containsKey(registryName); }
	
	public List<EnumCreatureType> getTypesFromSpecies()
	{
		List<EnumCreatureType> types = Lists.newArrayList();
		types.addAll(this.species.getTypes());
		return types;
	}
	
	public float getBludgeoning(){ return this.bludgeoning; }
	public void addBludgeoning(float bludgeonIn)
	{
		boolean knockedOutThen = this.bludgeoning >= (entity == null ? 20F : entity.getHealth());
		setBludgeoning(this.bludgeoning + bludgeonIn);
		boolean knockedOutNow = this.bludgeoning >= (entity == null ? 20F : entity.getHealth());
		
		if(bludgeonIn > 0F && entity != null && !entity.getLevel().isClientSide)
			PacketHandler.sendToNearby(entity.getLevel(), entity, new PacketBludgeoned(entity.getUUID(), knockedOutNow && !knockedOutThen));
	}
	public void setBludgeoning(float bludgeonIn)
	{
		this.bludgeoning = Mth.clamp(bludgeonIn, 0F, this.entity.getMaxHealth() + ConfigVO.GENERAL.bludgeoningCap());
		markDirty();
	}
	
	/**
	 * Returns true IF:<br>
	 * * The entity is alive<br>
	 * * The entity's health and bludgeoning damage are both greater than 0<br>
	 * * The bludgeoning damage is greater than health<br>
	 * Does NOT represent the actual consciousness state of the entity
	 */
	public boolean isUnconscious()
	{
		if(this.entity != null && this.entity.isAlive())
			return unconscious(entity);
		return false;
	}
	
	public static boolean unconscious(@Nonnull LivingEntity entity)
	{
		float health = entity.getHealth();
		if(!entity.isAlive() || health <= 0F)
			return false;
		
		LivingData data = LivingData.forEntity(entity);
		float bludgeoning = data.getBludgeoning();
		if(bludgeoning <= 0F)
			return false;
		
		if(health <= bludgeoning || entity.getEffect(VOMobEffects.SLEEP.get()) != null && entity.getEffect(VOMobEffects.SLEEP.get()).getDuration() > 0)
			return true;
		return false;
	}
	
	/** Returns true if the entity is currently actually unconscious */
	public boolean isActuallyUnconscious()
	{
		return this.entity != null && this.isPlayer ? PlayerData.isPlayerBodyAsleep(entity) : this.isUnconscious;
	}
	
	public boolean hasCustomTypes(){ return !this.customTypes.isEmpty(); }
	public List<EnumCreatureType> getCustomTypes(){ return this.customTypes; }
	public void clearCustomTypes()
	{
		this.customTypes.clear();
		tryMarkAbilitiesToRecache();
		markDirty();
	}
	public void addCustomType(EnumCreatureType type)
	{
		if(!this.customTypes.contains(type))
		{
			this.customTypes.add(type);
			tryMarkAbilitiesToRecache();
			markDirty();
		}
	}
	public void removeCustomType(EnumCreatureType type)
	{
		if(this.customTypes.contains(type))
		{
			this.customTypes.remove(type);
			tryMarkAbilitiesToRecache();
			markDirty();
		}
	}
	public void setCustomTypes(Collection<EnumCreatureType> typesIn)
	{
		this.customTypes.clear();
		this.customTypes.addAll(typesIn);
		tryMarkAbilitiesToRecache();
		markDirty();
	}
	
	public void addCondition(@Nullable ConditionInstance condition)
	{
		if(condition != null)
		{
			this.conditions.add(condition);
			
			condition.start(this.entity);
			markDirty();
		}
	}
	
	public boolean hasCondition(@Nonnull Condition condition)
	{
		return hasConditionFrom(condition, null);
	}
	
	public boolean hasCondition(@Nonnull Condition condition, @Nullable LivingEntity entity)
	{
		return entity == null ? false : hasConditionFrom(condition, entity.getUUID());
	}
	
	public boolean hasConditionFrom(@Nonnull Condition condition, @Nullable UUID uuidIn)
	{
		if(!this.conditions.isEmpty())
			for(ConditionInstance instance : this.conditions)
				if(instance.condition() == condition && (uuidIn == null || instance.originUUID().equals(uuidIn)))
					return true;
		return false;
	}
	
	public List<ConditionInstance> getConditions(@Nonnull Condition condition)
	{
		List<ConditionInstance> conditions = Lists.newArrayList();
		if(!this.conditions.isEmpty())
			this.conditions.forEach((instance) -> { if(instance.condition() == condition) conditions.add(instance); }); 
		return conditions;
	}
	
	public List<UUID> getConditionSources(@Nonnull Condition condition)
	{
		List<UUID> sources = Lists.newArrayList();
		if(!this.conditions.isEmpty())
			this.conditions.forEach((instance) -> { if(instance.condition() == condition && instance.originUUID() != null) sources.add(instance.originUUID()); });
		return sources;
	}
	
	public List<ConditionInstance> getConditionsFromUUID(@Nonnull UUID uuidIn)
	{
		List<ConditionInstance> conditions = Lists.newArrayList();
		if(!this.conditions.isEmpty())
			this.conditions.forEach((instance) -> { if(instance.originUUID().equals(uuidIn)) conditions.add(instance); }); 
		return conditions;
	}
	
	public void clearCondition(@Nonnull LivingEntity entity, Condition condition)
	{
		if(!hasCondition(condition, entity))
			return;
		
		getConditionsFromUUID(entity.getUUID()).forEach((instance) -> { if(instance.condition() == condition) removeCondition(instance); });
	}
	
	public void removeCondition(ConditionInstance instance)
	{
		if(this.conditions.remove(instance))
		{
			instance.reset(this.entity);
			markDirty();
		}
	}
	
	public List<LivingEntity> getMindControlled(Condition condition, double distance)
	{
		List<LivingEntity> entities = Lists.newArrayList();
		Collection<UUID> controllers = getConditionSources(condition);
		if(controllers.isEmpty())
			return entities;
		
		Level world = this.entity.getLevel();
		for(UUID uuid : controllers)
		{
			LivingEntity entity = world.getPlayerByUUID(uuid);
			if(entity != null)
			{
				if(PlayerData.isPlayerNormalFunction(entity))
					entities.add(entity);
			}
			else
				for(LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, this.entity.getBoundingBox().inflate(distance)))
					if(ent.getUUID().equals(uuid))
					{
						entities.add(ent);
						break;
					}
		}
		
		return entities;
	}
	
	public boolean isTargetingHindered(LivingEntity target)
	{
		for(ConditionInstance instance : this.conditions)
			if(instance.condition().affectsMobTargeting() && instance.originUUID() != null && instance.originUUID().equals(target.getUUID()))
				return true;
		return false;
	}
	
	public void tick(LivingEntity entity)
	{
		IDefaultSpecies mobDefaults = entity instanceof IDefaultSpecies ? (IDefaultSpecies)entity : null;
		
		Level world = entity.getLevel();
		boolean isServer = !world.isClientSide();
		if(!this.initialised && (!isPlayer || ((Player)entity).getGameProfile() != null))
		{
			// TODO Check default home dimension registry for creature before setting to current dim
			if(mobDefaults != null)
			{
				if(((IDefaultSpecies)entity).defaultHomeDimension() != null)
					setHomeDimension(((IDefaultSpecies)entity).defaultHomeDimension());
			}
			else
				setHomeDimension(world.dimension().location());
			
			if(isPlayer)
			{
				Player player = (Player)entity;
				String name = player.getName().getString();
				if(CreatureTypeDefaults.isTypedPatron(name))
				{
					setCustomTypes(CreatureTypeDefaults.getPatronTypes(name));
					VariousOddities.log.info("Initialised patron "+name+" as "+EnumCreatureType.getTypes(player).toHeader().getString());
				}
			}
			else
			{
				if(mobDefaults != null)
				{
					if(mobDefaults.defaultSpecies() != null)
					{
						Species defaultSpecies = SpeciesRegistry.getSpecies(mobDefaults.defaultSpecies());
						if(defaultSpecies != null)
							setSpecies(defaultSpecies);
					}
					
					if(!mobDefaults.defaultTemplates().isEmpty())
						mobDefaults.defaultTemplates().forEach((temp) -> 
						{
							Template template = VORegistries.TEMPLATES.getOrDefault(temp, null);
							if(template != null)
								addTemplateInitial(template);
						});
					
					if(!mobDefaults.defaultCreatureTypes().isEmpty())
						mobDefaults.defaultCreatureTypes().forEach((type) -> { addCustomType(type); } );
					
					AbilityData abilities = getAbilities();
					if(!mobDefaults.defaultAbilities().isEmpty() && abilities != null)
						mobDefaults.defaultAbilities().forEach((ability) -> abilities.addCustomAbility(AbilityRegistry.getAbility(ability.writeAtomically(new CompoundTag()))));
				}
				else
				{
					Species guess = SpeciesRegistry.getSpecies(EntityType.getKey(entity.getType()));
					if(guess != null)
						setSpecies(guess);
				}
				setSelectedSpecies(true);
			}
			
			this.initialised = true;
			markDirty();
		}
		
		if(!ConfigVO.MOBS.createCharacterOnLogin.get())
			setSelectedSpecies(true);
		
		handleTypes(entity, world);
		
		Player player = null;
		if(isPlayer)
			player = (Player)entity;
		
		if(isPlayer)
			handleHealth(player);
		
		ActionSet actions = ActionSet.fromTypes(this.entity, this.prevTypes);
		
		if(isServer)
		{
			ServerPlayer serverPlayer = (ServerPlayer)player;
			
			// Prevent phantoms due to sleeplessness
			if(isPlayer && !actions.sleeps())
			{
				ServerStatsCounter statManager = serverPlayer.getStats();
                statManager.setValue(serverPlayer, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 0);
			}
			
			if(this.bludgeoning > 0F)
				if(--this.recoveryTimer <= 0)
				{
					addBludgeoning(-(isPlayer && entity.isSleeping() ? 2F : 1F));
					this.recoveryTimer = ConfigVO.GENERAL.bludgeoningRecoveryRate();
				}
		}
		
		if(isUnconscious() != isActuallyUnconscious())
		{
			if(isPlayer)
			{
				if(isUnconscious())
					PlayerData.forPlayer((Player)entity).setBodyCondition(BodyCondition.UNCONSCIOUS);
				
				this.isUnconscious = isUnconscious();
			}
			else
			{
				if(isUnconscious())
				{
					AbstractBody.clearNearbyAttackTargetsOf(entity);
					
					// Spawn body
					LivingEntity body = EntityBodyUnconscious.createBodyFrom(entity);
					((AbstractBody)body).setPocketInventory(getPocketInventory());
					if(entity.isAddedToWorld() && isServer)
					{
						world.addFreshEntity(body);
						entity.discard();
					}
				}
				
				this.isUnconscious = isUnconscious();
			}
			
			markDirty();
		}
		
		handleConditions();
		
		if(this.dirty)
		{
			if(this.entity != null && !this.entity.getLevel().isClientSide)
				PacketHandler.sendToNearby(entity.getLevel(), entity, new PacketSyncLivingData(entity.getUUID(), this));
			this.dirty = false;
		}
		
		if(world.isClientSide && --this.potionSyncTimer <= 0)
		{
			this.potionSyncTimer = Reference.Values.TICKS_PER_MINUTE;
			PacketHandler.sendToServer(new PacketSyncVisualPotions(this.entity.getUUID()));
		}
	}
	
	private void handleConditions()
	{
		List<ConditionInstance> expired = Lists.newArrayList();
		this.conditions.forEach((instance) -> 
		{
			if(instance.isExpired())
			{
				instance.end(this.entity);
				expired.add(instance);
			}
			else
				instance.tick(this.entity);
		});
		this.conditions.removeAll(expired);
		if(!expired.isEmpty() || !this.conditions.isEmpty())
			markDirty();
	}
	
	public void setSpeciesSelected()
	{
		setSelectedSpecies(true);
		if(entity.hasEffect(MobEffects.DAMAGE_RESISTANCE) && entity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 15)
			entity.removeEffect(MobEffects.DAMAGE_RESISTANCE);
	}
	
	/** Manages the application and removal of creature types */
	public void handleTypes(LivingEntity entity, Level world)
	{
		List<EnumCreatureType> typesNow = EnumCreatureType.getCreatureTypes(entity);
		
		List<EnumCreatureType> typesNew = Lists.newArrayList(typesNow);
		typesNew.removeAll(prevTypes);
		typesNew.forEach((type) -> { MinecraftForge.EVENT_BUS.post(new TypeApplyEvent(entity, type)); });
		
		this.prevTypes.removeAll(typesNow);
		prevTypes.forEach((type) -> { MinecraftForge.EVENT_BUS.post(new TypeRemoveEvent(entity, type)); });
		
		if(!typesNew.isEmpty() || !prevTypes.isEmpty())
			markDirty();
		
		this.prevTypes.clear();
		this.prevTypes.addAll(typesNow);
	}
	
	public List<EnumCreatureType> getTypes() { return this.prevTypes; }
	
	/** Manage base health according to active supertypes */
	public void handleHealth(Player player)
	{
		List<EnumCreatureType> supertypes = new ArrayList<>();
		supertypes.addAll(this.prevTypes);
		supertypes.removeIf(EnumCreatureType.IS_SUBTYPE);
		
		double hitDieModifier = 0D;
		if(!supertypes.isEmpty() && !player.getAbilities().invulnerable)
		{
			for(EnumCreatureType type : supertypes)
			{
				double health = ((double)type.getHitDie() / (double)EnumCreatureType.HUMANOID.getHitDie()) * 20D;
				hitDieModifier += health - 20D;
			}
			hitDieModifier /= Math.max(1, supertypes.size());
		}
		
		AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier modifier = healthAttribute.getModifier(HEALTH_MODIFIER_UUID);
		if(!TypeBus.shouldFire() || supertypes.isEmpty() || player.getAbilities().invulnerable)
			healthAttribute.removeModifier(HEALTH_MODIFIER_UUID);
		else
		{
			hitDieModifier = Math.max(hitDieModifier, -healthAttribute.getBaseValue() + 1);
			if(modifier == null)
			{
				modifier = makeModifier(hitDieModifier);
				healthAttribute.addPermanentModifier(modifier);
			}
			else if(modifier.getAmount() != hitDieModifier)
			{
				boolean shouldHeal = player.getHealth() == player.getMaxHealth() && modifier.getAmount() < hitDieModifier;
				healthAttribute.removeModifier(modifier);
				healthAttribute.addPermanentModifier(makeModifier(hitDieModifier));
				if(shouldHeal)
					player.setHealth(player.getMaxHealth());
			}
		}
	}
	
	private static AttributeModifier makeModifier(double amount)
	{
		return new AttributeModifier(HEALTH_MODIFIER_UUID, "hit_die_modifier", amount, AttributeModifier.Operation.ADDITION);
	}
	
	public void markDirty()
	{
		this.dirty = true;
	}
	
	public static void syncOnDeath(Player original, Player next)
	{
		original.getCapability(VOCapabilities.LIVING_DATA).ifPresent(then -> next.getCapability(VOCapabilities.LIVING_DATA).ifPresent(now -> now.clone(then)));
	}
	
	public void clone(LivingData dataIn)
	{
		VariousOddities.log.info("Cloning LivingData");
		
		if(dataIn.hasCustomTypes())
		{
			VariousOddities.log.info("# Custom types: "+(new Types(dataIn.getCustomTypes())).toHeader().getString());
			setCustomTypes(dataIn.getCustomTypes());
		}
		else
			clearCustomTypes();
		
		this.selectedSpecies = dataIn.hasSelectedSpecies();
		
		if(dataIn.hasSpecies())
		{
			VariousOddities.log.info("# Species: "+dataIn.getSpecies().getDisplayName().getString());
			setSpecies(dataIn.getSpecies());
		}
		else
			this.species = null;
		
		if(dataIn.hasTemplates())
		{
			VariousOddities.log.info("# Templates:");
			dataIn.getTemplates().forEach((template) -> VariousOddities.log.info("#   -"+template.getDisplayName().getString()));
			setTemplates(dataIn.getTemplates());
		}
		else
			clearTemplates();
	}
}
