package com.lying.variousoddities.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

public class VOHelper
{
	public static final DyeColor[] SORTED_DYES =
		{
				DyeColor.BLACK,
				DyeColor.GRAY,
				DyeColor.LIGHT_GRAY,
				DyeColor.WHITE,
				DyeColor.PINK,
				DyeColor.MAGENTA,
				DyeColor.PURPLE,
				DyeColor.BLUE,
				DyeColor.LIGHT_BLUE,
				DyeColor.CYAN,
				DyeColor.GREEN,
				DyeColor.LIME,
				DyeColor.YELLOW,
				DyeColor.ORANGE,
				DyeColor.RED,
				DyeColor.BROWN
		};
	
	public static final String NEWLINE = "/n";
	
    public static String getFormattedTime(int seconds)
    {
		int minutes = Math.floorDiv(seconds, 60);
		seconds -= minutes * 60;
		
		int hours = Math.floorDiv(minutes, 60);
		minutes -= hours * 60;
		
		if(hours > 0)
		{
			if(minutes > 0 || seconds > 0)
				return getTimeElement(hours) + ":" + getTimeElement(minutes) + ":" + getTimeElement(seconds);
			else
				return getTimeElement(hours) + "h";
		}
		else if(minutes > 0)
		{
			if(seconds > 0)
				return getTimeElement(minutes) + ":" + getTimeElement(seconds);
			else
				return getTimeElement(minutes) + "m";
		}
		else
			return getTimeElement(seconds) + "s";
    }
    
    private static String getTimeElement(int time)
    {
    	return (time < 10 ? "0" : "") + (time > 0 ? time : "0");
    }
	
	public static boolean isCreativeOrSpectator(@Nullable LivingEntity player)
	{
		return player != null && (player.getType() == EntityType.PLAYER && ((PlayerEntity)player).abilities.isCreativeMode || player.isSpectator());
	}
    
    /** Returns the modulus of the given value, accounting for errors in Java's function when dealing with negative values */
    public static int getPreservedMod(int val, int mod)
    {
    	int xMod = val%mod;
    	if(val < 0 && xMod != 0) xMod += mod;
    	return xMod;
    }
	
	public static Vector3d getVectorForRotation(float pitch, float yaw)
	{
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vector3d((double)(f1 * f2), (double)f3, (double)(f * f2));
	}
	
	public static LivingEntity getEntityLookTarget(@Nullable LivingEntity entityIn)
	{
		if(entityIn == null) return null;
		return getEntityLookTarget(entityIn, (entityIn instanceof PlayerEntity ? ((PlayerEntity)entityIn).getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() : 5D));
	}
	
	public static LivingEntity getEntityLookTarget(LivingEntity entityIn, double range)
	{
		if(entityIn == null || entityIn.getEntityWorld() == null || range < 0D) return null;
		
		Vector3d headPos = entityIn.getEyePosition(1F);
		Vector3d lookVec = entityIn.getLookVec();
		if(entityIn instanceof MobEntity)
		{
			MobEntity living = (MobEntity)entityIn;
			LookController helper = living.getLookController();
			
			lookVec = new Vector3d(helper.getLookPosX(), helper.getLookPosY(), helper.getLookPosZ());
			lookVec = lookVec.subtract(headPos).normalize();
		}
		
		LivingEntity bestGuess = null;
		double smallestDist = Double.MAX_VALUE;
		for(LivingEntity nearby : entityIn.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, entityIn.getBoundingBox().grow(range), new Predicate<LivingEntity>()
				{
					public boolean apply(LivingEntity input)
					{
						Vector3d lookEnd = headPos.add(entityIn.getLookVec().mul(range, range, range));
						return entityIn.canEntityBeSeen(input) && input.getBoundingBox().intersects(headPos, lookEnd);
					}
				}))
		{
			if(nearby == entityIn) continue;
			
			double distToEnt = entityIn.getDistance(nearby);
			if(smallestDist > distToEnt)
			{
				bestGuess = nearby;
				smallestDist = distToEnt;
			}
		}
		
		return bestGuess;
	}
	
	/**
	 * Breaks the given string down into chunks based on spaces and newlines
	 */
	public static List<String> getStringAsLimitedList(String sentence, int length)
	{
		return getStringAsLimitedList(sentence, length, false);
	}
	
	public static List<String> getStringAsLimitedList(String sentence, int length, boolean hardEdge)
	{
		List<String> fragments = new ArrayList<String>();
		String fragment = "";
		for(int index=0; index < sentence.length(); index++)
		{
			int space = length - fragment.length();
			if(hardEdge && (space < (sentence.indexOf(" ", index) - index) || space < (sentence.indexOf(NEWLINE, index) - index)))
			{
				fragments.add(fragment);
				fragment = "";
			}
			
			char letter = sentence.charAt(index);
			boolean isNewline = (index+NEWLINE.length()-1) < sentence.length() && sentence.substring(index, index+NEWLINE.length()).equalsIgnoreCase(NEWLINE);
			if((fragment.length() >= length && letter == " ".charAt(0)) || isNewline)
			{
				fragments.add(fragment);
				fragment = "";
				if(isNewline) index += NEWLINE.length() - 1;
			}
			else fragment += letter;
		}
		if(fragment.length() > 0) fragments.add(fragment);
		return fragments;
	}
	
	public static List<String> getStringFromNewlines(String sentence)
	{
		List<String> fragments = new ArrayList<String>();
		String fragment = "";
		for(int index=0; index < sentence.length(); index++)
		{
			char letter = sentence.charAt(index);
			if((index+NEWLINE.length()-1) < sentence.length() && sentence.substring(index, index+NEWLINE.length()).equalsIgnoreCase(NEWLINE))
			{
				fragments.add(fragment);
				index += NEWLINE.length()-1;
				fragment = "";
			}
			else fragment += letter;
		}
		if(fragment.length() > 0) fragments.add(fragment);
		return fragments;
	}
	
	public static List<ITextProperties> getWrappedText(ITextComponent text, FontRenderer font, int maxWidth)
	{
		Style style = text.getStyle();
        List<ITextProperties> wrappedTextLines = new ArrayList<>();
        for(ITextProperties line : font.getCharacterManager().func_238362_b_(text, maxWidth, style))
            wrappedTextLines.add(line);
		return wrappedTextLines;
	}
	
	public static String obfuscateStringRandomly(String sentence, long seed, int odds, boolean preserveSpaces)
	{
		return obfuscateStringRandomly(sentence, TextFormatting.GRAY + "", seed, odds, preserveSpaces);
	}
	/**
	 * Returns the given sentence with characters randomly obfuscated according to the given RNG seed and odds.
	 * @param sentence The message to obfuscate
	 * @param defaultFormatting The formatting the message should use (this is reset per character so must be specified)
	 * @param seed The RNG seed to use, specified for reliable obfuscation
	 * @param percentageObfuscated The percentage of the message that should be obfuscated 
	 * @param preserveSpaces Whether spaces should be left un-obfuscated
	 * @return
	 */
	public static String obfuscateStringRandomly(String sentence, String defaultFormatting, long seed, float percentageObfuscated, boolean preserveSpaces)
	{
		percentageObfuscated = Math.max(0F, Math.min(1F, percentageObfuscated));
		Random rand = new Random(seed);
		
		String obfuscated = defaultFormatting + "";
		for(int index=0; index<sentence.length(); index++)
		{
			String letter = sentence.charAt(index) + "";
			boolean shouldObfuscate = rand.nextFloat() < percentageObfuscated && (preserveSpaces ? !letter.equals(" ") : true);
			if(shouldObfuscate) obfuscated += TextFormatting.OBFUSCATED + "" + letter + TextFormatting.RESET + defaultFormatting;
			else obfuscated += letter;
		}
		return obfuscated;
	}
	
    /**
     * Creates a Vec3 using the pitch and yaw of the entities rotation.
     */
	public static Vector3d getVectorForRotation(float yaw)
    {
    	return getVectorForRotation(0F, yaw);
    }
	
	public static int getTotalXPForLevel(int levelIn)
	{
		int level2 = levelIn * levelIn;
		if(levelIn <= 15)
		{
			return level2 + (6 * levelIn);
		}
		else if(levelIn <= 30)
		{
			return (int)(2.5 * level2) - (int)(40.5 * levelIn) + 360;
		}
		else if(levelIn > 0)
		{
			return (int)(4.5 * level2) - (int)(162.5 * levelIn) + 2220;
		}
		return 0;
	}
	
	public static int getTotalXPForLevel(float level)
	{
		if(level > Math.floor(level))
		{
			int low = getTotalXPForLevel((int)Math.floor(level));
			int high = getTotalXPForLevel((int)Math.ceil(level));
			
			return low + (int)((high - low)*(level - low));
		}
		return getTotalXPForLevel((int)level);
	}
    
    private static final TreeMap<Integer, String> numerals = new TreeMap<Integer, String>();
    static
    {
    	numerals.put(1000,	"M");
    	numerals.put(900,	"CM");
    	numerals.put(500,	"D");
    	numerals.put(400,	"CD");
    	numerals.put(100,	"C");
    	numerals.put(90,	"XC");
    	numerals.put(50,	"L");
    	numerals.put(40,	"XL");
    	numerals.put(10,	"X");
    	numerals.put(9,		"IX");
    	numerals.put(5,		"V");
    	numerals.put(4,		"IV");
    	numerals.put(1,		"I");
    }
    
    public static String numberToNumerals(int number)
    {
    	number *= Math.signum(number);
    	
    	if(number == 0) return "?";
    	
    	int minKey = numerals.floorKey(number);
    	if(number == minKey) return numerals.get(number);
    	return numerals.get(minKey) + numberToNumerals(number - minKey);
    }
    
    /**
     * Returns a list of all contiguous replaceable blocks around the given point.<br>
     * Note: Higher maximum distances will dramatically increase CPU load.
     */
    public static List<BlockPos> getReplaceableVolumeAround(BlockPos origin, World world, int maxDist)
    {
    	return getReplaceableVolumeAround(origin, world, maxDist, new IVolumePredicate()
    		{
    			public boolean test(BlockPos pos, IWorld world){ return true; }
    		});
    }
    
    public static List<BlockPos> getReplaceableVolumeAround(BlockPos origin, World world, int maxDist, IVolumePredicate predicateIn)
    {
    	List<BlockPos> currentSet = new ArrayList<>();
    	List<BlockPos> nextSet = new ArrayList<>();
    	
    	List<BlockPos> volume = new ArrayList<>();
    	if(isBlockReplaceable(origin, world) && predicateIn.test(origin, world))
    	{
    		volume.add(origin);
    		currentSet.add(origin);
    	}
    	
    	/**
    	 * For each block in currentSet
    	 * 	Test every neighbour for air or replaceable
    	 * 	Add all valid neighbours to nextSet
    	 * Loop until nextSet empty
    	 */
    	while(!currentSet.isEmpty())
    	{
    		nextSet.clear();
	    	for(BlockPos pos : currentSet)
	    	{
	    		for(Direction face : Direction.values())
	    		{
	    			BlockPos offset = pos.offset(face);
	    			if(offset.distanceSq(origin) > (maxDist * maxDist))
	    				continue;
	    			
	    			if(isBlockReplaceable(offset, world) && predicateIn.test(offset, world) && !(nextSet.contains(offset) || volume.contains(offset)))
	    			{
	    				volume.add(offset);
	    				nextSet.add(offset);
	    			}
	    		}
	    	}
	    	currentSet.clear();
	    	currentSet.addAll(nextSet);
    	}
    	
    	return volume;
    }
    
    @FunctionalInterface
    public interface IVolumePredicate
    {
       boolean test(BlockPos pos, IWorld world);
    }
    
    private static boolean isBlockReplaceable(BlockPos pos, World world)
    {
    	return world.isAirBlock(pos) || world.getBlockState(pos).getMaterial().isReplaceable();
    }
    
    public static PlayerEntity getPlayerEntityByName(World worldIn, String playerName)
    {
    	for(PlayerEntity player : worldIn.getPlayers())
    		if(player.getName().getUnformattedComponentText().equals(playerName))
    			return player;
    	return null;
    }
    
    public static void addRotationToEntityHead(@Nullable LivingEntity entity, double yaw, double pitch)
    {
    	if(entity == null)
    		return;
    	
        double pitchAdj = pitch * 0.15D;
        double yawAdj = yaw * 0.15D;
        entity.rotationPitch = (float)((double)entity.rotationPitch + pitchAdj);
        entity.rotationYaw = (float)((double)entity.rotationYaw + yawAdj);
        entity.rotationPitch = MathHelper.clamp(entity.rotationPitch, -90.0F, 90.0F);
        
        entity.prevRotationPitch = (float)((double)entity.prevRotationPitch + pitchAdj);
        entity.prevRotationYaw = (float)((double)entity.prevRotationYaw + yawAdj);
        entity.prevRotationPitch = MathHelper.clamp(entity.prevRotationPitch, -90.0F, 90.0F);
        
        if(entity.getRidingEntity() != null)
        	entity.getRidingEntity().applyOrientationToEntity(entity);
    }
    
    public static void copyRotationFrom(Entity from, Entity to)
    {
    	to.rotationPitch = from.rotationPitch;
    	to.rotationYaw = from.rotationYaw;
		
		to.prevRotationPitch = from.prevRotationPitch;
		to.prevRotationYaw = from.prevRotationYaw;
		
		if(to instanceof LivingEntity && from instanceof LivingEntity)
		{
			LivingEntity livingTo = (LivingEntity)to;
			LivingEntity livingFrom = (LivingEntity)from;
			
			livingTo.rotationYawHead = livingFrom.rotationYawHead;
			livingTo.prevRotationYawHead = livingFrom.prevRotationYawHead;
			
			livingTo.renderYawOffset = livingFrom.renderYawOffset;
			livingTo.prevRenderYawOffset = livingFrom.prevRenderYawOffset;
			
			livingTo.limbSwing = livingFrom.limbSwing;
			livingTo.limbSwingAmount = livingFrom.limbSwingAmount;
			livingTo.prevLimbSwingAmount = livingFrom.prevLimbSwingAmount;
		}
    }
}