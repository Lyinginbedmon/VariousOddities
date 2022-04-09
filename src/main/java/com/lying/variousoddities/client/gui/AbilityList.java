package com.lying.variousoddities.client.gui;

import java.util.Collection;

import org.lwjgl.opengl.GL11;

import com.lying.variousoddities.client.gui.AbilityList.AbilityListEntry;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.LanguageMap;

public class AbilityList extends AbstractList<AbilityListEntry>
{
	private final Minecraft mc;
	private boolean empty = true;
	
	public AbilityList(Minecraft mcIn, int xPos, int widthIn, int heightIn, int topIn)
	{
		super(mcIn, widthIn, heightIn, topIn, heightIn - 20, 60);
		this.mc = mcIn;
		this.x0 = xPos;
		this.x1 += xPos;
	}
	
	public int getRowWidth(){ return this.getWidth(); }
	
	protected int getScrollbarPosition(){ return this.x1 - 6; }
	
	public void setTop(int par1Int){ this.y0 = par1Int; }
	
	public void clear()
	{
		this.clearEntries();
		this.setScrollAmount(0D);
		this.empty = true;
	}
	
	public boolean isEmpty(){  return this.empty; }
	
	public void addAbility(Ability abilityIn)
	{
		this.addEntry(new AbilityListEntry(this.mc, abilityIn));
		this.empty = false;
	}
	
	public void addAbilities(Collection<Ability> abilitiesIn)
	{
		abilitiesIn.forEach((ability) -> { addAbility(ability); });
	}
	
	public class AbilityListEntry extends ExtendedList.AbstractListEntry<AbilityListEntry>
	{
		public final ResourceLocation WIDGET_TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/species_select.png");
		public final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
		
		private final Minecraft mc;
		private final IReorderingProcessor field_243407_e;
		private final Ability ability;
		
		public AbilityListEntry(Minecraft mcIn, Ability abilityIn)
		{
			this.ability = abilityIn;
			this.mc = mcIn;
			this.field_243407_e = func_244424_a(mcIn, abilityIn.getDisplayName());
		}
		
		private IReorderingProcessor func_244424_a(Minecraft p_244424_0_, ITextComponent p_244424_1_)
		{
			int i = p_244424_0_.fontRenderer.getStringPropertyWidth(p_244424_1_);
			if (i > 157)
			{
				ITextProperties itextproperties = ITextProperties.func_240655_a_(p_244424_0_.fontRenderer.func_238417_a_(p_244424_1_, 157 - p_244424_0_.fontRenderer.getStringWidth("...")), ITextProperties.func_240652_a_("..."));
				return LanguageMap.getInstance().func_241870_a(itextproperties);
			}
			else
				return p_244424_1_.func_241878_f();
		}
		
		public void render(MatrixStack matrixStack, int slotIndex, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean mouseOver, float partialTicks)
		{
			rowTop -= 1;
			rowTop -= slotIndex * 3;
			
			int drawXMin = rowLeft - 2;
			int drawXMax = drawXMin + rowWidth;
			drawSquare(matrixStack, drawXMin, drawXMax, rowTop, rowTop + rowHeight, 55);
			drawLine(matrixStack, drawXMin, drawXMax, rowTop, 41);
			drawLine(matrixStack, drawXMin, drawXMax, rowTop + 11, 97);
			drawLine(matrixStack, drawXMin, drawXMax, rowTop + rowHeight, 97);
			
			this.mc.fontRenderer.func_238407_a_(matrixStack, this.field_243407_e, (float)(rowLeft + 10), (float)(rowTop + 2), 16777215);
			
			float yPos = (float)(rowTop + 14);
			int lineCount = 0;
			for(ITextProperties line : VOHelper.getWrappedText(this.ability.getDescription(), this.mc.fontRenderer, rowWidth - 10))
			{
				this.mc.fontRenderer.func_238418_a_(line, rowLeft, (int)yPos, rowWidth, 13487565);
				yPos += this.mc.fontRenderer.FONT_HEIGHT;
				if(lineCount++ > 4)
					break;
			}
			
			GuiHandler.drawIconAt(matrixStack, rowLeft, rowTop + 1, this.ability.getType().texIndex, 0, 9D, 9D);
			if(!this.ability.passive())
				GuiHandler.drawAbilitySlot(matrixStack, rowLeft, rowTop + 1);
		}
		
		private void drawLine(MatrixStack matrixStack, int minX, int maxX, int yPos, int col)
		{
			drawSquare(matrixStack, minX, maxX, yPos, yPos + 1, col);
		}
		
		@SuppressWarnings("deprecation")
		private void drawSquare(MatrixStack matrixStack, int minX, int maxX, int minY, int maxY, int col)
		{
			RenderSystem.disableCull();
			RenderSystem.disableTexture();
			RenderSystem.shadeModel(GL11.GL_FLAT);
			matrixStack.push();
				Matrix4f matrix4f = matrixStack.getLast().getMatrix();
				BufferBuilder buffer = Tessellator.getInstance().getBuffer();
				buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
					buffer.pos(matrix4f, minX, minY, 0F).color(col, col, col, 255).endVertex();
					buffer.pos(matrix4f, maxX, minY, 0F).color(col, col, col, 255).endVertex();
					buffer.pos(matrix4f, maxX, maxY, 0F).color(col, col, col, 255).endVertex();
					buffer.pos(matrix4f, minX, maxY, 0F).color(col, col, col, 255).endVertex();
				buffer.finishDrawing();
				WorldVertexBufferUploader.draw(buffer);
			matrixStack.pop();
		}
		
		private static final float TEX_SIZE = 128F;
		private static final float ICON_TEX = 16F / TEX_SIZE;
	    
	    public void drawAbilityIcon(MatrixStack matrix, int xPos, int yPos, Ability.Type nature)
	    {
	    	yPos -= 1;
	    	
			float texXMin = ICON_TEX * (float)nature.texIndex;
			float texXMax = ICON_TEX + texXMin;
			
			float texYMin = ICON_TEX * 0F;
			float texYMax = ICON_TEX + texYMin;
			
			// Screen co-ordinates
			double endX = xPos + 9;
			double endY = yPos + 9;
			
			matrix.push();
				Minecraft.getInstance().getTextureManager().bindTexture(ABILITY_ICONS);
				iconBlit(matrix.getLast().getMatrix(), xPos, (int)endX, yPos, (int)endY, 0, texXMin, texXMax, texYMin, texYMax, 1F, 1F, 1F, 1F);
				iconBlit(matrix.getLast().getMatrix(), xPos, (int)endX, yPos, (int)endY, 0, ICON_TEX * 2, ICON_TEX * 3, ICON_TEX * 1, ICON_TEX * 2, 1F, 1F, 1F, 1F);
			matrix.pop();
	    }
		
		@SuppressWarnings("deprecation")
		private void iconBlit(Matrix4f matrix, int startX, int endX, int startY, int endY, int blitOffset, float texXMin, float texXMax, float texYMin, float texYMax, float red, float green, float blue, float alpha)
		{
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR.param, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
			GlStateManager.color4f(1F, 1F, 1F, 1F);
			BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
			bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
				bufferbuilder.pos(matrix, (float)startX, (float)endY, (float)blitOffset).color(red, green, blue, alpha).tex(texXMin, texYMax).endVertex();
				bufferbuilder.pos(matrix, (float)endX, (float)endY, (float)blitOffset).color(red, green, blue, alpha).tex(texXMax, texYMax).endVertex();
				bufferbuilder.pos(matrix, (float)endX, (float)startY, (float)blitOffset).color(red, green, blue, alpha).tex(texXMax, texYMin).endVertex();
				bufferbuilder.pos(matrix, (float)startX, (float)startY, (float)blitOffset).color(red, green, blue, alpha).tex(texXMin, texYMin).endVertex();
			bufferbuilder.finishDrawing();
			RenderSystem.enableAlphaTest();
			WorldVertexBufferUploader.draw(bufferbuilder);
		}
	}
}
