package com.lordmau5.wirelessutils.tile.base;

import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.TimeTracker;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.utils.ItemStackHandler;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

public abstract class TileEntityBaseMachine extends TileEntityBaseArea implements IUpgradeable, ILevellingBlock {

    protected ItemStackHandler itemStackHandler;
    protected ItemStack[] augments = new ItemStack[0];

    protected Level level = Level.getMinLevel();
    protected boolean isCreative = false;
    protected boolean wasDismantled = false;

    protected TimeTracker tracker = new TimeTracker();
    private boolean activeCooldown = false;

    @Override
    public String getTileName() {
        Machine machine = this.getClass().getAnnotation(Machine.class);
        if ( machine != null )
            return "tile." + WirelessUtils.MODID + "." + machine.name() + ".name";

        return null;
    }

    /* Levels and Augments */

    public Level getLevel() {
        return level;
    }

    public boolean isCreative() {
        return isCreative;
    }

    public void setLevel(Level level) {
        this.level = level;
        this.isCreative = level.isCreative;

        int slots = getNumAugmentSlots();
        if ( augments == null || slots != augments.length ) {
            ItemStack[] oldAugments = augments;

            augments = new ItemStack[slots];
            Arrays.fill(augments, ItemStack.EMPTY);

            if ( oldAugments != null && oldAugments.length > 0 )
                System.arraycopy(oldAugments, 0, augments, 0, Math.min(slots, oldAugments.length));
        }

        if ( world != null && !world.isRemote ) {
            IBlockState state = world.getBlockState(pos);
            if ( state.getValue(Properties.LEVEL) != level.toInt() ) {
                world.setBlockState(pos, state.withProperty(Properties.LEVEL, level.toInt()));
            }
        }

        updateLevel();
    }

    @Override
    public boolean canUpgrade(ItemStack upgrade) {
        Item item = upgrade.getItem();
        if ( item == ModItems.itemLevelUpgrade ) {
            Level currentLevel = getLevel();
            Level newLevel = ModItems.itemLevelUpgrade.getLevel(upgrade);

            return newLevel.ordinal() == currentLevel.ordinal() + 1;

        } else if ( item == ModItems.itemConversionUpgrade ) {
            Level currentLevel = getLevel();
            Level newLevel = ModItems.itemLevelUpgrade.getLevel(upgrade);

            return newLevel.ordinal() > currentLevel.ordinal();

        } else if ( item instanceof ItemAugment ) {
            if ( !isValidAugment(upgrade) )
                return false;

            ItemStack[] augments = getAugmentSlots();
            if ( augments == null )
                return false;

            for (int i = 0; i < augments.length; i++) {
                if ( augments[i].isEmpty() && isValidAugment(i, upgrade) )
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean installUpgrade(ItemStack upgrade) {
        Item item = upgrade.getItem();
        if ( item == ModItems.itemLevelUpgrade ) {
            Level currentLevel = getLevel();
            Level newLevel = ModItems.itemLevelUpgrade.getLevel(upgrade);

            if ( newLevel.ordinal() != currentLevel.ordinal() + 1 )
                return false;

            setLevel(newLevel);

        } else if ( item == ModItems.itemConversionUpgrade ) {
            Level currentLevel = getLevel();
            Level newLevel = ModItems.itemLevelUpgrade.getLevel(upgrade);

            if ( newLevel.ordinal() <= currentLevel.ordinal() )
                return false;

            setLevel(newLevel);

        } else if ( item instanceof ItemAugment ) {
            if ( !isValidAugment(upgrade) )
                return false;

            if ( upgrade.getCount() > 1 ) {
                upgrade = upgrade.copy();
                upgrade.setCount(1);
            }

            if ( !installAugment(upgrade) )
                return false;

            updateAugmentStatus();

        } else
            return false;

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        return true;
    }

    @Override
    public ItemStack[] getAugmentSlots() {
        return augments;
    }

    public void updateAugmentStatus() {
        Map<ItemAugment, ItemStack> augments = new Object2ObjectOpenHashMap<>();

        for (ItemStack stack : getAugmentSlots()) {
            if ( stack.isEmpty() )
                continue;

            Item itemIn = stack.getItem();
            if ( itemIn instanceof ItemAugment ) {
                ItemAugment item = (ItemAugment) itemIn;
                ItemStack other = augments.getOrDefault(item, ItemStack.EMPTY);
                if ( item.isUpgrade(other, stack) )
                    augments.put(item, stack);
            }
        }

        for (ItemAugment item : ItemAugment.AUGMENT_TYPES) {
            ItemStack stack = augments.getOrDefault(item, ItemStack.EMPTY);
            item.apply(stack, this);
        }
    }

    /* Inventory */

    public void onContentsChanged(int slot) {

    }

    public int getStackLimit(int slot) {
        return 64;
    }

    public int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(getStackLimit(slot), stack.getMaxStackSize());
    }

    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    public void readInventoryFromNBT(NBTTagCompound tag) {
        if ( itemStackHandler != null && tag.hasKey("Inventory") )
            itemStackHandler.deserializeNBT(tag.getCompoundTag("Inventory"));
    }

    public void writeInventoryToNBT(NBTTagCompound tag) {
        if ( itemStackHandler == null )
            return;

        NBTTagCompound inventory = itemStackHandler.serializeNBT();
        if ( inventory.getInteger("Size") > 0 )
            tag.setTag("Inventory", inventory);
    }

    protected void initializeItemStackHandler(int size) {
        itemStackHandler = new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                TileEntityBaseMachine.this.onContentsChanged(slot);
                TileEntityBaseMachine.this.markChunkDirty();
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if ( !isItemValid(slot, stack) )
                    return stack;

                return super.insertItem(slot, stack, simulate);
            }

            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                return TileEntityBaseMachine.this.getStackLimit(slot, stack);
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return isItemValidForSlot(slot, stack);
            }
        };
    }

    public ItemStackHandler getInventory() {
        return itemStackHandler;
    }

    @Override
    public int getInvSlotCount() {
        if ( itemStackHandler == null )
            return 0;

        return itemStackHandler.getSlots();
    }

    /* Life Cycle */

    public void dropContents() {
        ItemStack[] augments = getAugmentSlots();
        if ( augments != null ) {
            for (int i = 0; i < augments.length; i++)
                CoreUtils.dropItemStackIntoWorldWithVelocity(augments[i], world, pos);
        }

        if ( itemStackHandler != null ) {
            int length = itemStackHandler.getSlots();
            for (int i = 0; i < length; i++)
                CoreUtils.dropItemStackIntoWorldWithVelocity(itemStackHandler.getStackInSlot(i), world, pos);
        }
    }

    @Override
    public void blockBroken() {
        if ( world != null && pos != null && !wasDismantled )
            dropContents();

        super.blockBroken();
    }

    @Override
    public void blockDismantled() {
        wasDismantled = true;
        super.blockDismantled();
    }

    /* NBT Save and Load */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        // These need to happen before we read extra, so do
        // these before the super call.
        readLevelFromNBT(tag);
        readAugmentsFromNBT(tag);
        updateAugmentStatus();

        super.readFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeLevelToNBT(tag);
        writeAugmentsToNBT(tag);
        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        readInventoryFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);
        writeInventoryToNBT(tag);
        return tag;
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    /* Block State */

    public void setActive(boolean active) {
        isActive = active;
        if ( world == null || world.isRemote )
            return;

        if ( isActive == wasActive )
            return;

        if ( !activeCooldown ) {
            activeCooldown = true;
            tracker.markTime(world);

        } else if ( tracker.hasDelayPassed(world, 10) )
            activeCooldown = false;
        else
            return;

        wasActive = isActive;
        sendTilePacket(Side.CLIENT);
    }


    /* Packets */

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addByte(level.ordinal());
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        setLevel(payload.getByte());
    }

    /* Debugging */

    public void debugPrint() {
        System.out.println("Class: " + getClass().getCanonicalName());
        System.out.println("Level: " + level.toInt() + ": " + level.getName());
        System.out.println("Active: " + isActive);
        System.out.println("Dismantled: " + wasDismantled);

        ItemStack[] augments = getAugmentSlots();
        System.out.println("Augments: " + (augments == null ? "NULL" : "[" + augments.length + "]"));
        if ( augments != null )
            for (int i = 0; i < augments.length; i++)
                System.out.println("  " + i + ": " + augments[i].toString());

        System.out.println("Inventory: " + (itemStackHandler == null ? "NULL" : "[" + itemStackHandler.getSlots() + "]"));
        if ( itemStackHandler != null ) {
            int slots = itemStackHandler.getSlots();
            for (int i = 0; i < slots; i++)
                System.out.println("  " + i + ": " + itemStackHandler.getStackInSlot(i).toString());
        }
    }
}
