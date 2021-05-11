package com.lying.variousoddities.capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeApplyEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeRemoveEvent;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncAir;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.types.TypeBus;
import com.lying.variousoddities.world.savedata.TypesManager;

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
import net.minecraft.potion.EffectUtils;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
	@CapabilityInject(LivingData.class)
	public static final Capability<LivingData> CAPABILITY = null;
	public static final ResourceLocation IDENTIFIER = new ResourceLocation(Reference.ModInfo.MOD_ID, "living_data");
	
	private final LazyOptional<LivingData> handler;
	
	private LivingEntity entity = null;
	
	private List<EnumCreatureType> prevTypes = new ArrayList<>();
	private ResourceLocation originDimension = null;
	
//	private Species species = null;
//	private List<Template> templates = Lists.newArrayList();
	
	private Abilities abilities = new Abilities();
	
	private int air = Reference.Values.TICKS_PER_DAY;
	private boolean overridingAir = false;
	
	public boolean checkingFoodRegen = false;
	
	private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("1f1a65b2-2041-44d9-af77-e13166a2a5b3");
	
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
			if(this.originDimension != null)
				compound.putString("HomeDim", this.originDimension.toString());
			
			compound.putInt("Air", this.air);
			
			ListNBT types = new ListNBT();
			for(EnumCreatureType type : prevTypes)
				types.add(StringNBT.valueOf(type.getString()));
			compound.put("Types", types);
			
			compound.put("Abilities", this.abilities.serializeNBT());
		return compound;
	}
	
	public void deserializeNBT(CompoundNBT nbt)
	{
		if(nbt.contains("HomeDim", 8))
			this.originDimension = new ResourceLocation(nbt.getString("HomeDim"));
		
		this.air = nbt.getInt("Air");
		
		ListNBT types = nbt.getList("Types", 8);
		prevTypes.clear();
		for(int i=0; i<types.size(); i++)
			prevTypes.add(EnumCreatureType.fromName(types.getString(i)));
		
		this.abilities.deserializeNBT(nbt.getCompound("Abilities"));
	}
	
	public ResourceLocation getHomeDimension(){ return this.originDimension; }
	public void setHomeDimension(ResourceLocation dimension){ this.originDimension = dimension; }
	
	public Abilities getAbilities(){ return this.abilities; }
	
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
	
	public void tick(LivingEntity entity)
	{
		if(this.originDimension == null)
		{
			// TODO Check default home dimension registry for creature before setting to current dim
			this.originDimension = entity.getEntityWorld().getDimensionKey().getLocation();
		}
		
		if(!TypeBus.shouldFire()) return;
		
		TypesManager manager = TypesManager.get(entity.getEntityWorld());
		List<EnumCreatureType> typesNow = manager.getMobTypes(entity);
		boolean isRemote = entity.getEntityWorld().isRemote;
		
		List<EnumCreatureType> typesNew = new ArrayList<>();
		typesNew.addAll(typesNow);
		typesNew.removeAll(prevTypes);
		for(EnumCreatureType type : typesNew)
			MinecraftForge.EVENT_BUS.post(new TypeApplyEvent(entity, type));
		
		this.prevTypes.removeAll(typesNow);
		for(EnumCreatureType type : prevTypes)
			MinecraftForge.EVENT_BUS.post(new TypeRemoveEvent(entity, type));
		
		this.prevTypes.clear();
		this.prevTypes.addAll(typesNow);
		
		for(EnumCreatureType type : this.prevTypes)
			type.getHandler().onLivingTick(entity);
		
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
			if(isPlayer && !isRemote)
			{
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
                ServerStatisticsManager statManager = serverPlayer.getStats();
                statManager.setValue(serverPlayer, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 0);
			}
		}
		
		handleAir(actions.breathesAir(), actions.breathesWater(), entity);
		
		abilities.tick();
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
