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
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.Ability.Type;
import com.lying.variousoddities.species.types.Types;
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

public class ScreenSpeciesSelect extends Screen
{
	public static final ResourceLocation ABILITY_ICONS = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/abilities.png");
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
	
	private List<Species> selectables = Lists.newArrayList();
	private int index = 0;
	
	private Button typesButton;
	private Button selectButton;
	private boolean keepTypes = false;
	
	public ScreenSpeciesSelect(PlayerEntity playerIn)
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".species_select"));
		this.player = playerIn;
		
		VORegistries.SPECIES.values().forEach((species) -> { if(species.isPlayerSelectable() || player.isCreative()) selectables.add(species); });
		selectables.sort(new Comparator<Species>()
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
	
	public boolean shouldCloseOnEsc(){ return false; }
	
	public boolean isPauseScreen(){ return true; }
	
	public void tick()
	{
		super.tick();
		LivingData data = LivingData.forEntity(player);
		typesButton.visible = typesButton.active = data.hasCustomTypes();
		
		selectButton.visible = selectButton.active = !selectables.isEmpty();
		if(selectables.isEmpty())
		{
			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID(), null));
			Minecraft.getInstance().displayGuiScreen(null);
		}
	}
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderDirtBackground(0);
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
		
		if(selectables.isEmpty())
			return;
		
		Species currentSpecies = getCurrentSpecies();
		
		int yPos = 55;
		drawCenteredString(matrixStack, this.font, currentSpecies.getDisplayName(), this.width / 2, 60, 16777215);
		
		yPos += this.font.FONT_HEIGHT + 5;
		int power = currentSpecies.getPower();
		int stars = Math.max(Math.abs(power), 1);
		
		// Render stars of appropriate colour for power
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
		
		yPos += this.font.FONT_HEIGHT + 5;
		// Display types
		if(currentSpecies.hasTypes())
			drawCenteredString(matrixStack, this.font, new Types(currentSpecies.getTypes()).toHeader(), this.width / 2, yPos, 16777215);
		
		// Display abilities
		yPos += this.font.FONT_HEIGHT + 2;
		
		int maxWidth = 150;
		int xPos = (this.width - (maxWidth + 2 + this.font.FONT_HEIGHT)) / 2;
		List<Ability> abilities = currentSpecies.getAbilities();
		if(!abilities.isEmpty())
		{
			abilities.sort(ABILITY_SORT);
			for(Ability ability : abilities)
			{
				drawAbilityIcon(matrixStack, xPos, yPos, ability.getType());
				ITextComponent abilityName = ability.getDisplayName();
				for(ITextProperties string : VOHelper.getWrappedText(abilityName, this.font, maxWidth))
				{
					drawString(matrixStack, this.font, string.getString(), xPos + this.font.FONT_HEIGHT + 2, yPos, 16777215);
					yPos += this.font.FONT_HEIGHT;
				}
				yPos += 2;
			}
		}
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}
	
    public void init(Minecraft minecraft, int width, int height)
    {
    	super.init(minecraft, width, height);
        this.buttons.clear();
        
        int midX = width / 2;
        int midY = height / 2;
        
    	this.addButton(new Button(midX + 120, midY - 40, 20, 20, new StringTextComponent(">"), (button) -> 
    		{
    			index = ++index % selectables.size();
    		}));
    	this.addButton(new Button(midX - 120, midY - 40, 20, 20, new StringTextComponent("<"), (button) -> 
    		{
    			index--;
    			if(index < 0)
    				index = selectables.size() - 1;
    		}));
    	this.addButton(selectButton = new Button(midX - 20, midY + 90, 40, 20, new StringTextComponent("Select"), (button) -> 
    		{
    			// Select species
    			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID(), getCurrentSpecies().getRegistryName()));
    			Minecraft.getInstance().displayGuiScreen(null);
    		}));
    	
    	this.addButton(typesButton = new Button(midX - 10, midY + 110, 20, 20, new StringTextComponent(""), (button) ->
    		{
    			keepTypes = !keepTypes;
    		}));
    	
    	this.addButton(new Button(20, 20, 20, 20, new StringTextComponent("X"), (button) -> 
    	{
			PacketHandler.sendToServer(new PacketSpeciesSelected(player.getUniqueID(), null));
			Minecraft.getInstance().displayGuiScreen(null);
    	}));
    }
    
    public Species getCurrentSpecies()
    {
		return selectables.get(index);
    }
    
    public void drawAbilityIcon(MatrixStack matrix, int xPos, int yPos, Ability.Type nature)
    {
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
