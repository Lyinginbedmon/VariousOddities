package com.lying.variousoddities.init;

import com.lying.variousoddities.api.EnumArgumentChecked;
import com.lying.variousoddities.command.*;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraftforge.event.RegisterCommandsEvent;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class VOCommands
{
	/** Registers custom commands */
    public static void onCommandRegister(RegisterCommandsEvent event)
    {
    	CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
    	CommandSettlement.register(dispatcher);
    	CommandTypes.register(dispatcher);
    	CommandFaction.register(dispatcher);
    	CommandSpawns.register(dispatcher);
    	CommandAbilities.register(dispatcher);
    }
    
    /** Registers custom command arguments */
    public static void registerArguments()
    {
    	ArgumentTypes.register(Reference.ModInfo.MOD_PREFIX+"enum_checked", EnumArgumentChecked.class, (IArgumentSerializer)new EnumArgumentChecked.Serializer());
    }
}
