package com.lying.variousoddities.utility;

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
import com.lying.variousoddities.init.VOMobEffects;
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
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputEvent.MouseScrollingEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenEvent.MouseScrolled;
import net.minecraftforge.client.event.ViewportEvent.ComputeFogColor;
import net.minecraftforge.client.event.ViewportEvent.RenderFog;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
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
	
	@SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
	public static void noclipFog(RenderFog event)
	{
		if(playerInWall())
		{
	        event.setNearPlaneDistance(0F);
	        event.setFarPlaneDistance(10F);
	        event.setFogShape(FogShape.SPHERE);
	        event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
	public static void noclipFogColor(ComputeFogColor event)
	{
		if(playerInWall())
		{
			event.setRed(0F);
			event.setBlue(0F);
			event.setGreen(0F);
		}
	}
	
	@SubscribeEvent
	public static void onMouseScroll(MouseScrolled.Pre event)
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
			if(player.input.jumping && abilities.canBonusJump)
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
						player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
						PacketHandler.sendToServer(new PacketBonusJump());
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onWorldRender(RenderLevelStageEvent event)
	{
		Player localPlayer = mc.player;
		if(localPlayer == null || localPlayer.getLevel() == null)
			return;
		
		// Find all loaded phylacteries
		List<TileEntityPhylactery> phylacteries = Lists.newArrayList();
//		localPlayer.getLevel().loadedTileEntityList.forEach((tile) -> { if(tile.getType() == VOTileEntities.PHYLACTERY) phylacteries.add((TileEntityPhylactery)tile); });	// FIXME Needs accessor
		
		handleMistNotification(localPlayer, phylacteries);
		spawnMistParticles(localPlayer, phylacteries);
		
		if(Minecraft.renderNames())
			displayConditions(event.getPoseStack(), localPlayer);
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
			if(renderNameplateEvent.getResult() == Event.Result.ALLOW || (mob.shouldShowName() && mob.hasCustomName()) || mob.getType() == EntityType.PLAYER)
				mobPos = mobPos.add(0D, 0.5D, 0D);
		
		Vec3 viewVec = mc.getEntityRenderDispatcher().camera.getPosition();
		Vec3 iconPos = mobPos.subtract(viewVec);
		Vec3 direction = iconPos.normalize().multiply(1D, 0D, 1D).yRot((float)Math.toRadians(90D));
		
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
		
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		Matrix4f matrix = stack.last().pose();
		stack.pushPose();
			RenderSystem.enableBlend();
			RenderSystem.setShaderTexture(0, condition.getIconTexture(affecting));
			buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
				buffer.vertex(matrix, (float)(pos.x - xOff), (float)(pos.y + yOff), (float)(pos.z - zOff)).uv(1, 0).endVertex();
				buffer.vertex(matrix, (float)(pos.x + xOff), (float)(pos.y + yOff), (float)(pos.z + zOff)).uv(0, 0).endVertex();
				buffer.vertex(matrix, (float)(pos.x + xOff), (float)(pos.y - yOff), (float)(pos.z + zOff)).uv(0, 1).endVertex();
				buffer.vertex(matrix, (float)(pos.x - xOff), (float)(pos.y - yOff), (float)(pos.z - zOff)).uv(1, 1).endVertex();
			BufferUploader.drawWithShader(buffer.end());
    		RenderSystem.disableBlend();
		stack.popPose();
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
			
			localPlayer.getLevel().playSound(localPlayer, localPlayer.blockPosition(), SoundEvents.AMBIENT_CAVE, SoundSource.AMBIENT, 1F, rand.nextFloat());
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
	            MultiBufferSource iRenderTypeBuffer = mc.renderBuffers().bufferSource();
	            event.getPoseStack().pushPose();
	            	skipRenderEvent = true;
	            	event.getRenderer().render(renderTarget, renderTarget.getYRot(), event.getPartialTick(), event.getPoseStack(), iRenderTypeBuffer, 0xffffff);
	            event.getPoseStack().popPose();
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
		event.getPoseStack().scale(scale, scale, scale);
	}
	
	@SubscribeEvent
	public static void onDazedClickEvent(InputEvent.MouseButton event)
	{
		Player player = mc.player;
		if(player != null && VOMobEffects.isPotionVisible(player, VOMobEffects.DAZED.get()))
			event.setCanceled(true);
	}
	
	private static int dazzledTime = 0;
	
	@SubscribeEvent
	public static void onRenderDazzled(RenderGuiOverlayEvent.Pre event)
	{
		if(event.getOverlay().id() != VanillaGuiOverlay.VIGNETTE.id() || mc.player == null)
			return;
		
		Player player = mc.player;
		MobEffectInstance dazzle = player.getEffect(VOMobEffects.DAZZLED.get());
		if(dazzle != null && dazzle.getDuration() > 0)
		{
			int scaledWidth = mc.getWindow().getGuiScaledWidth();
			int scaledHeight = mc.getWindow().getGuiScaledHeight();
			
			int fadeTime = Reference.Values.TICKS_PER_SECOND * 5;
			int time = 0;
			if(dazzle.getDuration() >= fadeTime)
				time = dazzledTime++;
			else
				time = dazzledTime = dazzle.getDuration();
			float alpha = Math.min(1F, (float)time / (float)fadeTime);
			
			RenderSystem.disableDepthTest();
		    RenderSystem.depthMask(false);
		    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
			RenderSystem.setShaderTexture(0, new ResourceLocation(Reference.ModInfo.MOD_ID, "textures/misc/vignette_dazzled.png"));
		    Tesselator tessellator = Tesselator.getInstance();
		    BufferBuilder bufferbuilder = tessellator.getBuilder();
		    bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			    bufferbuilder.vertex(0.0D, (double)scaledHeight, -90.0D).uv(0.0F, 1.0F).endVertex();
			    bufferbuilder.vertex((double)scaledWidth, (double)scaledHeight, -90.0D).uv(1.0F, 1.0F).endVertex();
			    bufferbuilder.vertex((double)scaledWidth, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
			    bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
		    BufferUploader.drawWithShader(bufferbuilder.end());
		    RenderSystem.depthMask(true);
		    RenderSystem.enableDepthTest();
		    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			
			event.setCanceled(true);
		}
		else
			dazzledTime = 0;
	}
	
	@SubscribeEvent
	public static void onMountUIOpen(ScreenEvent.Opening event)
	{
		Player player = mc.player;
		if(player != null && player.getVehicle() != null && player.getVehicle() instanceof IMountInventory && event.getScreen() instanceof InventoryScreen)
		{
			event.setNewScreen(null);
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
        MutableBlockPos mutable = new MutableBlockPos();
        for(int i = 0; i < 8; ++i)
        {
            double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getBbWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getBbWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = playerEntity.level.getBlockState(mutable);
            if(blockState.getRenderShape() != RenderShape.INVISIBLE)
                return blockState;
        }
        return null;
    }
	
	@SubscribeEvent
	public static void onSilencedChatEvent(ClientChatEvent event)
	{
		Player player = mc.player;
		if(player != null && player.hasEffect(VOMobEffects.SILENCED.get()) && !event.getOriginalMessage().startsWith("/"))
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onDeafenedChatEvent(ClientChatReceivedEvent event)
	{
		Player player = mc.player;
		if(player != null && player.hasEffect(VOMobEffects.DEAFENED.get()) && BuiltinRegistries.CHAT_TYPE.get(ChatType.CHAT) == event.getType())
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public static void onDeafenedPlaySound(PlaySoundEvent event)
	{
		Player player = mc.player;
		if(player != null && player.hasEffect(VOMobEffects.DEAFENED.get()))
			event.setCanceled(true);
	}
}
