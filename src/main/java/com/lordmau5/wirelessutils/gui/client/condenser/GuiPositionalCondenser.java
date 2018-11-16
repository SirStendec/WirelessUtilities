package com.lordmau5.wirelessutils.gui.client.condenser;

import cofh.core.gui.container.IAugmentableContainer;
import cofh.core.gui.element.ElementEnergyStored;
import cofh.core.gui.element.tab.TabEnergy;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiPositional;
import com.lordmau5.wirelessutils.gui.client.elements.*;
import com.lordmau5.wirelessutils.gui.container.condenser.ContainerPositionalCondenser;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityPositionalCondenser;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class GuiPositionalCondenser extends BaseGuiPositional {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/positional_machine.png");
    public static final ItemStack BUCKET = new ItemStack(Items.BUCKET);

    private final TileEntityPositionalCondenser condenser;

    private TabWorkInfo workInfo;
    private FluidStack fluid;

    public GuiPositionalCondenser(InventoryPlayer player, TileEntityPositionalCondenser condenser) {
        super(new ContainerPositionalCondenser(player, condenser), condenser, TEXTURE);
        this.condenser = condenser;

        generateInfo("tab." + WirelessUtils.MODID + ".positional_condenser");
    }

    @Override
    public void initGui() {
        super.initGui();

        addElement(new ElementEnergyStored(this, 10, 26, condenser.getEnergyStorage()).setInfinite(condenser.isCreative()));
        addElement(new ElementAreaButton(this, condenser, 152, 74));

        addTab(new TabEnergy(this, condenser, false));
        workInfo = (TabWorkInfo) addTab(new TabWorkInfo(this, condenser).setItem(BUCKET));
        addTab(new TabInfo(this, myInfo));

        addTab(new TabAugmentTwoElectricBoogaloo(this, (IAugmentableContainer) inventorySlots));
        addTab(new TabRedstoneControl(this, condenser));
        addTab(new TabRoundRobin(this, condenser));

        addElement(new ElementRangeControls(this, condenser, 138, 18));
        addElement(new ElementFluidTankCondenser(this, 34, 22, condenser).setAlwaysShow(true).setSmall().drawTank(true).setInfinite(condenser.isCreative()));

        addElement(new ElementFluidLock(this, condenser, 33, 58));
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        FluidStack fluid = condenser.getTankFluid();
        if ( (fluid == null && this.fluid == null) || (fluid != null && fluid.isFluidEqual(this.fluid)) )
            return;

        this.fluid = fluid;
        ItemStack stack = fluid == null ? null : FluidUtil.getFilledBucket(fluid);
        if ( stack == null || stack.isEmpty() )
            workInfo.setItem(BUCKET);
        else
            workInfo.setItem(stack);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);

        String range = StringHelper.formatNumber(condenser.getRange());
        if ( condenser.isInterdimensional() )
            range = TextFormatting.OBFUSCATED + "999";

        drawRightAlignedText(StringHelper.localize("btn." + WirelessUtils.MODID + ".range"), 90, 40, 0x404040);
        fontRenderer.drawString(range, 94, 40, 0);
    }
}
