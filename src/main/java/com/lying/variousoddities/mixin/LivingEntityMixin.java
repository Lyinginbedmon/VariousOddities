package com.lying.variousoddities.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.types.TypeHandler;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(LivingEntity.class)
public class LivingEntityMixin extends EntityMixin
{
	@Shadow
	public float getHealth(){ return 0F; }
	
	@Shadow
	public float getMaxHealth(){ return 0F; }
	
	@Inject(method = "baseTick", at = @At("TAIL"))
	public void baseTick(CallbackInfo callbackInfo)
	{
		LazyOptional<LivingData> livingCap = this.getCapability(LivingData.CAPABILITY, null);
		if(livingCap.isPresent())
		{
			LivingData livingData = livingCap.orElseThrow(() -> new RuntimeException("No living data found in mixin"));
			
			livingData.tick((LivingEntity)(Object)this);
			if(livingData.overrideAir())
				this.setAir(livingData.getAir());
		}
	}
	
	@Inject(method = "isPotionApplicable", at = @At("HEAD"), cancellable = true)
	public void isPotionApplicable(EffectInstance effectInstanceIn, final CallbackInfoReturnable<Boolean> ci)
	{
		LivingEntity entity = (LivingEntity)(Object)this;
		TypesManager manager = TypesManager.get(entity.getEntityWorld());
		boolean isParalysis = VOPotions.isParalysisEffect(effectInstanceIn);
		for(EnumCreatureType mobType : manager.getMobTypes(entity))
		{
			TypeHandler handler = mobType.getHandler();
			if(effectInstanceIn.getPotion() == Effects.POISON && !handler.canBePoisoned())
				ci.setReturnValue(false);
			else if(isParalysis && !handler.canBeParalysed())
				ci.setReturnValue(false);
		}
	}
	
	@Inject(method = "isEntityInsideOpaqueBlock", at = @At("HEAD"), cancellable = true)
	public void isEntityInsideOpaqueBlock(final CallbackInfoReturnable<Boolean> ci)
	{
		if(EnumCreatureType.canPhase(world, null, (LivingEntity)(Object)this))
			ci.setReturnValue(false);
	}
	
	@Inject(method = "applyEntityCollision(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	public void applyEntityCollision(Entity entityIn, final CallbackInfo ci)
	{
		TypesManager manager = TypesManager.get(entityIn.getEntityWorld());
		Entity host = (Entity)(Object)this;
		if(host instanceof LivingEntity && manager.isMobOfType((LivingEntity)host, EnumCreatureType.INCORPOREAL))
			ci.cancel();
		else if(entityIn instanceof LivingEntity && manager.isMobOfType((LivingEntity)entityIn, EnumCreatureType.INCORPOREAL))
			ci.cancel();		
	}
}
