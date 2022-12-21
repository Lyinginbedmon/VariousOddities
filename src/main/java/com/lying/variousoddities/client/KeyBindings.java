package com.lying.variousoddities.client;

import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.AbilityData;
import com.lying.variousoddities.client.gui.ScreenAbilityMenu;
import com.lying.variousoddities.network.PacketAbilityActivate;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.ActivatedAbility;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class KeyBindings
{
	private static final List<KeyMapping> KEYS = Lists.newArrayList();
	private static final String CATEGORY = "keys."+Reference.ModInfo.MOD_ID+".category";
	
	private static final String OPEN_ABILITY_MENU = "keys."+Reference.ModInfo.MOD_ID+".ability_menu";
	private static final String ACTIVATE_ABILITY_1 = "keys."+Reference.ModInfo.MOD_ID+".ability_1";
	private static final String ACTIVATE_ABILITY_2 = "keys."+Reference.ModInfo.MOD_ID+".ability_2";
	private static final String ACTIVATE_ABILITY_3 = "keys."+Reference.ModInfo.MOD_ID+".ability_3";
	private static final String ACTIVATE_ABILITY_4 = "keys."+Reference.ModInfo.MOD_ID+".ability_4";
	private static final String ACTIVATE_ABILITY_5 = "keys."+Reference.ModInfo.MOD_ID+".ability_5";
	
	public static final KeyMapping ABILITY_MENU = register(new KeyMapping(OPEN_ABILITY_MENU, KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY));
	public static final KeyMapping ABILITY_1 = register(new KeyMapping(ACTIVATE_ABILITY_1, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_1, CATEGORY));
	public static final KeyMapping ABILITY_2 = register(new KeyMapping(ACTIVATE_ABILITY_2, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_2, CATEGORY));
	public static final KeyMapping ABILITY_3 = register(new KeyMapping(ACTIVATE_ABILITY_3, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_3, CATEGORY));
	public static final KeyMapping ABILITY_4 = register(new KeyMapping(ACTIVATE_ABILITY_4, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_4, CATEGORY));
	public static final KeyMapping ABILITY_5 = register(new KeyMapping(ACTIVATE_ABILITY_5, KeyConflictContext.IN_GAME, KeyModifier.ALT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_5, CATEGORY));
	
	private static KeyMapping register(KeyMapping binding)
	{
		KEYS.add(binding);
		return binding;
	}
	
	public static void registerKeybinds(Consumer<KeyMapping> consumer)
	{
		MinecraftForge.EVENT_BUS.register(new KeyBindings());
		KEYS.forEach((key) -> { consumer.accept(key); });
	}
	
	@SubscribeEvent
	public void handleInputEvent(InputEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		KeyMapping pressedKey = getPressedKey();
		LocalPlayer player = mc.player;
		if(pressedKey == null || player == null || !player.isAlive() || (player.isSleeping() || player.isSleepingLongEnough()) || mc.screen != null)
			return;
		
		if(pressedKey == ABILITY_MENU)
			mc.setScreen(new ScreenAbilityMenu());
		else if(pressedKey == ABILITY_1)
			handleAbilityKey(0, mc);
		else if(pressedKey == ABILITY_2)
			handleAbilityKey(1, mc);
		else if(pressedKey == ABILITY_3)
			handleAbilityKey(2, mc);
		else if(pressedKey == ABILITY_4)
			handleAbilityKey(3, mc);
		else if(pressedKey == ABILITY_5)
			handleAbilityKey(4, mc);
	}
	
	public KeyMapping getPressedKey()
	{
		for(KeyMapping key : KEYS)
			if(key.isDown())
				return key;
		
		return null;
	}
	
	private static void handleAbilityKey(int index, Minecraft mc)
	{
		AbilityData data = AbilityData.forEntity(mc.player);
		ResourceLocation mapName = data.getFavourite(index);
		if(mapName != null)
		{
			ActivatedAbility ability = (ActivatedAbility)AbilityRegistry.getAbilityByMapName(mc.player, mapName);
			if(ability != null && ability.canTrigger(mc.player))
			{
				ability.trigger(mc.player, Dist.CLIENT);
				PacketHandler.sendToServer(new PacketAbilityActivate(mapName));
			}
		}
	}
}
