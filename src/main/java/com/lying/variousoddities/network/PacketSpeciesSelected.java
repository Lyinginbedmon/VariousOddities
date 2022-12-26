package com.lying.variousoddities.network;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.lying.variousoddities.api.event.SpeciesEvent;
import com.lying.variousoddities.api.event.SpeciesEvent.SpeciesSelected;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Species.SpeciesInstance;
import com.lying.variousoddities.species.SpeciesRegistry;
import com.lying.variousoddities.species.Template;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

public class PacketSpeciesSelected
{
	private ResourceLocation selectedSpecies;
	private ResourceLocation[] selectedTemplates;
	private boolean keepTypes;
	private UUID playerID;
	
	public PacketSpeciesSelected(UUID playerIDIn)
	{
		this(playerIDIn, Species.HUMAN.getRegistryName(), true);
	}
	
	public PacketSpeciesSelected(UUID playerIDIn, ResourceLocation speciesIn, boolean keepTypesIn, ResourceLocation... templatesIn)
	{
		this.playerID = playerIDIn;
		this.keepTypes = keepTypesIn;
		this.selectedSpecies = speciesIn;
		this.selectedTemplates = templatesIn;
	}
	
	public static PacketSpeciesSelected decode(FriendlyByteBuf par1Buffer)
	{
		UUID player = par1Buffer.readUUID();
		
		CompoundTag data = par1Buffer.readNbt();
		boolean types = data.getBoolean("KeepTypes");
		if(data.contains("Species", 8))
		{
			ResourceLocation species = new ResourceLocation(data.getString("Species"));
			
			ListTag templates = data.getList("Templates", 8);

	    	List<ResourceLocation> templateNames = Lists.newArrayList();
	    	for(int i=0; i<templates.size(); i++)
	    		templateNames.add(new ResourceLocation(templates.getString(i)));
			
			return new PacketSpeciesSelected(player, species, types, templateNames.toArray(new ResourceLocation[0]));
		}
		else
			return new PacketSpeciesSelected(player, null, types);
	}
	
	public static void encode(PacketSpeciesSelected msg, FriendlyByteBuf par1Buffer)
	{
		par1Buffer.writeUUID(msg.playerID);
		
		CompoundTag data = new CompoundTag();
		data.putBoolean("KeepTypes", msg.keepTypes);
		if(msg.selectedSpecies != null)
		{
			data.putString("Species", msg.selectedSpecies.toString());
			
			ListTag templates = new ListTag();
			for(ResourceLocation template : msg.selectedTemplates)
				templates.add(StringTag.valueOf(template.toString()));
			data.put("Templates", templates);
		}
		par1Buffer.writeNbt(data);
	}
	
	public static void handle(PacketSpeciesSelected msg, Supplier<NetworkEvent.Context> cxt)
	{
		NetworkEvent.Context context = cxt.get();
		if(context.getDirection().getReceptionSide().isServer())
		{
			Level world = context.getSender().getLevel();
			Player player = world.getPlayerByUUID(msg.playerID);
			if(player != null)
			{
				LivingData data = LivingData.getCapability(player);
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
					
					if(!msg.keepTypes && data.hasCustomTypes())
						data.clearCustomTypes();
					
					data.clearTemplates();
					for(ResourceLocation templateName : msg.selectedTemplates)
					{
						Template template = VORegistries.TEMPLATES.get(templateName);
						if(template != null)
							data.addTemplateInitial(template);
					}
					
					SpeciesEvent.SpeciesSelected event = new SpeciesSelected(player, msg.selectedSpecies, msg.selectedTemplates);
					MinecraftForge.EVENT_BUS.post(event);
				}
			}
		}
		context.setPacketHandled(true);
	}
}
