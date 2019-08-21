package com.lordmau5.wirelessutils.plugins.JEI;

import cofh.core.gui.element.ElementBase;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementFluidLock;
import com.lordmau5.wirelessutils.gui.slot.SlotFilter;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GhostIngredientHandler implements IGhostIngredientHandler<BaseGuiContainer> {

    @Override
    public <I> List<Target<I>> getTargets(BaseGuiContainer gui, I ingredient, boolean doStart) {
        List<Target<I>> targets = new ArrayList<>();

        FluidStack fluid = null;

        if ( ingredient instanceof ItemStack ) {
            ItemStack stack = (ItemStack) ingredient;
            fluid = ElementFluidLock.getItemStackFluid(stack);

            for (Slot slot : gui.inventorySlots.inventorySlots) {
                if ( !slot.isEnabled() )
                    continue;

                if ( slot instanceof SlotFilter ) {
                    if ( !slot.isItemValid(stack) )
                        continue;

                    final Rectangle bounds = new Rectangle(
                            gui.getGuiLeft() + slot.xPos,
                            gui.getGuiTop() + slot.yPos,
                            17, 17
                    );

                    targets.add(new Target<I>() {
                        @Override
                        public Rectangle getArea() {
                            return bounds;
                        }

                        @Override
                        public void accept(I ingredient) {
                            slot.putStack((ItemStack) ingredient);
                        }
                    });
                }
            }
        } else if ( ingredient instanceof FluidStack )
            fluid = (FluidStack) ingredient;

        if ( fluid != null ) {
            for (ElementBase element : gui.getElements()) {
                if ( !element.isEnabled() || !element.isVisible() )
                    continue;

                if ( element instanceof ElementFluidLock ) {
                    final ElementFluidLock lock = (ElementFluidLock) element;
                    if ( lock.isLocked() && ((ElementFluidLock) element).getFluidStack() != null )
                        continue;

                    final Rectangle bounds = new Rectangle(
                            gui.getGuiLeft() + element.getPosX(),
                            gui.getGuiTop() + element.getPosY(),
                            element.getWidth(), element.getHeight()
                    );

                    targets.add(new Target<I>() {
                        @Override
                        public Rectangle getArea() {
                            return bounds;
                        }

                        @Override
                        public void accept(I ingredient) {
                            FluidStack fluid = null;
                            if ( ingredient instanceof FluidStack )
                                fluid = (FluidStack) ingredient;
                            else if ( ingredient instanceof ItemStack )
                                fluid = ElementFluidLock.getItemStackFluid((ItemStack) ingredient);

                            lock.setFluidStack(fluid);
                        }
                    });
                }
            }
        }

        return targets;
    }

    @Override
    public void onComplete() {
        /* no-op */
    }
}
