package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.augmentable.IBudgetInfoProvider;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class ElementWorkBudget extends ElementBase {

    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/work_budget.png");
    public static final int DEFAULT_SCALE = 42;

    protected IBudgetInfoProvider provider;

    // Always show 1 pixel if the value is non-zero.
    protected boolean alwaysShowMinimum = false;

    public ElementWorkBudget(GuiContainerCore gui, int posX, int posY, IBudgetInfoProvider provider) {
        super(gui, posX, posY);
        this.provider = provider;

        this.texture = DEFAULT_TEXTURE;
        this.sizeX = 8;
        this.sizeY = DEFAULT_SCALE;

        this.texH = 64;
        this.texW = 32;
    }

    public ElementWorkBudget setAlwaysShow(boolean show) {
        this.alwaysShowMinimum = show;
        return this;
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
        gui.bindTexture(texture);
        drawTexturedModalRect(posX, posY, 0, 0, sizeX, sizeY);

        int current = provider.getBudgetCurrent();
        int required = provider.getBudgetPerOperation();

        // First, render the current.
        int amount = getScaled(current, alwaysShowMinimum);
        drawTexturedModalRect(posX, posY + DEFAULT_SCALE - amount, 8, DEFAULT_SCALE - amount, sizeX, amount);

        // Now, render the required.
        amount = getScaled(Math.min(current, required), alwaysShowMinimum);
        drawTexturedModalRect(posX, posY + DEFAULT_SCALE - amount, required <= current ? 16 : 24, DEFAULT_SCALE - amount, sizeX, amount);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {

    }

    @Override
    public void addTooltip(List<String> list) {
        super.addTooltip(list);

        list.add(new TextComponentTranslation("info." + WirelessUtils.MODID + ".work_budget").setStyle(TextHelpers.YELLOW).getFormattedText());
        list.add(StringHelper.formatNumber(provider.getBudgetCurrent()) + " / " + StringHelper.formatNumber(provider.getBudgetMax()));
        list.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".work_budget.per_tick",
                new TextComponentString(StringHelper.formatNumber(provider.getBudgetPerTick())).setStyle(TextHelpers.GREEN)
        ).setStyle(TextHelpers.GRAY).getFormattedText());

        int per_action = provider.getBudgetPerOperation();
        if ( per_action > 0 )
            list.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".work_budget.per_op",
                    new TextComponentString(StringHelper.formatNumber(per_action)).setStyle(TextHelpers.RED)
            ).setStyle(TextHelpers.GRAY).getFormattedText());
    }

    private int getScaled(int value, boolean showMinimum) {
        if ( provider.getBudgetMax() <= 0 )
            return sizeY;

        int fraction = value * sizeY / provider.getBudgetMax();
        if ( showMinimum && value > 0 && fraction < 1 )
            return 1;

        return fraction;
    }
}
