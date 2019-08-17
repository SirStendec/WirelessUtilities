package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.api.tileentity.IEnergyInfo;
import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.init.CoreTextures;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.IEnergyHistory;
import com.lordmau5.wirelessutils.tile.base.IWorkInfoProvider;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class TabEnergyHistory extends TabBase {

    public static final String INTL_KEY = "info." + WirelessUtils.MODID + ".work_info";

    public static int defaultSide = 0;
    public static int defaultHeaderColor = 0xe1c92f;
    public static int defaultSubHeaderColor = 0xaaafb8;
    public static int defaultTextColor = 0x000000;
    public static int defaultBackgroundColorOut = 0xd0650b;
    public static int defaultBackgroundColorIn = 0x0a76d0;

    private IEnergyInfo energyInfo;
    private IEnergyHistory energyHistory;
    private IWorkInfoProvider workInfo;

    private boolean isProducer;
    private boolean displayGraph = true;
    private boolean displayMax = true;
    private boolean displayStored = true;
    private boolean displayTargets = false;

    private long[] history;
    private double max;
    private byte tick;
    private int hoveredLine = -1;

    static final String UNIT_INSTANT = " RF/t";
    static final String UNIT_STORAGE = " RF";

    public TabEnergyHistory(GuiContainerCore gui, TileEntity tile, boolean isProducer) {
        this(gui, defaultSide, tile, isProducer);
    }

    public TabEnergyHistory(GuiContainerCore gui, int side, TileEntity tile, boolean isProducer) {
        super(gui, side);

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = isProducer ? defaultBackgroundColorOut : defaultBackgroundColorIn;

        maxWidth = 100;
        recalculateHeight();

        energyInfo = (tile instanceof IEnergyInfo) ? (IEnergyInfo) tile : null;
        energyHistory = (tile instanceof IEnergyHistory) ? (IEnergyHistory) tile : null;
        workInfo = (tile instanceof IWorkInfoProvider) ? (IWorkInfoProvider) tile : null;

        this.isProducer = isProducer;

        if ( energyHistory != null )
            energyHistory.syncHistory();
    }

    protected void recalculateHeight() {
        maxHeight = 44;
        if ( displayStored )
            maxHeight += 24;
        if ( displayGraph )
            maxHeight += 24;
        if ( displayMax )
            maxHeight += 24;
        if ( displayTargets )
            maxHeight += 24;
    }

    public TabEnergyHistory isProducer(boolean isProducer) {
        this.isProducer = isProducer;
        return this;
    }

    public TabEnergyHistory displayGraph(boolean displayGraph) {
        this.displayGraph = displayGraph;
        recalculateHeight();
        return this;
    }

    public TabEnergyHistory displayMax(boolean displayMax) {
        this.displayMax = displayMax;
        recalculateHeight();
        return this;
    }

    public TabEnergyHistory displayStored(boolean displayStored) {
        this.displayStored = displayStored;
        recalculateHeight();
        return this;
    }

    public TabEnergyHistory displayTargets(boolean displayTargets) {
        this.displayTargets = displayTargets;
        recalculateHeight();
        return this;
    }

    @Override
    public void update() {
        super.update();

        if ( !displayGraph || !isFullyOpened() ) {
            hoveredLine = -1;
            return;
        }

        int mouseX = gui.getMouseX() - (posXOffset() + 8);
        int mouseY = gui.getMouseY() - (posY + 18);

        if ( mouseX < 0 || mouseX > 80 || mouseY < 0 || mouseY > 20 ) {
            hoveredLine = -1;
            return;
        }

        hoveredLine = Math.floorDiv(mouseX, 2);
    }

    @Override
    protected void drawForeground() {
        drawTabIcon(CoreTextures.ICON_ENERGY);
        if ( !isFullyOpened() )
            return;

        FontRenderer fontRenderer = getFontRenderer();
        fontRenderer.drawStringWithShadow(StringHelper.localize("info.cofh.energy"), sideOffset() + 20, 6, headerColor);

        int y = 18;

        boolean atTime = false;
        int current = energyInfo.getInfoEnergyPerTick();

        if ( displayGraph ) {
            if ( hoveredLine == -1 ) {
                history = energyHistory.getEnergyHistory();
                max = energyInfo.getInfoMaxEnergyPerTick();
                tick = (byte) (40 - energyHistory.getHistoryTick());
            }

            gui.drawSizedModalRect(sideOffset() + 6, y, sideOffset() + 88, y + 21, 0x20000000);
            gui.drawSizedModalRect(sideOffset() + 6, y + 10, sideOffset() + 88, y + 11, 0x40000000);

            int t = tick;
            for (int i = 0; i < 4; i++) {
                gui.drawSizedModalRect(
                        sideOffset() + 5 + (t * 2), y,
                        sideOffset() + 6 + (t * 2), y + 20,
                        0x40000000
                );

                t += 10;
                if ( t > 40 )
                    t -= 40;
                if ( t == tick )
                    break;
            }

            int x = 2;
            for (int i = 0; i < history.length; i++, x += 2) {
                int height = (int) Math.floor(20 * history[i] / (double) max);
                if ( height == 0 && history[i] > 0 )
                    height = 1;
                else if ( height > 20 )
                    height = 20;

                if ( hoveredLine == i ) {
                    atTime = true;
                    current = (int) history[i];

                    gui.drawSizedModalRect(
                            sideOffset() + 6 + x, y,
                            sideOffset() + 7 + x, y + 20,
                            0xFF000000
                    );

                    gui.drawSizedModalRect(
                            sideOffset() + 5 + x, y + (19 - height),
                            sideOffset() + 8 + x, y + 21,
                            0xFF000000
                    );
                }

                gui.drawSizedModalRect(
                        sideOffset() + 6 + x, y + (20 - height),
                        sideOffset() + 7 + x, y + 20,
                        hoveredLine == i ? 0xFFe1c92f : 0xFFFFFFFF
                );
            }

            gui.drawSizedModalRect(sideOffset() + 6, y, sideOffset() + 7, y + 21, 0xFF000000);
            gui.drawSizedModalRect(sideOffset() + 6, y + 20, sideOffset() + 88, y + 21, 0xFF000000);

            y += 24;
        }

        final String flowDirection = atTime ? "info." + WirelessUtils.MODID + ".energy_at_tick" : (isProducer ? "info.cofh.energyProduce" : "info.cofh.energyConsume");

        fontRenderer.drawStringWithShadow(StringHelper.localize(flowDirection) + ":",
                sideOffset() + 6, y, subheaderColor);
        y += 12;
        fontRenderer.drawString(StringHelper.formatNumber(current) + UNIT_INSTANT,
                sideOffset() + 14, y, textColor);
        y += 12;

        if ( displayMax ) {
            fontRenderer.drawStringWithShadow(StringHelper.localize("info.cofh.energyMax") + ":",
                    sideOffset() + 6, y, subheaderColor);
            y += 12;
            fontRenderer.drawString(StringHelper.formatNumber(energyInfo.getInfoMaxEnergyPerTick()) + UNIT_INSTANT,
                    sideOffset() + 14, y, textColor);
            y += 12;
        }

        if ( displayStored ) {
            fontRenderer.drawStringWithShadow(StringHelper.localize("info.cofh.energyStored") + ":",
                    sideOffset() + 6, y, subheaderColor);
            y += 12;
            fontRenderer.drawString(StringHelper.formatNumber(energyInfo.getInfoEnergyStored()) + UNIT_STORAGE,
                    sideOffset() + 14, y, textColor);
            y += 12;
        }

        if ( displayTargets && workInfo != null ) {
            fontRenderer.drawStringWithShadow(StringHelper.localize(INTL_KEY + ".targets.name"),
                    sideOffset() + 6, y, subheaderColor);
            y += 12;
            fontRenderer.drawString(StringHelper.localizeFormat(INTL_KEY + ".targets.info",
                    StringHelper.formatNumber(workInfo.getActiveTargetCount()),
                    StringHelper.formatNumber(workInfo.getValidTargetCount())
            ), sideOffset() + 14, y, textColor);
            y += 12;
        }

        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void addTooltip(List<String> list) {
        if ( !isFullyOpened() ) {
            list.add(new TextComponentTranslation(
                    INTL_KEY + ".tooltip.rate",
                    TextHelpers.getComponent(StringHelper.formatNumber(energyInfo.getInfoEnergyPerTick()) + UNIT_INSTANT).setStyle(TextHelpers.YELLOW)
            ).getFormattedText());

            if ( displayTargets && workInfo != null )
                list.add(new TextComponentTranslation(
                        INTL_KEY + ".tooltip.targets",
                        TextHelpers.getComponent(workInfo.getActiveTargetCount()).setStyle(TextHelpers.YELLOW),
                        TextHelpers.getComponent(workInfo.getValidTargetCount()).setStyle(TextHelpers.YELLOW)
                ).getFormattedText());
        }
    }
}
