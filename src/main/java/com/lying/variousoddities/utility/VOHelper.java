package com.lying.variousoddities.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
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
		return player != null && (player.getType() == EntityType.PLAYER && ((Player)player).isCreative() || player.isSpectator());
	}
    
    /** Returns the modulus of the given value, accounting for errors in Java's function when dealing with negative values */
    public static int getPreservedMod(int val, int mod)
    {
    	int xMod = val%mod;
    	if(val < 0 && xMod != 0) xMod += mod;
    	return xMod;
    }
	
	public static Vec3 getVectorForRotation(float pitch, float yaw)
	{
        float f = Mth.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
	}
	
	public static LivingEntity getEntityLookTarget(@Nullable LivingEntity entityIn)
	{
		if(entityIn == null) return null;
		return getEntityLookTarget(entityIn, (entityIn instanceof Player ? ((Player)entityIn).getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue() : 5D));
	}
	
	public static LivingEntity getEntityLookTarget(LivingEntity entityIn, double range)
	{
		if(entityIn == null || entityIn.getLevel() == null || range < 0D) return null;
		
		Vec3 headPos = entityIn.getEyePosition(1F);
		Vec3 lookVec = entityIn.getLookAngle();
		if(entityIn instanceof Monster)
		{
			Monster living = (Monster)entityIn;
			LookControl helper = living.getLookControl();
			
			lookVec = new Vec3(helper.getWantedX(), helper.getWantedY(), helper.getWantedZ());
			lookVec = lookVec.subtract(headPos).normalize();
		}
		
		LivingEntity bestGuess = null;
		double smallestDist = Double.MAX_VALUE;
		for(LivingEntity nearby : entityIn.getLevel().getEntitiesOfClass(LivingEntity.class, entityIn.getBoundingBox().inflate(range), new Predicate<LivingEntity>()
				{
					public boolean apply(LivingEntity input)
					{
						Vec3 lookEnd = headPos.add(entityIn.getLookAngle().multiply(range, range, range));
						return entityIn.hasLineOfSight(input) && input.getBoundingBox().intersects(headPos, lookEnd);
					}
				}))
		{
			if(nearby == entityIn) continue;
			
			double distToEnt = entityIn.distanceTo(nearby);
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
	
	public static List<FormattedText> getWrappedText(Component text, Font font, int maxWidth)
	{
		Style style = text.getStyle();
        List<FormattedText> wrappedTextLines = new ArrayList<>();
        for(FormattedText line : font.getSplitter().splitLines(text, maxWidth, style))
            wrappedTextLines.add(line);
		return wrappedTextLines;
	}
	
	public static String obfuscateStringRandomly(String sentence, long seed, int odds, boolean preserveSpaces)
	{
		return obfuscateStringRandomly(sentence, ChatFormatting.GRAY + "", seed, odds, preserveSpaces);
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
			if(shouldObfuscate) obfuscated += ChatFormatting.OBFUSCATED + "" + letter + ChatFormatting.RESET + defaultFormatting;
			else obfuscated += letter;
		}
		return obfuscated;
	}
	
    /**
     * Creates a Vec3 using the pitch and yaw of the entities rotation.
     */
	public static Vec3 getVectorForRotation(float yaw)
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
    public static List<BlockPos> getReplaceableVolumeAround(BlockPos origin, Level world, int maxDist)
    {
    	return getReplaceableVolumeAround(origin, world, maxDist, new IVolumePredicate()
    		{
    			public boolean test(BlockPos pos, Level world){ return true; }
    		});
    }
    
    public static List<BlockPos> getReplaceableVolumeAround(BlockPos origin, Level world, int maxDist, IVolumePredicate predicateIn)
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
	    			BlockPos offset = pos.relative(face);
	    			if(offset.distSqr(origin) > (maxDist * maxDist))
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
       boolean test(BlockPos pos, Level world);
    }
    
    private static boolean isBlockReplaceable(BlockPos pos, Level world)
    {
    	return world.isEmptyBlock(pos) || world.getBlockState(pos).getMaterial().isReplaceable();
    }
    
    public static Player getPlayerEntityByName(Level worldIn, String playerName)
    {
    	for(Player player : worldIn.players())
    		if(player.getName().getString().equals(playerName))
    			return player;
    	return null;
    }
	
	public static double getMobAttackReachSqr(LivingEntity attacker, LivingEntity attackTarget)
	{
		return (double)(attacker.getBbWidth() * 2.0F * attacker.getBbWidth() * 2.0F + attackTarget.getBbWidth());
	}
	
	public static int getSkyLight(BlockPos pos, Level world)
	{
		if(!world.dimensionType().hasSkyLight())
			return 0;
		
		int light = world.getBrightness(LightLayer.SKY, pos) - world.getSkyDarken();
		if(light > 0)
		{
			float sunAngle = world.getSunAngle(1.0F);
		    float f1 = sunAngle < (float)Math.PI ? 0.0F : ((float)Math.PI * 2F);
		    sunAngle = sunAngle + (f1 - sunAngle) * 0.2F;
		    light = Math.round((float)light * Mth.cos(sunAngle));
		}
		
		return Mth.clamp(light, 0, 15);
	}
	
	private static final Map<EntityType<?>, String> SKULL_MAP = new HashMap<>();
	
	public static ItemStack getSkullFromEntity(LivingEntity entity)
	{
		if(entity.getType() == EntityType.WITHER_SKELETON)
			return new ItemStack(Items.WITHER_SKELETON_SKULL);
		else if(entity.getType() == EntityType.SKELETON)
			return new ItemStack(Items.SKELETON_SKULL);
		else if(entity.getType() == EntityType.ZOMBIE)
			return new ItemStack(Items.ZOMBIE_HEAD);
		else if(entity.getType() == EntityType.CREEPER)
			return new ItemStack(Items.CREEPER_HEAD);
		else if(entity.getType() == EntityType.ENDER_DRAGON)
			return new ItemStack(Items.DRAGON_HEAD);
		else
		{
			ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
			CompoundTag data = new CompoundTag();
			if(SKULL_MAP.containsKey(entity.getType()))
				data.putString("SkullOwner", SKULL_MAP.get(entity.getType()));
			else if(entity.getType() == EntityType.PLAYER)
				data.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), ((Player)entity).getGameProfile()));
			stack.setTag(data);
			return stack;
		}
	}
	
	static
	{
		SKULL_MAP.put(EntityType.BLAZE, "MHF_Blaze");
		SKULL_MAP.put(EntityType.CAVE_SPIDER, "MHF_CaveSpider");
		SKULL_MAP.put(EntityType.CHICKEN, "MHF_Chicken");
		SKULL_MAP.put(EntityType.COW, "MHF_Cow");
		SKULL_MAP.put(EntityType.ENDERMAN, "MHF_Enderman");
		SKULL_MAP.put(EntityType.GHAST, "MHF_Ghast");
		SKULL_MAP.put(EntityType.IRON_GOLEM, "MHF_Golem");
		SKULL_MAP.put(EntityType.MAGMA_CUBE, "MHF_LavaSlime");
		SKULL_MAP.put(EntityType.MOOSHROOM, "MHF_MushroomCow");
		SKULL_MAP.put(EntityType.OCELOT, "MHF_Ocelot");
		SKULL_MAP.put(EntityType.PIG, "MHF_Pig");
		SKULL_MAP.put(EntityType.PIGLIN, "MHF_PigZombie");
		SKULL_MAP.put(EntityType.SHEEP, "MHF_Sheep");
		SKULL_MAP.put(EntityType.SLIME, "MHF_Slime");
		SKULL_MAP.put(EntityType.SPIDER, "MHF_Spider");
		SKULL_MAP.put(EntityType.SQUID, "MHF_Squid");
		SKULL_MAP.put(EntityType.VILLAGER, "MHF_Villager");
	}
}