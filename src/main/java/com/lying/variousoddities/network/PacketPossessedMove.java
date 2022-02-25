package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.PlayerData;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SMoveVehiclePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketPossessedMove
{
	private double x, y, z;
	private float yaw, pitch;
	
	public void process(PacketPossessedMove msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		if(!context.getDirection().getReceptionSide().isServer())
			return;
		
		ServerPlayerEntity player = context.getSender();
		PlayerData data = PlayerData.forPlayer(player);
		if(!data.isPossessing())
			return;
		
		LivingEntity possessed = data.getPossessed();
		Entity lowestRiding = possessed.getLowestRidingEntity();
		double lowestRiddenX = lowestRiding.getPosX();
		double lowestRiddenX1 = lowestRiding.getPosX();
		double lowestRiddenY = lowestRiding.getPosY();
		double lowestRiddenY1 = lowestRiding.getPosY();
		double lowestRiddenZ = lowestRiding.getPosZ();
		double lowestRiddenZ1 = lowestRiding.getPosZ();
		boolean vehicleFloating = false;
		
		if(lowestRiding != null && lowestRiding.getType() != EntityType.PLAYER)
		{
			MinecraftServer server = player.getServer();
			ServerWorld world = player.getServerWorld();
			double posX = lowestRiding.getPosX();
			double posY = lowestRiding.getPosY();
			double posZ = lowestRiding.getPosZ();
			double moveX = msg.x;
			double moveY = msg.y;
			double moveZ = msg.z;
			float yaw = msg.yaw;
			float pitch = msg.pitch;
			double motionX = moveX - lowestRiddenX;
			double motionY = moveY - lowestRiddenY;
			double motionZ = moveZ - lowestRiddenZ;
			double d9 = lowestRiding.getMotion().lengthSquared();
			double magnitude = motionX * motionX + motionY * motionY + motionZ * motionZ;
			if (magnitude - d9 > 100.0D && !isServerOwner(player))
			{
				VariousOddities.log.warn("{} (lowestRiding of {}) moved too quickly! {},{},{}", lowestRiding.getName().getString(), player.getName().getString(), motionX, motionY, motionZ);
				player.connection.getNetworkManager().sendPacket(new SMoveVehiclePacket(lowestRiding));
				return;
			}
			
			boolean moveClear = world.hasNoCollisions(lowestRiding, lowestRiding.getBoundingBox().shrink(0.0625D));
			motionX = moveX - lowestRiddenX1;
			motionY = moveY - lowestRiddenY1 - 1.0E-6D;
			motionZ = moveZ - lowestRiddenZ1;
			lowestRiding.move(MoverType.PLAYER, new Vector3d(motionX, motionY, motionZ));
			motionX = moveX - lowestRiding.getPosX();
			motionY = moveY - lowestRiding.getPosY();
			if(motionY > -0.5D || motionY < 0.5D)
				motionY = 0.0D;
			
			motionZ = moveZ - lowestRiding.getPosZ();
			magnitude = motionX * motionX + motionY * motionY + motionZ * motionZ;
			boolean badMove = false;
			if(magnitude > 0.0625D)
			{
				badMove = true;
				VariousOddities.log.warn("{} (vehicle of {}) moved wrongly! {}", lowestRiding.getName().getString(), player.getName().getString(), Math.sqrt(magnitude));
			}
			
			lowestRiding.setPositionAndRotation(moveX, moveY, moveZ, yaw, pitch);
			player.setPositionAndRotation(moveX, moveY, moveZ, player.rotationYaw, player.rotationPitch);
			boolean postMoveClear = world.hasNoCollisions(lowestRiding, lowestRiding.getBoundingBox().shrink(0.0625D));
			if(moveClear && (badMove || !postMoveClear))
			{
				lowestRiding.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
				player.setPositionAndRotation(moveX, moveY, moveZ, player.rotationYaw, player.rotationPitch);
				player.connection.getNetworkManager().sendPacket(new SMoveVehiclePacket(lowestRiding));
				return;
			}
			
			player.getServerWorld().getChunkProvider().updatePlayerPosition(player);
			player.addMovementStat(player.getPosX() - posX, player.getPosY() - posY, player.getPosZ() - posZ);
			vehicleFloating = motionY >= -0.03125D && !server.isFlightAllowed() && isSpaceEmpty(lowestRiding);
			lowestRiddenX1 = lowestRiding.getPosX();
			lowestRiddenY1 = lowestRiding.getPosY();
			lowestRiddenZ1 = lowestRiding.getPosZ();
		}
	}
	
	private boolean isServerOwner(ServerPlayerEntity player)
	{
		return player.getServer().isServerOwner(player.getGameProfile());
	}
	
	private boolean isSpaceEmpty(Entity entity)
	{
		return BlockPos.getAllInBox(entity.getBoundingBox().grow(0.0625D).expand(0.0D, -0.55D, 0.0D)).allMatch(b -> entity.world.getBlockState(b).getBlock() == Blocks.AIR);
	}
}
