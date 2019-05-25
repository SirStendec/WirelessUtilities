package com.lordmau5.wirelessutils.gui.client.vaporizer;

import cofh.core.gui.element.ElementBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.container.vaporizer.ContainerBaseVaporizer;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;

public class GuiBaseVaporizer extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/vaporizer.png");

    protected final TileBaseVaporizer vaporizer;
    protected final ContainerBaseVaporizer container;

    private TileBaseVaporizer.IVaporizerBehavior behavior = null;
    protected ElementModuleBase module = null;
    protected boolean moduleTab = false;

    protected ArrayList<ElementBase> moduleElements = new ArrayList<>();

    public GuiBaseVaporizer(ContainerBaseVaporizer container, TileBaseVaporizer vaporizer) {
        super(container, vaporizer, TEXTURE);
        this.container = container;
        this.vaporizer = vaporizer;

        drawTitle = false;
        ySize = 242;
    }

    public TileBaseVaporizer getVaporizer() {
        return vaporizer;
    }

    @Override
    public void drawScreen(int x, int y, float partialTick) {
        if ( moduleTab && module != null ) {
            ArrayList<ElementBase> elements = this.elements;
            this.elements = moduleElements;
            super.drawScreen(x, y, partialTick);
            this.elements = elements;
        } else
            super.drawScreen(x, y, partialTick);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        TileBaseVaporizer.IVaporizerBehavior behavior = vaporizer.getBehavior();
        if ( this.behavior != behavior ) {
            module = behavior != null ? behavior.getGUI(this) : null;

            moduleElements.clear();
            if ( module != null )
                moduleElements.add(module);

            setModuleTab(moduleTab);
            this.behavior = behavior;
        }
    }

    public void setModuleTab(boolean enabled) {
        if ( module == null && enabled )
            enabled = false;

        if ( enabled == moduleTab )
            return;

        moduleTab = enabled;
        BaseGuiContainer.playClickSound(1F);
        container.setSlotsVisible(!moduleTab);
    }

    @Override
    protected void drawElements(float partialTick, boolean foreground) {
        if ( moduleTab && module != null ) {
            if ( foreground )
                module.drawForeground(mouseX, mouseY);
            else
                module.drawBackground(mouseX, mouseY, partialTick);

        } else
            super.drawElements(partialTick, foreground);
    }

    @Override
    protected void keyTyped(char characterTyped, int keyPressed) throws IOException {
        if ( moduleTab && module != null ) {
            ArrayList<ElementBase> elements = this.elements;
            this.elements = moduleElements;
            super.keyTyped(characterTyped, keyPressed);
            this.elements = elements;

        } else
            super.keyTyped(characterTyped, keyPressed);
    }

    @Override
    public void handleMouseInput() throws IOException {
        if ( moduleTab && module != null ) {
            ArrayList<ElementBase> elements = this.elements;
            this.elements = moduleElements;
            super.handleMouseInput();
            this.elements = elements;

        } else
            super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mX, int mY, int mouseButton) throws IOException {
        int x = mX - guiLeft;
        int y = mY - guiTop;

        if ( moduleTab && module != null ) {
            if ( x >= 2 && x < 52 && y >= 0 && y < 20 ) {
                setModuleTab(false);

            } else {
                ArrayList<ElementBase> elements = this.elements;
                this.elements = moduleElements;
                super.mouseClicked(mX, mY, mouseButton);
                this.elements = elements;
            }
        } else {
            if ( module != null && x >= 52 && x < 102 && y >= 0 && y < 20 ) {
                setModuleTab(true);

            } else
                super.mouseClicked(mX, mY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mX, int mY, int mouseButton) {
        if ( moduleTab && module != null ) {
            ArrayList<ElementBase> elements = this.elements;
            this.elements = moduleElements;
            super.mouseReleased(mX, mY, mouseButton);
            this.elements = elements;
        } else
            super.mouseReleased(mX, mY, mouseButton);
    }

    @Override
    protected ElementBase getElementAtPosition(int mX, int mY) {
        if ( moduleTab && module != null )
            return module.intersectsWith(mX, mY) ? module : null;

        return super.getElementAtPosition(mX, mY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        if ( !moduleTab ) {
            String title = StringHelper.localize(name);
            fontRenderer.drawString(title, getCenteredOffset(title), 28, 0x404040);

            fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".buffer.in_out"), 8, 100, 0x404040);

        } else {
            String title = StringHelper.localize("btn." + WirelessUtils.MODID + ".main");
            fontRenderer.drawString(title, getCenteredOffset(title, 27), 8, 0x404040);
        }

        if ( module != null ) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(52, 0, 0);
            module.drawTab(moduleTab);
            GlStateManager.popMatrix();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTick, x, y);

        if ( !moduleTab ) {
            drawTopTab(2, true);
            drawModuleSlots();
            drawBuffers();

            if ( module != null )
                drawTopTab(52, false);

            if ( vaporizer.getModule().isEmpty() )
                drawGhostItem(new ItemStack(ModItems.itemBaseModule), guiLeft + 10, guiTop + 8, true, true, null);

            if ( vaporizer.getModifier().isEmpty() )
                drawGhostItem(vaporizer.getModifierGhost(), guiLeft + 28, guiTop + 8, true, true, null);

            ContainerBaseVaporizer container = (ContainerBaseVaporizer) inventorySlots;

            drawSlotLocks(vaporizer.getInputOffset(), container.inputOffset, guiLeft + 8, guiTop + 111, 2, 4);
            drawSlotLocks(vaporizer.getOutputOffset(), container.outputOffset, guiLeft + 98, guiTop + 111, 2, 4);
            drawSlotLocks(vaporizer.getModuleOffset(), container.moduleOffset, guiLeft + 10, guiTop + 8, 1, 2);

        } else {
            drawTopTab(2, false);
            drawTopTab(52, true);
        }
    }

    protected void drawTopTab(int xPos, boolean focused) {
        bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawSizedTexturedModalRect(guiLeft + xPos, guiTop, 176, focused ? 20 : 0, 50, focused ? 23 : 20, 256, 256);
    }

    protected void drawModuleSlots() {
        bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawSizedTexturedModalRect(guiLeft + 9, guiTop + 7, 176, 43, 36, 18, 256, 256);
    }

    protected void drawBuffers() {
        bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawSizedTexturedModalRect(guiLeft + 7, guiTop + 110, 176, 43, 72, 36, 256, 256);
        drawSizedTexturedModalRect(guiLeft + 97, guiTop + 110, 176, 43, 72, 36, 256, 256);
    }

    protected void drawSlotLocks(int slotIndex, int slotOffset, int xPos, int yPos, int rows, int cols) {
        ItemStack held = mc.player.inventory.getItemStack();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++, slotIndex++, slotOffset++) {
                Slot slot = inventorySlots.getSlot(slotOffset);
                if ( !vaporizer.isSlotUnlocked(slotIndex) || (!held.isEmpty() && !slot.isItemValid(held)) ) {
                    int xp = xPos + (x * 18);
                    int yp = yPos + (y * 18);

                    drawRect(xp, yp, xp + 16, yp + 16, 0x99444444);
                }
            }
        }
    }
}
