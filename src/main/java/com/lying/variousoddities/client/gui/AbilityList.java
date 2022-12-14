package com.lying.variousoddities.client.gui;

import java.util.Collection;

import com.lying.variousoddities.client.gui.AbilityList.AbilityListEntry;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class AbilityList extends ObjectSelectionList<AbilityListEntry>
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
	
	public class AbilityListEntry extends ObjectSelectionList.Entry<AbilityListEntry>
	{
		public final ResourceLocation WIDGET_TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/species_select.png");
		public final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
		
		private final Minecraft mc;
		private final FormattedCharSequence field_243407_e;
		private final Ability ability;
		
		public AbilityListEntry(Minecraft mcIn, Ability abilityIn)
		{
			this.ability = abilityIn;
			this.mc = mcIn;
			this.field_243407_e = func_244424_a(mcIn, abilityIn.getDisplayName());
		}
		
		private FormattedCharSequence func_244424_a(Minecraft p_244424_0_, Component p_244424_1_)
		{
			int i = p_244424_0_.font.width(p_244424_1_);
			if (i > 157)
			{
				FormattedText itextproperties = FormattedText.composite(p_244424_0_.font.substrByWidth(p_244424_1_, 157 - p_244424_0_.font.width("...")), FormattedText.of("..."));
				return Language.getInstance().getVisualOrder(itextproperties);
			}
			else
				return p_244424_1_.getVisualOrderText();
		}
		
		public void render(PoseStack matrixStack, int slotIndex, int rowTop, int rowLeft, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean mouseOver, float partialTicks)
		{
			rowTop -= 1;
			rowTop -= slotIndex * 3;
			
			int drawXMin = rowLeft - 2;
			int drawXMax = drawXMin + rowWidth;
			drawSquare(matrixStack, drawXMin, drawXMax, rowTop, rowTop + rowHeight, 55);
			drawLine(matrixStack, drawXMin, drawXMax, rowTop, 41);
			drawLine(matrixStack, drawXMin, drawXMax, rowTop + 11, 97);
			drawLine(matrixStack, drawXMin, drawXMax, rowTop + rowHeight, 97);
			
			this.mc.font.draw(matrixStack, this.field_243407_e, (float)(rowLeft + 10), (float)(rowTop + 2), 16777215);
			
			float yPos = (float)(rowTop + 14);
			int lineCount = 0;
			for(FormattedText line : VOHelper.getWrappedText(this.ability.getDescription(), this.mc.font, rowWidth - 10))
			{
				this.mc.font.drawWordWrap(line, rowLeft, (int)yPos, rowWidth, 13487565);
				yPos += this.mc.font.lineHeight;
				if(lineCount++ > 4)
					break;
			}
			
			GuiHandler.drawIconAt(matrixStack, rowLeft, rowTop + 1, this.ability.getType().texIndex, 0, 9D, 9D);
			if(!this.ability.passive())
				GuiHandler.drawAbilitySlot(matrixStack, rowLeft, rowTop + 1);
		}
		
		private void drawLine(PoseStack matrixStack, int minX, int maxX, int yPos, int col)
		{
			drawSquare(matrixStack, minX, maxX, yPos, yPos + 1, col);
		}
		
		private void drawSquare(PoseStack matrixStack, int minX, int maxX, int minY, int maxY, int col)
		{
			RenderSystem.disableCull();
			RenderSystem.disableTexture();
//			RenderSystem.shadeModel(GL11.GL_FLAT);
			matrixStack.pushPose();
				Matrix4f matrix4f = matrixStack.last().pose();
				BufferBuilder buffer = Tesselator.getInstance().getBuilder();
				buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
					buffer.vertex(matrix4f, minX, minY, 0F).color(col, col, col, 255).endVertex();
					buffer.vertex(matrix4f, maxX, minY, 0F).color(col, col, col, 255).endVertex();
					buffer.vertex(matrix4f, maxX, maxY, 0F).color(col, col, col, 255).endVertex();
					buffer.vertex(matrix4f, minX, maxY, 0F).color(col, col, col, 255).endVertex();
				BufferUploader.drawWithShader(buffer.end());
			matrixStack.popPose();
		}
		
		private static final float TEX_SIZE = 128F;
		private static final float ICON_TEX = 16F / TEX_SIZE;
	    
	    public void drawAbilityIcon(PoseStack matrix, int xPos, int yPos, Ability.Type nature)
	    {
	    	yPos -= 1;
	    	
			float texXMin = ICON_TEX * (float)nature.texIndex;
			float texXMax = ICON_TEX + texXMin;
			
			float texYMin = ICON_TEX * 0F;
			float texYMax = ICON_TEX + texYMin;
			
			// Screen co-ordinates
			double endX = xPos + 9;
			double endY = yPos + 9;
			
			matrix.pushPose();
				RenderSystem.setShaderTexture(0, ABILITY_ICONS);
				iconBlit(matrix.last().pose(), xPos, (int)endX, yPos, (int)endY, 0, texXMin, texXMax, texYMin, texYMax, 1F, 1F, 1F, 1F);
				iconBlit(matrix.last().pose(), xPos, (int)endX, yPos, (int)endY, 0, ICON_TEX * 2, ICON_TEX * 3, ICON_TEX * 1, ICON_TEX * 2, 1F, 1F, 1F, 1F);
			matrix.popPose();
	    }
		
		private void iconBlit(Matrix4f matrix, int startX, int endX, int startY, int endY, int blitOffset, float texXMin, float texXMax, float texYMin, float texYMax, float red, float green, float blue, float alpha)
		{
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
				bufferbuilder.vertex(matrix, (float)startX, (float)endY, (float)blitOffset).color(red, green, blue, alpha).uv(texXMin, texYMax).endVertex();
				bufferbuilder.vertex(matrix, (float)endX, (float)endY, (float)blitOffset).color(red, green, blue, alpha).uv(texXMax, texYMax).endVertex();
				bufferbuilder.vertex(matrix, (float)endX, (float)startY, (float)blitOffset).color(red, green, blue, alpha).uv(texXMax, texYMin).endVertex();
				bufferbuilder.vertex(matrix, (float)startX, (float)startY, (float)blitOffset).color(red, green, blue, alpha).uv(texXMin, texYMin).endVertex();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			BufferUploader.drawWithShader(bufferbuilder.end());
		}
		
		public Component getNarration() { return this.ability.getDisplayName(); }
	}
}
