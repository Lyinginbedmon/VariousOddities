package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.inventory.ContainerWarg;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSit;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiWarg extends ContainerScreen<ContainerWarg>
{
	private static final ResourceLocation HORSE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/horse.png");
	private final EntityWarg wargEntity;
	private float mousePosX, mousePosY;
	
	private Button sitButton;
	
	public GuiWarg(ContainerWarg wargContainer, PlayerInventory playerContainer, Component nameIn)
	{
		super(wargContainer, playerContainer, nameIn);
		this.wargEntity = wargContainer.theWarg;
		this.passEvents = false;
	}
    
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void init(Minecraft minecraft, int width, int height)
    {
    	super.init(minecraft, width, height);
    	
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
    	addButton(sitButton = new Button(i - 32, j, 30, 20, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".warg.sit"), (button) -> 
    	{
    		boolean sit = !wargEntity.isSitting();
    		PacketHandler.sendToServer(new PacketSit(this.minecraft.player.getUniqueID(), sit));
    	}));
    }
	
    public void tick()
    {
    	super.tick();
    	sitButton.active = wargEntity.isTamed();
    }
    
	@SuppressWarnings("deprecation")
	protected void drawGuiContainerBackgroundLayer(PoseStack matrixStack, float partialTicks, int x, int y)
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		this.minecraft.getTextureManager().bindTexture(HORSE_GUI_TEXTURES);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
		
		this.blit(matrixStack, i + 7, j + 35 - 18, 18, this.ySize + 54, 18, 18); // Saddle
		this.blit(matrixStack, i + 7, j + 35, 36, this.ySize + 54, 18, 18); // Carpet
		this.blit(matrixStack, i + 7, j + 35 + 18, 0, this.ySize + 54, 18, 18); // Armour
		
		if(wargEntity.hasChest())
			this.blit(matrixStack, i + 79, j + 17, 0, this.ySize, 5 * 18, 54);	// Columns of inventory
		
		InventoryScreen.drawEntityOnScreen(i + 51, j + 60, 17, (float)(i + 51) - this.mousePosX, (float)(j + 75 - 50) - this.mousePosY, this.wargEntity);
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		this.mousePosX = (float)mouseX;
		this.mousePosY = (float)mouseY;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}
}
