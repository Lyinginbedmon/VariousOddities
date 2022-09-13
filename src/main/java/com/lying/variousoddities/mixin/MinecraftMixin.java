package com.lying.variousoddities.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.SoulCondition;
import com.lying.variousoddities.network.PacketDeadDeath;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketUnconsciousAwaken;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	private static final Component SOCIAL_NOT_AVAILABLE = Component.translatable("multiplayer.socialInteractions.not_available");
	@Shadow
	private Tutorial tutorial;
	@Shadow
	public LocalPlayer player;
	@Shadow
	public Entity renderViewEntity;
	@Shadow
	public Screen currentScreen;
	@Shadow
	public Overlay overlay;
	@Shadow
	public MultiPlayerGameMode gameMode;
	@Shadow
	private TutorialToast tutorialToast;
	
	@Inject(method = "handleKeybinds()V", at = @At("HEAD"), cancellable = true)
	public void handleKeybinds(final CallbackInfo ci)
	{
		Player player = ((Minecraft)(Object)this).player;
		if(player == null)
			return;
		
		PlayerData data = PlayerData.forPlayer(player);
		if(data == null)
			return;
		
		Minecraft mc = Minecraft.getInstance();
		
		if(PlayerData.isPlayerNormalFunction(player) || VOHelper.isCreativeOrSpectator(player))
			return;
		else
			ci.cancel();
		
		while(mc.options.keyInventory.consumeClick())
		{
			switch(data.getBodyCondition())
			{
				case DEAD:
					// Send respawn packet if delay completed
					if(data.timeToRespawnable() == 0F)
						PacketHandler.sendToServer(new PacketDeadDeath());
					break;
				case UNCONSCIOUS:
					// Send wakeup packet if no longer unconscious
					if(!LivingData.forEntity(player).isUnconscious() && data.getSoulCondition() == SoulCondition.ALIVE)
						PacketHandler.sendToServer(new PacketUnconsciousAwaken());
					break;
				default:
					break;
			}
		}
		
		processVitalKeys(mc);
		
		if(this.player.isUsingItem())
			this.gameMode.releaseUsingItem(this.player);
		
		this.sendClickBlockToController(false);
	}
	
	private void processVitalKeys(Minecraft mc)
	{
		for(; mc.options.keyTogglePerspective.consumeClick(); mc.levelRenderer.needsUpdate())
		{
			CameraType pointofview = mc.options.getCameraType();
			mc.options.setCameraType(mc.options.getCameraType().cycle());
			if(pointofview.isFirstPerson() != mc.options.getCameraType().isFirstPerson())
				mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
		}
		
		while(mc.options.keySmoothCamera.consumeClick())
			mc.options.smoothCamera = !mc.options.smoothCamera;
		
		while(mc.options.keySocialInteractions.consumeClick())
		{
			if(!this.func_244600_aM())
			{
				this.player.displayClientMessage(SOCIAL_NOT_AVAILABLE, true);
				NarratorChatListener.INSTANCE.sayNow(SOCIAL_NOT_AVAILABLE);
			}
			else
			{
				if(this.tutorialToast != null)
				{
					this.tutorial.removeTimedToast(this.tutorialToast);
					this.tutorialToast = null;
				}
				
				this.displayGuiScreen(new SocialInteractionsScreen());
			}
		}
		
		while(mc.options.keyAdvancements.consumeClick())
			this.displayGuiScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
		
		if(mc.options.chatVisibility().get() != ChatVisiblity.HIDDEN)
		{
			while(mc.options.keyChat.consumeClick())
				this.openChatScreen("");
			
			if(this.currentScreen == null && this.overlay == null && mc.options.keyCommand.consumeClick())
				this.openChatScreen("/");
		}
	}
	
	@Shadow
	private boolean func_244600_aM(){ return false; }
	
	@Shadow
	private void openChatScreen(String defaultText){ }
	
	@Shadow
	public void displayGuiScreen(@Nullable Screen guiScreenIn){ }
	
	@Shadow
	private void sendClickBlockToController(boolean leftClick){ }
}
