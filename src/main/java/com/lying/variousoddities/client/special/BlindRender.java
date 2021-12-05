package com.lying.variousoddities.client.special;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityBlind;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class BlindRender
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static PlayerEntity player = mc.player;
	
	private static boolean blindnessActive = false;
	
	private static final Map<BlockPos, Integer> BLIND_RENDERS = new HashMap<>();
	private static final int BLOCK_MEMORY = Reference.Values.TICKS_PER_SECOND * 15;
	private static final double MEMORY_RANGE = 6D * 6D;
	
	/** Returns true if the local player has the Blind ability */
	public static boolean playerIsBlind()
	{
		return blindnessActive;
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event)
	{
		BLIND_RENDERS.clear();
	}
	
	@SubscribeEvent
	public static void renderBlindBlocks(RenderWorldLastEvent event)
	{
		float partialTicks = event.getPartialTicks();
		if(player == null)
			player = mc.player;
		if(player == null || player.getEntityWorld() == null)
			return;
		World world = player.getEntityWorld();
		
		List<BlockPos> clear = Lists.newArrayList();
		BLIND_RENDERS.forEach((blockPos, ticks) -> 
			{
				if(player.getPosition().distanceSq(blockPos) < MEMORY_RANGE)
					BLIND_RENDERS.put(blockPos, Math.min(++ticks, BLOCK_MEMORY));
				else
				{
					ticks--;
					if(ticks <= 0)
						clear.add(blockPos);
					else
						BLIND_RENDERS.put(blockPos, ticks);
				}
			});
		clear.forEach((blockPos) -> { BLIND_RENDERS.remove(blockPos); });
		
		// Block the player is standing on
		BlockPos floor = player.getPosition();
		for(int i=0; i<player.getHeight(); i++)
		{
			registerBlock(floor.up(i), world);
			for(Direction facing : Direction.values())
				registerBlock(floor.up(i).offset(facing), world);
		}
		
		// Block the player is looking at
		Vector3d eyes = player.getEyePosition(partialTicks);
		Vector3d lookVec = player.getLook(partialTicks);
		Vector3d vector = eyes.add(lookVec.x * mc.playerController.getBlockReachDistance(), lookVec.y * mc.playerController.getBlockReachDistance(), lookVec.z * mc.playerController.getBlockReachDistance());
		RayTraceContext context = new RayTraceContext(eyes, vector, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, player);
		BlockRayTraceResult result = world.rayTraceBlocks(context);
		
		if(result.getType() == RayTraceResult.Type.BLOCK)
			registerBlock(result.getPos(), world);
		
		blindnessActive = AbilityRegistry.hasAbility(player, AbilityBlind.REGISTRY_NAME);
		
		// Supplementary rendering to aid blind players in basic functioning
		if(blindnessActive && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
		{
			BlockPos playerPos = new BlockPos(mc.getRenderManager().info.getProjectedView());
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
				renderBlock(world.getBlockState(pos), pos, world, event.getMatrixStack(), (float)BLIND_RENDERS.get(pos) / (float)BLOCK_MEMORY, partialTicks);
		}
	}
	
	private static void registerBlock(BlockPos pos, World world)
	{
		if(!world.isAirBlock(pos))
			BLIND_RENDERS.put(pos, BLOCK_MEMORY);
	}
	
	private static void renderBlock(BlockState state, BlockPos pos, World world, MatrixStack stack, float alpha, float partialTicks)
	{
		BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRendererDispatcher();
		IBakedModel model = renderer.getModelForState(state);
		RenderType renderType = getStateRenderType(state);
		stack.push();
			Vector3d playerPos = mc.getRenderManager().info.getProjectedView();
			double alphaOffset = (1F - alpha) * 0.5D;
			stack.translate(pos.getX() - playerPos.x + alphaOffset, pos.getY() - playerPos.y + alphaOffset, pos.getZ() - playerPos.z + alphaOffset);
			stack.push();
				stack.scale(alpha, alpha, alpha);
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
}
