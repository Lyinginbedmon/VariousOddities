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
import com.lying.variousoddities.config.ConfigVO;
import com.lying.variousoddities.init.VOBlocks;
import com.lying.variousoddities.init.VOMobEffects;
import com.lying.variousoddities.init.VORegistries;
import com.lying.variousoddities.magic.IMagicEffect.MagicSubType;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability.Nature;
import com.lying.variousoddities.species.abilities.AbilityClimb;
import com.lying.variousoddities.species.abilities.AbilityDamageReduction;
import com.lying.variousoddities.species.abilities.AbilityDamageResistance;
import com.lying.variousoddities.species.abilities.AbilityDarkvision;
import com.lying.variousoddities.species.abilities.AbilityDrainHealth;
import com.lying.variousoddities.species.abilities.AbilityFastHealing;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityFlight.Grade;
import com.lying.variousoddities.species.abilities.AbilityForm;
import com.lying.variousoddities.species.abilities.AbilityGaze;
import com.lying.variousoddities.species.abilities.AbilityHoldBreath;
import com.lying.variousoddities.species.abilities.AbilityModifierCon;
import com.lying.variousoddities.species.abilities.AbilityModifierStr;
import com.lying.variousoddities.species.abilities.AbilityNaturalArmour;
import com.lying.variousoddities.species.abilities.AbilityNaturalRegen;
import com.lying.variousoddities.species.abilities.AbilityPoison;
import com.lying.variousoddities.species.abilities.AbilityResistance;
import com.lying.variousoddities.species.abilities.AbilityResistanceSpell;
import com.lying.variousoddities.species.abilities.AbilityScent;
import com.lying.variousoddities.species.abilities.AbilitySmite;
import com.lying.variousoddities.species.abilities.AbilityStartingItem;
import com.lying.variousoddities.species.abilities.AbilitySunBurn;
import com.lying.variousoddities.species.abilities.AbilityTremorsense;
import com.lying.variousoddities.species.abilities.DamageType;
import com.lying.variousoddities.species.templates.AbilityOperation;
import com.lying.variousoddities.species.templates.AbilityPrecondition;
import com.lying.variousoddities.species.templates.OperationReplaceSupertypes;
import com.lying.variousoddities.species.templates.TemplateOperation;
import com.lying.variousoddities.species.templates.TemplateOperation.Operation;
import com.lying.variousoddities.species.templates.TypeOperation;
import com.lying.variousoddities.species.templates.TypeOperation.Condition;
import com.lying.variousoddities.species.templates.TypeOperation.Condition.Style;
import com.lying.variousoddities.species.templates.TypePrecondition;
import com.lying.variousoddities.species.types.EnumCreatureType;
import com.lying.variousoddities.species.types.TypeHandler.DamageResist;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

public class TemplateRegistry extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	
	private static final List<Template> DEFAULT_TEMPLATES = Lists.newArrayList();
	
	public static final ResourceLocation TEMPLATE_CELESTIAL		= new ResourceLocation(Reference.ModInfo.MOD_ID, "celestial");
	public static final ResourceLocation TEMPLATE_FIENDISH		= new ResourceLocation(Reference.ModInfo.MOD_ID, "fiendish");
	public static final ResourceLocation TEMPLATE_INSECTILE		= new ResourceLocation(Reference.ModInfo.MOD_ID, "insectile");
	public static final ResourceLocation TEMPLATE_LICH			= new ResourceLocation(Reference.ModInfo.MOD_ID, "lich");
	public static final ResourceLocation TEMPLATE_NECROPOLITAN	= new ResourceLocation(Reference.ModInfo.MOD_ID, "necropolitan");
	public static final ResourceLocation TEMPLATE_REPTILIAN		= new ResourceLocation(Reference.ModInfo.MOD_ID, "reptilian");
	public static final ResourceLocation TEMPLATE_VAMPIRE		= new ResourceLocation(Reference.ModInfo.MOD_ID, "vampire");
	public static final ResourceLocation TEMPLATE_WINGED		= new ResourceLocation(Reference.ModInfo.MOD_ID, "winged");
	public static final ResourceLocation TEMPLATE_ZOMBIE		= new ResourceLocation(Reference.ModInfo.MOD_ID, "zombie");
	
	private static final UUID UUID_CELESTIAL = UUID.fromString("c70089ac-ff68-487c-abd1-3e4cab1cb336");
	private static final UUID UUID_FIENDISH = UUID.fromString("ff6ac129-6eaf-4015-bbc3-75ec226e5bf6");
	private static final UUID UUID_INSECTILE = UUID.fromString("d606248a-4cdc-47c4-b96e-33ad1bd16186");
	private static final UUID UUID_LICH = UUID.fromString("0f52db4b-4388-4fde-8e55-34ff8a68ca85");
	private static final UUID UUID_NECROPOLITAN = UUID.fromString("4ca65fb7-fe3d-4e2c-a86f-469b8cfc13c7");
	private static final UUID UUID_REPTILIAN = UUID.fromString("1e887c65-f18e-4418-9a0e-48ebc9baaa78");
	private static final UUID UUID_VAMPIRE = UUID.fromString("16c220bb-b995-4688-b477-53281049251f");
	private static final UUID UUID_WINGED = UUID.fromString("97b43a6a-8626-4a6d-8a8c-b0caca354bc6");
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
	
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager manager, ProfilerFiller filler)
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
                	if(ConfigVO.GENERAL.verboseLogs())
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
		
		// If no templates were found in the datapack, load the defaults
		if(loaded.isEmpty())
		{
			VariousOddities.log.warn("No templates found, loading defaults");
			DEFAULT_TEMPLATES.forEach((template) -> { loaded.put(template.getRegistryName(), template); });
		}
		
		VORegistries.TEMPLATES.clear();
		loaded.forEach((name,template) -> { VORegistries.TEMPLATES.put(name, template); });
	}
	
	private static void addTemplate(Template templateIn)
	{
		DEFAULT_TEMPLATES.add(templateIn);
	}
	
	static
	{
		/*
		 * Half-Fiend
		 *   Power 4
		 *   Flight (Good)
		 *   Spells
		 *   Smite Good
		 * Half-Celestial
		 *   Power 4
		 *   Flight (Good)
		 *   Spells
		 *   Daylight spell
		 *   Smite Evil
		 * Half-Dragon
		 *   Power 3
		 *   Flight (Average)
		 *   Breath weapon (type-based)
		 *   Damage immunity (type-based)
		 * Vampire
		 *   Power 8
		 *   Summon mobs
		 *   Create vampire spawn
		 *   Energy drain
		 *   Alternate forms
		 *   Turn resistance
		 *   Vampire weaknesses (?)
		 * Vampire Spawn
		 *   Energy drain
		 *   Vampire weaknesses
		 * Acidborn
		 *   Power 1
		 *   Breathe acid
		 *   Acid immunity
		 * Shadow creature
		 *   Power 2
		 *   Invisible in low light
		 *   Random additional ability (see LoM pg 168)
		 * Half-Vampire
		 *   Power 2
		 *   Summon mobs
		 *   Charm (randomly)
		 *   Drain blood (randomly, +blood dependency)
		 * Mummy
		 *   Power 4
		 *   Despair (temp paralysis on sight)
		 * Revived Fossil
		 * 	 Mostly just a buff skeleton
		 */
		
		addTemplate(new Template(TEMPLATE_FIENDISH, UUID_FIENDISH)
				.setPower(2)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".fiendish"))
				.addPrecondition(TypePrecondition.isCorporeal())
				.addPrecondition(TypePrecondition.isAnyOf(EnumCreatureType.ABERRATION, EnumCreatureType.ANIMAL, EnumCreatureType.DRAGON, EnumCreatureType.FEY, EnumCreatureType.GIANT, EnumCreatureType.HUMANOID, EnumCreatureType.MAGICAL_BEAST, EnumCreatureType.MONSTROUS_HUMANOID, EnumCreatureType.OOZE, EnumCreatureType.PLANT, EnumCreatureType.VERMIN))
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.MAGICAL_BEAST).setCondition(new Condition(Style.OR, EnumCreatureType.ANIMAL, EnumCreatureType.VERMIN)))
				.addOperation(new TypeOperation(Operation.ADD, EnumCreatureType.EXTRAPLANAR))
				.addOperation(AbilityOperation.add(new AbilitySmite(EnumCreatureType.HOLY)))
				.addOperation(AbilityOperation.add(new AbilityDamageReduction(5, DamageType.MAGIC)))
				.addOperation(AbilityOperation.add(true, new AbilityResistance(5, DamageType.COLD)))
				.addOperation(AbilityOperation.add(true, new AbilityResistance(5, DamageType.FIRE))));
		addTemplate(new Template(TEMPLATE_CELESTIAL, UUID_CELESTIAL)
				.setPower(2)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".celestial"))
				.addPrecondition(TypePrecondition.isCorporeal())
				.addPrecondition(TypePrecondition.isAnyOf(EnumCreatureType.ABERRATION, EnumCreatureType.ANIMAL, EnumCreatureType.DRAGON, EnumCreatureType.FEY, EnumCreatureType.GIANT, EnumCreatureType.HUMANOID, EnumCreatureType.MAGICAL_BEAST, EnumCreatureType.MONSTROUS_HUMANOID, EnumCreatureType.OOZE, EnumCreatureType.PLANT, EnumCreatureType.VERMIN))
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.MAGICAL_BEAST).setCondition(new Condition(Style.OR, EnumCreatureType.ANIMAL, EnumCreatureType.VERMIN)))
				.addOperation(new TypeOperation(Operation.ADD, EnumCreatureType.EXTRAPLANAR))
				.addOperation(AbilityOperation.add(new AbilitySmite(EnumCreatureType.EVIL)))
				.addOperation(AbilityOperation.add(new AbilityDamageReduction(5, DamageType.MAGIC)))
				.addOperation(AbilityOperation.add(true, new AbilityResistance(5, DamageType.COLD)))
				.addOperation(AbilityOperation.add(true, new AbilityResistance(5, DamageType.ACID)))
				.addOperation(AbilityOperation.add(true, new AbilityResistance(5, DamageType.LIGHTNING))));
		addTemplate(new Template(TEMPLATE_ZOMBIE, UUID_ZOMBIE)
				.setPower(1)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".zombie"))
				.addPrecondition(TypePrecondition.isCorporeal())
				.addPrecondition(TypePrecondition.isLiving())
				.addPrecondition(TypePrecondition.isNoneOf(EnumCreatureType.OOZE, EnumCreatureType.PLANT))
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.UNDEAD))
				.addOperation(AbilityOperation.loseCon())
				.addOperation(new AbilityOperation(Operation.REMOVE_ALL, Nature.SPELL_LIKE, Nature.SUPERNATURAL))
				.addOperation(AbilityOperation.add(true, new AbilityNaturalArmour(2D)))
				.addOperation(AbilityOperation.add(true, new AbilityModifierStr(1D)))
				.addOperation(AbilityOperation.add(true, new AbilityDamageReduction(3)))
				.addOperation(AbilityOperation.add(new AbilitySunBurn())));
		addTemplate(new Template(TEMPLATE_LICH, UUID_LICH)
				.setPower(4)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".lich"))
				.addPrecondition(TypePrecondition.isLiving())
				.addPrecondition(TypePrecondition.isAnyOf(EnumCreatureType.HUMANOID))
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.UNDEAD))
				.addOperation(AbilityOperation.loseCon())
				.addOperation(AbilityOperation.add(new AbilityNaturalArmour(5D)))
				.addOperation(AbilityOperation.add(new AbilityDamageReduction(15, DamageType.MAGIC)))
				.addOperation(AbilityOperation.add(new AbilityDamageResistance(DamageType.COLD, DamageResist.IMMUNE)))
				.addOperation(AbilityOperation.add(new AbilityDamageResistance(DamageType.LIGHTNING, DamageResist.IMMUNE)))
				.addOperation(AbilityOperation.add(new AbilityResistanceSpell(MagicSubType.MIND_AFFECTING)))
				.addOperation(AbilityOperation.add(new AbilityPoison(0.65F, new MobEffectInstance(VOMobEffects.PARALYSIS.get(), Reference.Values.TICKS_PER_SECOND * 15)).setDisplayName(Component.translatable("ability.varodd:lich_touch"))))
				.addOperation(AbilityOperation.add(new AbilityStartingItem(new ItemStack(VOBlocks.PHYLACTERY.get())))));
		addTemplate(new Template(TEMPLATE_WINGED, UUID_WINGED)
				.setPower(2)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".winged"))
				.addPrecondition(TypePrecondition.isAnyOf(EnumCreatureType.ANIMAL, EnumCreatureType.GIANT, EnumCreatureType.HUMANOID, EnumCreatureType.MONSTROUS_HUMANOID, EnumCreatureType.VERMIN))
				.addPrecondition(AbilityPrecondition.hasNo(new AbilityFlight(Grade.AVERAGE)))
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.MAGICAL_BEAST).setCondition(new Condition(Style.OR, EnumCreatureType.ANIMAL, EnumCreatureType.VERMIN)))
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.MONSTROUS_HUMANOID).setCondition(new Condition(Style.AND, EnumCreatureType.HUMANOID)))
				.addOperation(AbilityOperation.add(true, new AbilityFlight(Grade.AVERAGE))));
		addTemplate(new Template(TEMPLATE_REPTILIAN, UUID_REPTILIAN)
				.setPower(2)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".reptilian"))
				.addPrecondition(TypePrecondition.isAnyOf(EnumCreatureType.GIANT, EnumCreatureType.HUMANOID))
				.addPrecondition(TypePrecondition.isNoneOf(EnumCreatureType.AQUATIC, EnumCreatureType.REPTILE))
				.addOperation(new TypeOperation(Operation.ADD, EnumCreatureType.REPTILE))
				.addOperation(AbilityOperation.add(true, new AbilityNaturalArmour(2D)))
				.addOperation(AbilityOperation.add(true, new AbilityModifierCon(2D)))
				.addOperation(AbilityOperation.add(true, new AbilityModifierStr(2D)))
				.addOperation(AbilityOperation.add(new AbilityDarkvision()))
				.addOperation(AbilityOperation.add(true, new AbilityScent(16D)))
				.addOperation(AbilityOperation.add(new AbilityHoldBreath())));
		addTemplate(new Template(TEMPLATE_INSECTILE, UUID_INSECTILE)
				.setPower(2)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".insectile"))
				.addPrecondition(TypePrecondition.isAnyOf(EnumCreatureType.GIANT, EnumCreatureType.HUMANOID, EnumCreatureType.MONSTROUS_HUMANOID))
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.ABERRATION))
				.addOperation(AbilityOperation.add(true, new AbilityNaturalArmour(2D)))
				.addOperation(AbilityOperation.add(true, new AbilityClimb()))
				.addOperation(AbilityOperation.add(new AbilityDarkvision()))
				.addOperation(AbilityOperation.add(true, new AbilityTremorsense(16D))));
		addTemplate(new Template(TEMPLATE_NECROPOLITAN, UUID_NECROPOLITAN)
				.setPower(0)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".necropolitan"))
				.addPrecondition(TypePrecondition.isHumanShaped())
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.UNDEAD))
				.addOperation(AbilityOperation.loseCon())
				.addOperation(AbilityOperation.add(new AbilityNaturalRegen())));
		addTemplate(new Template(TEMPLATE_VAMPIRE, UUID_VAMPIRE)
				.setPower(8)
				.setDisplayName(Component.translatable("template."+Reference.ModInfo.MOD_ID+".vampire"))
				.addPrecondition(TypePrecondition.isHumanShaped())
				.addOperation(new OperationReplaceSupertypes(EnumCreatureType.UNDEAD))
				.addOperation(AbilityOperation.loseCon())
				.addOperation(AbilityOperation.add(true, new AbilityModifierStr(3D)))
				.addOperation(AbilityOperation.add(new AbilityDrainHealth()))
				.addOperation(AbilityOperation.add(new AbilityDamageReduction(10, DamageType.SILVER, DamageType.MAGIC)))
				.addOperation(AbilityOperation.add(new AbilityFastHealing(5)))
				.addOperation(AbilityOperation.add(new AbilityForm.Mist()))
				.addOperation(AbilityOperation.add(new AbilityResistance(10, DamageType.COLD)))
				.addOperation(AbilityOperation.add(new AbilityResistance(10, DamageType.LIGHTNING)))
				.addOperation(AbilityOperation.add(new AbilityClimb()))
				.addOperation(AbilityOperation.add(new AbilitySunBurn()))
				.addOperation(AbilityOperation.add(new AbilityGaze.Dominate())));
	}
}
