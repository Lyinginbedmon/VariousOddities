package com.lying.variousoddities.client.gui;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.Ability.Type;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ScreenTest extends Screen
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/species_select.png");
	public static final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
	public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
	
	public static final Comparator<Ability> ABILITY_SORT = new Comparator<Ability>()
	{
		public int compare(Ability o1, Ability o2)
		{
			Type type1 = o1.getType();
			Type type2 = o2.getType();
			return type1.texIndex > type2.texIndex ? 1 : type1.texIndex < type2.texIndex ? -1 : Ability.SORT_ABILITY.compare(o1, o2);
		}
	};
    
	private static final float TEX_SIZE = 128F;
	private static final float ICON_TEX = 16F / TEX_SIZE;
	
	public ResourceLocation healthKey = AbilityRegistry.getClassRegistryKey(AbilityModifierCon.class).location();
	public ResourceLocation armourKey = AbilityRegistry.getClassRegistryKey(AbilityNaturalArmour.class).location();
	
	private final Player player;
	
	private final Species initialSpecies;
	private List<Species> selectableSpecies = Lists.newArrayList();
	private int index = 0;
	
	private Button typesButton;
	private Button selectButton;
	private boolean keepTypes = false;
	
	private int targetPower;
	private boolean randomise;
	
	private static final int listWidth = 165;
//	boolean initialised = false;
	
	public ScreenTest(Player playerIn, int power, boolean random, @Nullable Species initialIn)
	{
		super(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".species_select"));
		this.player = playerIn;
		this.targetPower = power;
		this.randomise = random;
		this.initialSpecies = initialIn;
	}
	
	public ScreenTest(Player playerIn, int power, boolean random)
	{
		this(playerIn, power, random, Species.HUMAN);
	}
	
	public boolean shouldCloseOnEsc() { return true; }
	
	public boolean isPauseScreen() { return true; }
	
	protected void init()
	{
//		this.initialised = true;
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		renderDirtBackground(0);
		
//		if(!this.randomise)
//			this.speciesList.render(matrixStack, mouseX, mouseY, partialTicks);
		
//    	this.abilityList.render(matrixStack, mouseX, mouseY, partialTicks);
//		renderBackgroundLayer(matrixStack, partialTicks);
//    	hideListEdge();
		
//		drawListBorder(matrixStack, this.speciesList, this.height, 0, 180, 6, TEXTURE);
		
		int yPos = 20;
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 12, 16777215);
		yPos += 15;
		
		if(selectableSpecies.isEmpty())
			return;
		
		// Draw species display name
//		Species currentSpecies = getCurrentSpecies();
		
//		this.selectButton.setMessage(currentSpecies.getDisplayName());
		yPos += this.font.lineHeight + 12;
		
		// Render stars of appropriate colour for power
//		drawStars(matrixStack, yPos, currentSpecies.getPower());
		yPos += this.font.lineHeight + 3;
		
		// Display types
		int health = 20;
//		if(currentSpecies.hasTypes())
//		{
//			Component typesHeader = currentSpecies.getTypes().toHeader();
//			drawCenteredString(matrixStack, this.font, typesHeader, this.width / 2, yPos, -1);
//			health = (int)currentSpecies.getTypes().getPlayerHealth();
//		}
		
		// Health and armour
		yPos += this.font.lineHeight + 3;
		
		double armour = 0;
//		List<Ability> abilities = currentSpecies.getFullAbilities();
//		if(!abilities.isEmpty())
//			for(Ability ability : abilities) 
//			{
//				if(ability.getRegistryName().equals(armourKey))
//					armour += ((AbilityNaturalArmour)ability).amount(); 
//				
//				if(ability.getRegistryName().equals(healthKey))
//					health += ((AbilityModifier)ability).amount();
//			};
		
//		drawHealthAndArmour(matrixStack, this.font, this.width / 2, yPos, health, (int)armour);
		
		// Display abilities
		yPos += this.font.lineHeight + 3;
//		this.abilityList.setTop(yPos);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
