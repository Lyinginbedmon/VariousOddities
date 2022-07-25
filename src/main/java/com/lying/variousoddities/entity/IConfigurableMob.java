package com.lying.variousoddities.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.utility.CompanionMarking.Mark;

import net.minecraft.world.entity.player.Player;

public interface IConfigurableMob
{
	public default boolean shouldRespondToPlayer(Player player) { return false; }
	
	public default boolean shouldRespondToMark(Mark mark, Object values) { return false; }
	
	public default List<Mark> allowedMarks(){ return Lists.newArrayList(Mark.GO_TO_POS); }
}
