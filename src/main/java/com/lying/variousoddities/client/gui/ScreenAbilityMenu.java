package com.lying.variousoddities.client.gui;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.client.KeyBindings;
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.network.PacketAbilityActivate;
import com.lying.variousoddities.network.PacketAbilityFavourite;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.ActivatedAbility;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;

public class ScreenAbilityMenu extends Screen
{
	public static final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
	public static final Minecraft mc = Minecraft.getInstance();
	
	private final PlayerEntity thePlayer;
	private final LivingData theData;
	private final List<ActivatedAbility> abilities = Lists.newArrayList();
	private final List<ActivatedAbility> abilitySet = Lists.newArrayList();
	
	private static final int startup = 4;
	private int openTicks = 0;
	
	int index = 0;
	
	public ScreenAbilityMenu()
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".ability_menu"));
		thePlayer = Minecraft.getInstance().player;
		theData = LivingData.forEntity(thePlayer);
		
		for(Ability ability : AbilityRegistry.getCreatureAbilities(thePlayer).values())
			if(!ability.passive())
				abilities.add((ActivatedAbility)ability);
	}
	
	public boolean isPauseScreen(){ return false; }
	
	public void init()
	{
		this.buttons.clear();
		abilitySet.clear();
		if(!abilities.isEmpty())
		{
			index = MathHelper.clamp(index, 0, abilities.size() > 7 ? abilities.size() - 7 : abilities.size());
			int end = Math.min(index+7, abilities.size());
			
			for(int i=index; i<end; i++)
				abilitySet.add((ActivatedAbility)abilities.get(i));
		}
	}
	
	public void tick()
	{
		this.openTicks++;
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		ActivatedAbility selected = getAbilitySlice(mouseX, mouseY);
		if(selected == null)
		{
			closeScreen();
			return true;
		}
		else
		{
			activateAbility(selected, isFavourite(mouseX, mouseY), this.theData);
			return true;
		}
	}
	
	/** Returns the distance of the mouse from the crosshair */
	public double getMouseDist(double mouseX, double mouseY)
	{
		double midX = this.width * 0.5D;
		double midY = this.height * 0.5D;
		
		double dirX = mouseX - midX;
		double dirY = mouseY - midY;
		double lenX = Math.abs(dirX);
		double lenY = Math.abs(dirY);
		
		return Math.sqrt(lenX * lenX + lenY * lenY);
	}
	
	/** Returns true if the mouse is far enough from the crosshair to engage favouriting */
	public boolean isFavourite(double mouseX, double mouseY)
	{
		return getMouseDist(mouseX, mouseY) >= (this.height * 0.3D);
	}
	
	/** Returns the ability attached to the area the mouse is in, if any */
	public @Nullable ActivatedAbility getAbilitySlice(double mouseX, double mouseY)
	{
		if(getMouseDist(mouseX, mouseY) > (this.height * 0.45D) || this.openTicks < (Reference.Values.TICKS_PER_SECOND * 0.5D))
			return null;
		
		double midX = this.width * 0.5D;
		double midY = this.height * 0.5D;
		
		double dirX = mouseX - midX;
		double dirY = mouseY - midY;
		
		Vector3d direction = (new Vector3d(dirX, dirY, 0D)).normalize();
		double angle = (Math.atan2(direction.x, direction.y) / Math.PI) * 180D;
		while(angle < 0)
			angle += 360;
		
		int buttonCount = abilitySet.size() + 1;
		double menuInc = 360F / buttonCount;
		
		int index = 0;
		double radialStart = -(menuInc / 2);
		double radialMin = radialStart;
		double radialMax = radialMin + menuInc;
		while((angle < radialMin || angle > radialMax) && index < buttonCount)
		{
			++index;
			radialMin += menuInc;
			radialMax += menuInc;
		}
		
		index = index%buttonCount;
		if(index > 0)
			index = buttonCount - index;
		
		return index == 0 ? null : abilitySet.get(index - 1);
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		int maxRadius = (int)(this.height * 0.375D);
		int yOff = Math.min(maxRadius, (maxRadius / 2) + (int)((maxRadius / 2) * ((double)openTicks / (double)startup)));
		
		double angleInc = Math.toRadians(360F / (abilitySet.size() + 1));
		Vector2f vec = rotateVector(new Vector2f(0F, yOff), angleInc);
		Vector2f vec2 = rotateVector(new Vector2f(0F, this.height * 0.375F), angleInc);
		float angle = (float)-angleInc * 0.5F;
		
		ActivatedAbility currentlySelected = getAbilitySlice(mouseX, mouseY);
		int index = 0;
		for(ActivatedAbility ability : abilitySet)
		{
			
			int colour = ability.canTrigger(thePlayer) ? (currentlySelected != null && ability.getMapName().equals(currentlySelected.getMapName()) ? -1 : (index%2 == 0 ? 9868950 : 8553090)) : 0;
			drawRadialSlice(matrixStack, angle, (float)angleInc, yOff, colour, partialTicks);
			
			if(this.openTicks >= startup)
				if(theData.getAbilities().isFavourite(ability.getMapName()) || theData.getAbilities().hasEmptyFavourites())
					drawFavouriteButtonAt(matrixStack, vec2.x + this.width / 2, vec2.y + this.height / 2, theData.getAbilities().isFavourite(ability.getMapName()) ? 1F : 0.5F);
			
			vec = rotateVector(vec, angleInc);
			vec2 = rotateVector(vec2, angleInc);
			angle -= angleInc;
			index++;
		}
		
		if(this.openTicks >= startup)
			drawAbilityNames(matrixStack);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	public void drawAbilityNames(MatrixStack matrixStack)
	{
		int maxRadius = (int)(this.height * 0.375D);
		double angleInc = Math.toRadians(360F / (abilitySet.size() + 1));
		matrixStack.push();
			Vector2f vec = rotateVector(new Vector2f(0F, maxRadius), angleInc);
			for(ActivatedAbility ability : abilitySet)
			{
				// TODO Rotate text outwards from screen centre
				int y = (int)vec.y;
				int textY = (int)((y * 0.6F) + (this.height - font.FONT_HEIGHT) / 2);
				List<ITextProperties> messageLines = VOHelper.getWrappedText(ability.getDisplayName(), font, 90);
				if(messageLines.size() > 1)
				{
					textY = y + (this.height - font.FONT_HEIGHT / 2) / 2;
					textY -= (int)((double)messageLines.size() / 2D * 8);
				}
				
				for(ITextProperties line : messageLines)
				{
					drawCenteredString(matrixStack, font, line.getString(), (int)((vec.x * 0.6F) + this.width / 2), textY, -1);
					textY += 8;
				}
				
				vec = rotateVector(vec, angleInc);
			}
		matrixStack.pop();
	}
	
	@SuppressWarnings("deprecation")
	public void drawFavouriteButtonAt(MatrixStack matrix, double posX, double posY, float brightness)
	{
		posX -= 8;
		posY -= 8;
		
		// Texture co-ordinates
		float texXMin = 0;
		float texXMax = (16F / 128F);
		
		float texYMin = (16F / 128F);
		float texYMax = (32F / 128F);
		
		// Screen co-ordinates
		double endX = posX + 16;
		double endY = posY + 16;
		
		RenderSystem.enableTexture();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		matrix.push();
			Minecraft.getInstance().getTextureManager().bindTexture(ABILITY_ICONS);
			blit(matrix.getLast().getMatrix(), (int)posX, (int)endX, (int)posY, (int)endY, 0, texXMin, texXMax, texYMin, texYMax, brightness);
		matrix.pop();
	}
	
	@SuppressWarnings("deprecation")
	private void drawArc(MatrixStack stackIn, float initialAngle, float angleInc, float radius, int colour, int alpha, float partialTicks)
	{
		float width = (float)this.width;
		float height = (float)this.height;
		float midX = width * 0.5F;
		float midY = height * 0.5F;
		
		RenderSystem.disableCull();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		
		stackIn.push();
			Matrix4f matrix4f = stackIn.getLast().getMatrix();
			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				// Centre vertex
				buffer.pos(matrix4f, midX, midY, 0F).color(255, 255, 255, 255).endVertex();
				
				Vector2f vec = rotateVector(new Vector2f(0F, radius), -initialAngle);
				int rotationsPerArc = 8;
				// Perimeter vertices in pairs
				for(int i=0; i<=rotationsPerArc; i++)
				{
					buffer.pos(matrix4f, midX + vec.x, midY + vec.y, 0F).color(colour, colour, colour, alpha).endVertex();
					vec = rotateVector(vec, angleInc / rotationsPerArc);
				}
			buffer.finishDrawing();
			WorldVertexBufferUploader.draw(buffer);
		stackIn.pop();
		RenderSystem.enableCull();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
	
	public void drawRadialSlice(MatrixStack stackIn, float angle, float angleInc, float radius, int colour, float partialTicks)
	{
		drawArc(stackIn, angle, angleInc, radius * 1.15F, colour, 69, partialTicks);
		drawArc(stackIn, angle, angleInc, radius * 0.85F, colour, 120, partialTicks);
	}
	
	public void activateAbility(@Nonnull ActivatedAbility ability, boolean favourite, LivingData data)
	{
		if(ability == null)
			return;
		
		if(!favourite)
		{
			if(ability.canTrigger(thePlayer))
			{
				ability.trigger(thePlayer, Dist.CLIENT);
				PacketHandler.sendToServer(new PacketAbilityActivate(ability.getMapName()));
				closeScreen();
			}
		}
		else
		{
			boolean isFavourite = data.getAbilities().isFavourite(ability.getMapName());
			PacketHandler.sendToServer(new PacketAbilityFavourite(ability.getMapName(), !isFavourite));
		}
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(KeyBindings.ABILITY_MENU.matchesKey(keyCode, scanCode) && !ConfigVO.CLIENT.holdKeyForMenu.get())
		{
			this.closeScreen();
			return true;
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	public boolean keyReleased(int key, int scanCode, int modifiers)
	{
		if(KeyBindings.ABILITY_MENU.matchesKey(key, scanCode) && ConfigVO.CLIENT.holdKeyForMenu.get())
		{
			this.closeScreen();
			return true;
		}
		return super.keyReleased(key, scanCode, modifiers);
	}
	
	private Vector2f rotateVector(Vector2f vec, double angle)
	{
		double x = vec.x * Math.cos(angle) - vec.y * Math.sin(angle);
		double y = vec.x * Math.sin(angle) + vec.y * Math.cos(angle);
		return new Vector2f((float)x, (float)y);
	}
	
	@SuppressWarnings("deprecation")
	private static void blit(Matrix4f matrix, int startX, int endX, int startY, int endY, int blitOffset, float texXMin, float texXMax, float texYMin, float texYMax, float brightness)
	{
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR.param, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
			bufferbuilder.pos(matrix, (float)startX, (float)endY, (float)blitOffset).color(brightness, brightness, brightness, 1F).tex(texXMin, texYMax).endVertex();
			bufferbuilder.pos(matrix, (float)endX, (float)endY, (float)blitOffset).color(brightness, brightness, brightness, 1F).tex(texXMax, texYMax).endVertex();
			bufferbuilder.pos(matrix, (float)endX, (float)startY, (float)blitOffset).color(brightness, brightness, brightness, 1F).tex(texXMax, texYMin).endVertex();
			bufferbuilder.pos(matrix, (float)startX, (float)startY, (float)blitOffset).color(brightness, brightness, brightness, 1F).tex(texXMin, texYMin).endVertex();
		bufferbuilder.finishDrawing();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.draw(bufferbuilder);
	}
}
