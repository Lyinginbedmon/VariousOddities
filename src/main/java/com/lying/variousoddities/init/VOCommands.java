package com.lying.variousoddities.init;

<<<<<<< Updated upstream
=======
import com.lying.variousoddities.api.EnumArgumentChecked;
>>>>>>> Stashed changes
import com.lying.variousoddities.command.*;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraftforge.event.RegisterCommandsEvent;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class VOCommands
{
	/** Registers custom commands */
    public static void onCommandRegister(RegisterCommandsEvent event)
    {
<<<<<<< Updated upstream
    	onArgumentRegister();
    	
=======
>>>>>>> Stashed changes
    	CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
    	CommandSettlement.register(dispatcher);
    	CommandTypes.register(dispatcher);
    	CommandFaction.register(dispatcher);
    }
    
<<<<<<< Updated upstream
    public static void onArgumentRegister()
    {
		ArgumentTypes.register(Reference.ModInfo.MOD_PREFIX+"creature_type", CreatureTypeArgument.class, new CreatureTypeArgument.Serializer());
		ArgumentTypes.register(Reference.ModInfo.MOD_PREFIX+"room_function", RoomFunctionArgument.class, new RoomFunctionArgument.Serializer());
=======
    /** Registers custom command arguments */
    public static void registerArguments()
    {
    	ArgumentTypes.register(Reference.ModInfo.MOD_PREFIX+"enum_checked", EnumArgumentChecked.class, (IArgumentSerializer)new EnumArgumentChecked.Serializer());
>>>>>>> Stashed changes
    }
}
