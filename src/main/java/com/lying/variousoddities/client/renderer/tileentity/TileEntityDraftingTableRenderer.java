package com.lying.variousoddities.client.renderer.tileentity;

import com.lying.variousoddities.tileentity.TileEntityDraftingTable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TileEntityDraftingTableRenderer implements BlockEntityRenderer<TileEntityDraftingTable>
{
	public TileEntityDraftingTableRenderer(BlockEntityRendererProvider.Context p_i226017_1_){ }
	
    public void render(TileEntityDraftingTable te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        if(!Minecraft.renderNames() || Minecraft.getInstance().player.isSpectator() || !te.showBoundaries()) return;
        
        BlockPos posMin = te.min().subtract(te.getBlockPos());
        BlockPos size = te.size();
        
        if(size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1)
        {
            double startX = (double)posMin.getX();
            double startY = (double)posMin.getY();
            double startZ = (double)posMin.getZ();
            double endX = startX + (double)size.getX();
            double endY = startY + (double)size.getY();
            double endZ = startZ + (double)size.getZ();
            
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.lines());
            LevelRenderer.renderLineBox(matrixStackIn, ivertexbuilder, startX, startY, startZ, endX, endY, endZ, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
        }
    }
    
    public boolean isGlobalRenderer(TileEntityDraftingTable te)
    {
        return true;
    }
}
