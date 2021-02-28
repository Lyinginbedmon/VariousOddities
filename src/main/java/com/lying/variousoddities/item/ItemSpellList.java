package com.lying.variousoddities.item;

import com.lying.variousoddities.magic.IMagicEffect;
import com.lying.variousoddities.reference.Reference;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Reference.ModInfo.MOD_ID)
public class ItemSpellList extends ItemSpellContainer
{
	public ItemSpellList(Item.Properties properties)
	{
		super(properties.maxStackSize(1));
	}
    
//	public String getItemStackDisplayName(ItemStack stack)
//    {
//		String currentSpell = getCurrentSpell(stack);
//		if(MagicEffects.getSpellFromName(currentSpell) != null)
//			return MagicEffects.getSpellFromName(currentSpell).getTranslatedName();
//    	
//    	return super.getItemStackDisplayName(stack);
//    }
//    
//    @SideOnly(Side.CLIENT)
//	public void addInformation(ItemStack par1ItemStack, World par2World, List<String> par3List, ITooltipFlag par4Flags)
//	{
//    	PlayerEntity player = Minecraft.getMinecraft().player;
//    	if(player == null) return;
//    	
//    	VOPlayerData playerData = VOPlayerData.getPlayerData(player);
//    	if(playerData == null) return;
//    	
//		addSpellData(playerData.getCurrentSpell(), par1ItemStack, par3List);
//		if(getContainedSpells(par1ItemStack, player) != null && getContainedSpells(par1ItemStack, player).length > 1)
//			par3List.add(I18n.translateToLocalFormatted("info.varodd:spell_scroll.compound", getContainedSpells(par1ItemStack, player).length));
//	}
//    
//    public ItemStack scroll(PlayerEntity par1Player, ItemStack par2ItemStack, int par3Int, boolean updateHand)
//    {
//    	boolean shouldReturn = false;
//    	ItemStack cloneList = par2ItemStack.copy();
//    	String lastSpell = getCurrentSpell(par2ItemStack);
//    	VOPlayerData playerData = VOPlayerData.getPlayerData(par1Player);
//    	if(playerData != null)
//    	{
//    		playerData.incrementListIndex((int)Math.signum(par3Int));
//    		IMagicEffect currentSpell = playerData.getCurrentSpell();
//    		if(currentSpell == null && lastSpell.length() > 0)
//    		{
//    			setCurrentSpell(null, cloneList);
//    			shouldReturn = true;
//    		}
//    		else if(!lastSpell.equalsIgnoreCase(currentSpell.getName()))
//    		{
//    			setCurrentSpell(playerData.getCurrentSpell(), cloneList);
//    			shouldReturn = true;
//    		}
//    	}
//    	
//		par1Player.resetActiveHand();
//		if(par1Player.getEntityWorld().isRemote)
//    		if(updateHand)
//    		{
//    	        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
//    	        buffer.writeItemStack(cloneList);
//    	        PacketHandler.sendToServer(new PacketUpdateHeldItem(EnumHand.MAIN_HAND, buffer));
//    		}
//    	
//    	return shouldReturn ? cloneList : par2ItemStack;
//    }
//    
//    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
//    {
//    	super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
//    	if(entityIn instanceof PlayerEntity)
//    	{
//    		VOPlayerData playerData = VOPlayerData.getPlayerData((PlayerEntity)entityIn);
//    		setCurrentSpell(playerData.getCurrentSpell(), stack);
//    		
//			CompoundNBT stackData = stack.getTag();
//    		if(stackData.contains("Contents"))
//    		{
//    			NBTTagList contents = stackData.getTagList("Contents", 8);
//    			if(contents.hasNoTags()) return;
//    			
//    			List<String> applied = new ArrayList<>();
//    			for(int i=0; i<contents.tagCount(); i++)
//    			{
//    				String spell = contents.getStringTagAt(i);
//    				if(MagicEffects.getSpellFromName(spell) != null)
//    				{
//    					playerData.addSpell(spell);
//    					applied.add(spell);
//    				}
//    				else
//    					VariousOddities.log.warn("Spell list tried to add unrecognised spell to player "+entityIn.getName()+": "+spell);
//    			}
//    			VariousOddities.log.info("Spell list added spells to player "+entityIn.getName()+": "+applied);
//    			
//    			stackData.removeTag("Contents");
//    			stack.setTagCompound(stackData);
//    		}
//    	}
//    }
    
    public static ItemStack setCurrentSpell(IMagicEffect effect, ItemStack stack)
    {
    	CompoundNBT compound = stack.hasTag() ? stack.getTag() : new CompoundNBT();
    	if(effect == null)
    	{
    		if(compound.contains("CurrentSpell"))
    			compound.remove("CurrentSpell");
    	}
    	else if(!getCurrentSpell(stack).equalsIgnoreCase(effect.getSimpleName()))
    		compound.putString("CurrentSpell", effect.getSimpleName());
    	stack.setTag(compound);
        
    	return stack;
    }
    
    public static String getCurrentSpell(ItemStack stack)
    {
    	if(stack.hasTag() && stack.getTag().contains("CurrentSpell"))
    		return stack.getTag().getString("CurrentSpell");
    	return "";
    }
    
    @SubscribeEvent
    public static void onPlayerWakeUpEvent(PlayerWakeUpEvent event)
    {
//    	PlayerEntity player = event.getPlayer();
//    	VOPlayerData playerData = VOPlayerData.getPlayerData(player);
//    	if(playerData != null)
//    		playerData.refreshSpellList();
    }
    
    public void onSpellCast(String spellID, ItemStack stack, LivingEntity caster)
    {
    	if(caster instanceof PlayerEntity)
    	{
//    		PlayerEntity player = (PlayerEntity)caster;
//    		handlePlayerStats(player, MagicEffects.getSpellFromName(spellID));
//    		if(player.isCreative()) return;
//    		
//    		VOPlayerData playerData = VOPlayerData.getPlayerData(player);
//    		if(playerData != null)
//    			playerData.removeActiveSpell(spellID);
//    		setCurrentSpell(playerData.getCurrentSpell(), stack);
    	}
    	
    	setInverted(stack, false);
    }
}
