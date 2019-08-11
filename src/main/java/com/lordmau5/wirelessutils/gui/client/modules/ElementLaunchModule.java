package com.lordmau5.wirelessutils.gui.client.modules;

import cofh.core.gui.element.ElementSlider;
import cofh.core.gui.element.listbox.SliderHorizontal;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementFilterableModule;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemLaunchModule;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public class ElementLaunchModule extends ElementFilterableModule {

    public final ItemLaunchModule.LaunchBehavior behavior;

    public final ElementSlider sliderX;
    public final SliderHorizontal sliderY;
    public final SliderHorizontal sliderZ;

    public ElementLaunchModule(GuiBaseVaporizer gui, ItemLaunchModule.LaunchBehavior behavior) {
        super(gui, behavior);
        this.behavior = behavior;

        sliderX = new SliderHorizontal(gui, 57, 9, 110, 10, 100) {
            @Override
            public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
                if ( mouseButton == 1 )
                    setValue(0);

                return super.onMousePressed(mouseX, mouseY, mouseButton);
            }

            @Override
            public void onValueChanged(int value) {
                ItemStack stack = behavior.vaporizer.getModule();
                ModItems.itemLaunchModule.setXSpeed(stack, sliderX.getValue() / 10F);
                behavior.vaporizer.setModule(stack);
                behavior.vaporizer.sendModePacket();
            }
        };

        sliderY = new SliderHorizontal(gui, 57, 24, 110, 10, 100) {
            @Override
            public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
                if ( mouseButton == 1 )
                    setValue(0);

                return super.onMousePressed(mouseX, mouseY, mouseButton);
            }

            @Override
            public void onValueChanged(int value) {
                ItemStack stack = behavior.vaporizer.getModule();
                ModItems.itemLaunchModule.setYSpeed(stack, sliderY.getValue() / 10F);
                behavior.vaporizer.setModule(stack);
                behavior.vaporizer.sendModePacket();
            }
        };

        sliderZ = new SliderHorizontal(gui, 57, 39, 110, 10, 100) {
            @Override
            public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
                if ( mouseButton == 1 )
                    setValue(0);

                return super.onMousePressed(mouseX, mouseY, mouseButton);
            }

            @Override
            public void onValueChanged(int value) {
                ItemStack stack = behavior.vaporizer.getModule();
                ModItems.itemLaunchModule.setZSpeed(stack, sliderZ.getValue() / 10F);
                behavior.vaporizer.setModule(stack);
                behavior.vaporizer.sendModePacket();
            }
        };

        sliderX.setLimits(-100, 100);
        sliderY.setLimits(-100, 100);
        sliderZ.setLimits(-100, 100);

        addElement(sliderX);
        addElement(sliderY);
        addElement(sliderZ);
    }


    public int getContentHeight() {
        return 59;
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        sliderX.setValue((int) Math.floor(behavior.getSpeedX() * 10));
        sliderY.setValue((int) Math.floor(behavior.getSpeedY() * 10));
        sliderZ.setValue((int) Math.floor(behavior.getSpeedZ() * 10));
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        FontRenderer fontRenderer = getFontRenderer();

        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".x"), 8, posY + 11, 0x404040);
        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".y"), 8, posY + 26, 0x404040);
        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".z"), 8, posY + 41, 0x404040);

        gui.drawRightAlignedText(String.format("%.1f", behavior.getSpeedX()), 52, posY + 11, 0);
        gui.drawRightAlignedText(String.format("%.1f", behavior.getSpeedY()), 52, posY + 26, 0);
        gui.drawRightAlignedText(String.format("%.1f", behavior.getSpeedZ()), 52, posY + 41, 0);
    }
}
