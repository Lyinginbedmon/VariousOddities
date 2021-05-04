package com.lying.variousoddities.client;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.client.gui.ScreenAbilityMenu;
import com.lying.variousoddities.network.PacketAbilityActivate;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.types.abilities.AbilityRegistry;
import com.lying.variousoddities.types.abilities.ActivatedAbility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

@OnlyIn(Dist.CLIENT)
public class KeyBindings
{
	private static final List<KeyBinding> KEYS = Lists.newArrayList();
	private static final String CATEGORY = "keys."+Reference.ModInfo.MOD_ID+".category";

	private static final String OPEN_ABILITY_MENU = "keys."+Reference.ModInfo.MOD_ID+".ability_menu";
	private static final String ACTIVATE_ABILITY_1 = "keys."+Reference.ModInfo.MOD_ID+".ability_1";
	private static final String ACTIVATE_ABILITY_2 = "keys."+Reference.ModInfo.MOD_ID+".ability_2";
	private static final String ACTIVATE_ABILITY_3 = "keys."+Reference.ModInfo.MOD_ID+".ability_3";
	private static final String ACTIVATE_ABILITY_4 = "keys."+Reference.ModInfo.MOD_ID+".ability_4";
	private static final String ACTIVATE_ABILITY_5 = "keys."+Reference.ModInfo.MOD_ID+".ability_5";
	
	public static final KeyBinding ABILITY_MENU = register(new KeyBinding(OPEN_ABILITY_MENU, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY));
	public static final KeyBinding ABILITY_1 = register(new KeyBinding(ACTIVATE_ABILITY_1, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_1, CATEGORY));
	public static final KeyBinding ABILITY_2 = register(new KeyBinding(ACTIVATE_ABILITY_2, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_2, CATEGORY));
	public static final KeyBinding ABILITY_3 = register(new KeyBinding(ACTIVATE_ABILITY_3, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_3, CATEGORY));
	public static final KeyBinding ABILITY_4 = register(new KeyBinding(ACTIVATE_ABILITY_4, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_4, CATEGORY));
	public static final KeyBinding ABILITY_5 = register(new KeyBinding(ACTIVATE_ABILITY_5, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_5, CATEGORY));
	
	private static KeyBinding register(KeyBinding binding)
	{
		KEYS.add(binding);
		return binding;
	}
	
	public static void register()
	{
		MinecraftForge.EVENT_BUS.register(new KeyBindings());
		KEYS.forEach((key) -> { ClientRegistry.registerKeyBinding(key); });
	}
	
	@SubscribeEvent
	public void handleInputEvent(InputEvent event)
	{
		KeyBinding pressedKey = getPressedKey();
		if(pressedKey == null || Minecraft.getInstance().player == null || !Minecraft.getInstance().player.isAlive() || Minecraft.getInstance().player.isSleeping())
			return;
		
		if(Minecraft.getInstance().currentScreen == null)
			if(pressedKey == ABILITY_MENU)
				Minecraft.getInstance().displayGuiScreen(new ScreenAbilityMenu());
			else if(pressedKey == ABILITY_1)
				handleAbilityKey(0);
			else if(pressedKey == ABILITY_2)
				handleAbilityKey(1);
			else if(pressedKey == ABILITY_3)
				handleAbilityKey(2);
			else if(pressedKey == ABILITY_4)
				handleAbilityKey(3);
			else if(pressedKey == ABILITY_5)
				handleAbilityKey(4);
	}
	
	public KeyBinding getPressedKey()
	{
		for(KeyBinding key : KEYS)
			if(key.isKeyDown())
				return key;
		
		return null;
	}
	
	private static void handleAbilityKey(int index)
	{
		LivingData data = LivingData.forEntity(Minecraft.getInstance().player);
		ResourceLocation mapName = data.getAbilities().getFavourite(index);
		if(mapName != null)
		{
			ActivatedAbility ability = (ActivatedAbility)AbilityRegistry.getAbilityByName(Minecraft.getInstance().player, mapName);
			if(ability != null && ability.canTrigger(Minecraft.getInstance().player))
			{
				ability.trigger(Minecraft.getInstance().player, Dist.CLIENT);
				PacketHandler.sendToServer(new PacketAbilityActivate(mapName));
			}
		}
	}
}
