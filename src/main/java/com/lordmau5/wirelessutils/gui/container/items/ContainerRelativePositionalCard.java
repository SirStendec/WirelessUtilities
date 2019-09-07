package com.lordmau5.wirelessutils.gui.container.items;

import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerRelativePositionalCard extends BaseContainerItem {

    public ContainerRelativePositionalCard(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        super(stack, slot, inventory);

        BlockPosDimension origin = getOrigin();
        if ( origin == null ) {
            EntityPlayer player = inventory.player;
            if ( player != null && player.world != null ) {
                setOrigin(new BlockPosDimension(
                        player.getPosition(),
                        player.world.provider.getDimension()
                ));

                NBTTagCompound tag = stack.getTagCompound();
                if ( tag != null && tag.hasKey("X") && tag.hasKey("Y") && tag.hasKey("Z") )
                    setByte("Stage", (byte) 2);
                else
                    setByte("Stage", (byte) 1);
            }
        }
    }

    @Nullable
    public BlockPosDimension getTarget() {
        BlockPosDimension origin = getOrigin();
        if ( origin == null )
            return null;

        return ModItems.itemRelativePositionalCard.getTarget(stack, origin);
    }

    @Nullable
    public Vec3d getVector() {
        return ModItems.itemRelativePositionalCard.getVector(stack);
    }

    private boolean setByte(String key, byte value) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return false;

        tag.setByte(key, value);

        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    private boolean setInteger(String key, int value) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return false;

        tag.setInteger(key, value);

        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public int getX() {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : tag.getInteger("X");
    }

    public int getY() {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : tag.getInteger("Y");
    }

    public int getZ() {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : tag.getInteger("Z");
    }

    public byte getStage() {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? 0 : tag.getByte("Stage");
    }

    public void clear() {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag != null && !tag.hasKey("Locked") ) {
            tag.removeTag("Origin");
            tag.removeTag("Dimension");
            tag.removeTag("X");
            tag.removeTag("Y");
            tag.removeTag("Z");
            tag.removeTag("Stage");
            tag.removeTag("Facing");
            tag.removeTag("Range");

            stack.setTagCompound(tag);
            setItemStack(stack);
        }
    }

    public EnumFacing getFacing() {
        NBTTagCompound tag = stack.getTagCompound();
        byte ordinal = tag == null ? 0 : tag.getByte("Facing");
        return EnumFacing.byIndex(ordinal);
    }

    public BlockPosDimension getOrigin() {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey("Origin") || !tag.hasKey("Dimension") )
            return null;

        return new BlockPosDimension(
                BlockPos.fromLong(tag.getLong("Origin")),
                tag.getInteger("Dimension"),
                null
        );
    }

    public boolean setOrigin(BlockPosDimension origin) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return false;

        if ( origin == null ) {
            tag.removeTag("Origin");
            tag.removeTag("Dimension");
        } else {
            tag.setLong("Origin", origin.toLong());
            tag.setInteger("Dimension", origin.getDimension());
        }

        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public boolean setX(int x) {
        return setInteger("X", x) && (getOrigin() == null || setStage((byte) 2));
    }

    public boolean setY(int y) {
        return setInteger("Y", y) && (getOrigin() == null || setStage((byte) 2));
    }

    public boolean setZ(int z) {
        return setInteger("Z", z) && (getOrigin() == null || setStage((byte) 2));
    }

    public boolean setStage(byte stage) {
        return setByte("Stage", stage);
    }

    public boolean setFacing(EnumFacing facing) {
        if ( facing == null )
            facing = EnumFacing.NORTH;

        return setByte("Facing", (byte) facing.ordinal());
    }

}
