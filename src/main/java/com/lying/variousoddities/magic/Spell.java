package com.lying.variousoddities.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Predicate;
import com.lying.variousoddities.api.entity.IMobSpellcaster;
import com.lying.variousoddities.api.event.SpellEvent.SpellAffectEntityEvent;
import com.lying.variousoddities.api.event.SpellEvent.SpellAffectSpellEvent;
import com.lying.variousoddities.api.event.SpellEvent.SpellResistanceEvent;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.VOHelper;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.SpellManager.SpellData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;

public abstract class Spell implements IMagicEffect
{
	public static final int FREE_ACTION			= Reference.Values.TICKS_PER_SECOND / 4;
	public static final int MOVE_ACTION			= Reference.Values.TICKS_PER_SECOND / 2;
	public static final int STANDARD_ACTION		= Reference.Values.TICKS_PER_SECOND * 1;
	public static final int FULL_ROUND_ACTION	= Reference.Values.TICKS_PER_SECOND * 3;
	
	protected static final List<MagicSubType> NO_TYPES = new ArrayList<MagicSubType>();
	
	public Collection<String> getDescription()
	{
		ArrayList<String> description = new ArrayList<String>();
		description.add(Component.translatable("magic."+Reference.ModInfo.MOD_PREFIX+getSimpleName()+".info").getString());
		return description;
	}
	
	public LivingEntity getLookTarget(LivingEntity casterIn)
	{
		return VOHelper.getEntityLookTarget(casterIn, getTargetRange(casterIn));
	}
	
	public static double feetToMetres(double feetIn)
	{
		return feetIn * 0.3048;
	}
	
	public static int rollDie(int dieSize, RandomSource rand)
	{
		return 1 + rand.nextInt(dieSize);
	}
	
	public static int rollDice(int dieCount, int dieSize, RandomSource rand)
	{
		int tally = 0;
		for(int i=0; i<dieCount; i++) tally += rollDie(dieSize, rand);
		return tally;
	}
	
	/**
	 * Returns true if the given spell is able to affect the given target entity.
	 * @param spellData
	 * @param world
	 * @param targetIn
	 * @return
	 */
	public boolean canAffectEntity(SpellData spellData, Level world, Entity targetIn)
	{
		if(MagicEffects.isInsideAntiMagic(targetIn)) return false;
		
		LivingEntity spellOwner = spellData.getCaster(world);
		
		int SR = getEntitySpellResistance(targetIn);
		
		if(targetIn instanceof IMobSpellcaster)
		{
			IMobSpellcaster spellcaster = ((IMobSpellcaster)targetIn);
			
			// Apply any mob-specific magic resistances
			if(!spellcaster.canSpellAffect(this, spellOwner)) 
				return false;
			
			// Apply any spell resistance
			if(!(isWillingTarget(targetIn, spellOwner) || targetIn == spellOwner))
				if(allowsSpellResistance() && SR > 0)
					if(spellData.casterLevel() + Spell.rollDie(20, targetIn.getLevel().random) < SR)
						return false;
		}
		return !MinecraftForge.EVENT_BUS.post(new SpellAffectEntityEvent(spellData, world, targetIn));
	}
	
	public boolean canAffectSpell(SpellData spellData, Level world, SpellData targetIn)
	{
		if(MagicEffects.isInsideAntiMagic(world, targetIn.posX, targetIn.posY, targetIn.posZ)) return false;
		return !MinecraftForge.EVENT_BUS.post(new SpellAffectSpellEvent(spellData, world, targetIn));
	}
	
	/**
	 * Returns the spell resistance of the given entity.<br>
	 * If the entity has multiple sources of SR, the highest is returned.
	 * @param entityIn
	 * @return
	 */
	public static int getEntitySpellResistance(Entity entityIn)
	{
		int SR = 0;
		if(entityIn instanceof IMobSpellcaster)
			SR = ((IMobSpellcaster)entityIn).getSpellResistance();
		
		if(entityIn instanceof ItemEntity)
			SR = Math.max(SR, getSRFromItem(((ItemEntity)entityIn).getItem()));
		
//		if(entityIn instanceof Player)
//		{
//			IItemHandler inv = BaublesApi.getBaublesHandler((Player)entityIn);
//			for(int slot=0; slot<inv.getSlots(); slot++)
//				SR = Math.max(SR, getSRFromItem(inv.getStackInSlot(slot)));
//		}
		
		for(ItemStack armor : entityIn.getArmorSlots())
			SR = Math.max(SR, getSRFromItem(armor));
		
		SpellResistanceEvent event = new SpellResistanceEvent(entityIn, SR);
		MinecraftForge.EVENT_BUS.post(event);
		
		return event.getSR();
	}
	
	private static int getSRFromItem(ItemStack item)
	{
//		if(!item.isEmpty() && item.isItemEnchanted())
//		{
//			int tier = EnchantmentHelper.getEnchantmentLevel(VOEnchantments.SPELL_RESISTANCE, item);
//			if(tier > 0)
//				return 11 + (tier * 2);
//		}
		return 0;
	}
	
	public boolean canAffectBlock(Entity spellObjectIn, BlockPos posIn)
	{
		return true;
	}
	
	public static boolean isWillingTarget(Entity targetIn, LivingEntity casterIn)
	{
		Team targetTeam = targetIn.getTeam();
		Team casterTeam = casterIn.getTeam();
		if(targetTeam == null && casterTeam == null) return true;
		if(targetTeam != null && casterTeam != null) return targetTeam == casterTeam;
//		if(targetIn instanceof ITameable) return ((ITameable)targetIn).getOwnerId().equals(casterIn.getUniqueID());
		return false;
	}
	
	public static SpellData getTargetSpell(Player player)
	{
		return getTargetSpell(player, player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue());
	}
	
	public static SpellData getTargetSpell(LivingEntity caster, double range)
	{
		return getTargetSpell(caster, range, null);
	}
	
	public static SpellData getTargetSpell(LivingEntity caster, double range, Predicate<SpellData> exclude)
	{
		Vec3 headPos = new Vec3(caster.getX(), caster.getY() + caster.getEyeHeight(), caster.getZ());
		Vec3 lookVec = caster.getLookAngle();
		
		Level world = caster.getLevel();
		SpellData bestGuess = null;
		double smallestDist = Double.MAX_VALUE;
		for(SpellData spell : SpellManager.get(world).getSpellsWithin(world, caster.getBoundingBox().inflate(range)))
		{
			if(exclude != null && exclude.apply(spell)) continue;
			double distToEnt = headPos.distanceTo(new Vec3(spell.posX, spell.posY + 0.25D, spell.posZ));
			Vec3 posAtDist = headPos.add(new Vec3(lookVec.x * distToEnt, lookVec.y * distToEnt, lookVec.z * distToEnt));
			
			if(spell.getBoundingBox().contains(posAtDist) && smallestDist > distToEnt)
			{
				bestGuess = spell;
				smallestDist = distToEnt;
			}
		}
		return bestGuess;
	}
	
	/**
	 * Returns the minimum caster level necessary to cast the given effect
	 * @param effect
	 * @return
	 */
	public static int getMinCasterLevel(IMagicEffect effect)
	{
		if(effect == null) return 1;
		return Math.max(1, (effect.getLevel() * 2) - 1);
	}
}
