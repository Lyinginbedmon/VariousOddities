package com.lying.variousoddities.init;

import com.lying.variousoddities.command.*;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraftforge.event.RegisterCommandsEvent;

public class VOCommands
{
    public static void onCommandRegister(RegisterCommandsEvent event)
    {
    	onArgumentRegister();
    	
    	CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
    	CommandSettlement.register(dispatcher);
    	CommandTypes.register(dispatcher);
    	CommandFaction.register(dispatcher);
    }
    
    public static void onArgumentRegister()
    {
		ArgumentTypes.register(Reference.ModInfo.MOD_PREFIX+"creature_type", CreatureTypeArgument.class, new CreatureTypeArgument.Serializer());
		ArgumentTypes.register(Reference.ModInfo.MOD_PREFIX+"room_function", RoomFunctionArgument.class, new RoomFunctionArgument.Serializer());
    }
}
