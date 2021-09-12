package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityLightSensitivity extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "light_sensitivity");
	
	private int lightLimit;
	
	public AbilityLightSensitivity()
	{
		this(8);
	}
	
	public AbilityLightSensitivity(int limit)
	{
		super(REGISTRY_NAME);
		this.lightLimit = limit;
	}
	
	public Type getType(){ return Type.WEAKNESS; }
	
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		compound.putInt("Light", this.lightLimit);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		this.lightLimit = compound.contains("Light") ? compound.getInt("Light") : 8;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::applySensitivity);
	}
	
	public void applySensitivity(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(entity, REGISTRY_NAME))
		{
			World world = entity.getEntityWorld();
			BlockPos eyePos = entity.getPosition().add(0D, entity.getEyeHeight(), 0D);
			int block = world.getLightFor(LightType.BLOCK, eyePos);
			int sky = getSkyLight(eyePos, world);
			
			int light = Math.max(block, sky);
			if(light > ((AbilityLightSensitivity)AbilityRegistry.getAbilityByName(entity, REGISTRY_NAME)).lightLimit)
				entity.addPotionEffect(new EffectInstance(VOPotions.DAZZLED, Reference.Values.TICKS_PER_SECOND * 6, 0, false, false));
		}
	}
	
	private static int getSkyLight(BlockPos pos, World world)
	{
		if(!world.getDimensionType().hasSkyLight())
			return 0;
		
		int light = world.getLightFor(LightType.SKY, pos) - world.getSkylightSubtracted();
		if(light > 0)
		{
			float sunAngle = world.getCelestialAngleRadians(1.0F);
		    float f1 = sunAngle < (float)Math.PI ? 0.0F : ((float)Math.PI * 2F);
		    sunAngle = sunAngle + (f1 - sunAngle) * 0.2F;
		    light = Math.round((float)light * MathHelper.cos(sunAngle));
		}
		
		return MathHelper.clamp(light, 0, 15);
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			return compound.contains("Light") ? new AbilityLightSensitivity(compound.getInt("Light")) : new AbilityLightSensitivity();
		}
	}
}
