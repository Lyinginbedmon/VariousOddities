package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.species.abilities.AbilityBlindsight;
import com.lying.variousoddities.species.abilities.AbilityRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
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
		if(player != null && ent != player && AbilityRegistry.hasAbility(player, AbilityBlindsight.REGISTRY_NAME))
		{
			AbilityBlindsight blindsight = (AbilityBlindsight)AbilityRegistry.getAbilityByName(player, AbilityBlindsight.REGISTRY_NAME);
			double dist = Math.sqrt(player.getDistanceSq(ent));
			
			if(blindsight.isInRange(dist))
			{
				Vector3d eyePos = new Vector3d(player.getPosX(), player.getPosYEye(), player.getPosZ());
				for(int i=5; i>0; i--)
				{
					Vector3d pos = new Vector3d(ent.getPosX(), ent.getPosY() + (double)i / 5 * ent.getHeight(), ent.getPosZ());
					if(player.getEntityWorld().rayTraceBlocks(new RayTraceContext(eyePos, pos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, player)).getType() == Type.MISS)
					{
						ci.setReturnValue(true);
						break;
					}
				}
			}
		}
	}
}