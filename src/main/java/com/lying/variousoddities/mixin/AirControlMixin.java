package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.api.event.LivingBreathingEvent.LivingCanBreatheFluidEvent;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSyncAir;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.eventbus.api.Event.Result;

/**
 * Overrules the standard air supply management of LivingEntity to allow interaction with the ability system 
 * @author Remem
 *
 */
@Mixin(LivingEntity.class)
public class AirControlMixin
{
	private int cachedAir = 0;
	
	@Inject(method = "baseTick()V", at = @At("HEAD"))
	public void airControlHead(final CallbackInfo ci)
	{
		LivingEntity living = (LivingEntity)(Object)this;
		cachedAir = living.getAirSupply();
	}
	
	@Inject(method = "baseTick()V", at = @At("RETURN"))
	public void airControlReturn(final CallbackInfo ci)
	{
		LivingEntity living = (LivingEntity)(Object)this;
		LivingData data = LivingData.forEntity(living);
		if(data != null)
		{
			ActionSet actions = ActionSet.fromTypes(living, data.getTypes());
			living.setAirSupply(handleAir(actions.breathes(), living, cachedAir));
		}
	}
	
	/** Manage air for creatures that breathe */
	private int handleAir(boolean breathes, LivingEntity entity, int air)
	{
		if(entity.level.isClientSide())
			return air;
		
		boolean isPlayer = entity.getType() == EntityType.PLAYER;
		boolean isInvulnerablePlayer = isPlayer && (((Player)entity).getAbilities().invulnerable || !PlayerData.isPlayerNormalFunction(entity));
		
		int maxAir = entity.getMaxAirSupply();
		if(!breathes || air > maxAir || isInvulnerablePlayer)
			return maxAir;
		else if(entity.isAlive())
		{
			boolean hasSpecialBreathing = 
					MobEffectUtil.hasWaterBreathing(entity) ||
					entity.getLevel().getBlockState(new BlockPos(entity.getX(), entity.getEyeY(), entity.getZ())).is(Blocks.BUBBLE_COLUMN);
			
			if(canBreathe(entity))
			{
				if(air < maxAir)
					return determineNextAir(air, maxAir, entity);
			}
			else if(!hasSpecialBreathing)
			{
				if(air == -20)
				{
					entity.hurt(DamageSource.DROWN, 2.0F);
					return 0;
				}
				else
					return decreaseAirSupply(air, entity);
			}
		}
		
		if(isPlayer && !entity.getLevel().isClientSide() && entity.getLevel().getGameTime()%Reference.Values.TICKS_PER_MINUTE == 0)
			PacketHandler.sendTo((ServerPlayer)entity, new PacketSyncAir(air));
		
		return air;
	}
	
	/** 
	 * Returns true if the given entity is able to breathe based on the fluid contents of its eye level.<br>
	 * Similar to IForgeLivingEntity.canDrownInFluidType, but bypassing vanilla breathing lets us treat air as a fluid as well.
	 * @param entity
	 * @return
	 */
	private boolean canBreathe(LivingEntity entity)
	{
		FluidState stateAtEyes = entity.getLevel().getFluidState(entity.blockPosition().offset(0, entity.getEyeHeight(), 0));
		LivingCanBreatheFluidEvent event = new LivingCanBreatheFluidEvent(entity, entity.getLevel().getFluidState(entity.blockPosition().offset(0, entity.getEyeHeight(), 0)));
		MinecraftForge.EVENT_BUS.post(event);
		return event.getResult() == Result.ALLOW ? true : event.getResult() == Result.DENY ? false : !((IForgeLivingEntity)entity).canDrownInFluidType(stateAtEyes.getFluidType());
	}
	
	private int decreaseAirSupply(int air, LivingEntity entityIn)
	{
		int i = EnchantmentHelper.getRespiration(entityIn);
		return i > 0 && entityIn.getRandom().nextInt(i + 1) > 0 ? air : air - 1;
	}
	
	private int determineNextAir(int currentAir, int maxAir, LivingEntity entityIn)
	{
		return Math.min(currentAir + 4, maxAir);
	}
}
