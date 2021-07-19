package com.lying.variousoddities.potion;

import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketPetrifying;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.world.World;

public class PotionPetrifying extends PotionVO
{
	private static final UUID PETRIFYING_UUID = UUID.fromString("94b3271f-7c76-4230-88d7-f294ee6d4f7f");
	private static final BlockState BLOCK_STATE = Blocks.STONE.getDefaultState();
	
	public PotionPetrifying(int colorIn)
	{
		super("petrifying", EffectType.HARMFUL, colorIn);
		addAttributesModifier(Attributes.MOVEMENT_SPEED, PETRIFYING_UUID.toString(), -0.15D, Operation.MULTIPLY_TOTAL);
		addAttributesModifier(Attributes.FLYING_SPEED, PETRIFYING_UUID.toString(), -0.15D, Operation.MULTIPLY_TOTAL);
	}
	
	public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier)
	{
		return modifier.getAmount() * (double)(Math.max(0, 5 - amplifier) + 1);
	}
	
	public boolean isReady(int duration, int amplifier){ return duration == 1; }
	
	public void performEffect(LivingEntity living, int amplifier)
	{
		if(!living.getEntityWorld().isRemote)
			PacketHandler.sendToNearby(living.getEntityWorld(), living, new PacketPetrifying(living.getUniqueID(), amplifier));
		
		if(amplifier > 0)
			living.addPotionEffect(getPetrifying(amplifier));
		else
		{
			EffectInstance petrified = new EffectInstance(VOPotions.PETRIFIED, Reference.Values.TICKS_PER_DAY * 100, 0, false, false);
			petrified.setCurativeItems(Lists.newArrayList());
			living.addPotionEffect(petrified);
		}
	}
	
	private EffectInstance getPetrifying(int amplifier)
	{
		EffectInstance petrifying = new EffectInstance(VOPotions.PETRIFYING, Reference.Values.TICKS_PER_SECOND * 10, amplifier - 1, false, true);
		petrifying.setCurativeItems(Lists.newArrayList());
		return petrifying;
	}
	
	public static void doParticles(LivingEntity living, int amplifier)
	{
    	int newAmp = Math.max(0, 5 - amplifier);
    	World world = living.getEntityWorld();
		Random rand = living.getRNG();
		for(int i=(newAmp * 15); i>0; i--)
		{
			double velX = (rand.nextDouble() - 0.5D) * 0.5D;
			double velY = (rand.nextDouble() - 0.5D) * 0.5D;
			double velZ = (rand.nextDouble() - 0.5D) * 0.5D;
			
			double posX = living.getPosX() + (rand.nextDouble() - 0.5D) * living.getWidth();
			double posY = living.getPosY() + rand.nextDouble() * living.getHeight();
			double posZ = living.getPosZ() + (rand.nextDouble() - 0.5D) * living.getWidth();
			world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, BLOCK_STATE), posX, posY, posZ, velX, velY, velZ);
		}
	}
}
