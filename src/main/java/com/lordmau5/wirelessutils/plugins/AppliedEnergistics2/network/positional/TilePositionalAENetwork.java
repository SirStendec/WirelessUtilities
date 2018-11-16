package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AppliedEnergistics2Plugin;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.base.TileAENetworkBase;
import com.lordmau5.wirelessutils.tile.base.IPositionalMachine;
import com.lordmau5.wirelessutils.tile.base.IUnlockableSlots;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISlotAugmentable;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemRangeAugment;

@Machine(name = "positional_ae_network")
public class TilePositionalAENetwork extends TileAENetworkBase implements ISlotAugmentable, IUnlockableSlots, IPositionalMachine {

    private boolean interdimensional = false;
    private int range = 0;
    private int unlockedSlots = 0;

    public TilePositionalAENetwork() {
        super();
        initializeItemStackHandler(9);
    }

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("           Range: " + range);
        System.out.println("Interdimensional: " + interdimensional);
        System.out.println("  Unlocked Slots: " + unlockedSlots);
    }

    /* Events */

    @Override
    public void handleEvent(@Nonnull Event event) {
        if ( event instanceof BlockEvent ) {
            BlockEvent blockEvent = (BlockEvent) event;

            BlockPosDimension check = new BlockPosDimension(blockEvent.getPos(), blockEvent.getWorld().provider.getDimension());
            if ( validTargets.contains(check) ) {
                if ( blockEvent instanceof BlockEvent.PlaceEvent ) {
                    cachePlacedPosition(check);
                } else if ( blockEvent instanceof BlockEvent.BreakEvent ) {
                    destroyConnection(check);
                }
            }
        }
    }

    @Override
    public int calculateEnergyCost(double distance, boolean isInterdimensional) {
        int cost = ModConfig.plugins.appliedEnergistics.positionalAENetwork.costInterdimensional;
        if ( !isInterdimensional ) {
            int dimCost = 0;
            if ( distance > 0 )
                dimCost = (int) Math.floor(
                        (ModConfig.plugins.appliedEnergistics.positionalAENetwork.costPerBlock * distance) +
                                (ModConfig.plugins.appliedEnergistics.positionalAENetwork.costPerBlockSquared * (distance * distance))
                );

            if ( dimCost < cost )
                return dimCost;
        }

        return cost;
    }

    /* Inventory */

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        setNeedsRecalculation();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if ( !isPositionalCardValid(stack) || !isSlotUnlocked(slot) )
            return false;

        BlockPosDimension target = BlockPosDimension.fromTag(stack.getTagCompound());
        if ( target == null )
            return false;

        if ( !isTargetInRange(target) )
            return false;

        int slots = itemStackHandler.getSlots();
        for (int i = 0; i < slots; i++) {
            if ( i == slot )
                continue;

            ItemStack existing = itemStackHandler.getStackInSlot(i);
            if ( !isPositionalCardValid(existing) )
                continue;

            BlockPosDimension existingTarget = BlockPosDimension.fromTag(existing.getTagCompound());
            if ( existingTarget != null && existingTarget.equals(target) )
                return false;
        }

        return true;
    }

    /* Unlockable Slots */

    public boolean isSlotUnlocked(int slotIndex) {
        return slotIndex < unlockedSlots;
    }

    public void setUnlockedSlots(int slots, int tier) {
        unlockedSlots = slots;
    }

    /* Targeting */

    public void calculateTargets() {
        if ( validTargets == null )
            validTargets = new ArrayList<>();
        else
            validTargets.clear();

        clearRenderAreas();

        setEnergyCost(0);

        Set<BlockPosDimension> positions = new HashSet<>(getConnections().keySet());
        for (BlockPosDimension connectionPos : positions) {
            destroyConnection(connectionPos);
        }

        getConnections().clear();

        if ( !redstoneControlOrDisable() )
            return;

        setEnergyCost(IDLE_ENERGY_COST);

        BlockPosDimension origin = getPosition();
        int dimension = origin.getDimension();

        int slots = itemStackHandler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack slotted = itemStackHandler.getStackInSlot(i);
            if ( !isPositionalCardValid(slotted) )
                continue;

            BlockPosDimension target = BlockPosDimension.fromTag(slotted.getTagCompound());
            if ( target == null || !isTargetInRange(target) )
                continue;

            boolean sameDimension = target.getDimension() == dimension;
            if ( sameDimension )
                addRenderArea(target, NiceColors.COLORS[i]);

            BlockPosDimension targetPos = new BlockPosDimension(target, dimension);
            validTargets.add(targetPos);

            if ( !world.isRemote ) {
                cachePlacedPosition(targetPos);

                EventDispatcher.PLACE_BLOCK.addListener(targetPos, this);
                EventDispatcher.BREAK_BLOCK.addListener(targetPos, this);
            }
        }
    }

    @Nullable
    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(AppliedEnergistics2Plugin.blockPositionalRSNetwork);
    }

    /* Range */

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
        setNeedsRecalculation();
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

            int slots = itemStackHandler.getSlots();
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

            int slots = itemStackHandler.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = itemStackHandler.getStackInSlot(i);
                if ( !isPositionalCardValid(stack) )
                    continue;

                BlockPosDimension target = BlockPosDimension.fromTag(stack.getTagCompound());
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
        return new GuiPositionalAENetwork(inventory, this);
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerPositionalAENetwork(inventory, this);
    }
}
