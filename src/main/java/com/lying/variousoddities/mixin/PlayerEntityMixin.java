package com.lying.variousoddities.mixin;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.world.savedata.ScentsManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin extends LivingEntityMixin
{
	@Shadow
	public void startFallFlying(){ }
	
	@Inject(method = "tick()V", at = @At("HEAD"))
	public void tick(final CallbackInfo ci)
	{
		PlayerEntity player = (PlayerEntity)(Object)this;
		PlayerData data = PlayerData.forPlayer(player);
		if(data != null)
			data.tick(player);
	}
	
	@Inject(method = "isPlayerFullyAsleep()Z", at = @At("HEAD"), cancellable = true)
	public void fullySleeping(final CallbackInfoReturnable<Boolean> ci)
	{
		if(PlayerData.isPlayerBodyAsleep((PlayerEntity)(Object)this))
			ci.setReturnValue(true);
	}
	
	@Inject(method = "shouldHeal()Z", at = @At("HEAD"), cancellable = true)
	public void shouldHeal(final CallbackInfoReturnable<Boolean> ci)
	{
		PlayerEntity player = (PlayerEntity)(Object)this;
		LivingData livingData = LivingData.forEntity(player);
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
		PlayerEntity player = (PlayerEntity)(Object)this;
		Abilities abilities = LivingData.forEntity(player).getAbilities();
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(player);
		if(abilityMap.containsKey(AbilityFlight.REGISTRY_NAME) && abilityMap.get(AbilityFlight.REGISTRY_NAME).isActive())
		{
			if(!player.isOnGround() && !player.isElytraFlying() && abilities.canBonusJump)
			{
				player.startFallFlying();
				ci.setReturnValue(true);
			}
		}
	}
	
	private int scentTimer = -1;
	private Vector3d prevScentPos = null;
	
	@Inject(method = "livingTick()V", at = @At("HEAD"), cancellable = true)
	public void livingTick(final CallbackInfo ci)
	{
		PlayerEntity player = (PlayerEntity)(Object)this;
		World world = player.getEntityWorld();
		handlePlayerScent(player, world);
	}
	
	private void handlePlayerScent(PlayerEntity player, World worldIn)
	{
		if(world.isRemote || player.isCreative() || player.isSpectator() || PlayerData.isPlayerSoulDetached(player))
			return;
		
		Random rand = world.rand;
		if(scentTimer < 0)
			resetScentTimer(rand);
		else if(--scentTimer == 0)
		{
			Vector3d pos = player.getPositionVec();
			if(prevScentPos == null)
				prevScentPos = pos;
			else
			{
				ScentsManager manager = ScentsManager.get(world);
				
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
		PlayerEntity player = (PlayerEntity)(Object)this;
		if(PlayerData.isPlayerSoulDetached(player))
			ci.cancel();
	}
	
	@Inject(method = "canTriggerWalking()Z", at = @At("HEAD"), cancellable = true)
	public void canTriggerWalking(final CallbackInfoReturnable<Boolean> ci)
	{
		PlayerEntity player = (PlayerEntity)(Object)this;
		if(PlayerData.isPlayerSoulDetached(player))
			ci.setReturnValue(false);
	}
	
	private void resetScentTimer(Random rand)
	{
		scentTimer = Reference.Values.TICKS_PER_SECOND + rand.nextInt(Reference.Values.TICKS_PER_SECOND);
	}
}
