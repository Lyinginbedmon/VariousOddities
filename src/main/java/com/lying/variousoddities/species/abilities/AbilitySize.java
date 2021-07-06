package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilitySize extends Ability
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "size");
	
	private Size sizeClass;
	private boolean sizeNeedsRecalc = true;
	
	private float scale = -1F;
	
	public AbilitySize(Size sizeIn)
	{
		super(REGISTRY_NAME);
		this.sizeClass = sizeIn;
	}
	
	public AbilitySize(Size sizeIn, float scaleIn)
	{
		this(sizeIn);
		this.scale = MathHelper.clamp(scaleIn, 0F, 1F);
	}
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability." + Reference.ModInfo.MOD_ID + ".size", sizeClass.translate());
	}
	
	public Type getType(){ return Type.UTILITY; }
	protected Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putString("Size", this.sizeClass.getString());
		if(this.scale >= 0F)
			compound.putFloat("Scale", this.scale);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.sizeClass = Size.fromString(compound.getString("Size"));
		if(compound.contains("Scale", 5))
			this.scale = MathHelper.clamp(compound.getFloat("Scale"), 0F, 1F);
		this.sizeNeedsRecalc = true;
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::updateSize);
		bus.addListener(this::handleSize);
	}
	
	public float getScale()
	{
		return this.scale >= 0F ? this.sizeClass.scale(this.scale) : this.sizeClass.scale();
	}
	
	public void updateSize(LivingUpdateEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		
		LivingData data = LivingData.forEntity(living);
		if(data == null || !AbilityRegistry.hasAbility(living, REGISTRY_NAME))
			return;
		
		AbilitySize size = (AbilitySize)AbilityRegistry.getAbilityByName(living, REGISTRY_NAME);
		if(size == null)
			return;
		
		if(size.sizeNeedsRecalc)
		{
			living.recalculateSize();
			size.sizeNeedsRecalc = false;
		}
	}
	
	public void handleSize(EntityEvent.Size event)
	{
		if(event.getEntity() instanceof LivingEntity)
		{
			LivingEntity living = (LivingEntity)event.getEntity();
			
			LivingData data = LivingData.forEntity(living);
			if(data == null || !AbilityRegistry.hasAbility(living, REGISTRY_NAME))
				return;
			
			AbilitySize size = (AbilitySize)AbilityRegistry.getAbilityByName(living, REGISTRY_NAME);
			if(size == null)
				return;
			
			EntitySize oldSize = event.getOldSize();
			float scale = size.getScale();
			event.setNewSize(EntitySize.fixed(oldSize.width * scale, oldSize.height * scale));
			event.setNewEyeHeight(event.getOldEyeHeight() * scale);
		}
	}
	
	public static enum Size implements IStringSerializable
	{
		FINE(0.05F, 0.017F, 0.083F),
		DIMINUTIVE(0.125F, 0.083F, 0.167F),
		TINY(0.25F, 0.167F, 0.333F),
		SMALL(0.5F, 0.333F, 0.667F),
		MEDIUM(1F, 0.667F, 1.333F),
		LARGE(2F, 1.333F, 2.667F),
		HUGE(4F, 2.667F, 5.333F),
		GARGANTUAN(8F, 5.333F, 10.667F),
		COLOSSAL(16F, 10.667F, 21.333F);
		
		private final float scale;
		private final float scaleMin, scaleMax;
		
		private Size(float scaleIn, float minIn, float maxIn)
		{
			this.scale = scaleIn;
			this.scaleMin = minIn;
			this.scaleMax = maxIn;
		}
		
		public String getString()
		{
			return this.name().toLowerCase();
		}
		
		public ITextComponent translate()
		{
			return new TranslationTextComponent("enum.varodd.size." + getString());
		}
		
		public float scale()
		{
			return this.scale;
		}
		
		public float scale(float scaleIn)
		{
			return this.scaleMin + (this.scaleMax - this.scaleMin) * scaleIn;
		}
		
		public static Size fromString(String nameIn)
		{
			for (Size size : values())
				if (size.getString().equalsIgnoreCase(nameIn))
					return size;
			return MEDIUM;
		}
	}

	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			Size size = Size.fromString(compound.getString("Size"));
			return compound.contains("Scale", 5) && compound.getFloat("Scale") >= 0F ? new AbilitySize(size, compound.getFloat("Scale")) : new AbilitySize(size);
		}
	}
}
