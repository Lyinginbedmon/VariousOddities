package com.lying.variousoddities.client.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketSpeciesSelected;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Species;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.Ability.Type;
import com.lying.variousoddities.species.abilities.AbilityModifier;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.utility.VOHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ScreenSelectSpecies extends Screen
{
	public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/species_select.png");
	public static final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
	public static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
	
	private static final Comparator<Ability> ABILITY_SORT = new Comparator<Ability>()
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
	
	private final PlayerEntity player;
	
	private List<Species> selectableSpecies = Lists.newArrayList();
	private List<Template> selectableTemplates = Lists.newArrayList();
	private int index = 0;
	
	private Button typesButton;
	private Button selectButton;
	private boolean keepTypes = false;
	
	// TODO Replace arbitrary power value with server variable
	private int targetPower = 6;
	
	public ScreenSelectSpecies(PlayerEntity playerIn)
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".species_select"));
		this.player = playerIn;
		initSpecies();
		initTemplates();
	}
	
	private void initSpecies()
	{
		if(player.isCreative())
			selectableSpecies.addAll(VORegistries.SPECIES.values());
		else
			VORegistries.SPECIES.values().forEach((species) -> { if(species.getPower() <= targetPower) selectableSpecies.add(species); });
		
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
	
	private void initTemplates()
	{
		if(player.isCreative())
			selectableTemplates.addAll(VORegistries.TEMPLATES.values());
		else
			VORegistries.TEMPLATES.values().forEach((species) -> { if(species.getPower() <= targetPower) selectableTemplates.add(species); });
		
		selectableTemplates.sort(new Comparator<Template>()
			{
				public int compare(Template o1, Template o2)
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
	
	public boolean shouldCloseOnEsc(){ return false; }
	
	public boolean isPauseScreen(){ return true; }
	
	public void tick()
	{
		super.tick();
		LivingData data = LivingData.forEntity(player);
		typesButton.visible = typesButton.active = data.hasCustomTypes();
		
		selectButton.visible = selectButton.active = !selectableSpecies.isEmpty();
		if(selectableSpecies.isEmpty())
		{
			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID()));
			Minecraft.getInstance().displayGuiScreen(null);
		}
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		renderBackground(matrixStack);
		renderBackgroundLayer(matrixStack, partialTicks);
		int yPos = 20;
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, yPos, 16777215);
		yPos += 15;
		
		if(selectableSpecies.isEmpty())
			return;
		
		// Draw species display name
		Species currentSpecies = getCurrentSpecies();
		this.selectButton.setMessage(currentSpecies.getDisplayName());
		yPos += this.font.FONT_HEIGHT + 12;
		
		// Render stars of appropriate colour for power
		drawStars(matrixStack, yPos, currentSpecies.getPower());
		yPos += this.font.FONT_HEIGHT + 3;
		
		// Display types
		int health = 20;
		if(currentSpecies.hasTypes())
		{
			ITextComponent typesHeader = currentSpecies.getTypes().toHeader();
			drawCenteredString(matrixStack, this.font, typesHeader, this.width / 2, yPos, -1);
			health = (int)currentSpecies.getTypes().getPlayerHealth();
		}
		
		// Health and armour
		yPos += this.font.FONT_HEIGHT + 3;
		
		double armour = 0;
		List<Ability> abilities = currentSpecies.getFullAbilities();
		if(!abilities.isEmpty())
			for(Ability ability : abilities) 
			{
				if(ability.getRegistryName().equals(AbilityNaturalArmour.REGISTRY_NAME))
					armour += ((AbilityNaturalArmour)ability).amount(); 
				
				if(ability.getRegistryName().equals(AbilityModifierCon.REGISTRY_NAME))
					health += ((AbilityModifier)ability).amount();
			};
		
		drawHealthAndArmour(matrixStack, yPos, health, (int)armour);
		
		// Display abilities
		yPos += this.font.FONT_HEIGHT + 5;
		drawAbilities(matrixStack, abilities, yPos);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
	@SuppressWarnings("deprecation")
	public void renderBackgroundLayer(MatrixStack matrixStack, float partialTicks)
	{
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
		
		int sizeX = 175;
		int sizeY = 180;
		this.blit(matrixStack, (this.width - sizeX) / 2, 45, 0, 0, sizeX, sizeY);
	}
	
	public void drawStars(MatrixStack matrixStack, int yPos, int power)
	{
		int stars = Math.max(Math.abs(power), 1);
		
		int midX = this.width / 2;
		int startX = midX - (this.font.FONT_HEIGHT * stars) / 2;
		int endX = startX + this.font.FONT_HEIGHT;
		
		float red = power > 0 ? 0F : power < 0 ? 1F : 0.5F;
		float green = power > 0 ? 1F : power < 0 ? 0F : 0.5F;
		float blue = power > 0 ? 0F : power < 0 ? 0F : 0.5F;
		
		for(int i=0; i<stars; i++)
		{
			matrixStack.push();
				Minecraft.getInstance().getTextureManager().bindTexture(ABILITY_ICONS);
				blit(matrixStack.getLast().getMatrix(), startX, (int)endX, yPos, yPos + this.font.FONT_HEIGHT, 0, 0, ICON_TEX, ICON_TEX, ICON_TEX * 2, red, green, blue, 1F);
			matrixStack.pop();
			
			startX += this.font.FONT_HEIGHT;
			endX += this.font.FONT_HEIGHT;
		}
	}
	
    public void init(Minecraft minecraft, int width, int height)
    {
    	super.init(minecraft, width, height);
        this.buttons.clear();
        
        int midX = width / 2;
        
    	this.addButton(new Button(midX + 100, 120, 20, 20, new StringTextComponent(">"), (button) -> 
    		{
    			index = ++index % selectableSpecies.size();
    		}));
    	
    	this.addButton(new Button(midX - 120, 120, 20, 20, new StringTextComponent("<"), (button) -> 
    		{
    			index--;
    			if(index < 0)
    				index = selectableSpecies.size() - 1;
    		}));
    	
    	this.addButton(selectButton = new Button(midX - 50, 35, 100, 20, new StringTextComponent("Select"), (button) -> 
    		{
    			// Select species
    			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID(), getCurrentSpecies().getRegistryName(), this.keepTypes));
    			Minecraft.getInstance().displayGuiScreen(null);
    		}));
    	
    	this.addButton(typesButton = new Button(midX - 60, height - 25, 120, 20, new TranslationTextComponent("gui.varodd.species_select.lose_types"), (button) ->
    		{
    			keepTypes = !keepTypes;
    			typesButton.setMessage(new TranslationTextComponent("gui.varodd.species_select."+(keepTypes ? "keep_types" : "lose_types")));
    		}, (button,matrix,x,y) -> { renderTooltip(matrix, new TranslationTextComponent("gui.varodd.species_select.keep_types.info"), x, y); }));
    	
    	this.addButton(new Button(midX + 100, 35, 20, 20, new StringTextComponent("X"), (button) -> 
    	{
			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID()));
			Minecraft.getInstance().displayGuiScreen(null);
    	}, (button,matrix,x,y) -> { renderTooltip(matrix, new TranslationTextComponent("gui.varodd.species_select.exit"), x, y); }));
    }
    
    public Species getCurrentSpecies()
    {
		return selectableSpecies.get(index);
    }
    
    public void drawHealthAndArmour(MatrixStack matrix, int yPos, int health, int armour)
    {
		int xPos = this.width / 2;
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
			drawCenteredString(matrix, this.font, healthStr, healthX, yPos, -1);
			
			int heartX = healthX - (this.font.getStringWidth(healthStr) / 2) - 2 - this.font.FONT_HEIGHT;
			drawHUDIcon(matrix, heartX, yPos, 16, 0);
			drawHUDIcon(matrix, heartX, yPos, 52, 0);
		}
    	
		if(armour > 0)
		{
	    	String armourStr = String.valueOf(armour);
			drawCenteredString(matrix, this.font, armourStr, armourX, yPos, -1);
			
			int chestX = armourX + (this.font.getStringWidth(armourStr) / 2) + 2;
			drawHUDIcon(matrix, chestX, yPos, 34, 9);
		}
    }
    
    public void drawHUDIcon(MatrixStack matrix, int xPos, int yPos, int texX, int texY)
    {
    	yPos -= 1;
		matrix.push();
			Minecraft.getInstance().getTextureManager().bindTexture(GUI_ICONS_LOCATION);
			
			float scale = 1F / 256F;
			float xMin = texX * scale;
			float xMax = (texX + 9) * scale;
			float yMin = texY * scale;
			float yMax = (texY + 9) * scale;
			blit(matrix.getLast().getMatrix(), xPos, xPos + this.font.FONT_HEIGHT, yPos, yPos + this.font.FONT_HEIGHT, 0, xMin, xMax, yMin, yMax, 1F, 1F, 1F, 1F);
		matrix.pop();
    }
    
    public void drawAbilities(MatrixStack matrixStack, List<Ability> abilities, int yPos)
    {
		int maxWidth = 150;
		int xPos = (this.width - (maxWidth + 2 + this.font.FONT_HEIGHT)) / 2;
		if(!abilities.isEmpty())
		{
			abilities.sort(ABILITY_SORT);
			for(Ability ability : abilities)
			{
				if(!ability.displayInSpecies())
					continue;
				
				drawAbilityIcon(matrixStack, xPos, yPos, ability.getType());
				ITextComponent abilityName = ability.getDisplayName();
				for(ITextProperties string : VOHelper.getWrappedText(abilityName, this.font, maxWidth))
				{
					this.font.drawString(matrixStack, string.getString(), (float)(xPos + this.font.FONT_HEIGHT + 2), (float)yPos, -1);
					yPos += this.font.FONT_HEIGHT;
				}
				yPos += 2;
			}
		}
    }
    
    public void drawAbilityIcon(MatrixStack matrix, int xPos, int yPos, Ability.Type nature)
    {
    	yPos -= 1;
    	
		float texXMin = ICON_TEX * (float)nature.texIndex;
		float texXMax = ICON_TEX + texXMin;
		
		float texYMin = ICON_TEX * 0F;
		float texYMax = ICON_TEX + texYMin;
		
		// Screen co-ordinates
		double endX = xPos + this.font.FONT_HEIGHT;
		double endY = yPos + this.font.FONT_HEIGHT;
		
		matrix.push();
			Minecraft.getInstance().getTextureManager().bindTexture(ABILITY_ICONS);
			blit(matrix.getLast().getMatrix(), xPos, (int)endX, yPos, (int)endY, 0, texXMin, texXMax, texYMin, texYMax, 1F, 1F, 1F, 1F);
			blit(matrix.getLast().getMatrix(), xPos, (int)endX, yPos, (int)endY, 0, ICON_TEX * 2, ICON_TEX * 3, ICON_TEX * 1, ICON_TEX * 2, 1F, 1F, 1F, 1F);
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
