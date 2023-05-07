package com.lying.variousoddities.client.gui.screen;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.client.gui.AbilityList;
import com.lying.variousoddities.client.gui.GuiHandler;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.Template;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityBreatheFluid;
import com.lying.variousoddities.species.abilities.AbilityModifier;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.EnumCreatureType.ActionSet;
import com.lying.variousoddities.species.types.Types;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;

public class ScreenCharacterSheet extends Screen
{
	public static final ResourceLocation SHEET_GUI_TEXTURES = new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/gui/character_sheet.png");
	private static final int ACTION_ICON_SIZE = 12;
	private static final int ACTION_ICON_SEP = 4;
	public ResourceLocation healthKey = AbilityRegistry.getClassRegistryKey(AbilityModifierCon.class).location();
	public ResourceLocation armourKey = AbilityRegistry.getClassRegistryKey(AbilityNaturalArmour.class).location();
	
	private final MutableComponent speciesHeader;
	private Component typesHeader;
	private final ActionSet actionSet;
	private final List<TagKey<Fluid>> fluids;
	private double health, armour;
	private AbilityList listActives, listPassives;
	private boolean isDoubleList = false;
	
	public ScreenCharacterSheet()
	{
		super(Component.translatable("gui."+Reference.ModInfo.MOD_ID+".character_sheet"));
		
		Player player = Minecraft.getInstance().player;
		LivingData data = LivingData.getCapability(player);
		
		// Species name and actions
		this.speciesHeader = Component.literal("");
		if(data.hasSpecies())
			speciesHeader.append(((MutableComponent)data.getSpecies().getDisplayName().plainCopy()));
		
		actionSet = ActionSet.fromTypes(player, EnumCreatureType.getTypes(player).asSet());
		fluids = AbilityBreatheFluid.getBreathableFluids(player);
		
		// Templates (if any)
		List<Template> templates = Lists.newArrayList();
		templates.addAll(data.getTemplates());
		if(!templates.isEmpty())
		{
			speciesHeader.append(Component.literal(" ("));
			speciesHeader.append(Component.translatable("gui.varodd.character_sheet.templates", String.valueOf(templates.size())).withStyle((style) -> 
				{
					MutableComponent templateList = null;
					for(int i=0; i<templates.size(); i++)
					{
						MutableComponent name = (MutableComponent)templates.get(i).getDisplayName().copy();
						if(templateList == null)
							templateList = name;
						else
						{
							templateList.append("\n");
							templateList.append(templates.get(i).getDisplayName());
						}
					}
					return style.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, templateList));
				}));
			speciesHeader.append(Component.literal(")"));
		}
	}
	
	public boolean isPauseScreen(){ return false; }
	
    public void init()
    {
        this.clearWidgets();
		
		Player player = Minecraft.getInstance().player;
		
		Types types = EnumCreatureType.getTypes(player);
		this.typesHeader = types.toHeader();
		this.health = types.getPlayerHealth();
		this.armour = 0D;
		
		List<Ability> passives = Lists.newArrayList();
		List<Ability> actives = Lists.newArrayList();
		for(Ability ability : AbilityRegistry.getCreatureAbilities(player).values())
		{
			if(ability.getRegistryName().equals(armourKey))
				armour += ((AbilityNaturalArmour)ability).amount(); 
			
			if(ability.getRegistryName().equals(healthKey))
				health += ((AbilityModifier)ability).amount();
			
			if(ability.passive())
				passives.add(ability);
			else
				actives.add(ability);
		};
        
		this.isDoubleList = !actives.isEmpty();
		int listWidth = Mth.clamp((int)(this.width * (actives.isEmpty() ? 0.6D : 0.3D)), 200, 250);
		int listSep = (ACTION_ICON_SIZE + ACTION_ICON_SEP * 2) / 2;
		this.listPassives = new AbilityList(minecraft, isDoubleList ? (this.width / 2) + listSep : (this.width - listWidth) / 2, listWidth, this.height, 20);
		if(!passives.isEmpty())
		{
			passives.sort(ScreenSelectSpecies.ABILITY_SORT);
			this.listPassives.addAbilities(passives);
			addWidget(this.listPassives);
		}
		
		this.listActives = new AbilityList(minecraft, (this.width / 2) - listWidth - listSep, listWidth, this.height, 20);
		if(this.isDoubleList)
		{
			actives.sort(ScreenSelectSpecies.ABILITY_SORT);
			this.listActives.addAbilities(actives);
			addWidget(this.listActives);
		}
    }
	
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		Style tooltipToRender = null;
		
		renderDirtBackground(0);
		renderBg(matrixStack, this.isDoubleList);
		if(this.isDoubleList)
			this.listActives.render(matrixStack, mouseX, mouseY, partialTicks);
		this.listPassives.render(matrixStack, mouseX, mouseY, partialTicks);
		renderLabels(matrixStack, this.isDoubleList);
		
		RenderSystem.setShaderTexture(0, SHEET_GUI_TEXTURES);
		this.blit(matrixStack, (this.width - 160) / 2, 16, 0, 212, 160, 40);
		
		int yPos = 2;
		drawCenteredString(matrixStack, this.font, this.title, this.width / 2, yPos, 16777215);
		yPos += 17;
		drawCenteredString(matrixStack, this.font, this.speciesHeader, this.width / 2, yPos, 16777215);
		int speciesWidth = (int)this.font.getSplitter().stringWidth(this.speciesHeader);
		if(mouseY >= yPos && mouseY <= (yPos + 9))
			if(mouseX <= (this.width + speciesWidth) / 2 && mouseX >= (this.width - speciesWidth) / 2)
			{
				Style style = this.minecraft.font.getSplitter().componentStyleAtWidth(this.speciesHeader, mouseX - ((this.width - speciesWidth) / 2));
				if(style != null)
					tooltipToRender = style;
			}
		
		yPos += 14;
		
		drawCenteredString(matrixStack, this.font, typesHeader, this.width / 2, yPos, -1);
		int typesWidth = (int)this.font.getSplitter().stringWidth(typesHeader);
		if(mouseY >= yPos && mouseY <= (yPos + 9))
			if(mouseX <= (this.width + typesWidth) / 2 && mouseX >= (this.width - typesWidth) / 2)
			{
				Style style = this.minecraft.font.getSplitter().componentStyleAtWidth(this.typesHeader, mouseX - ((this.width - typesWidth) / 2));
				if(style != null)
					tooltipToRender = style;
			}
		
		yPos += 12;
		ScreenSelectSpecies.drawHealthAndArmour(matrixStack, this.font, this.width / 2, yPos + 2, (int)health, (int)armour);
		
		this.listActives.setTop(67);
		this.listPassives.setTop(67);
		if(this.isDoubleList)
		{
			Component activesTitle = Component.translatable("gui.varodd.character_sheet.actives");
			this.font.draw(matrixStack, activesTitle.getString(), this.listActives.getLeft() + this.listActives.getWidth() / 2 - this.font.width(activesTitle.getString()) / 2, this.listActives.getTop() - 1 - this.font.lineHeight, 4210752);
		}
		Component passivesTitle = Component.translatable("gui.varodd.character_sheet.passives");
		this.font.draw(matrixStack, passivesTitle.getString(), this.listPassives.getLeft() + this.listPassives.getWidth() / 2 - this.font.width(passivesTitle.getString()) / 2, this.listPassives.getTop() - 1 - this.font.lineHeight, 4210752);
		
		renderActionSet(matrixStack, mouseX, mouseY);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(tooltipToRender != null)
			renderComponentHoverEffect(matrixStack, tooltipToRender, mouseX, mouseY);
	}
	
	private void renderBg(PoseStack matrixStack, boolean isDouble)
	{
		// Fill
		TextureArea area = getTextureArea();
		renderFill(matrixStack, area.xMin + 6, area.yMin + 6, area.width() - 12, area.height() - 12);
	}
	
	private void renderOuterEdges(PoseStack matrixStack, boolean isDouble)
	{
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, SHEET_GUI_TEXTURES);
		
		TextureArea area = getTextureArea();
		
		// Corners
		this.blit(matrixStack, area.xMin, area.yMin, 0, 0, 6, 6);
		this.blit(matrixStack, area.xMax - 6, area.yMin, 206, 0, 6, 6);
		this.blit(matrixStack, area.xMin, area.yMax - 6, 0, 206, 6, 6);
		this.blit(matrixStack, area.xMax - 6, area.yMax - 6, 206, 206, 6, 6);
		
		// Edges
		int width = area.width() - 12;
		int height = area.height() - 12;
		Screen.blit(matrixStack, area.xMin, area.yMin + 6, 6, height, 0, 6, 6, 200, 512, 512);
		Screen.blit(matrixStack, area.xMax - 6, area.yMin + 6, 6, height, 206, 6, 6, 200, 512, 512);
		Screen.blit(matrixStack, area.xMin + 6, area.yMin, width, 6, 6, 0, 200, 6, 512, 512);
		Screen.blit(matrixStack, area.xMin + 6, area.yMax - 6, width, 6, 6, 206, 200, 6, 512, 512);
	}
	
	private void renderFill(PoseStack matrixStack, int startX, int startY, int width, int height)
	{
		RenderSystem.setShaderTexture(0, SHEET_GUI_TEXTURES);
		Screen.blit(matrixStack, startX, startY, width, height, 6, 6, Math.min(200, width), Math.min(200, height), 512, 512);
	}
	
	private void renderLabels(PoseStack matrixStack, boolean isDouble)
	{
		renderOuterEdges(matrixStack, isDouble);
		
		hideList(matrixStack, this.listPassives);
		if(isDouble)
			hideList(matrixStack, this.listActives);
	}
	
	private void hideList(PoseStack matrixStack, AbilityList list)
	{
		RenderSystem.setShaderTexture(0, SHEET_GUI_TEXTURES);
		
		int xMin = list.getLeft() - 6;
		int xMax = xMin + 6 + list.getWidth();
		int yMin = list.getTop() - 6;
		int yMax = list.getBottom() - 6;
		
		int width = xMax - (xMin + 6);
		int height = yMax - (yMin + 6);
		
		// Fill
		TextureArea area = getTextureArea();
		renderFill(matrixStack, xMin, area.yMin + 6, list.getWidth() + 12, Math.abs(area.yMin + 6 - yMin));
		renderFill(matrixStack, xMin, yMax + 6, list.getWidth() + 12, area.yMax - 6 - yMax);
		renderOuterEdges(matrixStack, isDoubleList);
		
		// Corners
		this.blit(matrixStack, xMin, yMin, 0, 252, 6, 6);
		this.blit(matrixStack, xMax, yMin, 26, 252, 6, 6);
		this.blit(matrixStack, xMin, yMax, 0, 278, 6, 6);
		this.blit(matrixStack, xMax, yMax, 26, 278, 6, 6);
		
		// Edges
		Screen.blit(matrixStack, xMin, yMin + 6, 6, height, 0, 258, 6, 20, 512, 512);
		Screen.blit(matrixStack, xMax, yMin + 6, 6, height, 26, 258, 6, 20, 512, 512);
		Screen.blit(matrixStack, xMin + 6, yMin, width, 6, 6, 252, 20, 6, 512, 512);
		Screen.blit(matrixStack, xMin + 6, yMax, width, 6, 6, 278, 20, 6, 512, 512);
	}
	
	public void blit(PoseStack matrixStack, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight)
	{
		blit(matrixStack, x, y, getBlitOffset(), (float)uOffset, (float)vOffset, uWidth, vHeight, 512, 512);
	}
	
	private void renderActionSet(PoseStack matrixStack, int mouseX, int mouseY)
	{
		EnumCreatureType.Action hovered = null;
		int count = EnumCreatureType.Action.values().length;
		int iconX = this.listPassives.getLeft() - ACTION_ICON_SEP - ACTION_ICON_SIZE;
		int iconY = (this.height - (count * ACTION_ICON_SIZE + count - 1 * ACTION_ICON_SEP)) / 2;
		
		RenderSystem.setShaderTexture(0, SHEET_GUI_TEXTURES);
		int barY = iconY - (16 - ACTION_ICON_SIZE) / 2;
		int barX = iconX - (16 - ACTION_ICON_SIZE) / 2;
		this.blit(matrixStack, barX, barY - 8, 212, 16, 16, 8);
		for(int i=0; i<count; i++)
			this.blit(matrixStack, barX, barY + (16 * i), 212, 0, 16, 16);
		this.blit(matrixStack, barX, barY + (count * 16), 212, 24, 16, 8);
		
		for(EnumCreatureType.Action action : EnumCreatureType.Action.values())
		{
			if(renderAction(matrixStack, iconX, iconY, mouseX, mouseY, action, this.actionSet.contains(action)))
				hovered = action;
			
			iconY += ACTION_ICON_SIZE + ACTION_ICON_SEP;
		}
		
		if(hovered != null && hovered.translated() != null)
		{
			List<Component> tooltip = Lists.newArrayList();
			String translated = hovered.translated().getString().toLowerCase();
			if(this.actionSet.contains(hovered))
			{
				tooltip.add(Component.translatable("enum.varodd.type_action.does", translated));
				
				if(hovered == EnumCreatureType.Action.BREATHES)
					this.fluids.forEach((fluid) -> 
					{
						String name = "air";
						if(fluid != null && fluid.location() != null)
							name = fluid.location().getPath();
						tooltip.add(Component.literal(" "+name));
					});
			}
			else
				tooltip.add(Component.translatable("enum.varodd.type_action.doesnt", translated));
			
			renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY, this.font);
		}
	}
	
	/** Renders the given action on the screen, returning true if the mouse is hovered over it */
	private boolean renderAction(PoseStack matrixStack, int x, int y, int mouseX, int mouseY, EnumCreatureType.Action action, boolean present)
	{
		float colR = present ? 0F : 1F;
		float colG = present ? 1F : 0F;
		GuiHandler.drawIconAt(matrixStack, x, y, action.index(), 2, ACTION_ICON_SIZE, ACTION_ICON_SIZE, colR, colG, 0F, 1F);
		return mouseX >= x && mouseX <= x + ACTION_ICON_SIZE && mouseY >= y && mouseY <= y + ACTION_ICON_SIZE;
	}
	
	private TextureArea getTextureArea()
	{
		int passivesRight = this.listPassives.getLeft() + this.listPassives.getWidth();
		int sizeX = (isDoubleList ? passivesRight - this.listActives.getLeft() : this.listPassives.getWidth()) + 10;
		
		int startX = (this.width - sizeX) / 2;
		if(!isDoubleList)
		{
			startX = this.listPassives.getLeft() - ACTION_ICON_SEP - ACTION_ICON_SIZE - 5;
			passivesRight = ((this.width + this.listPassives.getWidth()) / 2) + 5;
			sizeX = passivesRight - startX - 5;
		}
		
		int startY = this.listPassives.getTop() - this.font.lineHeight - 10;
		int sizeY = this.height - 5 - startY;
		
		int xMin = startX - 6;
		int xMax = this.listPassives.getLeft() + this.listPassives.getWidth() + 10;
		int yMin = startY;
		int yMax = startY + sizeY;
		
		return new TextureArea(xMin, yMin, xMax, yMax);
	}
	
	private static class TextureArea
	{
		public final int xMin, xMax;
		public final int yMin, yMax;
		
		public TextureArea(int a, int b, int c, int d)
		{
			xMin = a;
			xMax = c;
			yMin = b;
			yMax = d;
		}
		
		public int width() { return xMax - xMin; }
		public int height() { return yMax - yMin; }
	}
}
