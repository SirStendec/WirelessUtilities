package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.GuiContainerCore;
import cofh.core.gui.element.ElementBase;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityBaseCondenser;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.List;

public class ElementFluidLock extends ElementBase {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    private final TileEntityBaseCondenser condenser;

    public ElementFluidLock(GuiContainerCore gui, TileEntityBaseCondenser condenser, int posX, int posY) {
        super(gui, posX, posY, 18, 18);
        this.condenser = condenser;
    }

    @Override
    public void addTooltip(List<String> list) {
        super.addTooltip(list);

        FluidStack heldStack = getHeldFluidStack();
        if ( heldStack != null ) {
            list.add(new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".with",
                    getStackComponent(heldStack)
            ).getFormattedText());
        } else {
            if ( condenser.isLocked() )
                list.add(new TextComponentTranslation("btn." + WirelessUtils.MODID + ".lock.unlock").getFormattedText());
            else
                list.add(new TextComponentTranslation("btn." + WirelessUtils.MODID + ".lock.lock").getFormattedText());
        }

        FluidStack stack = condenser.getLockStack();
        if ( stack != null ) {
            list.add(new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".lock.current",
                    getStackComponent(stack)
            ).setStyle(TextHelpers.GRAY).getFormattedText());
        }
    }

    public ITextComponent getStackComponent(FluidStack stack) {
        if ( stack == null )
            return null;

        Fluid fluid = stack.getFluid();
        if ( fluid == null )
            return null;

        EnumRarity rarity = fluid.getRarity(stack);
        return new TextComponentString(stack.getLocalizedName())
                .setStyle(TextHelpers.getStyle(rarity.color));
    }

    public ItemStack getHeldItem() {
        return gui.mc.player.inventory.getItemStack();
    }

    public static FluidStack getItemStackFluid(ItemStack stack) {
        if ( stack.isEmpty() )
            return null;

        if ( stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) ) {
            IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if ( handler != null ) {
                IFluidTankProperties[] properties = handler.getTankProperties();
                if ( properties != null && properties.length == 1 )
                    return properties[0].getContents();
            }
        }

        Item item = stack.getItem();


        Block block = Block.getBlockFromItem(stack.getItem());
        if ( block == null )
            return null;

        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
        if ( fluid == null )
            return null;

        return new FluidStack(fluid, Fluid.BUCKET_VOLUME);
    }

    public FluidStack getHeldFluidStack() {
        return getItemStackFluid(getHeldItem());
    }

    public boolean isLocked() {
        return condenser.isLocked();
    }

    @Nullable
    public FluidStack getFluidStack() {
        return condenser.getLockStack();
    }

    public void setFluidStack(FluidStack stack) {
        if ( stack == null )
            condenser.setLocked(false);
        else
            condenser.setLocked(stack);

        condenser.sendModePacket();
    }

    @Override
    public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {
        if ( mouseButton == 1 ) {
            if ( condenser.isLocked() ) {
                condenser.setLocked(false);
                condenser.sendModePacket();
            }

            return true;
        }

        ItemStack held = getHeldItem();
        FluidStack stack = getItemStackFluid(held);
        if ( stack != null ) {
            condenser.setLocked(stack);
            condenser.sendModePacket();

        } else if ( held.isEmpty() ) {
            condenser.setLocked(!condenser.isLocked());
            condenser.sendModePacket();
        }

        return true;
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float gameTicks) {
        gui.bindTexture(TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(posX, posY, 204, 0, 18, 18);
        gui.drawFluid(posX + 1, posY + 1, condenser.getLockStack(), 16, 16);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY) {
        gui.bindTexture(TEXTURE);
        GlStateManager.color(1, 1, 1, 1);

        boolean intersected = intersectsWith(mouseX, mouseY);
        boolean locked = condenser.isLocked();
        boolean drawLocked;

        ItemStack held = getHeldItem();
        FluidStack stack = getItemStackFluid(held);
        if ( intersected && stack != null )
            drawLocked = true;
        else if ( intersected && !held.isEmpty() )
            drawLocked = false;
        else
            drawLocked = intersected != locked;

        if ( locked )
            drawTexturedModalRect(posX, posY, 204, 18, 18, 18);

        if ( condenser.getLockStack() == null || intersected )
            drawTexturedModalRect(posX, posY, 204, drawLocked ? 36 : 54, 18, 18);
    }


}
