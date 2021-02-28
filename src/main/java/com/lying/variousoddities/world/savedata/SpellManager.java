package com.lying.variousoddities.world.savedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.google.common.base.Predicates;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.api.event.SpellEvent.SpellExpireEvent;
import com.lying.variousoddities.entity.EntitySpell;
import com.lying.variousoddities.init.VOEntities;
import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.magic.IMagicEffect.DurationType;
import com.lying.variousoddities.magic.MagicEffects;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.utility.VOHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

public class SpellManager extends WorldSavedData
{
	protected static final String DATA_NAME = Reference.ModInfo.MOD_ID+"_spells";
	
	protected final Map<ResourceLocation, List<SpellData>> DIM_TO_SPELLS = new HashMap<>();
	protected int nextID = 0;
	
	public SpellManager()
	{
		this(DATA_NAME);
	}
	
	public SpellManager(String name)
	{
		super(name);
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		compound.putInt("NextID", nextID);
		
		ListNBT dimensions = new ListNBT();
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
		{
			CompoundNBT dimension = new CompoundNBT();
			dimension.putString("Dim", dim.toString());
			dimension.put("Spells", spellsToNBT(DIM_TO_SPELLS.get(dim)));
			dimensions.add(dimension);
		}
		compound.put("Dimensions", dimensions);
		
		return compound;
	}
	
	public void read(CompoundNBT compound)
	{
		if(compound.contains("NextID"))
			nextID = compound.getInt("NextID");
		
		DIM_TO_SPELLS.clear();
		ListNBT dimensions = compound.getList("Dimensions", 10);
		for(int i=0; i<dimensions.size(); i++)
		{
			CompoundNBT dimension = dimensions.getCompound(i);
			List<SpellData> spells = NBTToSpells(dimension.getList("Spells", 10));
			if(!spells.isEmpty())
				DIM_TO_SPELLS.put(new ResourceLocation(dimension.getString("Dim")), spells);
		}
	}
	
	public static ListNBT spellsToNBT(List<SpellData> spellsIn)
	{
		ListNBT spells = new ListNBT();
		for(SpellData spell : spellsIn)
			spells.add(spell.writeToNBT(new CompoundNBT()));
		
		return spells;
	}
	
	public static List<SpellData> NBTToSpells(ListNBT spellsIn)
	{
		List<SpellData> spellList = new ArrayList<SpellData>();
		if(!spellsIn.isEmpty())
			for(int j=0; j<spellsIn.size(); j++)
				spellList.add(new SpellData(spellsIn.getCompound(j)));
		
		return spellList;
	}
	
	public static SpellManager get(World worldIn)
	{
		if(worldIn.isRemote)
			return VariousOddities.proxy.getSpells();
		else
			return (SpellManager)((ServerWorld)worldIn).getSavedData().getOrCreate(SpellManager::new, DATA_NAME);
	}
	
	/**
	 * Updates all spells in the given world.
	 * @param world
	 */
	public void updateSpells(World world, Side side)
	{
		DimensionType dim = world.getDimensionType();
		List<Integer> deadSpells = new ArrayList<>();
		for(SpellData spell : getSpellsInDimension(dim))
		{
			if(spell.onUpdate(world, side))
				markDirty();
			
			if(spell.isDead() && side == Side.SERVER)
			{
				deadSpells.add(spell.getID());
				world.addParticle(ParticleTypes.EXPLOSION, spell.posX, spell.posY, spell.posZ, 0D, 0D, 0D);
				markDirty();
			}
		}
		
		for(Integer index : deadSpells)
			removeSpell(index);
				
	}
	
	public boolean removeSpell(int index)
	{
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
		{
			List<SpellData> spells = DIM_TO_SPELLS.get(dim);
			SpellData foundSpell = null;
			for(SpellData spell : spells)
				if(spell.getID() == index)
				{
					foundSpell = spell;
					break;
				}
			if(foundSpell != null)
			{
				spells.remove(foundSpell);
				DIM_TO_SPELLS.put(dim, spells);
				markDirty();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Registers the given spell data and returns its unique ID.
	 * @param spell
	 * @return
	 */
	public int registerNewSpell(SpellData spell, World world)
	{
		spell.setID(nextID);
		
		ResourceLocation dim = world.getDimensionKey().getRegistryName();
		spell.setDim(dim);
		List<SpellData> spells = DIM_TO_SPELLS.containsKey(dim) ? DIM_TO_SPELLS.get(dim) : new ArrayList<SpellData>();
		spells.add(spell);
		DIM_TO_SPELLS.put(dim, spells);
		
		markDirty();
//		BusSpells.notifyClients(world);
		return nextID++;
	}
	
	/**
	 * Returns a list of all spells currently affecting the given entity.<br>
	 * Mostly used for dispelling.
	 */
	public Map<ResourceLocation, List<SpellData>> getSpellsAffecting(LivingEntity entity)
	{
		Map<ResourceLocation, List<SpellData>> spells = new HashMap<>();
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
		{
			List<SpellData> spellsInDim = new ArrayList<>();
			for(SpellData spell : DIM_TO_SPELLS.get(dim))
				if(spell.getSpell().isAffectingEntity(spell, entity))
					spellsInDim.add(spell);
			
			spells.put(dim, spellsInDim);
		}
		
		return spells;
	}
	
	/**
	 * Returns a list of all ongoing spells which were cast by the given entity.
	 */
	public Map<ResourceLocation, List<SpellData>> getSpellsOwnedBy(LivingEntity entity)
	{
		Map<ResourceLocation, List<SpellData>> spells = new HashMap<>();
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
		{
			List<SpellData> spellsInDim = new ArrayList<>();
			for(SpellData spell : DIM_TO_SPELLS.get(dim))
				if(spell.getCaster(entity.getEntityWorld()) == entity)
					spellsInDim.add(spell);
			
			spells.put(dim, spellsInDim);
		}
		
		return spells;
	}
	
	/** 
	 * Returns a list of all spells currently affecting the given entity or which were cast by that entity.<br>
	 * Used by the active spell screen.
	 */
	public Map<ResourceLocation, List<SpellData>> getSpellsAffectingOrOwnedBy(LivingEntity entity)
	{
		Map<ResourceLocation, List<SpellData>> spells = new HashMap<>();
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
		{
			List<SpellData> spellsInDim = new ArrayList<>();
			for(SpellData spell : DIM_TO_SPELLS.get(dim))
				if(spell.getSpell().isAffectingEntity(spell, entity) || spell.getCaster(entity.getEntityWorld()) == entity)
					spellsInDim.add(spell);
			
			spells.put(dim, spellsInDim);
		}
		
		return spells;
	}
	
	public Map<ResourceLocation, List<SpellData>> getSpellsForClient(PlayerEntity player)
	{
		World world = player.getEntityWorld();
		ResourceLocation localDim = world.getDimensionKey().getRegistryName();
		Map<ResourceLocation, List<SpellData>> spells = getSpellsAffectingOrOwnedBy(player);
		List<SpellData> spellsNearby = getSpellsWithin(world, player.getBoundingBox().grow(64D));
		
		List<SpellData> spellsInDim = spells.containsKey(localDim) ? spells.get(localDim) : new ArrayList<>();
		spellsInDim.removeAll(spellsNearby);
		spellsInDim.addAll(spellsNearby);
		
		spells.put(localDim, spellsInDim);
		
		return spells;
	}
	
	/**
	 * Returns the spell data associated to the given ID, if any.
	 * @param par1Int
	 * @return
	 */
	public SpellData getSpellByID(int par1Int)
	{
		if(par1Int < 0 || par1Int > nextID) return null;
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
			for(SpellData spell : DIM_TO_SPELLS.get(dim))
				if(spell.getID() == par1Int)
					return spell;
		
		return null;
	}
	
	public void setSpellByID(SpellData spellIn)
	{
		if(spellIn.ID < 0 || spellIn.ID > nextID) return;
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
			for(SpellData spell : DIM_TO_SPELLS.get(dim))
				if(spell.getID() == spellIn.getID())
				{
					List<SpellData> dimSpells = DIM_TO_SPELLS.get(dim);
					dimSpells.remove(spell);
					dimSpells.add(spellIn);
					
					DIM_TO_SPELLS.put(dim, dimSpells);
					return;
				}
	}
	
	/**
	 * Returns all active spells of the given effect.
	 * @param effect
	 * @return
	 */
	public List<SpellData> getSpellsOfType(IMagicEffect effect)
	{
		List<SpellData> spells = new ArrayList<>();
		for(ResourceLocation dim : DIM_TO_SPELLS.keySet())
			for(SpellData spell : DIM_TO_SPELLS.get(dim))
				if(!spell.isDead() && spell.getSpell() == effect)
					spells.add(spell);
		return spells;
	}
	
	public List<SpellData> getSpellsOfTypeInDimension(IMagicEffect effect, DimensionType dim)
	{
		List<SpellData> spells = getSpellsInDimension(dim);
		spells.removeIf(new Predicate<SpellData>()
		{
			public boolean test(SpellData input)
			{
				return input.getSpell() != effect;
			}
		});
		return spells;
	}
	
	public List<SpellData> getSpellsWithin(World world, AxisAlignedBB bounds, Predicate<SpellData> predicate)
	{
		List<SpellData> spells = new ArrayList<>();
		for(SpellData spell : getSpellsInDimension(world.getDimensionType()))
			if(!spell.isDead() && bounds.contains(spell.getPos()) && predicate.test(spell))
				spells.add(spell);
		return spells;
	}
	
	public List<SpellData> getSpellsWithin(World world, AxisAlignedBB bounds)
	{
		return getSpellsWithin(world, bounds, Predicates.alwaysTrue());
	}
	
	public List<SpellData> getSpellsInDimension(DimensionType dim, Predicate<SpellData> predicate)
	{
		List<SpellData> spells = new ArrayList<>();
		if(DIM_TO_SPELLS.containsKey(dim))
			for(SpellData spell : DIM_TO_SPELLS.get(dim))
				if(predicate.test(spell))
					spells.add(spell);
		return spells;
	}
	
	public List<SpellData> getSpellsInDimension(DimensionType dim)
	{
		return getSpellsInDimension(dim, Predicates.alwaysTrue());
	}
	
	public static class SpellData
	{
		private int ID = -1;
		private ResourceLocation dim = null;
		
		private String casterName = "";
		@Nullable
		private UUID casterUUID = null;
		private long castTime = -1L;
		
		private final IMagicEffect spell;
		private int casterLevel;
		private CompoundNBT spellStorage;
		
		private String customName = "";
		
		public double posX, posY, posZ;
		public float rotationPitch, rotationYaw;
		
		private boolean inverted = false;
		
		private boolean isDead = false;
		
		public SpellData(IMagicEffect spellIn, int levelIn, CompoundNBT spellDataIn)
		{
			this.spell = spellIn;
			this.casterLevel = levelIn;
			this.spellStorage = spellDataIn;
		}
		
		public SpellData(IMagicEffect spellIn, int levelIn)
		{
			this(spellIn, levelIn, new CompoundNBT());
		}
		
		public SpellData(IMagicEffect spellIn, int levelIn, double x, double y, double z)
		{
			this(spellIn, levelIn);
			setPosition(x, y, z);
		}
		
		public SpellData(CompoundNBT compound)
		{
			this(MagicEffects.getSpellFromName(compound.getString("Spell")), 0);
			this.readFromNBT(compound);
		}
		
		public void setID(int par1Int){ this.ID = par1Int; }
		public int getID(){ return this.ID; }
		
		public void setDim(ResourceLocation par1Int){ this.dim = par1Int; }
		public ResourceLocation dim(){ return this.dim; }
		
		public CompoundNBT writeToNBT(CompoundNBT compound)
		{
			compound.putInt("ID", getID());
			compound.putString("Dim", dim.toString());
			
			if(this.casterName.length() > 0)
				compound.putString("CasterName", this.casterName);
			if(this.casterUUID != null)
				compound.putUniqueId("CasterUUID", this.casterUUID);
			compound.putLong("CastTime", this.castTime);
			
			compound.putString("Spell", this.spell.getSimpleName());
			compound.putInt("Level", this.casterLevel);
			compound.put("Storage", this.spellStorage);
			
			compound.putString("CustomName", this.customName);
			
            compound.put("Pos", this.newDoubleNBTList(this.posX, this.posY, this.posZ));
            compound.put("Rotation", this.newFloatNBTList(this.rotationYaw, this.rotationPitch));
			
			return compound;
		}
		
		public void readFromNBT(CompoundNBT compound)
		{
			setID(compound.getInt("ID"));
			this.dim = new ResourceLocation(compound.getString("Dim"));
			
			if(compound.contains("CasterName"))
				this.casterName = compound.getString("CasterName");
			if(compound.contains("CasterUUIDLeast"))
				this.casterUUID = compound.getUniqueId("CasterUUID");
			this.castTime = compound.getLong("CastTime");
			
			this.casterLevel = compound.getInt("Level");
			this.spellStorage = compound.getCompound("Storage");
			
			this.customName = compound.getString("CustomName");
			
            ListNBT positionData = compound.getList("Pos", 6);
            this.posX = positionData.getDouble(0);
            this.posY = positionData.getDouble(1);
            this.posZ = positionData.getDouble(2);
            ListNBT rotationData = compound.getList("Rotation", 5);
            this.rotationYaw = rotationData.getFloat(0);
            this.rotationPitch = rotationData.getFloat(1);
		}
		
		public IMagicEffect getSpell(){ return this.spell; }
		
		public void setInverted(boolean invertedIn){ this.inverted = invertedIn; }
		public boolean inverted(){ return this.inverted; }
		
		public void setCaster(LivingEntity casterIn)
		{
			setCaster(casterIn instanceof PlayerEntity || casterIn.hasCustomName() ? casterIn.getName().getUnformattedComponentText() : "", casterIn.getUniqueID());
		}
		
		public void setCaster(String name, UUID uuid)
		{
			this.casterUUID = uuid;
			this.casterName = name == null ? "" : name;
		}
		
		public boolean hasCaster(){ return this.casterUUID != null || this.casterName.length() > 0; }
		
		public void setCastTime(long castTimeIn)
		{
			this.castTime = castTimeIn;
		}
		
		public long castTime(){ return this.castTime; }
		
		public int casterLevel(){ return this.casterLevel < 0 ? 20 : this.casterLevel; }
		
		public CompoundNBT getStorage(){ return this.spellStorage == null ? new CompoundNBT() : this.spellStorage; }
		
		public void setStorage(CompoundNBT compound)
		{
			this.spellStorage = compound;
		}
		
		/**
		 * Returns the translated name of the spell, or its custom name if it has one.
		 * @return
		 */
		@OnlyIn(Dist.CLIENT)
		public String getDisplayName()
		{
			return this.customName.length() > 0 ? this.customName : getDefaultName();
		}
		
		@OnlyIn(Dist.CLIENT)
		public String getDefaultName()
		{
			return spell.getTranslatedName();
		}
		
		@OnlyIn(Dist.CLIENT)
		public String getCustomName(){ return this.customName; }
		
		@OnlyIn(Dist.CLIENT)
		public boolean hasCustomName()
		{
			return this.customName != null && this.customName.length() > 0 && !this.customName.equals(getDefaultName());
		}
		
		public void setCustomName(String nameIn){ this.customName = nameIn; }
		
		public boolean isPermanent(){ return false; }
		public void setPermanent(boolean bool){ }
		
		/**
		 * Returns true if the spell is dismissable by the caster.
		 * @return
		 */
		public boolean dismissable(){ return this.spell.isDismissable(); }
		
		/**
		 * Returns true if this is a concentration spell.
		 * @return
		 */
		public boolean isConcentration(){ return false; }
		
		/**
		 * Returns true if this spell can be altered post-casting.
		 * @return
		 */
		public boolean canEdit(){ return false; }
		
		public LivingEntity getCaster(World worldIn)
		{
			LivingEntity playerCaster = null;
			if(this.casterName.length() > 0 && (playerCaster = VOHelper.getPlayerEntityByName(worldIn, this.casterName)) != null)
				return playerCaster;
			else if((playerCaster = worldIn.getPlayerByUuid(this.casterUUID)) != null)
				return playerCaster;
			
			for(LivingEntity entity : worldIn.getLoadedEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE)))
				if(entity.getUniqueID().equals(this.casterUUID))
					return entity;
			
			return null;
		}
		
		public boolean isCaster(LivingEntity living)
		{
			if(this.casterName.length() > 0 && living.getName().equals(this.casterName))
				return true;
			
			if(living.getUniqueID().equals(this.casterUUID))
				return true;
			
			return false;
		}
		
		public boolean onUpdate(World world, Side side)
		{
			if(!isDead() && this.spell == null)
			{
				this.setDead(world);
				return false;
			}
			else if(isDead() && side != Side.CLIENT)
				return false;
			
			int activeTime = (int)(world.getGameTime() - this.castTime);
			if(!(MagicEffects.isInsideAntiMagic(world, posX, posY, posZ) && this.spell != MagicEffects.ANTIMAGIC))
			{
				if(this.castTime <= 0)
				{
					setCastTime(world.getGameTime());
					spell.doEffectStart(this, world, side);
				}
				else spell.doEffect(this, world, activeTime, side);
			}
			
			int duration = spell.getDuration(this.casterLevel);
			if(duration >= 0 && activeTime > duration && !(spell.canBePermanent() && isPermanent()))
				if(!MinecraftForge.EVENT_BUS.post(new SpellExpireEvent(this, world)))
					this.setDead(world);
			
			return true;
			
		}
		
		public boolean canBeDismissed()
		{
			return spell.isDismissable() && (!isPermanent() || spell.isAlwaysDismissable());
		}
		
		public boolean dismiss(World world, LivingEntity player)
		{
			if(!canBeDismissed())
				return false;
			
			int activeTime = (int)(world.getGameTime() - this.castTime);
	    	if(spell.getDurationType() != DurationType.INSTANT && activeTime > (Reference.Values.TICKS_PER_SECOND * 3))
    			if(player == getCaster(world) || player instanceof PlayerEntity && ((PlayerEntity)player).isCreative())
    	    	{
    				setDead(world);
    	    		return true;
    	    	}
	    	
	    	return false;
		}
		
		public void setPosition(double x, double y, double z)
		{
			this.posX = x;
			this.posY = y;
			this.posZ = z;
		}
		
		public Vector3d getPos(){ return new Vector3d(posX, posY, posZ); }
		public BlockPos getPosition(){ return new BlockPos(posX, posY, posZ); }
		public AxisAlignedBB getBoundingBox(){ return new AxisAlignedBB(posX - 0.25D, posY, posZ - 0.25D, posX + 0.25D, posY + 0.5D, posZ + 0.25D); }
		
		public double getDistance(double x, double y, double z){ return getPos().distanceTo(new Vector3d(x,y,z)); }
		
		public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch)
		{
			setPosition(x, y, z);
			this.rotationYaw = yaw;
			this.rotationPitch = pitch;
		}
		
		public void copyLocationAndAnglesFrom(Entity entity)
		{
			setPositionAndRotation(entity.getPosX(), entity.getPosY(), entity.getPosZ(), entity.rotationYaw, entity.rotationPitch);
		}
		
		public float yaw(){ return this.rotationYaw; }
		public float pitch(){ return this.rotationPitch; }
		
		/**
		 * Creates a spell entity at the location of this spell.<br>
		 * Mostly a hold-over from older code that relied on an entity to function from.
		 * @param worldIn
		 * @return
		 */
		public EntitySpell createSourceEntity(World worldIn)
		{
			EntitySpell spellEntity = VOEntities.SPELL.create(worldIn);
			spellEntity.setPosition(posX, posY, posZ);
			spellEntity.setSpell(this);
			return spellEntity;
		}
		
		/**
		 * True if the world registry should stop tracking this spell.<br>
		 * Only really used by the registry, as spell entities die instead.
		 */
		public boolean isDead(){ return this.isDead; }
		
		public void setDead(World world)
		{
			if(isDead()) return;
			this.isDead = true;
			
			if(spell != null)
			{
				int duration = spell.getDuration(casterLevel);
				int activeTime = (int)(world.getGameTime() - this.castTime);
		    	if(activeTime < duration || duration < 0)
		    		spell.doEffectCancel(this, world, world.isRemote ? Side.CLIENT : Side.SERVER);
			}
		}
		
	    /**
	     * creates a NBT list from the array of doubles passed to this function
	     */
	    protected ListNBT newDoubleNBTList(double... numbers)
	    {
	        ListNBT ListNBT = new ListNBT();
	        for (double d0 : numbers)
	            ListNBT.add(DoubleNBT.valueOf(d0));
	        return ListNBT;
	    }

	    /**
	     * Returns a new ListNBT filled with the specified floats
	     */
	    protected ListNBT newFloatNBTList(float... numbers)
	    {
	        ListNBT ListNBT = new ListNBT();
	        for (float f : numbers)
	            ListNBT.add(FloatNBT.valueOf(f));
	        return ListNBT;
	    }
	}
}
