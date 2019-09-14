package com.lordmau5.wirelessutils.gui.client.vaporizer;

import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.ElementFluidTankVaporizer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementWorkBudget;
import com.lordmau5.wirelessutils.gui.container.vaporizer.ContainerPositionalVaporizer;
import com.lordmau5.wirelessutils.item.base.ItemBaseEntityPositionalCard;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.vaporizer.TilePositionalVaporizer;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class GuiPositionalVaporizer extends GuiBaseVaporizer {

    private final TilePositionalVaporizer vaporizer;
    private final ContainerPositionalVaporizer container;

    public GuiPositionalVaporizer(InventoryPlayer player, TilePositionalVaporizer vaporizer) {
        super(new ContainerPositionalVaporizer(player, vaporizer), vaporizer);
        this.container = (ContainerPositionalVaporizer) inventorySlots;
        this.vaporizer = vaporizer;

        generateInfo("tab." + WirelessUtils.MODID + ".positional_vaporizer");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 10, 46, vaporizer.getEnergyStorage()).setInfinite(vaporizer.isCreative()));

        if ( shouldDisplayWorkBudget(vaporizer.hasSustainedRate()) )
            addElement(new ElementWorkBudget(this, 26, 46, vaporizer));

        if ( vaporizer.hasFluid() )
            addElement(new ElementFluidTankVaporizer(this, 36, 52, vaporizer).setAlwaysShow(true).setSmall().drawTank(true));

        addElement(new ElementAreaButton(this, vaporizer, 152, 92));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

        if ( !isMainPage() )
            return;

        int xPos = guiLeft + 116;
        int yPos = guiTop + 38;

        bindTexture(GuiBaseVaporizer.TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(xPos - 1, yPos - 1, 176, 43, 54, 54);

        int slotIndex = container.getPositionalSlotOffset();

        boolean hasInvalidStack = false;
        ItemStack held = mc.player.inventory.getItemStack();
        if ( !held.isEmpty() && held.getItem() instanceof ItemBasePositionalCard ) {
            if ( vaporizer.isPositionalCardValid(held) ) {
                ItemBasePositionalCard card = (ItemBasePositionalCard) held.getItem();
                if ( !card.shouldIgnoreDistance(held) ) {
                    if ( card instanceof ItemBaseEntityPositionalCard ) {
                        // TODO: Distance to entity?

                    } else {
                        BlockPosDimension target = card.getTarget(held, vaporizer.getPosition());
                        if ( target == null || !vaporizer.isTargetInRange(target) )
                            hasInvalidStack = true;
                    }
                }
            } else
                hasInvalidStack = true;
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++, slotIndex++) {
                int color = (0x99 << 24) + (NiceColors.COLORS[slotIndex % NiceColors.COLORS.length] & 0xFFFFFF);
                if ( hasInvalidStack || !vaporizer.isSlotUnlocked(slotIndex) )
                    color = 0x99444444;

                drawRect(xPos + (x * 18), yPos + (y * 18), xPos + 16 + (x * 18), yPos + 16 + (y * 18), color);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        if ( !isMainPage() )
            return;

        String range = StringHelper.formatNumber(vaporizer.getRange());
        if ( vaporizer.isInterdimensional() )
            range = TextFormatting.OBFUSCATED + "999";

        drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".range"), 90, 63, 0x404040);
        fontRenderer.drawString(range, 94, 63, 0);
    }
}
