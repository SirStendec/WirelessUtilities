package com.lordmau5.wirelessutils.gui.container;

import com.lordmau5.wirelessutils.gui.slot.SlotDontTakeHeld;
import com.lordmau5.wirelessutils.gui.slot.SlotVisible;
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

    protected ItemStack stack;
    protected EntityPlayer player;
    protected int slot;

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

    @Nonnull
    public ItemStack getItemStack() {
        return stack;
    }

    public void setItemStack(@Nonnull ItemStack stack) {
        this.stack = stack;
    }

    public void sendUpdate() {
        if ( !suppressUpdates ) {
            player.inventory.setInventorySlotContents(slot, stack);
            PacketUpdateItem.updateItem(player, admin, slot, stack);
        }
    }

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
