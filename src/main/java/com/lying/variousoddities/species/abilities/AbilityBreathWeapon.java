package com.lying.variousoddities.species.abilities;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityBreathWeapon extends ActivatedAbility
{
	private BreathType type;
	private double distance;
	private ParticleOptions particle = ParticleTypes.FLAME;
	
	private DamageType damage;
	private Pair<Float, Float> dmgAmount;
	private BlockState blockToPlace = Blocks.AIR.defaultBlockState();
	
	private int duration = Reference.Values.TICKS_PER_SECOND * 3;
	
	public AbilityBreathWeapon(DamageType damageIn, BreathType typeIn, double dist, Pair<Float, Float> dmgIn)
	{
		super(Reference.Values.TICKS_PER_MINUTE);
		this.damage = damageIn;
		this.type = typeIn;
		this.distance = dist;
		this.dmgAmount = dmgIn;
	}
	
	public AbilityBreathWeapon(DamageType damageIn, BreathType typeIn, double dist, float dmgMin, float dmgMax)
	{
		this(damageIn, typeIn, dist, Pair.of(dmgMin, dmgMax));
	}
	
	// TODO Add comparator function for breath weapons
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "breath_weapon_"+damage.getSerializedName()); }
	
	public Component translatedName(){ return Component.translatable("ability."+Reference.ModInfo.MOD_ID+".breath_weapon", (int)distance, type.translated(damage)); }
	
	public Component description(){ return Component.translatable("ability.varodd:breath_weapon.desc", damage.getTranslated()); }
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public AbilityBreathWeapon setParticle(ParticleOptions particleIn){ this.particle = particleIn; return this; }
	
	public AbilityBreathWeapon setBlock(BlockState state){ this.blockToPlace = state; return this; }
	
	public Type getType(){ return Ability.Type.ATTACK; }
	
	public CompoundTag writeToNBT(CompoundTag compound)
	{
		super.writeToNBT(compound);
		compound.putString("Type", this.type.getSerializedName());
		compound.putInt("Duration", this.duration);
		compound.putDouble("Distance", this.distance);
		
		CompoundTag damageData = new CompoundTag();
			damageData.putString("Type", damage.getSerializedName());
			if(dmgAmount.getLeft() == dmgAmount.getRight())
				damageData.putFloat("Amount", dmgAmount.getLeft());
			else
			{
				damageData.putFloat("Min", Math.min(dmgAmount.getLeft(), dmgAmount.getRight()));
				damageData.putFloat("Max", Math.max(dmgAmount.getLeft(), dmgAmount.getRight()));
			}
			if(blockToPlace.getBlock() != Blocks.AIR)
				damageData.put("BlockToPlace", NbtUtils.writeBlockState(this.blockToPlace));
		compound.put("Damage", damageData);
		
		compound.putString("Particle", this.particle.writeToString());
		return compound;
	}
	
	public void readFromNBT(CompoundTag compound)
	{
		super.readFromNBT(compound);
		this.type = compound.contains("Type", 8) ? BreathType.fromString(compound.getString("Type")) : BreathType.CONE;
		this.duration = compound.contains("Duration", 3) ? compound.getInt("Duration") : Reference.Values.TICKS_PER_SECOND * 6;
		this.distance = compound.contains("Distance", 6) ? compound.getDouble("Distance") : 8.0D;
		
		CompoundTag damageData = compound.getCompound("Damage");
		this.damage = damageData.contains("Type", 8) ? DamageType.fromString(damageData.getString("Type")) : DamageType.FIRE;
		if(damageData.contains("Amount", 5))
			this.dmgAmount = Pair.of(damageData.getFloat("Amount"), damageData.getFloat("Amount"));
		else if(damageData.contains("Min", 5))
			this.dmgAmount = Pair.of(damageData.getFloat("Min"), damageData.getFloat("Max"));
		else
			this.dmgAmount = Pair.of(1F, 8F);
		
		if(damageData.contains("BlockToPlace", 10))
		{
			this.blockToPlace = NbtUtils.readBlockState(damageData.getCompound("BlockToPlace"));
			if(this.blockToPlace == null)
				this.blockToPlace = Blocks.AIR.defaultBlockState();
		}
		
		if(compound.contains("Particle", 8))
		{
			try
			{
				this.particle = ParticleArgument.readParticle(new StringReader(compound.getString("Particle")));
			}
			catch(CommandSyntaxException e)
			{
				VariousOddities.log.warn("Couldn't load custom particle {}", compound.getString("Particle"), e);
				this.particle = ParticleTypes.FLAME;
			}
		}
	}
	
	public boolean canTrigger(LivingEntity entity)
	{
		if(super.canTrigger(entity) && !isActive())
		{
			/* Breath weapons cannot be used whilst another is active */
			for(Ability ability : AbilityRegistry.getAbilitiesOfType(entity, getRegistryName()))
				if(ability.getMapName().equals(this.getMapName()))
					continue;
				else if(((AbilityBreathWeapon)ability).isActive())
					return false;
			
			return true;
		}
		return false;
	}
	
	public void trigger(LivingEntity entity, Dist side)
	{
		switch(side)
		{
			case CLIENT:
				break;
			default:
				this.activeTicks = this.duration;
				putOnCooldown(entity);
				break;
		}
	}
	
	public void addListeners(IEventBus bus)
	{
		bus.addListener(this::performBreathWeapon);
	}
	
	public void performBreathWeapon(LivingTickEvent event)
	{
		LivingEntity entity = event.getEntity();
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(entity, getRegistryName()))
		{
			AbilityBreathWeapon breath = (AbilityBreathWeapon)ability;
			if(breath.isActive())
			{
				Level world = entity.getLevel();
				Vec3 eyePos = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
				Vec3 direction = entity.getLookAngle();
				if(world.isClientSide)
				{
					breath.type.makeParticles(world, breath.particle, eyePos, direction, breath.distance);
					if(breath.activeTicks%Reference.Values.TICKS_PER_SECOND == 0)
						entity.playSound(SoundEvents.ENDER_DRAGON_SHOOT, 1F, 0.5F + entity.getRandom().nextFloat() * 0.5F);
				}
				else
				{
					List<UUID> ignoreList = Lists.newArrayList(entity.getUUID());
					
					if(breath.activeTicks-- > 0)
						switch(breath.type)
						{
							case CONE:
								for(int i=0; i<entity.getRandom().nextInt(4) + 1; i++)
								{
									Vec3 dir = direction.xRot((float)Math.toRadians(entity.getRandom().nextInt(10) - 5)).yRot((float)Math.toRadians(entity.getRandom().nextInt(90) - 45));
									ignoreList.addAll(shootLine(world, eyePos, dir, breath, entity, activeTicks%Reference.Values.TICKS_PER_SECOND == 0, ignoreList));
								}
								break;
							case LINE:
								shootLine(world, eyePos, direction, breath, entity, activeTicks%Reference.Values.TICKS_PER_SECOND == 0, ignoreList);
								break;
						}
					
					breath.markForUpdate(entity);
				}
			}
		}
	}
	
	public List<UUID> shootLine(Level world, Vec3 eyePos, Vec3 direction, AbilityBreathWeapon breath, LivingEntity entity, boolean damage, List<UUID> ignoreEntities)
	{
		double distance = breath.distance;
		Vec3 maxPos = eyePos.add(direction.multiply(new Vec3(distance, distance, distance)));
		HitResult trace = world.clip(new ClipContext(eyePos, maxPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null));
		if(trace.getType() != HitResult.Type.MISS)
			distance = eyePos.distanceTo(trace.getLocation());
		
		if(trace.getType() == HitResult.Type.BLOCK && breath.blockToPlace.getBlock() != Blocks.AIR)
		{
			if(world.random.nextInt(10) == 0)
			{
				BlockHitResult traceResult = (BlockHitResult)trace;
				BlockPos hitPos = traceResult.getBlockPos();
				tryToPlaceBlock(world, hitPos, breath.blockToPlace, traceResult);
			}
		}
		
		List<UUID> hits = Lists.newArrayList();
		if(damage)
			for(LivingEntity hit : world.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(distance)))
			{
				if(hit == entity || ignoreEntities.contains(hit.getUUID()) || !canAbilityAffectEntity(hit, entity))
					continue;
				
				double distFromEye = eyePos.distanceTo(new Vec3(hit.getX(), hit.getY() + (hit.getBbHeight() / 2), hit.getZ()));
				Vec3 posAtHit = eyePos.add(direction.multiply(distFromEye, distFromEye, distFromEye));
				if(hit.getBoundingBox().contains(posAtHit))
				{
					// Damage hit entity
					float amount = Math.min(breath.dmgAmount.getLeft(), breath.dmgAmount.getRight());
					float range = Math.max(breath.dmgAmount.getLeft(), breath.dmgAmount.getRight()) - amount;
					amount += entity.getRandom().nextFloat() * range;
					
					amount *= Math.max(0.1F, 1 - ((hit.distanceTo(entity)) / breath.distance));
					
					DamageSource source = null;
					switch(breath.damage)
					{
						case ACID:		source = VODamageSource.ACID; break;
						case COLD:		source = VODamageSource.COLD; break;
						case EVIL:		source = VODamageSource.EVIL; break;
						case FALLING:	source = DamageSource.FALL; break;
						case FIRE:		source = DamageSource.IN_FIRE; break;
						case FORCE:		source = VODamageSource.FORCE; break;
						case HOLY:		source = VODamageSource.HOLY; break;
						case LIGHTNING:	source = DamageSource.LIGHTNING_BOLT; break;
						case MAGIC:		source = DamageSource.MAGIC; break;
						case POISON:	source = VODamageSource.POISON; break;
						case PSYCHIC:	source = VODamageSource.PSYCHIC; break;
						case SONIC:		source = VODamageSource.SONIC; break;
						default:
							source = DamageSource.mobAttack(entity);
							break;
					}
					if(source != null)
						hit.hurt(source, amount);
					
					hits.add(hit.getUUID());
				}
			}
		
		return hits;
	}
	
	@SuppressWarnings("deprecation")
	public boolean tryToPlaceBlock(Level world, BlockPos pos, BlockState stateToPlace, BlockHitResult trace)
	{
		if(!world.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))
			return false;
		
		BlockState stateAtPos = world.getBlockState(pos);
		ItemStack itemStack = new ItemStack(Item.byBlock(stateToPlace.getBlock()));
		BlockPlaceContext context = new BlockPlaceContext(new BlockPlaceContext(world, (Player)null, InteractionHand.MAIN_HAND, itemStack, trace));
		
		// Try to place at the block hit
		if(stateAtPos.canBeReplaced(context))
		{
			world.setBlock(pos, stateToPlace, 11);
			return true;
		}
		
		// Try to place at the neighbour of the block hit in the direction of the hit
		pos = pos.relative(trace.getDirection());
		stateAtPos = world.getBlockState(pos);
		if(stateAtPos.canBeReplaced(context))
		{
			world.setBlock(pos, stateToPlace, 11);
			return true;
		}
		return false;
	}
	
	public static enum BreathType implements StringRepresentable
	{
		LINE,
		CONE;
		
		public String getSerializedName(){ return name().toLowerCase(); }
		public static BreathType fromString(String str)
		{
			for(BreathType type : values())
				if(type.getSerializedName().equalsIgnoreCase(str))
					return type;
			return CONE;
		}
		
		public Component translated(DamageType type)
		{
			return Component.translatable("ability."+Reference.ModInfo.MOD_ID+".breath_weapon."+getSerializedName(), type.getTranslated());
		}
		
		public void makeParticles(Level world, ParticleOptions particleType, Vec3 origin, Vec3 direction, double distance)
		{
			if(distance <= 0D || !world.isClientSide)
				return;
			
			RandomSource random = world.random;
			switch(this)
			{
				case LINE:
					Vec3 maxPos = origin.add(direction.multiply(new Vec3(distance, distance, distance)));
					HitResult trace = world.clip(new ClipContext(origin, maxPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null));
					if(trace.getType() != HitResult.Type.MISS)
						distance = origin.distanceTo(trace.getLocation());
					
					for(int i=0; i<world.random.nextInt(Math.max(6, 20 * (int)(distance / 6D))); i++)
					{
						double mult = random.nextDouble() * distance;
						Vec3 pos = origin.add(direction.multiply(mult, mult, mult));
						double speed = 0.05D;
						world.addParticle(particleType, true, pos.x, pos.y, pos.z, (random.nextDouble() - 0.5D) * speed, (random.nextDouble() - 0.5D) * speed, (random.nextDouble() - 0.5D) * speed);
					}
					break;
				case CONE:
					for(int i=0; i<random.nextInt(4) + 1; i++)
					{
						Vec3 dir = direction.xRot((float)Math.toRadians(random.nextInt(10) - 5)).yRot((float)Math.toRadians(random.nextInt(90) - 45));
						LINE.makeParticles(world, particleType, origin, dir, distance);
					}
					break;
			}
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(); }
		
		public Ability create(CompoundTag compound)
		{
			BreathType type = compound.contains("Type", 8) ? BreathType.fromString(compound.getString("Type")) : BreathType.CONE;
			double distance = compound.contains("Distance", 6) ? compound.getDouble("Distance") : 8.0D;
			
			CompoundTag damageData = compound.getCompound("Damage");
			DamageType damage = damageData.contains("Type", 8) ? DamageType.fromString(damageData.getString("Type")) : DamageType.FIRE;
			Pair<Float, Float> dmgAmount;
			if(damageData.contains("Amount", 5))
				dmgAmount = Pair.of(damageData.getFloat("Amount"), damageData.getFloat("Amount"));
			else if(damageData.contains("Min", 5))
				dmgAmount = Pair.of(damageData.getFloat("Min"), damageData.getFloat("Max"));
			else
				dmgAmount = Pair.of(1F, 8F);
			
			AbilityBreathWeapon weapon = new AbilityBreathWeapon(damage, type, distance, dmgAmount);
			
			if(compound.contains("Cooldown", 3))
				weapon.cooldown = compound.getInt("Cooldown");
			
			if(compound.contains("Active", 3))
				weapon.activeTicks = compound.getInt("Active");
			
			if(damageData.contains("BlockToPlace", 10))
				weapon.setBlock(NbtUtils.readBlockState(damageData.getCompound("BlockToPlace")));
			
			if(compound.contains("Particle", 8))
			{
				ParticleOptions particle = null;
				try
				{
					particle = ParticleArgument.readParticle(new StringReader(compound.getString("Particle")));
				}
				catch(CommandSyntaxException e){ }
				if(particle != null)
					weapon.setParticle(particle);
			}
			
			return weapon;
		}
	}
}
