package com.lying.variousoddities.command;

import java.util.Collection;

import com.lying.variousoddities.api.world.settlement.EnumRoomFunction;
import com.lying.variousoddities.api.world.settlement.Settlement;
import com.lying.variousoddities.init.VOItems;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.tileentity.TileEntityDraftingTable;
import com.lying.variousoddities.world.savedata.SettlementManager;
import com.lying.variousoddities.world.settlement.BoxRoom;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.command.EnumArgument;

public class CommandSettlement extends CommandBase
{
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".settlement.";
	
	public String getName(){ return "settlement"; }
	
	public String getUsage(Player sender)
	{
		return translationSlug+"usage";
	}
	
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("settlement").requires((source) -> { return source.hasPermission(2); } )
			.then(VariantList.build())
			.then(VariantHere.build())
			.then(VariantRemove.build())
			.then(VariantAdd.build())
			.then(VariantEdit.build())
			.then(VariantMove.build())
			.then(VariantRoom.build()));
	}
	
	/** Notify the given source that the desired settlement was not found */
	public static void notifyUnknownSettlement(CommandSourceStack source)
	{
		source.sendFailure(makeErrorMessage(translationSlug+"failed_unknown"));
	}
	
	public static Component makeErrorMessage(String translation, Object... args)
	{
		return Component.translatable(translation, args).withStyle((style) -> {
			return style.applyFormat(ChatFormatting.RED);
		});
	}
    
    public static void notifyListenerOfRoom(int index, BoxRoom room, CommandSourceStack source)
    {
    	BlockPos min = room.min();
    	int minX = min.getX();
    	int minY = min.getY();
    	int minZ = min.getZ();
    	
    	BlockPos max = room.max();
    	int maxX = max.getX();
    	int maxY = max.getY();
    	int maxZ = max.getZ();
		if(room.hasCustomName())
			source.sendSuccess(Component.translatable(translationSlug+"list.room.index_name", new Object[]{index, room.getName(), room.getFunction().getName(), minX, minY, minZ, maxX, maxY, maxZ}), false);
		else
			source.sendSuccess(Component.translatable(translationSlug+"list.room.index", new Object[]{index, room.getFunction().getName(), minX, minY, minZ, maxX, maxY, maxZ}), false);
    }
    
    public static void notifyListenerOfSettlement(Settlement settlement, SettlementManager manager, CommandSourceStack source)
    {
		int index = manager.getIndexBySettlement(settlement);
		if(settlement.hasCustomName())
			source.sendSuccess(Component.translatable(translationSlug+"index_name", new Object[]{index, settlement.getCustomName(), settlement.typeName()}), false);
		else
			source.sendSuccess(Component.translatable(translationSlug+"index",new Object[]{index, settlement.typeName()}), false);
    }
    
    public static Component translateSettlementDetails(Settlement settlement, SettlementManager manager, boolean link)
    {
		int index = manager.getIndexBySettlement(settlement);
		
		MutableComponent component = 
				settlement.hasCustomName() ? 
						Component.translatable(translationSlug+"index_name", index, settlement.getCustomName(), settlement.typeName()) :  
						Component.translatable(translationSlug+"index", index, settlement.typeName());
		
		if(link)
		{
			component = ComponentUtils.wrapInSquareBrackets(component).withStyle((p_211752_2_) -> {
	            return p_211752_2_.applyFormat(ChatFormatting.DARK_AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/settlement list "+index))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable(translationSlug+"list.more_info")));
	         });
		}
		return component;
    }
    
    private static class VariantList
    {
		public static final SimpleCommandExceptionType LIST_FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.translatable(translationSlug+"list.failed"));
    	
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("list")
    				.executes(VariantList::listAllSettlements)
    				.then(Commands.argument("name", StringArgumentType.string())
    					.executes(VariantList::listTargetSettlementName))
    				.then(Commands.argument("index", IntegerArgumentType.integer(0))
    					.executes(VariantList::listTargetSettlementIndex));
    	}
    	
    	/** List all settlements in chat, including embedded commands to detail specific ones */
    	public static int listAllSettlements(final CommandContext<CommandSourceStack> source) throws CommandSyntaxException
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		
    		if(manager.isEmpty())
    			throw LIST_FAILED_EXCEPTION.create();
    		else
    		{
				Collection<Settlement> settlements = manager.getSettlements();
				source.getSource().sendSuccess(Component.translatable(translationSlug+"list.success", settlements.size()), true);
				for(Settlement settlement : settlements)
					source.getSource().sendSuccess(Component.literal("-").append(CommandSettlement.translateSettlementDetails(settlement, manager, true)), false);
    		}
    		
    		return manager.getSettlements().size();
    	}
    	
    	/** List a settlement in chat, including the details of all rooms */
    	private static int listSettlement(Settlement settlement, SettlementManager manager, CommandSourceStack source)
    	{
    		if(settlement != null)
    		{
    			source.sendSuccess(CommandSettlement.translateSettlementDetails(settlement, manager, false), true);
				if(settlement.hasRooms())
				{
					int index = 0;
					for(BoxRoom room : settlement.getRooms())
						notifyListenerOfRoom(index++, room, source);
				}
				return Math.max(1, settlement.getRooms().size());
    		}
    		else
    			notifyUnknownSettlement(source);
    		return 0;
    	}
    	
    	public static int listTargetSettlementName(final CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		return listSettlement(manager.getSettlementByName(StringArgumentType.getString(source, "name")), manager, source.getSource());
    	}
    	
    	public static int listTargetSettlementIndex(final CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		return listSettlement(manager.getSettlementByIndex(IntegerArgumentType.getInteger(source, "index")), manager, source.getSource());
    	}
    }
    
    private static class VariantAdd
    {
	 	public static final SuggestionProvider<CommandSourceStack> TYPE_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("settlement_types"), (context, builder) -> {
	 		return SharedSuggestionProvider.suggestResource(SettlementManager.getSettlementTypes(), builder);
	 		});
    	
	 	private static final String TYPE = "type";
	 	private static final String POS = "pos";
	 	private static final String NAME = "name";
	 	private static final String NBT = "nbt";
	 	
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("add")
    				.then(Commands.argument(TYPE, ResourceLocationArgument.id()).suggests(VariantAdd.TYPE_SUGGESTIONS)
    					.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getId(source, TYPE), null, null, null, source.getSource()); })
    					.then(Commands.argument(POS, BlockPosArgument.blockPos())
    						.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getId(source, TYPE), BlockPosArgument.getLoadedBlockPos(source, POS), null, null, source.getSource()); })
    						.then(Commands.argument(NAME, StringArgumentType.string())
    							.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getId(source, TYPE), BlockPosArgument.getLoadedBlockPos(source, POS), StringArgumentType.getString(source, NAME), null, source.getSource()); }))
    						.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
    							.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getId(source, TYPE), BlockPosArgument.getLoadedBlockPos(source, POS), null, CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); }))
    						.then(Commands.argument(NAME, StringArgumentType.string())
								.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
									.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getId(source, TYPE), BlockPosArgument.getLoadedBlockPos(source, POS), StringArgumentType.getString(source, NAME), CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); }))))
						.then(Commands.argument(NAME, StringArgumentType.string())
							.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getId(source, TYPE), null, StringArgumentType.getString(source, NAME), null, source.getSource()); })
							.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
								.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getId(source, TYPE), null, StringArgumentType.getString(source, NAME), CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); })))
						.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
							.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getId(source, TYPE), null, null, CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); })));
    	}
    	
    	public static int add(ResourceLocation type, BlockPos pos, String name, CompoundTag nbt, CommandSourceStack source)
    	{
    		CompoundTag data = nbt == null || nbt.isEmpty() ? new CompoundTag() : nbt;
    		SettlementManager manager = SettlementManager.get(source.getLevel());
    		Settlement settlement = SettlementManager.createSettlementFromNBT(type, data);
    		if(settlement != null)
    		{
        		if(pos != null)
        			settlement.setMarker(pos);
        		
        		if(name != null && name.length() > 0)
        			settlement.setCustomName(name.replace(" ", "_"));
        		
				// Default to invulnerable if starting without rooms for ease of use by command block chains
				if(!settlement.hasRooms())
					settlement.setInvulnerable(true);
				
	    		int newIndex = manager.add(settlement);
	    		source.sendSuccess(Component.translatable(translationSlug+"add.success", new Object[]{settlement.hasCustomName() ? settlement.getCustomName() : newIndex}), true);
	    		return 15;
    		}
    		else
    		{
	    		source.sendFailure(makeErrorMessage(translationSlug+"add.failed"));
	    		return 0;
    		}
    	}
    }
    
    private static class VariantRemove
    {
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("remove")
    				.then(Commands.argument("name", StringArgumentType.string())
						.executes(VariantRemove::removeSettlementName))
					.then(Commands.argument("index", IntegerArgumentType.integer(0))
						.executes(VariantRemove::removeSettlementIndex));
    	}
    	
    	private static int removeSettlement(Settlement target, String name, CommandSourceStack source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getLevel());
    		if(target != null)
    		{
    			if(manager.remove(target))
    				source.sendSuccess(Component.translatable(translationSlug+"remove.success", name), true);
    			else
    				source.sendFailure(makeErrorMessage(translationSlug+"remove.failed", name));
    		}
    		else
    			notifyUnknownSettlement(source);
    		return target != null ? 15 : 0;
    	}
    	
    	public static int removeSettlementName(CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		String name = StringArgumentType.getString(source, "name");
    		Settlement target = manager.getSettlementByName(name);
    		return removeSettlement(target, name, source.getSource());
    	}
    	
    	public static int removeSettlementIndex(CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		Integer index = IntegerArgumentType.getInteger(source, "index");
    		Settlement target = manager.getSettlementByIndex(index);
    		return removeSettlement(target, String.valueOf(index), source.getSource());
    	}
    }
    
    private static class VariantMove
    {
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("move")
    				.then(Commands.argument("index", IntegerArgumentType.integer(0))
						.then(Commands.argument("destination", BlockPosArgument.blockPos())
						.executes(VariantMove::moveSettlementIndex)))
    				.then(Commands.argument("name", StringArgumentType.string())
						.then(Commands.argument("destination", BlockPosArgument.blockPos())
						.executes(VariantMove::moveSettlementName)));
    	}
    	
    	private static int moveSettlement(Settlement target, String id, BlockPos pos, CommandSourceStack source)
    	{
    		if(target == null)
    		{
    			notifyUnknownSettlement(source);
    			return 0;
    		}
    		else if(pos == null)
    		{
    			source.sendFailure(makeErrorMessage(translationSlug+"move.failed", id));
    			return 0;
    		}
			else
			{
				target.setMarker(pos);
    			source.sendSuccess(Component.translatable(translationSlug+"move.success", id, pos.getX(), pos.getY(), pos.getZ()), true);
    			return 15;
			}
    	}
    	
    	public static int moveSettlementIndex(CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		int index = IntegerArgumentType.getInteger(source, "index");
    		BlockPos dest = null;
    		try {
				dest = BlockPosArgument.getLoadedBlockPos(source, "destination");
			} catch (CommandSyntaxException e){ }
    		return moveSettlement(manager.getSettlementByIndex(index), String.valueOf(index), dest, source.getSource());
    	}
    	
    	public static int moveSettlementName(CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		String index = StringArgumentType.getString(source, "name");
    		BlockPos dest = null;
    		try {
				dest = BlockPosArgument.getLoadedBlockPos(source, "destination");
			} catch (CommandSyntaxException e){ }
    		return moveSettlement(manager.getSettlementByName(index), index, dest, source.getSource());
    	}
    }
    
    private static class VariantEdit
    {
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("edit")
    				.then(Commands.argument("index", IntegerArgumentType.integer(0))
						.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
						.executes(VariantEdit::editIndex)))
    				.then(Commands.argument("name", StringArgumentType.string())
						.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
						.executes(VariantEdit::editName)));
    	}
    	
    	private static int edit(Settlement settlement, CompoundTag newNBT, SettlementManager manager, CommandSourceStack source)
    	{
    		if(settlement != null)
    		{
	    		int index = manager.getIndexBySettlement(settlement);
				CompoundTag settlementNBT = SettlementManager.settlementToNBT(index, settlement, new CompoundTag());
				CompoundTag originalNBT = settlementNBT.copy();
				settlementNBT.merge(newNBT);
				if(settlementNBT.equals(originalNBT))
					source.sendFailure(makeErrorMessage(translationSlug+"edit.failed", NbtUtils.toPrettyComponent(settlementNBT)));
				else
				{
					manager.add(index, SettlementManager.NBTToSettlement(settlementNBT));
					source.sendSuccess(Component.translatable(translationSlug+"edit.success", NbtUtils.toPrettyComponent(settlementNBT)), true);
				}
    		}
    		else
    			notifyUnknownSettlement(source);
			
			return settlement == null ? 0 : 15;
    	}
    	
    	public static int editIndex(CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		Settlement settlement = manager.getSettlementByIndex(IntegerArgumentType.getInteger(source, "index"));
    		return edit(settlement, CompoundTagArgument.getCompoundTag(source, "nbt"), manager, source.getSource());
    	}
    	
    	public static int editName(CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		Settlement settlement = manager.getSettlementByName(StringArgumentType.getString(source, "name"));
    		return edit(settlement, CompoundTagArgument.getCompoundTag(source, "nbt"), manager, source.getSource());
    	}
    }
    
    private static class VariantHere
    {
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("here")
    				.executes(VariantHere::listHere)
    				.then(Commands.argument("position", Vec3Argument.vec3())
    					.executes(VariantHere::listHerePos));
    	}
    	
    	private static int herePos(Vec3 pos, CommandSourceStack source) throws CommandSyntaxException
    	{
    		SettlementManager manager = SettlementManager.get(source.getLevel());
			Settlement settlementHere = manager.getSettlementAt(pos);
			if(settlementHere == null)
				throw VariantList.LIST_FAILED_EXCEPTION.create();
			else
				notifyListenerOfSettlement(settlementHere, manager, source);
    		return settlementHere == null ? 0 : 15;
    	}
    	
    	public static int listHere(CommandContext<CommandSourceStack> source) throws CommandSyntaxException
    	{
    		return herePos(source.getSource().getPosition(), source.getSource());
    	}
    	
    	public static int listHerePos(CommandContext<CommandSourceStack> source) throws CommandSyntaxException
    	{
    		Vec3 position = Vec3Argument.getVec3(source, "position");
			if(position == null)
			{
				source.getSource().sendFailure(makeErrorMessage(translationSlug+".here.failed.coords"));
				return 0;
			}
			else
				return herePos(position, source.getSource());
    	}
    }
    
    private static class VariantRoom
    {
    	protected static final String SET_INDEX = "settlement index";
    	protected static final String SET_NAME = "settlement name";
    	
    	protected static final String ROM_INDEX = "room index";
    	protected static final String ROM_NAME = "room name";
    	
    	protected static final String POS_A = "from";
		protected static final String POS_B = "to";
    	
    	public static LiteralArgumentBuilder<CommandSourceStack> build()
    	{
    		return Commands.literal("room")
    				.then(Commands.argument(SET_INDEX, IntegerArgumentType.integer(0))
	    				.then(VariantRoomAdd.build())
	    				.then(VariantRoomRemove.build())
	    				.then(VariantRoomEdit.build())
	    				.then(VariantRoomMove.build()))
    				.then(Commands.argument(SET_NAME, StringArgumentType.string())
	    				.then(VariantRoomAdd.build())
	    				.then(VariantRoomRemove.build())
	    				.then(VariantRoomEdit.build())
	    				.then(VariantRoomMove.build()));
    	}
    	
    	/** 
    	 * Returns the settlement indicated by the contained arguments.<br>
    	 * Produces an error message if it cannot be found. */
    	public static Settlement getSettlement(CommandContext<CommandSourceStack> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getLevel());
    		Settlement settlement = null;
    		try
    		{
    			int index = IntegerArgumentType.getInteger(source, SET_INDEX);
        		if(index >= 0)
        			settlement = manager.getSettlementByIndex(index);
    		}
    		catch(Exception e){ }
    		
    		if(settlement == null)
	    		try
	    		{
	    			String name = StringArgumentType.getString(source, SET_NAME);
	        		if(name != null && name.length() > 0)
	        			settlement = manager.getSettlementByName(name);
	    		}
	    		catch(Exception e){ }
    		
    		if(settlement == null)
    			notifyUnknownSettlement(source.getSource());
    		return settlement;
    	}
    	
    	public static BoxRoom getRoom(int index, Settlement settlement)
    	{
			if(settlement != null && settlement.hasRooms())
	    		return index >= 0 && index < settlement.getRooms().size() ? settlement.getRooms().get(index) : null;
			return null;
    	}
    	
    	public static BoxRoom getRoom(String name, Settlement settlement)
    	{
    		if(settlement != null && settlement.hasRooms())
    			for(BoxRoom room : settlement.getRooms())
    				if(room.hasCustomName() && room.getName().equals(name))
    					return room;
    		return null;
    	}
		
		private static Tuple<BlockPos, BlockPos> getCoordinates(CommandContext<CommandSourceStack> source)
		{
			BlockPos posA = null;
			try
			{
				posA = BlockPosArgument.getLoadedBlockPos(source, POS_A);
				if(posA != null)
					if(posA.getY() < 0)
						posA.offset(0, Math.abs(posA.getY()), 0);
					else if(posA.getY() > 255)
						posA.offset(0, 255 - posA.getY(), 0);
			}
			catch(Exception e){ }
			BlockPos posB = null;
			try
			{
				posB = BlockPosArgument.getLoadedBlockPos(source, POS_B);
				if(posB != null)
					if(posB.getY() < 0)
						posB.offset(0, Math.abs(posB.getY()), 0);
					else if(posB.getY() > 255)
						posB.offset(0, 255 - posB.getY(), 0);
			}
			catch(Exception e){ }
			if(posA == null || posB == null)
				return new Tuple<BlockPos, BlockPos>(null, null);
			return new Tuple<BlockPos, BlockPos>(posA, posB);
		}
    	
    	public static void notifyUnknownRoom(String token, CommandSourceStack source)
    	{
    		source.sendFailure(makeErrorMessage(translationSlug+"failed_room", token));
    	}
    	
    	private static class VariantRoomAdd
    	{
    		private static final String FUNCTION = "function";
    		private static final String NAME = "name";
    		private static final String NBT = "nbt";
    		
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("add")
    					.executes(VariantRoomAdd::addFromHand)
    					.then(Commands.argument(POS_A, BlockPosArgument.blockPos())
    						.then(Commands.argument(POS_B, BlockPosArgument.blockPos())
								.then(Commands.argument(FUNCTION, EnumArgument.enumArgument(EnumRoomFunction.class))
									.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getLoadedBlockPos(source, POS_A), BlockPosArgument.getLoadedBlockPos(source, POS_B), source.getArgument(FUNCTION, EnumRoomFunction.class), null, null, source.getSource()); })
									.then(Commands.argument(NAME, StringArgumentType.string())
										.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getLoadedBlockPos(source, POS_A), BlockPosArgument.getLoadedBlockPos(source, POS_B), source.getArgument(FUNCTION, EnumRoomFunction.class), StringArgumentType.getString(source, NAME), null, source.getSource()); }))
									.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
										.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getLoadedBlockPos(source, POS_A), BlockPosArgument.getLoadedBlockPos(source, POS_B), source.getArgument(FUNCTION, EnumRoomFunction.class), null, CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); }))
									.then(Commands.argument(NAME, StringArgumentType.string())
										.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
											.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getLoadedBlockPos(source, POS_A), BlockPosArgument.getLoadedBlockPos(source, POS_B), source.getArgument(FUNCTION, EnumRoomFunction.class), StringArgumentType.getString(source, NAME), CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); }))))
								.then(Commands.argument(NAME, StringArgumentType.string())
									.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getLoadedBlockPos(source, POS_A), BlockPosArgument.getLoadedBlockPos(source, POS_B), null, StringArgumentType.getString(source, NAME), null, source.getSource()); })
									.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
										.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getLoadedBlockPos(source, POS_A), BlockPosArgument.getLoadedBlockPos(source, POS_B), null, StringArgumentType.getString(source, NAME), CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); })))
								.then(Commands.argument(NBT, CompoundTagArgument.compoundTag())
										.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getLoadedBlockPos(source, POS_A), BlockPosArgument.getLoadedBlockPos(source, POS_B), null, null, CompoundTagArgument.getCompoundTag(source, NBT), source.getSource()); }))));
    		}
    		
    		public static int addFromHand(CommandContext<CommandSourceStack> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement == null)
    				return 0;
    			
    			try
    			{
    				Entity ent = source.getSource().getEntity();
    				if(ent instanceof LivingEntity)
    				{
    					LivingEntity living = (LivingEntity)ent;
    					if(!living.getMainHandItem().isEmpty() && living.getMainHandItem().getItem() == VOItems.DRAFTING_TABLE.get())
    					{
    						ItemStack table = living.getMainHandItem();
    						if(table.hasTag() && table.getTag().contains("BlockEntityTag", 10))
    						{
    							CompoundTag tableData = table.getTag().getCompound("BlockEntityTag");
    							if(tableData.contains("Locked", 3) && tableData.getInt("Locked") == 15)
    							{
	    							if(!(living instanceof Player && ((Player)living).isCreative()))
	    								table.shrink(1);
	    							
	    							BoxRoom room = TileEntityDraftingTable.getRoomFromNBT(tableData);
	    							return add(settlement, room, source.getSource());
    							}
    						}
    					}
    				}
    			}
    			catch(Exception e){ }
				source.getSource().sendFailure(makeErrorMessage(translationSlug+"room.add.failed"));
    			return 0;
    		}
    		
    		public static int add(Settlement settlement, BoxRoom room, CommandSourceStack source)
    		{
    			if(settlement != null && room != null)
    			{
	    			settlement.addRoom(room);
	    			
	    			SettlementManager manager = SettlementManager.get(source.getLevel());
	    			manager.add(manager.getIndexBySettlement(settlement), settlement);
	    			source.sendSuccess(Component.translatable(translationSlug+"room.add.success", room.hasCustomName() ? room.getName() : settlement.getIndexFromRoom(room)), true);
	    			return 15;
    			}
    			return 0;
    		}
    		
    		public static int add(Settlement settlement, BlockPos posA, BlockPos posB, EnumRoomFunction function, String name, CompoundTag nbt, CommandSourceStack source)
    		{
    			BoxRoom room = new BoxRoom(posA, posB);
    			if(function != null)
    				room.setFunction(function);
    			if(name != null && name.length() > 0)
    				room.setName(name.replace(" ", "_"));
    			if(nbt != null && !nbt.isEmpty())
    				room.readFromNBT(nbt);
    			
    			return add(settlement, room, source);
    		}
    	}
    	
    	private static class VariantRoomRemove
    	{
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("remove")
    					.then(Commands.argument(ROM_INDEX, IntegerArgumentType.integer(0))
							.executes(VariantRoomRemove::removeIndex))
    					.then(Commands.argument(ROM_NAME, StringArgumentType.string())
    						.executes(VariantRoomRemove::removeName));
    		}
    		
    		public static int remove(Settlement settlement, BoxRoom room, SettlementManager manager, CommandSourceStack source)
    		{
	    		if(settlement != null && room != null)
	    		{
	    			boolean success = settlement.removeRoom(room);
	    			if(success)
	    				manager.add(manager.getIndexBySettlement(settlement), settlement);
	    			return success ? 15 : 0;
	    		}
	    		return 0;
    		}
    		
    		public static int removeIndex(CommandContext<CommandSourceStack> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement == null) return 0;
    			
				int index = IntegerArgumentType.getInteger(source, ROM_INDEX);
	    		int result = remove(settlement, getRoom(index, settlement), SettlementManager.get(source.getSource().getLevel()), source.getSource());
	    		if(result == 0)
	    			notifyUnknownRoom(String.valueOf(index), source.getSource());
	    		else
	    			source.getSource().sendSuccess(Component.translatable(translationSlug+"room.remove.success", index), true);
	    		return result;
    		}
    		
    		public static int removeName(CommandContext<CommandSourceStack> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement == null) return 0;
    			
    			String name = StringArgumentType.getString(source, ROM_NAME);
	    		int result = remove(settlement, getRoom(name, settlement), SettlementManager.get(source.getSource().getLevel()), source.getSource());
	    		if(result == 0)
	    			notifyUnknownRoom(name, source.getSource());
	    		else
	    			source.getSource().sendSuccess(Component.translatable(translationSlug+"room.remove.success", name), true);
	    		return result;
    		}
    	}
    	
    	private static class VariantRoomEdit
    	{
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("edit")
    					.then(Commands.argument(ROM_INDEX, IntegerArgumentType.integer(0))
    						.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
    							.executes(VariantRoomEdit::editIndex)))
    					.then(Commands.argument(ROM_NAME, StringArgumentType.string())
        					.then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
        						.executes(VariantRoomEdit::editName)));
    		}
    		
    		public static int edit(BoxRoom room, CompoundTag newNBT, Settlement settlement, CommandSourceStack source)
    		{
                CompoundTag roomNBT = room.writeToNBT(new CompoundTag());
                CompoundTag originalNBT = roomNBT.copy();
                roomNBT.merge(newNBT);
                if(roomNBT.equals(originalNBT))
                {
                	source.sendFailure(makeErrorMessage(translationSlug+"room.edit.failed", NbtUtils.toPrettyComponent(roomNBT)));
                	return 0;
                }
                else
                {
                	room.readFromNBT(roomNBT);
                	settlement.addRoom(settlement.getIndexFromRoom(room), room);
                	SettlementManager manager = SettlementManager.get(source.getLevel());
                	manager.add(manager.getIndexBySettlement(settlement), settlement);
                	
                	source.sendSuccess(Component.translatable(translationSlug+"room.edit.success", NbtUtils.toPrettyComponent(roomNBT)), true);
                	return 15;
                }
    		}
    		
    		public static int editIndex(CommandContext<CommandSourceStack> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement != null)
    			{
    				int index = IntegerArgumentType.getInteger(source, ROM_INDEX);
    				BoxRoom room = getRoom(index, settlement);
    				if(room == null)
    				{
    					notifyUnknownRoom(String.valueOf(index), source.getSource());
    					return 0;
    				}
    				return edit(room, CompoundTagArgument.getCompoundTag(source, "nbt"), settlement, source.getSource());
    			}
    			return 0;
    		}
    		
    		public static int editName(CommandContext<CommandSourceStack> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement != null)
    			{
    				String name = StringArgumentType.getString(source, ROM_NAME);
    				BoxRoom room = getRoom(name, settlement);
    				if(room == null)
    				{
    					notifyUnknownRoom(name, source.getSource());
    					return 0;
    				}
    				return edit(room, CompoundTagArgument.getCompoundTag(source, "nbt"), settlement, source.getSource());
    			}
    			return 0;
    		}
    	}
    	
    	private static class VariantRoomMove
    	{
    		public static LiteralArgumentBuilder<CommandSourceStack> build()
    		{
    			return Commands.literal("move")
    					.then(Commands.argument(ROM_INDEX, IntegerArgumentType.integer(0))
    						.then(Commands.argument(POS_A, BlockPosArgument.blockPos())
    	    					.then(Commands.argument(POS_B, BlockPosArgument.blockPos())
    	    						.executes(VariantRoomMove::moveIndex))))
    					.then(Commands.argument(ROM_NAME, IntegerArgumentType.integer(0))
    						.then(Commands.argument(POS_A, BlockPosArgument.blockPos())
    	    					.then(Commands.argument(POS_B, BlockPosArgument.blockPos())
    	    						.executes(VariantRoomMove::moveName))));
    		}
    		
    		public static int move(int index, BoxRoom room, Settlement settlement, BlockPos posA, BlockPos posB, CommandSourceStack source)
    		{
    			if(settlement != null && room != null && posA != null && posB != null)
    			{
    				room.set(posA, posB);
    				settlement.addRoom(index, room);
    				SettlementManager manager = SettlementManager.get(source.getLevel());
    				manager.add(manager.getIndexBySettlement(settlement), settlement);
    				
    				BlockPos roomMin = room.min();
    				BlockPos roomMax = room.max();
    				source.sendSuccess(Component.translatable(translationSlug+"room.move.success", room.hasCustomName() ? room.getName() : index, roomMin.getX(), roomMin.getY(), roomMin.getZ(), roomMax.getX(), roomMax.getY(), roomMax.getZ()), true);
    				return 15;
    			}
    			return 0;
    		}
    		
    		public static int moveIndex(CommandContext<CommandSourceStack> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement != null)
    			{
    				int index = IntegerArgumentType.getInteger(source, ROM_INDEX);
    				BoxRoom room = getRoom(index, settlement);
    				if(room == null)
    				{
    	    			notifyUnknownRoom(String.valueOf(index), source.getSource());
    	    			return 0;
    				}
    				
    				Tuple<BlockPos, BlockPos> coords = getCoordinates(source);
    				return move(index, room, settlement, coords.getA(), coords.getB(), source.getSource());
    			}
    			return 0;
    		}
    		
    		public static int moveName(CommandContext<CommandSourceStack> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement != null)
    			{
    				String name = StringArgumentType.getString(source, ROM_NAME);
    				
    				BoxRoom room = getRoom(name, settlement);
    				if(room == null)
    				{
    	    			notifyUnknownRoom(name, source.getSource());
    	    			return 0;
    				}
    				int index = settlement.getIndexFromRoom(room);
    				
    				Tuple<BlockPos, BlockPos> coords = getCoordinates(source);
    				return move(index, room, settlement, coords.getA(), coords.getB(), source.getSource());
    			}
    			return 0;
    		}
    	}
    }
}
