package com.lying.variousoddities.species.abilities;

import com.lying.variousoddities.init.VOBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityBurrow extends AbilityMoveMode implements IPhasingAbility
{
	private boolean canBurrowStone = false;
	private boolean leavesTunnel = false;
	
	public AbilityBurrow()
	{
		super();
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
	
	public Component translatedName(){ return Component.translatable("ability.varodd.burrow."+(isActive() ? "active" : "inactive")); }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		if(canBurrowStone)
			compound.putBoolean("AllowStone", canBurrowStone);
		if(leavesTunnel)
			compound.putBoolean("MineBlocks", leavesTunnel);
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
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
			Player player = (Player)entity;
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
	
	public void tunnelBlocks(LivingTickEvent event)
	{
		LivingEntity living = event.getEntity();
		AbilityBurrow burrow = (AbilityBurrow)AbilityRegistry.getAbilityByMapName(living, getRegistryName());
		if(burrow != null && burrow.isActive())
		{
			living.setPose(Pose.SWIMMING);
			if(burrow.leavesTunnel)
			{
				for(int i=0; i<Math.ceil(living.getBbHeight()); i++)
				{
					BlockPos pos = living.blockPosition().offset(0, i, 0);
					if(burrow.canPhase(living.getLevel(), pos, living))
						living.getLevel().destroyBlock(pos, true, living);
				}
			}
		}
	}
	
	public boolean isPhaseable(BlockGetter worldIn, BlockPos pos, LivingEntity entity)
	{
		BlockState state = worldIn.getBlockState(pos);
		if(state.is(VOBlocks.UNPHASEABLE))
			return false;
		
		if(((canBurrowStone && isStoneOrEquivalent(state)) || isDirtOrEquivalent(state)) && isSoftMaterial(state))
			return entity.blockPosition().getY() <= pos.getY() || entity.isCrouching();
		return false;
	}
	
	private boolean isDirtOrEquivalent(BlockState state)
	{
		return state.is(BlockTags.SAND) || state.is(BlockTags.DIRT) || state.is(BlockTags.SNOW) || state.is(BlockTags.ICE);
	}
	
	private boolean isStoneOrEquivalent(BlockState state)
	{
		return state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(BlockTags.NEEDS_DIAMOND_TOOL);
	}
	
	private boolean isSoftMaterial(BlockState state)
	{
		return state.is(BlockTags.NEEDS_STONE_TOOL) || !(state.is(BlockTags.NEEDS_IRON_TOOL) || state.is(BlockTags.NEEDS_DIAMOND_TOOL));
	}
	
	public static class Builder extends ToggledAbility.Builder
	{
		public Builder(){ super(); }
		
		public ToggledAbility createAbility(CompoundTag compound)
		{
			return new AbilityBurrow(compound.getBoolean("AllowStone"), compound.getBoolean("MineBlocks"));
		}
	}
}
