package com.lying.variousoddities.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.VariousOddities;
import com.lying.variousoddities.capabilities.Abilities;
import com.lying.variousoddities.capabilities.LivingData;
import com.lying.variousoddities.client.gui.IScrollableGUI;
import com.lying.variousoddities.network.PacketAirJump;
import com.lying.variousoddities.network.PacketHandler;
import com.lying.variousoddities.proxy.CommonProxy;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityBlind;
import com.lying.variousoddities.species.abilities.AbilityFlight;
import com.lying.variousoddities.species.abilities.AbilityPhasing;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
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
	public static void onLivingJump(LivingUpdateEvent event)
	{
		if(event.getEntityLiving() == Minecraft.getInstance().player)
		{
			ClientPlayerEntity player = (ClientPlayerEntity)event.getEntityLiving();
			LivingData data = LivingData.forEntity(event.getEntityLiving());
			Abilities abilities = data.getAbilities();
			if(!player.isOnGround() && abilities.canAirJump && player.movementInput.jump)
				if(AbilityRegistry.hasAbility(player, AbilityFlight.REGISTRY_NAME) && ((AbilityFlight)AbilityRegistry.getAbilityByName(player, AbilityFlight.REGISTRY_NAME)).active())
				{
					abilities.doAirJump();
					player.connection.sendPacket(new CEntityActionPacket(player, CEntityActionPacket.Action.START_FALL_FLYING));
					PacketHandler.sendToServer(new PacketAirJump());
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
			for(BlockPos pos : BLIND_RENDERS.keySet())
				renderBlock(world.getBlockState(pos), pos, world, event.getMatrixStack(), partialTicks);
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
		stack.pop();
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
			return AbilityRegistry.hasAbility(player, AbilityPhasing.class) && getInWallBlockState(player) != null;
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
}
