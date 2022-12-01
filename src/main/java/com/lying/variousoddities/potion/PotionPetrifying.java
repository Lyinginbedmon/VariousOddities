package com.lying.variousoddities.potion;

import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketPetrifying;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PotionPetrifying extends PotionVO
{
	private static final UUID PETRIFYING_UUID = UUID.fromString("94b3271f-7c76-4230-88d7-f294ee6d4f7f");
	private static final BlockState BLOCK_STATE = Blocks.STONE.defaultBlockState();
	
	public PotionPetrifying(int colorIn)
	{
		super(MobEffectCategory.HARMFUL, colorIn);
		addAttributeModifier(Attributes.MOVEMENT_SPEED, PETRIFYING_UUID.toString(), -0.15D, Operation.MULTIPLY_TOTAL);
		addAttributeModifier(Attributes.FLYING_SPEED, PETRIFYING_UUID.toString(), -0.15D, Operation.MULTIPLY_TOTAL);
	}
	
	public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier)
	{
		return modifier.getAmount() * (double)(Math.max(0, 5 - amplifier) + 1);
	}
	
	public boolean isReady(int duration, int amplifier){ return duration == 1; }
	
	public void applyEffectTick(LivingEntity living, int amplifier)
	{
		if(!living.getLevel().isClientSide)
			PacketHandler.sendToNearby(living.getLevel(), living, new PacketPetrifying(living.getUUID(), amplifier));
		
		if(amplifier > 0)
			living.addEffect(getPetrifying(amplifier));
		else
		{
			MobEffectInstance petrified = new MobEffectInstance(VOMobEffects.PETRIFIED.get(), Reference.Values.TICKS_PER_DAY * 100, 0, false, false);
			petrified.setCurativeItems(Lists.newArrayList());
			living.addEffect(petrified);
		}
	}
	
	private MobEffectInstance getPetrifying(int amplifier)
	{
		MobEffectInstance petrifying = new MobEffectInstance(VOMobEffects.PETRIFYING.get(), Reference.Values.TICKS_PER_SECOND * 10, amplifier - 1, false, true);
		petrifying.setCurativeItems(Lists.newArrayList());
		return petrifying;
	}
	
	public static void doParticles(LivingEntity living, int amplifier)
	{
    	int newAmp = Math.max(0, 5 - amplifier);
    	Level world = living.getLevel();
		RandomSource rand = living.getRandom();
		for(int i=(newAmp * 15); i>0; i--)
		{
			double velX = (rand.nextDouble() - 0.5D) * 0.5D;
			double velY = (rand.nextDouble() - 0.5D) * 0.5D;
			double velZ = (rand.nextDouble() - 0.5D) * 0.5D;
			
			double posX = living.getX() + (rand.nextDouble() - 0.5D) * living.getBbWidth();
			double posY = living.getY() + rand.nextDouble() * living.getBbHeight();
			double posZ = living.getZ() + (rand.nextDouble() - 0.5D) * living.getBbWidth();
			world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, BLOCK_STATE), posX, posY, posZ, velX, velY, velZ);
		}
	}
}
