package com.lying.variousoddities.client.gui;

import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketParalysisResignation;
import com.lying.variousoddities.reference.Reference;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.DirtMessageScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ShareToLanScreen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ScreenParalysed extends Screen
{
	protected final PlayerEntity thePlayer;
	
	public ScreenParalysed(ITextComponent textComponent, PlayerEntity playerIn)
	{
		super(textComponent);
		this.thePlayer = playerIn;
	}
	
	public ScreenParalysed(PlayerEntity playerIn)
	{
		this(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".paralysed"), playerIn);
	}
	
	public boolean shouldCloseOnEsc(){ return false; }
	
	public boolean isPauseScreen(){ return false; }
	
	public int ticksToDisplay()
	{
		EffectInstance paralysis = thePlayer.getActivePotionEffect(VOPotions.PARALYSIS);
		if(paralysis == null)
			return 0;
		else if(paralysis.getIsPotionDurationMax())
			return -1;
		
		return paralysis.getDuration();
	}
	
	public boolean shouldClose()
	{
		return thePlayer.getActivePotionEffect(VOPotions.PARALYSIS) == null || thePlayer.getActivePotionEffect(VOPotions.PARALYSIS).getDuration() == 0;
	}
	
	public void init()
	{
		this.minecraft.keyboardListener.enableRepeatEvents(true);
		
		this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".paralysed.resignation"), (button2) ->
			{
				PacketHandler.sendToServer(new PacketParalysisResignation());
			}));
		
		this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, new TranslationTextComponent("gui.advancements"), (button2) ->
			{
			this.minecraft.displayGuiScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancementManager()));
			}));
		
		this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, new TranslationTextComponent("gui.stats"), (button2) ->
			{
			this.minecraft.displayGuiScreen(new StatsScreen(this, this.minecraft.player.getStats()));
			}));
		
		String s = SharedConstants.getVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
		this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, new TranslationTextComponent("menu.sendFeedback"), (button2) ->
			{
			this.minecraft.displayGuiScreen(new ConfirmOpenLinkScreen((open) ->
				{
					if(open)
						Util.getOSType().openURI(s);
					
					this.minecraft.displayGuiScreen(this);
				}, s, true));
			}));
		
		this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, new TranslationTextComponent("menu.reportBugs"), (button2) ->
			{
			this.minecraft.displayGuiScreen(new ConfirmOpenLinkScreen((open) ->
				{
					if(open)
					Util.getOSType().openURI("https://aka.ms/snapshotbugs?ref=game");
					
					this.minecraft.displayGuiScreen(this);
				}, "https://aka.ms/snapshotbugs?ref=game", true));
			}));
		
		this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, new TranslationTextComponent("menu.options"), (button2) ->
			{
				this.minecraft.displayGuiScreen(new OptionsScreen(this, this.minecraft.gameSettings));
			}));
		
		Button button = this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, new TranslationTextComponent("menu.shareToLan"), (button2) ->
			{
				this.minecraft.displayGuiScreen(new ShareToLanScreen(this));
			}));
		button.active = this.minecraft.isSingleplayer() && !this.minecraft.getIntegratedServer().getPublic();
		
		Button button1 = this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, new TranslationTextComponent("menu.returnToMenu"), (button2) ->
			{
				boolean flag = this.minecraft.isIntegratedServerRunning();
				boolean flag1 = this.minecraft.isConnectedToRealms();
				button2.active = false;
				this.minecraft.world.sendQuittingDisconnectingPacket();
				
				if(flag)
					this.minecraft.unloadWorld(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
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
			button1.setMessage(new TranslationTextComponent("menu.disconnect"));
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
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
		
		ITextComponent duration;
		if(ticksToDisplay() >= 0)
			duration = new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".paralysed.temporary", StringUtils.ticksToElapsedTime(MathHelper.floor((float)ticksToDisplay())));
		else
			duration = new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".paralysed.permanent");
		
		drawCenteredString(matrixStack, this.font, duration, this.width / 2, 55, 16777215);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
