package com.lying.variousoddities.potion;

import com.lying.variousoddities.client.gui.ScreenParalysed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionParalysis extends PotionImmobility
{
	public PotionParalysis(int colorIn)
	{
		super("paralysis", EffectType.HARMFUL, colorIn);
	}
    
    public void performEffect(LivingEntity livingEntity, int amplifier)
    {
    	if(livingEntity.getType() == EntityType.PLAYER && livingEntity.isAlive())
    		if(livingEntity.getEntityWorld().isRemote)
    			openParalysisScreen();
    }
    
    @OnlyIn(Dist.CLIENT)
    private void openParalysisScreen()
    {
    	Minecraft mc = Minecraft.getInstance();
    	Screen currentScreen = mc.currentScreen;
    	if(currentScreen == null)
    		mc.displayGuiScreen(new ScreenParalysed(mc.player));
    	else if(!(currentScreen instanceof ScreenParalysed))
    	{
    		currentScreen.closeScreen();
    		openParalysisScreen();
    	}
    }
}
