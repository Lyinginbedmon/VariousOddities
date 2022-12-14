package com.lying.variousoddities.client.special;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityBlind;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class BlindRender
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static Player player = mc.player;
	
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
	public static void onWorldUnload(LevelEvent.Unload event)
	{
		BLIND_RENDERS.clear();
	}
	
	@SubscribeEvent
	public static void renderBlindBlocks(RenderLevelStageEvent event)
	{
		float partialTicks = event.getPartialTick();
		if(player == null)
			player = mc.player;
		if(player == null || player.getLevel() == null)
			return;
		Level world = player.getLevel();
		
		List<BlockPos> clear = Lists.newArrayList();
		BLIND_RENDERS.forEach((blockPos, ticks) -> 
			{
				if(player.blockPosition().distSqr(blockPos) < MEMORY_RANGE)
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
		BlockPos floor = player.blockPosition();
		for(int i=0; i<player.getBbHeight(); i++)
		{
			registerBlock(floor.above(i), world);
			for(Direction facing : Direction.values())
				registerBlock(floor.above(i).relative(facing), world);
		}
		
		// Block the player is looking at
		Vec3 eyes = player.getEyePosition(partialTicks);
		Vec3 lookVec = player.getLookAngle();
		Vec3 vector = eyes.add(lookVec.x * mc.gameMode.getPickRange(), lookVec.y * mc.gameMode.getPickRange(), lookVec.z * mc.gameMode.getPickRange());
		ClipContext context = new ClipContext(eyes, vector, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player);
		BlockHitResult result = world.clip(context);
		
		if(result.getType() == HitResult.Type.BLOCK)
			registerBlock(result.getBlockPos(), world);
		
		blindnessActive = AbilityRegistry.hasAbilityOfMapName(player, AbilityRegistry.getClassRegistryKey(AbilityBlind.class).location());
		
		// Supplementary rendering to aid blind players in basic functioning
		if(blindnessActive && mc.options.getCameraType() == CameraType.FIRST_PERSON)
		{
			BlockPos playerPos = new BlockPos(mc.getEntityRenderDispatcher().camera.getPosition());
			List<BlockPos> blindRenders = Lists.newArrayList();
			blindRenders.addAll(BLIND_RENDERS.keySet());
			blindRenders.sort(new Comparator<BlockPos>()
					{
						public int compare(BlockPos o1, BlockPos o2)
						{
							double o1Dist = o1.distSqr(playerPos);
							double o2Dist = o2.distSqr(playerPos);
							return o1Dist > o2Dist ? -1 : o1Dist < o2Dist ? 1 : 0;
						}
					});
			for(BlockPos pos : blindRenders)
				renderBlock(world.getBlockState(pos), pos, world, event.getPoseStack(), (float)BLIND_RENDERS.get(pos) / (float)BLOCK_MEMORY, partialTicks);
		}
	}
	
	private static void registerBlock(BlockPos pos, Level world)
	{
		if(!world.isEmptyBlock(pos))
			BLIND_RENDERS.put(pos, BLOCK_MEMORY);
	}
	
	private static void renderBlock(BlockState state, BlockPos pos, Level world, PoseStack stack, float alpha, float partialTicks)
	{
		BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = renderer.getBlockModel(state);
		stack.pushPose();
			Vec3 playerPos = mc.getEntityRenderDispatcher().camera.getPosition();
			double alphaOffset = (1F - alpha) * 0.5D;
			stack.translate(pos.getX() - playerPos.x + alphaOffset, pos.getY() - playerPos.y + alphaOffset, pos.getZ() - playerPos.z + alphaOffset);
			stack.pushPose();
				stack.scale(alpha, alpha, alpha);
				ModelData modelData = model.getModelData(world, pos, state, ModelData.EMPTY);
				for(RenderType renderType : model.getRenderTypes(state, world.getRandom(), modelData))
				{
					VertexConsumer vertex = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderType);
					renderer.getModelRenderer().tesselateWithAO(world, model, state, pos, stack, vertex, false, world.getRandom(), 0, OverlayTexture.NO_OVERLAY, modelData, renderType);
				}
				
				if(state.hasBlockEntity())
				{
					BlockEntity tile = world.getBlockEntity(pos);
					renderTile(tile, stack, partialTicks);
				}
			stack.popPose();
		stack.popPose();
	}
	
	private static <E extends BlockEntity> void renderTile(E tile, PoseStack stack, float partialTicks)
	{
		BlockEntityRenderer<E> tileRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
		if(tileRenderer != null)
		{
	        MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			tileRenderer.render(tile, partialTicks, stack, buffer, 15, OverlayTexture.NO_OVERLAY);
		}
	}
}
