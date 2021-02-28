package com.lying.variousoddities.entity;

import java.util.Random;
import java.util.UUID;

import com.lying.variousoddities.item.ItemSpellContainer;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.MagicEffects;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.SpellManager.SpellData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

/**
 * Host entity for ongoing magical effects
 * @author Lying
 *
 */
public class EntitySpell extends Entity
{
    private static final DataParameter<ItemStack> ITEM				= EntityDataManager.<ItemStack>createKey(EntitySpell.class, DataSerializers.ITEMSTACK);
    private static final DataParameter<Integer> SPELL_ID			= EntityDataManager.<Integer>createKey(EntitySpell.class, DataSerializers.VARINT);
    private static final DataParameter<CompoundNBT> SPELLDATA	= EntityDataManager.<CompoundNBT>createKey(EntitySpell.class, DataSerializers.COMPOUND_NBT);
    
    public EntitySpell(EntityType<? extends EntitySpell> typeIn, World worldIn)
    {
    	super(typeIn, worldIn);
//		setSize(0.5F, 0.5F);
//		setItem(new ItemStack(VOItems.SPELL_SCROLL));
    }
    
	public EntitySpell(EntityType<EntitySpell> typeIn, World worldIn, double x, double y, double z)
	{
		super(typeIn, worldIn);
		setPosition(x, y, z);
	}
	
	public EntitySpell(EntityType<EntitySpell> typeIn, World worldIn, ItemStack stackIn, double x, double y, double z)
	{
		this(typeIn, worldIn, x, y, z);
		setItem(stackIn);
	}
	
	public IPacket<?> createSpawnPacket()
	{
		return new SSpawnObjectPacket(this);
	}
	
	protected void registerData()
	{
		getDataManager().register(ITEM, ItemStack.EMPTY);
		getDataManager().register(SPELL_ID, -1);
		getDataManager().register(SPELLDATA, new CompoundNBT());
	}
    
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
	    return true;
    }
	
	protected void readAdditional(CompoundNBT compound)
	{
		ItemStack item = ItemStack.read(compound.getCompound("Item"));
        setItem(item);
        if(getItem().isEmpty())
    	{
        	setDead();
        	return;
    	}
        if(compound.contains("SpellID"))
        	setSpellID(compound.getInt("SpellID"));
        else if(!item.isEmpty() && getEntityWorld() != null)
        {
        	// Attempt to construct SpellData from old version in memory and initialise from that
        	IMagicEffect spell = MagicEffects.getSpellFromName(ItemSpellContainer.getSpellName(item));
        	
        	// If the stored item has no spell, we can't reconstruct it. Alas!
        	if(spell == null) return;
        	
        	SpellData spellData = new SpellData(spell, -1);
        	spellData.copyLocationAndAnglesFrom(this);
        	spellData.setCastTime(compound.getLong("SpawnTime"));
        	if(compound.contains("SpellData"))
        		spellData.setStorage(compound.getCompound("SpellData"));
        	if(compound.contains("Permanent"))
        		spellData.setPermanent(compound.getBoolean("Permanent"));
        	
        	String name = compound.contains("OwnerName") ? compound.getString("OwnerName") : "";
        	UUID uuid = compound.contains("OwnerUUIDMost") ? new UUID(compound.getLong("OwnerUUIDMost"), compound.getLong("OwnerUUIDLeast")) : null;
        	spellData.setCaster(name, uuid);
        	
        	World world = getEntityWorld();
        	setSpellID(SpellManager.get(world).registerNewSpell(spellData, world));
        }
	}
	
	protected void writeAdditional(CompoundNBT compound)
	{
		compound.put("Item", this.getItem().write(new CompoundNBT()));
		compound.putInt("SpellID", getSpellID());
	}
	
	public void setSpell(SpellData dataIn)
	{
		setSpellID(dataIn.getID());
		setPositionAndRotation(dataIn.getPos().x, dataIn.getPos().y, dataIn.getPos().z, dataIn.yaw(), dataIn.pitch());
	}
	
	public void setSpellID(int par1Int)
	{
		getDataManager().set(SPELL_ID, par1Int);
	}
	
	public int getSpellID()
	{
		return getDataManager().get(SPELL_ID).intValue();
	}
	
	public SpellData getSpell()
	{
		return SpellManager.get(getEntityWorld()).getSpellByID(getSpellID());
	}
	
    public boolean isNoDespawnRequired(){ return true; }
    
    public ItemStack getItem(){ return (ItemStack)getDataManager().get(ITEM); }
    public void setItem(ItemStack stack)
    {
    	getDataManager().set(ITEM, stack);
//    	getDataManager().setDirty(ITEM);
    }
	
    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand)
    {
    	if(!player.getEntityWorld().isRemote)
    	{
    		SpellData data = SpellManager.get(getEntityWorld()).getSpellByID(getSpellID());
        	if(data.castTime() <= 0) return ActionResultType.FAIL;
        	return data.dismiss(getEntityWorld(), player) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
    	}
    	
    	return ActionResultType.FAIL;
    }
	
	public void tick()
	{
		super.tick();
		
		if(getEntityWorld().isRemote) return;
		
		SpellData data = getSpell();
		if(data == null || data.isDead())
			this.setDead();
	}
    
    /**
     * Called by the /kill command.
     */
    public void onKillCommand()
    {
        this.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
    }
    
    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if(isInvulnerable()) return false;
        else if(this.world.isRemote) return false;
        else if(source == DamageSource.OUT_OF_WORLD)
        {
            if(!this.isAlive()) return false;
            onDeath(source);
            setDead();
            return true;
        }
        
        return false;
    }
    
    public void onDeath(DamageSource cause)
    {
    	getEntityWorld().setEntityState(this, (byte)3);
//		if(!getEntityWorld().isRemote)
//			PacketHandler.sendToNearby(new PacketSpellDispel(posX, posY + height / 2, posZ), getEntityWorld(), this);
    }
    
    protected boolean canTriggerWalking()
    {
        return false;
    }
    
    public boolean hasNoGravity()
    {
    	return true;
    }
}
