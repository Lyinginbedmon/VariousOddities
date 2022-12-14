package com.lying.variousoddities.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.lying.variousoddities.tileentity.TileEntityPhylactery;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(BeaconBlockEntity.class)
public class BeaconTileEntityMixin
{
	@Shadow
	private int levels;
	@Shadow
	private MobEffect primaryPower;
	@Shadow
	private MobEffect secondaryPower;
	
	@Inject(method = "addEffectsToPlayers()V", at = @At("HEAD"))
	private void addEffects(final CallbackInfo ci)
	{
		BeaconBlockEntity tile = (BeaconBlockEntity)(Object)this;
		Level world = tile.getLevel();
		if(primaryPower == null || world == null || world.isClientSide)
			return;
		
		double range = (double)(levels * 10 + 10);
		int amplifier = levels >= 4 && primaryPower == secondaryPower ? 1 : 0;
		int duration = (9 + levels * 2) * 20;
		AABB area = new AABB(tile.getBlockPos()).inflate(range).inflate(0D, tile.getLevel().getHeight(), 0D);
		List<TileEntityPhylactery> phylacteries = Lists.newArrayList();
//		world.loadedBlockEntityList.forEach((tileEntity) -> // FIXME Requires accessor in Level
//		{
//			BlockPos pos = tileEntity.getPos();
//			if(tileEntity.getType() == VOTileEntities.PHYLACTERY && area.contains(pos.getX(), pos.getY(), pos.getZ()))
//				phylacteries.add((TileEntityPhylactery)tileEntity);
//		});
		
		boolean hasSecondary = levels >= 4 && primaryPower != secondaryPower && secondaryPower != null;
		for(TileEntityPhylactery phylactery : phylacteries)
		{
			LivingEntity owner = phylactery.getOwner();
			if(owner == null || owner.getType() == EntityType.PLAYER && area.contains(owner.position()) || owner.getLevel().dimension() != world.dimension())
				continue;
			
			owner.addEffect(new MobEffectInstance(primaryPower, duration, amplifier, true, true));
			if(hasSecondary)
				owner.addEffect(new MobEffectInstance(secondaryPower, duration, amplifier, true, true));
		}
	}
}
