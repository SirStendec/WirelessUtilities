package com.lordmau5.wirelessutils.gui.client.item;

import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.client.pages.PageItemRangeControls;
import com.lordmau5.wirelessutils.gui.client.pages.PageItemRelativeControls;
import com.lordmau5.wirelessutils.gui.container.items.ContainerRelativeAreaCard;
import com.lordmau5.wirelessutils.render.RenderManager;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockArea;

public class GuiRelativeAreaCard extends BaseGuiItem {

    private final ContainerRelativeAreaCard container;
    private int renderArea = -1;

    public GuiRelativeAreaCard(ContainerRelativeAreaCard container) {
        super(container);
        this.container = container;
        name = container.getItemStack().getDisplayName();
    }

    @Override
    public int getPageHorizontalOffset() {
        return 22 + super.getPageHorizontalOffset();
    }

    @Override
    public void initGui() {
        super.initGui();
        RenderManager.INSTANCE.disableHeldAreas();

        addPage(new PageItemRelativeControls(this, container, container));
        addPage(new PageItemRangeControls(this, container));
        setCurrentPage(0);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if ( renderArea != -1 ) {
            RenderManager.INSTANCE.removeArea(renderArea);
            renderArea = -1;
        }

        container.sendUpdateIfChanged();
        RenderManager.INSTANCE.enableHeldAreas();
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        if ( container.shouldUpdateRendering() ) {
            if ( renderArea != -1 )
                RenderManager.INSTANCE.removeArea(renderArea);

            BlockArea area = container.getItem().getHandRenderArea(container.getItemStack(), container.getPlayer(), null, NiceColors.HANDY_COLORS[0]);
            if ( area == null )
                renderArea = -1;
            else
                renderArea = RenderManager.INSTANCE.addArea(area, false);
        }
    }
}
