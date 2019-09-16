package com.lordmau5.wirelessutils.gui.client.vaporizer;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.TabAugmentTwoElectricBoogaloo;
import com.lordmau5.wirelessutils.gui.client.elements.TabEnergyHistory;
import com.lordmau5.wirelessutils.gui.client.elements.TabSideControl;
import com.lordmau5.wirelessutils.gui.client.elements.TabSpacer;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorkInfo;
import com.lordmau5.wirelessutils.gui.client.elements.TabWorldTickRate;
import com.lordmau5.wirelessutils.gui.client.modules.base.ElementModuleBase;
import com.lordmau5.wirelessutils.gui.container.vaporizer.ContainerBaseVaporizer;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GuiBaseVaporizer extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/vaporizer.png");

    protected final TileBaseVaporizer vaporizer;
    protected final ContainerBaseVaporizer container;

    protected TabSideControl sideControl;

    private TileBaseVaporizer.IVaporizerBehavior behavior = null;
    protected ElementModuleBase module = null;

    public GuiBaseVaporizer(ContainerBaseVaporizer container, TileBaseVaporizer vaporizer) {
        super(container, vaporizer, TEXTURE);
        this.container = container;
        this.vaporizer = vaporizer;

        setMainPageTabLabel("btn." + WirelessUtils.MODID + ".main");

        drawTitle = false;
        ySize = 242;
    }

    @Override
    public void initGui() {
        super.initGui();

        addTab(new TabSpacer(this, TabBase.LEFT, 20));
        addTab(new TabEnergyHistory(this, vaporizer, false));
        addTab(new TabWorkInfo(this, vaporizer).setItem(new ItemStack(ModItems.itemVoidPearl)));
        addTab(new TabWorldTickRate(this, vaporizer));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabSpacer(this, TabBase.RIGHT, 20));
        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, vaporizer));
        sideControl = (TabSideControl) addTab(new TabSideControl(this, vaporizer));
    }

    @Override
    public int getPageVerticalOffset() {
        return 20;
    }

    @Override
    public boolean isPageTabVisible() {
        return true;
    }

    @Override
    public int getPageTabHeight() {
        if ( isMainPage() )
            return 20;

        return super.getPageTabHeight();
    }

    @Override
    public int getPageTabWidth() {
        return 50;
    }

    @Override
    public int drawPageTabForeground(int x, int y) {
        if ( !isMainPage() )
            return super.drawPageTabForeground(x, y);

        return getPageTabWidth();
    }

    public TileBaseVaporizer getVaporizer() {
        return vaporizer;
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        sideControl.setVisible(vaporizer.isSidedTransferAugmented());

        TileBaseVaporizer.IVaporizerBehavior behavior = vaporizer.getBehavior();
        if ( this.behavior != behavior ) {
            if ( module != null )
                removePage(module);

            module = behavior != null ? behavior.getGUI(this) : null;
            if ( module != null )
                addPage(module);

            this.behavior = behavior;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        if ( isMainPage() ) {
            String title = StringHelper.localize(name);
            fontRenderer.drawString(title, getCenteredOffset(title), 28, 0x404040);
            fontRenderer.drawString(StringHelper.localize("info." + WirelessUtils.MODID + ".buffer.in_out"), 8, 100, 0x404040);

        }
    }

    @Override
    protected void drawBackgroundOverSlots() {
        if ( xSize > 256 || ySize > 256 )
            drawSizedTexturedModalRect(guiLeft + 7, guiTop + ySize - (76 + 7), 8, 28, 162, 76, 512, 512);
        else
            drawTexturedModalRect(guiLeft + 7, guiTop + ySize - (76 + 7), 8, 28, 162, 76);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTick, x, y);

        if ( isMainPage() ) {
            drawModuleSlots();
            drawBuffers();

            if ( vaporizer.getModule().isEmpty() )
                drawGhostItem(new ItemStack(ModItems.itemBaseModule), guiLeft + 12, guiTop + 8, true, true, null);

            if ( vaporizer.getModifier().isEmpty() )
                drawGhostItem(vaporizer.getModifierGhost(), guiLeft + 30, guiTop + 8, true, true, null);

            ContainerBaseVaporizer container = (ContainerBaseVaporizer) inventorySlots;

            drawSlotLocks(vaporizer.getInputOffset(), container.inputOffset, guiLeft + 8, guiTop + 111, 2, 4, true);
            drawSlotLocks(vaporizer.getOutputOffset(), container.outputOffset, guiLeft + 98, guiTop + 111, 2, 4, false);
            drawSlotLocks(vaporizer.getModuleOffset(), container.moduleOffset, guiLeft + 12, guiTop + 8, 1, 2, false);
        }
    }

    protected void drawModuleSlots() {
        bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawSizedTexturedModalRect(guiLeft + 11, guiTop + 7, 176, 43, 36, 18, 256, 256);
    }

    protected void drawBuffers() {
        bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawSizedTexturedModalRect(guiLeft + 7, guiTop + 110, 176, 43, 72, 36, 256, 256);
        drawSizedTexturedModalRect(guiLeft + 97, guiTop + 110, 176, 43, 72, 36, 256, 256);
    }

    @Override
    public List<String> getItemToolTip(ItemStack stack) {
        List<String> out = super.getItemToolTip(stack);

        TileBaseVaporizer.IVaporizerBehavior behavior = vaporizer.getBehavior();
        if ( behavior == null )
            return out;

        behavior.getItemToolTip(out, getSlotUnderMouse(), stack);
        return out;
    }

    protected void drawSlotLocks(int slotIndex, int slotOffset, int xPos, int yPos, int rows, int cols, boolean inputGhosts) {
        ItemStack held = mc.player.inventory.getItemStack();
        int idx = 0;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++, slotIndex++, slotOffset++, idx++) {
                int xp = xPos + (x * 18);
                int yp = yPos + (y * 18);
                Slot slot = inventorySlots.getSlot(slotOffset);
                if ( !vaporizer.isSlotUnlocked(slotIndex) || (!held.isEmpty() && !slot.isItemValid(held)) )
                    drawRect(xp, yp, xp + 16, yp + 16, 0x99444444);
                else if ( inputGhosts && !slot.getHasStack() ) {
                    ItemStack ghost = behavior.getInputGhost(idx);
                    if ( !ghost.isEmpty() )
                        drawGhostItem(ghost, xp, yp, true, true, null);
                }
            }
        }
    }
}
