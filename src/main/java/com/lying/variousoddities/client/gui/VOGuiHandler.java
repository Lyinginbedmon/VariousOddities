package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;

@OnlyIn(Dist.CLIENT)
public class VOGuiHandler
{
	public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
	{
		
	}
	
	public Object getServerGuiElement(int ID, PlayerEntity player, IWorld world, int x, int y, int z)
	{
		return getGuiElement(ID, player, world, x, y, z, Dist.DEDICATED_SERVER);
	}
	
	public Object getClientGuiElement(int ID, PlayerEntity player, IWorld world, int x, int y, int z)
	{
		return getGuiElement(ID, player, world, x, y, z, Dist.CLIENT);
	}
	
	private Object getGuiElement(int ID, PlayerEntity player, IWorld world, int x, int y, int z, Dist side)
	{
		switch(ID)
		{
			case Reference.GUI.GUI_DRAFTING_TABLE:
				TileEntityDraftingTable table = (TileEntityDraftingTable)world.getTileEntity(new BlockPos(x,y,z));
				switch(side)
				{
					case CLIENT:
						return new GuiDraftingTable(table);
					case DEDICATED_SERVER:
						return null;
				}
				break;
		}
		return null;
	}
}
