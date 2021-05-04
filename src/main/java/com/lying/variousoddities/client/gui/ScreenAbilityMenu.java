package com.lying.variousoddities.client.gui;

import java.util.List;

import javax.annotation.Nonnull;
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;

public class ScreenAbilityMenu extends Screen
{
	private final PlayerEntity thePlayer;
	private final LivingData theData;
	private final List<ActivatedAbility> abilities = Lists.newArrayList();
	private final List<ActivatedAbility> abilitySet = Lists.newArrayList();
	
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
		int xOff = 0;
		
		int maxRadius = (int)((this.height * 0.5D) * 0.75D);
		int yOff = Math.min(maxRadius, (maxRadius / 2) + (int)((maxRadius / 2) * ((double)openTicks / (double)startup)));
		double angleInc = Math.toRadians(360F / (abilitySet.size() + 1));
		Vector2f vec = rotateVector(new Vector2f(xOff, yOff), angleInc);
		
		ActivatedAbility currentlySelected = getAbilitySlice(mouseX, mouseY);
		
		for(ActivatedAbility ability : abilitySet)
		{
			int x = (int)vec.x;
			int y = (int)vec.y;
			
			int colour = ability.canTrigger(thePlayer) ? -1 : 0;
			if(currentlySelected != null && ability.getMapName().equals(currentlySelected.getMapName()))
			{
				x = (int)((double)x * 0.75D);
				y = (int)((double)y * 0.75D);
				
				if(isFavourite(mouseX, mouseY))
					colour = 8453920;
			}
			
			matrixStack.push();
				// TODO Rotate text outwards from screen centre
				int textY = y + (this.height - font.FONT_HEIGHT) / 2;
				List<ITextProperties> messageLines = VOHelper.getWrappedText(ability.translatedName(), font, 90);
				if(messageLines.size() > 1)
				{
					textY = y + (this.height - font.FONT_HEIGHT / 2) / 2;
					textY -= (int)((double)messageLines.size() / 2D * 8);
				}
				
				for(ITextProperties line : messageLines)
				{
					drawCenteredString(matrixStack, font, line.getString(), x + this.width / 2, textY, colour);
					textY += 8;
				}
			matrixStack.pop();
			
			vec = rotateVector(vec, angleInc);
		}
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	public void activateAbility(@Nonnull ActivatedAbility ability, boolean favourite, LivingData data)
	{
		if(ability == null)
			return;
		
		if(!favourite)
		{
			ability.trigger(thePlayer, Dist.CLIENT);
			PacketHandler.sendToServer(new PacketAbilityActivate(ability.getMapName()));
			closeScreen();
		}
		else
		{
			boolean isFavourite = data.getAbilities().isFavourite(ability.getMapName());
			PacketHandler.sendToServer(new PacketAbilityFavourite(ability.getMapName(), !isFavourite));
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
}
