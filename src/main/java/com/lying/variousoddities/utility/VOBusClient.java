package com.lying.variousoddities.utility;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.client.gui.IScrollableGUI;
import com.lying.variousoddities.client.renderer.RenderUtils;
import com.lying.variousoddities.entity.IMountInventory;
import com.lying.variousoddities.init.VOPotions;
import com.lying.variousoddities.network.PacketBonusJump;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.network.PacketMountGui;
import com.lying.variousoddities.network.PacketSyncVisualPotions;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.Ability;
import com.lying.variousoddities.species.abilities.AbilityBlind;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityPhasing;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityScent;
import com.lying.variousoddities.species.abilities.AbilitySize;
import com.lying.variousoddities.species.abilities.AbilitySwim;
import com.lying.variousoddities.species.abilities.IPhasingAbility;
import com.lying.variousoddities.world.savedata.ScentsManager;
import com.lying.variousoddities.world.savedata.ScentsManager.ScentMarker;
import com.lying.variousoddities.world.savedata.ScentsManager.ScentMarker.Connection;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class VOBusClient
{
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event)
	{
		((CommonProxy)VariousOddities.proxy).clearSettlements();
		BLIND_RENDERS.clear();
	}
	
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
			if(!AbilityRegistry.getAbilitiesOfType(renderTarget, AbilityPhasing.class).isEmpty())
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
		if(player != null && VOPotions.isPotionActive(player, VOPotions.DAZED))
			event.setCanceled(true);
	}
	
	private static final Map<BlockPos, Integer> BLIND_RENDERS = new HashMap<>();
	private static final int BLIND_MEMORY = Reference.Values.TICKS_PER_SECOND * 15;
	
	@SubscribeEvent
	public static void renderBlindBlocks(RenderWorldLastEvent event)
	{
		List<BlockPos> clear = Lists.newArrayList();
		BLIND_RENDERS.forEach((blockPos, ticks) -> { ticks--; if(ticks <= 0) clear.add(blockPos); else BLIND_RENDERS.put(blockPos, ticks); });
		clear.forEach((blockPos) -> { BLIND_RENDERS.remove(blockPos); });
		
		Minecraft mc = Minecraft.getInstance();
		if(mc.world == null || mc.player == null)
			return;
		World world = mc.world;
		PlayerEntity player = mc.player;
		float partialTicks = event.getPartialTicks();
		
		// Block the player is standing on
		BlockPos floor = player.isOnGround() ? player.getPosition().down() : player.getPosition();
		if(!world.isAirBlock(floor))
			BLIND_RENDERS.put(floor, BLIND_MEMORY);
		
		// Block the player is looking at
		Vector3d eyes = player.getEyePosition(partialTicks);
		Vector3d lookVec = player.getLook(partialTicks);
		Vector3d vector = eyes.add(lookVec.x * mc.playerController.getBlockReachDistance(), lookVec.y * mc.playerController.getBlockReachDistance(), lookVec.z * mc.playerController.getBlockReachDistance());
		RayTraceContext context = new RayTraceContext(eyes, vector, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, player);
		BlockRayTraceResult result = world.rayTraceBlocks(context);
		
		if(result.getType() == RayTraceResult.Type.BLOCK)
		{
			BlockPos look = result.getPos();
			if(!world.isAirBlock(look))
				BLIND_RENDERS.put(look, BLIND_MEMORY);
		}
		
		// Supplementary rendering to aid blind players in basic functioning
		if(VOBusClient.playerIsBlind() && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
		{
			BlockPos playerPos = new BlockPos(player.getPosX(), player.getPosYEye(), player.getPosZ());
			List<BlockPos> blindRenders = Lists.newArrayList();
			blindRenders.addAll(BLIND_RENDERS.keySet());
			blindRenders.sort(new Comparator<BlockPos>()
					{
						public int compare(BlockPos o1, BlockPos o2)
						{
							double o1Dist = o1.distanceSq(playerPos);
							double o2Dist = o2.distanceSq(playerPos);
							return o1Dist > o2Dist ? -1 : o1Dist < o2Dist ? 1 : 0;
						}
					});
			for(BlockPos pos : blindRenders)
				renderBlock(world.getBlockState(pos), pos, world, event.getMatrixStack(), partialTicks);
		}
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
	
	private static void renderBlock(BlockState state, BlockPos pos, World world, MatrixStack stack, float partialTicks)
	{
		BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRendererDispatcher();
		IBakedModel model = renderer.getModelForState(state);
		RenderType renderType = getStateRenderType(state);
		stack.push();
			Vector3d playerPos = Minecraft.getInstance().player.getEyePosition(partialTicks);
			stack.translate(pos.getX() - playerPos.x, pos.getY() - playerPos.y, pos.getZ() - playerPos.z);
			ForgeHooksClient.setRenderLayer(renderType);
				IVertexBuilder vertex = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(getStateRenderType(state));
				IModelData modelData = model.getModelData(world, pos, state, EmptyModelData.INSTANCE);
				renderer.getBlockModelRenderer().renderModelSmooth(world, model, state, pos, stack, vertex, false, world.rand, 0L, OverlayTexture.NO_OVERLAY, modelData);
			ForgeHooksClient.setRenderLayer(null);
			
			if(state.hasTileEntity())
			{
				TileEntity tile = world.getTileEntity(pos);
				renderTile(tile, stack, partialTicks);
			}
		stack.pop();
	}
	
	private static <E extends TileEntity> void renderTile(E tile, MatrixStack stack, float partialTicks)
	{
		TileEntityRenderer<E> tileRenderer = TileEntityRendererDispatcher.instance.getRenderer(tile);
		if(tileRenderer != null)
		{
	        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
			tileRenderer.render(tile, partialTicks, stack, buffer, 15, OverlayTexture.NO_OVERLAY);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static RenderType getStateRenderType(BlockState state)
	{
		for(RenderType type : RenderType.getBlockRenderTypes())
			if(RenderTypeLookup.canRenderInLayer(state, type))
				return type;
		return RenderTypeLookup.func_239221_b_(state);
	}
	
	public static boolean playerInWall()
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null)
			return IPhasingAbility.isPhasing(player) && getInWallBlockState(player) != null;
		return false;
	}
	
	public static boolean playerIsBlind()
	{
		PlayerEntity player = Minecraft.getInstance().player;
		if(player != null)
			return AbilityRegistry.hasAbility(player, AbilityBlind.REGISTRY_NAME);
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
	
	@SubscribeEvent
	public static void onRenderScents(RenderWorldLastEvent event)
	{
		Minecraft mc = Minecraft.getInstance();
		World world = mc.world;
		if(world == null) return;
		
		PlayerEntity player = mc.player;
		if(player == null) return;
		
		AbilityScent scent = (AbilityScent)AbilityRegistry.getAbilityByName(player, AbilityScent.REGISTRY_NAME);
		if(scent == null || !scent.isActive()) return;
		
		// Render marker network
		float partialTicks = event.getPartialTicks();
		ScentsManager manager = ScentsManager.get(world);
		List<ScentMarker> scents = Lists.newArrayList();
		manager.getAllScents().forEach((marker) -> { if(scent.isInRange(marker.getPosition(partialTicks), player)) scents.add(marker); });
		
		Vector3d camPos = mc.getRenderManager().info.getProjectedView();
        MatrixStack matrixStack = event.getMatrixStack();
		scents.forEach((marker) -> 
		{
			if(marker.isDead()) return;
			
			Vector3d origin = marker.origin();
			Random rand = new Random((long)(origin.x * origin.x + origin.z * origin.z));
			
			int color = marker.color();
			Vector3d markerPos = marker.getPosition(partialTicks);
			float duration = marker.duration() - partialTicks;
			
			float red = (float)((color & 16711680) >> 16) / 255F;
			float green = (float)((color & '\uff00') >> 8) / 255F;
			float blue = (float)((color & 255) >> 0) / 255F;
			
			float alphaByDist = MathHelper.clamp((1F - (float)(markerPos.distanceTo(camPos) / scent.range())) * 0.75F, 0F, 1F);
			float startAlpha = marker.alpha() * alphaByDist;
			drawMarker(matrixStack, markerPos, camPos, duration, red, green, blue, startAlpha, rand);
			
			for(Connection ping : marker.getConnections())
			{
				Vector3d end = ping.position();
				double dist = end.distanceTo(markerPos);
				if(dist < 0.5D)
					continue;
				
				dist = MathHelper.clamp(end.distanceTo(markerPos), 0.5D, 2.5D);
				Vector3d start = markerPos.add(end.subtract(markerPos).normalize().mul(0.45D, 0.45D, 0.45D));
				end = markerPos.add(end.subtract(markerPos).normalize().mul(dist, dist, dist));
				
				drawScent(matrixStack, start, end, camPos, red, green, blue, startAlpha * 0.8F, ping.alpha() * alphaByDist, rand);
			}
		});
	}
	
	private static void drawMarker(MatrixStack matrixStack, Vector3d pos, Vector3d eyePos, float duration, float red, float green, float blue, float alpha, Random rand)
	{
		duration = Math.min(duration, ScentMarker.DEFAULT_DURATION * 10);
		double height = Math.max(0.15D, (duration / ScentMarker.DEFAULT_DURATION) * 0.15D);
		
		int points = (int)Math.ceil(duration / (float)(Reference.Values.TICKS_PER_SECOND * 10));
		for(int i=0; i<points; i++)
		{
			double y = ((double)i / (double)points) + (rand.nextDouble() * 0.01D);
			double x = -Math.sqrt((y*y*y) * (1 - y));
			
			Vector3d position = pos.add(new Vector3d(x, y, 0D).rotateYaw((float)(Math.toRadians(rand.nextInt(360)))).mul(height, height, height));
			matrixStack.push();
				RenderUtils.drawCube(matrixStack, position, eyePos, red, green, blue, 1F, 0.1D);
			matrixStack.pop();
		}
	}
	
	private static void drawScent(MatrixStack matrixStack, Vector3d start, Vector3d end, Vector3d eyePos, float red, float green, float blue, float startAlpha, float endAlpha, Random rand)
	{
		eyePos = eyePos.subtract(0, 0.25D, 0D);
        double stepDist = 0.3D;
        Vector3d offset = end.subtract(start).normalize();
        
        double dist = end.distanceTo(start);
        float alphaDelta = endAlpha - startAlpha;
        
        double wiggleVol = 0.3D;
        
        Vector3d posA = start;
        Vector3d posB = posA.add(offset.mul(stepDist, stepDist, stepDist)).add(makeWiggleVec(offset, rand, wiggleVol));
        double time = (rand.nextDouble() * 1000D) + System.currentTimeMillis() * 0.005D;
        while(posB.distanceTo(end) > 0)
        {
        	double heightA = 0.25D * (posA.distanceTo(end) / start.distanceTo(end));
        	double heightB = 0.25D * (posB.distanceTo(end) / start.distanceTo(end));
        	
        	float alphaA = startAlpha + (alphaDelta * (float)(posA.distanceTo(start) / dist));
        	float alphaB = startAlpha + (alphaDelta * (float)(posB.distanceTo(start) / dist));
        	
        	double size = Math.min(0.1D, (heightA + heightB) * 0.5D);
        	RenderUtils.drawCube(matrixStack, posA, eyePos, red, green, blue, (alphaA + alphaB) / 2, size);
        	
//        	drawLine(matrixStack, posA, posB, eyePos, red, green, blue, alphaA, alphaB, heightA, heightB);
//        	drawLine(matrixStack, posB, posA, eyePos, red, green, blue, alphaB, alphaA, heightB, heightA);
        	
        	posA = posB;
        	
        	double maxDist = Math.min(stepDist, posB.distanceTo(end));
        	offset = end.subtract(posB).normalize();
        	posB = posB.add(offset.mul(maxDist, maxDist, maxDist));
        	
        	if(posB.distanceTo(end) > 0)
        		posB = posB.add(makeWiggleVec(end.subtract(posB).normalize(), rand, wiggleVol)).add(0D, Math.sin(time + posB.distanceTo(end)) * 0.01D, 0D);
        }
	}
	
	private static Vector3d makeWiggleVec(Vector3d direction, Random rand, double wiggleVol)
	{
		return new Vector3d((rand.nextDouble() - 0.5D) * wiggleVol, (rand.nextDouble() - 0.5D) * wiggleVol, (rand.nextDouble() - 0.5D) * wiggleVol);
	}
}
