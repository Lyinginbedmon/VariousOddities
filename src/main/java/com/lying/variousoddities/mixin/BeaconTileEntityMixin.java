package com.lying.variousoddities.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.lying.variousoddities.init.VOTileEntities;
import com.lying.variousoddities.tileentity.TileEntityPhylactery;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BeaconTileEntity.class)
public class BeaconTileEntityMixin
{
	@Shadow
	private int levels;
	@Shadow
	private Effect primaryEffect;
	@Shadow
	private Effect secondaryEffect;
	
	@Inject(method = "addEffectsToPlayers()V", at = @At("HEAD"))
	private void addEffects(final CallbackInfo ci)
	{
		BeaconTileEntity tile = (BeaconTileEntity)(Object)this;
		World world = tile.getWorld();
		if(primaryEffect == null || world == null || world.isRemote)
			return;
		
		double range = (double)(levels * 10 + 10);
		int amplifier = levels >= 4 && primaryEffect == secondaryEffect ? 1 : 0;
		int duration = (9 + levels * 2) * 20;
		AxisAlignedBB area = new AxisAlignedBB(tile.getPos()).grow(range).expand(0D, tile.getWorld().getHeight(), 0D);
		List<TileEntityPhylactery> phylacteries = Lists.newArrayList();
		world.loadedTileEntityList.forEach((tileEntity) -> 
		{
			BlockPos pos = tileEntity.getPos();
			if(tileEntity.getType() == VOTileEntities.PHYLACTERY && area.contains(pos.getX(), pos.getY(), pos.getZ()))
				phylacteries.add((TileEntityPhylactery)tileEntity);
		});
		
		boolean hasSecondary = levels >= 4 && primaryEffect != secondaryEffect && secondaryEffect != null;
		for(TileEntityPhylactery phylactery : phylacteries)
		{
			LivingEntity owner = phylactery.getOwner();
			if(owner == null || owner.getType() == EntityType.PLAYER && area.contains(owner.getPositionVec()) || owner.getEntityWorld().getDimensionKey() != world.getDimensionKey())
				continue;
			
			owner.addPotionEffect(new EffectInstance(primaryEffect, duration, amplifier, true, true));
			if(hasSecondary)
				owner.addPotionEffect(new EffectInstance(secondaryEffect, duration, amplifier, true, true));
		}
	}
}
