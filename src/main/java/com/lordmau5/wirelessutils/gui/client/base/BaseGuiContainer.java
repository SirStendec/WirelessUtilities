package com.lordmau5.wirelessutils.gui.client.base;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.elements.IContainsButtons;
import com.lordmau5.wirelessutils.gui.client.pages.base.IPageTabProvider;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.gui.container.IVisibleSlotContainer;
import com.lordmau5.wirelessutils.gui.slot.SlotFilter;
import com.lordmau5.wirelessutils.item.base.ILockExplanation;
import com.lordmau5.wirelessutils.item.base.ISlotContextTooltip;
import com.lordmau5.wirelessutils.tile.base.TileEntityBase;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseGuiContainer extends GuiContainerCore implements IPageTabProvider, IContainsButtons {

    private static final ArrayList<ElementBase> EMPTY_ELEMENTS = new ArrayList<>();

    private final TileEntityBase tile;

    private boolean drawMainPage = false;
    private int currentPage = -1;
    private List<PageBase> pages = null;

    private String mainLabel = null;
    private TextureAtlasSprite mainIcon = null;
    private ItemStack mainItem = ItemStack.EMPTY;
    private boolean mainPageNeedsSize = true;
    private int mainPageWidth = 0;
    private int mainPageHeight = 0;

    protected int backgroundColor = 0xFFFFFF;
    protected int textColor = 0x404040;

    private final IVisibleSlotContainer visibleSlotContainer;
    private boolean slotsVisible = true;

    protected BaseGuiContainer(Container container) {
        super(container);
        if ( container instanceof IVisibleSlotContainer )
            visibleSlotContainer = (IVisibleSlotContainer) container;
        else
            visibleSlotContainer = null;

        this.tile = null;

        ySize = 176;
    }

    protected BaseGuiContainer(Container container, ResourceLocation texture) {
        super(container, texture);
        if ( container instanceof IVisibleSlotContainer )
            visibleSlotContainer = (IVisibleSlotContainer) container;
        else
            visibleSlotContainer = null;

        this.tile = null;

        ySize = 176;
    }

    protected BaseGuiContainer(Container container, TileEntity tile) {
        super(container);
        if ( container instanceof IVisibleSlotContainer )
            visibleSlotContainer = (IVisibleSlotContainer) container;
        else
            visibleSlotContainer = null;

        this.tile = (TileEntityBase) tile;

        name = this.tile.getName();
        ySize = 176;
    }

    protected BaseGuiContainer(Container container, TileEntity tile, ResourceLocation texture) {
        super(container, texture);
        if ( container instanceof IVisibleSlotContainer )
            visibleSlotContainer = (IVisibleSlotContainer) container;
        else
            visibleSlotContainer = null;

        this.tile = (TileEntityBase) tile;

        name = this.tile.getName();
        ySize = 176;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if ( pages != null )
            for (PageBase page : pages)
                page.onGuiClosed();
    }

    /* Pages */

    @Nullable
    public PageBase getPageAtIndex(int index) {
        if ( pages == null )
            return null;

        return pages.get(index);
    }

    @Nullable
    public PageBase getCurrentPage() {
        if ( currentPage == -1 || pages == null )
            return null;

        return pages.get(currentPage);
    }

    public boolean setCurrentPage(IPageTabProvider page) {
        if ( page == this )
            return setCurrentPage(-1);

        if ( pages != null && page != null ) {
            int length = pages.size();
            for (int i = 0; i < length; i++) {
                if ( page == pages.get(i) )
                    return setCurrentPage(i);
            }
        }

        return setCurrentPage(-1);
    }

    public boolean setCurrentPage(int index) {
        if ( pages == null )
            index = -1;
        else if ( index >= pages.size() )
            index = -1;
        else if ( index < -1 )
            index = pages.size() - 1;

        if ( index == currentPage )
            return false;

        if ( currentPage == -1 )
            onPageFocusLost();
        else {
            PageBase current = getCurrentPage();
            if ( current != null )
                current.onPageFocusLost();
        }

        currentPage = index;
        boolean wantsSlots;

        PageBase current = getCurrentPage();
        if ( current == null ) {
            onPageFocused();
            wantsSlots = wantsSlots();
        } else {
            current.onPageFocused();
            wantsSlots = current.wantsSlots();
        }

        setSlotsVisible(wantsSlots);

        return true;
    }

    public PageBase addPage(PageBase page) {
        if ( pages == null )
            pages = new ArrayList<>();

        pages.add(page);
        return page;
    }

    public PageBase removePage(int index) {
        if ( pages != null ) {
            PageBase page = pages.get(index);
            pages.remove(index);
            return page;
        }

        return null;
    }

    public PageBase removePage(PageBase page) {
        if ( pages != null && page != null ) {
            for (int i = 0; i < pages.size(); i++) {
                if ( pages.get(i) == page ) {
                    if ( i == currentPage )
                        setCurrentPage(-1);

                    pages.remove(i);
                    break;
                }
            }
        }

        return page;
    }

    public BaseGuiContainer clearPages() {
        if ( pages != null ) {
            setCurrentPage(-1);
            pages.clear();
        }

        return this;
    }

    public int getPageVerticalOffset() {
        return 0;
    }

    public int getPageHorizontalOffset() {
        return 4;
    }

    public boolean isMainPage() {
        return getCurrentPage() == null;
    }

    public void focusPage() {
        setCurrentPage(-1);
    }

    @Nullable
    public Rectangle getPageTabArea() {
        final boolean drawMain = isPageTabVisible();
        if ( !drawMain && pages == null )
            return null;

        int left = guiLeft + getPageHorizontalOffset();
        int bottom = guiTop + getPageVerticalOffset();

        int height = 0;
        int width = 0;

        if ( drawMain ) {
            height = getPageTabHeight();
            width = getPageTabWidth();
        }

        if ( pages != null ) {
            for (PageBase page : pages) {
                if ( !page.isEnabled() || !page.isVisible() || !page.isPageTabVisible() )
                    continue;

                width += page.getPageTabWidth();
                height = Math.max(page.getPageTabHeight(), height);
            }
        }

        if ( width == 0 && height == 0 )
            return null;

        return new Rectangle(left, bottom - height, width, height);
    }

    /* Rendering Main Tab */

    public BaseGuiContainer setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public BaseGuiContainer setTextColor(int color) {
        this.textColor = color;
        return this;
    }

    public BaseGuiContainer setMainPageTabLabel(String label) {
        this.mainLabel = label;
        mainPageNeedsSize = true;
        return this;
    }

    public BaseGuiContainer setMainPageTabIcon(TextureAtlasSprite icon) {
        this.mainIcon = icon;
        mainPageNeedsSize = true;
        return this;
    }

    public BaseGuiContainer setMainPageTabItem(ItemStack item) {
        if ( item == null )
            item = ItemStack.EMPTY;

        this.mainItem = item;
        mainPageNeedsSize = true;
        return this;
    }

    public void updateMainPageTabSize() {
        mainPageNeedsSize = false;
        mainPageWidth = 0;
        mainPageHeight = 0;

        if ( mainLabel != null && !mainLabel.isEmpty() ) {
            if ( fontRenderer == null )
                mainPageNeedsSize = true;
            else {
                String lb = StringHelper.localize(mainLabel);
                mainPageWidth = fontRenderer.getStringWidth(lb);
                mainPageHeight = 8;
            }
        }

        if ( mainIcon != null ) {
            mainPageHeight = Math.max(mainIcon.getIconHeight(), mainPageHeight);
            if ( mainPageWidth != 0 )
                mainPageWidth += 2;
            mainPageWidth += mainIcon.getIconWidth();
        }

        if ( !mainItem.isEmpty() ) {
            mainPageHeight = Math.max(12, mainPageHeight);
            if ( mainPageWidth != 0 )
                mainPageWidth += 2;
            mainPageWidth += 16;
        }

        if ( mainPageHeight != 0 && mainPageWidth != 0 ) {
            mainPageWidth += 8;
            mainPageHeight += 8;
            drawMainPage = true;
        } else {
            drawMainPage = false;
        }
    }

    public boolean isPageTabVisible() {
        return drawMainPage;
    }

    public int getPageTabWidth() {
        return mainPageWidth;
    }

    public int getPageTabHeight() {
        return mainPageHeight;
    }

    public int drawPageTabBackground(int x, int y) {
        int width = getPageTabWidth();
        float colorR = (backgroundColor >> 16 & 255) / 255.0F;
        float colorG = (backgroundColor >> 8 & 255) / 255.0F;
        float colorB = (backgroundColor & 255) / 255.0F;

        GlStateManager.color(colorR, colorG, colorB, 1.0F);
        IPageTabProvider.drawBackground(this, isMainPage(), x, y, width, getPageTabHeight());
        GlStateManager.color(1, 1, 1, 1);
        return width;
    }

    public int drawPageTabForeground(int x, int y) {
        int width = getPageTabWidth();
        String label = mainLabel == null ? null : StringHelper.localize(mainLabel);
        IPageTabProvider.drawForeground(this, isMainPage(), x, y, width, getPageTabHeight(), label, mainIcon, mainItem, textColor);
        return getPageTabWidth();
    }

    /* Slot Visibility */

    public boolean wantsSlots() {
        return true;
    }

    public boolean setSlotsVisible(boolean visible) {
        if ( visible )
            return showSlots();
        return hideSlots();
    }

    public boolean hideSlots() {
        if ( visibleSlotContainer == null )
            return false;

        visibleSlotContainer.hideSlots();
        slotsVisible = false;
        return true;
    }

    public boolean showSlots() {
        if ( visibleSlotContainer == null )
            return false;

        visibleSlotContainer.showSlots();
        slotsVisible = true;
        return true;
    }


    /* Helpers */

    @Override
    public GuiContainerCore getGui() {
        return this;
    }

    public List<ElementBase> getElements() {
        PageBase page = getCurrentPage();
        if ( page != null )
            return page.getElements();

        return elements;
    }

    public RenderItem getItemRenderer() {
        return itemRender;
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

    public void drawRightAlignedText(String text, int x, int y, int color) {
        fontRenderer.drawString(text, x - fontRenderer.getStringWidth(text), y, color);
    }

    /* Overriden Methods */

    @Override
    public void initGui() {
        super.initGui();
        updateMainPageTabSize();

        setSlotsVisible(wantsSlots());
        currentPage = -1;
        if ( pages != null )
            pages.clear();
    }

    @Override
    public void drawScreen(int x, int y, float partialTick) {
        PageBase page = getCurrentPage();
        if ( page != null ) {
            ArrayList<ElementBase> elements = this.elements;
            this.elements = page.getElements();
            super.drawScreen(x, y, partialTick);
            this.elements = elements;

        } else
            super.drawScreen(x, y, partialTick);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        PageBase page = getCurrentPage();
        if ( page != null )
            page.updateElementInformation();
    }

    @Override
    protected void drawElements(float partialTick, boolean foreground) {
        PageBase page = getCurrentPage();
        if ( page != null )
            page.drawElements(partialTick, foreground);
        else
            super.drawElements(partialTick, foreground);
    }

    @Override
    public void addTooltips(List<String> tooltip) {
        IPageTabProvider pageTab = getPageTabAtPosition(mouseX, mouseY);
        if ( pageTab != null )
            pageTab.addPageTabTooltip(tooltip);

        super.addTooltips(tooltip);
    }

    @Override
    protected void keyTyped(char characterTyped, int keyPressed) throws IOException {
        for (TabBase tab : tabs) {
            if ( !tab.isVisible() || !tab.isEnabled() )
                continue;

            if ( tab.onKeyTyped(characterTyped, keyPressed) )
                return;
        }

        PageBase page = getCurrentPage();
        if ( page != null ) {
            if ( page.onKeyTyped(characterTyped, keyPressed) )
                return;
            ArrayList<ElementBase> elements = this.elements;
            this.elements = EMPTY_ELEMENTS;
            super.keyTyped(characterTyped, keyPressed);
            this.elements = elements;
            EMPTY_ELEMENTS.clear();
        } else
            super.keyTyped(characterTyped, keyPressed);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        mouseX = x - guiLeft;
        mouseY = y - guiTop;

        PageBase page = getCurrentPage();
        int wheelMovement = Mouse.getEventDWheel();

        if ( wheelMovement != 0 ) {
            if ( page != null ) {
                if ( page.onMouseWheel(mouseX, mouseY, wheelMovement) )
                    return;
            } else {
                for (ElementBase element : elements) {
                    if ( !element.isVisible() || !element.isEnabled() || !element.intersectsWith(mouseX, mouseY) )
                        continue;

                    if ( element.onMouseWheel(mouseX, mouseY, wheelMovement) )
                        return;
                }
            }

            IPageTabProvider pageTab = getPageTabAtPosition(mouseX, mouseY);
            if ( pageTab != null && pageTab.onPageTabMouseWheel(mouseX, mouseY, wheelMovement) )
                return;

            TabBase tab = getTabAtPosition(mouseX, mouseY);
            if ( tab != null && tab.onMouseWheel(mouseX, mouseY, wheelMovement) )
                return;
        }

        super.handleMouseInput();
    }

    @Nullable
    protected IPageTabProvider getPageTabAtPosition(int mouseX, int mouseY) {
        int x = mouseX - getPageHorizontalOffset();
        int y = mouseY - getPageVerticalOffset();

        if ( y >= 0 || x < 0 )
            return null;

        if ( isPageTabVisible() ) {
            int width = getPageTabWidth();
            int height = getPageTabHeight();

            if ( height + y >= 0 && x < width )
                return this;

            x -= width;
            if ( x < 0 )
                return null;
        }

        if ( pages != null ) {
            for (PageBase page : pages) {
                if ( !page.isEnabled() || !page.isVisible() || !page.isPageTabVisible() )
                    continue;

                int width = page.getPageTabWidth();
                if ( page.getPageTabHeight() + y >= 0 && x < width )
                    return page;

                x -= width;
                if ( x < 0 )
                    return null;
            }
        }

        return null;
    }

    @Override
    protected void mouseClicked(int mX, int mY, int mouseButton) throws IOException {
        int x = mX - guiLeft;
        int y = mY - guiTop;

        IPageTabProvider pageTab = getPageTabAtPosition(x, y);
        if ( pageTab != null ) {
            if ( pageTab.onPageTabMousePressed(mouseX, mouseY, mouseButton) )
                return;

            if ( mouseButton == 0 && setCurrentPage(pageTab) ) {
                playClickSound(1F);
                return;
            }
        }

        PageBase page = getCurrentPage();
        if ( page != null ) {
            if ( page.onMousePressed(x, y, mouseButton) )
                return;

            ArrayList<ElementBase> elements = this.elements;
            this.elements = EMPTY_ELEMENTS;
            super.mouseClicked(mX, mY, mouseButton);
            this.elements = elements;
            EMPTY_ELEMENTS.clear();

        } else
            super.mouseClicked(mX, mY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mX, int mY, int mouseButton) {
        int x = mX - guiLeft;
        int y = mY - guiTop;

        PageBase page = getCurrentPage();
        if ( page != null ) {
            page.onMouseReleased(x, y);

            ArrayList<ElementBase> elements = this.elements;
            this.elements = EMPTY_ELEMENTS;
            super.mouseReleased(mX, mY, mouseButton);
            this.elements = elements;
            EMPTY_ELEMENTS.clear();
        } else
            super.mouseReleased(mX, mY, mouseButton);
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
    public ElementBase getElementAtPosition(int mX, int mY) {
        PageBase page = getCurrentPage();
        if ( page != null )
            return page.getElementAtPosition(mX, mY);

        for (ElementBase element : elements) {
            if ( element.isVisible() && element.intersectsWith(mX, mY) )
                return element;
        }

        return null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        PageBase currentPage = getCurrentPage();
        if ( currentPage == null ) {
            if ( drawTitle && name != null ) {
                String name = StringHelper.localize(this.name);
                fontRenderer.drawString(name, getCenteredOffset(name), 6, 0x404040);
            }

            if ( drawInventory )
                fontRenderer.drawString(StringHelper.localize("container.inventory"), 8, ySize - 96 + 3, 0x404040);
        }

        drawElements(0, true);
        drawTabs(0, true);

        int x = getPageHorizontalOffset();
        int y = getPageVerticalOffset();

        if ( mainPageNeedsSize )
            updateMainPageTabSize();

        if ( isPageTabVisible() )
            x += drawPageTabForeground(x, y);

        if ( pages == null )
            return;

        for (PageBase page : pages) {
            if ( !page.isVisible() || !page.isEnabled() || !page.isPageTabVisible() )
                continue;

            x += page.drawPageTabForeground(x, y);
        }
    }

    protected void drawBackgroundOverSlots() {
        if ( xSize > 256 || ySize > 256 )
            drawSizedTexturedModalRect(guiLeft + 7, guiTop + ySize - (76 + 7), 8, 8, 162, 76, 512, 512);
        else
            drawTexturedModalRect(guiLeft + 7, guiTop + ySize - (76 + 7), 8, 8, 162, 76);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        PageBase currentPage = getCurrentPage();
        int color = backgroundColor;
        if ( currentPage != null )
            color = currentPage.backgroundColor;

        float colorR = (color >> 16 & 255) / 255.0F;
        float colorG = (color >> 8 & 255) / 255.0F;
        float colorB = (color & 255) / 255.0F;

        GlStateManager.color(colorR, colorG, colorB, 1.0F);
        bindTexture(texture);

        if ( xSize > 256 || ySize > 256 ) {
            drawSizedTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize, 512, 512);
        } else {
            drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        }

        if ( !slotsVisible )
            drawBackgroundOverSlots();

        this.mouseX = mouseX - guiLeft;
        this.mouseY = mouseY - guiTop;

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);

        drawElements(partialTick, false);
        drawTabs(partialTick, false);

        GlStateManager.popMatrix();

        int x = guiLeft + getPageHorizontalOffset();
        int y = guiTop + getPageVerticalOffset();

        if ( mainPageNeedsSize )
            updateMainPageTabSize();

        if ( isPageTabVisible() )
            x += drawPageTabBackground(x, y);

        if ( pages == null )
            return;

        for (PageBase page : pages) {
            if ( !page.isEnabled() || !page.isVisible() || !page.isPageTabVisible() )
                continue;

            x += page.drawPageTabBackground(x, y);
        }
    }

    @Override
    public List<String> getItemToolTip(ItemStack stack) {
        List<String> list = super.getItemToolTip(stack);
        Slot hoveredSlot = getSlotUnderMouse();
        if ( hoveredSlot != null ) {
            Item item = stack.getItem();
            if ( item instanceof ISlotContextTooltip )
                ((ISlotContextTooltip) item).addTooltipContext(list, tile, hoveredSlot, stack);

            if ( !hoveredSlot.canTakeStack(mc.player) && !(hoveredSlot instanceof SlotFilter) ) {
                List<String> additional = new ArrayList<>();
                additional.add(new TextComponentTranslation("info." + WirelessUtils.MODID + ".slot_lock")
                        .setStyle(TextHelpers.RED)
                        .getFormattedText());

                if ( item instanceof ILockExplanation )
                    ((ILockExplanation) item).addSlotLockExplanation(additional, tile, hoveredSlot, stack);

                list.addAll(1, additional);
            }
        }

        return list;
    }

    public void drawSizedSlicedTexturedRect(int x, int y, int width, int height, int u, int v, int border, int slicedWidth, int slicedHeight, float texW, float texH) {
        int mid = border * 2;

        // Upper Left Corner
        drawSizedTexturedModalRect(x, y, u, v, border, border, texW, texH);

        // Upper Middle
        drawSizedTexturedTiledRect(
                x + border, y,
                width - mid, border,
                u + border, v,
                slicedWidth - mid, border,
                texW, texH
        );

        // Upper Right Corner
        drawSizedTexturedModalRect(x + width - border, y, u + slicedWidth - border, v, border, border, texW, texH);

        // Mid Left
        drawSizedTexturedTiledRect(
                x, y + border,
                border, height - mid,
                u, v + border,
                border, slicedHeight - mid,
                texW, texH
        );

        // Middle
        drawSizedTexturedTiledRect(
                x + border, y + border,
                width - mid, height - mid,
                u + border, v + border,
                slicedWidth - mid, slicedHeight - mid,
                texW, texH
        );

        // Mid Right
        drawSizedTexturedTiledRect(
                x + width - border, y + border,
                border, height - mid,
                u + slicedWidth - border, v + border,
                border, slicedHeight - mid,
                texW, texH
        );

        // Bottom Left
        drawSizedTexturedModalRect(x, y + height - border, u, v + slicedHeight - border, border, border, texW, texH);

        // Bottom Mid
        drawSizedTexturedTiledRect(
                x + border, y + height - border,
                width - mid, border,
                u + border, v + slicedHeight - border,
                slicedWidth - mid, border,
                texW, texH
        );

        // Bottom Right
        drawSizedTexturedModalRect(x + width - border, y + height - border, u + slicedWidth - border, v + slicedHeight - border, border, border, texW, texH);
    }

    public void drawSizedTexturedTiledRect(int x, int y, int width, int height, int u, int v, int tileWidth, int tileHeight, float texW, float texH) {
        for (int i = 0; i < width; i += tileWidth) {
            for (int j = 0; j < height; j += tileHeight) {
                int drawWidth = Math.min(tileWidth, width - i);
                int drawHeight = Math.min(tileHeight, height - j);

                drawSizedTexturedModalRect(x + i, y + j, u, v, drawWidth, drawHeight, texW, texH);
            }
        }
    }

    public void drawTooltipHoveringText(List<String> list, int x, int y) {
        super.drawTooltipHoveringText(list, x, y, fontRenderer);
    }

    @Override
    public void drawTooltipHoveringText(List<String> list, int x, int y, FontRenderer font) {
        super.drawTooltipHoveringText(list, x, y, font);
    }

    public static boolean shouldDisplayWorkBudget(boolean hasSustained) {
        if ( ModConfig.common.workBudgetGUI == ModConfig.Common.WorkBudgetGUIState.DISABLED )
            return false;
        return hasSustained || ModConfig.common.workBudgetGUI == ModConfig.Common.WorkBudgetGUIState.ENABLED;
    }
}
