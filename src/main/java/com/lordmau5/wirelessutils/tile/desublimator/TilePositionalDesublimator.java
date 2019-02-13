package com.lordmau5.wirelessutils.tile.desublimator;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.gui.client.desublimator.GuiPositionalDesublimator;
import com.lordmau5.wirelessutils.gui.container.desublimator.ContainerPositionalDesublimator;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.IPositionalMachine;
import com.lordmau5.wirelessutils.tile.base.ITargetProvider;
import com.lordmau5.wirelessutils.tile.base.IUnlockableSlots;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISlotAugmentable;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;

import javax.annotation.Nonnull;
import java.util.ArrayList;

import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemRangeAugment;
import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemRelativePositionalCard;

@Machine(name = "positional_desublimator")
public class TilePositionalDesublimator extends TileBaseDesublimator implements IRangeAugmentable, ISlotAugmentable, IUnlockableSlots, IPositionalMachine {

    private boolean interdimensional = false;
    private int range = 0;
    private int unlockedSlots = 0;

    public TilePositionalDesublimator() {
        super();
        initializeItemStackHandler(18 + getBufferOffset());
    }

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("           Range: " + range);
        System.out.println("Interdimensional: " + interdimensional);
        System.out.println("  Unlocked Slots: " + unlockedSlots);
    }

    /* Energy */

    public int getEnergyCost(double distance, boolean interdimensional) {
        int cost = ModConfig.condensers.positionalCondenser.costInterdimensional;
        if ( !interdimensional ) {
            int dimCost = 0;
            if ( distance > 0 )
                dimCost = (int) Math.floor(
                        (ModConfig.condensers.positionalCondenser.costPerBlock * distance) +
                                (ModConfig.condensers.positionalCondenser.costPerBlockSquared * (distance * distance))
                );

            if ( dimCost < cost )
                return dimCost;
        }

        return cost;
    }

    /* Inventory */

    @Override
    public int getBufferOffset() {
        return 9;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if ( slot < getBufferOffset() )
            calculateTargets();
    }

    @Override
    public void readInventoryFromNBT(NBTTagCompound tag) {
        super.readInventoryFromNBT(tag);
        calculateTargets();
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        if ( slot >= getBufferOffset() )
            return super.isItemValidForSlot(slot, stack);

        if ( !isPositionalCardValid(stack) || !isSlotUnlocked(slot) )
            return false;

        ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
        if ( card == null )
            return false;

        BlockPosDimension origin = getPosition();
        if ( origin == null )
            return false;

        BlockPosDimension target = card.getTarget(stack, origin);
        if ( target == null )
            return false;

        if ( !isTargetInRange(target) && !card.shouldIgnoreDistance(stack) )
            return false;

        int slots = itemStackHandler.getSlots();
        int maxSlot = getBufferOffset();
        for (int i = 0; i < slots && i < maxSlot; i++) {
            if ( i == slot )
                continue;

            ItemStack existing = itemStackHandler.getStackInSlot(i);
            if ( !isPositionalCardValid(existing) )
                continue;

            ItemBasePositionalCard existingCard = (ItemBasePositionalCard) existing.getItem();
            if ( existingCard == null )
                continue;

            BlockPosDimension existingTarget = existingCard.getTarget(existing, origin);
            if ( existingTarget != null && existingTarget.equals(target) )
                return false;
        }

        return true;
    }

    /* Unlockable Slots */

    public boolean isSlotUnlocked(int slotIndex) {
        if ( slotIndex >= getBufferOffset() )
            return super.isSlotUnlocked(slotIndex);

        return slotIndex < unlockedSlots;
    }

    @Override
    public void setUnlockedSlots(int slots, int tier) {
        unlockedSlots = slots;
    }

    /* Targeting */

    public void calculateTargets() {
        if ( world == null )
            return;

        if ( validTargets == null )
            validTargets = new ArrayList<>();
        else
            validTargets.clear();

        worker.clearTargetCache();
        clearRenderAreas();
        unloadAllChunks();

        BlockPosDimension origin = getPosition();

        int slots = Math.min(getBufferOffset(), itemStackHandler.getSlots());
        for (int i = 0; i < slots; i++) {
            ItemStack slotted = itemStackHandler.getStackInSlot(i);
            if ( !isPositionalCardValid(slotted) )
                continue;

            ItemBasePositionalCard card = (ItemBasePositionalCard) slotted.getItem();
            if ( card == null )
                continue;

            if ( card.updateCard(slotted, this) ) {
                itemStackHandler.setStackInSlot(i, slotted);
                markDirty();
            }

            BlockPosDimension target = card.getTarget(slotted, origin);
            if ( target == null || (!isTargetInRange(target) && !card.shouldIgnoreDistance(slotted)) )
                continue;

            boolean sameDimension = target.getDimension() == origin.getDimension();
            if ( sameDimension )
                addRenderArea(
                        target,
                        NiceColors.COLORS[i],
                        slotted.hasDisplayName() ? slotted.getDisplayName() : null,
                        card == itemRelativePositionalCard ? itemRelativePositionalCard.getVector(slotted) : null
                );

            if ( chunkLoading )
                loadChunk(target);

            validTargets.add(new Tuple<>(target, slotted));
        }

        ITargetProvider.sortTargetList(origin, validTargets);
    }

    /* Range and IRangeAugmentable */

    public boolean isInterdimensional() {
        return ModConfig.augments.range.enableInterdimensional && interdimensional;
    }

    public int getRange() {
        return range;
    }

    public void setRange(ItemStack augment) {
        boolean interdimensional = false;
        int range = ModConfig.augments.range.blocksPerTier;

        if ( !augment.isEmpty() && augment.getItem() == ModItems.itemRangeAugment ) {
            interdimensional = ModItems.itemRangeAugment.isInterdimensional(augment);
            range = ModItems.itemRangeAugment.getPositionalRange(augment);
        }

        if ( range == this.range && interdimensional == this.interdimensional )
            return;

        this.interdimensional = interdimensional;
        this.range = range;
        calculateTargets();
    }

    /* Augments */

    @Override
    public boolean canRemoveAugment(EntityPlayer player, int slot, ItemStack augment, ItemStack replacement) {
        if ( !super.canRemoveAugment(player, slot, augment, replacement) )
            return false;

        if ( itemStackHandler == null )
            return true;

        Item item = augment.getItem();
        if ( item == ModItems.itemSlotAugment ) {
            int maxSlots = 1;
            if ( !replacement.isEmpty() && replacement.getItem().equals(item) )
                maxSlots = ModItems.itemSlotAugment.getAvailableSlots(replacement);

            int slots = Math.min(getBufferOffset(), itemStackHandler.getSlots());
            if ( slots > maxSlots )
                for (int i = maxSlots; i < slots; i++) {
                    if ( !itemStackHandler.getStackInSlot(i).isEmpty() )
                        return false;
                }

        } else if ( item == itemRangeAugment ) {
            int maxRange = ModConfig.augments.range.blocksPerTier;
            if ( !replacement.isEmpty() && replacement.getItem().equals(item) ) {
                if ( itemRangeAugment.isInterdimensional(replacement) && ModConfig.augments.range.enableInterdimensional )
                    return true;
                else
                    maxRange = itemRangeAugment.getPositionalRange(replacement);
            }

            BlockPosDimension origin = getPosition();

            int slots = Math.min(getBufferOffset(), itemStackHandler.getSlots());
            for (int i = 0; i < slots; i++) {
                ItemStack stack = itemStackHandler.getStackInSlot(i);
                if ( !isPositionalCardValid(stack) )
                    continue;

                ItemBasePositionalCard card = (ItemBasePositionalCard) stack.getItem();
                if ( card == null )
                    continue;

                if ( card.shouldIgnoreDistance(stack) )
                    continue;

                BlockPosDimension target = card.getTarget(stack, origin);
                if ( target == null )
                    continue;

                if ( !isTargetInRange(target, maxRange, false) )
                    return false;
            }
        }

        return true;
    }

    /* Packets */

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addByte(unlockedSlots);
        payload.addBool(interdimensional);
        payload.addInt(range);
        return payload;
    }

    @Override
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);
        unlockedSlots = payload.getByte();
        interdimensional = payload.getBool();
        range = payload.getInt();
    }

    /* GUI */

    @Override
    public Object getGuiClient(InventoryPlayer inventory) {
        return new GuiPositionalDesublimator(inventory, this);
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerPositionalDesublimator(inventory, this);
    }
}
