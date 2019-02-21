package com.lordmau5.wirelessutils.gui.client;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

public class GuiPlayerCard extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    public GuiPlayerCard(Container container) {
        super(container, TEXTURE);
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    public ItemStack getStack() {
        return mc.player.getHeldItem(EnumHand.MAIN_HAND);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        ItemStack stack = getStack();
        NBTTagCompound tag = stack.isEmpty() ? null : stack.getTagCompound();
        if ( tag == null )
            return;

        String player = tag.getString("Player");

        fontRenderer.drawString(player, 94, 40, 0);

    }
}
