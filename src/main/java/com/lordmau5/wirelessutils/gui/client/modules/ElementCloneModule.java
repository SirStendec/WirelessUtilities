package com.lordmau5.wirelessutils.gui.client.modules;

import cofh.core.gui.element.listbox.SliderHorizontal;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemCloneModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.IllegalFormatException;
import java.util.List;

public class ElementCloneModule extends ElementModuleBase {

    private final ItemCloneModule.CloneBehavior behavior;

    private final SliderHorizontal sliderLimit;
    private final ElementDynamicContainedButton btnExact;

    private boolean ready = false;

    public ElementCloneModule(GuiBaseVaporizer gui, ItemCloneModule.CloneBehavior behavior) {
        super(gui);
        this.behavior = behavior;

        sliderLimit = new SliderHorizontal(gui, 8, 57, sizeX - 16, 10, ModConfig.vaporizers.modules.clone.maxCount) {
            @Override
            public void addTooltip(List<String> list) {
                super.addTooltip(list);

                String[] lines = TextHelpers.getLocalizedLines("item." + WirelessUtils.MODID + ".clone_module.limit.info");
                if ( lines == null )
                    return;

                String range = StringHelper.formatNumber(ModConfig.vaporizers.modules.clone.maxRange);
                for (String line : lines) {
                    try {
                        list.add(String.format(line, range));
                    } catch (IllegalFormatException ex) {
                        list.add("Format Error: " + line);
                    }
                }
            }

            @Override
            public void onValueChanged(int value) {
                if ( ready && value != behavior.getEntityLimit() ) {
                    ItemStack stack = behavior.vaporizer.getModule();
                    ModItems.itemCloneModule.setEntityLimit(stack, sliderLimit.getValue());
                    behavior.vaporizer.setModule(stack);
                    behavior.vaporizer.sendModePacket();
                }
            }
        };

        sliderLimit.setLimits(1, ModConfig.vaporizers.modules.clone.maxCount);
        addElement(sliderLimit);

        btnExact = new ElementDynamicContainedButton(this, "Exact", 8, 22, 160, 16, "");
        btnExact.setVisible(behavior.canExact());
        addElement(btnExact);

        ready = true;
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        super.drawForeground(mouseX, mouseY);

        FontRenderer render = getFontRenderer();

        render.drawString(StringHelper.localizeFormat("item." + WirelessUtils.MODID + ".clone_module.limit", ""), 8, posY + 44, 0x404040);
        gui.drawRightAlignedText(StringHelper.formatNumber(behavior.getEntityLimit()), sizeX - 7, posY + 44, 0);

        if ( !behavior.hasEntity() )
            return;

        int cost = behavior.getCost();
        String sCost = StringHelper.formatNumber(cost);

        render.drawString(StringHelper.localize("btn." + WirelessUtils.MODID + ".cost"), 8, posY + 9, 0x404040);

        if ( gui.getVaporizer().hasFluid() )
            sCost = new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".cost_fluid",
                    sCost,
                    StringHelper.formatNumber(cost * gui.getVaporizer().getFluidRate())
            ).getFormattedText();

        gui.drawRightAlignedText(sCost, sizeX - 7, posY + 9, 0);
    }

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        boolean locked = isLocked();

        sliderLimit.setEnabled(!locked);
        btnExact.setEnabled(!locked);

        sliderLimit.setValue(behavior.getEntityLimit());

        btnExact.setText(StringHelper.localize("item." + WirelessUtils.MODID + ".clone_module.exact" +
                (behavior.isExact() ? "" : ".off")
        ));
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        if ( buttonName.equals("Exact") ) {
            TileBaseVaporizer vaporizer = behavior.vaporizer;
            ItemStack stack = vaporizer.getModule();

            if ( ModItems.itemCloneModule.setExactCopies(stack, !ModItems.itemCloneModule.getExactCopies(stack)).isEmpty() )
                return;

            BaseGuiContainer.playClickSound(1F);
            vaporizer.setModule(stack);
            vaporizer.sendModePacket();
            return;
        }

        super.handleElementButtonClick(buttonName, mouseButton);
    }
}
