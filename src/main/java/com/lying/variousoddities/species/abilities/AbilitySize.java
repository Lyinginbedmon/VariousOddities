package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
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
	
	// FIXME Bounding box of resized creatures renders with twice the rescaling amount
	
	public float getScale()
	{
		return this.scale >= 0F ? this.sizeClass.scale(this.scale) : this.sizeClass.baseScale();
	}
	
	/** Ensures that size is recalculated after initial application as well as when NBT data is edited */
	public void updateSize(LivingUpdateEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		if(living.getType() != EntityType.PLAYER)
			return;
		
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
	
	/** Applies modifier to size when recalculated */
	public void handleSize(EntityEvent.Size event)
	{
		if(event.getEntity() instanceof LivingEntity && event.getEntity().getType() == EntityType.PLAYER)
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
	
	public void onAbilityRemoved(LivingEntity entity)
	{
		entity.recalculateSize();
	}
	
	public static enum Size implements IStringSerializable
	{
		FINE(0.25F, 0.1F, 0.25F),
		DIMINUTIVE(0.35F, 0.26F, 0.45F),
		TINY(1F, 0.55F, 1.1F),
		SMALL(1.6F, 1.15F, 1.6F),
		MEDIUM(1.8F, 1.7F, 2F),
		LARGE(2.5F, 2.1F, 3F),
		HUGE(4F, 3.1F, 5F),
		GARGANTUAN(7F, 5.1F, 9F),
		COLOSSAL(12F, 9.1F, 16F);
		
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
		
		public float baseScale()
		{
			return this.scale / 1.8F;
		}
		
		public float scale(float scaleIn)
		{
			return (this.scaleMin + (this.scaleMax - this.scaleMin) * scaleIn) / 1.8F;
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
