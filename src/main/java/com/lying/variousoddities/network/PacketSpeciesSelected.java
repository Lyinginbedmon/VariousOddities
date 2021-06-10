package com.lying.variousoddities.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Species.SpeciesInstance;
import com.lying.variousoddities.species.SpeciesRegistry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSpeciesSelected
{
	private ResourceLocation selectedSpecies;
	private UUID playerID;
	
	public PacketSpeciesSelected(UUID playerIDIn, ResourceLocation speciesIn){ this.playerID = playerIDIn; this.selectedSpecies = speciesIn; }
	
	public static PacketSpeciesSelected decode(PacketBuffer par1Buffer)
	{
		UUID player = par1Buffer.readUniqueId();
		int len = par1Buffer.readInt();
		if(len >= 0)
		{
			String name = par1Buffer.readString(len);
			return new PacketSpeciesSelected(player, new ResourceLocation(name));
		}
		else
			return new PacketSpeciesSelected(player, null);
	}
	
	public static void encode(PacketSpeciesSelected msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeUniqueId(msg.playerID);
		if(msg.selectedSpecies == null)
			par1Buffer.writeInt(-1);
		else
		{
			String name = msg.selectedSpecies.toString();
			par1Buffer.writeInt(name.length());
			par1Buffer.writeString(name);
		}
	}
	
	public static void handle(PacketSpeciesSelected msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			World world = context.getSender().getEntityWorld();
			PlayerEntity player = world.getPlayerByUuid(msg.playerID);
			if(player != null)
			{
				LivingData data = LivingData.forEntity(player);
				if(data != null)
				{
					if(msg.selectedSpecies == null)
						data.setSpecies((SpeciesInstance)null);
					else
					{
						Species selected = SpeciesRegistry.getSpecies(msg.selectedSpecies);
						if(!data.hasSpecies() || (data.getSpecies().getRegistryName() != msg.selectedSpecies && selected != null))
							data.setSpecies(selected);
					}
					data.setSpeciesSelected();
				}
			}
		}
		context.setPacketHandled(true);
	}
}
