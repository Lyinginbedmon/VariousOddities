package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.inventory.ContainerPlayerBody;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiPlayerBody extends ContainerScreen<ContainerPlayerBody>
{
	private static final ResourceLocation BODY_GUI_TEXTURES = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/container/body_unconscious.png");
	
	public GuiPlayerBody(ContainerPlayerBody bodyContainer, PlayerInventory playerContainer, Component nameIn)
	{
		super(bodyContainer, playerContainer, Component.translatable("gui.varodd.unconscious", bodyContainer.theBody.getDisplayName()));
		this.passEvents = false;
		this.titleY -= 16;
	}
	
	@SuppressWarnings("deprecation")
	protected void drawGuiContainerBackgroundLayer(PoseStack matrixStack, float partialTicks, int x, int y)
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		this.minecraft.getTextureManager().bindTexture(BODY_GUI_TEXTURES);
		int i = (this.width - 248) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(matrixStack, i, j - 16, 0, 0, 248, 182);
	}
	
	protected void drawGuiContainerForegroundLayer(PoseStack matrixStack, int x, int y)
	{
		Component title = this.title;
		int titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;
		this.font.func_243248_b(matrixStack, title, (float)titleX, (float)this.titleY, 4210752);
		this.font.func_243248_b(matrixStack, this.playerInventory.getDisplayName(), (float)this.playerInventoryTitleX, (float)this.playerInventoryTitleY, 4210752);
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}
}
