package com.lordmau5.wirelessutils.gui.client.modules;

import cofh.core.gui.element.ElementSlider;
import cofh.core.gui.element.listbox.SliderHorizontal;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementFilterableModule;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemLaunchModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;

public class ElementLaunchModule extends ElementFilterableModule {

    public final ItemLaunchModule.LaunchBehavior behavior;

    public final ElementSlider sliderX;
    public final SliderHorizontal sliderY;
    public final SliderHorizontal sliderZ;
    public final ElementDynamicContainedButton btnFallProtect;

    public ElementLaunchModule(GuiBaseVaporizer gui, ItemLaunchModule.LaunchBehavior behavior) {
        super(gui, behavior);
        this.behavior = behavior;

        sliderX = new SliderHorizontal(gui, 57, 9, 110, 10, 400) {
            @Override
            public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
                if ( mouseButton == 1 )
                    setValue(0);

                return super.onMousePressed(mouseX, mouseY, mouseButton);
            }

            @Override
            public void onValueChanged(int value) {
                ItemStack stack = behavior.vaporizer.getModule();
                ModItems.itemLaunchModule.setXSpeed(stack, sliderX.getValue() / 100F);
                behavior.vaporizer.setModule(stack);
                behavior.vaporizer.sendModePacket();
            }
        };

        sliderY = new SliderHorizontal(gui, 57, 24, 110, 10, 400) {
            @Override
            public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
                if ( mouseButton == 1 )
                    setValue(0);

                return super.onMousePressed(mouseX, mouseY, mouseButton);
            }

            @Override
            public void onValueChanged(int value) {
                ItemStack stack = behavior.vaporizer.getModule();
                ModItems.itemLaunchModule.setYSpeed(stack, sliderY.getValue() / 100F);
                behavior.vaporizer.setModule(stack);
                behavior.vaporizer.sendModePacket();
            }
        };

        sliderZ = new SliderHorizontal(gui, 57, 39, 110, 10, 400) {
            @Override
            public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
                if ( mouseButton == 1 )
                    setValue(0);

                return super.onMousePressed(mouseX, mouseY, mouseButton);
            }

            @Override
            public void onValueChanged(int value) {
                ItemStack stack = behavior.vaporizer.getModule();
                ModItems.itemLaunchModule.setZSpeed(stack, sliderZ.getValue() / 100F);
                behavior.vaporizer.setModule(stack);
                behavior.vaporizer.sendModePacket();
            }
        };

        btnFallProtect = new ElementDynamicContainedButton(this, "FallProtect", 91, 54, 78, 16, "");
        btnFallProtect.setToolTipLines("btn." + WirelessUtils.MODID + ".fall_protect.info");

        btnFallProtect.setVisible(ModConfig.vaporizers.modules.launch.allowFallProtect);

        sliderX.setLimits(-400, 400);
        sliderY.setLimits(-400, 400);
        sliderZ.setLimits(-400, 400);

        addElement(sliderX);
        addElement(sliderY);
        addElement(sliderZ);
        addElement(btnFallProtect);
    }


    public int getContentHeight() {
        if ( ModConfig.vaporizers.modules.launch.allowFallProtect )
            return 75;

        return 59;
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        boolean locked = isLocked();

        sliderX.setEnabled(!locked);
        sliderY.setEnabled(!locked);
        sliderZ.setEnabled(!locked);

        btnFallProtect.setEnabled(!locked);

        sliderX.setValue((int) Math.floor(behavior.getSpeedX() * 100));
        sliderY.setValue((int) Math.floor(behavior.getSpeedY() * 100));
        sliderZ.setValue((int) Math.floor(behavior.getSpeedZ() * 100));

        btnFallProtect.setText(StringHelper.localize("btn." + WirelessUtils.MODID + ".fall_protect." + (behavior.getFallProtect() ? 1 : 0)));
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;

        TileBaseVaporizer vaporizer = behavior.vaporizer;
        ItemStack stack = vaporizer.getModule();

        switch (buttonName) {
            case "FallProtect":
                if ( ModItems.itemLaunchModule.setFallProtect(stack, !behavior.getFallProtect()).isEmpty() )
                    return;
                break;

            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        BaseGuiContainer.playClickSound(pitch);
        vaporizer.setModule(stack);
        vaporizer.sendModePacket();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        FontRenderer fontRenderer = getFontRenderer();

        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".x"), 8, posY + 11, 0x404040);
        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".y"), 8, posY + 26, 0x404040);
        fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".z"), 8, posY + 41, 0x404040);

        if ( ModConfig.vaporizers.modules.launch.allowFallProtect )
            fontRenderer.drawString(StringHelper.localize("btn." + WirelessUtils.MODID + ".fall_protect"), 8, posY + 58, 0x404040);

        gui.drawRightAlignedText(String.format("%.2f", behavior.getSpeedX()), 52, posY + 11, 0);
        gui.drawRightAlignedText(String.format("%.2f", behavior.getSpeedY()), 52, posY + 26, 0);
        gui.drawRightAlignedText(String.format("%.2f", behavior.getSpeedZ()), 52, posY + 41, 0);
    }
}
