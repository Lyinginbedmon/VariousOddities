package com.lying.variousoddities.entity.ai;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import com.lying.variousoddities.api.event.LivingWakeUpEvent;
import com.lying.variousoddities.potion.PotionSleep;
import com.lying.variousoddities.types.EnumCreatureType;
import com.lying.variousoddities.world.savedata.TypesManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraftforge.common.MinecraftForge;

public class EntityAISleep extends Goal
{
	private final MobEntity theMob;
	private final Random rand;
	
	/** If the mob should be sleeping right now */
	private boolean sleeping = false;
	
	/** If the current sleep period was started by the Sleep potion effect.<br>
	 * If true, sleep will forcibly end when the effect is removed. */
	private boolean isMagicSleep = false;
	
	public EntityAISleep(MobEntity livingIn)
	{
		this.theMob = livingIn;
		this.rand = livingIn.getRNG();
		this.setMutexFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.TARGET));
	}
	
	public boolean shouldExecute()
	{
		return canSleep() && sleepState();
	}
	
	public boolean shouldContinueExecuting()
	{
		return isMagicSleep ? (PotionSleep.hasSleepEffect(theMob) || MinecraftForge.EVENT_BUS.post(new LivingWakeUpEvent(theMob, false))) : sleepState();
	}
	
	/**
	 * Reset the task's internal state. Called when this task is interrupted by
	 * another one
	 */
	public void resetTask()
	{
		this.sleeping = false;
	}
	
	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting()
	{
		theMob.getNavigator().clearPath();
		if(theMob.getAttackTarget() != null)
			theMob.setAttackTarget(null);
	}
	
	public void tick()
	{
		if(rand.nextInt(30) == 0)
			spawnParticles(theMob, rand);
	}
	
	/**
	 * Sets the sleeping flag.
	 */
	public void setSleeping(boolean sleepIn){ this.sleeping = canSleep() && sleepIn; }
	public boolean sleepState(){ return this.sleeping; }
	
	public boolean canSleep()
	{
		this.isMagicSleep = PotionSleep.hasSleepEffect(theMob);
		TypesManager manager = TypesManager.get(theMob.getEntityWorld());
		List<EnumCreatureType> types = manager.getMobTypes(theMob);
		if(this.isMagicSleep)
			for(EnumCreatureType type : types)
				;
		
		return EnumCreatureType.ActionSet.fromTypes(types).sleeps();
	}
	
	public static void spawnParticles(LivingEntity theMob, Random rand)
	{
		for(int i=0; i<3; i++)
		{
//			double xPos = theMob.getPosX() + rand.nextDouble() - 0.5D;
//			double yPos = theMob.getPosYEye() + (rand.nextDouble() - 0.5D) * 0.2D;
//			double zPos = theMob.getPosZ() + rand.nextDouble() - 0.5D;
//			PacketHandler.sendToNearby(new PacketCustomParticle(theMob.getEntityWorld(), VOParticle.SLEEP, xPos, yPos, zPos, 0D, 0D, 0D), theMob.getEntityWorld(), theMob);
		}
	}
}
