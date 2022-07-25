package com.lying.variousoddities.client;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

public class ClientPlayerDummy extends LocalPlayer
{
	public ClientPlayerDummy(ClientLevel world, GameProfile profile)
	{
		super(world, profile);
	}
}
