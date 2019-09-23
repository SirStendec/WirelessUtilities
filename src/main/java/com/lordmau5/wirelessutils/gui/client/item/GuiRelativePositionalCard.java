package com.lordmau5.wirelessutils.gui.client.item;

import cofh.core.gui.element.ElementTextField;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.pages.PageItemRelativeControls;
import com.lordmau5.wirelessutils.gui.container.items.ContainerRelativePositionalCard;
import com.lordmau5.wirelessutils.render.RenderManager;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockArea;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModItems;

public class GuiRelativePositionalCard extends BaseGuiItem {

    private final ContainerRelativePositionalCard container;

    private ElementTextField txtX;
    private ElementTextField txtY;
    private ElementTextField txtZ;

    private BlockPosDimension target;
    private int renderArea = -1;

    private ElementDynamicContainedButton btnClear;
    private ElementDynamicContainedButton btnFacing;

    public GuiRelativePositionalCard(ContainerRelativePositionalCard container) {
        super(container);
        this.container = container;
        name = container.getItemStack().getDisplayName();
    }

    @Override
    public void initGui() {
        super.initGui();
        RenderManager.INSTANCE.disableHeldAreas();

        addPage(new PageItemRelativeControls(this, container, container).setPageTabVisible(false));
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

            BlockArea area = ModItems.itemRelativePositionalCard.getHandRenderArea(container.getItemStack(), container.getPlayer(), null, NiceColors.HANDY_COLORS[0]);
            if ( area == null )
                renderArea = -1;
            else
                renderArea = RenderManager.INSTANCE.addArea(area, false);
        }
    }
}
