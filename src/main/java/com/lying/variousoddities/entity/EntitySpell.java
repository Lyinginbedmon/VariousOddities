package com.lying.variousoddities.entity;

import java.util.UUID;

import com.lying.variousoddities.item.ItemSpellContainer;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.MagicEffects;
import com.lying.variousoddities.world.savedata.SpellManager;
import com.lying.variousoddities.world.savedata.SpellManager.SpellData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Host entity for ongoing magical effects
 * @author Lying
 *
 */
public class EntitySpell extends Entity
{
    private static final EntityDataAccessor<ItemStack> ITEM				= SynchedEntityData.defineId(EntitySpell.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> SPELL_ID			= SynchedEntityData.defineId(EntitySpell.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> SPELLDATA	= SynchedEntityData.defineId(EntitySpell.class, EntityDataSerializers.COMPOUND_TAG);
    
    public EntitySpell(EntityType<? extends EntitySpell> typeIn, Level worldIn)
    {
    	super(typeIn, worldIn);
//		setSize(0.5F, 0.5F);
//		setItem(new ItemStack(VOItems.SPELL_SCROLL));
    }
    
	public EntitySpell(EntityType<EntitySpell> typeIn, Level worldIn, double x, double y, double z)
	{
		super(typeIn, worldIn);
		setPos(x, y, z);
	}
	
	public EntitySpell(EntityType<EntitySpell> typeIn, Level worldIn, ItemStack stackIn, double x, double y, double z)
	{
		this(typeIn, worldIn, x, y, z);
		setItem(stackIn);
	}
	
	public IPacket<?> createSpawnPacket()
	{
		return new SSpawnObjectPacket(this);
	}
	
	protected void defineSynchedData()
	{
		getEntityData().define(ITEM, ItemStack.EMPTY);
		getEntityData().define(SPELL_ID, -1);
		getEntityData().define(SPELLDATA, new CompoundTag());
	}
	
	protected void readAdditional(CompoundTag compound)
	{
		ItemStack item = ItemStack.of(compound.getCompound("Item"));
        setItem(item);
        if(getItem().isEmpty())
    	{
        	setRemoved(Entity.RemovalReason.DISCARDED);
        	return;
    	}
        if(compound.contains("SpellID"))
        	setSpellID(compound.getInt("SpellID"));
        else if(!item.isEmpty() && getLevel() != null)
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
        	
        	Level level = getLevel();
        	setSpellID(SpellManager.get(level).registerNewSpell(spellData, level));
        }
	}
	
	protected void writeAdditional(CompoundTag compound)
	{
		compound.put("Item", this.getItem().save(new CompoundTag()));
		compound.putInt("SpellID", getSpellID());
	}
	
	public void setSpell(SpellData dataIn)
	{
		setSpellID(dataIn.getID());
		moveTo(dataIn.getPos().x, dataIn.getPos().y, dataIn.getPos().z, dataIn.yaw(), dataIn.pitch());
	}
	
	public void setSpellID(int par1Int)
	{
		getEntityData().set(SPELL_ID, par1Int);
	}
	
	public int getSpellID()
	{
		return getEntityData().get(SPELL_ID).intValue();
	}
	
	public SpellData getSpell()
	{
		return SpellManager.get(getLevel()).getSpellByID(getSpellID());
	}
	
    public boolean isNoDespawnRequired(){ return true; }
    
    public ItemStack getItem(){ return (ItemStack)getEntityData().get(ITEM); }
    public void setItem(ItemStack stack)
    {
    	getEntityData().set(ITEM, stack);
//    	getEntityData().setDirty(ITEM);
    }
	
    public ActionResultType processInitialInteract(Player player, InteractionHand hand)
    {
    	if(!player.getLevel().isClientSide)
    	{
    		SpellData data = SpellManager.get(getLevel()).getSpellByID(getSpellID());
        	if(data.castTime() <= 0) return ActionResultType.FAIL;
        	return data.dismiss(getLevel(), player) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
    	}
    	
    	return ActionResultType.FAIL;
    }
	
	public void tick()
	{
		super.tick();
		
		if(getLevel().isClientSide) return;
		
		SpellData data = getSpell();
		if(data == null || data.isDead())
			setRemoved(Entity.RemovalReason.DISCARDED);
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
        else if(this.level.isClientSide) return false;
        else if(source == DamageSource.OUT_OF_WORLD)
        {
            if(!this.isAlive()) return false;
            onDeath(source);
            setRemoved(Entity.RemovalReason.KILLED);
            return true;
        }
        
        return false;
    }
    
    public void onDeath(DamageSource cause)
    {
    	getLevel().setEntityState(this, (byte)3);
//		if(!getLevel().isClientSide)
//			PacketHandler.sendToNearby(new PacketSpellDispel(posX, posY + height / 2, posZ), getLevel(), this);
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
