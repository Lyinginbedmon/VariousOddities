package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketParalysisResignation;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractParalysisScreen extends Screen
{
	protected final Player thePlayer;
	
	public AbstractParalysisScreen(Component textComponent, Player playerIn)
	{
		super(textComponent);
		this.thePlayer = playerIn;
	}
	
	public boolean shouldCloseOnEsc(){ return false; }
	
	public boolean isPauseScreen(){ return false; }
	
	public abstract boolean shouldClose();
	
	public void init()
	{
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		
		this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".paralysed.resignation"), (button2) ->
			{
				PacketHandler.sendToServer(new PacketParalysisResignation());
			}));
		
		this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, Component.translatable("gui.advancements"), (button2) ->
			{
			this.minecraft.displayGuiScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancementManager()));
			}));
		
		this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, Component.translatable("gui.stats"), (button2) ->
			{
			this.minecraft.displayGuiScreen(new StatsScreen(this, this.minecraft.player.getStats()));
			}));
		
		String s = SharedConstants.getVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
		this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, Component.translatable("menu.sendFeedback"), (button2) ->
			{
			this.minecraft.displayGuiScreen(new ConfirmOpenLinkScreen((open) ->
				{
					if(open)
						Util.getOSType().openURI(s);
					
					this.minecraft.displayGuiScreen(this);
				}, s, true));
			}));
		
		this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, Component.translatable("menu.reportBugs"), (button2) ->
			{
			this.minecraft.displayGuiScreen(new ConfirmOpenLinkScreen((open) ->
				{
					if(open)
					Util.getOSType().openURI("https://aka.ms/snapshotbugs?ref=game");
					
					this.minecraft.displayGuiScreen(this);
				}, "https://aka.ms/snapshotbugs?ref=game", true));
			}));
		
		this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, Component.translatable("menu.options"), (button2) ->
			{
				this.minecraft.displayGuiScreen(new OptionsScreen(this, this.minecraft.gameSettings));
			}));
		
		Button button = this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, Component.translatable("menu.shareToLan"), (button2) ->
			{
				this.minecraft.displayGuiScreen(new ShareToLanScreen(this));
			}));
		button.active = this.minecraft.isSingleplayer() && !this.minecraft.getIntegratedServer().getPublic();
		
		Button button1 = this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, Component.translatable("menu.returnToMenu"), (button2) ->
			{
				boolean flag = this.minecraft.isIntegratedServerRunning();
				boolean flag1 = this.minecraft.isConnectedToRealms();
				button2.active = false;
				this.minecraft.level.sendQuittingDisconnectingPacket();
				
				if(flag)
					this.minecraft.unloadWorld(new DirtMessageScreen(Component.translatable("menu.savingLevel")));
				else
					this.minecraft.unloadWorld();
			
				if(flag)
					this.minecraft.displayGuiScreen(new MainMenuScreen());
				else if(flag1)
				{
					RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
					realmsbridgescreen.func_231394_a_(new MainMenuScreen());
				}
				else
					this.minecraft.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
			
			}));
		
		if(!this.minecraft.isIntegratedServerRunning())
			button1.setMessage(Component.translatable("menu.disconnect"));
	}
	
	public void tick()
	{
		if(shouldClose() || !thePlayer.isAlive())
			closeScreen();
	}
	
	public void onClose()
	{
		this.minecraft.keyboardListener.enableRepeatEvents(false);
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
