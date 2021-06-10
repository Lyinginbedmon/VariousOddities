package com.lying.variousoddities.species.abilities;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.init.VODamageSource;
import com.lying.variousoddities.reference.Reference;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.ParticleArgument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class AbilityBreathWeapon extends ActivatedAbility
{
	public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.ModInfo.MOD_ID, "breath_weapon");
	
	private BreathType type;
	private double distance;
	private IParticleData particle = ParticleTypes.FLAME;
	
	private DamageType damage;
	private Pair<Float, Float> dmgAmount;
	private BlockState blockToPlace = Blocks.AIR.getDefaultState();
	
	private int duration = Reference.Values.TICKS_PER_SECOND * 3;
	
	public AbilityBreathWeapon(DamageType damageIn, BreathType typeIn, double dist, Pair<Float, Float> dmgIn)
	{
		super(REGISTRY_NAME, Reference.Values.TICKS_PER_MINUTE);
		this.damage = damageIn;
		this.type = typeIn;
		this.distance = dist;
		this.dmgAmount = dmgIn;
	}
	
	public AbilityBreathWeapon(DamageType damageIn, BreathType typeIn, double dist, float dmgMin, float dmgMax)
	{
		this(damageIn, typeIn, dist, Pair.of(dmgMin, dmgMax));
	}
	
	public ResourceLocation getMapName(){ return new ResourceLocation(Reference.ModInfo.MOD_ID, "breath_weapon_"+damage.getString()); }
	
	public ITextComponent translatedName(){ return new TranslationTextComponent("ability."+Reference.ModInfo.MOD_ID+".breath_weapon", (int)distance, type.translated(damage)); }
	
	protected Nature getDefaultNature(){ return Nature.SUPERNATURAL; }
	
	public AbilityBreathWeapon setParticle(IParticleData particleIn){ this.particle = particleIn; return this; }
	
	public AbilityBreathWeapon setBlock(BlockState state){ this.blockToPlace = state; return this; }
	
	public Type getType(){ return Ability.Type.ATTACK; }
	
	public CompoundNBT writeToNBT(CompoundNBT compound)
	{
		super.writeToNBT(compound);
		compound.putString("Type", this.type.getString());
		compound.putInt("Duration", this.duration);
		compound.putDouble("Distance", this.distance);
		
		CompoundNBT damageData = new CompoundNBT();
			damageData.putString("Type", damage.getString());
			if(dmgAmount.getLeft() == dmgAmount.getRight())
				damageData.putFloat("Amount", dmgAmount.getLeft());
			else
			{
				damageData.putFloat("Min", Math.min(dmgAmount.getLeft(), dmgAmount.getRight()));
				damageData.putFloat("Max", Math.max(dmgAmount.getLeft(), dmgAmount.getRight()));
			}
			if(blockToPlace.getBlock() != Blocks.AIR)
				damageData.put("BlockToPlace", NBTUtil.writeBlockState(this.blockToPlace));
		compound.put("Damage", damageData);
		
		compound.putString("Particle", this.particle.getParameters());
		return compound;
	}
	
	public void readFromNBT(CompoundNBT compound)
	{
		super.readFromNBT(compound);
		this.type = compound.contains("Type", 8) ? BreathType.fromString(compound.getString("Type")) : BreathType.CONE;
		this.duration = compound.contains("Duration", 3) ? compound.getInt("Duration") : Reference.Values.TICKS_PER_SECOND * 6;
		this.distance = compound.contains("Distance", 6) ? compound.getDouble("Distance") : 8.0D;
		
		CompoundNBT damageData = compound.getCompound("Damage");
		this.damage = damageData.contains("Type", 8) ? DamageType.fromString(damageData.getString("Type")) : DamageType.FIRE;
		if(damageData.contains("Amount", 5))
			this.dmgAmount = Pair.of(damageData.getFloat("Amount"), damageData.getFloat("Amount"));
		else if(damageData.contains("Min", 5))
			this.dmgAmount = Pair.of(damageData.getFloat("Min"), damageData.getFloat("Max"));
		else
			this.dmgAmount = Pair.of(1F, 8F);
		
		if(damageData.contains("BlockToPlace", 10))
		{
			this.blockToPlace = NBTUtil.readBlockState(damageData.getCompound("BlockToPlace"));
			if(this.blockToPlace == null)
				this.blockToPlace = Blocks.AIR.getDefaultState();
		}
		
		if(compound.contains("Particle", 8))
		{
			try
			{
				this.particle = ParticleArgument.parseParticle(new StringReader(compound.getString("Particle")));
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
			for(Ability ability : AbilityRegistry.getAbilitiesOfType(entity, REGISTRY_NAME))
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
	
	public void performBreathWeapon(LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();
		for(Ability ability : AbilityRegistry.getAbilitiesOfType(entity, REGISTRY_NAME))
		{
			AbilityBreathWeapon breath = (AbilityBreathWeapon)ability;
			if(breath.isActive())
			{
				World world = entity.getEntityWorld();
				Vector3d eyePos = new Vector3d(entity.getPosX(), entity.getPosYEye(), entity.getPosZ());
				Vector3d direction = entity.getLookVec();
				if(world.isRemote)
				{
					breath.type.makeParticles(world, breath.particle, eyePos, direction, breath.distance);
					if(breath.activeTicks%Reference.Values.TICKS_PER_SECOND == 0)
						entity.playSound(SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, 1F, 0.5F + entity.getRNG().nextFloat() * 0.5F);
				}
				else
				{
					List<UUID> ignoreList = Lists.newArrayList(entity.getUniqueID());
					
					if(breath.activeTicks-- > 0)
						switch(breath.type)
						{
							case CONE:
								for(int i=0; i<entity.getRNG().nextInt(4) + 1; i++)
								{
									Vector3d dir = direction.rotatePitch((float)Math.toRadians(entity.getRNG().nextInt(10) - 5)).rotateYaw((float)Math.toRadians(entity.getRNG().nextInt(90) - 45));
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
	
	public List<UUID> shootLine(World world, Vector3d eyePos, Vector3d direction, AbilityBreathWeapon breath, LivingEntity entity, boolean damage, List<UUID> ignoreEntities)
	{
		double distance = breath.distance;
		Vector3d maxPos = eyePos.add(direction.mul(new Vector3d(distance, distance, distance)));
		RayTraceResult trace = world.rayTraceBlocks(new RayTraceContext(eyePos, maxPos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null));
		if(trace.getType() != RayTraceResult.Type.MISS)
			distance = eyePos.distanceTo(trace.getHitVec());
		
		if(trace.getType() == RayTraceResult.Type.BLOCK && breath.blockToPlace.getBlock() != Blocks.AIR)
		{
			if(world.rand.nextInt(10) == 0)
			{
				BlockRayTraceResult traceResult = (BlockRayTraceResult)trace;
				BlockPos hitPos = traceResult.getPos();
				tryToPlaceBlock(world, hitPos, breath.blockToPlace, traceResult);
			}
		}
		
		List<UUID> hits = Lists.newArrayList();
		if(damage)
			for(LivingEntity hit : world.getEntitiesWithinAABB(LivingEntity.class, entity.getBoundingBox().grow(distance)))
			{
				if(hit == entity || ignoreEntities.contains(hit.getUniqueID()))
					continue;
				
				double distFromEye = eyePos.distanceTo(new Vector3d(hit.getPosX(), hit.getPosY() + (hit.getHeight() / 2), hit.getPosZ()));
				Vector3d posAtHit = eyePos.add(direction.mul(distFromEye, distFromEye, distFromEye));
				if(hit.getBoundingBox().contains(posAtHit))
				{
					// Damage hit entity
					float amount = Math.min(breath.dmgAmount.getLeft(), breath.dmgAmount.getRight());
					float range = Math.max(breath.dmgAmount.getLeft(), breath.dmgAmount.getRight()) - amount;
					amount += entity.getRNG().nextFloat() * range;
					
					amount *= Math.max(0.1F, 1 - ((hit.getDistance(entity)) / breath.distance));
					
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
							source = DamageSource.causeMobDamage(entity);
							break;
					}
					if(source != null)
						hit.attackEntityFrom(source, amount);
					
					hits.add(hit.getUniqueID());
				}
			}
		
		return hits;
	}
	
	@SuppressWarnings("deprecation")
	public boolean tryToPlaceBlock(World world, BlockPos pos, BlockState stateToPlace, BlockRayTraceResult trace)
	{
		if(!world.getGameRules().getBoolean(GameRules.MOB_GRIEFING))
			return false;
		
		BlockState stateAtPos = world.getBlockState(pos);
		ItemStack itemStack = new ItemStack(Item.getItemFromBlock(stateToPlace.getBlock()));
		BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(world, (PlayerEntity)null, Hand.MAIN_HAND, itemStack, trace));
		
		// Try to place at the block hit
		if(stateAtPos.isReplaceable(context))
		{
			world.setBlockState(pos, stateToPlace, 11);
			return true;
		}
		
		// Try to place at the neighbour of the block hit in the direction of the hit
		pos = pos.offset(trace.getFace());
		stateAtPos = world.getBlockState(pos);
		if(stateAtPos.isReplaceable(context))
		{
			world.setBlockState(pos, stateToPlace, 11);
			return true;
		}
		return false;
	}
	
	public static enum BreathType implements IStringSerializable
	{
		LINE,
		CONE;
		
		public String getString(){ return name().toLowerCase(); }
		public static BreathType fromString(String str)
		{
			for(BreathType type : values())
				if(type.getString().equalsIgnoreCase(str))
					return type;
			return CONE;
		}
		
		public ITextComponent translated(DamageType type)
		{
			return new TranslationTextComponent("ability."+Reference.ModInfo.MOD_ID+".breath_weapon."+getString(), type.getTranslated());
		}
		
		public void makeParticles(World world, IParticleData particleType, Vector3d origin, Vector3d direction, double distance)
		{
			if(distance <= 0D || !world.isRemote)
				return;
			
			Random rand = world.rand;
			switch(this)
			{
				case LINE:
					Vector3d maxPos = origin.add(direction.mul(new Vector3d(distance, distance, distance)));
					RayTraceResult trace = world.rayTraceBlocks(new RayTraceContext(origin, maxPos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, null));
					if(trace.getType() != RayTraceResult.Type.MISS)
						distance = origin.distanceTo(trace.getHitVec());
					
					for(int i=0; i<world.rand.nextInt(Math.max(6, 20 * (int)(distance / 6D))); i++)
					{
						double mult = rand.nextDouble() * distance;
						Vector3d pos = origin.add(direction.mul(mult, mult, mult));
						double speed = 0.05D;
						world.addOptionalParticle(particleType, true, pos.x, pos.y, pos.z, (rand.nextDouble() - 0.5D) * speed, (rand.nextDouble() - 0.5D) * speed, (rand.nextDouble() - 0.5D) * speed);
					}
					break;
				case CONE:
					for(int i=0; i<rand.nextInt(4) + 1; i++)
					{
						Vector3d dir = direction.rotatePitch((float)Math.toRadians(rand.nextInt(10) - 5)).rotateYaw((float)Math.toRadians(rand.nextInt(90) - 45));
						LINE.makeParticles(world, particleType, origin, dir, distance);
					}
					break;
			}
		}
	}
	
	public static class Builder extends Ability.Builder
	{
		public Builder(){ super(REGISTRY_NAME); }
		
		public Ability create(CompoundNBT compound)
		{
			BreathType type = compound.contains("Type", 8) ? BreathType.fromString(compound.getString("Type")) : BreathType.CONE;
			double distance = compound.contains("Distance", 6) ? compound.getDouble("Distance") : 8.0D;
			
			CompoundNBT damageData = compound.getCompound("Damage");
			DamageType damage = damageData.contains("Type", 8) ? DamageType.fromString(damageData.getString("Type")) : DamageType.FIRE;
			Pair<Float, Float> dmgAmount;
			if(damageData.contains("Amount", 5))
				dmgAmount = Pair.of(damageData.getFloat("Amount"), damageData.getFloat("Amount"));
			else if(damageData.contains("Min", 5))
				dmgAmount = Pair.of(damageData.getFloat("Min"), damageData.getFloat("Max"));
			else
				dmgAmount = Pair.of(1F, 8F);
			
			AbilityBreathWeapon weapon = new AbilityBreathWeapon(damage, type, distance, dmgAmount);
			
			if(damageData.contains("BlockToPlace", 10))
				weapon.setBlock(NBTUtil.readBlockState(damageData.getCompound("BlockToPlace")));
			
			if(compound.contains("Particle", 8))
			{
				IParticleData particle = null;
				try
				{
					particle = ParticleArgument.parseParticle(new StringReader(compound.getString("Particle")));
				}
				catch(CommandSyntaxException e){ }
				if(particle != null)
					weapon.setParticle(particle);
			}
			
			return weapon;
		}
	}
}
