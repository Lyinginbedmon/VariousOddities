package com.lying.variousoddities.client.gui;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.client.KeyBindings;
import com.lying.variousoddities.network.PacketAbilityActivate;
import com.lying.variousoddities.network.PacketAbilityFavourite;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.abilities.Ability;
import com.lying.variousoddities.types.abilities.AbilityRegistry;
import com.lying.variousoddities.types.abilities.ActivatedAbility;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;

public class ScreenAbilityMenu extends Screen
{
	private final List<AbilityButton> abilityList = Lists.newArrayList();
	private FavouriteButton[] favouriteList = new FavouriteButton[7];
	
	private final PlayerEntity thePlayer;
	private final LivingData theData;
	private final List<ActivatedAbility> abilities = Lists.newArrayList();
	
	private static final int maxRadius = 50;
	private static final int startup = 6;
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
		int midX = this.width / 2;
		int midY = this.height / 2;
		
		this.buttons.clear();
		abilityList.clear();
		abilityList.add(addButton(new AbilityButton(midX, midY, null)));
		if(!abilities.isEmpty())
		{
			index = MathHelper.clamp(index, 0, abilities.size() > 7 ? abilities.size() - 7 : abilities.size());
			int end = Math.min(index+7, abilities.size());
			favouriteList = new FavouriteButton[end];
			
			for(int i=index; i<end; i++)
			{
				ActivatedAbility ability = (ActivatedAbility)abilities.get(i);
				AbilityButton button = new AbilityButton(midX, midY, ability);
				button.active = false;
				abilityList.add(addButton(button));
				
				favouriteList[i - index] = addButton(new FavouriteButton(midX, midY, ability, this.theData));
			}
		}
	}
	
	public void tick()
	{
		this.openTicks++;
		
		int midX = this.width / 2;
		int midY = this.height / 2;
		int xOff = 0;
		int yOff = Math.min(maxRadius, (maxRadius / 2) + (int)((maxRadius / 2) * ((double)openTicks / (double)startup)));
		Vector2f vec = new Vector2f(xOff, yOff);
		double angle = Math.toRadians(360F / abilityList.size());
		for(AbilityButton button : abilityList)
		{
			button.active = button.isExit() || openTicks > startup && button.isActive(thePlayer);
			button.setPosition(midX + (int)vec.x - button.getWidth() / 2, midY + (int)vec.y - button.getHeightRealms() / 2);
			vec = rotateVector(vec, angle);
		}
		
		int radius = maxRadius + 50;
		xOff = 0;
		yOff = Math.min(radius, (radius / 2) + (int)((radius / 2) * ((double)openTicks / (double)startup)));
		vec = rotateVector(new Vector2f(xOff, yOff), angle);
		for(int i=0; i<favouriteList.length; i++)
		{
			FavouriteButton button = favouriteList[i];
			button.setPosition(midX + (int)vec.x - button.getWidth() / 2, midY + (int)vec.y - button.getHeightRealms() / 2);
			button.active = button.visible = button.isFavourite() || theData.getAbilities().hasEmptyFavourites();
			vec = rotateVector(vec, angle);
		}
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(KeyBindings.ABILITY_MENU.matchesKey(keyCode, scanCode))
		{
			this.closeScreen();
			return true;
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	private Vector2f rotateVector(Vector2f vec, double angle)
	{
		double x = vec.x * Math.cos(angle) - vec.y * Math.sin(angle);
		double y = vec.x * Math.sin(angle) + vec.y * Math.cos(angle);
		return new Vector2f((float)x, (float)y);
	}
	
	private class AbilityButton extends Widget
	{
		@Nullable
		private final ActivatedAbility ability;
		
		public AbilityButton(int x, int y, @Nullable ActivatedAbility abilityIn)
		{
			super(x - 10, y - 10, 20, 20, abilityIn == null ? StringTextComponent.EMPTY : abilityIn.translatedName());
			this.ability = abilityIn;
		}
		
		public void setPosition(int xIn, int yIn)
		{
			this.x = xIn;
			this.y = yIn;
		}
		
		public void onClick(double mouseX, double mouseY)
		{
			if(this.ability != null)
			{
				this.ability.trigger(thePlayer, Dist.CLIENT);
				PacketHandler.sendToServer(new PacketAbilityActivate(this.ability.getMapName()));
			}
			Minecraft.getInstance().currentScreen.closeScreen();
		}
		
		@Nullable
		public ActivatedAbility getAbility(){ return this.ability; }
		
		public boolean isExit(){ return this.ability == null; }
		
		public boolean isActive(LivingEntity entity)
		{
			return this.ability == null || this.ability.canTrigger(entity);
		}
		
		@SuppressWarnings("deprecation")
		public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
		{
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontrenderer = minecraft.fontRenderer;
			minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
			int i = this.getYImage(this.isHovered());
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			this.blit(matrixStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
			this.blit(matrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
			this.renderBg(matrixStack, minecraft, mouseX, mouseY);
			
			int j = getFGColor();
			
			matrixStack.push();
				// TODO Rotate text outwards from screen centre
				int textY = this.y + (this.height - fontrenderer.FONT_HEIGHT) / 2;
				List<ITextProperties> messageLines = VOHelper.getWrappedText(getMessage(), fontrenderer, 90);
				if(messageLines.size() > 1)
				{
					textY = this.y + (this.height - fontrenderer.FONT_HEIGHT / 2) / 2;
					textY -= (int)((double)messageLines.size() / 2D * 8);
				}
				for(ITextProperties line : messageLines)
				{
					drawCenteredString(matrixStack, fontrenderer, line.getString(), this.x + this.width / 2, textY, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
					textY += 8;
				}
			matrixStack.pop();
		}
	}
	
	private class FavouriteButton extends Widget
	{
		private final ActivatedAbility ability;
		private final LivingData data;
		
		public FavouriteButton(int x, int y, ActivatedAbility abilityIn, LivingData dataIn)
		{
			super(x - 10, y - 10, 20, 20, StringTextComponent.EMPTY);
			this.ability = abilityIn;
			this.data = dataIn;
		}
		
		public void setPosition(int xIn, int yIn)
		{
			this.x = xIn;
			this.y = yIn;
		}
		
		public boolean isFavourite()
		{
			return data.getAbilities().isFavourite(ability.getMapName());
		}
		
		public void onClick(double mouseX, double mouseY)
		{
			if(this.ability != null)
				PacketHandler.sendToServer(new PacketAbilityFavourite(ability.getMapName(), !isFavourite()));
		}
	}
}
