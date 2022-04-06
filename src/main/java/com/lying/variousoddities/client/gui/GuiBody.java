package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.inventory.ContainerBody;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiBody extends ContainerScreen<ContainerBody>
{
	private static final ResourceLocation BODY_GUI_TEXTURES = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/container/body.png");
	
	public GuiBody(ContainerBody bodyContainer, PlayerInventory playerContainer, ITextComponent nameIn)
	{
		super(bodyContainer, playerContainer, new TranslationTextComponent(bodyContainer.isCorpse ? "gui.varodd.corpse" : "gui.varodd.unconscious", bodyContainer.theBody.getDisplayName()));
		this.passEvents = false;
		this.titleY -= 16;
	}
    
	@SuppressWarnings("deprecation")
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y)
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		this.minecraft.getTextureManager().bindTexture(BODY_GUI_TEXTURES);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(matrixStack, i, j - 16, 0, 0, 176, 182);
	}
	
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y)
	{
		ITextComponent title = this.title;
		int titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;
		this.font.func_243248_b(matrixStack, title, (float)titleX, (float)this.titleY, 4210752);
		this.font.func_243248_b(matrixStack, this.playerInventory.getDisplayName(), (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}
}
