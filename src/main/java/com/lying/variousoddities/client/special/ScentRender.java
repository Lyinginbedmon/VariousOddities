package com.lying.variousoddities.client.special;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.lying.variousoddities.client.renderer.RenderUtils;
import com.lying.variousoddities.reference.Reference;
import com.lying.variousoddities.species.abilities.AbilityRegistry;
import com.lying.variousoddities.species.abilities.AbilityScent;
import com.lying.variousoddities.world.savedata.ScentsManager;
import com.lying.variousoddities.world.savedata.ScentsManager.ScentMarker;
import com.lying.variousoddities.world.savedata.ScentsManager.ScentMarker.Connection;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ScentRender
{
	private static final Minecraft mc = Minecraft.getInstance();
	private static Player player = mc.player;
	
	@SubscribeEvent
	public static void onRenderScents(RenderLevelStageEvent event)
	{
		if(player == null || player.getLevel() == null) return;
		Level world = player.getLevel();
		
		AbilityScent scent = (AbilityScent)AbilityRegistry.getAbilityByMapName(player, AbilityRegistry.getClassRegistryKey(AbilityScent.class).location());
		if(scent == null || !scent.isActive()) return;
		
		// Render marker network
		float partialTicks = event.getPartialTick();
		ScentsManager manager = ScentsManager.get(world);
		List<ScentMarker> scents = Lists.newArrayList();
		manager.getAllScents().forEach((marker) -> { if(scent.isInRange(marker.getPosition(partialTicks), player)) scents.add(marker); });
		
		Vec3 camPos = mc.getEntityRenderDispatcher().camera.getPosition();
        PoseStack matrixStack = event.getPoseStack();
		scents.forEach((marker) -> 
		{
			if(marker.isDead()) return;
			
			Vec3 origin = marker.origin();
			Random rand = new Random((long)(origin.x * origin.x + origin.z * origin.z));
			
			int color = marker.color();
			Vec3 markerPos = marker.getPosition(partialTicks);
			float duration = marker.duration() - partialTicks;
			
			float red = (float)((color & 16711680) >> 16) / 255F;
			float green = (float)((color & '\uff00') >> 8) / 255F;
			float blue = (float)((color & 255) >> 0) / 255F;
			
			float alphaByDist = Mth.clamp((1F - (float)(markerPos.distanceTo(camPos) / scent.range())) * 0.75F, 0F, 1F);
			float startAlpha = marker.alpha() * alphaByDist;
			drawMarker(matrixStack, markerPos, camPos, duration, red, green, blue, startAlpha, rand);
			
			for(Connection ping : marker.getConnections())
			{
				Vec3 end = ping.position();
				double dist = end.distanceTo(markerPos);
				if(dist < 0.5D)
					continue;
				
				dist = Mth.clamp(end.distanceTo(markerPos), 0.5D, 2.5D);
				Vec3 start = markerPos.add(end.subtract(markerPos).normalize().multiply(0.45D, 0.45D, 0.45D));
				end = markerPos.add(end.subtract(markerPos).normalize().multiply(dist, dist, dist));
				
				drawScent(matrixStack, start, end, camPos, red, green, blue, startAlpha * 0.8F, ping.alpha() * alphaByDist, rand);
			}
		});
	}
	
	private static void drawMarker(PoseStack matrixStack, Vec3 pos, Vec3 eyePos, float duration, float red, float green, float blue, float alpha, Random rand)
	{
		duration = Math.min(duration, ScentMarker.DEFAULT_DURATION * 10);
		double height = Math.max(0.15D, (duration / ScentMarker.DEFAULT_DURATION) * 0.15D);
		
		int points = (int)Math.ceil(duration / (float)(Reference.Values.TICKS_PER_SECOND * 10));
		for(int i=0; i<points; i++)
		{
			double y = ((double)i / (double)points) + (rand.nextDouble() * 0.01D);
			double x = -Math.sqrt((y*y*y) * (1 - y));
			
			Vec3 position = pos.add(new Vec3(x, y, 0D).yRot((float)(Math.toRadians(rand.nextInt(360)))).multiply(height, height, height));
			matrixStack.pushPose();
				RenderUtils.drawCube(matrixStack, position, eyePos, red, green, blue, 1F, 0.1D);
			matrixStack.popPose();
		}
	}
	
	private static void drawScent(PoseStack matrixStack, Vec3 start, Vec3 end, Vec3 eyePos, float red, float green, float blue, float startAlpha, float endAlpha, Random rand)
	{
		eyePos = eyePos.subtract(0, 0.25D, 0D);
        double stepDist = 0.3D;
        Vec3 offset = end.subtract(start).normalize();
        
        double dist = end.distanceTo(start);
        float alphaDelta = endAlpha - startAlpha;
        
        double wiggleVol = 0.3D;
        
        Vec3 posA = start;
        Vec3 posB = posA.add(offset.multiply(stepDist, stepDist, stepDist)).add(makeWiggleVec(offset, rand, wiggleVol));
        double time = (rand.nextDouble() * 1000D) + System.currentTimeMillis() * 0.005D;
        while(posB.distanceTo(end) > 0)
        {
        	double heightA = 0.25D * (posA.distanceTo(end) / start.distanceTo(end));
        	double heightB = 0.25D * (posB.distanceTo(end) / start.distanceTo(end));
        	
        	float alphaA = startAlpha + (alphaDelta * (float)(posA.distanceTo(start) / dist));
        	float alphaB = startAlpha + (alphaDelta * (float)(posB.distanceTo(start) / dist));
        	
        	double size = Math.min(0.1D, (heightA + heightB) * 0.5D);
        	RenderUtils.drawCube(matrixStack, posA, eyePos, red, green, blue, (alphaA + alphaB) / 2, size);
        	
        	posA = posB;
        	
        	double maxDist = Math.min(stepDist, posB.distanceTo(end));
        	offset = end.subtract(posB).normalize();
        	posB = posB.add(offset.multiply(maxDist, maxDist, maxDist));
        	
        	if(posB.distanceTo(end) > 0)
        		posB = posB.add(makeWiggleVec(end.subtract(posB).normalize(), rand, wiggleVol)).add(0D, Math.sin(time + posB.distanceTo(end)) * 0.01D, 0D);
        }
	}
	
	private static Vec3 makeWiggleVec(Vec3 direction, Random rand, double wiggleVol)
	{
		return new Vec3((rand.nextDouble() - 0.5D) * wiggleVol, (rand.nextDouble() - 0.5D) * wiggleVol, (rand.nextDouble() - 0.5D) * wiggleVol);
	}
}
