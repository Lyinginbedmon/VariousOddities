package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketTileUpdate;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiDraftingTable extends Screen
{
	private static final ResourceLocation SCREEN_TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_ID,"textures/gui/drafting_table.png");
	private final TileEntityDraftingTable theTable;
	
	private Button maxXUp, maxYUp, maxZUp;
	private Button maxXDown, maxYDown, maxZDown;
	
	private Button minXUp, minYUp, minZUp;
	private Button minXDown, minYDown, minZDown;
	
	private Button functionSelect;
	
    private TextFieldWidget nameField;
    
    private Button signButton;
	
	public GuiDraftingTable(TileEntityDraftingTable tableIn)
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table"));
		this.theTable = tableIn;
	}
	
	public static void open(TileEntityDraftingTable tableIn)
	{
		Minecraft.getInstance().displayGuiScreen(new GuiDraftingTable(tableIn));
	}
	
	public boolean isPauseScreen(){ return false; }
    
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void init(Minecraft minecraft, int width, int height)
    {
    	super.init(minecraft, width, height);
        this.buttons.clear();
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        int midX = width / 2;
        int midY = height / 2;
        
    	this.addButton(minXUp = new BoundsButton(midX - 80, midY - 40, Axis.X, AxisDirection.POSITIVE, true, this));
    	this.addButton(minYUp = new BoundsButton(midX - 60, midY - 40, Axis.Y, AxisDirection.POSITIVE, true, this));
    	this.addButton(minZUp = new BoundsButton(midX - 40, midY - 40, Axis.Z, AxisDirection.POSITIVE, true, this));
    	this.addButton(minXDown = new BoundsButton(midX - 80, midY - 0, Axis.X, AxisDirection.NEGATIVE, true, this));
    	this.addButton(minYDown = new BoundsButton(midX - 60, midY - 0, Axis.Y, AxisDirection.NEGATIVE, true, this));
    	this.addButton(minZDown = new BoundsButton(midX - 40, midY - 0, Axis.Z, AxisDirection.NEGATIVE, true, this));
    	
    	this.addButton(maxXUp = new BoundsButton(midX + 20, midY - 40, Axis.X, AxisDirection.POSITIVE, false, this));
    	this.addButton(maxYUp = new BoundsButton(midX + 40, midY - 40, Axis.Y, AxisDirection.POSITIVE, false, this));
    	this.addButton(maxZUp = new BoundsButton(midX + 60, midY - 40, Axis.Z, AxisDirection.POSITIVE, false, this));
    	this.addButton(maxXDown = new BoundsButton(midX + 20, midY - 0, Axis.X, AxisDirection.NEGATIVE, false, this));
    	this.addButton(maxYDown = new BoundsButton(midX + 40, midY - 0, Axis.Y, AxisDirection.NEGATIVE, false, this));
    	this.addButton(maxZDown = new BoundsButton(midX + 60, midY - 0, Axis.Z, AxisDirection.NEGATIVE, false, this));
    	
        this.addButton(new Button(midX - 17, midY - 40, 34, 20, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table.button.showbb"), (button) -> 
    	{
    		theTable.toggleBoundaries();
    	}));
    	
    	this.addButton(functionSelect = new Button(midX - 50, midY - 65, 100, 20, theTable.getFunction().getName(), (button) -> 
    		{
	    		EnumRoomFunction currentFunction = theTable.getFunction();
	    		theTable.setFunction(EnumRoomFunction.values()[(currentFunction.ordinal() + 1) % EnumRoomFunction.values().length]);
	    		functionSelect.setMessage(theTable.getFunction().getName());
	        	updateButtons();
	        	sendToServer();
    		}));
    	
        this.nameField = new TextFieldWidget(this.font, midX - 50, midY - 80, 100, 12, new StringTextComponent(""));
        this.nameField.setTextColor(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setMaxStringLength(16);
        this.nameField.setText(theTable.getTitle());
    	this.children.add(this.nameField);
    	if(theTable.canAlter(8))
    		this.setFocusedDefault(this.nameField);
        
        this.addButton(signButton = new Button(midX - 35, midY + 30, 70, 20, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".drafting_table.button.save"), (button) -> 
        	{
        		theTable.setMask(15);
        		sendToServer();
                this.minecraft.player.closeScreen();
        	}));
        
        updateButtons();
    }
    
    private void updateButtons()
    {
    	minXUp.active =	theTable.canApplyToMin(1, 0, 0) && theTable.canAlter(1);
    	minYUp.active =	theTable.canApplyToMin(0, 1, 0) && theTable.canAlter(1);
    	minZUp.active =	theTable.canApplyToMin(0, 0, 1) && theTable.canAlter(1);
    	
    	minXDown.active =	theTable.canApplyToMin(-1, 0, 0) && theTable.canAlter(1);
    	minYDown.active =	theTable.canApplyToMin(0, -1, 0) && theTable.canAlter(1);
    	minZDown.active =	theTable.canApplyToMin(0, 0, -1) && theTable.canAlter(1);
    	
    	maxXUp.active =	theTable.canApplyToMax(1, 0, 0) && theTable.canAlter(2);
		maxYUp.active =	theTable.canApplyToMax(0, 1, 0) && theTable.canAlter(2);
		maxZUp.active =	theTable.canApplyToMax(0, 0, 1) && theTable.canAlter(2);
		
    	maxXDown.active =	theTable.canApplyToMax(-1, 0, 0) && theTable.canAlter(2);
    	maxYDown.active =	theTable.canApplyToMax(0, -1, 0) && theTable.canAlter(2);
    	maxZDown.active =	theTable.canApplyToMax(0, 0, -1) && theTable.canAlter(2);
    	
    	functionSelect.active = theTable.canAlter(4);
    	
    	nameField.setEnabled(theTable.canAlter(8));
    	
    	signButton.active = theTable.bitMask() != 15;
    }
	
	@SuppressWarnings("deprecation")
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		this.minecraft.getTextureManager().bindTexture(SCREEN_TEXTURE);
		int width = 170;
        int i = (this.width - 176) / 2 + 3;
        int j = (this.height - 166) / 2 - 18;
		this.blit(matrixStack, i, j, 0, 0, width, 160);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		RenderSystem.disableBlend();
		this.nameField.render(matrixStack, mouseX, mouseY, partialTicks);
		
		int midX = this.width / 2;
		int midY = this.height / 2;
        drawCentred(matrixStack, getTitle(), midX, midY - 95, 4210752);
        
        ITextComponent start = new StringTextComponent(theTable.min().getX() + ", " + theTable.min().getY() + ", " + theTable.min().getZ());
        drawCentred(matrixStack, start, midX - 50, midY - 14, 4210752);
        ITextComponent end = new StringTextComponent(theTable.max().getX() + ", " + theTable.max().getY() + ", " + theTable.max().getZ());
        drawCentred(matrixStack, end, midX + 50, midY - 14, 4210752);
	}
	
	private void drawCentred(MatrixStack matrix, ITextComponent text, int x, int y, int colour)
	{
		IReorderingProcessor ireorderingprocessor = text.func_241878_f();
		this.font.func_238422_b_(matrix, ireorderingprocessor, (float)(x - this.font.func_243245_a(ireorderingprocessor) / 2), (float)y, colour);
	}
    
    private void updateTitle()
    {
    	String s = this.nameField.getText();
    	
    	if(s == null || s.length() == 0)
    		s = "";
    	
    	theTable.setTitle(s);
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
    	if(keyCode == 256)
    	{
    		sendToServer();
    		this.minecraft.player.closeScreen();
    	}
    	return !this.nameField.keyPressed(keyCode, scanCode, modifiers) && !this.nameField.canWrite() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
    }
    
    private static class BoundsButton extends Button
    {
		public BoundsButton(int x, int y, Axis axis, AxisDirection direction, boolean minPos, GuiDraftingTable theScreen)
		{
			super(x, y, 20, 20, new StringTextComponent(direction == AxisDirection.POSITIVE ? "+" : "-"), (button) -> 
				{
					if(!button.active) return;
					TileEntityDraftingTable theTable = theScreen.theTable;
		        	for(int i = (Screen.hasShiftDown() ? 5 : 1); i>0; i--)
		        	{
			        	int moveX = 0;
			        	int moveY = 0;
			        	int moveZ = 0;
		        		switch(axis)
		        		{
							case X:	moveX = direction.getOffset(); break;
							case Y:	moveY = direction.getOffset(); break;
							case Z:	moveZ = direction.getOffset(); break;
		        		}
		        		
		        		if(minPos)
		        			theTable.moveMin(moveX, moveY, moveZ);
		        		else
		        			theTable.moveMax(moveX, moveY, moveZ);
		        		
						theScreen.updateButtons();
		        		if(!button.active) break;
		        	}
		        	theScreen.sendToServer();
				});
		}
    }
    
    private boolean sendToServer()
    {
        try
        {
        	updateTitle();
            PacketHandler.sendToServer(new PacketTileUpdate(theTable));
            return true;
        }
        catch (Exception exception)
        {
        	VariousOddities.log.warn("Could not send drafting table info", (Throwable)exception);
            return false;
        }
    }
}
