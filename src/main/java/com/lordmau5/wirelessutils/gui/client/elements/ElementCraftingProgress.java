package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.plugins.JEI.JEIPlugin;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.crafting.IWUCraftingMachine;
import com.lordmau5.wirelessutils.utils.crafting.IWURecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.IOException;
import java.util.List;

public class ElementCraftingProgress extends ElementBase {

    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/work_budget.png");
    public static final int DEFAULT_SCALE = 42;

    protected final IWUCraftingMachine machine;

    // Always show 1 pixel if the value is non-zero.
    protected boolean alwaysShowMinimum = false;

    private boolean shouldRender = false;

    public ElementCraftingProgress(GuiContainerCore gui, int posX, int posY, IWUCraftingMachine machine) {
        super(gui, posX, posY);
        this.machine = machine;

        texture = DEFAULT_TEXTURE;
        sizeX = 8;
        sizeY = DEFAULT_SCALE;

        texH = 64;
        texW = 32;
    }

    @Override
    public void update() {
        super.update();
        shouldRender = machine.canCraft();
    }

    public ElementCraftingProgress setAlwaysShow(boolean show) {
        alwaysShowMinimum = show;
        return this;
    }

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {
        String category = machine.getRecipeCategory();
        if ( category != null && JEIPlugin.showRecipeCategory(category) ) {
            BaseGuiContainer.playClickSound(1F);
            return true;
        }

        return super.onMousePressed(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
        if ( !shouldRender )
            return;

        gui.bindTexture(texture);
        drawTexturedModalRect(posX, posY, 0, 0, sizeX, sizeY);

        float progress = machine.getCraftingProgress();
        if ( progress == 0 )
            return;

        int amount = (int) Math.floor(progress * sizeY);
        if ( amount < 1 )
            amount = 1;
        else if ( amount > sizeY )
            amount = sizeY;

        // Render the progress.
        drawTexturedModalRect(posX, posY + DEFAULT_SCALE - amount, 8, DEFAULT_SCALE - amount, sizeX, amount);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {

    }

    @Override
    public void addTooltip(List<String> list) {
        if ( !shouldRender )
            return;

        super.addTooltip(list);

        list.add(new TextComponentTranslation("info." + WirelessUtils.MODID + ".crafting").setStyle(TextHelpers.YELLOW).getFormattedText());
        IWURecipe recipe = machine.getCurrentRecipe();
        if ( recipe == null ) {
            list.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".crafting.none"
            ).setStyle(TextHelpers.GRAY).getFormattedText());

        } else
            recipe.addTooltip(list, machine);

        if ( JEIPlugin.hasJEI() && machine.getRecipeCategory() != null ) {
            list.add("");
            list.add(new TextComponentTranslation("info." + WirelessUtils.MODID + ".crafting.recipes").setStyle(TextHelpers.GRAY).getFormattedText());
        }
    }
}
