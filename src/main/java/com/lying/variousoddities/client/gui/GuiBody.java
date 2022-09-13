package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.inventory.ContainerBody;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiBody extends AbstractContainerScreen<ContainerBody>
{
	private static final ResourceLocation BODY_GUI_TEXTURES = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/container/body.png");
	
	public GuiBody(ContainerBody bodyContainer, Inventory playerContainer, Component nameIn)
	{
		super(bodyContainer, playerContainer, Component.translatable(bodyContainer.isCorpse ? "gui.varodd.corpse" : "gui.varodd.unconscious", bodyContainer.theBody.getDisplayName()));
		this.passEvents = false;
		this.titleLabelY -= 16;
	}
    
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y)
	{
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, BODY_GUI_TEXTURES);
		int i = (this.width - this.getXSize()) / 2;
		int j = (this.height - this.getYSize()) / 2;
		this.blit(matrixStack, i, j - 16, 0, 0, 176, 182);
	}
	
	protected void renderLabels(PoseStack matrixStack, int x, int y)
	{
		Component title = this.title;
		int titleX = (this.getXSize() - this.font.width(this.title)) / 2;
		this.font.draw(matrixStack, title, (float)titleX, (float)this.titleLabelY, 4210752);
		this.font.draw(matrixStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}
}
