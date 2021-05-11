package com.lying.variousoddities.potion;

import java.util.UUID;

import com.lying.variousoddities.client.gui.ScreenParalysed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotionParalysis extends PotionVO
{
	private static final UUID PARALYSIS_UUID = UUID.fromString("94b3271f-7c76-4230-88d7-f294ee6d4f7f");
	
	public PotionParalysis(int colorIn)
	{
		super("paralysis", EffectType.HARMFUL, colorIn);
		addAttributesModifier(Attributes.MOVEMENT_SPEED, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
		addAttributesModifier(Attributes.FLYING_SPEED, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
		addAttributesModifier(Attributes.ATTACK_DAMAGE, PARALYSIS_UUID.toString(), -1.0D, Operation.MULTIPLY_TOTAL);
	}
	
    public boolean isReady(int duration, int amplifier){ return true; }
    
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
