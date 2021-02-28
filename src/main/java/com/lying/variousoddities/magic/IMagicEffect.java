package com.lying.variousoddities.magic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.lying.variousoddities.api.entity.IMobSpellcaster;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.SpellManager.SpellData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

/**
 * A container class for a variety of magical effects used chiefly through WorldSavedDataSpells and BusSpells
 * @author Lying
 *
 */
public interface IMagicEffect
{
	public String getSimpleName();
	public default String getName(){ return "magic."+Reference.ModInfo.MOD_PREFIX+getSimpleName() + ".name"; }
	public default String getTranslatedName(){ return (new TranslationTextComponent(getName())).getUnformattedComponentText(); }
	
	/** The level of this spell, typically between 0 and 9 */
	public default int getLevel(){ return 0; }
	
	public List<EnumSpellProperty> getSpellProperties();
	
	public static float getXPToInscribe(IMagicEffect spell)
	{
		float spellLevel = Math.max(0.5F, spell == null ? 1 : spell.getLevel());
		return spellLevel * 4.444F;
	}
	
	/**
	 * Returns true if this particular spell requires certain items when inscribing
	 */
	public default boolean needsReagents()
	{
		for(ItemStack stack : getReagents()) if(!stack.isEmpty()) return true;
		return true;
	}
	
	/**
	 * Returns a list of additional items necessary when inscribing this spell
	 */
	public default NonNullList<ItemStack> getReagents()
	{
		return NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
	}
	
	/**
	 * Returns true if all necessary prerequisites to casting the spell by the given caster have been met.
	 * Used primarily by scrolls to inform the tooltip information.
	 * @param casterIn
	 * @param scrollIn
	 */
	public default boolean canCast(LivingEntity casterIn, ItemStack scrollIn)
	{
		if(getDurationType() == DurationType.CONCENTRATE && casterIn != null && casterIn.getEntityWorld() != null)
		{
			Map<ResourceLocation, List<SpellData>> activeOwnedSpells = SpellManager.get(casterIn.getEntityWorld()).getSpellsOwnedBy(casterIn);
			for(ResourceLocation dim : activeOwnedSpells.keySet())
				for(SpellData spell : activeOwnedSpells.get(dim))
					if(!spell.isPermanent() && spell.getSpell().getDurationType() == DurationType.CONCENTRATE)
						return false;
		}
		
		if(MagicEffects.isInsideAntiMagic(casterIn)) return false;
		
		for(Components component : getCastingComponents())
			if(!component.isValid(casterIn, this)) return false;
		
		int casterLevel = getCasterLevelFromEntity(casterIn);
		if(casterLevel > -1 && casterLevel < Spell.getMinCasterLevel(this)) return false;
		
		return getCastingState(casterIn, scrollIn) == EnumCastingError.CASTABLE;
	}
	
	/** 
	 * Returns true if this spell should spawn an EntitySpell when it is cast.<br>
	 * This is mostly a utility feature following the spell system overhaul. 
	 */
	public default boolean shouldSpawnEntity(){ return false; }
	
	/**
	 * Returns true if the given spell is presently affecting the given entity.<br>
	 * Usually only true for de/buff effects.
	 */
	public default boolean isAffectingEntity(SpellData dataIn, LivingEntity entityIn){ return false;}
	
	/**
	 * Returns TRUE if the spell is considered to be at its object position instead of at the position of an affected entity.<br>
	 * Usually FALSE for de/buff spells and spells with a PERSONAL range type.
	 */
	public default boolean activeFromObject(SpellData dataIn, World worldIn){ return getRangeType() != RangeType.PERSONAL; }
	
	/**
	 * Returns the caster level of players or IMobSpellcaster entities, or 0 if the caster is neither.
	 * @param casterIn
	 * @return
	 */
	public static int getCasterLevelFromEntity(Entity casterIn)
	{
		if(casterIn != null)
		{
//			if(casterIn instanceof PlayerEntity)
//				return VOPlayerData.getCasterLevel((PlayerEntity)casterIn);
			if(casterIn instanceof IMobSpellcaster)
				return ((IMobSpellcaster)casterIn).getCasterLevel();
		}
		
		return 0;
	}
	
	public default EnumCastingError getFullCastingState(LivingEntity casterIn, ItemStack scrollIn)
	{
		if(getDurationType() == DurationType.CONCENTRATE && casterIn != null && casterIn.getEntityWorld() != null)
		{
			Map<ResourceLocation, List<SpellData>> activeOwnedSpells = SpellManager.get(casterIn.getEntityWorld()).getSpellsOwnedBy(casterIn);
			for(ResourceLocation dim : activeOwnedSpells.keySet())
				for(SpellData spell : activeOwnedSpells.get(dim))
					if(!spell.isPermanent() && spell.getSpell().getDurationType() == DurationType.CONCENTRATE)
						return EnumCastingError.CONCENTRATING;
		}
		
		if(MagicEffects.isInsideAntiMagic(casterIn)) return EnumCastingError.ANTI_MAGIC;
		
		int casterLevel = getCasterLevelFromEntity(casterIn);
		if(casterLevel > -1 && casterLevel < Spell.getMinCasterLevel(this)) return EnumCastingError.CASTER_LEVEL;
		
		for(Components component : getCastingComponents())
			if(!component.isValid(casterIn, this))
				switch(component)
				{
					case FOCUS:		return EnumCastingError.NO_FOCUS;
					case MATERIAL:	return EnumCastingError.INGREDIENTS;
					case SOMATIC:	return EnumCastingError.NO_GESTURE;
					case VERBAL:	return EnumCastingError.NO_SPEECH;
				}
		
		return getCastingState(casterIn, scrollIn);
	}
	
	public default boolean itemMatchesFocus(ItemStack heldItem)
	{
		ItemStack focusIn = getFocus();
		return !heldItem.isEmpty() && heldItem.getItem() == focusIn.getItem();
	}
	
	/**
	 * Returns the contextual response to attempting to cast this spell, whether that be CASTABLE or an informative error
	 * @param casterIn
	 * @param scrollIn
	 */
	public default EnumCastingError getCastingState(LivingEntity casterIn, ItemStack scrollIn){ return EnumCastingError.CASTABLE; }
	
	public default List<Components> getCastingComponents(){ return Arrays.asList(Components.VERBAL, Components.SOMATIC); }
	public static String getFormattedComponents(List<Components> components)
	{
		List<String> componentNames = new ArrayList<String>();
		for(Components component : components) componentNames.add(component.translatedName());
		Collections.sort(componentNames);
		
		String output = "";
		for(String component : componentNames)
		{
			if(output.length() > 0) output += ", ";
			output += component;
		}
		return output;
	}
	
	public default ItemStack getFocus()
	{
		return getCastingComponents().contains(Components.FOCUS) ? getFocusItem() : ItemStack.EMPTY;
	}
	
	public default ItemStack getFocusItem(){ return new ItemStack(Items.DIAMOND); }
	
	public default NonNullList<ItemStack> getMaterialComponents(){ return NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY); }
	
	/** How long it takes to cast the spell, in ticks */
	public default int getCastingTime(){ return Spell.STANDARD_ACTION; }
	
	/** How long the spell lasts, in ticks */
	public default int getDuration(int casterLevel){ return Reference.Values.TICKS_PER_SECOND; }
	
	public default DurationType getDurationType(){ return DurationType.NORMAL; }
	
	public default boolean allowsSpellResistance(){ return false; }
	
	/** True if the spell can be ended prematurely by interacting with the spell object */
	public default boolean isDismissable(){ return false; }
	
	/** True if the spell can be dismissed even if it is or has been made permanent */
	public default boolean isAlwaysDismissable(){ return false; }
	
	/** True if the spell has variables that can be edited post-casting */
	public default boolean isEditable(){ return false; }
	
	/** Opens the corresponding UI for editing the variables of this spell */
	public default void edit(PlayerEntity playerIn, SpellData dataIn, World worldIn){ }
	
	/** True if the spell object should never be visible */
	public default boolean shouldHideEntity(){ return false; }
	
	/** Returns true if the spell can be made permanent, such as by Permanency */
	public default boolean canBePermanent(){ return false; }
	
	/** True if this spell performs differently when cast whilst sneaking */
	public default boolean hasAltFire(){ return false; }
	
	/** Returns a list of strings describing this spell */
	public default Collection<String> getDescription()
	{
		return new ArrayList<String>();
	}
	
	/** Determines where the EntitySpell entity is spawned */
	public default SpawnStyle getSpawnStyle(){ return SpawnStyle.FEET; }

	/** Sets the initial position and angles of the SpellData, used by the CUSTOM SpawnStyle */
	public default void setSpawnPosition(SpellData data, LivingEntity casterIn){ }
	
	public default RangeType getRangeType(){ return RangeType.MEDIUM; }
	
	/** Returns the maximum distance from caster to target, used by the LOOK SpawnStyle */
	public default double getTargetRange(Entity casterIn){ return RangeType.getRange(casterIn, this); }
	
	/** Called by SpellData when it first updates, chiefly meant for Instantaneous effects */
	public default void doEffectStart(SpellData data, World world, Side onSide){ }
	public void doEffect(SpellData data, World world, int ticksActive, Side onSide);
	/** Called by SpellData when it is destroyed before its duration completes */
	public default void doEffectCancel(SpellData data, World world, Side onSide){ doEffect(data, world, getDuration(data.casterLevel()), onSide); }
	
//	@OnlyIn(Dist.CLIENT)
//	public default void renderEffect(SpellData dataIn, int activeTime, World world, double x, double y, double z, float entityYaw, float partialTicks, RenderManager rendererIn){ }
	
	public MagicSchool getSchool();
	public List<MagicSubType> getDescriptors();
	
	public enum Components
	{
		FOCUS,
		MATERIAL,
		SOMATIC,
		VERBAL;
		
		public String translatedName(){ return (new TranslationTextComponent("enum."+Reference.ModInfo.MOD_PREFIX+"components."+this.name().toLowerCase())).getUnformattedComponentText(); }
		
		/** Returns true if the given caster could fulfill this component's requirement */
		public boolean isValid(LivingEntity casterIn, IMagicEffect spellIn)
		{
			if(casterIn == null) return true;
			switch(this)
			{
				case FOCUS:
					if(casterIn instanceof PlayerEntity && ((PlayerEntity)casterIn).isCreative()) return true;
					
					boolean focusFound = false;
					for(Hand hand : Hand.values())
					{
						ItemStack heldItem = casterIn.getHeldItem(hand);
						if(spellIn.itemMatchesFocus(heldItem))
						{
							focusFound = true;
							break;
						}
					}
					
					if(!focusFound && casterIn instanceof PlayerEntity)
					{
						PlayerEntity player = (PlayerEntity)casterIn;
						for(int slot=0; slot<9; slot++)
						{
							ItemStack heldItem = player.inventory.getStackInSlot(slot);
							if(spellIn.itemMatchesFocus(heldItem))
							{
								focusFound = true;
								break;
							}
						}
					}
					
					return focusFound;
				case MATERIAL:
					if(casterIn instanceof PlayerEntity && ((PlayerEntity)casterIn).isCreative()) ;
					else
					{
						PlayerEntity player = casterIn instanceof PlayerEntity ? (PlayerEntity)casterIn : null;
						for(ItemStack material : spellIn.getMaterialComponents())
						{
							int count = material.getCount();
							
							// Check player inventory
							if(player != null)
							{
								for(int slot=0; slot<player.inventory.getSizeInventory(); slot++)
								{
									ItemStack heldItem = player.inventory.getStackInSlot(slot);
									if(heldItem.isEmpty()) continue;
									if(isMatchingItem(heldItem, material)) count -= Math.min(count, heldItem.getCount());
								}
							}
							else
							{
								// Check held items
								for(Hand hand : Hand.values())
								{
									ItemStack heldItem = casterIn.getHeldItem(hand);
									if(heldItem.isEmpty()) continue;
									if(isMatchingItem(heldItem, material)) count -= Math.min(count, heldItem.getCount());
								}
								
								// Check armour slots
								for(EquipmentSlotType hand : EquipmentSlotType.values())
								{
									ItemStack heldItem = casterIn.getItemStackFromSlot(hand);
									if(heldItem.isEmpty()) continue;
									if(isMatchingItem(heldItem, material)) count -= Math.min(count, heldItem.getCount());
								}
							}
							
							if(count > 0) return false;
						}
					}
					return true;
				case SOMATIC:
					return !VOPotions.isParalysed(casterIn);
				case VERBAL:
					return !VOPotions.isSilenced(casterIn);
				default: return true;
			}
		}
		
		/** Extracts the requirements for the component from the given caster */
		public void extractFrom(LivingEntity casterIn, IMagicEffect spellIn)
		{
			switch(this)
			{
				case SOMATIC:
					break;
				case VERBAL:
					break;
				case FOCUS:
					break;
				case MATERIAL:
	    			for(ItemStack item : spellIn.getMaterialComponents())
	    			{
	    				if(casterIn instanceof PlayerEntity)
	    				{
//	    					PlayerEntity player = (PlayerEntity)casterIn;
//	    					if(!player.isCreative())
//	    						player.inventory.clearMatchingItems(item.getItem(), item.getMetadata(), item.getCount(), item.hasTag() ? item.getTag() : null);
	    				}
	    				else
	    				{
		    				int count = item.getCount();
		    				for(Hand hand : Hand.values())
		    				{
								if(count <= 0) break;
		    					ItemStack heldItem = casterIn.getHeldItem(hand);
		    					if(heldItem.isEmpty()) continue;
								if(isMatchingItem(heldItem, item))
								{
									int dif = Math.min(count, heldItem.getCount());
									count -= dif;
									if(dif >= heldItem.getCount()) casterIn.setHeldItem(hand, ItemStack.EMPTY);
									else heldItem.setCount(heldItem.getCount() - dif);
								}
		    				}
		    				
		    				for(EquipmentSlotType slot : EquipmentSlotType.values())
		    				{
		    					if(count <= 0) break;
		    					ItemStack heldItem = casterIn.getItemStackFromSlot(slot);
		    					if(heldItem.isEmpty()) continue;
								if(isMatchingItem(heldItem, item))
								{
									int dif = Math.min(count, heldItem.getCount());
									count -= dif;
									if(dif >= heldItem.getCount()) casterIn.setItemStackToSlot(slot, ItemStack.EMPTY);
									else heldItem.setCount(heldItem.getCount() - dif);
								}
		    				}
	    				}
	    			}
					break;
			}
		}
		
		private boolean isMatchingItem(ItemStack heldItem, ItemStack item)
		{
			return heldItem.isItemEqual(item);
		}
	}
	
	public enum DurationType
	{
		NORMAL,
		INSTANT,
		PERMANENT,
		CONCENTRATE;
		
		public String translatedName(){ return (new TranslationTextComponent("enum."+Reference.ModInfo.MOD_PREFIX+"magic_duration."+this.name().toLowerCase())).getUnformattedComponentText(); }
	}
	
	public enum RangeType
	{
		TOUCH,
		PERSONAL,
		CLOSE,
		MEDIUM,
		LONG,
		FEET,
		UNLIMITED;
		
		public String translatedName(){ return (new TranslationTextComponent("enum."+Reference.ModInfo.MOD_PREFIX+"magic_range."+this.name().toLowerCase())).getUnformattedComponentText(); }
		
		public static double getRange(Entity caster, IMagicEffect spell)
		{
			int casterCL = getCasterLevelFromEntity(caster);
			int CL = casterCL < 0 ? 20 : (casterCL > -1 ? casterCL : Spell.getMinCasterLevel(spell));
			
			switch(spell.getRangeType())
			{
				case PERSONAL:
				case TOUCH:		return (caster != null && caster instanceof PlayerEntity) ? ((PlayerEntity)caster).getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() : 5D;
				case CLOSE:		return Spell.feetToMetres(25 + Math.floor(CL / 2) * 5);
				case MEDIUM:	return Spell.feetToMetres(100 + CL * 10);
				case LONG:		return Spell.feetToMetres(400 + CL * 40);
				case UNLIMITED:	return Double.MAX_VALUE;
				default:		return 5D;
			}
		}
	}
	
	public enum SpawnStyle
	{
		/** Spawn at the point you are looking at */
		LOOK,
		/** Spawn at your head position */
		HEAD,
		/** Spawn at your feet */
		FEET,
		/** Use the setSpawnPosition function */
		CUSTOM;
	}
	
	public enum MagicSchool
	{
		ABJURATION(-1, TextFormatting.GRAY),
		CONJURATION(16449280, TextFormatting.YELLOW),
		DIVINATION(61695, TextFormatting.AQUA),
		ENCHANTMENT(16711848, TextFormatting.LIGHT_PURPLE),
		EVOCATION(16711680, TextFormatting.RED),
		ILLUSION(10354943, TextFormatting.DARK_PURPLE),
		NECROMANCY(0, TextFormatting.DARK_GRAY),
		TRANSMUTATION(65305, TextFormatting.GREEN);
		
		private final int colour;
		private final TextFormatting textColour;
		
		private MagicSchool(int colourIn, TextFormatting textIn)
		{
			colour = colourIn;
			textColour = textIn;
		}
		
		public int getColour(){ return this.colour; }
		public TextFormatting getTextColour(){ return this.textColour; }
		public String translatedName(){ return (new TranslationTextComponent("enum."+Reference.ModInfo.MOD_PREFIX+"magic_school."+this.name().toLowerCase())).getUnformattedComponentText(); }
	}
	
	public enum MagicSubType
	{
		ACID,
		AIR,
		COLD,
		DARKNESS,
		DEATH,
		EARTH,
		ELECTRICITY,
		EVIL,
		FEAR,
		FIRE,
		FORCE,
		GOOD,
		HEALING,
		LIGHT,
		MIND_AFFECTING,
		SONIC,
		WATER;
		
		public static String getListAsString(List<MagicSubType> list)
		{
			String string = "";
			if(!list.isEmpty())
			{
				for(int i=0; i<list.size(); i++)
				{
					string += list.get(i).translatedName();
					if(i < (list.size()-1)) string += ", ";
				}
			}
			return string;
		}
		
		public String translatedName(){ return (new TranslationTextComponent("enum."+Reference.ModInfo.MOD_PREFIX+"magic_subtype."+this.name().toLowerCase())).getUnformattedComponentText(); }
	}
}
