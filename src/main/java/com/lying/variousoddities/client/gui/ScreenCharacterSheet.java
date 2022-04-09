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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

public class ScreenCharacterSheet extends Screen
{
	private final StringTextComponent speciesHeader;
	private ITextComponent typesHeader;
	private double health, armour;
	private AbilityList listActives, listPassives;
	
	public ScreenCharacterSheet()
	{
		super(new TranslationTextComponent("gui."+Reference.ModInfo.MOD_ID+".character_sheet"));
		
		PlayerEntity player = Minecraft.getInstance().player;
		LivingData data = LivingData.forEntity(player);
		
		this.speciesHeader = new StringTextComponent("");
		
		// Species name and actions
		ITextComponent actionSet = ActionSet.fromTypes(player, EnumCreatureType.getTypes(player).asSet()).translated();
		speciesHeader.append(((IFormattableTextComponent)data.getSpecies().getDisplayName().copyRaw()).modifyStyle((style) -> 
			{
				return style.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, actionSet));
			}));
		
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
	
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		Style tooltipToRender = null;
		
		renderDirtBackground(0);
		if(!this.listActives.isEmpty())
			this.listActives.render(matrixStack, mouseX, mouseY, partialTicks);
		this.listPassives.render(matrixStack, mouseX, mouseY, partialTicks);
		
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
		
		if(!this.listActives.isEmpty())
			drawCenteredString(matrixStack, this.font, new TranslationTextComponent("gui.varodd.character_sheet.actives"), this.listActives.getLeft() + this.listActives.getWidth() / 2, yPos, 16777215);
		
		drawCenteredString(matrixStack, this.font, new TranslationTextComponent("gui.varodd.character_sheet.passives"), this.listPassives.getLeft() + this.listPassives.getWidth() / 2, yPos, 16777215);
		
		yPos += this.font.FONT_HEIGHT;
		this.listActives.setTop(yPos);
		this.listPassives.setTop(yPos);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(tooltipToRender != null)
			renderComponentHoverEffect(matrixStack, tooltipToRender, mouseX, mouseY);
	}
	
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
        
		boolean singleList = actives.isEmpty();
		int listWidth = Math.max((int)(this.width * (actives.isEmpty() ? 0.6D : 0.3D)), 200);
		this.listPassives = new AbilityList(minecraft, singleList ? (this.width - listWidth) / 2 : (this.width / 2) + 2, listWidth, this.height, 20);
		if(!passives.isEmpty())
		{
			passives.sort(ScreenSelectSpecies.ABILITY_SORT);
			this.listPassives.addAbilities(passives);
			this.children.add(this.listPassives);
		}
		
		this.listActives = new AbilityList(minecraft, (this.width / 2) - listWidth - 2, listWidth, this.height, 20);
		if(!actives.isEmpty())
		{
			actives.sort(ScreenSelectSpecies.ABILITY_SORT);
			this.listActives.addAbilities(actives);
			this.children.add(this.listActives);
		}
    }
}
