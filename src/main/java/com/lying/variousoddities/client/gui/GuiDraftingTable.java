package com.lying.variousoddities.client.gui;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketTileUpdate;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiDraftingTable extends Screen
{
	private static final ResourceLocation SCREEN_TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_ID,"textures/gui/drafting_table.png");
    private static final Logger LOGGER = LogManager.getLogger();
    
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
		super(new TranslationTextComponent(""));
		theTable = tableIn;
	}
	
    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return false;
    }
    
    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
//    	super.initGui();
//        this.buttonList.clear();
//        Keyboard.enableRepeatEvents(true);
//        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
//        int midX = res.getScaledWidth() / 2;
//        int midY = res.getScaledHeight() / 2;
//        
//    	this.addButton(minXUp = new Button(0, midX - 80, midY - 40, 20, 20, "+"));
//    	this.addButton(minYUp = new Button(1, midX - 60, midY - 40, 20, 20, "+"));
//    	this.addButton(minZUp = new Button(2, midX - 40, midY - 40, 20, 20, "+"));
//    	this.addButton(minXDown = new Button(3, midX - 80, midY - 0, 20, 20, "-"));
//    	this.addButton(minYDown = new Button(4, midX - 60, midY - 0, 20, 20, "-"));
//    	this.addButton(minZDown = new Button(5, midX - 40, midY - 0, 20, 20, "-"));
//    	
//    	this.addButton(maxXUp = new Button(6, midX + 20, midY - 40, 20, 20, "+"));
//    	this.addButton(maxYUp = new Button(7, midX + 40, midY - 40, 20, 20, "+"));
//    	this.addButton(maxZUp = new Button(8, midX + 60, midY - 40, 20, 20, "+"));
//    	this.addButton(maxXDown = new Button(9, midX + 20, midY - 0, 20, 20, "-"));
//    	this.addButton(maxYDown = new Button(10, midX + 40, midY - 0, 20, 20, "-"));
//    	this.addButton(maxZDown = new Button(11, midX + 60, midY - 0, 20, 20, "-"));
//    	
//    	this.addButton(functionSelect = new Button(12, midX - 50, midY - 65, 100, 20, theTable.getFunction().name().toLowerCase()));
//    	
//        this.nameField = new TextFieldWidget(13, this.fontRenderer, midX - 50, midY - 80, 100, 12);
//        this.nameField.setTextColor(-1);
//        this.nameField.setEnableBackgroundDrawing(false);
//        this.nameField.setDisabledTextColour(-1);
//        this.nameField.setMaxStringLength(25);
//        this.nameField.setText(theTable.getCustomName());
//        
//        this.addButton(signButton = new Button(13, midX - 50, midY + 30, 100, 20, I18n.format("book.signButton")));
//        
//        updateButtons();
    }
    
    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    	theTable.setCustomName(this.nameField.getText());
    }
    
    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
//        ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
//        int midX = res.getScaledWidth() / 2;
//        int midY = res.getScaledHeight() / 2;
//        
//		this.drawDefaultBackground();
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        this.mc.getTextureManager().bindTexture(SCREEN_TEXTURE);
//        int width = 170;
//        drawTexturedModalRect(midX - width/2, midY - 100, 0, 0, width, 160);
//        super.drawScreen(mouseX, mouseY, partialTicks);
//		
//        GlStateManager.disableLighting();
//        GlStateManager.disableBlend();
//        this.nameField.drawTextBox();
//        
//        String title = "Drafting Table";
//        this.fontRenderer.drawString(title, midX - this.fontRenderer.getStringWidth(title) / 2, midY - 94, 4210752);
//        
//        String minX = String.valueOf(theTable.min().getX() + ", " + theTable.min().getY() + ", " + theTable.min().getZ());
//        this.fontRenderer.drawString(minX, midX - 20 - this.fontRenderer.getStringWidth(minX), midY - 14, 4210752);
//        String maxX = String.valueOf(theTable.max().getX() + ", " + theTable.max().getY() + ", " + theTable.max().getZ());
//        this.fontRenderer.drawString(maxX, midX + 80 - this.fontRenderer.getStringWidth(maxX), midY - 14, 4210752);
    }
    
    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
//        Keyboard.enableRepeatEvents(false);
        sendToServer();
    }
    
    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
//        if(this.nameField.textboxKeyTyped(typedChar, keyCode))
//        	updateName();
//        else
//            super.keyTyped(typedChar, keyCode);
    }
    
    private void updateButtons()
    {
    	BlockPos currentMin = theTable.min();
    	BlockPos currentMax = theTable.max();
    	
    	minXUp.active =	theTable.areBoundsValid(currentMax, currentMin.add(1, 0, 0)) && theTable.canAlter(1);
    	minYUp.active =	theTable.areBoundsValid(currentMax, currentMin.add(0, 1, 0)) && theTable.canAlter(1);
    	minZUp.active =	theTable.areBoundsValid(currentMax, currentMin.add(0, 0, 1)) && theTable.canAlter(1);
    	
    	minXDown.active =	theTable.areBoundsValid(currentMax, currentMin.add(-1, 0, 0)) && theTable.canAlter(1);
    	minYDown.active =	theTable.areBoundsValid(currentMax, currentMin.add(0, -1, 0)) && theTable.canAlter(1);
    	minZDown.active =	theTable.areBoundsValid(currentMax, currentMin.add(0, 0, -1)) && theTable.canAlter(1);
    	
    	maxXUp.active =	theTable.areBoundsValid(currentMin, currentMax.add(1, 0, 0)) && theTable.canAlter(2);
		maxYUp.active =	theTable.areBoundsValid(currentMin, currentMax.add(0, 1, 0)) && theTable.canAlter(2);
		maxZUp.active =	theTable.areBoundsValid(currentMin, currentMax.add(0, 0, 1)) && theTable.canAlter(2);
		
    	maxXDown.active =	theTable.areBoundsValid(currentMin, currentMax.add(-1, 0, 0)) && theTable.canAlter(2);
    	maxYDown.active =	theTable.areBoundsValid(currentMin, currentMax.add(0, -1, 0)) && theTable.canAlter(2);
    	maxZDown.active =	theTable.areBoundsValid(currentMin, currentMax.add(0, 0, -1)) && theTable.canAlter(2);
    	
    	functionSelect.active = theTable.canAlter(4);
    	
    	nameField.setEnabled(theTable.canAlter(8));
    	
    	signButton.active = theTable.bitMask() < 15;
    }
    
    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(Button button) throws IOException
    {
        if(button.active)
        {
//        	if(button.id == functionSelect.id)
//        	{
//        		EnumRoomFunction currentFunction = theTable.getFunction();
//        		theTable.setFunction(EnumRoomFunction.values()[(currentFunction.ordinal() + 1) % EnumRoomFunction.values().length]);
//        		functionSelect.displayString = theTable.getFunction().name().toLowerCase();
//        	}
//        	else if(button.id == signButton.id)
//        	{
//        		theTable.setMask(15);
//        		sendToServer();
//                this.mc.displayScreen((Screen)null);
//        	}
//        	else
//        	{
//	        	boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
//	        	for(int i = (shift ? 5 : 1); i>0; i--)
//	        	{
//	        		if(!button.active) break;
//		        	BlockPos currentMin = theTable.min();
//		        	BlockPos currentMax = theTable.max();
//		        	
//	            	// Max controls
//	            	if(button.id == maxXUp.id)			theTable.setBounds(currentMin, currentMax.add(1, 0, 0));
//	            	else if(button.id == maxYUp.id)		theTable.setBounds(currentMin, currentMax.add(0, 1, 0));
//	            	else if(button.id == maxZUp.id)		theTable.setBounds(currentMin, currentMax.add(0, 0, 1));
//	            	else if(button.id == maxXDown.id)	theTable.setBounds(currentMin, currentMax.add(-1, 0, 0));
//	            	else if(button.id == maxYDown.id)	theTable.setBounds(currentMin, currentMax.add(0, -1, 0));
//	            	else if(button.id == maxZDown.id)	theTable.setBounds(currentMin, currentMax.add(0, 0, -1));
//	            	
//	            	// Min controls
//	            	else if(button.id == minXUp.id)		theTable.setBounds(currentMin.add(1, 0, 0), currentMax);
//	            	else if(button.id == minYUp.id)		theTable.setBounds(currentMin.add(0, 1, 0), currentMax);
//	            	else if(button.id == minZUp.id)		theTable.setBounds(currentMin.add(0, 0, 1), currentMax);
//	            	else if(button.id == minXDown.id)	theTable.setBounds(currentMin.add(-1, 0, 0), currentMax);
//	            	else if(button.id == minYDown.id)	theTable.setBounds(currentMin.add(0, -1, 0), currentMax);
//	            	else if(button.id == minZDown.id)	theTable.setBounds(currentMin.add(0, 0, -1), currentMax);
//	            	
//	            	updateButtons();
//	        	}
//        	}
//        	
//        	updateButtons();
//        	sendToServer();
        }
    }
    
    private void updateName()
    {
    	String s = this.nameField.getText();
    	
    	if(s == null || s.length() == 0)
    		s = "";
    	
    	theTable.customName = s;
    }
    
    private boolean sendToServer()
    {
        try
        {
        	updateName();
            PacketHandler.sendToServer(new PacketTileUpdate(theTable));
            return true;
        }
        catch (Exception exception)
        {
            LOGGER.warn("Could not send drafting table info", (Throwable)exception);
            return false;
        }
    }
}
