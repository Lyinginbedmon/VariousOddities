package com.lying.variousoddities.entity.passive;

import com.lying.variousoddities.entity.AbstractGoblinWolf;
import com.lying.variousoddities.entity.ai.passive.EntityAIWorgFetch;
import com.lying.variousoddities.entity.ai.passive.EntityAIWorgSpook;
import com.lying.variousoddities.entity.mount.EntityWarg;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityWorg extends AbstractGoblinWolf
{
	private static final EntityDataAccessor<Boolean> SPOOKED = SynchedEntityData.defineId(EntityWorg.class, EntityDataSerializers.BOOLEAN);
	
	public EntityWorg(EntityType<? extends EntityWorg> type, Level worldIn)
	{
		super(type, worldIn);
	}
	
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		getEntityData().define(SPOOKED, false);
	}

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMobAttributes()
        		.add(Attributes.MAX_HEALTH, 30.0D)
        		.add(Attributes.ARMOR, 4.0D)
        		.add(Attributes.MOVEMENT_SPEED, (double)0.3F)
        		.add(Attributes.ATTACK_DAMAGE, 10.0D);
    }
    
	public void registerGoals()
	{
		super.registerGoals();
		
		this.goalSelector.addGoal(1, new EntityAIWorgSpook(this, 1.0D));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
		this.goalSelector.addGoal(2, new EntityAIWorgFetch(this, 6D));
		this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
		
	}
    
	public void getAggressiveBehaviours()
	{
		this.addGeneticAI(3, new NearestAttackableTargetGoal<Chicken>(this, Chicken.class, true));
	}
	
    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.4F;
    }
	
    public void spook(){ getEntityData().set(SPOOKED, true); }
    
    public boolean isSpooked(){ return getEntityData().get(SPOOKED).booleanValue(); }
    
    public void unSpook(){ getEntityData().set(SPOOKED, false); }
    
    public InteractionResult mobInteract(Player player, InteractionHand hand)
    {
    	ItemStack heldItem = player.getItemInHand(hand);
    	if(getLevel().isClientSide)
    	{
    		boolean flag = this.isOwnedBy(player) || this.isTame() || heldItem.getItem() == Items.BONE && !this.isTame() && getTarget() == null;
    		return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
    	}
    	else
    	{
	    	if(isTame())
			{
				if(isFoodItem(heldItem) && getHealth() < getMaxHealth())
	    		{
	    			heal(heldItem.getItem().getFood().getHealing());
		    		if(!player.isCreative())
		    			heldItem.shrink(1);
		    		
		    		return InteractionResult.sidedSuccess(this.level.isClientSide);
		    	}
				
				if(heldItem.getItem() == Items.BONE && getAge() == 0)
				{
	    			setInLove(player);
	    			
	    			if(!player.isCreative())
	    				heldItem.shrink(1);
	    			
		    		return InteractionResult.sidedSuccess(this.level.isClientSide);
				}
				
				if(this.isOwnedBy(player))
	    		{
					if(player.isCrouching())
					{
						this.setOrderedToSit(!this.isOrderedToSit());
						return InteractionResult.SUCCESS;
					}
					else
					{
						player.setItemInHand(hand, getMainHandItem());
						setItemInHand(InteractionHand.MAIN_HAND, heldItem);
					}
	    		}
			}
	    	else if(isTameable() && heldItem.getItem() == Items.BONE && this.getTarget() == null)
	    	{
	    		if(!player.isCreative())
	    			heldItem.shrink(1);
	    		
	    		// Taming
	    		if(getRandom().nextInt(5) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player))
	    		{
	    			this.tame(player);
	    			this.navigation.stop();
	    			this.setTarget(null);
	    			this.setOrderedToSit(true);
	    			this.getLevel().broadcastEntityEvent(this, (byte)7);
	    		}
	    		else
	    			this.getLevel().broadcastEntityEvent(this, (byte)6);
	    		
	    		return InteractionResult.SUCCESS;
	    	}
	    	
	    	return super.mobInteract(player, hand);
    	}
    }
    
	public AgeableMob getBreedOffspring(ServerLevel arg0, AgeableMob arg1)
	{
		if(arg1.getType() == VOEntities.WORG)
		{
			EntityWorg worg2 = (EntityWorg)arg1;
			Genetics genesA = getGenetics();
			Genetics genesB = worg2.getGenetics();
			
			Genetics genesC = Genetics.cross(genesA, genesB, getRandom());
			
			AbstractGoblinWolf offspring;
			if(genesC.gene(6) == false && genesC.gene(7) == false)
			{
				EntityWarg warg = VOEntities.WARG.create(arg0);
				warg.setGenetics(genesC);
				offspring = warg;
			}
			else
			{
				EntityWorg worg = VOEntities.WORG.create(arg0);
				worg.setGenetics(genesC);
				offspring = worg;
			}
			
			offspring.setColor(getRandom().nextBoolean() ? getColor() : worg2.getColor());
			return offspring;
		}
		return null;
	}
}
