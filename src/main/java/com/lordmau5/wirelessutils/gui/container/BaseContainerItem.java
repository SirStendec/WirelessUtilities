package com.lordmau5.wirelessutils.gui.container;

import com.lordmau5.wirelessutils.gui.slot.SlotDontTakeHeld;
import com.lordmau5.wirelessutils.gui.slot.SlotVisible;
import com.lordmau5.wirelessutils.item.base.IClearableItem;
import com.lordmau5.wirelessutils.item.base.ItemBase;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BaseContainerItem extends BaseContainer {

    public final static EntityEquipmentSlot[] ARMOR_SLOTS = new EntityEquipmentSlot[]{
            EntityEquipmentSlot.HEAD,
            EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS,
            EntityEquipmentSlot.FEET
    };

    private boolean shouldUpdateRendering = true;
    private boolean watchingChanges = true;
    private boolean changed = false;

    protected ItemStack stack;
    protected final EntityPlayer player;
    protected final int slot;

    final private boolean admin;
    protected boolean suppressUpdates = false;

    public BaseContainerItem(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        this(stack, false, slot, inventory);
    }

    public BaseContainerItem(@Nonnull ItemStack stack, boolean admin, int slot, @Nonnull InventoryPlayer inventory) {
        this.admin = admin;
        this.stack = stack;
        this.slot = slot;
        this.player = inventory.player;

        bindOwnSlots();
        bindPlayerInventory(inventory);
        bindPlayerArmorSlots(inventory);
    }

    public boolean isAdmin() {
        return admin;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    /* NBT Manipulation */

    public boolean setLocked(boolean locked) {
        if ( !isAdmin() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        if ( tag.getBoolean("Locked") )
            tag.removeTag("Locked");
        else
            tag.setBoolean("Locked", true);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public boolean isLocked() {
        Item item = stack.getItem();
        if ( !stack.isEmpty() && item instanceof ItemBase )
            return ((ItemBase) item).isLocked(stack);

        return false;
    }

    public boolean removeTag(String key) {
        if ( isLocked() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key) )
            return true;

        tag.removeTag(key);
        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    public byte getByte(String key, byte def) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_BYTE) )
            return def;

        return tag.getByte(key);
    }

    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    public int getInteger(String key, int def) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_INT) )
            return def;

        return tag.getInteger(key);
    }

    public double getDouble(String key) {
        return getDouble(key, 0D);
    }

    public double getDouble(String key, double def) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_DOUBLE) )
            return def;

        return tag.getDouble(key);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean def) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_BYTE) )
            return def;

        return tag.getBoolean(key);
    }

    public String getString(String key) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_STRING) )
            return null;

        return tag.getString(key);
    }

    public boolean setString(String key, String value) {
        if ( isLocked() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null && value == null )
            return true;
        else if ( tag == null )
            tag = new NBTTagCompound();

        if ( value == null ) {
            if ( !tag.hasKey(key, Constants.NBT.TAG_STRING) )
                return true;
            tag.removeTag(key);
        } else {
            if ( value.equals(tag.getString(key)) )
                return true;

            tag.setString(key, value);
        }

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public boolean setByte(String key, byte value) {
        if ( isLocked() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        if ( tag.hasKey(key, Constants.NBT.TAG_BYTE) && tag.getByte(key) == value )
            return true;

        tag.setByte(key, value);
        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public boolean setInteger(String key, int value) {
        if ( isLocked() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        if ( tag.hasKey(key, Constants.NBT.TAG_INT) && tag.getInteger(key) == value )
            return true;

        tag.setInteger(key, value);
        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public boolean setDouble(String key, double value) {
        if ( isLocked() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        if ( tag.hasKey(key, Constants.NBT.TAG_DOUBLE) && tag.getDouble(key) == value )
            return true;

        tag.setDouble(key, value);
        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public boolean setBoolean(String key, boolean value) {
        if ( isLocked() )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        if ( tag.hasKey(key, Constants.NBT.TAG_BYTE) && tag.getBoolean(key) == value )
            return true;

        tag.setBoolean(key, value);
        stack.setTagCompound(tag);
        setItemStack(stack);
        return true;
    }

    public boolean hasTag(String key) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(key);
    }

    /* ItemStack Stuff */

    public boolean canClearItemStack() {
        ItemStack stack = getItemStack();
        Item item = stack.getItem();
        if ( item instanceof IClearableItem )
            return ((IClearableItem) item).canClearItem(stack, player);

        return false;
    }

    public boolean clearItemStack() {
        ItemStack stack = getItemStack();
        Item item = stack.getItem();
        if ( item instanceof IClearableItem ) {
            IClearableItem clearable = (IClearableItem) item;
            if ( clearable.canClearItem(stack, player) ) {
                ItemStack cleared = clearable.clearItem(stack, player);
                if ( !cleared.isEmpty() ) {
                    setItemStack(cleared);
                    return true;
                }
            }
        }

        return false;
    }

    public void setWatchingChanges(boolean watching) {
        watchingChanges = watching;
    }

    public boolean hasChanges() {
        return changed;
    }

    @Nonnull
    public ItemStack getItemStack() {
        return stack;
    }

    public void setItemStack(@Nonnull ItemStack stack) {
        if ( stack == null )
            stack = ItemStack.EMPTY;

        if ( watchingChanges )
            changed = true;

        this.stack = stack;
        shouldUpdateRendering = true;
    }

    public boolean shouldUpdateRendering() {
        final boolean out = shouldUpdateRendering;
        shouldUpdateRendering = false;
        return out;
    }

    public void sendUpdateIfChanged() {
        if ( changed )
            sendUpdate();
    }

    public void sendUpdate() {
        if ( !suppressUpdates ) {
            player.inventory.setInventorySlotContents(slot, stack);
            PacketUpdateItem.updateItem(player, admin, slot, stack);
            changed = false;
        }
    }

    /* Container Stuff */

    @Override
    protected int getSizeInventory() {
        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    protected int getPlayerInventoryVerticalOffset() {
        return 94;
    }

    @Override
    protected int getPlayerInventoryHorizontalOffset() {
        return 30;
    }

    protected void bindOwnSlots() {

    }

    @Override
    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        int xOffset = getPlayerInventoryHorizontalOffset();
        int yOffset = getPlayerInventoryVerticalOffset();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new SlotDontTakeHeld(inventoryPlayer, j + i * 9 + 9, xOffset + j * 18, yOffset + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotDontTakeHeld(inventoryPlayer, i, xOffset + i * 18, yOffset + 58));
        }
    }

    protected void bindPlayerArmorSlots(InventoryPlayer inventory) {
        for (int i = 0; i < 4; i++) {
            EntityEquipmentSlot slot = ARMOR_SLOTS[i];
            addSlotToContainer(new SlotVisible(inventory, 35 + (4 - i), 8, 76 + (i * 18)) {
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, slot, player);
                }

                @Override
                public boolean canTakeStack(EntityPlayer playerIn) {
                    return !(!stack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(stack)) && super.canTakeStack(playerIn);
                }

                @Nullable
                @Override
                @SideOnly(Side.CLIENT)
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[slot.getIndex()];
                }
            });
        }

        addSlotToContainer(new SlotVisible(inventory, 40, 8, 152) {
            @Nullable
            @Override
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });
    }
}
