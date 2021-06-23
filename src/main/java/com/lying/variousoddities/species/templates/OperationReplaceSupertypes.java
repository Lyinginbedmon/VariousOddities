package com.lying.variousoddities.species.templates;

import com.lying.variousoddities.species.types.EnumCreatureType;

public class OperationReplaceSupertypes extends CompoundOperation
{
	public OperationReplaceSupertypes(EnumCreatureType... typesIn)
	{
		super();
		addOperation(new TypeOperation(Operation.REMOVE_ALL, true));
		addOperation(new TypeOperation(Operation.ADD, typesIn));
	}
}
