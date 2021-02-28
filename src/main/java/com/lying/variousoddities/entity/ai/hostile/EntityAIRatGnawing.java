package com.lying.variousoddities.entity.ai.hostile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.lying.variousoddities.entity.AbstractRat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class EntityAIRatGnawing extends Goal
{
	private final World theWorld;
	private final AbstractRat theRat;
	
	private final int maxSearchAttempts;
	private final int range;
	
	private BlockPos gnawSite;
	private PathNavigator theNavigator;
	
	private int breakingTime;
	private int breakingProgPrev = -1;
	
	/**
	 * List of specific blocks to gnaw on
	 */
	private static final List<Block> gnawables = Arrays.asList
				(
					Blocks.MELON,
					Blocks.PUMPKIN,
					Blocks.CARVED_PUMPKIN,
					Blocks.HAY_BLOCK,
					Blocks.CAKE
				);
	
	private int pathingAbandon = 0;
	
	public EntityAIRatGnawing(AbstractRat par1Rat, int par2Range, int par2Tries)
	{
		theRat = par1Rat;
		theWorld = par1Rat.getEntityWorld();
		range = par2Range;
		maxSearchAttempts = par2Tries;
		theNavigator = par1Rat.getNavigator();
		setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
	}
	
	public boolean shouldExecute()
	{
		if(!theWorld.getGameRules().getBoolean(GameRules.MOB_GRIEFING) || theRat.getAttackTarget() != null)
			return false;
		
		gnawSite = getGnawSite();
		return this.theRat.getRNG().nextInt(200) == 0 && gnawSite != null;
	}
	
	public boolean shouldContinueExecuting()
	{
		return theRat.getAttackTarget() == null && gnawSite != null;
	}
	
	public void resetTask()
	{
		gnawSite = null;
		pathingAbandon = 0;
		breakingTime = 0;
		breakingProgPrev = -1;
	}
	
	public void startExecuting()
	{
		theRat.getLookController().setLookPosition(gnawSite.getX() + 0.5D, gnawSite.getY() + 0.5D, gnawSite.getZ() + 0.5D, 10F, theRat.getVerticalFaceSpeed());
		if(getDistanceTo(gnawSite) > 0.75D)
			theNavigator.tryMoveToXYZ(gnawSite.getX(), gnawSite.getY(), gnawSite.getZ(), 1D);
	}
	
	public void tick()
	{
		if(gnawSite == null || !isGnawable(theWorld.getBlockState(gnawSite)))
		{
			gnawSite = null;
			return;
		}
		
		theRat.getLookController().setLookPosition(gnawSite.getX() + 0.5D, gnawSite.getY() + 0.5D, gnawSite.getZ() + 0.5D, 10F, theRat.getVerticalFaceSpeed());
		switch((getDistanceTo(gnawSite) > Math.max(1.0D, theRat.getWidth())) ? 0 : 1)
		{
			case 0: // Approaching
				if(theNavigator.noPath())
				{
					if(pathingAbandon++ >= 60)
						gnawSite = null;
					else if(canPathTo(gnawSite))
						theNavigator.setPath(theRat.getNavigator().getPathToPos(gnawSite, (int)theRat.getAttributeValue(Attributes.FOLLOW_RANGE)), 1D);
					else
						gnawSite = null;
				}
				break;
			case 1: // Gnawing
				theRat.getNavigator().clearPath();
				
				BlockState theBlock = theWorld.getBlockState(gnawSite);
				int timeToBreak = (int)Math.max(20, theBlock.getBlockHardness(theWorld, gnawSite) * 80);
				
		        ++this.breakingTime;
		        int breakProgress = (int)((float)this.breakingTime / (float)timeToBreak * 10.0F);
		        if(breakProgress != this.breakingProgPrev)
		        {
		            this.theWorld.sendBlockBreakProgress(this.theRat.getEntityId(), this.gnawSite, breakProgress);
		            this.breakingProgPrev = breakProgress;
		        }
		        
		        if(this.breakingTime >= timeToBreak)
		        {
		        	if(shouldHeal(theWorld.getBlockState(gnawSite).getBlock()))
		        		this.theRat.addPotionEffect(new EffectInstance(Effects.REGENERATION, 100, 0));
		        	
		            this.theWorld.destroyBlock(gnawSite, false);
		            gnawSite = null;
		        }
		        break;
		}
	}
	
	private BlockPos getGnawSite()
	{
		BlockPos ratPos = theRat.getPosition();
		if(isGnawable(theWorld.getBlockState(ratPos)))
			return ratPos;
		
		BlockPos ratEyePos = new BlockPos(theRat.getPosX(), theRat.getPosYEye(), theRat.getPosZ());
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
			
			BlockPos testPos = ratPos.add(posX, posY, posZ);
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
		return theRat.getRNG().nextInt(range) - (range / 2);
	}
	
	private double getDistanceTo(BlockPos par1BlockPos)
	{
		Vector3d ratPos = theRat.getPositionVec().add(0D, theRat.getHeight() / 2D, 0D);
		Vector3d gnawPos = new Vector3d(par1BlockPos.getX() + 0.5D, par1BlockPos.getY() + 0.5D, par1BlockPos.getZ() + 0.5D);
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
		else
		{
			float hardness = par1BlockState.getBlockHardness(theWorld, gnawSite);
			Material material = par1BlockState.getMaterial();
			if(hardness <= 2F && material == Material.WOOD)
				return true;
		}
		return shouldHeal(par1BlockState.getBlock());
	}
	
	public boolean shouldHeal(Block theBlock)
	{
		return gnawables.contains(theBlock) || theBlock instanceof CropsBlock || theBlock instanceof StemBlock;
	}
	
	private boolean canPathTo(BlockPos par1BlockPos)
	{
		return theRat.getNavigator().getPathToPos(par1BlockPos, (int)theRat.getAttributeValue(Attributes.FOLLOW_RANGE)) != null;
	}
}
