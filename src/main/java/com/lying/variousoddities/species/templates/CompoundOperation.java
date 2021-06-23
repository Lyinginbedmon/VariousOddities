package com.lying.variousoddities.species.templates;

import java.util.List;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

/** A template operation containing a set of sub operations */
public class CompoundOperation extends TemplateOperation
{
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "compound");
	
	private List<TemplateOperation> subOperations = Lists.newArrayList();
	
	public CompoundOperation()
	{
		super(Operation.SET);
	}
	
	public ResourceLocation getRegistryName(){ return REGISTRY_NAME; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		ListNBT operationList = new ListNBT();
		for(TemplateOperation operation : subOperations)
			operationList.add(operation.write(new CompoundNBT()));
		compound.put("Operations", operationList);
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		// TODO Auto-generated method stub
		ListNBT operationList = compound.getList("Operations", 10);
		for(int i=0; i<operationList.size(); i++)
		{
			
		}
	}
	
	public CompoundOperation addOperation(TemplateOperation operationIn){ this.subOperations.add(operationIn); return this; }
	
	public static class Builder extends TemplateOperation.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public CompoundOperation create()
		{
			return new CompoundOperation();
		}
	}
}
