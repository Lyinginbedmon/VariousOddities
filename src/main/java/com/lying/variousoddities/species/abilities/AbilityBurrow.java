package com.lying.variousoddities.species.abilities;

import java.util.Arrays;
import java.util.List;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityBurrow extends AbilityMoveMode implements IPhasingAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "burrow");
	private static final List<Material> DIRT_BLOCKS = Arrays.asList(Material.EARTH, Material.ORGANIC, Material.SNOW_BLOCK, Material.ICE, Material.SAND);
	
	private boolean canBurrowStone = false;
	private boolean leavesTunnel = false;
	
	public AbilityBurrow()
	{
		super(REGISTRY_NAME);
	}
	
	public AbilityBurrow(boolean stone, boolean tunnel)
	{
		this();
		this.canBurrowStone = stone;
		this.leavesTunnel = tunnel;
	}
	
	public int compare(Ability abilityIn)
	{
		AbilityBurrow burrow = (AbilityBurrow)abilityIn;
		if(burrow.canBurrowStone && !canBurrowStone)
			return -1;
		else if(canBurrowStone && !burrow.canBurrowStone)
			return 1;
		
		return 0;
	}
	
	public Nature getDefaultNature(){ return Nature.EXTRAORDINARY; }
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability.varodd.burrow."+(isActive() ? "active" : "inactive")); }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		if(canBurrowStone)
			compound.putBoolean("AllowStone", canBurrowStone);
		if(leavesTunnel)
			compound.putBoolean("MineBlocks", leavesTunnel);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		canBurrowStone = compound.getBoolean("AllowStone");
		leavesTunnel = compound.getBoolean("MineBlocks");
	}
	
	public boolean preventsFallDamage(Ability abilityIn){ return false; }
	
	public void trigger(LivingEntity entity, Dist side)
	{
		if(entity.getType() == EntityType.PLAYER)
		{
			PlayerEntity player = (PlayerEntity)entity;
			if(!this.isActive)
			{
				// Set crawling pose
				player.setForcedPose(Pose.SWIMMING);
			}
			else
			{
				// Unset crawling pose
				if(player.getForcedPose() == Pose.SWIMMING)
					player.setForcedPose(null);
			}
		}
		super.trigger(entity, side);
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::tunnelBlocks);
	}
	
	public void tunnelBlocks(LivingUpdateEvent event)
	{
		LivingEntity living = event.getEntityLiving();
		if(AbilityRegistry.hasAbility(living, REGISTRY_NAME) && AbilityRegistry.getAbilityByName(living, REGISTRY_NAME).isActive())
		{
			living.setPose(Pose.SWIMMING);
			AbilityBurrow burrow = (AbilityBurrow)AbilityRegistry.getAbilityByName(living, REGISTRY_NAME);
			if(burrow.leavesTunnel)
			{
				for(int i=0; i<Math.ceil(living.getHeight()); i++)
				{
					BlockPos pos = living.getPosition().add(0, i, 0);
					if(burrow.canPhase(living.getEntityWorld(), pos, living))
						living.getEntityWorld().destroyBlock(pos, true, living);
				}
			}
		}
	}
	
	public boolean isPhaseable(IBlockReader worldIn, BlockPos pos, LivingEntity entity)
	{
		BlockState state = worldIn.getBlockState(pos);
		Material material = state.getMaterial();
		if(((canBurrowStone && material == Material.ROCK) || DIRT_BLOCKS.contains(material)) && state.getHarvestLevel() <= 1)
			return entity.getPosition().getY() <= pos.getY() || entity.isSneaking();
		return false;
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public ToggledAbility createAbility(CompoundNBT compound)
		{
			return new AbilityBurrow(compound.getBoolean("AllowStone"), compound.getBoolean("MineBlocks"));
		}
	}
}
