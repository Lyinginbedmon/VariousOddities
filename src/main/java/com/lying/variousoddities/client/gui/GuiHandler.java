package com.lying.variousoddities.client.gui;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.Abilities;
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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.gui.ForgeIngameGui;

@OnlyIn(Dist.CLIENT)
public class GuiHandler
{
	public static final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
	public static final ResourceLocation HUD_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/hud.png");
	
	public static Minecraft mc;
	public static IProfiler profiler;
	public static PlayerEntity player;
	
	private static final double ICON_SIZE = 9D;
	private static final float TEX_SIZE = 128F;
	private static final float ICON_TEX = 16F / TEX_SIZE;
	
	public static void renderAbilityOverlay(RenderGameOverlayEvent.Pre event)
	{
		mc = Minecraft.getInstance();
		profiler = mc.getProfiler();
		
		if(event.getType() == ElementType.CROSSHAIRS && !ConfigVO.CLIENT.hideAbilities.get())
		{
			MatrixStack matrix = event.getMatrixStack();
			float partialTicks = event.getPartialTicks();
			
			profiler.startSection("varodd-hud-abilities");
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
			profiler.endSection();
		}
	}
	
	private static final EnumSet<ElementType> CURTAIL_EXCEPTIONS = EnumSet.of
			(
				ElementType.ALL, 
				ElementType.EXPERIENCE, 
				ElementType.VIGNETTE,
				ElementType.TEXT,
				ElementType.CHAT,
				ElementType.PLAYER_LIST,
				ElementType.FPS_GRAPH,
				ElementType.DEBUG,
				ElementType.SUBTITLES,
				ElementType.CROSSHAIRS);
	
	public static void curtailHUDWhenAbnormal(RenderGameOverlayEvent.Pre event)
	{
		if(!CURTAIL_EXCEPTIONS.contains(event.getType()))
		{
			PlayerEntity localPlayer = Minecraft.getInstance().player;
			if(!PlayerData.isPlayerNormalFunction(localPlayer) && !VOHelper.isCreativeOrSpectator(localPlayer))
				event.setCanceled(true);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void renderBludgeoning(RenderGameOverlayEvent.Pre event)
	{
		mc = Minecraft.getInstance();
		if(event.getType() != RenderGameOverlayEvent.ElementType.HEALTH || event.isCanceled() || !PlayerData.isPlayerNormalFunction(mc.player) || VOHelper.isCreativeOrSpectator(mc.player))
			return;
		
		profiler = mc.getProfiler();
		profiler.startSection("varodd-hud-bludgeoning");
			player = Minecraft.getInstance().player;
			if(player != null)
			{
				LivingData data = LivingData.forEntity(player);
				if(data != null && !PlayerData.isPlayerBodyAsleep(player))
				{
					mc.getTextureManager().bindTexture(HUD_ICONS);
					
					float bludgeoning = data.getBludgeoning();
					float val = MathHelper.clamp(bludgeoning / player.getHealth(), 0F, 1F);
					
					int width = (int)(val * 81);
					int height = 9;
					
					int right = mc.getMainWindow().getScaledWidth() / 2 - 91;
					int top = mc.getMainWindow().getScaledHeight() - ForgeIngameGui.right_height;
					
					RenderSystem.enableBlend();
					RenderSystem.color4f(1F, 1F, 1F, 0.75F);
					RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					mc.ingameGUI.blit(event.getMatrixStack(), right, top, 0, 0, width, height);
					RenderSystem.disableBlend();
					RenderSystem.color4f(1F, 1F, 1F, 1F);
					
					mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
				}
			}
		profiler.endSection();
	}
	
	private static void drawFavouritedAbilities(MatrixStack matrix, MainWindow window, float partialTicks, EnumCorner corner)
	{
		profiler.startSection("abilities");
		
		Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(player);
		Abilities abilities = LivingData.forEntity(player).getAbilities();
		
		float posXStart = 5F;
		float posYStart = 5F;
		float posXInc = 3F;
		float posYInc = 11F;
		
		switch(corner)
		{
			case TOP_LEFT:
				posXStart = (Abilities.FAVOURITE_SLOTS * 3F);
				break;
			case BOTTOM_LEFT:
				posXInc = -3F;
				posYStart = (float)(window.getScaledHeight() - (Abilities.FAVOURITE_SLOTS * posYInc) - 5F);
				break;
			case BOTTOM_RIGHT:
				posXStart = (float)(window.getScaledWidth() - (Abilities.FAVOURITE_SLOTS * 3F));
				posYStart = (float)(window.getScaledHeight() - (Abilities.FAVOURITE_SLOTS * posYInc) - 5F);
				break;
			case TOP_RIGHT:
				posXStart = (float)(window.getScaledWidth() - (Abilities.FAVOURITE_SLOTS * 3F) - ICON_SIZE);
				posXInc = -3F;
				break;
		}
		
		matrix.push();
			float posX = posXStart;
			float posY = posYStart;
			int maxFav = 0;
			for(int i=0; i<Abilities.FAVOURITE_SLOTS; i++)
			{
				ResourceLocation mapName = abilities.getFavourite(i);
				if(mapName != null)
				{
					maxFav = Math.max(maxFav, i);
					ActivatedAbility ability = (ActivatedAbility)abilityMap.get(mapName);
					if(ability != null)
						drawAbility(ability, posX, posY, matrix, corner.textSide);
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
				for(int i=0; i<Math.min(Abilities.FAVOURITE_SLOTS, slots); i++)
				{
					drawAbilitySlot(matrix, posX, posY);
					posX -= posXInc;
					posY += posYInc;
				}
			}
		matrix.pop();
		
		profiler.endSection();
	}
	
	public static float getStartX(SideX side, MainWindow window)
	{
		float inc = 3F;
		switch(side)
		{
			case RIGHT:	return (float)(window.getScaledWidth() - 5F - ICON_SIZE - (Abilities.FAVOURITE_SLOTS * inc));
			default:	return 5F + Abilities.FAVOURITE_SLOTS * inc;
		}
	}
	
	private static void drawIconAt(MatrixStack matrix, double posX, double posY, int indexX, int indexY, double sizeX, double sizeY)
	{
		drawIconAt(matrix, posX, posY, indexX, indexY, sizeX, sizeY, 1F, 1F, 1F, 1F);
	}
	
	private static void drawIconAt(MatrixStack matrix, double posX, double posY, int indexX, int indexY, double sizeX, double sizeY, float red, float green, float blue, float alpha)
	{
		// Texture co-ordinates
		float texXMin = ICON_TEX * (float)indexX;
		float texXMax = ICON_TEX + texXMin;
		
		float texYMin = ICON_TEX * (float)indexY;
		float texYMax = ICON_TEX + texYMin;
		
		// Screen co-ordinates
		double endX = posX + sizeX;
		double endY = posY + sizeY;
		
		matrix.push();
			mc.getTextureManager().bindTexture(ABILITY_ICONS);
			blit(matrix.getLast().getMatrix(), (int)posX, (int)endX, (int)posY, (int)endY, 0, texXMin, texXMax, texYMin, texYMax, red, green, blue, alpha);
		matrix.pop();
	}
	
	private static void drawAbilitySlot(MatrixStack matrix, float posX, float posY)
	{
		drawIconAt(matrix, posX - 1, posY - 1, 1, 1, ICON_SIZE + 2, ICON_SIZE + 2);
	}
	
	private static void drawAbility(ActivatedAbility ability, double posX, double posY, MatrixStack matrix, SideX side)
	{
		EnumNameDisplay displayStyle = ConfigVO.CLIENT.nameDisplay.get();
		if(displayStyle == null)
			displayStyle = EnumNameDisplay.CROPPED;
		
		// Screen co-ordinates
		double startX = posX;
		double startY = posY;
		double endX = startX + ICON_SIZE;
		
		matrix.push();
			boolean canTrigger = ability.canTrigger(player);
			float col = canTrigger ? 1F : 0.5F;
			drawIconAt(matrix, posX, posY, ability.getType().texIndex, 0, ICON_SIZE, ICON_SIZE, col, col, col, 1F);
			
			if(displayStyle != EnumNameDisplay.SNEAKING || player.isSneaking())
			{
				int textColor = ability.isActive() ? TextFormatting.GOLD.getColor() : canTrigger ? -1 : 10526880;
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
					startInd = MathHelper.clamp(startInd, 0, endInd - 14);
					displayName = name.substring(startInd, endInd);
					while(displayName.lastIndexOf(" ") == displayName.length() - 1 && displayName.length() > 0)
						displayName = displayName.substring(0, displayName.length() - 1);
					
					if(startInd > 0)
						displayName = "..." + displayName;
					else if(endInd < name.length())
						displayName += "...";
				}
				
				int textPos = side == SideX.RIGHT ? (int)(endX + 5D) : (int)(startX - 5D - mc.fontRenderer.getStringWidth(displayName));
				mc.fontRenderer.drawString(matrix, displayName, textPos, (int)startY + 1, textColor);
			}
		matrix.pop();
	}
	
	@SuppressWarnings("deprecation")
	private static void blit(Matrix4f matrix, int startX, int endX, int startY, int endY, int blitOffset, float texXMin, float texXMax, float texYMin, float texYMax, float red, float green, float blue, float alpha)
	{
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR.param, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
			bufferbuilder.pos(matrix, (float)startX, (float)endY, (float)blitOffset).color(red, green, blue, alpha).tex(texXMin, texYMax).endVertex();
			bufferbuilder.pos(matrix, (float)endX, (float)endY, (float)blitOffset).color(red, green, blue, alpha).tex(texXMax, texYMax).endVertex();
			bufferbuilder.pos(matrix, (float)endX, (float)startY, (float)blitOffset).color(red, green, blue, alpha).tex(texXMax, texYMin).endVertex();
			bufferbuilder.pos(matrix, (float)startX, (float)startY, (float)blitOffset).color(red, green, blue, alpha).tex(texXMin, texYMin).endVertex();
		bufferbuilder.finishDrawing();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.draw(bufferbuilder);
	}
}
