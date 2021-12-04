package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.proxy.CommonProxy;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPossessionClick
{
	private static UUID PLAYER_UUID = UUID.fromString("0f8c1f59-27a4-4315-8a2a-ff558477a75a");
	private boolean lClick, rClick;
	
	public PacketPossessionClick(){ }
	public PacketPossessionClick(boolean lClick, boolean rClick)
	{
		this.lClick = lClick;
		this.rClick = rClick;
	}
	
	public static PacketPossessionClick decode(PacketBuffer par1Buffer)
	{
		PacketPossessionClick packet = new PacketPossessionClick();
		packet.lClick = par1Buffer.readBoolean();
		packet.rClick = par1Buffer.readBoolean();
		return packet;
	}
	
	public static void encode(PacketPossessionClick msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeBoolean(msg.lClick);
		par1Buffer.writeBoolean(msg.rClick);
	}
	
	public static void handle(PacketPossessionClick msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			PlayerEntity player = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(player != null && PlayerData.forPlayer(player) != null)
			{
				LivingEntity ent = PlayerData.forPlayer(player).getPossessed();
				if(ent != null && ent instanceof MobEntity)
				{
					MobEntity mob = (MobEntity)ent;
					World world = mob.getEntityWorld();
					RayTraceResult trace = createRayTrace(mob, world, 5D);
					
					if(msg.lClick)
						handleLeftClick(mob, world, trace);
					
					if(msg.rClick)
						handleRightClick(mob, world, trace);
				}
			}
		}
		
		context.setPacketHandled(true);
	}
	
	private static RayTraceResult createRayTrace(MobEntity mob, World world, double range)
	{
		Vector3d eyePos = new Vector3d(mob.getPosX(), mob.getPosYEye(), mob.getPosZ());
		Vector3d lookVec = mob.getLookVec().mul(range, range, range);
		Vector3d endPos = eyePos.add(lookVec);
		RayTraceResult traceBlocks = mob.pick(range, 0F, false);
		if(traceBlocks.getType() != RayTraceResult.Type.MISS)
			endPos = traceBlocks.getHitVec();
		
		RayTraceResult traceEntities = ProjectileHelper.rayTraceEntities(world, mob, eyePos, endPos, mob.getBoundingBox().grow(range + 1D), (entity) -> {
            return !entity.isSpectator() && entity.canBeCollidedWith();
         });
		if(traceEntities != null)
			return traceEntities;
		
		return traceBlocks;
	}
	
	private static PlayerEntity getFakePlayer(MobEntity mob, ServerWorld world)
	{
		PlayerEntity fakePlayer = FakePlayerFactory.get(world, new GameProfile(PLAYER_UUID, "possessed_mob"));
		fakePlayer.copyLocationAndAnglesFrom(mob);
		for(EquipmentSlotType slot : EquipmentSlotType.values())
			fakePlayer.setItemStackToSlot(slot, mob.getItemStackFromSlot(slot));
		return fakePlayer;
	}
	
	private static void handleLeftClick(MobEntity mob, World world, RayTraceResult trace)
	{
		mob.swing(Hand.MAIN_HAND, true);
		
		if(trace == null)
			return;
		
		PlayerEntity fakePlayer = getFakePlayer(mob, (ServerWorld)world);
		switch(trace.getType())
		{
			case BLOCK:
				BlockPos pos = ((BlockRayTraceResult)trace).getPos();
				BlockState state = world.getBlockState(pos);
				state.onBlockClicked(world, pos, fakePlayer);
				break;
			case ENTITY:
				Entity targetEnt = ((EntityRayTraceResult)trace).getEntity();
				mob.attackEntityAsMob(targetEnt);
				break;
			case MISS:
			default:
				break;
		}
		fakePlayer.remove();
	}
	
	private static void handleRightClick(MobEntity mob, World world, RayTraceResult trace)
	{
		if(trace == null)
			return;
		
		PlayerEntity fakePlayer = getFakePlayer(mob, (ServerWorld)world);
		switch(trace.getType())
		{
			case BLOCK:
				BlockPos pos = ((BlockRayTraceResult)trace).getPos();
				BlockState state = world.getBlockState(pos);
				swingHandConditonal(mob, state.onBlockActivated(world, fakePlayer, Hand.OFF_HAND, (BlockRayTraceResult)trace), Hand.MAIN_HAND);
				break;
			case ENTITY:
				Entity targetEnt = ((EntityRayTraceResult)trace).getEntity();
				if(targetEnt == null || !(targetEnt instanceof LivingEntity))
					return;
				ItemStack heldItem = mob.getHeldItem(Hand.OFF_HAND);
				swingHandConditonal(mob, heldItem.interactWithEntity(fakePlayer, (LivingEntity)targetEnt, Hand.OFF_HAND), Hand.OFF_HAND);
				break;
			case MISS:
			default:
				break;
		}
		fakePlayer.remove();
	}
	
	private static void swingHandConditonal(MobEntity mob, ActionResultType result, Hand hand)
	{
		switch(result)
		{
			case FAIL:
			case PASS:
				break;
			case CONSUME:
			case SUCCESS:
				mob.swing(hand, true);
				break;
			default:
				break;
		}
	}
}
