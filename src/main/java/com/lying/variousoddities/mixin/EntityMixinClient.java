package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.entity.AbstractBody;
import com.lying.variousoddities.species.abilities.AbilityBlindsight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityTremorsense;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Entity.class)
public class EntityMixinClient
{
	@Inject(method = "isGlowing()Z", at = @At("HEAD"), cancellable = true)
	public void isGlowing(final CallbackInfoReturnable<Boolean> ci)
	{
		Entity ent = (Entity)(Object)this;
		
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && ent != player)
		{
			if(PlayerData.isPlayerSoulBound(player) && PlayerData.isPlayerBody(player, ent))
				ci.setReturnValue(true);
			
			double dist = Math.sqrt(player.getDistanceSq(ent));
			if(AbilityRegistry.hasAbility(player, AbilityTremorsense.REGISTRY_NAME))
			{
				AbilityTremorsense tremorsense = (AbilityTremorsense)AbilityRegistry.getAbilityByName(player, AbilityTremorsense.REGISTRY_NAME);
				if(tremorsense != null && tremorsense.isInRange(dist) && tremorsense.testEntity(ent, player))
					ci.setReturnValue(true);
			}
			
			if(AbilityRegistry.hasAbility(player, AbilityBlindsight.REGISTRY_NAME))
			{
				AbilityBlindsight blindsight = (AbilityBlindsight)AbilityRegistry.getAbilityByName(player, AbilityBlindsight.REGISTRY_NAME);
				if(blindsight != null && blindsight.isInRange(dist) && blindsight.testEntity(ent, player))
					ci.setReturnValue(true);
			}
		}
	}
	
	@Inject(method = "tick()V", at = @At("TAIL"))
	public void tick(final CallbackInfo ci)
	{
		Entity ent = (Entity)(Object)this;
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && ent != player)
			if(PlayerData.isPlayerSoulBound(player) && PlayerData.isPlayerBody(player, ent) && !VOHelper.isCreativeOrSpectator(player))
				AbstractBody.moveWithinRangeOf(ent, player, PlayerData.forPlayer(player).getSoulCondition().getWanderRange());
			else if(PlayerData.isPlayerPossessing(player, ent))
			{
//				Entity.IMoveCallback callback = Entity::setPosition;
//				callback.accept(player, ent.getPosX(), ent.getPosY(), ent.getPosZ());
			}
	}
}