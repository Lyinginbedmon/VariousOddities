package com.lying.variousoddities.init;

import com.lying.variousoddities.command.CommandAbilities;
import com.lying.variousoddities.command.CommandFaction;
import com.lying.variousoddities.command.CommandKnockout;
import com.lying.variousoddities.command.CommandSettlement;
import com.lying.variousoddities.command.CommandSpecies;
import com.lying.variousoddities.command.CommandTypes;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;

public class VOCommands
{
	/** Registers custom commands */
    public static void init(RegisterCommandsEvent event)
    {
    	CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		
    	CommandSettlement.register(dispatcher);
    	CommandTypes.register(dispatcher);
    	CommandFaction.register(dispatcher);
    	CommandAbilities.register(dispatcher);
    	CommandSpecies.register(dispatcher);
    	CommandKnockout.register(dispatcher);
    }
    
    /** Registers custom command arguments */
//    public static void registerArguments()
//    {
//    	ArgumentUtils.register(Reference.ModInfo.MOD_PREFIX+"enum_checked", EnumArgumentChecked.class, (IArgumentSerializer)new EnumArgumentChecked.Serializer());
//    }
}
