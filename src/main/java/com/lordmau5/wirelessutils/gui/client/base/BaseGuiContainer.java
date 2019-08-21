package com.lordmau5.wirelessutils.gui.client.base;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.gui.element.tab.TabBase;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.elements.IContainsButtons;
import com.lordmau5.wirelessutils.gui.slot.SlotFilter;
import com.lordmau5.wirelessutils.item.base.ILockExplanation;
import com.lordmau5.wirelessutils.item.base.ISlotContextTooltip;
import com.lordmau5.wirelessutils.tile.base.TileEntityBase;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseGuiContainer extends GuiContainerCore implements IContainsButtons {

    private final TileEntityBase tile;
    private Slot cachedSlot = null;

    protected BaseGuiContainer(Container container) {
        super(container);
        this.tile = null;

        ySize = 176;
    }

    protected BaseGuiContainer(Container container, ResourceLocation texture) {
        super(container, texture);
        this.tile = null;

        ySize = 176;
    }

    protected BaseGuiContainer(Container container, TileEntity tile) {
        super(container);
        this.tile = (TileEntityBase) tile;

        name = this.tile.getName();
        ySize = 176;
    }

    protected BaseGuiContainer(Container container, TileEntity tile, ResourceLocation texture) {
        super(container, texture);
        this.tile = (TileEntityBase) tile;

        name = this.tile.getName();
        ySize = 176;
    }

    @Override
    public GuiContainerCore getGui() {
        return this;
    }

    public RenderItem getItemRenderer() {
        return itemRender;
    }

    @Override
    public int getCenteredOffset(String string) {
        return super.getCenteredOffset(string);
    }

    @Override
    public int getCenteredOffset(String string, int xPos) {
        return super.getCenteredOffset(string, xPos);
    }

    @Override
    protected ElementBase getElementAtPosition(int mX, int mY) {
        for (ElementBase element : elements) {
            if ( element.isVisible() && element.intersectsWith(mX, mY) )
                return element;
        }

        return null;
    }

    protected void drawGhostItem(@Nonnull ItemStack stack, int x, int y, boolean drawOverlay, boolean drawRect, @Nullable String text) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        if ( drawOverlay )
            itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, text);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();

        if ( drawRect )
            drawRect(x, y, x + 16, y + 16, 0x808b8b8b);
    }

    @Override
    protected void keyTyped(char characterTyped, int keyPressed) throws IOException {
        for (TabBase tab : tabs) {
            if ( !tab.isVisible() || !tab.isEnabled() )
                continue;

            if ( tab.onKeyTyped(characterTyped, keyPressed) )
                return;
        }

        super.keyTyped(characterTyped, keyPressed);
    }

    public void drawRightAlignedText(String text, int x, int y, int color) {
        fontRenderer.drawString(text, x - fontRenderer.getStringWidth(text), y, color);
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        if ( !mc.player.inventory.getItemStack().isEmpty() )
            return;

        Slot hoveredSlot = getSlotUnderMouse();
        if ( hoveredSlot == null || !hoveredSlot.getHasStack() )
            return;

        cachedSlot = hoveredSlot;
        renderToolTip(hoveredSlot.getStack(), mouseX, mouseY);
        cachedSlot = null;
    }

    @Override
    public List<String> getItemToolTip(ItemStack stack) {
        List<String> list = super.getItemToolTip(stack);
        if ( cachedSlot != null ) {
            Item item = stack.getItem();
            if ( item instanceof ISlotContextTooltip )
                ((ISlotContextTooltip) item).addTooltipContext(list, tile, cachedSlot, stack);

            if ( !cachedSlot.canTakeStack(mc.player) && !(cachedSlot instanceof SlotFilter) ) {
                List<String> additional = new ArrayList<>();
                additional.add(new TextComponentTranslation("info." + WirelessUtils.MODID + ".slot_lock")
                        .setStyle(TextHelpers.RED)
                        .getFormattedText());

                if ( item instanceof ILockExplanation )
                    ((ILockExplanation) item).addSlotLockExplanation(additional, tile, cachedSlot, stack);

                list.addAll(1, additional);
            }
        }

        return list;
    }
}
