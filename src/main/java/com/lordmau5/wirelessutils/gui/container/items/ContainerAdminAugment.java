package com.lordmau5.wirelessutils.gui.container.items;

import cofh.core.network.PacketHandler;
import com.lordmau5.wirelessutils.gui.container.BaseContainerItem;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.item.base.IGuiItem;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import com.lordmau5.wirelessutils.utils.Level;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ContainerAdminAugment extends BaseContainerItem {

    private final ItemAugment item;
    private boolean wantNormalGui = false;

    public ContainerAdminAugment(@Nonnull ItemStack stack, int slot, @Nonnull InventoryPlayer inventory) {
        super(stack, true, slot, inventory);

        hideSlots();

        Item item = stack.getItem();
        if ( item instanceof ItemAugment )
            this.item = (ItemAugment) item;
        else
            this.item = null;
    }

    @Override
    public void sendUpdate() {
        if ( !suppressUpdates ) {
            player.inventory.setInventorySlotContents(slot, stack);
            PacketUpdateItem packet = PacketUpdateItem.getUpdatePacket(true, slot, stack);
            packet.addBool(wantNormalGui);
            PacketHandler.sendToServer(packet);
        }
    }

    public boolean hasNormalGui() {
        return item instanceof IGuiItem;
    }

    public void setWantNormalGui(boolean want) {
        wantNormalGui = want;
    }

    void removeTag(String key) {
        if ( isLocked() )
            return;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return;

        tag.removeTag(key);
        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        setItemStack(stack);
    }

    byte getByte(String key, byte def) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_BYTE) )
            return def;

        return tag.getByte(key);
    }

    int getInteger(String key, int def) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_INT) )
            return def;

        return tag.getInteger(key);
    }

    double getDouble(String key, double def) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_DOUBLE) )
            return def;

        return tag.getDouble(key);
    }

    boolean getBoolean(String key, boolean def) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_BYTE) )
            return def;

        return tag.getBoolean(key);
    }

    String getString(String key) {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey(key, Constants.NBT.TAG_STRING) )
            return null;

        return tag.getString(key);
    }

    void setString(String key, String value) {
        if ( isLocked() )
            return;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null && value == null )
            return;
        else if ( tag == null )
            tag = new NBTTagCompound();

        if ( value == null )
            tag.removeTag(key);
        else
            tag.setString(key, value);

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        setItemStack(stack);
    }

    void setByte(String key, byte value) {
        if ( isLocked() )
            return;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        tag.setByte(key, value);
        stack.setTagCompound(tag);
        setItemStack(stack);
    }

    void setInteger(String key, int value) {
        if ( isLocked() )
            return;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        tag.setInteger(key, value);
        stack.setTagCompound(tag);
        setItemStack(stack);
    }

    void setDouble(String key, double value) {
        if ( isLocked() )
            return;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        tag.setDouble(key, value);
        stack.setTagCompound(tag);
        setItemStack(stack);
    }

    void setBoolean(String key, boolean value) {
        if ( isLocked() )
            return;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        tag.setBoolean(key, value);
        stack.setTagCompound(tag);
        setItemStack(stack);
    }

    private boolean hasTag(String key) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(key);
    }

    public boolean hasRequiredLevel() {
        return hasTag("RequiredLevel");
    }

    @Nullable
    public Level getRequiredLevel() {
        return item.getRequiredLevel(stack);
    }

    public void setRequiredLevel(@Nullable Level level) {
        if ( level == null )
            removeTag("RequiredLevel");
        else
            setByte("RequiredLevel", (byte) level.toInt());
    }

    public boolean hasEnergyAddition() {
        return hasTag("EnergyAdd");
    }

    public int getEnergyAddition() {
        return item.getEnergyAddition(stack, null);
    }

    public void clearEnergyAddition() {
        removeTag("EnergyAdd");
    }

    public void setEnergyAddition(int value) {
        setInteger("EnergyAdd", value);
    }

    public boolean hasEnergyMultiplier() {
        return hasTag("EnergyMult");
    }

    public double getEnergyMultiplier() {
        return item.getEnergyMultiplier(stack, null);
    }

    public void clearEnergyMultiplier() {
        removeTag("EnergyMult");
    }

    public void setEnergyMultiplier(double value) {
        setDouble("EnergyMult", value);
    }

    public boolean hasEnergyDrain() {
        return hasTag("EnergyDrain");
    }

    public int getEnergyDrain() {
        return item.getEneryDrain(stack, null);
    }

    public void clearEnergyDrain() {
        removeTag("EnergyDrain");
    }

    public void setEnergyDrain(int value) {
        setInteger("EnergyDrain", value);
    }

    public boolean hasBudgetAddition() {
        return hasTag("BudgetAdd");
    }

    public int getBudgetAddition() {
        return item.getBudgetAddition(stack, null);
    }

    public void clearBudgetAddition() {
        removeTag("BudgetAdd");
    }

    public void setBudgetAddition(int value) {
        setInteger("BudgetAdd", value);
    }

    public boolean hasBudgetMultiplier() {
        return hasTag("BudgetMult");
    }

    public double getBudgetMultiplier() {
        return item.getBudgetMultiplier(stack, null);
    }

    public void clearBudgetMultiplier() {
        removeTag("BudgetMult");
    }

    public void setBudgetMultiplier(double value) {
        setDouble("BudgetMult", value);
    }

    public boolean hasTierName() {
        return hasTag("TierName");
    }

    public String getTierName() {
        String out = getString("TierName");
        if ( out == null )
            out = "";

        return out;
    }

    public void setTierName(@Nullable String name) {
        setString("TierName", name);
    }

    public boolean hasAllowedMachines() {
        return hasTag("AllowedMachines");
    }

    @Nullable
    public String[] getAllowedMachines() {
        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey("AllowedMachines", Constants.NBT.TAG_LIST) )
            return null;

        NBTTagList list = tag.getTagList("AllowedMachines", Constants.NBT.TAG_STRING);
        if ( list == null )
            return null;

        String[] out = new String[list.tagCount()];

        for (int i = 0; i < out.length; i++)
            out[i] = list.getStringTagAt(i);

        return out;
    }

    public void setAllowedMachines(@Nullable String[] machines) {
        if ( isLocked() )
            return;

        if ( machines == null ) {
            removeTag("AllowedMachines");
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();

        NBTTagList list = new NBTTagList();
        for (String machine : machines)
            list.appendTag(new NBTTagString(machine));

        stack.setTagCompound(tag);
        setItemStack(stack);
    }
}
