package com.lying.variousoddities.client;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

public class ClientPlayerDummy extends AbstractClientPlayerEntity
{
	public ClientPlayerDummy(ClientWorld world, GameProfile profile)
	{
		super(world, profile);
	}
}
