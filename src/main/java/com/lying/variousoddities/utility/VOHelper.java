package com.lying.variousoddities.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
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
	
	public static boolean isCreativeOrSpectator(PlayerEntity player)
	{
		return player.isCreative() || player.isSpectator();
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
	
	public static LivingEntity getEntityLookTarget(LivingEntity entityIn)
	{
		if(entityIn == null) return null;
		return getEntityLookTarget(entityIn, (entityIn instanceof PlayerEntity ? ((PlayerEntity)entityIn).getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() : 5D));
	}
	
	public static LivingEntity getEntityLookTarget(LivingEntity entityIn, double range)
	{
		if(entityIn == null || entityIn.getEntityWorld() == null || range < 0D) return null;
		
		Vector3d headPos = new Vector3d(entityIn.getPosX(), entityIn.getPosY() + entityIn.getEyeHeight(), entityIn.getPosZ());
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
		for(LivingEntity nearby : entityIn.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, entityIn.getBoundingBox().grow(range)))
		{
			if(nearby == entityIn || !entityIn.canEntityBeSeen(nearby)) continue;
			double distToEnt = entityIn.getDistance(nearby);
			if(nearby.getBoundingBox().contains(headPos.add(new Vector3d(lookVec.x * distToEnt, lookVec.y * distToEnt, lookVec.z * distToEnt))) && smallestDist > distToEnt)
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
}