package com.lying.variousoddities.command;

import java.util.Collection;

import com.lying.variousoddities.api.EnumArgumentChecked;
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
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.NBTCompoundTagArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandSettlement extends CommandBase
{
	private static final String translationSlug = "command."+Reference.ModInfo.MOD_ID+".settlement.";
	
	public String getName(){ return "settlement"; }
	
	public String getUsage(PlayerEntity sender)
	{
		return translationSlug+"usage";
	}
	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		LiteralArgumentBuilder<CommandSource> literal = newLiteral("settlement").requires((source) -> { return source.hasPermissionLevel(2); } )
			.then(VariantList.build())
			.then(VariantHere.build())
			.then(VariantRemove.build())
			.then(VariantAdd.build())
			.then(VariantEdit.build())
			.then(VariantMove.build())
			.then(VariantRoom.build());
		
		dispatcher.register(literal);
	}
	
	/** Notify the given source that the desired settlement was not found */
	public static void notifyUnknownSettlement(CommandSource source)
	{
		source.sendFeedback(makeErrorMessage(translationSlug+"failed_unknown"), true);
	}
	
	public static ITextComponent makeErrorMessage(String translation, Object... args)
	{
		return new TranslationTextComponent(translation, args).modifyStyle((style) -> {
			return style.setFormatting(TextFormatting.RED);
		});
	}
    
    public static void notifyListenerOfRoom(int index, BoxRoom room, CommandSource source)
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
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.room.index_name", new Object[]{index, room.getName(), room.getFunction().getName(), minX, minY, minZ, maxX, maxY, maxZ}), false);
		else
			source.sendFeedback(new TranslationTextComponent(translationSlug+"list.room.index", new Object[]{index, room.getFunction().getName(), minX, minY, minZ, maxX, maxY, maxZ}), false);
    }
    
    public static void notifyListenerOfSettlement(Settlement settlement, SettlementManager manager, CommandSource source)
    {
		int index = manager.getIndexBySettlement(settlement);
		if(settlement.hasCustomName())
			source.sendFeedback(new TranslationTextComponent(translationSlug+"index_name", new Object[]{index, settlement.getCustomName(), settlement.typeName()}), false);
		else
			source.sendFeedback(new TranslationTextComponent(translationSlug+"index",new Object[]{index, settlement.typeName()}), false);
    }
    
    public static ITextComponent translateSettlementDetails(Settlement settlement, SettlementManager manager, boolean link)
    {
		int index = manager.getIndexBySettlement(settlement);
		
		IFormattableTextComponent component = 
				settlement.hasCustomName() ? 
						new TranslationTextComponent(translationSlug+"index_name", index, settlement.getCustomName(), settlement.typeName()) :  
						new TranslationTextComponent(translationSlug+"index", index, settlement.typeName());
		
		if(link)
		{
			component = TextComponentUtils.wrapWithSquareBrackets(component).modifyStyle((p_211752_2_) -> {
	            return p_211752_2_.setFormatting(TextFormatting.DARK_AQUA).setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf("/settlement list "+index))).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent(translationSlug+"list.more_info")));
	         });
		}
		return component;
    }
    
    private static class VariantList
    {
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("list")
    				.executes(VariantList::listAllSettlements)
    				.then(newArgument("name", StringArgumentType.string())
    					.executes(VariantList::listTargetSettlementName))
    				.then(newArgument("index", IntegerArgumentType.integer(0))
    					.executes(VariantList::listTargetSettlementIndex));
    	}
    	
    	/** List all settlements in chat, including embedded commands to detail specific ones */
    	public static int listAllSettlements(final CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		
    		if(manager.isEmpty())
    			source.getSource().sendFeedback(makeErrorMessage(translationSlug+"list.failed"), true);
    		else
    		{
				Collection<Settlement> settlements = manager.getSettlements();
				source.getSource().sendFeedback(new TranslationTextComponent(translationSlug+"list.success", settlements.size()), true);
				for(Settlement settlement : settlements)
					source.getSource().sendFeedback(new StringTextComponent("-").append(CommandSettlement.translateSettlementDetails(settlement, manager, true)), false);
    		}
    		
    		return manager.getSettlements().size();
    	}
    	
    	/** List a settlement in chat, including the details of all rooms */
    	private static int listSettlement(Settlement settlement, SettlementManager manager, CommandSource source)
    	{
    		if(settlement != null)
    		{
    			source.sendFeedback(CommandSettlement.translateSettlementDetails(settlement, manager, false), true);
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
    	
    	public static int listTargetSettlementName(final CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		return listSettlement(manager.getSettlementByName(StringArgumentType.getString(source, "name")), manager, source.getSource());
    	}
    	
    	public static int listTargetSettlementIndex(final CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		return listSettlement(manager.getSettlementByIndex(IntegerArgumentType.getInteger(source, "index")), manager, source.getSource());
    	}
    }
    
    private static class VariantAdd
    {
	 	public static final SuggestionProvider<CommandSource> TYPE_SUGGESTIONS = SuggestionProviders.register(new ResourceLocation("settlement_types"), (context, builder) -> {
	 		return ISuggestionProvider.suggestIterable(SettlementManager.getSettlementTypes(), builder);
	 		});
    	
	 	private static final String TYPE = "type";
	 	private static final String POS = "pos";
	 	private static final String NAME = "name";
	 	private static final String NBT = "nbt";
	 	
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("add")
    				.then(newArgument(TYPE, ResourceLocationArgument.resourceLocation()).suggests(VariantAdd.TYPE_SUGGESTIONS)
    					.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getResourceLocation(source, TYPE), null, null, null, source.getSource()); })
    					.then(newArgument(POS, BlockPosArgument.blockPos())
    						.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getResourceLocation(source, TYPE), BlockPosArgument.getBlockPos(source, POS), null, null, source.getSource()); })
    						.then(newArgument(NAME, StringArgumentType.string())
    							.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getResourceLocation(source, TYPE), BlockPosArgument.getBlockPos(source, POS), StringArgumentType.getString(source, NAME), null, source.getSource()); }))
    						.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
    							.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getResourceLocation(source, TYPE), BlockPosArgument.getBlockPos(source, POS), null, NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); }))
    						.then(newArgument(NAME, StringArgumentType.string())
								.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
									.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getResourceLocation(source, TYPE), BlockPosArgument.getBlockPos(source, POS), StringArgumentType.getString(source, NAME), NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); }))))
						.then(newArgument(NAME, StringArgumentType.string())
							.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getResourceLocation(source, TYPE), null, StringArgumentType.getString(source, NAME), null, source.getSource()); })
							.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
								.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getResourceLocation(source, TYPE), null, StringArgumentType.getString(source, NAME), NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); })))
						.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
							.executes((source) -> { return VariantAdd.add(ResourceLocationArgument.getResourceLocation(source, TYPE), null, null, NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); })));
    	}
    	
    	public static int add(ResourceLocation type, BlockPos pos, String name, CompoundNBT nbt, CommandSource source)
    	{
    		CompoundNBT data = nbt == null || nbt.isEmpty() ? new CompoundNBT() : nbt;
    		SettlementManager manager = SettlementManager.get(source.getWorld());
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
	    		source.sendFeedback(new TranslationTextComponent(translationSlug+"add.success", new Object[]{settlement.hasCustomName() ? settlement.getCustomName() : newIndex}), true);
	    		return 15;
    		}
    		else
    		{
	    		source.sendFeedback(makeErrorMessage(translationSlug+"add.failed"), true);
	    		return 0;
    		}
    	}
    }
    
    private static class VariantRemove
    {
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("remove")
    				.then(newArgument("name", StringArgumentType.string())
						.executes(VariantRemove::removeSettlementName))
					.then(newArgument("index", IntegerArgumentType.integer(0))
						.executes(VariantRemove::removeSettlementIndex));
    	}
    	
    	private static int removeSettlement(Settlement target, String name, CommandSource source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getWorld());
    		if(target != null)
    		{
    			if(manager.remove(target))
    				source.sendFeedback(new TranslationTextComponent(translationSlug+"remove.success", name), true);
    			else
    				source.sendFeedback(makeErrorMessage(translationSlug+"remove.failed", name), true);
    		}
    		else
    			notifyUnknownSettlement(source);
    		return target != null ? 15 : 0;
    	}
    	
    	public static int removeSettlementName(CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		String name = StringArgumentType.getString(source, "name");
    		Settlement target = manager.getSettlementByName(name);
    		return removeSettlement(target, name, source.getSource());
    	}
    	
    	public static int removeSettlementIndex(CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		Integer index = IntegerArgumentType.getInteger(source, "index");
    		Settlement target = manager.getSettlementByIndex(index);
    		return removeSettlement(target, String.valueOf(index), source.getSource());
    	}
    }
    
    private static class VariantMove
    {
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("move")
    				.then(newArgument("index", IntegerArgumentType.integer(0))
						.then(newArgument("destination", BlockPosArgument.blockPos())
						.executes(VariantMove::moveSettlementIndex)))
    				.then(newArgument("name", StringArgumentType.string())
						.then(newArgument("destination", BlockPosArgument.blockPos())
						.executes(VariantMove::moveSettlementName)));
    	}
    	
    	private static int moveSettlement(Settlement target, String id, BlockPos pos, CommandSource source)
    	{
    		if(target == null)
    		{
    			notifyUnknownSettlement(source);
    			return 0;
    		}
    		else if(pos == null)
    		{
    			source.sendFeedback(makeErrorMessage(translationSlug+"move.failed", id), true);
    			return 0;
    		}
			else
			{
				target.setMarker(pos);
    			source.sendFeedback(new TranslationTextComponent(translationSlug+"move.success", id, pos.getX(), pos.getY(), pos.getZ()), true);
    			return 15;
			}
    	}
    	
    	public static int moveSettlementIndex(CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		int index = IntegerArgumentType.getInteger(source, "index");
    		BlockPos dest = null;
    		try {
				dest = BlockPosArgument.getBlockPos(source, "destination");
			} catch (CommandSyntaxException e){ }
    		return moveSettlement(manager.getSettlementByIndex(index), String.valueOf(index), dest, source.getSource());
    	}
    	
    	public static int moveSettlementName(CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		String index = StringArgumentType.getString(source, "name");
    		BlockPos dest = null;
    		try {
				dest = BlockPosArgument.getBlockPos(source, "destination");
			} catch (CommandSyntaxException e){ }
    		return moveSettlement(manager.getSettlementByName(index), index, dest, source.getSource());
    	}
    }
    
    private static class VariantEdit
    {
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("edit")
    				.then(newArgument("index", IntegerArgumentType.integer(0))
						.then(newArgument("nbt", NBTCompoundTagArgument.nbt())
						.executes(VariantEdit::editIndex)))
    				.then(newArgument("name", StringArgumentType.string())
						.then(newArgument("nbt", NBTCompoundTagArgument.nbt())
						.executes(VariantEdit::editName)));
    	}
    	
    	private static int edit(Settlement settlement, CompoundNBT newNBT, SettlementManager manager, CommandSource source)
    	{
    		if(settlement != null)
    		{
	    		int index = manager.getIndexBySettlement(settlement);
				CompoundNBT settlementNBT = SettlementManager.settlementToNBT(index, settlement, new CompoundNBT());
				CompoundNBT originalNBT = settlementNBT.copy();
				settlementNBT.merge(newNBT);
				if(settlementNBT.equals(originalNBT))
					source.sendFeedback(makeErrorMessage(translationSlug+"edit.failed", settlementNBT.toString()), true);
				else
				{
					manager.add(index, SettlementManager.NBTToSettlement(settlementNBT));
					source.sendFeedback(new TranslationTextComponent(translationSlug+"edit.success", settlementNBT.toString()), true);
				}
    		}
    		else
    			notifyUnknownSettlement(source);
			
			return settlement == null ? 0 : 15;
    	}
    	
    	public static int editIndex(CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		Settlement settlement = manager.getSettlementByIndex(IntegerArgumentType.getInteger(source, "index"));
    		return edit(settlement, NBTCompoundTagArgument.getNbt(source, "nbt"), manager, source.getSource());
    	}
    	
    	public static int editName(CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
    		Settlement settlement = manager.getSettlementByName(StringArgumentType.getString(source, "name"));
    		return edit(settlement, NBTCompoundTagArgument.getNbt(source, "nbt"), manager, source.getSource());
    	}
    }
    
    private static class VariantHere
    {
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("here")
    				.executes(VariantHere::listHere)
    				.then(newArgument("position", Vec3Argument.vec3())
    					.executes(VariantHere::listHerePos));
    	}
    	
    	private static int herePos(Vector3d pos, CommandSource source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getWorld());
			Settlement settlementHere = manager.getSettlementAt(pos);
			if(settlementHere == null)
				source.sendFeedback(makeErrorMessage(translationSlug+"here.failed", (int)pos.getX(), (int)pos.getY(), (int)pos.getZ()), true);
			else
				notifyListenerOfSettlement(settlementHere, manager, source);
    		return settlementHere == null ? 0 : 15;
    	}
    	
    	public static int listHere(CommandContext<CommandSource> source)
    	{
    		return herePos(source.getSource().getPos(), source.getSource());
    	}
    	
    	public static int listHerePos(CommandContext<CommandSource> source)
    	{
    		Vector3d position = null;
			try
			{
				position = Vec3Argument.getVec3(source, "position");
			} catch (CommandSyntaxException e){ }
			
			if(position == null)
			{
				source.getSource().sendFeedback(makeErrorMessage(translationSlug+".here.failed.coords"), true);
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
    	
    	public static LiteralArgumentBuilder<CommandSource> build()
    	{
    		return newLiteral("room")
    				.then(newArgument(SET_INDEX, IntegerArgumentType.integer(0))
	    				.then(VariantRoomAdd.build())
	    				.then(VariantRoomRemove.build())
	    				.then(VariantRoomEdit.build())
	    				.then(VariantRoomMove.build()))
    				.then(newArgument(SET_NAME, StringArgumentType.string())
	    				.then(VariantRoomAdd.build())
	    				.then(VariantRoomRemove.build())
	    				.then(VariantRoomEdit.build())
	    				.then(VariantRoomMove.build()));
    	}
    	
    	/** 
    	 * Returns the settlement indicated by the contained arguments.<br>
    	 * Produces an error message if it cannot be found. */
    	public static Settlement getSettlement(CommandContext<CommandSource> source)
    	{
    		SettlementManager manager = SettlementManager.get(source.getSource().getWorld());
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
		
		private static Tuple<BlockPos, BlockPos> getCoordinates(CommandContext<CommandSource> source)
		{
			BlockPos posA = null;
			try
			{
				posA = BlockPosArgument.getBlockPos(source, POS_A);
				if(posA != null)
					if(posA.getY() < 0)
						posA.add(0, Math.abs(posA.getY()), 0);
					else if(posA.getY() > 255)
						posA.add(0, 255 - posA.getY(), 0);
			}
			catch(Exception e){ }
			BlockPos posB = null;
			try
			{
				posB = BlockPosArgument.getBlockPos(source, POS_B);
				if(posB != null)
					if(posB.getY() < 0)
						posB.add(0, Math.abs(posB.getY()), 0);
					else if(posB.getY() > 255)
						posB.add(0, 255 - posB.getY(), 0);
			}
			catch(Exception e){ }
			if(posA == null || posB == null)
				return new Tuple<BlockPos, BlockPos>(null, null);
			return new Tuple<BlockPos, BlockPos>(posA, posB);
		}
    	
    	public static void notifyUnknownRoom(String token, CommandSource source)
    	{
    		source.sendFeedback(makeErrorMessage(translationSlug+"failed_room", token), true);
    	}
    	
    	private static class VariantRoomAdd
    	{
    		private static final String FUNCTION = "function";
    		private static final String NAME = "name";
    		private static final String NBT = "nbt";
    		
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("add")
    					.executes(VariantRoomAdd::addFromHand)
    					.then(newArgument(POS_A, BlockPosArgument.blockPos())
    						.then(newArgument(POS_B, BlockPosArgument.blockPos())
								.then(newArgument(FUNCTION, EnumArgumentChecked.enumArgument(EnumRoomFunction.class))
									.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getBlockPos(source, POS_A), BlockPosArgument.getBlockPos(source, POS_B), source.getArgument(FUNCTION, EnumRoomFunction.class), null, null, source.getSource()); })
									.then(newArgument(NAME, StringArgumentType.string())
										.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getBlockPos(source, POS_A), BlockPosArgument.getBlockPos(source, POS_B), source.getArgument(FUNCTION, EnumRoomFunction.class), StringArgumentType.getString(source, NAME), null, source.getSource()); }))
									.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
										.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getBlockPos(source, POS_A), BlockPosArgument.getBlockPos(source, POS_B), source.getArgument(FUNCTION, EnumRoomFunction.class), null, NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); }))
									.then(newArgument(NAME, StringArgumentType.string())
										.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
											.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getBlockPos(source, POS_A), BlockPosArgument.getBlockPos(source, POS_B), source.getArgument(FUNCTION, EnumRoomFunction.class), StringArgumentType.getString(source, NAME), NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); }))))
								.then(newArgument(NAME, StringArgumentType.string())
									.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getBlockPos(source, POS_A), BlockPosArgument.getBlockPos(source, POS_B), null, StringArgumentType.getString(source, NAME), null, source.getSource()); })
									.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
										.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getBlockPos(source, POS_A), BlockPosArgument.getBlockPos(source, POS_B), null, StringArgumentType.getString(source, NAME), NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); })))
								.then(newArgument(NBT, NBTCompoundTagArgument.nbt())
										.executes((source) -> { return VariantRoomAdd.add(getSettlement(source), BlockPosArgument.getBlockPos(source, POS_A), BlockPosArgument.getBlockPos(source, POS_B), null, null, NBTCompoundTagArgument.getNbt(source, NBT), source.getSource()); }))));
    		}
    		
    		public static int addFromHand(CommandContext<CommandSource> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement == null)
    				return 0;
    			
    			try
    			{
    				Entity ent = source.getSource().assertIsEntity();
    				if(ent instanceof LivingEntity)
    				{
    					LivingEntity living = (LivingEntity)ent;
    					if(!living.getHeldItemMainhand().isEmpty() && living.getHeldItemMainhand().getItem() == VOItems.DRAFTING_TABLE)
    					{
    						ItemStack table = living.getHeldItemMainhand();
    						if(table.hasTag() && table.getTag().contains("BlockEntityTag", 10))
    						{
    							CompoundNBT tableData = table.getTag().getCompound("BlockEntityTag");
    							if(tableData.contains("Locked", 3) && tableData.getInt("Locked") == 15)
    							{
	    							if(!(living instanceof PlayerEntity && ((PlayerEntity)living).isCreative()))
	    								table.shrink(1);
	    							
	    							BoxRoom room = TileEntityDraftingTable.getRoomFromNBT(tableData);
	    							return add(settlement, room, source.getSource());
    							}
    						}
    					}
    				}
    			}
    			catch(Exception e){ }
				source.getSource().sendFeedback(makeErrorMessage(translationSlug+"room.add.failed"), false);
    			return 0;
    		}
    		
    		public static int add(Settlement settlement, BoxRoom room, CommandSource source)
    		{
    			if(settlement != null && room != null)
    			{
	    			settlement.addRoom(room);
	    			
	    			SettlementManager manager = SettlementManager.get(source.getWorld());
	    			manager.add(manager.getIndexBySettlement(settlement), settlement);
	    			source.sendFeedback(new TranslationTextComponent(translationSlug+"room.add.success", room.hasCustomName() ? room.getName() : settlement.getIndexFromRoom(room)), true);
	    			return 15;
    			}
    			return 0;
    		}
    		
    		public static int add(Settlement settlement, BlockPos posA, BlockPos posB, EnumRoomFunction function, String name, CompoundNBT nbt, CommandSource source)
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
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("remove")
    					.then(newArgument(ROM_INDEX, IntegerArgumentType.integer(0))
							.executes(VariantRoomRemove::removeIndex))
    					.then(newArgument(ROM_NAME, StringArgumentType.string())
    						.executes(VariantRoomRemove::removeName));
    		}
    		
    		public static int remove(Settlement settlement, BoxRoom room, SettlementManager manager, CommandSource source)
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
    		
    		public static int removeIndex(CommandContext<CommandSource> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement == null) return 0;
    			
				int index = IntegerArgumentType.getInteger(source, ROM_INDEX);
	    		int result = remove(settlement, getRoom(index, settlement), SettlementManager.get(source.getSource().getWorld()), source.getSource());
	    		if(result == 0)
	    			notifyUnknownRoom(String.valueOf(index), source.getSource());
	    		else
	    			source.getSource().sendFeedback(new TranslationTextComponent(translationSlug+"room.remove.success", index), true);
	    		return result;
    		}
    		
    		public static int removeName(CommandContext<CommandSource> source)
    		{
    			Settlement settlement = getSettlement(source);
    			if(settlement == null) return 0;
    			
    			String name = StringArgumentType.getString(source, ROM_NAME);
	    		int result = remove(settlement, getRoom(name, settlement), SettlementManager.get(source.getSource().getWorld()), source.getSource());
	    		if(result == 0)
	    			notifyUnknownRoom(name, source.getSource());
	    		else
	    			source.getSource().sendFeedback(new TranslationTextComponent(translationSlug+"room.remove.success", name), true);
	    		return result;
    		}
    	}
    	
    	private static class VariantRoomEdit
    	{
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("edit")
    					.then(newArgument(ROM_INDEX, IntegerArgumentType.integer(0))
    						.then(newArgument("nbt", NBTCompoundTagArgument.nbt())
    							.executes(VariantRoomEdit::editIndex)))
    					.then(newArgument(ROM_NAME, StringArgumentType.string())
        					.then(newArgument("nbt", NBTCompoundTagArgument.nbt())
        						.executes(VariantRoomEdit::editName)));
    		}
    		
    		public static int edit(BoxRoom room, CompoundNBT newNBT, Settlement settlement, CommandSource source)
    		{
                CompoundNBT roomNBT = room.writeToNBT(new CompoundNBT());
                CompoundNBT originalNBT = roomNBT.copy();
                roomNBT.merge(newNBT);
                if(roomNBT.equals(originalNBT))
                {
                	source.sendFeedback(makeErrorMessage(translationSlug+"room.edit.failed", roomNBT.toString()), true);
                	return 0;
                }
                else
                {
                	room.readFromNBT(roomNBT);
                	settlement.addRoom(settlement.getIndexFromRoom(room), room);
                	SettlementManager manager = SettlementManager.get(source.getWorld());
                	manager.add(manager.getIndexBySettlement(settlement), settlement);
                	
                	source.sendFeedback(new TranslationTextComponent(translationSlug+"room.edit.success", roomNBT.toString()), true);
                	return 15;
                }
    		}
    		
    		public static int editIndex(CommandContext<CommandSource> source)
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
    				return edit(room, NBTCompoundTagArgument.getNbt(source, "nbt"), settlement, source.getSource());
    			}
    			return 0;
    		}
    		
    		public static int editName(CommandContext<CommandSource> source)
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
    				return edit(room, NBTCompoundTagArgument.getNbt(source, "nbt"), settlement, source.getSource());
    			}
    			return 0;
    		}
    	}
    	
    	private static class VariantRoomMove
    	{
    		public static LiteralArgumentBuilder<CommandSource> build()
    		{
    			return newLiteral("move")
    					.then(newArgument(ROM_INDEX, IntegerArgumentType.integer(0))
    						.then(newArgument(POS_A, BlockPosArgument.blockPos())
    	    					.then(newArgument(POS_B, BlockPosArgument.blockPos())
    	    						.executes(VariantRoomMove::moveIndex))))
    					.then(newArgument(ROM_NAME, IntegerArgumentType.integer(0))
    						.then(newArgument(POS_A, BlockPosArgument.blockPos())
    	    					.then(newArgument(POS_B, BlockPosArgument.blockPos())
    	    						.executes(VariantRoomMove::moveName))));
    		}
    		
    		public static int move(int index, BoxRoom room, Settlement settlement, BlockPos posA, BlockPos posB, CommandSource source)
    		{
    			if(settlement != null && room != null && posA != null && posB != null)
    			{
    				room.set(posA, posB);
    				settlement.addRoom(index, room);
    				SettlementManager manager = SettlementManager.get(source.getWorld());
    				manager.add(manager.getIndexBySettlement(settlement), settlement);
    				
    				BlockPos roomMin = room.min();
    				BlockPos roomMax = room.max();
    				source.sendFeedback(new TranslationTextComponent(translationSlug+"room.move.success", room.hasCustomName() ? room.getName() : index, roomMin.getX(), roomMin.getY(), roomMin.getZ(), roomMax.getX(), roomMax.getY(), roomMax.getZ()), true);
    				return 15;
    			}
    			return 0;
    		}
    		
    		public static int moveIndex(CommandContext<CommandSource> source)
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
    		
    		public static int moveName(CommandContext<CommandSource> source)
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
