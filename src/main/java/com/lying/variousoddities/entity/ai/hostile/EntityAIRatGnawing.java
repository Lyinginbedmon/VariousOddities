package com.lying.variousoddities.entity.ai.hostile;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.lying.variousoddities.entity.AbstractRat;
import com.lying.variousoddities.init.VOBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EntityAIRatGnawing extends Goal
{
	private final Level theWorld;
	private final AbstractRat theRat;
	
	private final int maxSearchAttempts;
	private final int range;
	
	private BlockPos gnawSite;
	private PathNavigation theNavigator;
	
	private int breakingTime;
	private int breakingProgPrev = -1;
	
	private int pathingAbandon = 0;
	
	public EntityAIRatGnawing(AbstractRat par1Rat, int par2Range, int par2Tries)
	{
		theRat = par1Rat;
		theWorld = par1Rat.getLevel();
		range = par2Range;
		maxSearchAttempts = par2Tries;
		theNavigator = par1Rat.getNavigation();
		setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
	}
	
	public boolean canUse()
	{
		if(!theWorld.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) || theRat.getTarget() != null)
			return false;
		
		gnawSite = getGnawSite();
		return this.theRat.getRandom().nextInt(200) == 0 && gnawSite != null;
	}
	
	public boolean canContinueToUse()
	{
		return theRat.getTarget() == null && gnawSite != null;
	}
	
	public void stop()
	{
		gnawSite = null;
		pathingAbandon = 0;
		breakingTime = 0;
		breakingProgPrev = -1;
	}
	
	public void start()
	{
		theRat.getLookControl().setLookAt(gnawSite.getX() + 0.5D, gnawSite.getY() + 0.5D, gnawSite.getZ() + 0.5D, 10F, theRat.getMaxHeadYRot());
		if(getDistanceTo(gnawSite) > 0.75D)
			theNavigator.moveTo(gnawSite.getX(), gnawSite.getY(), gnawSite.getZ(), 1D);
	}
	
	public void tick()
	{
		if(gnawSite == null || !isGnawable(theWorld.getBlockState(gnawSite)))
		{
			gnawSite = null;
			return;
		}
		
		theRat.getLookControl().setLookAt(gnawSite.getX() + 0.5D, gnawSite.getY() + 0.5D, gnawSite.getZ() + 0.5D, 10F, theRat.getMaxHeadYRot());
		switch((getDistanceTo(gnawSite) > Math.max(1.0D, theRat.getBbWidth())) ? 0 : 1)
		{
			case 0: // Approaching
				if(theNavigator.isDone())
				{
					if(pathingAbandon++ >= 60)
						gnawSite = null;
					else if(canPathTo(gnawSite))
						theNavigator.moveTo(theRat.getNavigation().createPath(gnawSite, (int)theRat.getAttributeValue(Attributes.FOLLOW_RANGE)), 1D);
					else
						gnawSite = null;
				}
				break;
			case 1: // Gnawing
				theRat.getNavigation().stop();
				
				BlockState theBlock = theWorld.getBlockState(gnawSite);
				int timeToBreak = (int)Math.max(20, theBlock.getDestroySpeed(theWorld, gnawSite) * 80);
				
		        ++this.breakingTime;
		        int breakProgress = (int)((float)this.breakingTime / (float)timeToBreak * 10.0F);
		        if(breakProgress != this.breakingProgPrev)
		        {
		            this.theWorld.destroyBlockProgress(this.theRat.getId(), this.gnawSite, breakProgress);
		            this.breakingProgPrev = breakProgress;
		        }
		        
		        if(this.breakingTime >= timeToBreak)
		        {
		        	if(theWorld.getBlockState(gnawSite).is(VOBlocks.GNAWABLE_HEALING))
		        		this.theRat.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
		        	
		            this.theWorld.destroyBlock(gnawSite, false);
		            gnawSite = null;
		        }
		        break;
		}
	}
	
	private BlockPos getGnawSite()
	{
		BlockPos ratPos = theRat.blockPosition();
		if(isGnawable(theWorld.getBlockState(ratPos)))
			return ratPos;
		
		BlockPos ratEyePos = new BlockPos(theRat.getX(), theRat.getEyeY(), theRat.getZ());
		if(isGnawable(theWorld.getBlockState(ratEyePos)))
			return ratEyePos;
		
		List<BlockPos> tested = new ArrayList<>();
		tested.add(ratPos);
		tested.add(ratEyePos);
		
		int i = 0;
		do
		{
			double posX = randomDouble();
			double posY = randomDouble() * 0.5;
			double posZ = randomDouble();
			
			BlockPos testPos = ratPos.offset(posX, posY, posZ);
			if(!tested.contains(testPos) && isGnawable(theWorld.getBlockState(testPos)) && canPathTo(testPos))
				return testPos;
			
			tested.add(testPos);
			++i;
		}
		while(i < maxSearchAttempts);
		
		return null;
	}
	
	private int randomDouble()
	{
		return theRat.getRandom().nextInt(range) - (range / 2);
	}
	
	private double getDistanceTo(BlockPos par1BlockPos)
	{
		Vec3 ratPos = theRat.position().add(0D, theRat.getBbHeight() / 2D, 0D);
		Vec3 gnawPos = new Vec3(par1BlockPos.getX() + 0.5D, par1BlockPos.getY() + 0.5D, par1BlockPos.getZ() + 0.5D);
		return ratPos.distanceTo(gnawPos);
	}
	
	/**
	 * Block is either a sufficiently-vulnerable form of wood OR will heal the rat when eaten
	 * @param par1BlockState
	 * @return
	 */
	private boolean isGnawable(BlockState par1BlockState)
	{
		if(par1BlockState == null || theWorld == null)
			return false;
		
		return par1BlockState.is(VOBlocks.GNAWABLE);
	}
	
	private boolean canPathTo(BlockPos par1BlockPos)
	{
		return theRat.getNavigation().createPath(par1BlockPos, (int)theRat.getAttributeValue(Attributes.FOLLOW_RANGE)) != null;
	}
}
