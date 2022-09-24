package com.lying.variousoddities.client.renderer.entity;

import com.lying.variousoddities.entity.EntitySpell;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntitySpellRenderer extends EntityRenderer<EntitySpell>
{
	@SuppressWarnings("unused")
	private final EntityRendererProvider.Context manager;
	
	public EntitySpellRenderer(EntityRendererProvider.Context renderManager)
	{
		super(renderManager);
		manager = renderManager;
	}
	
	public ResourceLocation getTextureLocation(EntitySpell entity)
	{
		return null;
	}
	
	public boolean shouldRender(EntitySpell livingEntityIn, Frustum camera, double camX, double camY, double camZ)
    {
    	return true;
    }
	
    public void doRender(EntitySpell entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
//    	if(BusSpells.shouldShowSpellForPlayer(entity.getSpell(), manager))
//    	{
//    		renderItem(entity, x, y, z);
//	        
//	        if(entity.getSpell() != null)
//	        	drawCircle(entity, x, y, z);
//	        
//    		if(BusSpells.shouldShowSpellForPlayer(entity.getSpell(), manager))
//    			renderName(entity, x, y, z);
//    	}
    }
    
//    private void renderItem(EntitySpell entity, double x, double y, double z)
//    {
//        GlStateManager.pushMatrix();
//	    	double posY = (entity.height * 0.5D) + (Math.sin((float)entity.ticksExisted / (float)(Reference.Values.TICKS_PER_SECOND * 2)) * 0.1D);
//	    	GlStateManager.translate(x, y + posY, z);
//	    	
//	    	Vec3d viewPos = new Vec3d(renderManager.viewerPosX, renderManager.viewerPosY + renderManager.renderViewEntity.getEyeHeight(), renderManager.viewerPosZ);
//	    	Vec3d offset = new Vec3d(viewPos.x - entity.posX, viewPos.y - entity.posY, viewPos.z - entity.posZ);
//	    	
//	    	float viewYaw = (float)(Math.atan2(offset.x, offset.z) * 180/Math.PI) + 180F;
//	    	GlStateManager.rotate(viewYaw, 0F, 1F, 0F);
//	    	
//	    	float viewPit = (float)(Math.asin(offset.y/offset.lengthVector()) * 180/Math.PI);
//	    	GlStateManager.rotate(viewPit, 1F, 0F, 0F);
//	    	
//	        GlStateManager.disableLighting();
//	        GlStateManager.scale(0.5F, 0.5F, 0.5F);
//	        GlStateManager.pushAttrib();
//	        RenderHelper.enableStandardItemLighting();
//	        Minecraft.getMinecraft().getRenderItem().renderItem(new ItemStack(VOItems.SPELL_SCROLL), ItemCameraTransforms.TransformType.FIXED);
//	        RenderHelper.disableStandardItemLighting();
//	        GlStateManager.popAttrib();
//	        GlStateManager.enableLighting();
//	    GlStateManager.popMatrix();
//    }
    
//    private void drawCircle(EntitySpell entity, double x, double y, double z)
//    {
//    	if(entity == null || entity.getSpell() == null) return;
//        bindTexture(RenderMagicHelper.CIRCLE_TEXTURE1);
//    	RenderMagicHelper.drawCircle(x, y + entity.height / 2, z, entity.ticksExisted, entity.width, entity.getSpell().getSpell().getSchool().getColour());
//    }
    
//    protected void renderName(EntitySpell entity, double x, double y, double z)
//    {
//        if(Minecraft.isGuiEnabled() && entity.getSpell() != null && BusSpells.isSpellTargeted(entity.getSpell(), 64.0F))
//        {
//            String s = entity.getSpell().getDisplayName();
//            this.renderLivingLabel(entity, s, x, y, z, 64);
//        }
//    }
}
