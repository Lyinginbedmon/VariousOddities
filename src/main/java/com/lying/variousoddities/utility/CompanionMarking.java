package com.lying.variousoddities.utility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.lying.variousoddities.entity.IConfigurableMob;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Utility used for notifying companions of different information in the environment.
 * @author Lying
 *
 */
public class CompanionMarking
{
	public static MarkCategory markCategory = MarkCategory.MOTION;
	private static Mark currentMark = null;
	
	public static Mark[] markOptions = new Mark[]{Mark.CANCEL};
	public static int markIndex = 0;
	
	public static Object targetObj = null;
	
	public static List<UUID> nextMarkList = new ArrayList<>();
	public static Map<UUID, Long> lastMarkMap = new HashMap<>();
	
	/**
	 * Called when the companion key is pressed, adding/removing the target entity from the next mark list.
	 * @param playerIn
	 */
	public static void onCompanionKey(PlayerEntity playerIn, boolean sneakingIn)
	{
		if(sneakingIn)
		{
			nextMarkList.clear();
			playerIn.sendStatusMessage(new TranslationTextComponent("key.varodd:mark.clear", new Object[]{}), true);
		}
		else
		{
			Entity targetEntity = getPlayerTargetCompanion(playerIn, 16D);
			if(targetEntity != null && targetEntity.isAlive() && targetEntity instanceof IConfigurableMob && ((IConfigurableMob)targetEntity).shouldRespondToPlayer(playerIn))
			{
				UUID targetUUID = targetEntity.getUniqueID();
				if(nextMarkList.contains(targetUUID))
				{
					nextMarkList.remove(targetUUID);
					playerIn.sendStatusMessage(new TranslationTextComponent("key.varodd:mark.remove", new Object[]{targetEntity.getName()}), true);
				}
				else
				{
					nextMarkList.add(targetUUID);
					
					if(targetEntity instanceof MobEntity)
						((MobEntity)targetEntity).playAmbientSound();
					
					playerIn.sendStatusMessage(new TranslationTextComponent("key.varodd:mark.add", new Object[]{targetEntity.getName()}), true);
				}
			}
		}
	}
	
	/**
	 * Called when the marking key is pressed, altering the current mark target.
	 * @param playerIn
	 */
	public static void onMarkKey(PlayerEntity playerIn)
	{
		if(currentMark == null)
		{
			// Step 1: Identify a rough guess of who might receive the command
			List<Entity> markTargets = new ArrayList<>();
			if(nextMarkList.isEmpty())
				markTargets = playerIn.getEntityWorld().getEntitiesWithinAABB(Entity.class, playerIn.getBoundingBox().grow(32D), new Predicate<Entity>()
				{
					public boolean apply(Entity input)
					{
						return input instanceof IConfigurableMob && ((IConfigurableMob)input).shouldRespondToPlayer(playerIn);
					}
				});
			else
			{
				for(UUID id : nextMarkList)
					for(Entity ent : playerIn.getEntityWorld().getEntitiesWithinAABB(Entity.class, playerIn.getBoundingBox().grow(32D)))
						if(ent.isAlive() && ent.getUniqueID().equals(id))
						{
							markTargets.add(ent);
							break;
						}
			}
			if(markTargets.isEmpty()) return;
			
			// Step 2: Identify all commands that at least one person in the target group can receive
			List<Mark> possibleMarks = getPossibleCommands(markTargets);
			if(possibleMarks.isEmpty()) return;
			
			// Step 3: Identify what the player is targeting for this command
			Entity targetEnt = getPlayerLookTarget(playerIn, 16D);
			BlockPos targetPos = getPlayerLookPos(playerIn, 16D);
			if(targetEnt != null)
				targetObj = targetEnt;
			else if(targetPos != null)
				targetObj = targetPos;
			else
				targetObj = playerIn;
			
			// Step 4: Based on the now-known target object, reduce the list of possible commands by excluding invalid ones
			possibleMarks.retainAll(markCategory.getMarksForObject(targetObj));
			possibleMarks.sort(new Comparator<Mark>()
			{
				public int compare(Mark o1, Mark o2)
				{
					int ind0 = o1.index;
					int ind1 = o2.index;
					return ind0 > ind1 ? 1 : ind1 > ind0 ? -1 : 0;
				}
			});
			possibleMarks.add(Mark.CANCEL);
			markOptions = possibleMarks.toArray(new Mark[0]);
		}
		
//		if(currentMark != null)
//			playerIn.sendStatusMessage(currentAction().translate(targetObj, false).setStyle(STYLE_SELECT), true);
	}
	
	/** Returns a list of commands acceptable by any current mark target */
	public static List<Mark> getPossibleCommands(List<Entity> markTargets)
	{
		List<Mark> possibleMarks = new ArrayList<>();
		for(Entity mob : markTargets)
			if(mob instanceof IConfigurableMob)
			{
				for(Mark command : ((IConfigurableMob)mob).allowedMarks())
					if(!possibleMarks.contains(command))
						possibleMarks.add(command);
			}
			else
			{
				;
			}
		return possibleMarks;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void incMarkIndex(PlayerEntity playerIn, int par1Int)
	{
		if(currentMark == null) return;
		
		markIndex += par1Int;
		int len = markOptions.length;
		if(markIndex < 0)
			markIndex = len - 1;
		else
			markIndex = markIndex % len;
		
//		playerIn.sendStatusMessage(currentAction().translate(targetObj, false).setStyle(STYLE_SELECT), true);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static Mark currentAction()
	{
		return currentMark == null ? null : markOptions[markIndex];
	}
	
	/**
	 * Called when a marking attempt times out, issues the relevant marking, and reports the result to the player.
	 * @param player
	 */
	public static void issueMarking(PlayerEntity player)
	{
		if(currentMark == null) return;
		
		if(currentAction() != Mark.CANCEL)
		{
			player.sendStatusMessage(currentAction().translate(targetObj, true), true);
			notifyCompanions(player, currentAction(), targetObj);
		}
		
		currentMark = null;
		targetObj = null;
		markIndex = 0;
	}
	
	/**
	 * Notifies either all nearby companions or those marked for the next mark only.
	 * @param player
	 * @param pos
	 * @param entity
	 */
	private static void notifyCompanions(PlayerEntity player, Mark markType, Object values)
	{
		if(!player.getEntityWorld().isRemote || currentMark == null || markType == Mark.CANCEL)
			return;
		
//		MarkingEvent event = new MarkingEvent(player, markType, values);
//		MinecraftForge.EVENT_BUS.post(event);
//		if(event.getResult() == Result.DENY) return;
		
		World world = player.getEntityWorld();
		long time = world.getGameTime();
		if(nextMarkList.isEmpty())
		{
			for(Entity ent : world.getEntitiesWithinAABB(Entity.class, player.getBoundingBox().grow(32D)))
			{
				if(ent instanceof IConfigurableMob)
				{
					IConfigurableMob mob = (IConfigurableMob)ent;
					if(mob.shouldRespondToPlayer(player) && mob.shouldRespondToMark(markType, values))
						lastMarkMap.put(ent.getUniqueID(), time);
				}
			}
			
//			PacketHandler.sendToServer(new PacketMark(currentAction(), targetObj));
		}
		else
		{
			for(Entity ent : world.getEntitiesWithinAABB(Entity.class, player.getBoundingBox().grow(64D)))
			{
				if(nextMarkList.contains(ent.getUniqueID()) && ((IConfigurableMob)ent).shouldRespondToMark(markType, values))
				{
//					PacketHandler.sendToServer(new PacketMark(currentAction(), targetObj));
					lastMarkMap.put(ent.getUniqueID(), time);
				}
			}
		}
		
		nextMarkList.clear();
	}
	
	/**
	 * Returns the closest companion mob within range of the player's look vector.
	 * @param playerIn
	 * @param maxRange
	 * @return
	 */
	private static Entity getPlayerTargetCompanion(PlayerEntity playerIn, double maxRange)
	{
        List<Entity> list = playerIn.getEntityWorld().getEntitiesInAABBexcluding(playerIn, playerIn.getBoundingBox().grow(maxRange), Predicates.and(Predicates.not(Entity::isSpectator), new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity input)
            {
                return input != null && input.isAlive();// && input instanceof IConfigurableMob && ((IConfigurableMob)input).shouldRespondToPlayer(playerIn);
            }
        }));
        if(list.isEmpty())
        	return null;
        
        Vector3d eyePos = new Vector3d(playerIn.getPosX(), playerIn.getPosYEye(), playerIn.getPosZ());
        Vector3d lookVec = playerIn.getLook(1.0F);
        
        Entity targetEntity = null;
        double minDist = Double.MAX_VALUE;
        for(Entity entity : list)
        {
        	Vector3d entityCore = new Vector3d(entity.getPosX(), entity.getPosY() + entity.getHeight() / 2, entity.getPosZ());
        	double distToEntity = eyePos.distanceTo(entityCore);
        	Vector3d lookAtEntity = eyePos.add(lookVec.x * distToEntity, lookVec.y * distToEntity, lookVec.z * distToEntity);
        	
        	if(entity.getBoundingBox().contains(lookAtEntity) && distToEntity < minDist)
        	{
        		targetEntity = entity;
        		minDist = distToEntity;
        	}
        }
        
		return targetEntity;
	}
	
	private static Entity getPlayerLookTarget(PlayerEntity playerIn, double maxRange)
	{
		if(playerIn == null) return null;
        Vector3d eyePos = new Vector3d(playerIn.getPosX(), playerIn.getPosYEye(), playerIn.getPosZ());
        Vector3d lookVec = playerIn.getLookVec();
        lookVec = new Vector3d(eyePos.x + lookVec.x * maxRange, eyePos.y + lookVec.y * maxRange, eyePos.z + lookVec.z * maxRange);
        
        RayTraceResult trace = playerIn.pick(maxRange, 1F, true);
        if(trace != null && trace.getType() == RayTraceResult.Type.ENTITY) return ((EntityRayTraceResult)trace).getEntity();
        
		return null;
	}
	
	private static BlockPos getPlayerLookPos(PlayerEntity playerIn, double maxRange)
	{
		if(playerIn == null) return null;
        Vector3d eyePos = new Vector3d(playerIn.getPosX(), playerIn.getPosYEye(), playerIn.getPosZ());
        Vector3d lookVec = playerIn.getLookVec();
        lookVec = new Vector3d(eyePos.x + lookVec.x * maxRange, eyePos.y + lookVec.y * maxRange, eyePos.z + lookVec.z * maxRange);
        
        RayTraceResult trace = playerIn.pick(maxRange, 1F, true);
        if(trace != null && trace.getType() == RayTraceResult.Type.BLOCK) return ((BlockRayTraceResult)trace).getPos();
        
		return null;
	}
	
	/**
	 * Returns the list of entity UUIDs that received mark alerts within the last 5 seconds.
	 * @param time
	 * @return
	 */
	public static List<UUID> getLatestMarks(long time)
	{
		List<UUID> latest = new ArrayList<>();
		List<UUID> elapsed = new ArrayList<>();
		
		for(UUID id : lastMarkMap.keySet())
			if(time - lastMarkMap.get(id) < (Reference.Values.TICKS_PER_SECOND * 5))
				latest.add(id);
			else
				elapsed.add(id);
		
		for(UUID elapse : elapsed)
			lastMarkMap.remove(elapse);
		
		return latest;
	}
	
	public static int getAlertStatus(Entity entityIn)
	{
		for(UUID entityID : nextMarkList)
			if(entityIn.getUniqueID().equals(entityID))
				return 0;
		
		for(UUID entityID : getLatestMarks(entityIn.getEntityWorld().getGameTime()))
			if(entityIn.getUniqueID().equals(entityID))
				return 1;
		
		return -1;
	}
	
	/**
	 * Returns the time remaining on the entity's current alert icon, counting up.<br>
	 * Returns -1 if the alert icon is not temporary.
	 * @param entityIn
	 * @return
	 */
	public static int getAlertTime(Entity entityIn)
	{
		for(UUID entityID : nextMarkList)
			if(entityIn.getUniqueID().equals(entityID))
				return -1;
		
		long time = entityIn.getEntityWorld().getGameTime();
		for(UUID entityID : getLatestMarks(time))
			if(entityIn.getUniqueID().equals(entityID))
				return (int)(time - lastMarkMap.get(entityID));
		
		return 0;
	}
	
	private static final Predicate<Object> isMob = new Predicate<Object>()
			{
				public boolean apply(Object input)
				{
					return input instanceof LivingEntity;
				}
			};
	private static final Predicate<Object> isItem = new Predicate<Object>()
			{
				public boolean apply(Object input)
				{
					return input instanceof ItemEntity;
				}
			};
	private static final Predicate<Object> isPosition = new Predicate<Object>()
			{
				public boolean apply(Object input)
				{
					return input instanceof BlockPos;
				}
			};
	private static final Predicate<Object> isSelf = new Predicate<Object>()
			{
				public boolean apply(Object input)
				{
					return input == Minecraft.getInstance().player;
				}
			};
	
	public static enum MarkCategory
	{
		MOTION(Mark.GO_TO_POS, Mark.GO_TO_MOB, Mark.FOLLOW_MOB, Mark.MOUNT_MOB, Mark.DISMOUNT),
		COMBAT(Mark.ATTACK_MOB, Mark.CEASE_ATTACKING_MOB, Mark.CEASE_ATTACK, Mark.GUARD_MOB, Mark.GUARD_POS),
		UTILITY(Mark.PICK_UP_ITEM, Mark.EQUIP_ITEM, Mark.ACTIVATE_POS, Mark.MINE_POS, Mark.QUARRY_POS);
		
		private final Mark[] commands;
		
		private MarkCategory(Mark... commandsIn)
		{
			List<Mark> marks = Lists.newArrayList(commandsIn);
			marks.add(Mark.CANCEL);
			this.commands = marks.toArray(new Mark[0]);
		}
		
		@OnlyIn(Dist.CLIENT)
		public List<Mark> getMarksForObject(Object object)
		{
			List<Mark> marks = Lists.newArrayList();
			for(Mark mark : commands)
				if(mark.predicate.apply(object))
					marks.add(mark);
			return marks;
		}
	}
	
	public static enum Mark
	{
		GO_TO_MOB(true, true, 0, CompanionMarking.isMob),
		GO_TO_POS(false, true, 1, CompanionMarking.isPosition),
		GUARD_MOB(true, false, 2, CompanionMarking.isMob),
		GUARD_POS(false, false, 3, CompanionMarking.isPosition),
		FOLLOW_MOB(true, true, 4, CompanionMarking.isMob),
		MINE_POS(false, true, 5, CompanionMarking.isPosition),
		QUARRY_POS(false, false, 6, CompanionMarking.isPosition),
		ATTACK_MOB(true, true, 7, Predicates.and(CompanionMarking.isMob, Predicates.not(CompanionMarking.isSelf))),
		CEASE_ATTACK(true, true, 8, CompanionMarking.isSelf),
		CEASE_ATTACKING_MOB(true, true, 9, Predicates.and(CompanionMarking.isMob, Predicates.not(CompanionMarking.isSelf))),
		MOUNT_MOB(true, false, 10, Predicates.and(CompanionMarking.isMob, new Predicate<Object>()
				{
					public boolean apply(Object object)
					{
						LivingEntity living = (LivingEntity)object;
						if(!living.isBeingRidden())
						{
							// Disallow mounting mobs personally engaged in combat
							if(!(living instanceof MobEntity) || ((MobEntity)living).getAttackTarget() == null)
							{
								Scoreboard scoreboard = living.getEntityWorld().getScoreboard();
								ScorePlayerTeam livingTeam = scoreboard.getPlayersTeam(living.getCachedUniqueIdString());
								
								// Disallow mounting mobs from opposing teams
								return livingTeam == null || livingTeam.isSameTeam(scoreboard.getPlayersTeam(Minecraft.getInstance().player.getName().getString()));
							}
						}
						else if(living.getType() == EntityType.BOAT)
							return ((BoatEntity)object).getPassengers().size() < 2;
						return false;
					}
				})),
		DISMOUNT(true, true, 11, CompanionMarking.isMob),
		PICK_UP_ITEM(true, true, 12, CompanionMarking.isItem),
		EQUIP_ITEM(true, false, 13, CompanionMarking.isItem),
		ACTIVATE_POS(false, true, 14, CompanionMarking.isPosition),
		CANCEL(false, true, 15, Predicates.alwaysTrue());
		
		private final boolean isEntity;
		private final boolean isSimple;
		private final int iconIndex;
		private final Predicate<Object> predicate;
		public final int index;
		
		private Mark(boolean isEntityIn, boolean isSimpleIn, int iconIndexIn, Predicate<Object> predicateIn)
		{
			this.isEntity = isEntityIn;
			this.isSimple = isSimpleIn;
			this.iconIndex = iconIndexIn;
			this.predicate = predicateIn;
			this.index = Mark.values().length;
		}
		
		public int iconIndex(){ return this.iconIndex; }
		
		public boolean isSimple(){ return this.isSimple; }
		
		@OnlyIn(Dist.CLIENT)
		public boolean valid(Object object){ return predicate.apply(object); }
		
		@OnlyIn(Dist.CLIENT)
		public TranslationTextComponent translate(Object obj, boolean success)
		{
			if(this != CANCEL)
				return new TranslationTextComponent("key."+Reference.ModInfo.MOD_PREFIX+"mark."+name().toLowerCase() + (success ? ".success" : ""), getTranslationIdentifier(isEntity, obj));
			else
				return new TranslationTextComponent("key."+Reference.ModInfo.MOD_PREFIX+"mark."+name().toLowerCase() + (success ? ".success" : ""));
		}
		
		@OnlyIn(Dist.CLIENT)
		public static ITextComponent getTranslationIdentifier(boolean isEntity, Object obj)
		{
			if(isEntity)
			{
				if(obj == Minecraft.getInstance().player)
					return new TranslationTextComponent("key."+Reference.ModInfo.MOD_PREFIX+"mark.self");
				else
					return ((Entity)obj).getDisplayName();
			}
			else
			{
				BlockPos pos = (BlockPos)obj;
				return new StringTextComponent("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]");
			}
		}
	}
}
