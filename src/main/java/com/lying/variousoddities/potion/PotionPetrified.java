package com.lying.variousoddities.potion;

import com.lying.variousoddities.client.gui.screen.ScreenPetrified;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionPetrified extends PotionImmobility implements IVisualPotion
{
	public PotionPetrified(int colorIn)
	{
		super(MobEffectCategory.HARMFUL, colorIn);
	}
    
    public void applyEffectTick(LivingEntity livingEntity, int amplifier)
    {
    	if(livingEntity.getType() == EntityType.PLAYER && livingEntity.isAlive())
    		if(livingEntity.getLevel().isClientSide)
    			openPetrifiedScreen();
    }
    
    @OnlyIn(Dist.CLIENT)
    private void openPetrifiedScreen()
    {
    	Minecraft mc = Minecraft.getInstance();
    	Screen currentScreen = mc.screen;
    	if(currentScreen == null)
    		mc.setScreen(new ScreenPetrified(mc.player));
    	else if(!(currentScreen instanceof ScreenPetrified))
    	{
    		currentScreen.onClose();
    		openPetrifiedScreen();
    	}
    }
}
