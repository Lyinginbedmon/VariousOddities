package com.lying.variousoddities.capabilities;

import java.util.ArrayList;
import java.util.List;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeApplyEvent;
import com.lying.variousoddities.api.event.CreatureTypeEvent.TypeRemoveEvent;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
	
	private List<EnumCreatureType> prevTypes = new ArrayList<>();
	private int air = Reference.Values.TICKS_PER_DAY;
	private boolean overridingAir = false;
	
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
	
	public static LivingData forEntity(LivingEntity entity) throws RuntimeException
	{
		return entity.getCapability(CAPABILITY).orElseThrow(() -> new RuntimeException("No living data found for "+entity.getName()));
	}
	
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return CAPABILITY.orEmpty(cap, this.handler);
	}
	
	public CompoundNBT serializeNBT()
	{
		CompoundNBT compound = new CompoundNBT();
			compound.putInt("Air", this.air);
			
			ListNBT types = new ListNBT();
			for(EnumCreatureType type : prevTypes)
				types.add(StringNBT.valueOf(type.getSimpleName()));
			compound.put("Types", types);
		return compound;
	}
	
	public void deserializeNBT(CompoundNBT nbt)
	{
		this.air = nbt.getInt("Air");
		
		ListNBT types = nbt.getList("Types", 8);
		prevTypes.clear();
		for(int i=0; i<types.size(); i++)
			prevTypes.add(EnumCreatureType.fromName(types.getString(i)));
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
	
	public void tick(LivingEntity entity)
	{
		TypesManager manager = TypesManager.get(entity.getEntityWorld());
		List<EnumCreatureType> typesNow = manager.getMobTypes(entity);
		boolean isRemote = entity.getEntityWorld().isRemote;
		
		for(EnumCreatureType type : typesNow)
			if(!this.prevTypes.contains(type))
				MinecraftForge.EVENT_BUS.post(new TypeApplyEvent(entity, type));
		
		for(EnumCreatureType type : prevTypes)
			if(!typesNow.contains(type))
				MinecraftForge.EVENT_BUS.post(new TypeRemoveEvent(entity, type));
		
		this.prevTypes.clear();
		this.prevTypes.addAll(typesNow);
		
		boolean isPlayer = false;
		boolean isInvulnerablePlayer = false;
		PlayerEntity player = null;
		if(entity.getType() == EntityType.PLAYER)
		{
			isPlayer = true;
			player = (PlayerEntity)entity;
			isInvulnerablePlayer = player.abilities.disableDamage;
		}
		
		ActionSet actions = ActionSet.fromTypes(this.prevTypes);
		if(!actions.sleeps())
		{
			if(isPlayer && !isRemote)
			{
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
                ServerStatisticsManager statManager = serverPlayer.getStats();
                statManager.setValue(serverPlayer, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 0);
			}
		}
		
		this.overridingAir = false;
		if(getAir() > entity.getMaxAir())
			setAir(entity.getMaxAir());
		if(!actions.breathes())
		{
			setAir(entity.getMaxAir());
			if(entity.getAir() < getAir())
				this.overridingAir = true;
		}
		else if(entity.isAlive() && !isInvulnerablePlayer)
		{
			boolean isInWater = entity.areEyesInFluid(FluidTags.WATER) && !entity.getEntityWorld().getBlockState(new BlockPos(entity.getPosX(), entity.getPosYEye(), entity.getPosZ())).isIn(Blocks.BUBBLE_COLUMN);
			
			// Prevents drowning due to water
			if(actions.breathesWater())
				if(isInWater && getAir() < entity.getMaxAir())
				{
					this.overridingAir = true;
					setAir(determineNextAir(getAir(), entity));
				}
			
			// Causes drowning due to air
			if(!actions.breathesAir())
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
		
		if(!actions.regenerates())
		{
			if(isPlayer && player.getFoodStats().getFoodLevel() > 17)
					player.getFoodStats().setFoodLevel(17);
		}
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
