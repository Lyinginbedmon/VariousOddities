package com.lying.variousoddities.magic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum EnumSpellProperty implements StringRepresentable
{
	ABJURE		(DyeColor.BLUE, 8),
	ARROW		(DyeColor.BLUE, 5),
	CHAOS		(DyeColor.BLUE, 9),
	DARKNESS	(DyeColor.BLUE, 0),
	EMPOWER		(DyeColor.BLUE, 6),
	ICE			(DyeColor.BLUE, 1),
	RAY			(DyeColor.BLUE, 7),
	TRICK		(DyeColor.BLUE, 4),
	VAPOR		(DyeColor.BLUE, 3),
	WATER		(DyeColor.BLUE, 2),
	
	ACID		(DyeColor.GREEN, 1),
	CHANGE		(DyeColor.GREEN, 4),
	DEATH		(DyeColor.GREEN, 7),
	EARTH		(DyeColor.GREEN, 2),
	OBJECT		(DyeColor.GREEN, 8),
	ORDER		(DyeColor.GREEN, 9),
	SELF		(DyeColor.GREEN, 5),
	SUSTAIN		(DyeColor.GREEN, 0),
	TIME		(DyeColor.GREEN, 3),
	UNDEAD		(DyeColor.GREEN, 6),
	
	AREA		(DyeColor.RED, 5),
	FIRE		(DyeColor.RED, 2),
	FORCE		(DyeColor.RED, 3),
	HARM		(DyeColor.RED, 4),
	LIGHT		(DyeColor.RED, 0),
	LIGHTNING	(DyeColor.RED, 1),
	LINE		(DyeColor.RED, 7),
	MOTION		(DyeColor.RED, 8),
	PURIFY		(DyeColor.RED, 6),
	UNHOLY		(DyeColor.RED, 9),
	
	AIR			(DyeColor.WHITE, 2),
	CONDEMN		(DyeColor.WHITE, 8),
	CREATION	(DyeColor.WHITE, 6),
	DISTANT		(DyeColor.WHITE, 7),
	HEAL		(DyeColor.WHITE, 0),
	HOLY		(DyeColor.WHITE, 9),
	SOUND		(DyeColor.WHITE, 3),
	SPACE		(DyeColor.WHITE, 1),
	TARGET		(DyeColor.WHITE, 5),
	VISION		(DyeColor.WHITE, 4);
	
	public static final int PER_COLOUR = 10;
	
	private static final Comparator<EnumSpellProperty> byOrder = new Comparator<EnumSpellProperty>()
			{
				public int compare(EnumSpellProperty o1, EnumSpellProperty o2)
				{
					return o1.order > o2.order ? 1 : o1.order < o2.order ? -1 : 0;
				}
			};
	
	private final DyeColor inkColor;
	private final int order;
	
	private EnumSpellProperty(DyeColor inkColorIn, int orderIn)
	{
		inkColor = inkColorIn;
		order = orderIn;
	}
	
	public DyeColor getColor(){ return this.inkColor; }
	
	public static EnumSpellProperty[] getPropertiesByColor(DyeColor colorIn)
	{
		List<EnumSpellProperty> properties = new ArrayList<>();
		for(EnumSpellProperty property : values())
			if(property.inkColor == colorIn)
				properties.add(property);
		properties.sort(byOrder);
		return properties.toArray(new EnumSpellProperty[0]);
	}
	
	public static List<IMagicEffect> getSpellsWithProperties(EnumSpellProperty... properties)
	{
		List<IMagicEffect> spells = new ArrayList<>();
		for(IMagicEffect spell : MagicEffects.getAllSpells())
		{
			boolean match = true;
			for(EnumSpellProperty property : properties)
				if(!spell.getSpellProperties().contains(property))
				{
					match = false;
					break;
				}
			
			if(match)
				spells.add(spell);
		}
		return spells;
	}
	
	public String getSerializedName()
	{
		return "enum."+Reference.ModInfo.MOD_PREFIX+"spell_property."+name().toLowerCase()+".name";
	}
	
	public String getTranslatedName(){ return Component.translatable(getSerializedName()).getString(); }
	
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getTexture()
	{
		return new ResourceLocation(Reference.ModInfo.MOD_PREFIX+"textures/spell_properties/"+name().toLowerCase()+".png");
	}
	
	@OnlyIn(Dist.CLIENT)
	public void drawPropertySymbol(int x, int y, int width, int height, double zLevel)
	{
//		GlStateManager.pushMatrix();
//	    	GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
//	    	GlStateManager.enableAlpha();
//	        float f = 1F / 32F;
//	        Tessellator tessellator = Tessellator.getInstance();
//	        BufferBuilder bufferbuilder = tessellator.getBuffer();
//	        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//		        bufferbuilder.pos((double)(x + 0), (double)(y + height), (double)zLevel).tex((double)((float)(0) * f), (double)((float)(18) * f)).endVertex();
//		        bufferbuilder.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((float)(18) * f), (double)((float)(18) * f)).endVertex();
//		        bufferbuilder.pos((double)(x + width), (double)(y + 0), (double)zLevel).tex((double)((float)(18) * f), (double)((float)(0) * f)).endVertex();
//		        bufferbuilder.pos((double)(x + 0), (double)(y + 0), (double)zLevel).tex((double)((float)(0) * f), (double)((float)(0) * f)).endVertex();
//	        tessellator.draw();
//	    	GlStateManager.disableAlpha();
//        GlStateManager.popMatrix();
	}
}
