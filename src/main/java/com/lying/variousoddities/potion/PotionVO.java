package com.lying.variousoddities.potion;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionVO extends Effect
{
	public static final ResourceLocation ICONS	= new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/container/potions.png");
	
	private int iconIndex = 0;
	
	public PotionVO(String nameIn, EffectType badEffectIn, int colorIn)
	{
		super(badEffectIn, colorIn);
		setRegistryName(new ResourceLocation(Reference.ModInfo.MOD_ID, nameIn));
//		setPotionName("potion."+Reference.ModInfo.MOD_PREFIX+nameIn);
	}
	
	public boolean hasEffect(LivingEntity entity){ return VOPotions.isPotionActive(entity, this); }
	
	public boolean hasStatusIcon(){ return false; }
	
    /**
     * Sets the index for the icon displayed in the player's inventory when the status is active.
     */
    public Effect setIconIndex(int column, int row)
    {
    	this.iconIndex = column + row * 14;
        return this;
    }
    
    public int getStatusIconIndex(){ return this.iconIndex; }
	
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(int x, int y, EffectInstance effect, net.minecraft.client.Minecraft mc)
    {
        renderPotionIcon(x + 6, y + 7, mc);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void renderHUDEffect(int x, int y, EffectInstance effect, net.minecraft.client.Minecraft mc, float alpha)
    {
        renderPotionIcon(x + 3, y + 3, mc);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void renderPotionIcon(int x, int y, net.minecraft.client.Minecraft mc)
    {
        mc.getTextureManager().bindTexture(ICONS);
        int index = getStatusIconIndex();
        
        int column = index % 14;
        int row = Math.floorDiv(index, 14);
        
        int texX = column * 18;
        int texY = (row * 18);
        
        this.drawTexturedModalRect(x, y, texX, texY, 18, 18);
    }
    
    /**
     * Draws a textured rectangle at the current z-value.
     */
    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
//    	float zLevel = 10F;
//        float f = 1F / 256F;
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferbuilder = tessellator.getBuffer();
//        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//	        bufferbuilder.pos((double)(x + 0),		(double)(y + height),	(double)zLevel).tex((double)((float)(textureX + 0) * f),		(double)((float)(textureY + height) * f)).endVertex();
//	        bufferbuilder.pos((double)(x + width),	(double)(y + height),	(double)zLevel).tex((double)((float)(textureX + width) * f),	(double)((float)(textureY + height) * f)).endVertex();
//	        bufferbuilder.pos((double)(x + width),	(double)(y + 0),		(double)zLevel).tex((double)((float)(textureX + width) * f),	(double)((float)(textureY + 0) * f)).endVertex();
//	        bufferbuilder.pos((double)(x + 0),		(double)(y + 0),		(double)zLevel).tex((double)((float)(textureX + 0) * f),		(double)((float)(textureY + 0) * f)).endVertex();
//        tessellator.draw();
    }
}
