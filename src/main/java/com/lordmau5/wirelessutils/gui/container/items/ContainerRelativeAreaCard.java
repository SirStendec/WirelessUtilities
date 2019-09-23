package com.lordmau5.wirelessutils.gui.container.items;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
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

public class ContainerRelativeAreaCard extends ContainerAreaCard implements IRelativeCardConfig {

    public ContainerRelativeAreaCard(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        super(stack, slot, inventory);

        setWatchingChanges(false);

        BlockPosDimension origin = getOrigin();
        if ( origin == null ) {
            EntityPlayer player = inventory.player;
            if ( player != null && player.world != null && player.world.provider != null ) {
                setOrigin(new BlockPosDimension(
                        player.getPosition(),
                        player.world.provider.getDimension()
                ));

                NBTTagCompound tag = stack.getTagCompound();
                if ( tag != null && tag.hasKey("X") && tag.hasKey("Y") && tag.hasKey("Z") )
                    setByte("Stage", (byte) 2);
                else
                    setByte("Stage", (byte) 1);

                if ( !allowNullFacing() && (tag == null || !tag.hasKey("Facing")) )
                    setFacing(EnumFacing.NORTH);
            }
        }

        setWatchingChanges(true);
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

    public boolean setOrigin(@Nullable BlockPosDimension origin) {
        if ( isLocked() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null && origin == null )
            return true;
        else if ( tag == null )
            tag = new NBTTagCompound();

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

    @Nullable
    public BlockPosDimension getTarget() {
        BlockPosDimension origin = getOrigin();
        if ( origin == null )
            return null;

        return ModItems.itemRelativeAreaCard.getTarget(stack, origin);
    }

    @Nullable
    public Vec3d getVector() {
        return ModItems.itemRelativeAreaCard.getVector(stack);
    }

    public int getX() {
        return getInteger("X");
    }

    public int getY() {
        return getInteger("Y");
    }

    public int getZ() {
        return getInteger("Z");
    }

    public byte getStage() {
        return getByte("Stage");
    }

    public boolean allowNullFacing() {
        return ModConfig.items.relativeCards.allowNullFacing;
    }

    @Nullable
    public EnumFacing getFacing() {
        return ModItems.itemRelativeAreaCard.getFacing(getItemStack());
    }

    public boolean setStage(byte stage) {
        return setByte("Stage", stage);
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

    public boolean setFacing(@Nullable EnumFacing facing) {
        if ( facing == null )
            return removeTag("Facing");

        return setByte("Facing", (byte) facing.ordinal());
    }
}
