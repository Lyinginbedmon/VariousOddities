package com.lying.variousoddities.network;

import java.util.Map;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.species.Species;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncSpecies
{
	private CompoundTag speciesData = new CompoundTag();
	
	public PacketSyncSpecies(){ }
	public PacketSyncSpecies(Map<ResourceLocation, Species> registryIn)
	{
		ListTag data = new ListTag();
		registryIn.values().forEach((species) -> 
		{
			data.add(species.storeInNBT(new CompoundTag()));
		});
		speciesData.put("Species", data);
		VariousOddities.log.info("Sending species to client, "+data.size()+" species");
	}
	
	public static PacketSyncSpecies decode(FriendlyByteBuf par1Buffer)
	{
		PacketSyncSpecies packet = new PacketSyncSpecies();
		packet.speciesData = par1Buffer.readNbt();
		return packet;
	}
	
	public static void encode(PacketSyncSpecies msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeNbt(msg.speciesData);
	}
	
	public static void handle(PacketSyncSpecies msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		context.setPacketHandled(true);
		
		if(context.getDirection().getReceptionSide().isServer())
			return;
		
		VORegistries.SPECIES.clear();
		ListTag data = msg.speciesData.getList("Species", 10);
		for(int i=0; i<data.size(); i++)
		{
			Species species = null;
			try
			{
				species = Species.createFromNBT(data.getCompound(i));
			}
			catch(Exception e){ VariousOddities.log.error("Malformed species received from server, data: "+data.getCompound(i)); }
			
			if(species != null && species.getRegistryName() != null)
				VORegistries.SPECIES.put(species.getRegistryName(), species);
		}
	}
}
