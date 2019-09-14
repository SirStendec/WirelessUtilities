package com.lordmau5.wirelessutils.tile.vaporizer;

import cofh.core.network.PacketBase;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiDirectionalVaporizer;
import com.lordmau5.wirelessutils.gui.container.vaporizer.ContainerDirectionalVaporizer;
import com.lordmau5.wirelessutils.item.augment.ItemRangeAugment;
import com.lordmau5.wirelessutils.tile.base.IDirectionalMachine;
import com.lordmau5.wirelessutils.tile.base.ITargetProvider;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.base.augmentable.IFacingAugmentable;
import com.lordmau5.wirelessutils.tile.base.augmentable.IRangeAugmentable;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Machine(name = "directional_vaporizer")
public class TileDirectionalVaporizer extends TileBaseVaporizer implements
        IRangeAugmentable, IDirectionalMachine, IFacingAugmentable {

    private EnumFacing facing = EnumFacing.NORTH;
    private boolean rotationX = false;

    private boolean hasFacingAugment = false;
    private EnumFacing facingAugment = null;

    private int range = 0;
    private int rangeHeight = 0;
    private int rangeLength = 0;
    private int rangeWidth = 0;

    private int offsetHorizontal = 0;
    private int offsetVertical = 0;

    public TileDirectionalVaporizer() {
        super();
        initializeItemStackHandler(18);
    }

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
        System.out.println(" FaceAug: " + hasFacingAugment + " -- " + facingAugment);
    }

    /* IFacingAugmentable */

    public void setFacingAugmented(boolean augmented, @Nullable EnumFacing facing) {
        hasFacingAugment = augmented;
        facingAugment = facing;

        if ( validTargets != null )
            calculateTargets();
    }

    /* IFacing */

    @Override
    public boolean onWrench(EntityPlayer player, EnumFacing side) {
        return rotateBlock(side);
    }

    public boolean getRotationX() {
        return rotationX;
    }

    public boolean setRotationX(boolean rotationX) {
        if ( rotationX == this.rotationX )
            return true;

        this.rotationX = rotationX;
        if ( validTargets != null )
            calculateTargets();

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
        if ( validTargets != null )
            calculateTargets();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        return true;
    }

    /* Energy */

    public int getEnergyCost(double distance, boolean interdimensional) {
        int cost = ModConfig.vaporizers.directional.maximumCost;
        if ( !interdimensional ) {
            int dimCost = 0;
            if ( distance > 0 )
                dimCost = (int) Math.floor(
                        (ModConfig.vaporizers.directional.costPerBlock * distance) +
                                (ModConfig.vaporizers.directional.costPerBlockSquared * (distance * distance))
                );

            if ( dimCost < cost )
                return dimCost;
        }

        return cost;
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

    @Override
    public boolean canGetFullEntities() {
        return true;
    }

    @Nullable
    @Override
    public AxisAlignedBB getFullEntitiesAABB() {
        return getTargetBoundingBox(getPosition());
    }

    public void calculateTargets() {
        if ( world == null || pos == null || !world.isBlockLoaded(pos) )
            return;

        clearRenderAreas();
        worker.clearTargetCache();
        unloadAllChunks();

        if ( validTargets == null )
            validTargets = new ArrayList<>();
        else
            validTargets.clear();

        BlockPosDimension origin = getPosition();
        Tuple<BlockPosDimension, BlockPosDimension> corners = calculateTargetCorners(origin);

        int dimension = origin.getDimension();
        EnumFacing facing = hasFacingAugment ? facingAugment : getEnumFacing().getOpposite();

        for (BlockPos target : BlockPos.getAllInBox(corners.getFirst(), corners.getSecond())) {
            if ( target.equals(pos) )
                continue;

            BlockPosDimension bpd = new BlockPosDimension(target, dimension, facing);
            if ( chunkLoading )
                loadChunk(bpd);

            validTargets.add(new Tuple<>(bpd, ItemStack.EMPTY));
        }

        ITargetProvider.sortTargetList(origin, validTargets);
        addRenderArea(corners.getFirst().facing(facing), corners.getSecond());
    }

    public Iterable<Tuple<Entity, ItemStack>> getEntityTargets() {
        return null;
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
        if ( validTargets != null )
            calculateTargets();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }
    }

    public void setOffsetVertical(int offset) {
        if ( offset == offsetVertical )
            return;

        offsetVertical = offset;
        if ( validTargets != null )
            calculateTargets();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }
    }

    /* Range */

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

        if ( validTargets != null )
            calculateTargets();
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

        if ( validTargets != null )
            calculateTargets();
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

        if ( validTargets != null )
            calculateTargets();
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

        if ( validTargets != null )
            calculateTargets();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }
    }

    public int getRange() {
        return range;
    }

    @Override
    public void setRange(ItemStack augment) {
        this.range = ModItems.itemRangeAugment.getDirectionalRange(augment);
        setRanges(rangeHeight, rangeLength, rangeWidth);
    }

    /* Augments */

    @Override
    public boolean isValidAugment(int slot, ItemStack augment) {
        if ( augment.isEmpty() )
            return false;

        Item item = augment.getItem();
        if ( item instanceof ItemRangeAugment ) {
            ItemRangeAugment i = (ItemRangeAugment) item;
            if ( i.isInterdimensional(augment) )
                return false;

            if ( augment.getMetadata() >= ModConfig.augments.range.maxTierDirectional )
                return false;
        }

        return super.isValidAugment(slot, augment);
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

    /* GUI */

    @Override
    public Object getGuiClient(InventoryPlayer inventory) {
        return new GuiDirectionalVaporizer(inventory, this);
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerDirectionalVaporizer(inventory, this);
    }
}
