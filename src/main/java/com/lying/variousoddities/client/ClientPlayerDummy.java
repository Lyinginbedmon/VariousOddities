package com.lying.variousoddities.client;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

public class ClientPlayerDummy extends LocalPlayer
{
	public ClientPlayerDummy(ClientLevel world, GameProfile profile)
	{
		super(Minecraft.getInstance(), world, Minecraft.getInstance().getConnection(), Minecraft.getInstance().player.getStats(), Minecraft.getInstance().player.getRecipeBook(), false, false);
	}
}
