package com.lying.variousoddities.potion;

import com.lying.variousoddities.client.gui.ScreenPetrified;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionPetrified extends PotionImmobility implements IVisualPotion
{
	public PotionPetrified(int colorIn)
	{
		super("petrified", EffectType.HARMFUL, colorIn);
	}
    
    public void performEffect(LivingEntity livingEntity, int amplifier)
    {
    	if(livingEntity.getType() == EntityType.PLAYER && livingEntity.isAlive())
    		if(livingEntity.getEntityWorld().isRemote)
    			openPetrifiedScreen();
    }
    
    @OnlyIn(Dist.CLIENT)
    private void openPetrifiedScreen()
    {
    	Minecraft mc = Minecraft.getInstance();
    	Screen currentScreen = mc.currentScreen;
    	if(currentScreen == null)
    		mc.displayGuiScreen(new ScreenPetrified(mc.player));
    	else if(!(currentScreen instanceof ScreenPetrified))
    	{
    		currentScreen.closeScreen();
    		openPetrifiedScreen();
    	}
    }
}
