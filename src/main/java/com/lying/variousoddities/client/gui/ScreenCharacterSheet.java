package com.lying.variousoddities.client.gui;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityModifier;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.species.types.Types;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

public class ScreenCharacterSheet extends Screen
{
	private static final ResourceLocation SHEET_GUI_TEXTURES = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/character_sheet.png");
	private static final int ACTION_ICON_SIZE = 12;
	private static final int ACTION_ICON_SEP = 4;
	
	private final StringTextComponent speciesHeader;
	private ITextComponent typesHeader;
	private final ActionSet actionSet;
	private final List<ITag.INamedTag<Fluid>> fluids;
	private double health, armour;
	private AbilityList listActives, listPassives;
	private boolean isDoubleList = false;
	
	public ScreenCharacterSheet()
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".character_sheet"));
		
		PlayerEntity player = Minecraft.getInstance().player;
		LivingData data = LivingData.forEntity(player);
		
		this.speciesHeader = new StringTextComponent("");
		
		// Species name and actions
		actionSet = ActionSet.fromTypes(player, EnumCreatureType.getTypes(player).asSet());
		fluids = data.getBreathableFluids(player);
		
		speciesHeader.append(((IFormattableTextComponent)data.getSpecies().getDisplayName().copyRaw()));
		
		// Templates (if any)
		List<Template> templates = Lists.newArrayList();
		templates.addAll(data.getTemplates());
		if(!templates.isEmpty())
		{
			speciesHeader.append(new StringTextComponent(" ("));
			speciesHeader.append(new TranslationTextComponent("gui.varodd.character_sheet.templates", String.valueOf(templates.size())).modifyStyle((style) -> 
				{
					IFormattableTextComponent templateList = null;
					for(int i=0; i<templates.size(); i++)
					{
						IFormattableTextComponent name = (IFormattableTextComponent)templates.get(i).getDisplayName().deepCopy();
						if(templateList == null)
							templateList = name;
						else
						{
							templateList.appendString("\n");
							templateList.append(templates.get(i).getDisplayName());
						}
					}
					return style.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, templateList));
				}));
			speciesHeader.append(new StringTextComponent(")"));
		}
	}
	
	public boolean isPauseScreen(){ return false; }
	
    public void init(Minecraft minecraft, int width, int height)
    {
    	super.init(minecraft, width, height);
        this.buttons.clear();
		
		PlayerEntity player = Minecraft.getInstance().player;
		
		Types types = EnumCreatureType.getTypes(player);
		this.typesHeader = types.toHeader();
		this.health = types.getPlayerHealth();
		this.armour = 0D;
		
		List<Ability> passives = Lists.newArrayList();
		List<Ability> actives = Lists.newArrayList();
		for(Ability ability : AbilityRegistry.getCreatureAbilities(player).values())
		{
			if(ability.getRegistryName().equals(AbilityNaturalArmour.REGISTRY_NAME))
				armour += ((AbilityNaturalArmour)ability).amount(); 
			
			if(ability.getRegistryName().equals(AbilityModifierCon.REGISTRY_NAME))
				health += ((AbilityModifier)ability).amount();
			
			if(ability.passive())
				passives.add(ability);
			else
				actives.add(ability);
		};
        
		this.isDoubleList = !actives.isEmpty();
		int listWidth = MathHelper.clamp((int)(this.width * (actives.isEmpty() ? 0.6D : 0.3D)), 200, 250);
		int listSep = (ACTION_ICON_SIZE + ACTION_ICON_SEP * 2) / 2;
		this.listPassives = new AbilityList(minecraft, isDoubleList ? (this.width / 2) + listSep : (this.width - listWidth) / 2, listWidth, this.height, 20);
		if(!passives.isEmpty())
		{
			passives.sort(ScreenSelectSpecies.ABILITY_SORT);
			this.listPassives.addAbilities(passives);
			this.children.add(this.listPassives);
		}
		
		this.listActives = new AbilityList(minecraft, (this.width / 2) - listWidth - listSep, listWidth, this.height, 20);
		if(this.isDoubleList)
		{
			actives.sort(ScreenSelectSpecies.ABILITY_SORT);
			this.listActives.addAbilities(actives);
			this.children.add(this.listActives);
		}
    }
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		Style tooltipToRender = null;
		
		renderDirtBackground(0);
		renderBackgroundTexture(matrixStack, this.isDoubleList);
		if(this.isDoubleList)
			this.listActives.render(matrixStack, mouseX, mouseY, partialTicks);
		this.listPassives.render(matrixStack, mouseX, mouseY, partialTicks);
		renderForegroundTexture(matrixStack, this.isDoubleList);
		
		int yPos = 2;
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, yPos, 16777215);
		yPos += 17;
		drawCenteredString(matrixStack, this.font, this.speciesHeader, this.width / 2, yPos, 16777215);
		int speciesWidth = this.font.getStringPropertyWidth(this.speciesHeader);
		if(mouseY >= yPos && mouseY <= (yPos + 9))
			if(mouseX <= (this.width + speciesWidth) / 2 && mouseX >= (this.width - speciesWidth) / 2)
			{
				Style style = this.minecraft.fontRenderer.getCharacterManager().func_238357_a_(this.speciesHeader, mouseX - ((this.width - speciesWidth) / 2));
				if(style != null)
					tooltipToRender = style;
			}
		
		yPos += 14;
		
		drawCenteredString(matrixStack, this.font, typesHeader, this.width / 2, yPos, -1);
		int typesWidth = this.font.getStringPropertyWidth(typesHeader);
		if(mouseY >= yPos && mouseY <= (yPos + 9))
			if(mouseX <= (this.width + typesWidth) / 2 && mouseX >= (this.width - typesWidth) / 2)
			{
				Style style = this.minecraft.fontRenderer.getCharacterManager().func_238357_a_(this.typesHeader, mouseX - ((this.width - typesWidth) / 2));
				if(style != null)
					tooltipToRender = style;
			}
		
		yPos += 12;
		ScreenSelectSpecies.drawHealthAndArmour(matrixStack, this.font, this.width / 2, yPos + 2, (int)health, (int)armour);
		yPos += this.font.FONT_HEIGHT + 2;
		
		if(this.isDoubleList)
		{
			drawCenteredString(matrixStack, this.font, new TranslationTextComponent("gui.varodd.character_sheet.actives"), this.listActives.getLeft() + this.listActives.getWidth() / 2, yPos, 16777215);
			drawCenteredString(matrixStack, this.font, new TranslationTextComponent("gui.varodd.character_sheet.passives"), this.listPassives.getLeft() + this.listPassives.getWidth() / 2, yPos, 16777215);
		}
		
		this.listActives.setTop(65);
		this.listPassives.setTop(65);
		
		renderActionSet(matrixStack, mouseX, mouseY);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(tooltipToRender != null)
			renderComponentHoverEffect(matrixStack, tooltipToRender, mouseX, mouseY);
	}
	
	@SuppressWarnings("deprecation")
	private void renderBackgroundTexture(MatrixStack matrixStack, boolean isDouble)
	{
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		this.minecraft.getTextureManager().bindTexture(SHEET_GUI_TEXTURES);
		
		int passivesRight = this.listPassives.getLeft() + this.listPassives.getWidth();
		int sizeX = (isDouble ? passivesRight - this.listActives.getLeft() : this.listPassives.getWidth()) + 10;
		
		int startX = (this.width - sizeX) / 2;
		if(!isDouble)
		{
			startX = this.listPassives.getLeft() - ACTION_ICON_SEP - ACTION_ICON_SIZE - 5;
			passivesRight = ((this.width + this.listPassives.getWidth()) / 2) + 5;
			sizeX = passivesRight - startX;
		}
		
		int startY = 13;
		int sizeY = this.height - 5 - startY;
		
		int segWidth = Math.min(240, sizeX / 2);
		// Top Left
		this.blit(matrixStack, startX, startY, 0, 0, segWidth, sizeY);
		
		// Top Right
		// Texture min X offset to ensure far right of segment is displayed
		this.blit(matrixStack, startX + segWidth, startY, 240 + (240 - segWidth), 0, segWidth, sizeY);
		
		// Bottom Left
		// Bottom Right
		// Header bar
		
//		this.blit(matrixStack, startX, startY, 0, 0, sizeX, sizeY);
	}
	
	private void renderForegroundTexture(MatrixStack matrixStack, boolean isDouble)
	{
		// FIXME Render edge overlays of lists
	}
	
	public void blit(MatrixStack matrixStack, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight)
	{
		blit(matrixStack, x, y, getBlitOffset(), (float)uOffset, (float)vOffset, uWidth, vHeight, 512, 512);
	}
	
	private void renderActionSet(MatrixStack matrixStack, int mouseX, int mouseY)
	{
		EnumCreatureType.Action hovered = null;
		int count = EnumCreatureType.Action.values().length;
		int iconX = this.listPassives.getLeft() - ACTION_ICON_SEP - ACTION_ICON_SIZE;
		int iconY = (this.height - (count * ACTION_ICON_SIZE + count - 1 * ACTION_ICON_SEP)) / 2;
		for(EnumCreatureType.Action action : EnumCreatureType.Action.values())
		{
			if(renderAction(matrixStack, iconX, iconY, mouseX, mouseY, action, this.actionSet.contains(action)))
				hovered = action;
			
			iconY += ACTION_ICON_SIZE + ACTION_ICON_SEP;
		}
		
		if(hovered != null)
		{
			List<ITextProperties> tooltip = Lists.newArrayList();
			String translated = hovered.translated().getString().toLowerCase();
			if(this.actionSet.contains(hovered))
			{
				tooltip.add(new TranslationTextComponent("enum.varodd.type_action.does", translated));
				
				if(hovered == EnumCreatureType.Action.BREATHES)
					this.fluids.forEach((fluid) -> 
					{
						String name = "air";
						if(fluid != null)
							name = fluid.getName().getPath();
						tooltip.add(new StringTextComponent(" "+name));
					});
			}
			else
				tooltip.add(new TranslationTextComponent("enum.varodd.type_action.doesnt", translated));
			
			renderWrappedToolTip(matrixStack, tooltip, mouseX, mouseY, this.font);
		}
	}
	
	/** Renders the given action on the screen, returning true if the mouse is hovered over it */
	private boolean renderAction(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, EnumCreatureType.Action action, boolean present)
	{
		float colR = present ? 0F : 1F;
		float colG = present ? 1F : 0F;
		GuiHandler.drawIconAt(matrixStack, x, y, action.index(), 2, ACTION_ICON_SIZE, ACTION_ICON_SIZE, colR, colG, 0F, 1F);
		return mouseX >= x && mouseX <= x + ACTION_ICON_SIZE && mouseY >= y && mouseY <= y + ACTION_ICON_SIZE;
	}
}
