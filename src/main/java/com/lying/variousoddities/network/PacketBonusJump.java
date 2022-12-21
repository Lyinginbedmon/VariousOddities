package com.lying.variousoddities.network;

import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.proxy.CommonProxy;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketBonusJump
{
	boolean isAir = true;
	
	public PacketBonusJump(){ }
	public PacketBonusJump(boolean isAirJump)
	{
		this.isAir = isAirJump;
	}
	
	public static PacketBonusJump decode(FriendlyByteBuf par1Buffer)
	{
		return new PacketBonusJump(par1Buffer.readBoolean());
	}
	
	public static void encode(PacketBonusJump msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeBoolean(msg.isAir);
	}
	
	public static void handle(PacketBonusJump msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			Player sender = ((CommonProxy)VariousOddities.proxy).getPlayerEntity(context);
			if(sender != null)
			{
				AbilityData abilities = AbilityData.forEntity(sender);
				if(abilities != null)
				{
					if(abilities.canBonusJump)
						if(msg.isAir)
							abilities.doAirJump();
						else
							abilities.doWaterJump();
				}
			}
		}
		
		context.setPacketHandled(true);
	}
}
