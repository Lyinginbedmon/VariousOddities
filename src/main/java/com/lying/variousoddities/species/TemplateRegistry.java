package com.lying.variousoddities.species;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability.Nature;
import com.lying.variousoddities.species.abilities.AbilityDamageReduction;
import com.lying.variousoddities.species.abilities.AbilityDamageResistance;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityPoison;
import com.lying.variousoddities.species.abilities.AbilityResistance;
import com.lying.variousoddities.species.abilities.AbilityResistanceSpell;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.templates.AbilityOperation;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.templates.TemplateOperation.Operation;
import com.lying.variousoddities.species.templates.TypeOperation;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;

import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.potion.EffectInstance;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class TemplateRegistry extends JsonReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	
	private static final List<Template> DEFAULT_TEMPLATES = Lists.newArrayList();
	
	public static final ResourceLocation TEMPLATE_FIENDISH		= new ResourceLocation(Reference.ModInfo.MOD_ID, "fiendish");
	public static final ResourceLocation TEMPLATE_CELESTIAL		= new ResourceLocation(Reference.ModInfo.MOD_ID, "celestial");
	public static final ResourceLocation TEMPLATE_LICH			= new ResourceLocation(Reference.ModInfo.MOD_ID, "lich");
	public static final ResourceLocation TEMPLATE_ZOMBIE		= new ResourceLocation(Reference.ModInfo.MOD_ID, "zombie");
	
	private static final UUID UUID_FIENDISH = UUID.fromString("ff6ac129-6eaf-4015-bbc3-75ec226e5bf6");
	private static final UUID UUID_CELESTIAL = UUID.fromString("c70089ac-ff68-487c-abd1-3e4cab1cb336");
	private static final UUID UUID_LICH = UUID.fromString("0f52db4b-4388-4fde-8e55-34ff8a68ca85");
	private static final UUID UUID_ZOMBIE = UUID.fromString("eec62733-5e7b-4c47-a939-9590b9a6f492");
	
	private static TemplateRegistry instance;
	
	public static TemplateRegistry getInstance()
	{
		if(instance == null)
			instance = new TemplateRegistry();
		return instance;
	}
	
	public TemplateRegistry()
	{
		super(GSON, "templates");
	}
	
	public static List<Template> getDefaultTemplates(){ return Lists.newArrayList(DEFAULT_TEMPLATES); }
	
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn)
	{
		VariousOddities.log.info("Attempting to load templates from data, entries: "+objectIn.size());
		Map<ResourceLocation, Template> loaded = new HashMap<>();
		objectIn.forEach((name, json) -> {
            try
            {
                Template template = Template.fromJson(json);
                if(template != null)
                {
                	VariousOddities.log.info(" -Loaded: "+name.toString()+", with "+template.getOperations().size()+" operations");
                	for(TemplateOperation operation : template.getOperations())
                    	VariousOddities.log.info("   -"+operation.translate().getString());
                	
                	template.setRegistryName(name);
                    loaded.put(name, template);
                }
            }
            catch (IllegalArgumentException | JsonParseException e)
            {
                VariousOddities.log.error("Failed to load template {}: {}", name);
            }
            catch(Exception e)
            {
            	VariousOddities.log.error("Unrecognised error loading template {}", name);
            }
        });
		
		loaded.forEach((name,template) -> { VORegistries.TEMPLATES.put(name, template); });
		
		// If no templates were found in the datapack, load the defaults
		if(loaded.isEmpty())
		{
			VariousOddities.log.warn("No templates found, loading defaults");
			DEFAULT_TEMPLATES.forEach((template) -> { VORegistries.TEMPLATES.put(template.getRegistryName(), template); });
		}
	}
	
	static
	{
		/*
		 * Fiendish
		 * Celestial
		 * Half-Fiend
		 * Half-Celestial
		 * Half-Dragon
		 * Lich
		 * Vampire
		 * Vampire Spawn
		 */
		
		DEFAULT_TEMPLATES.add(new Template(TEMPLATE_FIENDISH, UUID_FIENDISH)
				.setPower(2)
				.setPlayerSelect(true)
				.addOperation(new TypeOperation(Operation.ADD, EnumCreatureType.EXTRAPLANAR))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityDamageReduction(5, DamageType.MAGIC)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityResistance(5, DamageType.COLD)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityResistance(5, DamageType.FIRE))));
		DEFAULT_TEMPLATES.add(new Template(TEMPLATE_CELESTIAL, UUID_CELESTIAL)
				.setPower(2)
				.setPlayerSelect(true)
				.addOperation(new TypeOperation(Operation.ADD, EnumCreatureType.EXTRAPLANAR))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityDamageReduction(5, DamageType.MAGIC)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityResistance(5, DamageType.COLD)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityResistance(5, DamageType.ACID)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityResistance(5, DamageType.LIGHTNING))));
		DEFAULT_TEMPLATES.add(new Template(TEMPLATE_ZOMBIE, UUID_ZOMBIE)
				.setPower(1)
				.setPlayerSelect(false)
				.addOperation(new TypeOperation(Operation.REMOVE_ALL, true))
				.addOperation(new TypeOperation(Operation.ADD, EnumCreatureType.UNDEAD))
				.addOperation(new AbilityOperation(Operation.REMOVE_ALL, Nature.SPELL_LIKE, Nature.SUPERNATURAL))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityNaturalArmour(2D)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityDamageReduction(3))));
		DEFAULT_TEMPLATES.add(new Template(TEMPLATE_LICH, UUID_LICH)
				.setPower(4)
				.setPlayerSelect(false)
				.addOperation(new TypeOperation(Operation.REMOVE_ALL, true))
				.addOperation(new TypeOperation(Operation.ADD, EnumCreatureType.UNDEAD))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityNaturalArmour(5D)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityDamageReduction(15, DamageType.MAGIC)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityDamageResistance(DamageType.COLD, DamageResist.IMMUNE)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityDamageResistance(DamageType.LIGHTNING, DamageResist.IMMUNE)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityResistanceSpell(MagicSubType.MIND_AFFECTING)))
				.addOperation(new AbilityOperation(Operation.ADD, new AbilityPoison(0.65F, new EffectInstance(VOPotions.PARALYSIS, Reference.Values.TICKS_PER_SECOND * 15)).setDisplayName(new TranslationTextComponent("ability.varodd:lich_touch")))));
	}
}
