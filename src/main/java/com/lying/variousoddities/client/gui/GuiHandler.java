package com.lying.variousoddities.client.gui;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.config.ConfigVO.Client.EnumCorner;
import com.lying.variousoddities.config.ConfigVO.Client.EnumCorner.SideX;
import com.lying.variousoddities.config.ConfigVO.Client.EnumNameDisplay;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.ActivatedAbility;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class GuiHandler
{
	public static final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
	public static final ResourceLocation HUD_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/hud.png");
	public static final ResourceLocation TRACKING_EYE = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/tracking.png");
	
	public static Minecraft mc;
	public static ProfilerFiller profiler;
	public static Player player;
	
	private static final double ICON_SIZE = 9D;
	private static final float TEX_SIZE = 128F;
	private static final float ICON_TEX = 16F / TEX_SIZE;
	
	public static int trackingEyeTicks = 0;
	
	public static void renderAbilityOverlay(RenderGuiOverlayEvent.Pre event)
	{
		mc = Minecraft.getInstance();
		profiler = mc.getProfiler();
		
		if(event.getOverlay().id() == VanillaGuiOverlay.CROSSHAIR.id())
		{
			PoseStack matrix = event.getPoseStack();
			float partialTicks = event.getPartialTick();
			
			if(trackingEyeTicks > 0 && --trackingEyeTicks > 0)
			{
				profiler.push("varodd-hud-tracking");
				int right = mc.getWindow().getWidth() / 2;
				int top = (mc.getWindow().getHeight() - 20) / 2;
				
				int index = 3 - (trackingEyeTicks / 5);
				int size = 16;
				
				int texXMin = 0;
				int texXMax = texXMin + size;
				int texYMin = index * size;
				int texYMax = texYMin + size;
				
				int startX = right - (size / 2);
				int endX = startX + size;
				int startY = top - (size / 2);
				int endY = startY + size;
				
				matrix.pushPose();
					RenderSystem.setShaderTexture(0, TRACKING_EYE);
					blit(matrix.last().pose(), (int)startX, (int)endX, (int)startY, (int)endY, 0, texXMin / 16F, texXMax / 16F, texYMin / 64F, texYMax / 64F, 1F, 1F, 1F, 1F);
				matrix.popPose();
				profiler.pop();
			}
			
			if(!ConfigVO.CLIENT.hideAbilities.get())
			{
				profiler.push("varodd-hud-abilities");
					player = Minecraft.getInstance().player;
					if(player != null)
					{
						if(!player.isSpectator() && player.isAlive() && !PlayerData.isPlayerSoulDetached(player))
						{
							EnumCorner corner = ConfigVO.CLIENT.abilityCorner.get();
							if(corner == null)
								corner = EnumCorner.TOP_LEFT;
							
							drawFavouritedAbilities(matrix, event.getWindow(), partialTicks, corner);
						}
					}
				profiler.pop();
			}
		}
	}
	
	private static final EnumSet<VanillaGuiOverlay> CURTAIL_EXCEPTIONS = EnumSet.of
			(
//				VanillaGuiOverlay.ALL, 
				VanillaGuiOverlay.EXPERIENCE_BAR, 
				VanillaGuiOverlay.VIGNETTE,
//				VanillaGuiOverlay.TEXT,
				VanillaGuiOverlay.CHAT_PANEL,
				VanillaGuiOverlay.PLAYER_LIST,
				VanillaGuiOverlay.FPS_GRAPH,
				VanillaGuiOverlay.DEBUG_TEXT,
				VanillaGuiOverlay.SUBTITLES,
				VanillaGuiOverlay.CROSSHAIR);
	
	public static void curtailHUDWhenAbnormal(RenderGuiOverlayEvent.Pre event)
	{
		if(!CURTAIL_EXCEPTIONS.contains(event.getOverlay()))
		{
			Player localPlayer = Minecraft.getInstance().player;
			if(!PlayerData.isPlayerNormalFunction(localPlayer) && !VOHelper.isCreativeOrSpectator(localPlayer))
				event.setCanceled(true);
		}
	}
	
	public static void renderBludgeoning(RenderGuiOverlayEvent.Pre event)
	{
		mc = Minecraft.getInstance();
		if(event.getOverlay().id() != VanillaGuiOverlay.PLAYER_HEALTH.id() || event.isCanceled() || !PlayerData.isPlayerNormalFunction(mc.player) || VOHelper.isCreativeOrSpectator(mc.player))
			return;
		
		profiler = mc.getProfiler();
		profiler.push("varodd-hud-bludgeoning");
			player = Minecraft.getInstance().player;
			if(player != null)
			{
				LivingData data = LivingData.getCapability(player);
				if(data != null && !PlayerData.isPlayerBodyAsleep(player))
				{
					RenderSystem.setShaderTexture(0, HUD_ICONS);
					
					float bludgeoning = data.getBludgeoning();
					float val = Mth.clamp(bludgeoning / player.getHealth(), 0F, 1F);
					
					int width = (int)(val * 81);
					int height = 9;
					
					int right = mc.getWindow().getWidth() / 2 - 91;
					int top = mc.getWindow().getHeight() - ((ForgeGui)mc.gui).rightHeight;
					
					RenderSystem.enableBlend();
					RenderSystem.setShaderColor(1F, 1F, 1F, 0.75F);
					RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					mc.gui.blit(event.getPoseStack(), right, top, 0, 0, width, height);
					RenderSystem.disableBlend();
					RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
					
					RenderSystem.setShaderTexture(0, Screen.GUI_ICONS_LOCATION);
				}
			}
		profiler.pop();
	}
	
	private static void drawFavouritedAbilities(PoseStack matrix, Window window, float partialTicks, EnumCorner corner)
	{
		profiler.push("abilities");
		
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(player);
		AbilityData abilities = AbilityData.getCapability(player);
		
		float posXStart = 5F;
		float posYStart = 5F;
		float posXInc = 3F;
		float posYInc = 11F;
		
		switch(corner)
		{
			case TOP_LEFT:
				posXStart = (AbilityData.FAVOURITE_SLOTS * 3F);
				break;
			case BOTTOM_LEFT:
				posXInc = -3F;
				posYStart = (float)(window.getGuiScaledHeight() - (AbilityData.FAVOURITE_SLOTS * posYInc) - 5F);
				break;
			case BOTTOM_RIGHT:
				posXStart = (float)(window.getGuiScaledWidth() - (AbilityData.FAVOURITE_SLOTS * 3F));
				posYStart = (float)(window.getGuiScaledHeight() - (AbilityData.FAVOURITE_SLOTS * posYInc) - 5F);
				break;
			case TOP_RIGHT:
				posXStart = (float)(window.getGuiScaledWidth() - (AbilityData.FAVOURITE_SLOTS * 3F) - ICON_SIZE);
				posXInc = -3F;
				break;
		}
		
		matrix.pushPose();
			float posX = posXStart;
			float posY = posYStart;
			int maxFav = 0;
			for(int i=0; i<AbilityData.FAVOURITE_SLOTS; i++)
			{
				ResourceLocation mapName = abilities.getFavourite(i);
				if(mapName != null)
				{
					maxFav = Math.max(maxFav, i);
					ActivatedAbility ability = (ActivatedAbility)abilityMap.get(mapName);
					if(ability != null)
						drawAbility(ability, abilities.getCooldown(mapName), posX, posY, matrix, corner.textSide);
				}
				posX -= posXInc;
				posY += posYInc;
			}
			
			List<Ability> activatedAbilities = Lists.newArrayList();
			activatedAbilities.addAll(abilityMap.values());
			activatedAbilities.removeIf(new Predicate<Ability>(){
				public boolean apply(Ability input){ return input.passive(); }
			});
			if(!activatedAbilities.isEmpty())
			{
				// Draw either as many slots as you can fill OR up to the highest index of slots in use
				int slots = Math.max(maxFav, activatedAbilities.size());
				
				posX = posXStart;
				posY = posYStart;
				for(int i=0; i<Math.min(AbilityData.FAVOURITE_SLOTS, slots); i++)
				{
					drawAbilitySlot(matrix, posX, posY);
					posX -= posXInc;
					posY += posYInc;
				}
			}
		matrix.popPose();
		
		profiler.pop();
	}
	
	public static float getStartX(SideX side, Window window)
	{
		float inc = 3F;
		switch(side)
		{
			case RIGHT:	return (float)(window.getGuiScaledWidth() - 5F - ICON_SIZE - (AbilityData.FAVOURITE_SLOTS * inc));
			default:	return 5F + AbilityData.FAVOURITE_SLOTS * inc;
		}
	}
	
	public static void drawIconAt(PoseStack matrix, double posX, double posY, int indexX, int indexY, double sizeX, double sizeY)
	{
		drawIconAt(matrix, posX, posY, indexX, indexY, sizeX, sizeY, 1F, 1F, 1F, 1F);
	}
	
	public static void drawIconAt(PoseStack matrix, double posX, double posY, int indexX, int indexY, double sizeX, double sizeY, float red, float green, float blue, float alpha)
	{
		// Texture co-ordinates
		float texXMin = ICON_TEX * (float)indexX;
		float texXMax = ICON_TEX + texXMin;
		
		float texYMin = ICON_TEX * (float)indexY;
		float texYMax = ICON_TEX + texYMin;
		
		// Screen co-ordinates
		double endX = posX + sizeX;
		double endY = posY + sizeY;
		
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		matrix.pushPose();
			RenderSystem.setShaderTexture(0, ABILITY_ICONS);
			blit(matrix.last().pose(), (int)posX, (int)endX, (int)posY, (int)endY, 0, texXMin, texXMax, texYMin, texYMax, red, green, blue, alpha);
		matrix.popPose();
	}
	
	public static void drawPartialIcon(PoseStack matrix, double posX, double posY, int indexX, int indexY, double sizeX, double sizeY, float startP, float endP, float red, float green, float blue, float alpha)
	{
		double yMin = Mth.clamp(Math.min(startP, endP), 0F, 1F);
		double yMax = Mth.clamp(Math.max(startP, endP), 0F, 1F);
		
		// Texture co-ordinates
		float texXMin = ICON_TEX * (float)indexX;
		float texXMax = ICON_TEX + texXMin;
		
		float texYMin = ICON_TEX * (float)indexY + (float)(ICON_TEX * yMin);
		float texYMax = ICON_TEX * (float)indexY + (float)(ICON_TEX * yMax);
		
		// Screen co-ordinates
		double endX = posX + sizeX;
		double endY = posY + (sizeY * yMax);
		posY += sizeY * yMin;
		
		matrix.pushPose();
			RenderSystem.setShaderTexture(0, ABILITY_ICONS);
			blit(matrix.last().pose(), (int)posX, (int)endX, (int)posY, (int)endY, 0, texXMin, texXMax, texYMin, texYMax, red, green, blue, alpha);
		matrix.popPose();
	}
	
	public static void drawAbilitySlot(PoseStack matrix, float posX, float posY)
	{
		drawIconAt(matrix, posX - 1, posY - 1, 1, 1, ICON_SIZE + 2, ICON_SIZE + 2);
	}
	
	private static void drawAbility(ActivatedAbility ability, int cooldown, double posX, double posY, PoseStack matrix, SideX side)
	{
		EnumNameDisplay displayStyle = ConfigVO.CLIENT.nameDisplay.get();
		if(displayStyle == null)
			displayStyle = EnumNameDisplay.CROPPED;
		
		// Screen co-ordinates
		double startX = posX;
		double startY = posY;
		double endX = startX + ICON_SIZE;
		
		matrix.pushPose();
			boolean canTrigger = ability.canTrigger(player);
			
			if(cooldown <= 0)
			{
				float col = canTrigger ? 1F : 0.5F;
				drawIconAt(matrix, posX, posY, ability.getType().texIndex, 0, ICON_SIZE, ICON_SIZE, col, col, col, 1F);
			}
			else
			{
				int texIndex = ability.getType().texIndex;
				float coolRemaining = (float)cooldown / (float)ability.getCooldown();
				coolRemaining -= coolRemaining % 0.1F;
				
				drawIconAt(matrix, posX, posY, texIndex, 0, ICON_SIZE, ICON_SIZE, 0.5F, 0.5F, 0.5F, 1F);
				drawPartialIcon(matrix, posX, posY, texIndex, 0, ICON_SIZE, ICON_SIZE, coolRemaining, 1F, 1F, 1F, 1F, 1F);
			}
			
			if(displayStyle != EnumNameDisplay.SNEAKING || player.isCrouching())
			{
				int textColor = ability.isActive() ? ChatFormatting.GOLD.getColor() : canTrigger ? -1 : 10526880;
				String name = ability.getDisplayName().getString();
				String displayName = name;
				if(name.length() > 15 && displayStyle == EnumNameDisplay.CROPPED)
				{
					double interval = 0.5D;
					double speed = (1D / 40D);
					
					int second = (int)(System.currentTimeMillis() / 100);
					double sin = Math.max(0, Math.abs(Math.sin(second * speed)) - interval) / (1D - interval);
					
					int startInd = (int)(sin * name.length());
					int endInd = Math.min(startInd + 14, name.length());
					startInd = Mth.clamp(startInd, 0, endInd - 14);
					displayName = name.substring(startInd, endInd);
					while(displayName.lastIndexOf(" ") == displayName.length() - 1 && displayName.length() > 0)
						displayName = displayName.substring(0, displayName.length() - 1);
					
					if(startInd > 0)
						displayName = "..." + displayName;
					else if(endInd < name.length())
						displayName += "...";
				}
				
				int textPos = side == SideX.RIGHT ? (int)(endX + 5D) : (int)(startX - 5D - mc.font.width(displayName));
				mc.font.draw(matrix, displayName, textPos, (int)startY + 1, textColor);
			}
		matrix.popPose();
	}
	
	private static void blit(Matrix4f matrix, int startX, int endX, int startY, int endY, int blitOffset, float texXMin, float texXMax, float texYMin, float texYMax, float red, float green, float blue, float alpha)
	{
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
			bufferbuilder.vertex(matrix, (float)startX, (float)endY, (float)blitOffset).color(red, green, blue, alpha).uv(texXMin, texYMax).endVertex();
			bufferbuilder.vertex(matrix, (float)endX, (float)endY, (float)blitOffset).color(red, green, blue, alpha).uv(texXMax, texYMax).endVertex();
			bufferbuilder.vertex(matrix, (float)endX, (float)startY, (float)blitOffset).color(red, green, blue, alpha).uv(texXMax, texYMin).endVertex();
			bufferbuilder.vertex(matrix, (float)startX, (float)startY, (float)blitOffset).color(red, green, blue, alpha).uv(texXMin, texYMin).endVertex();
		RenderSystem.enableDepthTest();
		BufferUploader.drawWithShader(bufferbuilder.end());
	}
}
