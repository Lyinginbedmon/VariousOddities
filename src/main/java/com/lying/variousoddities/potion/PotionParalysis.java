package com.lying.variousoddities.potion;

import com.lying.variousoddities.client.gui.ScreenParalysed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionParalysis extends PotionImmobility
{
	public PotionParalysis(int colorIn)
	{
		super(MobEffectCategory.HARMFUL, colorIn);
	}
    
    public void applyEffectTick(LivingEntity livingEntity, int amplifier)
    {
    	if(livingEntity.getType() == EntityType.PLAYER && livingEntity.isAlive())
    		if(livingEntity.getLevel().isClientSide)
    			openParalysisScreen();
    }
    
    @OnlyIn(Dist.CLIENT)
    private void openParalysisScreen()
    {
    	Minecraft mc = Minecraft.getInstance();
    	Screen currentScreen = mc.screen;
    	if(currentScreen == null)
    		mc.setScreen(new ScreenParalysed(mc.player));
    	else if(!(currentScreen instanceof ScreenParalysed))
    	{
    		currentScreen.onClose();
    		openParalysisScreen();
    	}
    }
}
