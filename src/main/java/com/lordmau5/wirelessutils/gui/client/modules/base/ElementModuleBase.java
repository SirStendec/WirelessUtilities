package com.lordmau5.wirelessutils.gui.client.modules.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.base.ItemBase;
import net.minecraft.item.ItemStack;

public class ElementModuleBase extends PageBase {

    protected final GuiBaseVaporizer gui;

    public ElementModuleBase(GuiBaseVaporizer gui) {
        super(gui, 0, 20);
        this.gui = gui;

        setLabel("btn." + WirelessUtils.MODID + ".module");
    }

    public boolean isLocked() {
        ItemStack stack = gui.getVaporizer().getModule();
        ItemBase item = (ItemBase) stack.getItem();
        return item != null && item.isLocked(stack);
    }
}
