package com.lying.variousoddities.api.event;

import com.lying.variousoddities.world.savedata.SpellManager.SpellData;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class SpellEvent extends Event
{
	private final SpellData spellData;
	private final Level world;
	
	public SpellEvent(SpellData dataIn, Level worldIn)
	{
		spellData = dataIn;
		world = worldIn;
	}
	
	public SpellData getSpellData(){ return this.spellData; }
	public Level getWorld(){ return this.world; }
	
	/**
	 * Fired whenever a spell is cast, regardless of source or means<br>
     * This event is {@link Cancelable}.<br>
     * If this event is canceled, the spell is not cast and any materials used to cast it are wasted.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
	 * @author Lying<br>
	 *
	 */
    @Cancelable
	public static class SpellCastEvent extends SpellEvent
	{
		private LivingEntity caster;
		
		public SpellCastEvent(SpellData dataIn, Level worldIn)
		{
			super(dataIn, worldIn);
			this.caster = null;
		}
		public SpellCastEvent(SpellData dataIn, Level worldIn, LivingEntity casterIn)
		{
			this(dataIn, worldIn);
			this.caster = casterIn;
		}
		
		public boolean hasCaster(){ return this.caster != null; }
		public LivingEntity getCaster(){ return this.caster; }
	}
	
	/**
	 * Fired whenever a spell tries to affect an entity, such as an AoE damage spell or a buff spell<br>
     * This event is {@link Cancelable}.<br>
     * If this event is canceled, the spell does not affect the target entity.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
	 * @author Lying<br>
	 *
	 */
    @Cancelable
	public static class SpellAffectEntityEvent extends SpellEvent
	{
		private final Entity targetEntity;
		
		public SpellAffectEntityEvent(SpellData dataIn, Level worldIn, Entity targetIn)
		{
			super(dataIn, worldIn);
			targetEntity = targetIn;
		}
		
		public Entity getTarget(){ return this.targetEntity; }
	}
    
    @Cancelable
    public static class SpellAffectSpellEvent extends SpellEvent
    {
    	private final SpellData targetSpell;
    	
    	public SpellAffectSpellEvent(SpellData dataIn, Level worldIn, SpellData targetIn)
    	{
    		super(dataIn, worldIn);
    		targetSpell = targetIn;
    	}
    	
    	public SpellData getTarget(){ return this.targetSpell; }
    }
    
    /**
     * Fired whenever a EntitySpell would die due to its spell expiring.<br>
     * This event is {@link Cancelable}.<br>
     * If this event is canceled, the EntitySpell does not die.<br>
     * This event does not have a result. {@link HasResult}<br>
     * @author Lying
     *
     */
    @Cancelable
    public static class SpellExpireEvent extends SpellEvent
    {
    	public SpellExpireEvent(SpellData dataIn, Level worldIn)
    	{
    		super(dataIn, worldIn);
    	}
    }
    
    /**
     * Fired whenever a EntitySpell would die due to dispel magic or dismissal.<br>
     * This event is {@link Cancelable}.<br>
     * If this event is canceled, the EntitySpell does not die.<br>
     * This event does not have a result. {@link HasResult}<br>
     * @author Lying
     *
     */
    @Cancelable
    public static class SpellCancelEvent extends SpellEvent
    {
    	public SpellCancelEvent(SpellData dataIn, Level worldIn)
    	{
    		super(dataIn, worldIn);
    	}
    }
    
    public static class SpellResistanceEvent extends EntityEvent
    {
    	private int SR;
    	
    	public SpellResistanceEvent(Entity livingIn, int resistanceIn)
    	{
    		super(livingIn);
    		this.SR = resistanceIn;
    	}
    	
    	public int getSR(){ return this.SR; }
    	
    	public void setSR(int par1Int){ this.SR = Math.max(0, par1Int); }
    }
}
