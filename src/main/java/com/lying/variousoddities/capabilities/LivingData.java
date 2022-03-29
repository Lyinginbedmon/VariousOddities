package com.lying.variousoddities.capabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.entity.IDefaultSpecies;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeApplyEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeRemoveEvent;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.entity.EntityBodyUnconscious;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncAir;
import com.lying.variousoddities.network.PacketSyncBludgeoning;
import com.lying.variousoddities.network.PacketSyncLivingData;
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
import com.lying.variousoddities.utility.DataHelper;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Data manipulated by the types system for some special effects.<br>
 * Used predominantly by players, but is not exclusive to them.<br>
 * @author Lying
 */
public class LivingData implements ICapabilitySerializable<CompoundNBT>
{
	private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("1f1a65b2-2041-44d9-af77-e13166a2a5b3");
	
	@CapabilityInject(LivingData.class)
	public static final Capability<LivingData> CAPABILITY = null;
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "living_data");
	
	private final LazyOptional<LivingData> handler;
	
	private LivingEntity entity = null;
	
	private boolean initialised = false;
	private List<EnumCreatureType> customTypes = Lists.newArrayList();
	private List<EnumCreatureType> prevTypes = Lists.newArrayList();
	private ResourceLocation originDimension = null;
	
	private boolean selectedSpecies = false;
	private Species.SpeciesInstance species = null;
	private Map<ResourceLocation, Template> templates = new HashMap<>();
	
	private Abilities abilities = new Abilities();
	
	private byte visualPotions = (byte)0;
	private int potionSyncTimer = 0;
	
	private int air = Reference.Values.TICKS_PER_DAY;
	private boolean overridingAir = false;
	
	private float bludgeoning = 0F;
	private boolean isUnconscious = false;
	private int recoveryTimer = ConfigVO.GENERAL.bludgeoningRecoveryRate();
	
	public boolean checkingFoodRegen = false;
	
	private boolean dirty = false;
	
	public LivingData()
	{
		this.handler = LazyOptional.of(() -> this);
	}
	
	public static void register()
	{
		CapabilityManager.INSTANCE.register(LivingData.class, new LivingData.Storage(), () -> null);
		if(ConfigVO.GENERAL.verboseLogs())
			VariousOddities.log.info("Registered living data capability");
	}
	
	public LazyOptional<LivingData> handler(){ return this.handler; }
	
	@Nullable
	public static LivingData forEntity(LivingEntity entity)
	{
		if(entity == null)
			return null;
		
		LivingData data = null;
		try
		{
			data = entity.getCapability(CAPABILITY).orElse(null);
		}
		catch(Exception e){ }
		
		if(data != null)
			data.setEntity(entity);
		return data;
	}
	
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return CAPABILITY.orEmpty(cap, this.handler);
	}
	
	private void setEntity(LivingEntity entityIn)
	{
		this.entity = entityIn;
		this.abilities.entity = entityIn;
	}
	
	public CompoundNBT serializeNBT()
	{
		CompoundNBT compound = new CompoundNBT();
			compound.putBoolean("Initialised", this.initialised);
			
			if(this.originDimension != null)
				compound.putString("HomeDim", this.originDimension.toString());
			
			compound.putInt("Air", this.air);
			compound.putFloat("Bludgeoning", getBludgeoning());
			compound.putInt("Recovery", this.recoveryTimer);
			compound.putBoolean("Unconscious", isActuallyUnconscious());
			
			if(this.species != null)
				compound.put("Species", this.species.writeToNBT(new CompoundNBT()));
			compound.putBoolean("SelectedSpecies", this.selectedSpecies);
			
			if(!this.templates.isEmpty())
			{
				ListNBT templateList = new ListNBT();
				for(ResourceLocation template : templates.keySet())
					templateList.add(StringNBT.valueOf(template.toString()));
				compound.put("Templates", templateList);
			}
			
			ListNBT types = new ListNBT();
			for(EnumCreatureType type : prevTypes)
				types.add(StringNBT.valueOf(type.getString()));
			compound.put("Types", types);
			
			if(!this.customTypes.isEmpty())
			{
				ListNBT customTypes = new ListNBT();
				for(EnumCreatureType type : this.customTypes)
					customTypes.add(StringNBT.valueOf(type.getString()));
				compound.put("CustomTypes", customTypes);
			}
			
			compound.put("Abilities", this.abilities.serializeNBT());
			
			compound.putByte("Potions", this.visualPotions);
		return compound;
	}
	
	public void deserializeNBT(CompoundNBT nbt)
	{
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
			CompoundNBT speciesData = nbt.getCompound("Species");
			this.species = SpeciesRegistry.instanceFromNBT(speciesData);
		}
		this.selectedSpecies = nbt.getBoolean("SelectedSpecies");
		
		this.templates.clear();
		if(nbt.contains("Templates", 9))
		{
			ListNBT templateList = nbt.getList("Templates", 8);
			for(int i=0; i<templateList.size(); i++)
			{
				ResourceLocation registryName = new ResourceLocation(templateList.getString(i));
				if(VORegistries.TEMPLATES.containsKey(registryName))
					this.templates.put(registryName, VORegistries.TEMPLATES.get(registryName));
			}
		}
		
		ListNBT types = nbt.getList("Types", 8);
		prevTypes.clear();
		for(int i=0; i<types.size(); i++)
			prevTypes.add(EnumCreatureType.fromName(types.getString(i)));
		
		this.customTypes.clear();
		if(nbt.contains("CustomTypes", 9))
		{
			ListNBT customTypes = nbt.getList("CustomTypes", 8);
			for(int i=0; i<customTypes.size(); i++)
				this.customTypes.add(EnumCreatureType.fromName(types.getString(i)));
		}
		
		this.abilities.deserializeNBT(nbt.getCompound("Abilities"));
		
		this.visualPotions = nbt.getByte("Potions");
	}
	
	public ResourceLocation getHomeDimension(){ return this.originDimension; }
	public void setHomeDimension(ResourceLocation dimension){ this.originDimension = dimension; markDirty(); }
	
	public Abilities getAbilities(){ return this.abilities; }
	
	public byte getVisualPotions(){ return this.visualPotions; }
	public boolean getVisualPotion(@Nullable Effect potion){ return potion == null ? false : getVisualPotion(VOPotions.getVisualPotionIndex(potion)); }
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
		if(this.entity != null && !this.entity.getEntityWorld().isRemote)
			PacketHandler.sendToNearby(this.entity.getEntityWorld(), this.entity, new PacketVisualPotion(this.entity.getUniqueID(), index, bool));
	}
	
	public void setVisualPotions(byte value)
	{
		this.visualPotions = (byte)Math.max(0, value);
	}
	
	public boolean hasSpecies(){ return this.species != null; }
	public SpeciesInstance getSpecies(){ return this.species; }
	public void setSpecies(SpeciesInstance speciesIn)
	{
		this.species = null;
		this.abilities.updateAbilityCache();
		
		this.species = speciesIn;
		this.abilities.updateAbilityCache();
		this.abilities.markForRecache();
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
	
	public void addTemplate(Template templateIn)
	{
		if(this.templates.containsKey(templateIn.getRegistryName()))
			return;
		
		this.templates.put(templateIn.getRegistryName(), templateIn);
		this.abilities.markForRecache();
		this.markDirty();
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
		this.abilities.markForRecache();
		this.markDirty();
	}
	
	public void clearTemplates()
	{
		this.templates.clear();
		this.abilities.markForRecache();
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
	
	/** True if this object should override the vanilla air value */
	public boolean overrideAir(){ return this.overridingAir; }
	
	public int getAir(){ return this.air; }
	public void setAir(int airIn){ this.air = airIn; }
	
	private int decreaseAirSupply(int air, LivingEntity entityIn)
	{
		int i = EnchantmentHelper.getRespirationModifier(entityIn);
		return i > 0 && entityIn.getRNG().nextInt(i + 1) > 0 ? air : air - 1;
	}
	
	private int determineNextAir(int currentAir, LivingEntity entityIn)
	{
		return Math.min(currentAir + 4, entityIn.getMaxAir());
	}
	
	public float getBludgeoning(){ return this.bludgeoning; }
	public void setBludgeoning(float bludgeonIn)
	{
		float oldDamage = this.bludgeoning;
		this.bludgeoning = Math.max(0F, Math.min(bludgeonIn, this.entity.getMaxHealth() + ConfigVO.GENERAL.bludgeoningCap()));
		
		if(oldDamage != this.bludgeoning)
		{
			this.recoveryTimer = ConfigVO.GENERAL.bludgeoningRecoveryRate();
			
			if(this.entity != null && this.entity.getType() == EntityType.PLAYER && !this.entity.getEntityWorld().isRemote)
				PacketHandler.sendTo((ServerPlayerEntity)this.entity, new PacketSyncBludgeoning(this.bludgeoning));
			markDirty();
		}
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
		return this.entity != null && this.entity.getHealth() > 0 && this.entity.isAlive() && getBludgeoning() > 0 && this.entity.getHealth() <= getBludgeoning();
	}
	
	/** Returns true if the entity is currently actually unconscious */
	public boolean isActuallyUnconscious()
	{
		return this.entity != null && this.entity.getType() == EntityType.PLAYER ? PlayerData.isPlayerBodyAsleep(entity) : this.isUnconscious;
	}
	
	public boolean hasCustomTypes(){ return !this.customTypes.isEmpty(); }
	public List<EnumCreatureType> getCustomTypes(){ return this.customTypes; }
	public void clearCustomTypes()
	{
		this.customTypes.clear();
		this.abilities.markForRecache();
		markDirty();
	}
	public void addCustomType(EnumCreatureType type)
	{
		if(!this.customTypes.contains(type))
		{
			this.customTypes.add(type);
			this.abilities.markForRecache();
			markDirty();
		}
	}
	public void removeCustomType(EnumCreatureType type)
	{
		if(this.customTypes.contains(type))
		{
			this.customTypes.remove(type);
			this.abilities.markForRecache();
			markDirty();
		}
	}
	public void setCustomTypes(Collection<EnumCreatureType> typesIn)
	{
		this.customTypes.clear();
		this.customTypes.addAll(typesIn);
		this.abilities.markForRecache();
		markDirty();
	}
	
	public void tick(LivingEntity entity)
	{
		IDefaultSpecies mobDefaults = entity instanceof IDefaultSpecies ? (IDefaultSpecies)entity : null;
		
		World world = entity.getEntityWorld();
		if(!this.initialised && (!(entity.getType() == EntityType.PLAYER) || entity.getType() == EntityType.PLAYER && ((PlayerEntity)entity).getGameProfile() != null))
		{
			// TODO Check default home dimension registry for creature before setting to current dim
			if(mobDefaults != null)
			{
				if(((IDefaultSpecies)entity).defaultHomeDimension() != null)
					setHomeDimension(((IDefaultSpecies)entity).defaultHomeDimension());
			}
			else
				setHomeDimension(world.getDimensionKey().getLocation());
			
			if(entity.getType() == EntityType.PLAYER)
			{
				PlayerEntity player = (PlayerEntity)entity;
				String name = player.getName().getUnformattedComponentText();
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
								addTemplate(template);
						});
					
					if(!mobDefaults.defaultCreatureTypes().isEmpty())
						mobDefaults.defaultCreatureTypes().forEach((type) -> { addCustomType(type); } );
					
					if(!mobDefaults.defaultAbilities().isEmpty())
						mobDefaults.defaultAbilities().forEach((ability) -> { this.abilities.addCustomAbility(AbilityRegistry.getAbility(ability.writeAtomically(new CompoundNBT()))); });
				}
				else
				{
					Species guess = SpeciesRegistry.getSpecies(entity.getType().getRegistryName());
					if(guess != null)
						setSpecies(guess);
				}
				setSelectedSpecies(true);
			}
			
			this.initialised = true;
			markDirty();
		}
		
		if(!ConfigVO.MOBS.selectSpeciesOnLogin.get())
			setSelectedSpecies(true);
		
		handleTypes(entity, world);
		
		boolean isPlayer = false;
		PlayerEntity player = null;
		if(entity.getType() == EntityType.PLAYER)
		{
			isPlayer = true;
			player = (PlayerEntity)entity;
		}
		
		if(isPlayer)
			handleHealth(player);
		
		ActionSet actions = ActionSet.fromTypes(this.entity, this.prevTypes);
		
		// Prevent phantoms due to sleeplessness
		if(!actions.sleeps())
		{
			if(isPlayer && !world.isRemote)
			{
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
                ServerStatisticsManager statManager = serverPlayer.getStats();
                statManager.setValue(serverPlayer, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 0);
			}
		}
		
		handleAir(actions.breathesAir(), actions.breathesWater(), entity);
		
		if(this.bludgeoning > 0F)
			if(--this.recoveryTimer <= 0)
				setBludgeoning(this.bludgeoning - 1F);
		
		if(isUnconscious() != isActuallyUnconscious())
		{
			if(entity.getType() == EntityType.PLAYER)
			{
				if(isUnconscious)
					PlayerData.forPlayer((PlayerEntity)entity).setBodyCondition(BodyCondition.UNCONSCIOUS);
				
				this.isUnconscious = isUnconscious();
			}
			else
			{
				if(isUnconscious())
				{
					AbstractBody.clearNearbyAttackTargetsOf(entity);
					
					// Spawn body
					LivingEntity body = EntityBodyUnconscious.createBodyFrom(entity);
					if(entity.isAddedToWorld())
					{
						// TODO Play crit attack noise when creature is knocked unconscious
						if(!world.isRemote)
						{
							world.addEntity(body);
							entity.remove();
						}
					}
				}
				
				this.isUnconscious = isUnconscious();
			}
			
			markDirty();
		}
		
		abilities.tick();
		
		if(this.dirty)
		{
			if(this.entity != null && !this.entity.getEntityWorld().isRemote)
				PacketHandler.sendToNearby(entity.getEntityWorld(), entity, new PacketSyncLivingData(entity.getUniqueID(), this));
			this.dirty = false;
		}
		
		if(world.isRemote && --this.potionSyncTimer <= 0)
		{
			this.potionSyncTimer = Reference.Values.TICKS_PER_MINUTE;
			// Ping server for visualPotion value
			
		}
	}
	
	public void setSpeciesSelected()
	{
		setSelectedSpecies(true);
		if(entity.isPotionActive(Effects.RESISTANCE) && entity.getActivePotionEffect(Effects.RESISTANCE).getAmplifier() == 15)
			entity.removeActivePotionEffect(Effects.RESISTANCE);
	}
	
	/** Manages the application and removal of creature types */
	public void handleTypes(LivingEntity entity, World world)
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
	
	/** Manage base health according to active supertypes */
	public void handleHealth(PlayerEntity player)
	{
		List<EnumCreatureType> supertypes = new ArrayList<>();
		supertypes.addAll(this.prevTypes);
		supertypes.removeIf(EnumCreatureType.IS_SUBTYPE);
		
		double hitDieModifier = 0D;
		if(!supertypes.isEmpty() && !player.abilities.disableDamage)
		{
			for(EnumCreatureType type : supertypes)
			{
				double health = ((double)type.getHitDie() / (double)EnumCreatureType.HUMANOID.getHitDie()) * 20D;
				hitDieModifier += health - 20D;
			}
			hitDieModifier /= Math.max(1, supertypes.size());
		}
		
		ModifiableAttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier modifier = healthAttribute.getModifier(HEALTH_MODIFIER_UUID);
		if(!TypeBus.shouldFire() || supertypes.isEmpty() || player.abilities.disableDamage)
			healthAttribute.removeModifier(HEALTH_MODIFIER_UUID);
		else
		{
			hitDieModifier = Math.max(hitDieModifier, -healthAttribute.getBaseValue() + 1);
			if(modifier == null)
			{
				modifier = makeModifier(hitDieModifier);
				healthAttribute.applyPersistentModifier(modifier);
			}
			else if(modifier.getAmount() != hitDieModifier)
			{
				boolean shouldHeal = player.getHealth() == player.getMaxHealth() && modifier.getAmount() < hitDieModifier;
				healthAttribute.removeModifier(modifier);
				healthAttribute.applyPersistentModifier(makeModifier(hitDieModifier));
				if(shouldHeal)
					player.setHealth(player.getMaxHealth());
			}
		}
	}
	
	/** Manage air for creatures that breathe water and/or don't breathe air */
	public void handleAir(boolean breatheAir, boolean breatheWater, LivingEntity entity)
	{
		boolean isPlayer = false;
		boolean isInvulnerablePlayer = false;
		if(entity.getType() == EntityType.PLAYER)
		{
			isPlayer = true;
			isInvulnerablePlayer = ((PlayerEntity)entity).abilities.disableDamage;
		}
		
		this.overridingAir = false;
		if(getAir() > entity.getMaxAir())
			setAir(entity.getMaxAir());
		if(!breatheAir && !breatheWater)
		{
			setAir(entity.getMaxAir());
			if(entity.getAir() < getAir())
				this.overridingAir = true;
		}
		else if(entity.isAlive() && !isInvulnerablePlayer)
		{
			boolean isInWater = entity.areEyesInFluid(FluidTags.WATER) && !entity.getEntityWorld().getBlockState(new BlockPos(entity.getPosX(), entity.getPosYEye(), entity.getPosZ())).isIn(Blocks.BUBBLE_COLUMN);
			
			// Prevents drowning due to water
			if(breatheWater)
				if(isInWater && getAir() < entity.getMaxAir())
				{
					this.overridingAir = true;
					setAir(determineNextAir(getAir(), entity));
				}
			
			// Causes drowning due to air
			if(!breatheAir)
				if(!(EffectUtils.canBreatheUnderwater(entity) || isInWater))
				{
					setAir(Math.min(entity.getMaxAir(), decreaseAirSupply(getAir(), entity)));
					if(getAir() == -20)
					{
						setAir(0);
						entity.attackEntityFrom(DamageSource.DROWN, 2.0F);
					}
					this.overridingAir = true;
				}
		}
		
		if(overrideAir() && isPlayer && !entity.getEntityWorld().isRemote && entity.getEntityWorld().getGameTime()%Reference.Values.TICKS_PER_MINUTE == 0)
			PacketHandler.sendTo((ServerPlayerEntity)entity, new PacketSyncAir(getAir()));
	}
	
	private static AttributeModifier makeModifier(double amount)
	{
		return new AttributeModifier(HEALTH_MODIFIER_UUID, "hit_die_modifier", amount, AttributeModifier.Operation.ADDITION);
	}
	
	public void markDirty()
	{
		this.dirty = true;
	}
	
	public static class Storage implements Capability.IStorage<LivingData>
	{
		public INBT writeNBT(Capability<LivingData> capability, LivingData instance, Direction side)
		{
			return instance.serializeNBT();
		}
		
		public void readNBT(Capability<LivingData> capability, LivingData instance, Direction side, INBT nbt)
		{
			if(nbt.getId() == 10)
				instance.deserializeNBT((CompoundNBT)nbt);
		}
	}
}
