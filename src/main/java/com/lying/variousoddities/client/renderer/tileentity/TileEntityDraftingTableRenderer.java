package com.lying.variousoddities.client.renderer.tileentity;

import com.lying.variousoddities.tileentity.TileEntityDraftingTable;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;

public class TileEntityDraftingTableRenderer extends TileEntityRenderer<TileEntityDraftingTable>
{
	public TileEntityDraftingTableRenderer(TileEntityRendererDispatcher p_i226017_1_)
	{
		super(p_i226017_1_);
	}
	
    public void render(TileEntityDraftingTable te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        if(!Minecraft.isGuiEnabled()) return;
        
        BlockPos posMin = te.min().subtract(te.getPos());
        BlockPos size = te.size();
        
        if(size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1)
        {
            double startX = (double)posMin.getX();
            double startY = (double)posMin.getY();
            double startZ = (double)posMin.getZ();
            double endY = startY + (double)size.getY();
            double endX = startX + (double)size.getX();
            double endZ = startZ + (double)size.getZ();
            
//            double d4 = endX < 0.0D ? startX + 1.0D : startX;
//            double d6 = endZ < 0.0D ? startZ + 1.0D : startZ;
            
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getLines());
            WorldRenderer.drawBoundingBox(matrixStackIn, ivertexbuilder, startX, startY, startZ, endX, endY, endZ, 0.9F, 0.9F, 0.9F, 1.0F, 0.5F, 0.5F, 0.5F);
        }
    }
    
    public boolean isGlobalRenderer(TileEntityDraftingTable te)
    {
        return true;
    }
}
