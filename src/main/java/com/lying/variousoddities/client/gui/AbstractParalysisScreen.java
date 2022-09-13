package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketParalysisResignation;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
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
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		
		this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".paralysed.resignation"), (button2) ->
			{
				PacketHandler.sendToServer(new PacketParalysisResignation());
			}));
		
		this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, Component.translatable("gui.advancements"), (button2) ->
			{
			this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()));
			}));
		
		this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, Component.translatable("gui.stats"), (button2) ->
			{
			this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()));
			}));
		
		String s = SharedConstants.getCurrentVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
		this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, Component.translatable("menu.sendFeedback"), (button2) ->
			{
			this.minecraft.setScreen(new ConfirmLinkScreen((open) ->
				{
					if(open)
						Util.getPlatform().openUri(s);
					
					this.minecraft.setScreen(this);
				}, s, true));
			}));
		
		this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, Component.translatable("menu.reportBugs"), (button2) ->
			{
			this.minecraft.setScreen(new ConfirmLinkScreen((open) ->
				{
					if(open)
					Util.getPlatform().openUri("https://aka.ms/snapshotbugs?ref=game");
					
					this.minecraft.setScreen(this);
				}, "https://aka.ms/snapshotbugs?ref=game", true));
			}));
		
		this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, Component.translatable("menu.options"), (button2) ->
			{
				this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
			}));
		
		Button button = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, Component.translatable("menu.shareToLan"), (button2) ->
			{
				this.minecraft.setScreen(new ShareToLanScreen(this));
			}));
		button.active = this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished();
		
		Button button1 = this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, Component.translatable("menu.returnToMenu"), (button2) ->
			{
				boolean localServer = this.minecraft.isLocalServer();
				boolean onRealms = this.minecraft.isConnectedToRealms();
				button2.active = false;
				this.minecraft.level.disconnect();
				
				if(localServer)
					this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
				else
					this.minecraft.clearLevel();
				
				TitleScreen titleScreen = new TitleScreen();
				if(localServer)
					this.minecraft.setScreen(titleScreen);
				else if(onRealms)
					this.minecraft.setScreen(new RealmsMainScreen(titleScreen));
				else
					this.minecraft.setScreen(new JoinMultiplayerScreen(titleScreen));
			
			}));
		
		if(!this.minecraft.isLocalServer())
			button1.setMessage(Component.translatable("menu.disconnect"));
	}
	
	public void tick()
	{
		if(shouldClose() || !thePlayer.isAlive())
			onClose();
	}
	
	public void onClose()
	{
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
