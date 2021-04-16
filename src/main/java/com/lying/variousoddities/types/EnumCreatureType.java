package com.lying.variousoddities.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.init.VOEnchantments;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.IMagicEffect.MagicSchool;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.types.TypeHandler.EnumDamageResist;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public enum EnumCreatureType
{
	ABERRATION(null, TypeHandler.get(), Action.STANDARD, 8),
	AIR(null, new TypeHandler()
		{
			public void onLivingTick(LivingEntity living)
			{
				if(living.getType() == EntityType.PLAYER)
				{
					PlayerEntity player = (PlayerEntity)living;
					if(!VOPotions.isParalysed(player))
					{
						if(!(player.isCreative() || player.isSpectator()) && !player.abilities.allowFlying)
						{
							player.abilities.allowFlying = true;
							player.sendPlayerAbilities();
						}
					}
				}
			}
			
			public void onRemove(LivingEntity living)
			{
				if(living.getType() == EntityType.PLAYER)
				{
					PlayerEntity player = (PlayerEntity)living;
					if(!player.isCreative() && !player.isSpectator())
					{
						player.abilities.allowFlying = false;
						player.sendPlayerAbilities();
					}
				}
			}
		}),
	AMPHIBIOUS(null, new TypeHandler()
		{
			public boolean canApplyTo(List<EnumCreatureType> types){ return types.contains(AQUATIC); }
		}),
	ANIMAL(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 8),
	AQUATIC(null, new TypeHandlerAquatic(false)),
	AUGMENTED(),
	COLD(null, new TypeHandler().addResistance(VODamageSource.COLD, EnumDamageResist.IMMUNE).setFireResist(EnumDamageResist.VULNERABLE)),
	CONSTRUCT(null, new TypeHandler()
	{
		public EnumDamageResist getDamageResist(DamageSource source)
		{
			return VODamageSource.isFalling(source) ? EnumDamageResist.IMMUNE : super.getDamageResist(source);
		}
		public boolean canSpellAffect(IMagicEffect spellIn)
		{
			if(spellIn.getSchool() == MagicSchool.ENCHANTMENT) return false;
			if(spellIn.getSchool() == MagicSchool.NECROMANCY || spellIn.getDescriptors().contains(MagicSubType.DEATH)) return false;
			return true;
		}
	}.noCriticalHit().noParalysis().noPoison(), Action.NONE, 10),
	DRAGON(null, new TypeHandler().noParalysis(), Action.STANDARD, 12),
	EARTH(),
	ELEMENTAL(null, new TypeHandler().noCriticalHit().noParalysis().noPoison(), Action.REGEN_ONLY, 8),
	EXTRAPLANAR(),
	EVIL(null, new TypeHandler().addResistance(VODamageSource.EVIL, EnumDamageResist.IMMUNE).addResistance(VODamageSource.HOLY, EnumDamageResist.VULNERABLE)),
	FEY(null, new TypeHandler()
		{
			public void onDamageEventPost(LivingHurtEvent event)
			{
				DamageSource source = event.getSource();
				if(source instanceof EntityDamageSource)
				{
					Entity attacker = source.getTrueSource();
					if(attacker != null && attacker instanceof LivingEntity)
					{
						boolean resist = true;
						ItemStack heldItem = ((LivingEntity)attacker).getHeldItemMainhand();
						if(!heldItem.isEmpty())
						{
							Item held = heldItem.getItem();
							IItemTier itemTier = held instanceof TieredItem ? ((TieredItem)held).getTier() : null;
							if(itemTier != null && (itemTier.toString().toLowerCase().contains("silver") || EnchantmentHelper.getEnchantmentLevel(VOEnchantments.SILVERSHEEN, heldItem) > 0))
								resist = false;
						}
						
						if(resist)
							event.setAmount(Math.max(0F, event.getAmount() - 4F));
					}
				}
			}
		}, Action.STANDARD, 6),
	FIRE(null, new TypeHandler().setFireResist(EnumDamageResist.IMMUNE).addResistance(VODamageSource.COLD, EnumDamageResist.VULNERABLE)),
	GIANT(null, TypeHandler.get(), Action.STANDARD, 8),
	GOBLIN(),
	HOLY(null, new TypeHandler().addResistance(VODamageSource.HOLY, EnumDamageResist.IMMUNE).addResistance(VODamageSource.EVIL, EnumDamageResist.VULNERABLE)),
	HUMANOID(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 8),
	INCORPOREAL(null, new TypeHandler().addResistance(DamageSource.FALL, EnumDamageResist.IMMUNE)),
	MAGICAL_BEAST(null, TypeHandler.get(), Action.STANDARD, 10),
	MONSTROUS_HUMANOID(CreatureAttribute.UNDEFINED, TypeHandler.get(), Action.STANDARD, 8),
	OUTSIDER(null, new TypeHandler(), EnumSet.of(Action.BREATHE_AIR, Action.REGENERATE), 8),
	NATIVE(null, new TypeHandler()
		{
			public EnumSet<Action> applyActions(EnumSet<Action> actions, Collection<EnumCreatureType> types){ actions.addAll(Arrays.asList(Action.SLEEP, Action.EAT)); return actions; }
			public boolean canApplyTo(List<EnumCreatureType> types){ return types.contains(OUTSIDER); }
		}),
	PLANT(null, new TypeHandler()
		{
			public boolean canSpellAffect(IMagicEffect spellIn)
			{
				MagicSchool school = spellIn.getSchool();
				return !(school == MagicSchool.ENCHANTMENT || school == MagicSchool.TRANSMUTATION);
			}
		}.noCriticalHit().noParalysis().noPoison().setFireResist(EnumDamageResist.VULNERABLE), EnumSet.of(Action.BREATHE_AIR, Action.EAT, Action.REGENERATE), 8),
	REPTILE(),
	SHAPECHANGER(),
	OOZE(null, new TypeHandler()
		{
			public boolean canSpellAffect(IMagicEffect spellIn)
			{
				return spellIn.getSchool() != MagicSchool.TRANSMUTATION;
			}
		}.noCriticalHit().noParalysis().noPoison(), EnumSet.of(Action.BREATHE_AIR, Action.EAT, Action.REGENERATE), 10),
	UNDEAD(CreatureAttribute.UNDEAD, new TypeHandler()
		{
			public boolean canSpellAffect(IMagicEffect spellIn)
			{
				return !(spellIn.getSchool() == MagicSchool.ENCHANTMENT || spellIn.getDescriptors().contains(MagicSubType.DEATH));
			}
		}.noCriticalHit().noParalysis().noPoison().addResistance(VODamageSource.HOLY, EnumDamageResist.VULNERABLE), Action.NONE, 12),
	VERMIN(CreatureAttribute.ARTHROPOD, TypeHandler.get(), Action.STANDARD, 8),
	WATER(CreatureAttribute.WATER, new TypeHandlerAquatic(true));
	
	public static final Predicate<EnumCreatureType> IS_SUPERTYPE = new Predicate<EnumCreatureType>()
		{
			public boolean apply(EnumCreatureType input){ return input.isSupertype(); }
		};
	public static final Predicate<EnumCreatureType> IS_SUBTYPE = new Predicate<EnumCreatureType>()
		{
			public boolean apply(EnumCreatureType input){ return !input.isSupertype(); }
		};
	
	private static final EnumSet<EnumCreatureType> SUPERTYPES = EnumSet.of(ABERRATION, ANIMAL, CONSTRUCT, DRAGON, ELEMENTAL, FEY, GIANT, HUMANOID, MAGICAL_BEAST, MONSTROUS_HUMANOID, OUTSIDER, PLANT, OOZE, UNDEAD, VERMIN);
	private static final EnumSet<EnumCreatureType> SUBTYPES = EnumSet.complementOf(SUPERTYPES);
	
	private static final String translationBase = "enum.varodd.creature_type.";
	private final CreatureAttribute parentAttribute;
	private final boolean supertype;
	private final TypeHandler handler;
	private final EnumSet<Action> actions;
	private final int hitDie;
	
	private EnumCreatureType(CreatureAttribute attribute, TypeHandler handlerIn, EnumSet<Action> actionsIn, int hitDieIn)
	{
		parentAttribute = attribute;
		supertype = true;
		handler = handlerIn;
		actions = actionsIn.clone();
		hitDie = hitDieIn;
	}
	
	private EnumCreatureType()
	{
		this(null, TypeHandler.get());
	}
	
	private EnumCreatureType(CreatureAttribute attribute, TypeHandler handlerIn)
	{
		parentAttribute = attribute;
		supertype = false;
		handler = handlerIn;
		actions = Action.NONE;
		hitDie = 8;
	}
	
	public static List<String> getSupertypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : SUPERTYPES)
			names.add(type.getSimpleName().toLowerCase());
		return names;
	}
	
	public static List<String> getSubtypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : SUBTYPES)
			names.add(type.getSimpleName().toLowerCase());
		return names;
	}
	
	public static List<String> getTypeNames()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : values())
			names.add(type.getSimpleName().toLowerCase());
		return names;
	}
	
	public boolean hasParentAttribute(){ return parentAttribute != null; }
	public CreatureAttribute getParentAttribute(){ return parentAttribute; }
	
	public String getName(){ return translationBase+getSimpleName(); }
	public String getSimpleName(){ return this.name().toLowerCase(); }
	public String getDefinition(){ return translationBase+getSimpleName()+".definition"; }
	public String getProperties(){ return translationBase+getSimpleName()+".properties"; }
	public String getType(){ return translationBase + (supertype ? "supertype" : "subtype"); }
	public TypeHandler getHandler(){ return this.handler; }
	public boolean isSupertype(){ return this.supertype; }
	
	public EnumSet<Action> actions(){ return actions; }
	
	public double healthModForPlayer()
	{
		if(!isSupertype()) return 0D;
		return (((double)getHitDie() / (double)HUMANOID.getHitDie()) * Attributes.MAX_HEALTH.getDefaultValue()) - Attributes.MAX_HEALTH.getDefaultValue();
	}
	public int getHitDie(){ return hitDie; }
	
	/** Returns the translated name of the given creature type, with a hover event displaying its definition */
	public IFormattableTextComponent getTranslated()
	{
		return new TranslationTextComponent(getName()).modifyStyle((style) -> { return style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(getDefinition()))); });
	}
	
	public static EnumCreatureType fromName(String nameIn)
	{
		for(EnumCreatureType type : EnumCreatureType.values())
			if(type.getSimpleName().equalsIgnoreCase(nameIn))
				return type;
		return null;
	}
	
	public static List<String> names()
	{
		List<String> names = new ArrayList<>();
		for(EnumCreatureType type : values())
			names.add(type.getSimpleName());
		java.util.Collections.sort(names);
		return names;
	}
	
	public static List<EnumCreatureType> stringToTypes(String[] namesIn)
	{
		List<EnumCreatureType> types = new ArrayList<EnumCreatureType>();
		for(String name : namesIn)
			if(fromName(name) != null)
				types.add(fromName(name));
		java.util.Collections.sort(types);
		return types;
	}
	
	public static EnumCreatureType getTypeFromAttribute(CreatureAttribute attribute)
	{
		for(EnumCreatureType type : values())
			if(type.getParentAttribute() == attribute)
				return type;
		return null;
	}
	
	/** Converts a list of assorted creature types into a type entry, as in a stat block. */
	public static ITextComponent typesToHeader(Collection<EnumCreatureType> types)
	{
		List<EnumCreatureType> supertypes = new ArrayList<>();
		List<EnumCreatureType> subtypes = new ArrayList<>();
		
		for(EnumCreatureType type : types)
			if(type.isSupertype())
				supertypes.add(type);
			else
				subtypes.add(type);
		java.util.Collections.sort(supertypes);
		java.util.Collections.sort(subtypes);
		
		StringTextComponent supertype = new StringTextComponent("");
		if(supertypes.isEmpty())
			supertype.append(new TranslationTextComponent(translationBase+"no_supertype"));
		else
			for(EnumCreatureType sup : supertypes)
			{
				if(supertype.getSiblings().size() > 0)
					supertype.append(new StringTextComponent(" "));
				supertype.append(sup.getTranslated());
			}
		
		StringTextComponent subtype = new StringTextComponent("");
		if(!subtypes.isEmpty())
		{
			subtype = new StringTextComponent(" (");
			for(EnumCreatureType sup : subtypes)
			{
				if(subtype.getSiblings().size() > 0)
					subtype.append(new StringTextComponent(", "));
				subtype.append(sup.getTranslated());
			}
			subtype.append(new StringTextComponent(")"));
		}
		
		return supertype.append(subtype);
	}
	
	/** Returns true if the given entity can pass through obstacles at the given position */
	public static boolean canPhase(IBlockReader worldIn, @Nullable BlockPos pos, LivingEntity entity)
	{
		if(entity == null)
			return false;
		
		// Is the creature of a type that can phase?
		TypesManager manager = TypesManager.get(entity.getEntityWorld());
		if(!manager.isMobOfType(entity, EnumCreatureType.INCORPOREAL))
			return false;
		
		// Position is null if this is just a check to see if the creature can phase at all
		if(pos == null)
			return true;
		
		// Deny for pre-defined blocks, such as unbreakable blocks and portals
		BlockState state = worldIn.getBlockState(pos);
		if(VOBlocks.UNPHASEABLE.contains(state.getBlock()))
			return false;
		
		// Attempt to catch any of the above that haven't already been tagged in data
		if(state.getBlockHardness(worldIn, pos) < 0F || state.getMaterial() == Material.PORTAL)
			return false;
		
		// Phasing or not is irrelevant here, but prevents pressure plates etc. from firing
		VoxelShape collision = state.getCollisionShape(worldIn, pos);
		if(collision == null || collision == VoxelShapes.empty())
			return true;
		
		// Lastly, only allow phasing if any open side exists around the target block
		double blockTop = pos.getY() + collision.getBoundingBox().maxY;
		if((entity.getPosY() + 0.2D) <= blockTop)
			for(Direction direction : Direction.values())
			{
				BlockPos offset = pos.offset(direction);
				BlockState stateAtOffset = worldIn.getBlockState(offset);
				if(stateAtOffset.getCollisionShape(worldIn, offset) == VoxelShapes.empty() || stateAtOffset.getFluidState().getFluid() != Fluids.EMPTY)
					return true;
			}
		return false;
	}
	
	public static class ActionSet
	{
		EnumSet<Action> actions = Action.NONE.clone();
		
		public ActionSet(){ }
		public ActionSet(EnumSet<Action> actions)
		{
			super();
			add(actions);
		}
		
		public String toString(){ return actions.toString(); }
		
		public void applyType(TypeHandler handler, Collection<EnumCreatureType> types)
		{
			actions = handler.applyActions(actions, types);
		}
		
		public void add(Action actionIn)
		{
			if(!actions.contains(actionIn))
				actions.add(actionIn);
		}
		
		public void add(EnumSet<Action> actionsIn)
		{
			for(Action action : actionsIn)
				add(action);
		}
		
		public void remove(Action actionIn)
		{
			if(actions.contains(actionIn))
				actions.remove(actionIn);
		}
		
		public void remove(EnumSet<Action> actionsIn)
		{
			for(Action action : actionsIn)
				remove(action);
		}
		
		public boolean eats(){ return actions.contains(Action.EAT); }
		public boolean sleeps(){ return actions.contains(Action.SLEEP); }
		public boolean breathes(){ return breathesAir() || breathesWater(); }
		public boolean breathesAir(){ return actions.contains(Action.BREATHE_AIR); }
		public boolean breathesWater(){ return actions.contains(Action.BREATHE_WATER); }
		public boolean regenerates(){ return actions.contains(Action.REGENERATE); }
		
		public static ActionSet fromSupertypes(Collection<EnumCreatureType> types)
		{
			ActionSet set = new ActionSet();
			types.removeIf(IS_SUBTYPE);
			
			// Actions shared by all supertypes
			EnumSet<Action> actions = EnumSet.allOf(Action.class);
			for(EnumCreatureType type : types)
			{
				EnumSet<Action> typeActions = type.actions();
				EnumSet<Action> remove = EnumSet.noneOf(Action.class);
				for(Action action : actions)
					if(!typeActions.contains(action))
						remove.add(action);
				actions.removeAll(remove);
			}
			set.add(actions);
			
			return set;
		}
		
		public static ActionSet fromTypes(Collection<EnumCreatureType> types)
		{
			if(types.isEmpty())
				return new ActionSet(Action.STANDARD);
			
			List<EnumCreatureType> supertypes = new ArrayList<>();
			supertypes.addAll(types);
			supertypes.removeIf(IS_SUBTYPE);
			
			ActionSet set = fromSupertypes(supertypes);
			
			List<EnumCreatureType> subtypes = new ArrayList<>();
			subtypes.addAll(types);
			subtypes.removeIf(IS_SUPERTYPE);
			// Modifiers to actions from subtypes
			subtypes.forEach((type) -> 
				{
					set.applyType(type.getHandler(), types);
				});
			
			return set;
		}
	}
	
	/** Common meaningful properties of creatures that must be acted upon to survive */
	public enum Action
	{
		/** Eating food and starving to death without it */
		EAT,
		/** Sleeping in a bed and spawning phantoms without sleep */
		SLEEP,
		/** Breathing air and drowning without it */
		BREATHE_AIR,
		/** Breathing water and suffocating without it */
		BREATHE_WATER,
		/** Regain health naturally over time */
		REGENERATE;
		
		/** The most common array. */
		public static final EnumSet<Action> STANDARD = EnumSet.of(Action.EAT, Action.SLEEP, Action.BREATHE_AIR, REGENERATE);
		/** Creatures that do not need to eat, sleep, or breathe, but can regenerate health. */
		public static final EnumSet<Action> REGEN_ONLY = EnumSet.of(REGENERATE);
		/** Creatures that have no natural needs, but also cannot regenerate health. */
		public static final EnumSet<Action> NONE = EnumSet.noneOf(Action.class);
		public static final EnumSet<Action> ALL = EnumSet.allOf(Action.class);
	}
}