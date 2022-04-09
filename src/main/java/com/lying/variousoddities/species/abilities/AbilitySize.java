package com.lying.variousoddities.species.abilities;

import java.util.UUID;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilitySize extends AbilityModifier
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "size");
	private static final UUID SIZE_MODIFIER = UUID.fromString("3e3cf3f2-7d4f-41ce-91de-8557f02b2b91");
	
	public static final Ability MEDIUM = new AbilitySize(Size.MEDIUM).setTemporary();
	
	private Size sizeClass;
	
	private float scale = -1F;
	
	public AbilitySize(Size sizeIn)
	{
		super(REGISTRY_NAME, sizeIn.baseScale());
		this.sizeClass = sizeIn;
	}
	
	public AbilitySize(Size sizeIn, float scaleIn)
	{
		this(sizeIn);
		this.scale = MathHelper.clamp(scaleIn, 0F, 1F);
	}
	
	public int compare(Ability abilityIn)
	{
		AbilitySize size = (AbilitySize)abilityIn;
		return size.getScale() < getScale() ? 1 : size.getScale() > getScale() ? -1 : 0;
	}
	
	public boolean displayInSpecies(){ return sizeClass != Size.MEDIUM; }
	
	public ITextComponent translatedName()
	{
		return new TranslationTextComponent("ability." + Reference.ModInfo.MOD_ID + ".size", sizeClass.translate());
	}
	
	public ITextComponent description()
	{
		switch(sizeClass)
		{
			case COLOSSAL:
			case GARGANTUAN:
			case HUGE:
			case LARGE:
				return new TranslationTextComponent("ability.varodd:size.big");
			case FINE:
			case DIMINUTIVE:
			case TINY:
			case SMALL:
				return new TranslationTextComponent("ability.varodd:size.small");
			case MEDIUM:
			default:
				return new TranslationTextComponent("ability.varodd:size.normal");
		}
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
	}
	
	public void addListeners(IEventBus bus)
	{
		super.addListeners(bus);
		bus.addListener(this::handleSize);
	}
	
	public float getScale()
	{
		return this.scale >= 0F ? this.sizeClass.scale(this.scale) : this.sizeClass.baseScale();
	}
	
	public void applyModifier(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		if(entity.getType() != EntityType.PLAYER)
			return;
		
		ModifiableAttributeInstance attribute = entity.getAttribute(ForgeMod.REACH_DISTANCE.get());
		if(attribute == null)
			return;
		
		if(AbilityRegistry.hasAbility(entity, getMapName()))
		{
			AbilitySize size = (AbilitySize)AbilityRegistry.getAbilityByName(entity, getMapName());
			double amount = size.sizeClass.baseScale() - 1;
			
			AttributeModifier modifier = attribute.getModifier(SIZE_MODIFIER);
			if(modifier != null && modifier.getAmount() != amount)
			{
				attribute.removeModifier(SIZE_MODIFIER);
				modifier = null;
			}
			
			if(modifier == null)
			{
				modifier = new AttributeModifier(SIZE_MODIFIER, "size_modifier", amount, Operation.MULTIPLY_BASE);
				attribute.applyPersistentModifier(modifier);
			}
		}
		else if(attribute.getModifier(SIZE_MODIFIER) != null)
			attribute.removeModifier(SIZE_MODIFIER);
	}
	
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
			
			if(!event.getEntity().isAddedToWorld())
				return;
			
			float scale = size.getScale();
			EntitySize baseSize = event.getNewSize();
			event.setNewSize(EntitySize.fixed(baseSize.width * scale, baseSize.height * scale));
			event.setNewEyeHeight(event.getNewEyeHeight() * scale);
		}
	}
	
	public void onAbilityAdded(LivingEntity entity)
	{
		entity.recalculateSize();
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
