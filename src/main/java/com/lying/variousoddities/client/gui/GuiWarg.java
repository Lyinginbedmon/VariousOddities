package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.inventory.ContainerWarg;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSit;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiWarg extends AbstractContainerScreen<ContainerWarg>
{
	private static final ResourceLocation HORSE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/horse.png");
	private final EntityWarg wargEntity;
	private float mousePosX, mousePosY;
	
	private Button sitButton;
	
	public GuiWarg(ContainerWarg wargContainer, Inventory playerContainer, Component nameIn)
	{
		super(wargContainer, playerContainer, nameIn);
		this.wargEntity = wargContainer.theWarg;
		this.passEvents = false;
	}
    
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void init()
    {
		int i = (this.width - this.getXSize()) / 2;
		int j = (this.height - this.getYSize()) / 2;
    	addRenderableWidget(sitButton = new Button(i - 32, j, 30, 20, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".warg.sit"), (button) -> 
    	{
    		boolean sit = !wargEntity.isOrderedToSit();
    		PacketHandler.sendToServer(new PacketSit(this.minecraft.player.getUUID(), sit));
    	}));
    }
	
    public void containerTick()
    {
    	super.containerTick();
    	sitButton.active = wargEntity.isTamed();
    }
    
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y)
	{
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, HORSE_GUI_TEXTURES);
		int i = (this.width - this.getXSize()) / 2;
		int j = (this.height - this.getYSize()) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.getXSize(), this.getYSize());
		
		this.blit(matrixStack, i + 7, j + 35 - 18, 18, this.getYSize() + 54, 18, 18); // Saddle
		this.blit(matrixStack, i + 7, j + 35, 36, this.getYSize() + 54, 18, 18); // Carpet
		this.blit(matrixStack, i + 7, j + 35 + 18, 0, this.getYSize() + 54, 18, 18); // Armour
		
		if(wargEntity.hasChest())
			this.blit(matrixStack, i + 79, j + 17, 0, this.getYSize(), 5 * 18, 54);	// Columns of inventory
		
		InventoryScreen.renderEntityInInventory(i + 51, j + 60, 17, (float)(i + 51) - this.mousePosX, (float)(j + 75 - 50) - this.mousePosY, this.wargEntity);
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		this.mousePosX = (float)mouseX;
		this.mousePosY = (float)mouseY;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}
}
