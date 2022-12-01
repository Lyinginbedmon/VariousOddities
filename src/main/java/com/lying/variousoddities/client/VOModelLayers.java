package com.lying.variousoddities.client;

import com.lying.variousoddities.reference.Reference;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VOModelLayers
{
	public static final ModelLayerLocation KOBOLD				= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "kobold"), "main");
	public static final ModelLayerLocation KOBOLD_ARMOR_INNER	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "kobold"), "inner_armor");
	public static final ModelLayerLocation KOBOLD_ARMOR_OUTER	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "kobold"), "outer_armor");
	
	public static final ModelLayerLocation GOBLIN				= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "goblin"), "main");
	public static final ModelLayerLocation GOBLIN_ARMOR_INNER	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "goblin"), "inner_armor");
	public static final ModelLayerLocation GOBLIN_ARMOR_OUTER	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "goblin"), "outer_armor");
	
	public static final ModelLayerLocation MIND_FLAYER				= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "mind_flayer"), "main");
	public static final ModelLayerLocation MIND_FLAYER_ARMOR_INNER	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "mind_flayer"), "inner_armor");
	public static final ModelLayerLocation MIND_FLAYER_ARMOR_OUTER	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "mind_flayer"), "outer_armor");
	
	public static final ModelLayerLocation WARG				= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "warg"), "main");
	public static final ModelLayerLocation WARG_ARMOR_INNER	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "warg"), "inner_armor");
	public static final ModelLayerLocation WARG_ARMOR_OUTER	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "warg"), "outer_armor");
	public static final ModelLayerLocation WARG_CHEST		= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "warg"), "chest");
	public static final ModelLayerLocation WARG_SADDLE		= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "warg"), "saddle");
	public static final ModelLayerLocation WARG_DECOR		= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "warg"), "decor");
	
	public static final ModelLayerLocation CRAB				= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "crab"), "main");
	public static final ModelLayerLocation CRAB_BARNACLES	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "crab"), "barnacles");
	
	public static final ModelLayerLocation SCORPION			= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "scorpion"), "main");
	public static final ModelLayerLocation SCORPION_BABIES	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "scorpion"), "babies");
	
	public static final ModelLayerLocation PATRON_KIRIN			= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "kirin"), "main");
	public static final ModelLayerLocation PATRON_KIRIN_HORNS	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "kirin"), "horns");
	
	public static final ModelLayerLocation PATRON_WITCH_HUMAN		= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "witch_human"), "main");
	public static final ModelLayerLocation PATRON_WITCH_ELF			= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "witch_elf"), "main");
	public static final ModelLayerLocation PATRON_WITCH_CRONE		= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "witch_crone"), "main");
	public static final ModelLayerLocation PATRON_WITCH_CHANGELING	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "witch_changeling"), "main");
	
	public static final ModelLayerLocation CHANGELING		= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "changeling"), "main");
	public static final ModelLayerLocation CHANGELING_ELF	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "changeling_elf"), "main");
	
	public static final ModelLayerLocation GHASTLING	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "ghastling"), "main");
	public static final ModelLayerLocation MARIMO		= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "marimo"), "main");
	public static final ModelLayerLocation RAT			= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "rat"), "main");
	public static final ModelLayerLocation WORG			= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "worg"), "main");
	
	public static final ModelLayerLocation DAZED			= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "dazed"), "main");
	public static final ModelLayerLocation FOX_ACCESSORIES	= new ModelLayerLocation(new ResourceLocation(Reference.ModInfo.MOD_ID, "fox_accessories"), "main");
}
