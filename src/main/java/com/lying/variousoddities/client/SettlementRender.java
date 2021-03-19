package com.lying.variousoddities.client;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.settlement.BoxRoom;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class SettlementRender
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static PlayerEntity clientPlayer = mc.player;
	
	private static int latestSettlement = -1;
	private static BoxRoom latestRoom = null;
	
	public static void getPlayer()
	{
		if(mc.world != null)
			clientPlayer = mc.player;
	}
	
	public static boolean canRender()
	{
		return mc.world != null && clientPlayer != null;
	}
	
	@SubscribeEvent
	public static void onRenderRoomsEvent(RenderWorldLastEvent event)
	{
		getPlayer();
		if(!canRender()) return;
		
		SettlementManager localManager = VariousOddities.proxy.getSettlementManager(mc.world);
		if(!localManager.isEmpty())
		{
			Settlement currentSettlement = localManager.getTitleSettlementAt(clientPlayer.getPosition());
			int settlementHere = localManager.getIndexBySettlement(currentSettlement);
			
			// If there's no settlement here, reset to nulls
			if(settlementHere < 0 && latestSettlement >= 0)
				clearSettlement();
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
				BoxRoom roomHere = currentSettlement.getRoomAt(clientPlayer.getPosition());
				if(roomHere == null)
					latestRoom = null;
				else if(!roomHere.equals(latestRoom))
				{
					if(roomHere != null)
					{
						ITextComponent subtitle = null;
						if(roomHere.hasTitle())
							subtitle = roomHere.getTitle();
						else if(clientPlayer.isCreative())
							subtitle = new TranslationTextComponent(roomHere.getFunction().name());
						
						if(subtitle != null)
							mc.getConnection().handleTitle(new STitlePacket(STitlePacket.Type.ACTIONBAR, subtitle));
					}
					
					latestRoom = roomHere;
				}
			}
			
			// If player is creative or spectator, display room boundaries
			if(clientPlayer.canUseCommandBlock())
				for(Settlement settlement : localManager.getSettlements())
					renderSettlementRooms(settlement, event.getMatrixStack(), event.getPartialTicks());
		}
	}
	
	public static int latestSettlement(){ return latestSettlement; }
	
	public static void clearSettlement()
	{
		latestSettlement = -1;
		latestRoom = null;
	}
	
	private static void renderSettlementRooms(Settlement settlement, MatrixStack matrixIn, float partialTicks)
	{
		for(BoxRoom room : settlement.getRooms())
			renderRoom(room, matrixIn, partialTicks);
	}
	
	// FIXME Correct third-person view and stabilise colour
	private static void renderRoom(BoxRoom room, MatrixStack matrixStackIn, float partialTicks)
	{
        float alpha = 255F / 255F;
        float red = 223F / 255F;
		float blue = 255F / 255F;
        float green = 127F / 255F;
        
        green = 1F;
        
        IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
        IVertexBuilder ivertexbuilder = buffers.getBuffer(RenderType.getLines());
        
        double playerX = clientPlayer.getPosX();
        playerX = (clientPlayer.prevPosX) + (playerX - clientPlayer.prevPosX) * partialTicks;
        
        double playerY = clientPlayer.getPosYEye();
        playerY = (clientPlayer.prevPosY + clientPlayer.getEyeHeight()) + (playerY - (clientPlayer.prevPosY + clientPlayer.getEyeHeight())) * partialTicks;
        
        double playerZ = clientPlayer.getPosZ();
        playerZ = (clientPlayer.prevPosZ) + (playerZ - clientPlayer.prevPosZ) * partialTicks;
        
        matrixStackIn.push();
	        BlockPos min = room.min();
	        
	        double minX = (double)min.getX() - playerX;
	        double minY = (double)min.getY() - playerY;
	        double minZ = (double)min.getZ() - playerZ;
	        
	        int sizeX = room.sizeX();
	        int sizeY = room.sizeY();
	        int sizeZ = room.sizeZ();
	        
	        RenderSystem.lineWidth(2.0F);
	        WorldRenderer.drawBoundingBox(matrixStackIn, ivertexbuilder, 
	        		minX, minY, minZ, 
	        		minX + sizeX, minY + sizeY, minZ + sizeZ, red, green, blue, alpha);
	        RenderSystem.lineWidth(1.0F);
        matrixStackIn.pop();
	}
}
