package com.lying.variousoddities.client.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesSelected;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.Ability.Type;
import com.lying.variousoddities.species.abilities.AbilityModifier;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class ScreenSelectSpecies extends Screen
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
	
	private SpeciesList speciesList;
	private List<Species> selectableSpecies = Lists.newArrayList();
	private int index = 0;
	
	private Button typesButton;
	private Button selectButton;
	private boolean keepTypes = false;
	
	private int targetPower;
	private boolean randomise;
	
	private AbilityList abilityList;
	
	public ScreenSelectSpecies(Player playerIn, int power, boolean random)
	{
		super(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".species_select"));
		this.player = playerIn;
		this.targetPower = power;
		this.randomise = random;
		setCurrentSpecies(Species.HUMAN);
	}
	
	public ScreenSelectSpecies(Player playerIn, int power, boolean random, @Nullable Species initialIn)
	{
		this(playerIn, power, random);
		setCurrentSpecies(initialIn);
	}
	
	private void initSpecies()
	{
		if(player.isCreative())
			selectableSpecies.addAll(VORegistries.SPECIES.values());
		else
			VORegistries.SPECIES.values().forEach((species) -> { if(species.getPower() <= targetPower && species.isPlayerSelectable()) selectableSpecies.add(species); });
		
		selectableSpecies.sort(new Comparator<Species>()
			{
				public int compare(Species o1, Species o2)
				{
					String name1 = o1.getDisplayName().getString();
					String name2 = o2.getDisplayName().getString();
					
					List<String> names = Arrays.asList(name1, name2);
					Collections.sort(names);
					
					int index1 = names.indexOf(name1);
					int index2 = names.indexOf(name2);
					return (index1 > index2 ? 1 : index1 < index2 ? -1 : 0);
				}
			});
	}
	
	private int indexBySpecies(@Nullable Species speciesIn)
	{
		if(speciesIn != null && !this.selectableSpecies.isEmpty())
			for(int index = 0; index < this.selectableSpecies.size(); index++)
				if(this.selectableSpecies.get(index).getRegistryName().equals(speciesIn.getRegistryName()))
					return index;
		return 0;
	}
	
	public void setCurrentSpecies(@Nullable Species speciesIn)
	{
		if(this.selectableSpecies.isEmpty())
			initSpecies();
		this.index = indexBySpecies(speciesIn);
		
		if(this.abilityList != null)
			this.abilityList.clear();
	}
	
	public boolean shouldCloseOnEsc(){ return false; }
	
	public boolean isPauseScreen(){ return true; }
	
	public void tick()
	{
		super.tick();
		if(this.typesButton == null)
			return;
		
		LivingData data = LivingData.forEntity(player);
		typesButton.visible = typesButton.active = data.hasCustomTypes();
		
		Species currentSpecies = getCurrentSpecies();
		if(currentSpecies != null && !currentSpecies.getAbilities().isEmpty() && this.abilityList.isEmpty())
			populateAbilityList(currentSpecies.getFullAbilities());
		
		if(this.randomise && !this.selectableSpecies.isEmpty())
		{
			Species selected = Species.HUMAN;
			RandomSource rand = player.getRandom();
			if(!this.selectableSpecies.isEmpty())
				do
				{
					selected = this.selectableSpecies.get(rand.nextInt(this.selectableSpecies.size()));
				}
				while(selected.getPower() > this.targetPower);
			
			Minecraft.getInstance().setScreen(new ScreenSelectTemplates(player, selected, EnumSet.noneOf(EnumCreatureType.class), this.targetPower, this.randomise));
		}
	}
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		renderDirtBackground(0);
		if(!this.randomise)
			this.speciesList.render(matrixStack, mouseX, mouseY, partialTicks);
		
    	this.abilityList.render(matrixStack, mouseX, mouseY, partialTicks);
		renderBackgroundLayer(matrixStack, partialTicks);
    	hideListEdge();
		
		drawListBorder(matrixStack, this.speciesList, this.height, 0, 180, 6, TEXTURE);
		
		int yPos = 20;
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 12, 16777215);
		yPos += 15;
		
		if(selectableSpecies.isEmpty())
			return;
		
		// Draw species display name
		Species currentSpecies = getCurrentSpecies();
		
		this.selectButton.setMessage(currentSpecies.getDisplayName());
		yPos += this.font.lineHeight + 12;
		
		// Render stars of appropriate colour for power
		drawStars(matrixStack, yPos, currentSpecies.getPower());
		yPos += this.font.lineHeight + 3;
		
		// Display types
		int health = 20;
		if(currentSpecies.hasTypes())
		{
			Component typesHeader = currentSpecies.getTypes().toHeader();
			drawCenteredString(matrixStack, this.font, typesHeader, this.width / 2, yPos, -1);
			health = (int)currentSpecies.getTypes().getPlayerHealth();
		}
		
		// Health and armour
		yPos += this.font.lineHeight + 3;
		
		double armour = 0;
		List<Ability> abilities = currentSpecies.getFullAbilities();
		if(!abilities.isEmpty())
			for(Ability ability : abilities) 
			{
				if(ability.getRegistryName().equals(armourKey))
					armour += ((AbilityNaturalArmour)ability).amount(); 
				
				if(ability.getRegistryName().equals(healthKey))
					health += ((AbilityModifier)ability).amount();
			};
		
		drawHealthAndArmour(matrixStack, this.font, this.width / 2, yPos, health, (int)armour);
		
		// Display abilities
		yPos += this.font.lineHeight + 3;
		this.abilityList.setTop(yPos);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
    
	public static void drawListBorder(PoseStack matrixStack, ObjectSelectionList<?> listIn, int heightIn, int texX, int texY, int growth, ResourceLocation texture)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, texture);
		
		int listLeft = listIn.getLeft() - growth;
		int listRight = listIn.getLeft() + listIn.getWidth() - 6 + growth;
		int listTop = 32 - growth;
		int listBottom = heightIn - 51 - 6 + growth;
		int sideHeight = listBottom - (listTop + 6);
		int sideWidth = listRight - (listLeft + 6);
		// Top Left
		Screen.blit(matrixStack, listLeft, listTop, 6, 6, texX, texY, 6, 6, 256, 256);
		// Top Right
		Screen.blit(matrixStack, listRight, listTop, 6, 6, texX + 26, texY, 6, 6, 256, 256);
		// Bot Left
		Screen.blit(matrixStack, listLeft, listBottom, 6, 6, texX, texY + 26, 6, 6, 256, 256);
		// Bot Right
		Screen.blit(matrixStack, listRight, listBottom, 6, 6, texX + 26, texY + 26, 6, 6, 256, 256);
		// Left
		Screen.blit(matrixStack, listLeft, listTop + 6, 6, sideHeight, texX, texY + 6, 6, 20, 256, 256);
		// Right
		Screen.blit(matrixStack, listRight, listTop + 6, 6, sideHeight, texX + 26, texY + 6, 6, 20, 256, 256);
		// Top
		Screen.blit(matrixStack, listLeft + 6, listTop, sideWidth, 6, texX + 6, texY, 20, 6, 256, 256);
		// Bottom
		Screen.blit(matrixStack, listLeft + 6, listBottom, sideWidth, 6, texX + 6, texY + 26, 20, 6, 256, 256);
	}
	
    private void hideListEdge()
    {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
		int listRight = this.speciesList.getLeft() + this.speciesList.getWidth() + 5;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int listStart = 32;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
	        bufferbuilder.vertex(0.0D, (double)listStart, 0.0D).uv(0.0F, (float)listStart / 32.0F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.vertex((double)listRight, (double)listStart, 0.0D).uv((float)listRight / 32.0F, (float)listStart / 32.0F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.vertex((double)listRight, (double)0, 0.0D).uv((float)listRight / 32.0F, (float)0 / 32F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.vertex(0.0D, (double)0, 0.0D).uv(0.0F, (float)0 / 32F).color(64, 64, 64, 255).endVertex();
	    BufferUploader.drawWithShader(bufferbuilder.end());
		
		int listEnd = this.height - 51;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
	        bufferbuilder.vertex(0.0D, (double)this.height, 0.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.vertex((double)listRight, (double)this.height, 0.0D).uv((float)listRight / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.vertex((double)listRight, (double)listEnd, 0.0D).uv((float)listRight / 32.0F, (float)listEnd / 32F).color(64, 64, 64, 255).endVertex();
	        bufferbuilder.vertex(0.0D, (double)listEnd, 0.0D).uv(0.0F, (float)listEnd / 32F).color(64, 64, 64, 255).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
		RenderSystem.enableTexture();
//		RenderSystem.shadeModel(7424);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
    }
	
	public void renderBackgroundLayer(PoseStack matrixStack, float partialTicks)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		
		int x = (this.width - 175) / 2;
		// Top
		this.blit(matrixStack, x, 45, 0, 0, 175, 46);
		// Middle
		Screen.blit(matrixStack, x, 45 + 46, 175, this.height - 20 - 45 - 46, 0, 47, 175, 125, 256, 256);
		// Bottom
		this.blit(matrixStack, x, this.height - 20, 0, 172, 175, 8);
	}
	
	public void drawStars(PoseStack matrixStack, int yPos, int power)
	{
		int stars = Math.max(Math.abs(power), 1);
		
		int midX = this.width / 2;
		int startX = midX - (9 * stars) / 2;
		
		drawStars(matrixStack, power, startX, yPos);
	}
	
	public static void drawStars(PoseStack matrixStack, int power, int xPos, int yPos)
	{
		float red = power > 0 ? 0F : power < 0 ? 1F : 0.5F;
		float green = power > 0 ? 1F : power < 0 ? 0F : 0.5F;
		float blue = power > 0 ? 0F : power < 0 ? 0F : 0.5F;
		drawStars(matrixStack, power, xPos, yPos, red, green, blue);
	}
	
	public static void drawStars(PoseStack matrixStack, int power, int xPos, int yPos, float red, float green, float blue)
	{
		int stars = Math.max(Math.abs(power), 1);
		int startX = xPos;
		for(int i=0; i<stars; i++)
		{
			drawStar(matrixStack, startX, yPos, red, green, blue);
			startX += 9;
		}
	}
	
	public static void drawStar(PoseStack matrixStack, int xPos, int yPos, float red, float green, float blue)
	{
		int startX = xPos;
		int endX = startX + 9;
		matrixStack.pushPose();
			RenderSystem.setShaderTexture(0, ABILITY_ICONS);
			blit(matrixStack.last().pose(), startX, (int)endX, yPos, yPos + 9, 0, 0, ICON_TEX, ICON_TEX, ICON_TEX * 2, red, green, blue, 1F);
		matrixStack.popPose();
	}
	
    public void init()
    {
    	clearWidgets();
        
        int midX = width / 2;
    	
        if(this.selectableSpecies.isEmpty())
        	initSpecies();
		this.speciesList = new SpeciesList(minecraft, this, 200, this.height, this.selectableSpecies);
		this.speciesList.setLeftPos((this.width - 170) / 2 - 11 - this.speciesList.getRowWidth());
		if(!this.randomise)
			addWidget(this.speciesList);
		
		int listWidth = 165;
		this.abilityList = new AbilityList(minecraft, (this.width - listWidth) / 2, listWidth, this.height, 20);
		addWidget(this.abilityList);
    	
    	this.addRenderableWidget(selectButton = new Button(midX - 50, 35, 100, 20, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".species_select.select"), (button) -> 
    		{
    			Minecraft.getInstance().setScreen(new ScreenSelectTemplates(player, getCurrentSpecies(), keepTypes ? EnumCreatureType.getCustomTypes(player).asSet() : EnumSet.noneOf(EnumCreatureType.class), this.targetPower, this.randomise));
    		},
				(button,matrix,x,y) -> { renderTooltip(matrix, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".species_select.select"), x, y); }));
    	
    	this.addRenderableWidget(typesButton = new Button(midX - 60, height - 25, 120, 20, Component.translatable("gui.varodd.species_select.lose_types"), (button) ->
    		{
    			keepTypes = !keepTypes;
    			typesButton.setMessage(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".species_select."+(keepTypes ? "keep_types" : "lose_types")));
    		}, (button,matrix,x,y) -> { renderTooltip(matrix, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".species_select.keep_types.info"), x, y); }));
    	
    	this.addRenderableWidget(new Button(midX + 100, 35, 20, 20, Component.literal("X"), (button) -> 
    	{
			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUUID()));
			Minecraft.getInstance().setScreen(null);
    	}, (button,matrix,x,y) -> { renderTooltip(matrix, Component.translatable("gui.varodd.species_select.exit"), x, y); }));
    	this.addRenderableWidget(new Button(this.width - 23, 3, 20, 20, Component.literal(">"), (button) -> 
    		{
    			Minecraft.getInstance().setScreen(new ScreenSelectTemplates(player, Species.HUMAN, keepTypes ? EnumCreatureType.getCustomTypes(player).asSet() : EnumSet.noneOf(EnumCreatureType.class), this.targetPower, this.randomise));
    		},
    			(button,matrix,x,y) -> { renderTooltip(matrix, Component.translatable("gui."+Reference.ModInfo.MOD_ID+".templates_select"), x, y); }));
    }
    
    private void populateAbilityList(List<Ability> abilitiesIn)
    {
		if(!abilitiesIn.isEmpty())
		{
			abilitiesIn.sort(ABILITY_SORT);
			for(Ability ability : abilitiesIn)
			{
				if(!ability.displayInSpecies())
					continue;
				this.abilityList.addAbility(ability);
			}
		}
    }
    
    public Species getCurrentSpecies()
    {
		return selectableSpecies.get(index);
    }
    
    public static void drawHealthAndArmour(PoseStack matrix, Font font, int xPos, int yPos, int health, int armour)
    {
		yPos -= 2;
    	int healthX = xPos;
    	int armourX = xPos;
    	
    	if(armour > 0 && health > 0)
    	{
    		healthX -= 20;
    		armourX += 20;
    	}
    	
		if(health > 0)
		{
	    	String healthStr = String.valueOf(health);
			drawCenteredString(matrix, font, healthStr, healthX, yPos, -1);
			
			int heartX = healthX - (font.width(healthStr) / 2) - 2 - font.lineHeight;
			drawHUDIcon(matrix, heartX, yPos, 16, 0);
			drawHUDIcon(matrix, heartX, yPos, 52, 0);
		}
    	
		if(armour > 0)
		{
	    	String armourStr = String.valueOf(armour);
			drawCenteredString(matrix, font, armourStr, armourX, yPos, -1);
			
			int chestX = armourX + (font.width(armourStr) / 2) + 2;
			drawHUDIcon(matrix, chestX, yPos, 34, 9);
		}
    }
    
    public static void drawHUDIcon(PoseStack matrix, int xPos, int yPos, int texX, int texY)
    {
    	yPos -= 1;
		matrix.pushPose();
			RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
			
			float scale = 1F / 256F;
			float xMin = texX * scale;
			float xMax = (texX + 9) * scale;
			float yMin = texY * scale;
			float yMax = (texY + 9) * scale;
			blit(matrix.last().pose(), xPos, xPos + Minecraft.getInstance().font.lineHeight, yPos, yPos + Minecraft.getInstance().font.lineHeight, 0, xMin, xMax, yMin, yMax, 1F, 1F, 1F, 1F);
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
		BufferUploader.drawWithShader(bufferbuilder.end());
		RenderSystem.enableDepthTest();
	}
}
