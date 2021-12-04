package com.lying.variousoddities.utility;

import java.util.Map;

import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.client.gui.IScrollableGUI;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.network.PacketBonusJump;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketMountGui;
import com.lying.variousoddities.network.PacketSyncVisualPotions;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityPhasing;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilitySize;
import com.lying.variousoddities.species.abilities.AbilitySwim;
import com.lying.variousoddities.species.abilities.IPhasingAbility;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class VOBusClient
{
	@SuppressWarnings("deprecation")
	@SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
	public static void noclipFog(FogDensity event)
	{
		if(playerInWall())
		{
	        RenderSystem.fogStart(0.0F);
	        RenderSystem.fogEnd(10F);
	        event.setDensity(0.25F);
	        event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
	public static void noclipFogColor(FogColors event)
	{
		if(playerInWall())
		{
			event.setRed(0F);
			event.setBlue(0F);
			event.setGreen(0F);
		}
	}
	
	@SubscribeEvent
	public static void onMouseScroll(GuiScreenEvent.MouseScrollEvent.Pre event)
	{
		if(Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen instanceof IScrollableGUI)
		{
			((IScrollableGUI)Minecraft.getInstance().currentScreen).onScroll((int)Math.signum(event.getScrollDelta()));
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onDeadScroll(InputEvent.MouseScrollEvent event)
	{
		if(!PlayerData.isPlayerNormalFunction(Minecraft.getInstance().player))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onEntityLoadEvent(EntityJoinWorldEvent event)
	{
		if(event.getWorld().isRemote)
			PacketHandler.sendToServer(new PacketSyncVisualPotions(event.getEntity().getUniqueID()));
	}
	
	@SubscribeEvent
	public static void onLivingJump(LivingUpdateEvent event)
	{
		if(event.getEntityLiving() == Minecraft.getInstance().player)
		{
			ClientPlayerEntity player = (ClientPlayerEntity)event.getEntityLiving();
			LivingData data = LivingData.forEntity(event.getEntityLiving());
			Abilities abilities = data.getAbilities();
			Map<ResourceLocation, Ability> abilityMap = AbilityRegistry.getCreatureAbilities(player);
			if(player.movementInput.jump && abilities.canBonusJump)
			{
				if(AbilitySwim.isEntitySwimming(player))
				{
					if(abilityMap.containsKey(AbilitySwim.REGISTRY_NAME))
					{
						abilities.doWaterJump();
						PacketHandler.sendToServer(new PacketBonusJump(false));
					}
				}
				else if(!player.isOnGround())
				{
					if(abilityMap.containsKey(AbilityFlight.REGISTRY_NAME) && abilityMap.get(AbilityFlight.REGISTRY_NAME).isActive())
					{
						abilities.doAirJump();
						player.connection.sendPacket(new CEntityActionPacket(player, CEntityActionPacket.Action.START_FALL_FLYING));
						PacketHandler.sendToServer(new PacketBonusJump());
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent event)
	{
		PlayerEntity localPlayer = Minecraft.getInstance().player;
		PlayerEntity rendering = event.getPlayer();
		
		if(localPlayer == rendering)
			return;
		else
		{
			PlayerData playerData = PlayerData.forPlayer(localPlayer);
			PlayerData renderData = PlayerData.forPlayer(rendering);
			if(renderData != null && playerData != null && renderData.getBodyCondition() != playerData.getBodyCondition())
				event.setCanceled(true);
		}
	}
	
	/** True if the current render event is most likely being caused by Incorporeal transparency. */
	private static boolean skipRenderEvent = false;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
	public static <T extends LivingEntity, M extends EntityModel<T>> void noclipHideEntities(RenderLivingEvent.Pre event)
	{
		LivingEntity renderTarget = event.getEntity();
		if(playerInWall())
		{
			Vector3d posFeet = renderTarget.getPositionVec();
			Vector3d posEyes = posFeet.add(0D, renderTarget.getEyeHeight(), 0D);
			
			PlayerEntity player = Minecraft.getInstance().player;
			Vector3d posView = player.getPositionVec().add(0D, player.getEyeHeight(), 0D);
			
			if(posView.distanceTo(posFeet) > 8D || posView.distanceTo(posEyes) > 8D)
				event.setCanceled(true);
		}
		
		if(skipRenderEvent)
			skipRenderEvent = false;
		else if(renderTarget instanceof PlayerEntity)
		{
			PlayerData data = PlayerData.forPlayer((PlayerEntity)renderTarget);
			if(!AbilityRegistry.getAbilitiesOfType(renderTarget, AbilityPhasing.class).isEmpty() || (data != null && data.getBodyCondition() != BodyCondition.ALIVE))
			{
	            event.setCanceled(true);
	            IRenderTypeBuffer.Impl iRenderTypeBuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
	            event.getMatrixStack().push();
	            	skipRenderEvent = true;
	            	event.getRenderer().render(renderTarget, renderTarget.rotationYaw, event.getPartialRenderTick(), event.getMatrixStack(), iRenderTypeBuffer, 0xffffff);
	            event.getMatrixStack().pop();
	        }
		}
	}
	
	@SuppressWarnings("rawtypes")
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public static <T extends LivingEntity, M extends EntityModel<T>> void resizeEntity(RenderLivingEvent.Pre event)
	{
		LivingEntity renderTarget = event.getEntity();
		AbilitySize size = (AbilitySize)AbilityRegistry.getAbilityByName(renderTarget, AbilitySize.REGISTRY_NAME);
		if(size == null)
			return;
		
		float scale = size.getScale();
		event.getMatrixStack().scale(scale, scale, scale);
	}
	
	@SubscribeEvent
	public static void onDazedClickEvent(InputEvent.ClickInputEvent event)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && VOPotions.isPotionVisible(player, VOPotions.DAZED))
			event.setCanceled(true);
	}
	
	private static int dazzledTime = 0;
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onRenderDazzled(RenderGameOverlayEvent.Pre event)
	{
		Minecraft mc = Minecraft.getInstance();
		if(event.getType() != ElementType.VIGNETTE || mc.player == null)
			return;
		
		PlayerEntity player = mc.player;
		EffectInstance dazzle = player.getActivePotionEffect(VOPotions.DAZZLED);
		if(dazzle != null && dazzle.getDuration() > 0)
		{
			int scaledWidth = mc.getMainWindow().getScaledWidth();
			int scaledHeight = mc.getMainWindow().getScaledHeight();
			
			int fadeTime = Reference.Values.TICKS_PER_SECOND * 5;
			int time = 0;
			if(dazzle.getDuration() >= fadeTime)
				time = dazzledTime++;
			else
				time = dazzledTime = dazzle.getDuration();
			float alpha = Math.min(1F, (float)time / (float)fadeTime);
			
			RenderSystem.disableDepthTest();
		    RenderSystem.depthMask(false);
		    RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
			mc.getTextureManager().bindTexture(new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/misc/vignette_dazzled.png"));
		    Tessellator tessellator = Tessellator.getInstance();
		    BufferBuilder bufferbuilder = tessellator.getBuffer();
		    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			    bufferbuilder.pos(0.0D, (double)scaledHeight, -90.0D).tex(0.0F, 1.0F).endVertex();
			    bufferbuilder.pos((double)scaledWidth, (double)scaledHeight, -90.0D).tex(1.0F, 1.0F).endVertex();
			    bufferbuilder.pos((double)scaledWidth, 0.0D, -90.0D).tex(1.0F, 0.0F).endVertex();
			    bufferbuilder.pos(0.0D, 0.0D, -90.0D).tex(0.0F, 0.0F).endVertex();
		    tessellator.draw();
		    RenderSystem.depthMask(true);
		    RenderSystem.enableDepthTest();
		    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			
			event.setCanceled(true);
		}
		else
			dazzledTime = 0;
	}
	
	@SubscribeEvent
	public static void onMountUIOpen(GuiOpenEvent event)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && player.getRidingEntity() != null && player.getRidingEntity() instanceof IMountInventory && event.getGui() instanceof InventoryScreen)
		{
			event.setGui(null);
			PacketHandler.sendToServer(new PacketMountGui());
		}
	}
	
	public static boolean playerInWall()
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null)
			return IPhasingAbility.isPhasing(player) && getInWallBlockState(player) != null;
		return false;
	}
	
	private static BlockState getInWallBlockState(PlayerEntity playerEntity)
	{
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for(int i = 0; i < 8; ++i)
        {
            double d = playerEntity.getPosX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            double e = playerEntity.getPosYEye() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getPosZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            mutable.setPos(d, e, f);
            BlockState blockState = playerEntity.world.getBlockState(mutable);
            if(blockState.getRenderType() != BlockRenderType.INVISIBLE)
                return blockState;
        }
        return null;
    }
	
	@SubscribeEvent
	public static void onSilencedChatEvent(ClientChatEvent event)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && player.isPotionActive(VOPotions.SILENCED) && !event.getOriginalMessage().startsWith("/"))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onDeafenedChatEvent(ClientChatReceivedEvent event)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && player.isPotionActive(VOPotions.DEAFENED) && event.getType() == ChatType.CHAT)
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onDeafenedPlaySound(PlaySoundAtEntityEvent event)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null && player.isPotionActive(VOPotions.DEAFENED))
			event.setCanceled(true);
	}
}
