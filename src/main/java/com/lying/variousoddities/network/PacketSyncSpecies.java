package com.lying.variousoddities.network;

import java.util.Map;
import java.util.function.Supplier;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.species.Species;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSyncSpecies
{
	private CompoundNBT speciesData = new CompoundNBT();
	
	public PacketSyncSpecies(){ }
	public PacketSyncSpecies(Map<ResourceLocation, Species> registryIn)
	{
		ListNBT data = new ListNBT();
		registryIn.values().forEach((species) -> 
		{
			data.add(species.storeInNBT(new CompoundNBT()));
		});
		speciesData.put("Species", data);
		VariousOddities.log.info("Sending species to client, "+data.size()+" species");
	}
	
	public static PacketSyncSpecies decode(PacketBuffer par1Buffer)
	{
		PacketSyncSpecies packet = new PacketSyncSpecies();
		packet.speciesData = par1Buffer.readCompoundTag();
		return packet;
	}
	
	public static void encode(PacketSyncSpecies msg, PacketBuffer par1Buffer)
	{
		par1Buffer.writeCompoundTag(msg.speciesData);
	}
	
	public static void handle(PacketSyncSpecies msg, Supplier<NetworkEvent.Context> cxt)
	{
		VORegistries.SPECIES.clear();
		ListNBT data = msg.speciesData.getList("Species", 10);
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
		cxt.get().setPacketHandled(true);
	}
}
