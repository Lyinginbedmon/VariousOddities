package com.lying.variousoddities.client.special;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.client.renderer.RenderUtils;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.settlement.BoxRoom;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class SettlementRender
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static Player clientPlayer = mc.player;
	
	private static int latestSettlement = -1;
	private static BoxRoom latestRoom = null;
	
	public static Player getPlayer()
	{
		if(clientPlayer == null)
			clientPlayer = mc.player;
		
		return clientPlayer;
	}
	
	public static boolean canRender()
	{
		return mc.level != null && getPlayer() != null;
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event)
	{
		((CommonProxy)VariousOddities.proxy).clearSettlements();
	}
	
	@SubscribeEvent
	public static void onRenderRoomsEvent(RenderWorldLastEvent event)
	{
		getPlayer();
		if(!canRender()) return;
		
		SettlementManager localManager = VariousOddities.proxy.getSettlementManager(mc.level);
		if(!localManager.isEmpty())
		{
			Settlement currentSettlement = localManager.getTitleSettlementAt(getPlayer().getPosition());
			int settlementHere = localManager.getIndexBySettlement(currentSettlement);
			
			// If there's no settlement here, reset to nulls
			if(settlementHere < 0 && latestSettlement >= 0)
			{
				latestSettlement = -1;
				latestRoom = null;
			}
			// If we've entered a different settlement, announce accordingly
			else if(settlementHere != latestSettlement)
			{
				if(currentSettlement != null && currentSettlement.hasTitle())
					mc.getConnection().handleTitle(new STitlePacket(STitlePacket.Type.TITLE, currentSettlement.getTitle()));
				
				latestSettlement = settlementHere;
			}
			
			// If we've entered a different room in a settlement, announce accordingly
			if(currentSettlement != null)
			{
				BoxRoom roomHere = currentSettlement.getRoomAt(getPlayer().blockPosition());
				if(roomHere == null)
					latestRoom = null;
				else if(!roomHere.equals(latestRoom))
				{
					if(roomHere != null)
					{
						Component subtitle = null;
						if(roomHere.hasTitle())
							subtitle = roomHere.getTitle();
						else if(getPlayer().isCreative())
							subtitle = Component.translatable(roomHere.getFunction().name());
						
						if(subtitle != null)
							mc.getConnection().handleTitle(new STitlePacket(STitlePacket.Type.ACTIONBAR, subtitle));
					}
					
					latestRoom = roomHere;
				}
			}
			
			// If player is creative, display room boundaries
			if(getPlayer().canUseCommandBlock())
				for(Settlement settlement : localManager.getSettlements())
					renderSettlementRooms(settlement, mc.getRenderManager().info.getProjectedView(), event.getMatrixStack());
		}
	}
	
	public static int latestSettlement(){ return latestSettlement; }
	
	private static void renderSettlementRooms(Settlement settlement, Vec3 eyePos, PoseStack matrixIn)
	{
        float alpha = 255F / 255F;
        float red = 223F / 255F;
		float blue = 255F / 255F;
        float green = 255F / 255F;
		for(BoxRoom room : settlement.getRooms())
		{
	        Vec3 boxMin = new Vec3(room.min().getX(), room.min().getY(), room.min().getZ());
	        Vec3 boxMax = boxMin.add(room.sizeX(), room.sizeY(), room.sizeZ());
	        RenderUtils.drawBoundingBox(matrixIn, boxMin, boxMax, getPlayer().getLookAngle(), eyePos, red, green, blue, alpha, 0.025F, true);
		}
	}
}
