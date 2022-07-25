package com.lying.variousoddities.utility;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.capabilities.PlayerData;
import com.lying.variousoddities.capabilities.PlayerData.BodyCondition;
import com.lying.variousoddities.client.gui.IScrollableGUI;
import com.lying.variousoddities.condition.Condition;
import com.lying.variousoddities.condition.ConditionInstance;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.init.VOTileEntities;
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
import com.lying.variousoddities.tileentity.TileEntityPhylactery;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class VOBusClient
{
	private static final Minecraft mc = Minecraft.getInstance();
	
	private static int phylacteryNotification = -1;
	
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
		if(mc.screen != null && mc.screen instanceof IScrollableGUI)
		{
			((IScrollableGUI)mc.screen).onScroll((int)Math.signum(event.getScrollDelta()));
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onDeadScroll(MouseScrollingEvent event)
	{
		if(!PlayerData.isPlayerNormalFunction(mc.player))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onEntityLoadEvent(EntityJoinLevelEvent event)
	{
		if(event.getLevel().isClientSide)
			PacketHandler.sendToServer(new PacketSyncVisualPotions(event.getEntity().getUUID()));
	}
	
	@SubscribeEvent
	public static void onLivingJump(LivingTickEvent event)
	{
		if(event.getEntity() == mc.player)
		{
			LocalPlayer player = (LocalPlayer)event.getEntity();
			LivingData data = LivingData.forEntity(event.getEntity());
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
	public static void onWorldRender(RenderWorldLastEvent event)
	{
		Player localPlayer = mc.player;
		if(localPlayer == null || localPlayer.getLevel() == null)
			return;
		
		// Find all loaded phylacteries
		List<TileEntityPhylactery> phylacteries = Lists.newArrayList();
		localPlayer.getLevel().loadedTileEntityList.forEach((tile) -> { if(tile.getType() == VOTileEntities.PHYLACTERY) phylacteries.add((TileEntityPhylactery)tile); });
		
		handleMistNotification(localPlayer, phylacteries);
		spawnMistParticles(localPlayer, phylacteries);
		
		if(Minecraft.isGuiEnabled())
			displayConditions(event.getMatrixStack(), localPlayer);
	}
	
	private static void displayConditions(PoseStack stack, Player localPlayer)
	{
		LivingData playerData = LivingData.forEntity(localPlayer);
		List<LivingEntity> nearbyMobs = localPlayer.getLevel().getEntitiesOfClass(LivingEntity.class, localPlayer.getBoundingBox().inflate(16D));
		for(LivingEntity mob : nearbyMobs)
		{
			List<ConditionInstance> conditions = playerData == null ? Lists.newArrayList() : playerData.getConditionsFromUUID(mob.getUUID());
			
			LivingData data = LivingData.forEntity(mob);
			if(data != null)
				conditions.addAll(data.getConditionsFromUUID(localPlayer.getUUID()));
			
			if(!conditions.isEmpty())
				renderConditionStack(stack, mob, conditions);
		}
	}
	
	private static void renderConditionStack(PoseStack stack, LivingEntity mob, List<ConditionInstance> conditions)
	{
		if(conditions.isEmpty())
			return;
		
		double scale = Math.min(0.5D, mob.getBbWidth());
		
		net.minecraftforge.client.event.RenderNameTagEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameTagEvent(mob, mob.getDisplayName(), null, stack, null, 15, 1F);
		MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
		
		Vec3 mobPos = mob.position().add(0D, mob.getBbHeight() + (scale * 0.5D) + 0.1D, 0D);
		if(renderNameplateEvent.getResult() != Event.Result.DENY)
			if(renderNameplateEvent.getResult() == Event.Result.ALLOW || (mob.getAlwaysRenderNameTagForRender() && mob.hasCustomName()) || mob.getType() == EntityType.PLAYER)
				mobPos = mobPos.add(0D, 0.5D, 0D);
		
		Vec3 viewVec = mc.getRenderManager().info.getProjectedView();
		Vec3 iconPos = mobPos.subtract(viewVec);
		Vec3 direction = iconPos.normalize().multiply(1D, 0D, 1D).rotateYaw((float)Math.toRadians(90D));
		
		double iconSep = 0.1D;
		
		if(conditions.size() > 1)
		{
			double barWidth = (scale + iconSep) * (conditions.size() - 1);
			iconPos = iconPos.subtract(direction.scale(barWidth * 0.5D));
		}
		
		for(ConditionInstance instance : conditions)
		{
			renderConditionIcon(stack, iconPos, direction, scale * 0.5D, instance.condition(), !instance.originUUID().equals(mob.getUUID()));
			iconPos = iconPos.add(direction.scale(scale + iconSep));
		}
	}
	
	/**
	 * @param stack
	 * @param pos	Relative position icon should be rendered relative to the projected view position
	 * @param direction	Direction vector from projected view position to icon position
	 * @param scale	Radius of the icon, ie. how far left/right/etc. from pos it will be rendered 
	 * @param condition
	 * @param affecting
	 */
	private static void renderConditionIcon(PoseStack stack, Vec3 pos, Vec3 direction, double scale, Condition condition, boolean affecting)
	{
		double xOff = direction.x * scale;
		double yOff = scale;
		double zOff = direction.z * scale;
		
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		Matrix4f matrix = stack.getLast().getMatrix();
		stack.push();
			RenderSystem.enableBlend();
			mc.getTextureManager().bindTexture(condition.getIconTexture(affecting));
			buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
				buffer.pos(matrix, (float)(pos.getX() - xOff), (float)(pos.getY() + yOff), (float)(pos.getZ() - zOff)).tex(1, 0).endVertex();
				buffer.pos(matrix, (float)(pos.getX() + xOff), (float)(pos.getY() + yOff), (float)(pos.getZ() + zOff)).tex(0, 0).endVertex();
				buffer.pos(matrix, (float)(pos.getX() + xOff), (float)(pos.getY() - yOff), (float)(pos.getZ() + zOff)).tex(0, 1).endVertex();
				buffer.pos(matrix, (float)(pos.getX() - xOff), (float)(pos.getY() - yOff), (float)(pos.getZ() - zOff)).tex(1, 1).endVertex();
			buffer.finishDrawing();
    		WorldVertexBufferUploader.draw(buffer);
    		RenderSystem.disableBlend();
		stack.pop();
	}
	
	private static void spawnMistParticles(Player localPlayer, List<TileEntityPhylactery> phylacteries)
	{
		RandomSource rand = localPlayer.getRandom();
		if(phylacteries.isEmpty() || rand.nextInt(20) != 0)
			return;
		
		// Select up to 36 positions that are valid for mist effects near player
		Level world = localPlayer.getLevel();
		BlockPos origin = localPlayer.blockPosition();
		List<BlockPos> particleBlocks = Lists.newArrayList();
		int attempts = 150;
		while(particleBlocks.size() < 36 && attempts > 0)
		{
			int offX = rand.nextInt(32) - 16;
			int offY = Math.min(origin.getY(), rand.nextInt(4) - 2);
			int offZ = rand.nextInt(32) - 16;
			BlockPos pos = origin.offset(offX, offY, offZ);
			if(TileEntityPhylactery.isValidForMist(pos, world) && !particleBlocks.contains(pos))
				particleBlocks.add(pos);
			else
				--attempts;
		}
		if(particleBlocks.isEmpty())
			return;
		
		// Constrain positions to actual phylactery mist area
		List<BlockPos> actualMist = Lists.newArrayList();
		for(BlockPos pos : particleBlocks)
		{
			boolean found = false;
			for(TileEntityPhylactery phylactery : phylacteries)
				if(phylactery.isInsideMist(pos))
				{
					found = true;
					break;
				}
			
			if(found)
				actualMist.add(pos);
		}
		if(actualMist.isEmpty())
			return;
		
		// Spawn particles at all remaining positions
		double speed = 0.01D;
		double windX = (rand.nextInt(3) - 1) * speed;
		double windZ = (rand.nextInt(3) - 1) * speed;
		for(BlockPos pos : actualMist)
			world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, windX, 0, windZ);
	}
	
	private static void handleMistNotification(Player localPlayer, List<TileEntityPhylactery> phylacteries)
	{
		boolean isPlayerInMist = false;
		for(TileEntityPhylactery phylactery : phylacteries)
			if(phylactery.isInsideMist(localPlayer))
				if(isPlayerInMist = !phylactery.isOwner(localPlayer))
					break;
		
		if(!isPlayerInMist)
			phylacteryNotification = Math.max(--phylacteryNotification, -1);
		else if(phylacteryNotification < 0)
		{
			phylacteryNotification = Reference.Values.TICKS_PER_MINUTE * 3;
			
			RandomSource rand = localPlayer.getRandom();
			String translation = "gui."+Reference.ModInfo.MOD_ID+":ominous_warning_" + rand.nextInt(20);
			MutableComponent warning = Component.translatable(translation);
			MutableComponent obfuscated = Component.literal(VOHelper.obfuscateStringRandomly(warning.getString(), ChatFormatting.WHITE + "", rand.nextLong(), 0.2F, true));
			localPlayer.displayClientMessage(obfuscated, true);
			
			localPlayer.getLevel().playSound(localPlayer, localPlayer.position(), SoundEvents.AMBIENT_CAVE, SoundSource.AMBIENT, 1F, rand.nextFloat());
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent event)
	{
		Player localPlayer = mc.player;
		Player rendering = event.getEntity();
		
		PlayerData playerData = PlayerData.forPlayer(localPlayer);
		PlayerData renderData = PlayerData.forPlayer(rendering);
		if(playerData == null || renderData == null)
			return;
		
		if(rendering != localPlayer)
		{
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
			Vec3 posFeet = renderTarget.position();
			Vec3 posEyes = posFeet.add(0D, renderTarget.getEyeHeight(), 0D);
			
			Player player = mc.player;
			Vec3 posView = player.position().add(0D, player.getEyeHeight(), 0D);
			
			if(posView.distanceTo(posFeet) > 8D || posView.distanceTo(posEyes) > 8D)
				event.setCanceled(true);
		}
		
		if(skipRenderEvent)
			skipRenderEvent = false;
		else if(renderTarget instanceof Player)
		{
			PlayerData data = PlayerData.forPlayer((Player)renderTarget);
			if(!AbilityRegistry.getAbilitiesOfType(renderTarget, AbilityPhasing.class).isEmpty() || (data != null && data.getBodyCondition() != BodyCondition.ALIVE))
			{
	            event.setCanceled(true);
	            IRenderTypeBuffer.Impl iRenderTypeBuffer = mc.getRenderTypeBuffers().getBufferSource();
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
		Player player = mc.player;
		if(player != null && VOPotions.isPotionVisible(player, VOPotions.DAZED))
			event.setCanceled(true);
	}
	
	private static int dazzledTime = 0;
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onRenderDazzled(RenderGameOverlayEvent.Pre event)
	{
		if(event.getType() != ElementType.VIGNETTE || mc.player == null)
			return;
		
		Player player = mc.player;
		MobEffectInstance dazzle = player.getEffect(VOPotions.DAZZLED);
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
		Player player = mc.player;
		if(player != null && player.getRidingEntity() != null && player.getRidingEntity() instanceof IMountInventory && event.getGui() instanceof InventoryScreen)
		{
			event.setGui(null);
			PacketHandler.sendToServer(new PacketMountGui());
		}
	}
	
	public static boolean playerInWall()
	{
		Player player = mc.player;
		if(player != null)
			return IPhasingAbility.isPhasing(player) && getInWallBlockState(player) != null;
		return false;
	}
	
	private static BlockState getInWallBlockState(Player playerEntity)
	{
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for(int i = 0; i < 8; ++i)
        {
            double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getBbWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getBbWidth() * 0.8F);
            mutable.setPos(d, e, f);
            BlockState blockState = playerEntity.level.getBlockState(mutable);
            if(blockState.getRenderType() != BlockRenderType.INVISIBLE)
                return blockState;
        }
        return null;
    }
	
	@SubscribeEvent
	public static void onSilencedChatEvent(ClientChatEvent event)
	{
		Player player = mc.player;
		if(player != null && player.hasEffect(VOPotions.SILENCED) && !event.getOriginalMessage().startsWith("/"))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onDeafenedChatEvent(ClientChatReceivedEvent event)
	{
		Player player = mc.player;
		if(player != null && player.hasEffect(VOPotions.DEAFENED) && event.getType() == ChatType.CHAT)
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onDeafenedPlaySound(PlaySoundAtEntityEvent event)
	{
		Player player = mc.player;
		if(player != null && player.hasEffect(VOPotions.DEAFENED))
			event.setCanceled(true);
	}
}
