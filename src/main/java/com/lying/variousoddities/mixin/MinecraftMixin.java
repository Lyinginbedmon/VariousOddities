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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LoadingGui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	private static final Component field_244596_I = Component.translatable("multiplayer.socialInteractions.not_available");
	@Shadow
	private Tutorial tutorial;
	@Shadow
	public LocalPlayer player;
	@Shadow
	public Entity renderViewEntity;
	@Shadow
	public Screen currentScreen;
	@Shadow
	public LoadingGui loadingGui;
	@Shadow
	public PlayerController playerController;
	@Shadow
	private TutorialToast field_244598_aV;
	
	@Inject(method = "processKeyBinds()V", at = @At("HEAD"), cancellable = true)
	public void processKeyBinds(final CallbackInfo ci)
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
		
		while(mc.gameSettings.keyBindInventory.isPressed())
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
		
		if(this.player.isHandActive())
			this.playerController.onStoppedUsingItem(this.player);
		
		this.sendClickBlockToController(false);
	}
	
	private void processVitalKeys(Minecraft mc)
	{
		for(; mc.gameSettings.keyBindTogglePerspective.isPressed(); mc.worldRenderer.setDisplayListEntitiesDirty())
		{
			PointOfView pointofview = mc.gameSettings.getPointOfView();
			mc.gameSettings.setPointOfView(mc.gameSettings.getPointOfView().func_243194_c());
			if(pointofview.func_243192_a() != mc.gameSettings.getPointOfView().func_243192_a())
				mc.gameRenderer.loadEntityShader(mc.gameSettings.getPointOfView().func_243192_a() ? mc.getRenderViewEntity() : null);
		}
		
		while(mc.gameSettings.keyBindSmoothCamera.isPressed())
			mc.gameSettings.smoothCamera = !mc.gameSettings.smoothCamera;
		
		while(mc.gameSettings.field_244602_au.isPressed())
		{
			if(!this.func_244600_aM())
			{
				this.player.sendStatusMessage(field_244596_I, true);
				NarratorChatListener.INSTANCE.say(field_244596_I.getSerializedName());
			}
			else
			{
				if(this.field_244598_aV != null)
				{
					this.tutorial.func_244697_a(this.field_244598_aV);
					this.field_244598_aV = null;
				}
				
				this.displayGuiScreen(new SocialInteractionsScreen());
			}
		}
		
		while(mc.gameSettings.keyBindAdvancements.isPressed())
			this.displayGuiScreen(new AdvancementsScreen(this.player.connection.getAdvancementManager()));
		
		if(mc.gameSettings.chatVisibility != ChatVisibility.HIDDEN)
		{
			while(mc.gameSettings.keyBindChat.isPressed())
				this.openChatScreen("");
			
			if(this.currentScreen == null && this.loadingGui == null && mc.gameSettings.keyBindCommand.isPressed())
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
