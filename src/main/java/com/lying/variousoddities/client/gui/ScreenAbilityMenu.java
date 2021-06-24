package com.lying.variousoddities.client.gui;

import java.util.Collections;
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
import com.lying.variousoddities.network.PacketSyncAbilities;
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
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenAbilityMenu extends Screen implements IScrollableGUI
{
	public static final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
	
	public static final Minecraft mc = Minecraft.getInstance();
	
	private final PlayerEntity thePlayer;
	private final LivingData theData;
	private final List<ActivatedAbility> abilities = Lists.newArrayList();
	private final List<Ability> passives = Lists.newArrayList();
	private final List<ActivatedAbility> abilitySet = Lists.newArrayList();
	
	private static final int startup = 4;
	private int openTicks = 0;
	
	private int index = 0;
	private int indexEnd = 0;
	
	public ScreenAbilityMenu()
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".ability_menu"));
		thePlayer = Minecraft.getInstance().player;
		theData = LivingData.forEntity(thePlayer);
		
		PacketHandler.sendToServer(new PacketSyncAbilities(thePlayer.getUniqueID()));
		
		for(Ability ability : AbilityRegistry.getCreatureAbilities(thePlayer).values())
			if(!ability.passive())
				abilities.add((ActivatedAbility)ability);
			else
				passives.add(ability);
		
		Collections.sort(abilities, Ability.SORT_ABILITY);
		Collections.sort(passives, Ability.SORT_ABILITY);
	}
	
	public boolean isPauseScreen(){ return false; }
	
	public void onScroll(int wheel)
	{
		int initialIndex = index;
		index -= wheel;
		if(index != initialIndex)
			init();
	}
	
	public void init()
	{
		this.buttons.clear();
		
		abilitySet.clear();
		if(!abilities.isEmpty())
		{
			index = MathHelper.clamp(index, 0, abilities.size() > 7 ? abilities.size() - 7 : abilities.size());
			indexEnd = Math.min(index+7, abilities.size());
			abilitySet.addAll(this.abilities.subList(index, indexEnd));
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
		if(getMouseDist(mouseX, mouseY) > (this.height * 0.45D) || !isLoaded())
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
	
	public boolean isLoaded(){ return this.openTicks > startup; }
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		int maxRadius = (int)(this.height * 0.375D);
		int currentRadius = isLoaded() ? maxRadius : (maxRadius / 2) + (int)((maxRadius / 2) * ((double)openTicks + partialTicks) / (double)startup);
		
		double angleInc = Math.toRadians(360F / (abilitySet.size() + 1));
		Vector2f vec = rotateVector(new Vector2f(0F, currentRadius), angleInc);
		Vector2f vec2 = rotateVector(new Vector2f(0F, this.height * 0.375F), angleInc);
		float angle = (float)-angleInc * 0.5F;
		
		float midX = this.width * 0.5F;
		float midY = this.height * 0.5F;
		if(abilities.size() > 7)
		{
			drawCenteredString(matrixStack, font, new StringTextComponent("..."), (int)midX, (int)(midY + 50), -1);
			if(index > 0)
				drawCenteredString(matrixStack, font, new StringTextComponent("<"), (int)midX - 6, (int)(midY + 50 + font.FONT_HEIGHT * 0.25D), -1);
			if(indexEnd < abilities.size())
				drawCenteredString(matrixStack, font, new StringTextComponent(">"), (int)midX + 5, (int)(midY + 50 + font.FONT_HEIGHT * 0.25D), -1);
		}
		
		if(!abilitySet.isEmpty())
		{
			boolean canFavourite = theData.getAbilities().hasEmptyFavourites();
			ActivatedAbility currentlySelected = getAbilitySlice(mouseX, mouseY);
			int index = 0;
			for(ActivatedAbility ability : abilitySet)
			{
				boolean isSelected = currentlySelected != null && ability.getMapName().equals(currentlySelected.getMapName());
				
				/*
				 * Colour slice
				 * 	black if the ability cannot be used right now
				 * 	white if currently selected
				 * 	alternating shades of cyan otherwise 
				 */
				
				int red = 0, green = 0, blue = 0;
				if(ability.canTrigger(thePlayer))
				{
					if(isSelected)
						red = green = blue = 255;
					else
					{
						red = 200;
						green = 230;
						blue = 255;
					}
				}
				
				int alpha = isSelected ? 150 : (index%2 == 0 ? 120 : 90);
				drawRadialSlice(matrixStack, midX, midY, angle, (float)angleInc, isSelected ? currentRadius * 1.05F : currentRadius, red, green, blue, alpha, partialTicks);
				
				boolean isFavourite = theData.getAbilities().isFavourite(ability.getMapName());
				if(isLoaded() && (canFavourite || isFavourite))
					drawFavouriteButtonAt(matrixStack, midX + vec2.x, midY + vec2.y, isFavourite);
				
				vec = rotateVector(vec, angleInc);
				vec2 = rotateVector(vec2, angleInc);
				angle -= angleInc;
				index++;
			}
			
			if(isLoaded())
				drawAbilityNames(matrixStack);
		}
		else
		{
			// Draw full circle
			drawRadialSlice(matrixStack, midX, midY, angle, (float)angleInc, currentRadius, 200, 230, 255, 90, partialTicks);
			drawCenteredString(matrixStack, font, new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".abilities_menu.empty"), (int)midX, (int)(midY + 50), -1);
		}
		
		if(!passives.isEmpty())
		{
			matrixStack.push();
				matrixStack.translate(midX + maxRadius * 1.25D, midY, 0D);
				matrixStack.push();
					float scale = 0.75F;
					matrixStack.scale(scale, scale, scale);
					int textY = (int)(-(passives.size() * (font.FONT_HEIGHT + 2) * 0.5F));
					int textInc = font.FONT_HEIGHT + 2;
					for(int i=0; i<Math.min(passives.size(), 1 + openTicks); i++)
					{
						Ability passive = passives.get(i);
						drawString(matrixStack, font, passive.getDisplayName(), 0, textY, -1);
						textY += textInc;
					}
				matrixStack.pop();
			matrixStack.pop();
		}
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	public void drawAbilityNames(MatrixStack matrixStack)
	{
		matrixStack.push();
			matrixStack.translate(this.width * 0.5D, this.height * 0.5D, 0);
			matrixStack.push();
				float angleInc = 360F / (abilitySet.size() + 1);
				for(int index=0; index<abilitySet.size(); index++)
				{
					ActivatedAbility ability = abilitySet.get(index);
					matrixStack.push();
						float angle = angleInc * (index + 1);
						matrixStack.rotate(Vector3f.ZP.rotationDegrees(angleInc * (index + 1)));
						matrixStack.translate(0, 60, 0);
						matrixStack.push();
							if(angle%90 == 0)
								matrixStack.rotate(Vector3f.ZP.rotationDegrees(-angle));
							else
							{
								matrixStack.rotate(Vector3f.ZP.rotationDegrees(-90F));
								if(angle > 180)
									matrixStack.rotate(Vector3f.ZP.rotationDegrees(-180F));
							}
							
							List<ITextProperties> messageLines = VOHelper.getWrappedText(ability.getDisplayName(), font, 90);
							int textX = 0;
							for(ITextProperties line : messageLines)
							{
								int length = font.getStringWidth(line.getString());
								if(length > 80)
									textX = Math.min(textX, length - 80);
							}
							
							int textCol = ability.isActive() ? TextFormatting.GOLD.getColor() : -1;
							int textY = (int)(messageLines.size() * font.FONT_HEIGHT * -0.5D);
							for(ITextProperties line : messageLines)
							{
								drawCenteredString(matrixStack, font, line.getString(), textX, textY, textCol);
								textY += font.FONT_HEIGHT;
							}
						matrixStack.pop();
					matrixStack.pop();
				}
			matrixStack.pop();
		matrixStack.pop();
	}
	
	@SuppressWarnings("deprecation")
	public void drawFavouriteButtonAt(MatrixStack matrix, double posX, double posY, boolean bright)
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
			blit(matrix.getLast().getMatrix(), (int)posX, (int)endX, (int)posY, (int)endY, 0, texXMin, texXMax, texYMin, texYMax, bright);
		matrix.pop();
	}
	
	@SuppressWarnings("deprecation")
	private void drawArc(MatrixStack stackIn, float originX, float originY, float initialAngle, float angleInc, float radius, int red, int green, int blue, int alpha, float partialTicks)
	{
		RenderSystem.disableCull();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.shadeModel(GL11.GL_FLAT);
		
		stackIn.push();
			Matrix4f matrix4f = stackIn.getLast().getMatrix();
			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
				// Centre vertex
				buffer.pos(matrix4f, originX, originY, 0F).color(255, 255, 255, 255).endVertex();
				
				Vector2f vec = rotateVector(new Vector2f(0F, radius), -initialAngle);
				int rotationsPerArc = 16;
				// Perimeter vertices in pairs
				for(int i=0; i<=rotationsPerArc; i++)
				{
					buffer.pos(matrix4f, originX + vec.x, originY + vec.y, 0F).color(red, green, blue, alpha).endVertex();
					vec = rotateVector(vec, angleInc / rotationsPerArc);
				}
			buffer.finishDrawing();
			WorldVertexBufferUploader.draw(buffer);
		stackIn.pop();
		RenderSystem.disableBlend();
	}
	
	public void drawRadialSlice(MatrixStack stackIn, float originX, float originY, float angle, float angleInc, float radius, int red, int green, int blue, int alpha, float partialTicks)
	{
		drawArc(stackIn, originX, originY, angle, angleInc, radius * 1.15F, red, green, blue, alpha - 50, partialTicks);
		drawArc(stackIn, originX, originY, angle, angleInc, radius * 0.85F, red, green, blue, alpha, partialTicks);
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
	private static void blit(Matrix4f matrix, int startX, int endX, int startY, int endY, int blitOffset, float texXMin, float texXMax, float texYMin, float texYMax, boolean bright)
	{
		int red = bright ? 255 : 100;
		int green = bright ? 170 : 100;
		int blue = bright ? 0 : 100;
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
			buffer.pos(matrix, (float)startX, (float)endY, (float)blitOffset).color(red, green, blue, 255).tex(texXMin, texYMax).endVertex();
			buffer.pos(matrix, (float)endX, (float)endY, (float)blitOffset).color(red, green, blue, 255).tex(texXMax, texYMax).endVertex();
			buffer.pos(matrix, (float)endX, (float)startY, (float)blitOffset).color(red, green, blue, 255).tex(texXMax, texYMin).endVertex();
			buffer.pos(matrix, (float)startX, (float)startY, (float)blitOffset).color(red, green, blue, 255).tex(texXMin, texYMin).endVertex();
		buffer.finishDrawing();
		RenderSystem.enableAlphaTest();
		WorldVertexBufferUploader.draw(buffer);
	}
}
