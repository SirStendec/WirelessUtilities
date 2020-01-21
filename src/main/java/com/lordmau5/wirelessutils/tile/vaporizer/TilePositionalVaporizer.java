package com.lordmau5.wirelessutils.tile.vaporizer;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiPositionalVaporizer;
import com.lordmau5.wirelessutils.gui.container.vaporizer.ContainerPositionalVaporizer;
import com.lordmau5.wirelessutils.item.base.ItemBaseAreaCard;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.tile.base.IFacing;
import com.lordmau5.wirelessutils.tile.base.IPositionalMachine;
import com.lordmau5.wirelessutils.tile.base.IUnlockableSlots;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.ISlotAugmentable;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemRangeAugment;
import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemRelativePositionalCard;

@Machine(name = "positional_vaporizer")
public class TilePositionalVaporizer extends TileBaseVaporizer implements IFacing, IRangeAugmentable, ISlotAugmentable, IUnlockableSlots, IPositionalMachine {

    protected List<Tuple<BlockPosDimension, ItemStack>> validTargets;
    protected List<Iterable<Tuple<BlockPosDimension, ItemStack>>> areaTargets;
    private EnumFacing facing = EnumFacing.NORTH;

    private boolean interdimensional = false;
    private int range = 0;
    private int unlockedSlots = 0;

    public TilePositionalVaporizer() {
        super();
        initializeItemStackHandler(18 + getInputOffset());
    }

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("           Range: " + range);
        System.out.println("Interdimensional: " + interdimensional);
        System.out.println("  Unlocked Slots: " + unlockedSlots);
        System.out.println("          Facing: " + facing);
        System.out.println("   Valid Targets: " + (validTargets == null ? "NULL" : validTargets.size()));
        System.out.println("    Area Targets: " + (areaTargets == null ? "NULL" : areaTargets.size()));
    }

    /* IFacing */

    @Override
    public boolean canSideTransfer(TransferSide side) {
        return ModConfig.common.positionalConnections || (side != TransferSide.FRONT && side != TransferSide.TOP);
    }

    @Override
    public boolean onWrench(EntityPlayer player, EnumFacing side) {
        return rotateBlock(side);
    }

    @Override
    public EnumFacing getEnumFacing() {
        return facing;
    }

    @Override
    public boolean getRotationX() {
        return false;
    }

    @Override
    public boolean setRotationX(boolean rotationX) {
        return false;
    }

    @Override
    public boolean setFacing(EnumFacing facing) {
        if ( facing == this.facing )
            return true;

        if ( facing == EnumFacing.UP || facing == EnumFacing.DOWN )
            return false;

        this.facing = facing;
        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
            updateNodes();
        }

        return true;
    }

    @Override
    public boolean allowYAxisFacing() {
        return false;
    }

    /* Energy */

    public int getEnergyCost(double distance, boolean interdimensional) {
        int cost = ModConfig.vaporizers.positional.costInterdimensional;
        if ( !interdimensional ) {
            int dimCost = 0;
            if ( distance > 0 )
                dimCost = (int) Math.floor(
                        (ModConfig.vaporizers.positional.costPerBlock * distance) +
                                (ModConfig.vaporizers.positional.costPerBlockSquared * (distance * distance))
                );

            if ( dimCost < cost )
                return dimCost;
        }

        return cost;
    }

    /* Inventory */

    @Override
    public int getInputOffset() {
        return 9;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if ( slot < getInputOffset() )
            calculateTargets();
    }

    @Override
    public void readInventoryFromNBT(NBTTagCompound tag) {
        super.readInventoryFromNBT(tag);
        calculateTargets();
    }

    @Override
    public boolean allowEntityCards() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        if ( slot >= getInputOffset() )
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
        int maxSlot = getInputOffset();
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
        if ( slotIndex >= getInputOffset() )
            return super.isSlotUnlocked(slotIndex);

        return slotIndex < unlockedSlots;
    }

    @Override
    public void setUnlockedSlots(int slots, int tier) {
        unlockedSlots = slots;
    }

    /* Targeting */

    public Iterable<Tuple<Entity, ItemStack>> getEntityTargets() {
        return null;
    }

    public Iterable<Tuple<BlockPosDimension, ItemStack>> getTargets() {
        if ( areaTargets == null ) {
            tickActive();
            calculateTargets();
        }

        return IPositionalMachine.buildTargetIterator(areaTargets);
    }

    @Override
    public void onInactive() {
        super.onInactive();
        validTargets = null;
        areaTargets = null;
    }

    public void calculateTargets() {
        if ( world == null )
            return;

        if ( validTargets == null )
            validTargets = new ArrayList<>(9);
        else
            validTargets.clear();

        if ( areaTargets == null )
            areaTargets = new ArrayList<>();
        else
            areaTargets.clear();

        worker.clearTargetCache();
        clearRenderAreas();
        unloadAllChunks();

        BlockPosDimension origin = getPosition();

        int slots = Math.min(getInputOffset(), itemStackHandler.getSlots());
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

            final boolean sameDimension = target.getDimension() == origin.getDimension();

            if ( card instanceof ItemBaseAreaCard ) {
                ItemBaseAreaCard areaCard = (ItemBaseAreaCard) card;
                Iterable<Tuple<BlockPosDimension, ItemStack>> iterable = areaCard.getTargetArea(slotted, origin);
                if ( iterable == null )
                    continue;

                areaTargets.add(iterable);

                if ( sameDimension || chunkLoading ) {
                    Tuple<BlockPosDimension, BlockPosDimension> corners = areaCard.getCorners(slotted, target);
                    if ( corners != null ) {
                        if ( chunkLoading ) {
                            int x1 = corners.getFirst().getX() >> 4;
                            int z1 = corners.getFirst().getZ() >> 4;
                            int x2 = corners.getSecond().getX() >> 4;
                            int z2 = corners.getSecond().getZ() >> 4;

                            for (int x = x1; x <= x2; ++x) {
                                for (int z = z1; z <= z2; ++z) {
                                    loadChunk(BlockPosDimension.fromChunk(x, z, target.getDimension()));
                                }
                            }
                        }

                        if ( sameDimension )
                            addRenderArea(
                                    corners.getFirst(), corners.getSecond(),
                                    NiceColors.COLORS[i],
                                    slotted.hasDisplayName() ? slotted.getDisplayName() : null,
                                    null
                            );
                    }
                }

                continue;
            }

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

        if ( !validTargets.isEmpty() )
            areaTargets.add(validTargets);
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

            int slots = Math.min(getInputOffset(), itemStackHandler.getSlots());
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

            int slots = Math.min(getInputOffset(), itemStackHandler.getSlots());
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

    /* NBT */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        facing = EnumFacing.byIndex(tag.getByte("Facing"));
        if ( facing == EnumFacing.UP || facing == EnumFacing.DOWN )
            facing = EnumFacing.DOWN;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setByte("Facing", (byte) facing.ordinal());
        return tag;
    }

    /* Packets */

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addByte(getFacing());
        return payload;
    }

    @Override
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        setFacing(payload.getByte(), false);
    }

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
        return new GuiPositionalVaporizer(inventory, this);
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerPositionalVaporizer(inventory, this);
    }
}
