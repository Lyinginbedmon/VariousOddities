package com.lying.variousoddities.mixin;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.world.savedata.ScentsManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Player.class)
public class PlayerEntityMixin extends LivingEntityMixin
{
	@Shadow
	public void startFallFlying(){ }
	
	@Inject(method = "tick()V", at = @At("HEAD"))
	public void tick(final CallbackInfo ci)
	{
		Player player = (Player)(Object)this;
		PlayerData data = PlayerData.getCapability(player);
		if(data != null)
			data.tick(player);
	}
	
	@Inject(method = "isPlayerFullyAsleep()Z", at = @At("HEAD"), cancellable = true)
	public void fullySleeping(final CallbackInfoReturnable<Boolean> ci)
	{
		if(PlayerData.isPlayerBodyAsleep((Player)(Object)this))
			ci.setReturnValue(true);
	}
	
	@Inject(method = "shouldHeal()Z", at = @At("HEAD"), cancellable = true)
	public void shouldHeal(final CallbackInfoReturnable<Boolean> ci)
	{
		Player player = (Player)(Object)this;
		LivingData livingData = LivingData.getCapability(player);
		if(livingData != null && livingData.checkingFoodRegen)
		{
			ActionSet actions = ActionSet.fromTypes(player, EnumCreatureType.getCreatureTypes(player));
			if(!actions.regenerates())
				ci.setReturnValue(false);
			
			livingData.checkingFoodRegen = false;
		}
		
		if(PlayerData.isPlayerBodyDead(player))
			ci.setReturnValue(false);
	}
	
	@Inject(method = "tryToStartFallFlying()Z", at = @At("HEAD"), cancellable = true)
	public void startElytraFlying(final CallbackInfoReturnable<Boolean> ci)
	{
		Player player = (Player)(Object)this;
		AbilityData abilities = AbilityData.getCapability(player);
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(player);
		ResourceLocation flightKey = AbilityRegistry.getClassRegistryKey(AbilityFlight.class).location();
		if(abilityMap.containsKey(flightKey) && abilityMap.get(flightKey).isActive())
		{
			if(!player.isOnGround() && !player.isFallFlying() && abilities.canBonusJump)
			{
				player.startFallFlying();
				ci.setReturnValue(true);
			}
		}
	}
	
	private int scentTimer = -1;
	private Vec3 prevScentPos = null;
	
	@Inject(method = "livingTick()V", at = @At("HEAD"), cancellable = true)
	public void livingTick(final CallbackInfo ci)
	{
		Player player = (Player)(Object)this;
		Level world = player.getLevel();
		handlePlayerScent(player, world);
	}
	
	private void handlePlayerScent(Player player, Level worldIn)
	{
		if(level.isClientSide || player.isCreative() || player.isSpectator() || PlayerData.isPlayerSoulDetached(player))
			return;
		
		RandomSource rand = level.random;
		if(scentTimer < 0)
			resetScentTimer(rand);
		else if(--scentTimer == 0)
		{
			Vec3 pos = player.position();
			if(prevScentPos == null)
				prevScentPos = pos;
			else
			{
				ScentsManager manager = ScentsManager.get(level);
				
				List<EnumCreatureType> types = EnumCreatureType.getCreatureTypes(player);
				types.removeIf(EnumCreatureType.IS_SUBTYPE);
				if(types.isEmpty()) return;
				
				EnumCreatureType supertype = types.get(0);
				manager.addScentMarker(pos, prevScentPos, supertype);
				resetScentTimer(rand);
				
				prevScentPos = pos;
			}
		}
		
	}
	
	@Inject(method = "collideWithPlayer(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	public void collideWithPlayer(Entity entityIn, final CallbackInfo ci)
	{
		Player player = (Player)(Object)this;
		if(PlayerData.isPlayerSoulDetached(player))
			ci.cancel();
	}
	
	@Inject(method = "canTriggerWalking()Z", at = @At("HEAD"), cancellable = true)
	public void canTriggerWalking(final CallbackInfoReturnable<Boolean> ci)
	{
		Player player = (Player)(Object)this;
		if(PlayerData.isPlayerSoulDetached(player))
			ci.setReturnValue(false);
	}
	
	private void resetScentTimer(RandomSource rand)
	{
		scentTimer = Reference.Values.TICKS_PER_SECOND + rand.nextInt(Reference.Values.TICKS_PER_SECOND);
	}
}
