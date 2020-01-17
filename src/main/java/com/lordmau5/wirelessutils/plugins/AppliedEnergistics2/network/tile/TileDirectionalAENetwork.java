package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.tile;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AppliedEnergistics2Plugin;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.directional.ContainerDirectionalAENetwork;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.directional.GuiDirectionalAENetwork;
import com.lordmau5.wirelessutils.tile.base.IConfigurableRange;
import com.lordmau5.wirelessutils.tile.base.IDirectionalMachine;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.EnumSet;

@Machine(name = "directional_ae_network")
public class TileDirectionalAENetwork extends TileAENetworkBase implements
        IConfigurableRange, IDirectionalMachine {

    private EnumFacing facing = EnumFacing.NORTH;
    private boolean rotationX = false;

    private int range = 0;
    private int rangeHeight = 0;
    private int rangeLength = 0;
    private int rangeWidth = 0;

    private int offsetHorizontal = 0;
    private int offsetVertical = 0;

    /* Debugging */

    @Override
    public void debugPrint() {
        super.debugPrint();
        System.out.println("   Range: " + range);
        System.out.println("  Height: " + rangeHeight);
        System.out.println("  Length: " + rangeLength);
        System.out.println("   Width: " + rangeWidth);
        System.out.println("Offset H: " + offsetHorizontal);
        System.out.println("Offset V: " + offsetVertical);
        System.out.println("  Facing: " + facing);
        System.out.println(" Rotated: " + rotationX);
    }

    public boolean isDense() {
        boolean[] dense = ModConfig.plugins.appliedEnergistics.directionalAENetwork.dense;
        int idx = level.toInt();
        if ( idx < 0 )
            idx = 0;
        else if ( idx >= dense.length )
            idx = dense.length - 1;

        return dense[idx];
    }

    /* Energy */

    public int getBaseEnergy() {
        int[] levels = ModConfig.plugins.appliedEnergistics.directionalAENetwork.baseEnergy;
        int idx = level.toInt();
        if ( idx < 0 )
            idx = 0;
        else if ( idx >= levels.length )
            idx = levels.length - 1;

        return levels[idx];
    }

    public int getEnergyCost(double distance, boolean isInterdimensional) {
        int cost = ModConfig.plugins.appliedEnergistics.directionalAENetwork.maximumCost;
        if ( !isInterdimensional ) {
            int dimCost = 0;
            if ( distance > 0 )
                dimCost = (int) Math.floor(
                        (ModConfig.plugins.appliedEnergistics.directionalAENetwork.costPerBlock * distance) +
                                (ModConfig.plugins.appliedEnergistics.directionalAENetwork.costPerBlockSquared * (distance * distance))
                );

            if ( dimCost < cost )
                return dimCost;
        }

        return cost;
    }


    /* IFacing */

    @Override
    public boolean onWrench(EntityPlayer player, EnumFacing side) {
        scheduleRecalculate();
        return rotateBlock(side);
    }

    public boolean getRotationX() {
        return rotationX;
    }

    public boolean setRotationX(boolean rotationX) {
        if ( rotationX == this.rotationX )
            return true;

        this.rotationX = rotationX;
        scheduleRecalculate();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        return true;
    }

    public EnumFacing getEnumFacing() {
        return facing;
    }

    public boolean setFacing(EnumFacing facing) {
        if ( facing == this.facing )
            return true;

        if ( !allowYAxisFacing() && (facing == EnumFacing.UP || facing == EnumFacing.DOWN) )
            return false;

        if ( facing == EnumFacing.UP || facing == EnumFacing.DOWN ) {
            if ( this.facing != EnumFacing.DOWN && this.facing != EnumFacing.UP )
                rotationX = this.facing.getAxis() == EnumFacing.Axis.X;
        }

        this.facing = facing;
        scheduleRecalculate();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        return true;
    }


    /* Targeting */

    @Override
    public boolean usesDefaultColor() {
        return true;
    }

    @Override
    public void setDefaultColor(int color) {
        super.setDefaultColor(color);
        if ( validTargets != null )
            calculateTargets();
    }

    public void calculateTargetsDelegate() {
        clearRenderAreas();

        final BlockPosDimension origin = getPosition();
        final int dimension = origin.getDimension();

        final Tuple<BlockPosDimension, BlockPosDimension> corners = calculateTargetCorners(origin);
        final BlockPosDimension first = corners.getFirst();
        final BlockPosDimension second = corners.getSecond();

        for (BlockPosDimension target : BlockPosDimension.getAllInBox(first, second, dimension, null)) {
            if ( target.equalsIgnoreFacing(origin) )
                continue;

            addValidTarget(target, ItemStack.EMPTY);
        }

        addRenderArea(first.facing(getEnumFacing().getOpposite()), second);
    }

    @Nonnull
    @Override
    public ItemStack getMachineRepresentation() {
        return new ItemStack(AppliedEnergistics2Plugin.blockDirectionalAENetwork, 1, level.toInt());
    }

    /* Offset */

    public int getOffsetHorizontal() {
        if ( offsetHorizontal > rangeWidth )
            return rangeWidth;
        if ( offsetHorizontal < -rangeWidth )
            return -rangeWidth;

        return offsetHorizontal;
    }

    public int getOffsetVertical() {
        boolean facingY = getEnumFacing().getAxis() == EnumFacing.Axis.Y;
        int maxVertical = facingY ? rangeLength : rangeHeight;

        if ( offsetVertical > maxVertical )
            return maxVertical;
        if ( offsetVertical < -maxVertical )
            return -maxVertical;

        return offsetVertical;
    }

    public void setOffsetHorizontal(int offset) {
        if ( offset == offsetHorizontal )
            return;

        offsetHorizontal = offset;
        scheduleRecalculate();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }
    }

    public void setOffsetVertical(int offset) {
        if ( offset == offsetVertical )
            return;

        offsetVertical = offset;
        scheduleRecalculate();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }
    }

    /* Range */

    public boolean isFacingY() {
        return getEnumFacing().getAxis() == EnumFacing.Axis.Y;
    }

    public void saveRanges() {
        if ( world != null && world.isRemote )
            sendModePacket();
    }

    public int getRangeHeight() {
        return rangeHeight;
    }

    public int getRangeLength() {
        return rangeLength;
    }

    public int getRangeWidth() {
        return rangeWidth;
    }

    public void setRangeHeight(int range) {
        if ( range < 0 )
            range = 0;

        if ( range == rangeHeight )
            return;

        if ( range > rangeHeight ) {
            while ( range > 0 && !IDirectionalMachine.isRangeValid(this.range, range, rangeLength, rangeWidth) )
                range--;
        }

        rangeHeight = range;
        scheduleRecalculate();
    }

    public void setRangeLength(int range) {
        if ( range < 0 )
            range = 0;

        if ( range == rangeLength )
            return;

        if ( range > rangeLength ) {
            while ( range > 0 && !IDirectionalMachine.isRangeValid(this.range, rangeHeight, range, rangeWidth) )
                range--;
        }

        rangeLength = range;
        scheduleRecalculate();
    }

    public void setRangeWidth(int range) {
        if ( range < 0 )
            range = 0;

        if ( range == rangeWidth )
            return;

        if ( range > rangeWidth ) {
            while ( range > 0 && !IDirectionalMachine.isRangeValid(this.range, rangeHeight, rangeLength, range) )
                range--;
        }

        rangeWidth = range;
        scheduleRecalculate();
    }

    public void setRanges(int height, int length, int width) {
        height = Math.max(0, height);
        length = Math.max(0, length);
        width = Math.max(0, width);

        while ( !IDirectionalMachine.isRangeValid(range, height, length, width) ) {
            if ( height > 0 )
                height--;
            if ( width > 0 )
                width--;
            if ( length > 0 )
                length--;

            if ( height == 0 && width == 0 && length == 0 )
                break;
        }

        if ( height == rangeHeight && length == rangeLength && width == rangeWidth )
            return;

        rangeHeight = height;
        rangeLength = length;
        rangeWidth = width;

        scheduleRecalculate();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }
    }

    public int getRange() {
        return range;
    }

    public void setRange(ItemStack augment) {
        range = ModItems.itemRangeAugment.getDirectionalRange(augment);
        setRanges(rangeHeight, rangeLength, rangeWidth);
    }

    /* Packets */

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addByte(getFacing());
        payload.addBool(rotationX);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        setFacing(payload.getByte(), false);
        rotationX = payload.getBool();
    }

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addShort(range);
        payload.addByte(rangeHeight);
        payload.addByte(rangeLength);
        payload.addByte(rangeWidth);
        payload.addByte(offsetHorizontal);
        payload.addByte(offsetVertical);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);
        range = payload.getShort();
        setRanges(
                payload.getByte(),
                payload.getByte(),
                payload.getByte()
        );
        setOffsetHorizontal(payload.getByte());
        setOffsetVertical(payload.getByte());
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();
        payload.addByte(rangeHeight);
        payload.addByte(rangeLength);
        payload.addByte(rangeWidth);
        payload.addByte(offsetHorizontal);
        payload.addByte(offsetVertical);
        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);
        setRanges(payload.getByte(), payload.getByte(), payload.getByte());
        setOffsetHorizontal(payload.getByte());
        setOffsetVertical(payload.getByte());
    }

    /* NBT Save and Load */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        facing = EnumFacing.values()[tag.getByte("Facing")];
        rotationX = tag.getBoolean("RotationX");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setByte("Facing", (byte) facing.ordinal());
        tag.setBoolean("RotationX", rotationX);
        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);

        rangeHeight = tag.getByte("RangeHeight");
        rangeLength = tag.getByte("RangeLength");
        rangeWidth = tag.getByte("RangeWidth");
        offsetHorizontal = tag.getByte("OffsetHorizontal");
        offsetVertical = tag.getByte("OffsetVertical");
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);

        tag.setByte("RangeHeight", (byte) rangeHeight);
        tag.setByte("RangeLength", (byte) rangeLength);
        tag.setByte("RangeWidth", (byte) rangeWidth);
        tag.setByte("OffsetHorizontal", (byte) offsetHorizontal);
        tag.setByte("OffsetVertical", (byte) offsetVertical);
        return tag;
    }

    @Nonnull
    @Override
    public EnumSet<EnumFacing> getConnectableSides() {
        EnumSet<EnumFacing> sides = super.getConnectableSides();
        sides.remove(getEnumFacing());
        return sides;
    }

    /* GUI */

    @Override
    public Object getGuiClient(InventoryPlayer inventory) {
        return new GuiDirectionalAENetwork(inventory, this);
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerDirectionalAENetwork(inventory, this);
    }
}
