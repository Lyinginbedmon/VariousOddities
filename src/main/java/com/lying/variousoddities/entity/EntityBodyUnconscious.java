package com.lying.variousoddities.entity;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.init.VOEntities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class EntityBodyUnconscious extends AbstractBody
{
	public EntityBodyUnconscious(EntityType<? extends EntityBodyUnconscious> type, World worldIn)
	{
		super(type, worldIn);
	}
    
    public static boolean canSpawnAt(EntityType<?> animal, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
	    return true;
    }
	
	@Nullable
	public static EntityBodyUnconscious createBodyFrom(@Nonnull LivingEntity living)
	{
		if(living == null) return null;
		EntityBodyUnconscious body = new EntityBodyUnconscious(VOEntities.BODY, living.getEntityWorld());
		body.copyFrom(living, false);
		body.setSoulUUID(living.getUniqueID());
		
		if(living.getType() == EntityType.PLAYER)
		{
			PlayerEntity player = (PlayerEntity)living;
			PlayerData.forPlayer(player).setBodyUUID(body.getUniqueID());
		}
		
		return body;
	}
	
	public static EntityBodyUnconscious getBodyFromEntity(@Nonnull LivingEntity living)
	{
		World world = living.getEntityWorld();
		for(EntityBodyUnconscious body : world.getEntitiesWithinAABB(EntityBodyUnconscious.class, AbstractBody.ENTIRE_WORLD))
			if(body.getSoulUUID().equals(living.getUniqueID()))
				return body;
		return null;
	}
	
	public boolean shouldBindIfPersistent(){ return true; }
	
	public void setBody(@Nullable LivingEntity living)
	{
		CompoundNBT data = new CompoundNBT();
		
		// Store entity equipment in body inventory
		if(living != null)
		{
			setSoulUUID(living.getUniqueID());
			
			living.writeWithoutTypeId(data);
			if(living.getType() == EntityType.PLAYER)
			{
				data.putString("id", "player");
				getDataManager().set(PROFILE, NBTUtil.writeGameProfile(new CompoundNBT(), ((PlayerEntity)living).getGameProfile()));
			}
			else
				data.putString("id", living.getEntityString());
			
			if(data.contains("Passengers"))
				data.remove("Passengers");
		}
		else
		{
			this.bodyInventory.clear();
			setSoulUUID(null);
		}
		
		getDataManager().set(ENTITY, data);
		updateSize();
		onInventoryChanged(null);
	}
	
	public LivingEntity getBody()
	{
		for(LivingEntity living : getEntityWorld().getLoadedEntitiesWithinAABB(LivingEntity.class, ENTIRE_WORLD))
			if(living.getUniqueID() == this.getSoulUUID())
				return living;
		
		return super.getBody();
	}
	
    public boolean isNoDespawnRequired(){ return true; }
	
	protected void dropInventory(){ }
	
	// FIXME Unconscious bodies despawning should respawn their associated mob, if any
	public void tick()
	{
		super.tick();
		
		if(hasBody())
		{
			if(isPlayer())
			{
				LivingEntity soul = getSoul();
//				if(soul == null)
//				{
//					if(!isPlayer())
//						onKillCommand();
//				}
//				else
//					moveWithinRangeOf(this, soul, PlayerData.forPlayer((PlayerEntity)soul).getSoulCondition().getWanderRange());
				
				// If the player is online and not unconscious, remove body
				if(!PlayerData.isPlayerBodyAsleep(soul))
					this.onKillCommand();
				
				return;
			}
			else
			{
				LivingEntity body = getBody();
				LivingData bodyData = LivingData.forEntity(body);
				if(!body.isAlive() || !bodyData.isUnconscious())
				{
					respawnMob(body);
					return;
				}
				
				body.tick();
				setBody(body);
			}
		}
	}
	
	public void respawnMob(LivingEntity body)
	{
		body.setPosition(getPosX(), getPosY(), getPosZ());
		getEntityWorld().addEntity(body);
		remove();
	}
	
	public boolean attackEntityFrom(DamageSource cause, float amount)
	{
		if(cause != DamageSource.OUT_OF_WORLD)
			if(hasBody() && getSoul() != null)
			{
				if(isPlayer())
					getSoul().attackEntityFrom(cause, amount);
				else
				{
					LivingEntity body = getBody();
					body.attackEntityFrom(cause, amount);
					setBody(body);
				}
			}
		return super.attackEntityFrom(cause, amount);
	}
	
	public boolean addPotionEffect(EffectInstance effectInstanceIn)
	{
		if(hasBody() && getSoul() != null)
		{
			if(isPlayer())
				getSoul().addPotionEffect(effectInstanceIn);
			else
			{
				LivingEntity body = getBody();
				body.addPotionEffect(effectInstanceIn);
				setBody(body);
			}
		}
		return false;
	}
	
	public void onKillCommand()
	{
		super.onKillCommand();
		if(!isPlayer() && getBody() != null && !getEntityWorld().isRemote)
		{
			LivingEntity body = getBody();
			body.setPosition(getPosX(), getPosY(), getPosZ());
			
			LivingData data = LivingData.forEntity(body);
			data.setBludgeoning(0F);
			
			getEntityWorld().addEntity(body);
			body.onKillCommand();
		}
	}
}
