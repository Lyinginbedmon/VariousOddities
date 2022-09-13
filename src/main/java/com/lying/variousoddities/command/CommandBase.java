package com.lying.variousoddities.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.util.Tuple;

public abstract class CommandBase
{
    protected static CommandSelection getEnvironmentType()
    {
        return CommandSelection.ALL;
    }
    
    protected static LiteralArgumentBuilder<CommandSourceStack> build()
    {
        throw new RuntimeException("Missing command builder!");
    }
    
    protected static LiteralArgumentBuilder<CommandSourceStack> newLiteral(final String name)
    {
        return LiteralArgumentBuilder.literal(name);
    }
    
    protected static <T> RequiredArgumentBuilder<CommandSourceStack, T> newArgument(final String name, final ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }
    
    /**
     * Throws command syntax exception.
     *
     * @param key language key to translate
     */
//    public static void throwSyntaxException(final String key) throws CommandSyntaxException
//    {
//        throw new CommandSyntaxException(new StructurizeCommandExceptionType(), new LiteralMessage(LanguageHandler.translateKey(key)));
//    }

    /**
     * Throws command syntax exception.
     *
     * @param key    language key to translate
     * @param format String.format() attributes
     */
//    public static void throwSyntaxException(final String key, final Object... format) throws CommandSyntaxException
//    {
//        throw new CommandSyntaxException(new StructurizeCommandExceptionType(),
//            new LiteralMessage(LanguageHandler.translateKeyWithFormat(key, format)));
//    }
    
    protected static class CommandTree
    {
        /**
         * List of child trees, commands are directly baked into rootNode
         */
        private final List<CommandTree> childTrees;
        private final List<Tuple<Supplier<CommandSelection>, Supplier<LiteralArgumentBuilder<CommandSourceStack>>>> childNodes;
        /**
         * Target environment type.
         */
        private final CommandSelection buildWhenOn;
        private final String commandName;

        /**
         * @return constructs new root node
         */
        protected static CommandTree newRootNode()
        {
            return new CommandTree(CommandSelection.ALL, Reference.ModInfo.MOD_ID);
        }

        /**
         * Creates new command tree.
         *
         * @param commandName root vertex name
         */
        protected CommandTree(final CommandSelection environment, final String commandName)
        {
            this.childTrees = new ArrayList<>();
            this.childNodes = new ArrayList<>();
            this.buildWhenOn = environment;
            this.commandName = commandName;
        }

        /**
         * Adds new tree as leaf into this tree.
         *
         * @param tree new tree to add
         * @return this
         */
        protected CommandTree addNode(final CommandTree tree)
        {
            childTrees.add(tree);
            return this;
        }

        /**
         * Adds new command as leaf into this tree.
         *
         * @param commandBuilder    command to add
         * @param commandEnviroment command's enviroment getter
         * @return this
         */
        protected CommandTree addNode(final Supplier<LiteralArgumentBuilder<CommandSourceStack>> commandBuilder, final Supplier<CommandSelection> commandEnviroment)
        {
            childNodes.add(new Tuple<>(commandEnviroment, commandBuilder));
            return this;
        }
        
        /**
         * Builds whole tree for dispatcher.
         *
         * @return tree as command node
         */
        protected Optional<LiteralArgumentBuilder<CommandSourceStack>> build(final CommandSelection environment)
        {
            if (!checkEnvironment(environment, buildWhenOn))
            {
                return Optional.empty();
            }

            final LiteralArgumentBuilder<CommandSourceStack> rootNode = newLiteral(commandName);

            for (final Tuple<Supplier<CommandSelection>, Supplier<LiteralArgumentBuilder<CommandSourceStack>>> node : childNodes)
            {
                if (checkEnvironment(environment, node.getA().get()))
                {
                    rootNode.then(node.getB().get());
                }
            }
            for (final CommandTree tree : childTrees)
            {
                final Optional<LiteralArgumentBuilder<CommandSourceStack>> builtTree = tree.build(environment);
                if (builtTree.isPresent())
                {
                    rootNode.then(builtTree.get().build());
                }
            }

            return childNodes.isEmpty() && childTrees.isEmpty() ? Optional.empty() : Optional.of(rootNode);
        }
        
        protected void register(final CommandDispatcher<CommandSourceStack> commandDispatcher, final CommandSelection serverEnvironmentType)
        {
            final Optional<LiteralArgumentBuilder<CommandSourceStack>> builtTree = build(serverEnvironmentType);

            if (builtTree.isPresent())
            {
                commandDispatcher.register(builtTree.get());
            }
        }

        /**
         * @return true if either of arguments is {@link CommandSelection#ALL} or arguments are of the same type
         */
        private boolean checkEnvironment(final CommandSelection server, final CommandSelection command)
        {
            return server == CommandSelection.ALL || command == CommandSelection.ALL || server == command;
        }
    }
}