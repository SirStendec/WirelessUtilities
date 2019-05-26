package com.lordmau5.wirelessutils.gui.client.vaporizer;

import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.ElementFluidTank;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.container.vaporizer.ContainerPositionalVaporizer;
import com.lordmau5.wirelessutils.item.base.ItemBaseEntityPositionalCard;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.vaporizer.TilePositionalVaporizer;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

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

        if ( vaporizer.hasFluid() )
            addElement(new ElementFluidTank(this, 34, 52, vaporizer.getTank()).setAlwaysShow(true).setSmall().drawTank(true));

        addElement(new ElementAreaButton(this, vaporizer, 152, 92));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

        if ( moduleTab )
            return;

        int xPos = guiLeft + 116;
        int yPos = guiTop + 38;

        bindTexture(GuiBaseVaporizer.TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(xPos - 1, yPos - 1, 176, 43, 54, 54);

        int slotIndex = container.getPositionalSlotOffset();

        boolean hasInvalidStack = false;
        ItemStack held = mc.player.inventory.getItemStack();
        if ( !held.isEmpty() && vaporizer.isPositionalCardValid(held) ) {
            ItemBasePositionalCard card = (ItemBasePositionalCard) held.getItem();
            if ( card != null && !card.shouldIgnoreDistance(held) ) {
                if ( card instanceof ItemBaseEntityPositionalCard ) {
                    // TODO: Distance to entity?

                } else {
                    BlockPosDimension target = card.getTarget(held, vaporizer.getPosition());
                    if ( target == null || !vaporizer.isTargetInRange(target) )
                        hasInvalidStack = true;
                }
            }
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
}
