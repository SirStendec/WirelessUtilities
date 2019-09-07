package com.lordmau5.wirelessutils.item.augment;

import cofh.api.core.IAugmentable;
import cofh.core.util.helpers.ItemHelper;
import cofh.core.util.helpers.StringHelper;
import com.google.common.base.Predicate;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.item.GuiFilterAugment;
import com.lordmau5.wirelessutils.gui.container.items.ContainerFilterAugment;
import com.lordmau5.wirelessutils.item.base.IGuiItem;
import com.lordmau5.wirelessutils.item.base.IUpdateableItem;
import com.lordmau5.wirelessutils.packet.PacketUpdateItem;
import com.lordmau5.wirelessutils.tile.base.augmentable.IFilterAugmentable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.crafting.INBTPreservingIngredient;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemFilterAugment extends ItemAugment implements IGuiItem, IUpdateableItem, INBTPreservingIngredient {

    public ItemFilterAugment() {
        super();
        setName("filter_augment");
    }

    private final static String[] VALID_KEYS = {
            "MatchMod",
            "IgnoreMeta",
            "IgnoreNBT",
            "UseOreDict",
            "Whitelist",
            "Voiding",
            "List"
    };

    @Override
    public boolean isValidForCraft(@Nonnull IRecipe recipe, @Nonnull InventoryCrafting craft, @Nonnull ItemStack stack, @Nonnull ItemStack output) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        return tag == null || !tag.getBoolean("Locked");
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTTagForCraft(@Nonnull IRecipe recipe, @Nonnull InventoryCrafting craft, @Nonnull ItemStack stack, @Nonnull ItemStack output) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            return null;

        NBTTagCompound out = new NBTTagCompound();
        for (String key : VALID_KEYS) {
            if ( tag.hasKey(key) )
                out.setTag(key, tag.getTag(key));
        }

        if ( out.getSize() == 0 )
            return null;

        return out;
    }

    @Override
    public int getTiers() {
        return Math.min(ModConfig.augments.filter.availableTiers, Level.values().length);
    }

    public boolean canWhitelist(@Nonnull ItemStack stack) {
        int tier = getLevel(stack).toInt();
        boolean[] options = ModConfig.augments.filter.allowWhitelist;
        if ( options.length == 0 )
            return true;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    public boolean canIgnoreMetadata(@Nonnull ItemStack stack) {
        int tier = getLevel(stack).toInt();
        boolean[] options = ModConfig.augments.filter.allowIgnoreMetadata;
        if ( options.length == 0 )
            return true;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    public boolean canOreDict(@Nonnull ItemStack stack) {
        int tier = getLevel(stack).toInt();
        boolean[] options = ModConfig.augments.filter.allowOreDict;
        if ( options.length == 0 )
            return true;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    public boolean canIgnoreNBT(@Nonnull ItemStack stack) {
        int tier = getLevel(stack).toInt();
        boolean[] options = ModConfig.augments.filter.allowIgnoreNBT;
        if ( options.length == 0 )
            return true;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    public boolean canMatchMod(@Nonnull ItemStack stack) {
        int tier = getLevel(stack).toInt();
        boolean[] options = ModConfig.augments.filter.allowMatchingMod;
        if ( options.length == 0 )
            return true;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    public boolean canVoid(@Nonnull ItemStack stack) {
        int tier = getLevel(stack).toInt();
        boolean[] options = ModConfig.augments.filter.allowVoiding;
        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    public int getAvailableSlots(@Nonnull ItemStack stack) {
        int tier = getLevel(stack).toInt();
        int[] options = ModConfig.augments.filter.slotsPerTier;
        if ( options.length == 0 )
            return 18;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    @Override
    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int tier = getLevel(stack).toInt();
        double[] options = ModConfig.augments.filter.energyMultiplier;
        if ( options.length == 0 )
            return 1;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    @Override
    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int tier = getLevel(stack).toInt();
        int[] options = ModConfig.augments.filter.energyAddition;
        if ( options.length == 0 )
            return 0;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    @Override
    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int tier = getLevel(stack).toInt();
        int[] options = ModConfig.augments.filter.energyDrain;
        if ( options.length == 0 )
            return 0;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    @Override
    public int getBudgetAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int tier = getLevel(stack).toInt();
        int[] options = ModConfig.augments.filter.budgetAddition;
        if ( options.length == 0 )
            return 0;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    @Override
    public double getBudgetMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        int tier = getLevel(stack).toInt();
        double[] options = ModConfig.augments.filter.budgetMultiplier;
        if ( options.length == 0 )
            return 1;

        if ( tier >= options.length )
            tier = options.length - 1;

        return options[tier];
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        String name = "item." + WirelessUtils.MODID + ".filter_augment";

        boolean matchMod = getMatchMod(stack);
        if ( matchMod ) {
            tooltip.add(StringHelper.localize(name + ".match_mod"));
        } else {
            if ( getIgnoreMetadata(stack) )
                tooltip.add(StringHelper.localize(name + ".meta"));

            if ( getIgnoreNBT(stack) )
                tooltip.add(StringHelper.localizeFormat(name + ".nbt"));

            if ( getUseOreDict(stack) )
                tooltip.add(new TextComponentTranslation(name + ".ore")
                        .setStyle(TextHelpers.GREEN).getFormattedText());
        }

        if ( isVoiding(stack) )
            tooltip.add(new TextComponentTranslation(name + ".voiding")
                    .setStyle(TextHelpers.RED).getFormattedText());

        ItemStack[] list = getList(stack);
        if ( list != null && list.length > 0 ) {
            String bl = StringHelper.localize("btn." + WirelessUtils.MODID + "." + (isWhitelist(stack) ? "whitelist" : "blacklist"));
            if ( matchMod ) {
                Set<String> mods = new HashSet<>();
                for (ItemStack item : list) {
                    ResourceLocation itemName = item.isEmpty() ? null : item.getItem().getRegistryName();
                    String ns = itemName == null ? null : itemName.getNamespace();
                    if ( ns != null )
                        mods.add(ns);
                }

                if ( mods.size() == 1 || StringHelper.isAltKeyDown() ) {
                    Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
                    tooltip.add(bl);
                    for (String modId : mods) {
                        ModContainer mod = modMap == null ? null : modMap.get(modId);
                        tooltip.add(new TextComponentTranslation(
                                name + ".entry",
                                mod == null ? modId : mod.getName()
                        ).getFormattedText());
                    }
                } else
                    tooltip.add(new TextComponentTranslation(
                            name + ".info_list",
                            bl,
                            mods.size()
                    ).getFormattedText());

            } else if ( list.length == 1 || StringHelper.isAltKeyDown() ) {
                tooltip.add(bl);
                for (ItemStack item : list) {
                    tooltip.add(new TextComponentTranslation(
                            name + ".entry",
                            item.getTextComponent()
                    ).getFormattedText());
                }
            } else
                tooltip.add(new TextComponentTranslation(
                        name + ".info_list",
                        bl,
                        list.length
                ).getFormattedText());
        }
    }

    public boolean getMatchMod(@Nonnull ItemStack stack) {
        if ( !canMatchMod(stack) )
            return false;

        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("MatchMod");
        }

        return false;
    }

    @Nonnull
    public ItemStack setMatchMod(@Nonnull ItemStack stack, boolean match) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( match )
            tag.setBoolean("MatchMod", true);
        else
            tag.removeTag("MatchMod");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public boolean getIgnoreMetadata(@Nonnull ItemStack stack) {
        if ( !canIgnoreMetadata(stack) )
            return false;

        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("IgnoreMeta");
        }

        return false;
    }

    @Nonnull
    public ItemStack setIgnoreMetadata(@Nonnull ItemStack stack, boolean ignore) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( ignore )
            tag.setBoolean("IgnoreMeta", true);
        else
            tag.removeTag("IgnoreMeta");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public boolean getIgnoreNBT(@Nonnull ItemStack stack) {
        if ( !canIgnoreNBT(stack) )
            return false;

        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("IgnoreNBT");
        }

        return false;
    }

    @Nonnull
    public ItemStack setIgnoreNBT(@Nonnull ItemStack stack, boolean ignore) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( ignore )
            tag.setBoolean("IgnoreNBT", true);
        else
            tag.removeTag("IgnoreNBT");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public boolean getUseOreDict(@Nonnull ItemStack stack) {
        if ( !canOreDict(stack) )
            return false;

        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("UseOreDict");
        }

        return false;
    }

    @Nonnull
    public ItemStack setUseOreDict(@Nonnull ItemStack stack, boolean use) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( use )
            tag.setBoolean("UseOreDict", true);
        else
            tag.removeTag("UseOreDict");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public boolean isWhitelist(@Nonnull ItemStack stack) {
        if ( !canWhitelist(stack) )
            return false;

        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("Whitelist");
        }

        return false;
    }

    @Nonnull
    public ItemStack setWhitelist(@Nonnull ItemStack stack, boolean whitelist) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( whitelist )
            tag.setBoolean("Whitelist", true);
        else
            tag.removeTag("Whitelist");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    public boolean isVoiding(@Nonnull ItemStack stack) {
        if ( !canVoid(stack) )
            return false;

        if ( !stack.isEmpty() && stack.getItem() == this && stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag != null && tag.getBoolean("Voiding");
        }

        return false;
    }

    @Nonnull
    public ItemStack setVoiding(@Nonnull ItemStack stack, boolean voiding) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( voiding )
            tag.setBoolean("Voiding", true);
        else
            tag.removeTag("Voiding");

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Nullable
    public ItemStack[] getList(@Nonnull ItemStack stack) {
        return getList(stack, false);
    }

    @Nullable
    public ItemStack[] getList(@Nonnull ItemStack stack, boolean sparseList) {
        if ( stack.isEmpty() || stack.getItem() != this || !stack.hasTagCompound() )
            return null;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null || !tag.hasKey("List", Constants.NBT.TAG_LIST) )
            return null;

        NBTTagList list = tag.getTagList("List", Constants.NBT.TAG_COMPOUND);
        if ( list == null || list.tagCount() == 0 )
            return null;

        int length = list.tagCount();
        int maxSlots = getAvailableSlots(stack);

        if ( length > maxSlots )
            length = maxSlots;

        ItemStack[] out;

        if ( sparseList ) {
            out = new ItemStack[maxSlots];
            Arrays.fill(out, ItemStack.EMPTY);
        } else
            out = new ItemStack[length];

        for (int i = 0; i < length; i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int idx = sparseList ? itemTag.getByte("WUIndex") : i;
            if ( idx < out.length )
                out[idx] = new ItemStack(itemTag);
        }

        return out;
    }

    @Nonnull
    public ItemStack setList(@Nonnull ItemStack stack, ItemStack[] list) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return ItemStack.EMPTY;

        NBTTagCompound tag = stack.getTagCompound();
        if ( tag == null )
            tag = new NBTTagCompound();
        else if ( tag.getBoolean("Locked") )
            return ItemStack.EMPTY;

        if ( list == null || list.length == 0 )
            tag.removeTag("List");
        else {
            NBTTagList tagList = new NBTTagList();
            for (int i = 0; i < list.length; i++) {
                ItemStack item = list[i];
                if ( !item.isEmpty() ) {
                    NBTTagCompound itemTag = item.serializeNBT();
                    itemTag.setByte("WUIndex", (byte) i);
                    tagList.appendTag(itemTag);
                }
            }

            if ( tagList.tagCount() == 0 )
                tag.removeTag("List");
            else
                tag.setTag("List", tagList);
        }

        if ( tag.isEmpty() )
            tag = null;

        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if ( world.isRemote || player.isSneaking() )
            return super.onItemRightClick(world, player, hand);

        openGui(player, hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public Object getClientGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new GuiFilterAugment(new ContainerFilterAugment(stack, slot, player.inventory));
    }

    public Object getServerGuiElement(@Nonnull ItemStack stack, int slot, @Nonnull EntityPlayer player, @Nonnull World world) {
        return new ContainerFilterAugment(stack, slot, player.inventory);
    }

    @Override
    public void handleUpdatePacket(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, int slot, @Nonnull ItemStack newStack, @Nonnull PacketUpdateItem packet) {
        if ( stack.isEmpty() || newStack.isEmpty() || stack.getItem() != newStack.getItem() )
            return;

        setList(stack, getList(newStack, true));
        setWhitelist(stack, isWhitelist(newStack));
        setMatchMod(stack, getMatchMod(newStack));
        setIgnoreMetadata(stack, getIgnoreMetadata(newStack));
        setIgnoreNBT(stack, getIgnoreNBT(newStack));
        setUseOreDict(stack, getUseOreDict(newStack));
        setVoiding(stack, isVoiding(newStack));

        player.inventory.setInventorySlotContents(slot, stack);
    }

    private static String getMode(boolean mode) {
        return StringHelper.localize("btn." + WirelessUtils.MODID + ".mode." + (mode ? 2 : 1));
    }

    @Nullable
    public Predicate<ItemStack> getItemFilter(@Nonnull ItemStack stack) {
        final ItemStack[] matching = getList(stack);
        final boolean retVal = isWhitelist(stack);
        final boolean matchMod = getMatchMod(stack);
        final boolean ignoreMeta = getIgnoreMetadata(stack);
        final boolean ignoreNBT = getIgnoreNBT(stack);
        final boolean useOre = !matchMod && getUseOreDict(stack);

        if ( matching == null || matching.length == 0 )
            return null;

        Set<String> mods = matchMod ? new ObjectOpenHashSet<>() : null;
        IntOpenHashSet oreIDs = useOre ? new IntOpenHashSet() : null;
        List<ItemStack> stacks = matchMod ? null : new ObjectArrayList<>(matching.length);

        for (ItemStack input : matching) {
            if ( input == null || input.isEmpty() )
                continue;

            if ( useOre ) {
                int[] itemOres = OreDictionary.getOreIDs(input);
                if ( itemOres != null )
                    for (int ore : itemOres)
                        oreIDs.add(ore);
            }

            if ( matchMod ) {
                ResourceLocation name = input.getItem().getRegistryName();
                String mod = name == null ? null : name.getNamespace();

                if ( mod != null )
                    mods.add(mod);

            } else {
                ItemStack test = input.copy();
                if ( ignoreMeta )
                    test.setItemDamage(0);
                if ( ignoreNBT )
                    test.setTagCompound(null);

                boolean matched = false;
                for (ItemStack s : stacks)
                    if ( s.isItemEqual(test) ) {
                        matched = true;
                        break;
                    }

                if ( !matched )
                    stacks.add(test);
            }
        }

        // Return TRUE for items that should be allowed
        // into the machine, and FALSE for items that we
        // are filtering out.
        return item -> {
            // Always just skip empty items.
            if ( item == null || item.isEmpty() )
                return false;

            if ( matchMod ) {
                ResourceLocation name = item.getItem().getRegistryName();
                String mod = name == null ? null : name.getNamespace();
                if ( mod == null )
                    return false;

                if ( mods.contains(mod) )
                    return retVal;

            } else {
                if ( useOre ) {
                    int[] itemOres = OreDictionary.getOreIDs(item);
                    if ( itemOres != null )
                        for (int ore : itemOres)
                            if ( oreIDs.contains(ore) )
                                return retVal;
                }

                Item itemItem = item.getItem();
                for (ItemStack test : stacks) {
                    if ( test.getItem() != itemItem )
                        continue;
                    if ( !ignoreMeta && test.getItemDamage() != item.getItemDamage() )
                        continue;
                    if ( !ignoreNBT && !ItemHelper.doNBTsMatch(item.getTagCompound(), test.getTagCompound()) )
                        continue;

                    return retVal;
                }
            }

            return !retVal;
        };
    }

    @Override
    public void apply(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        if ( augmentable instanceof IFilterAugmentable ) {
            IFilterAugmentable filtered = (IFilterAugmentable) augmentable;
            filtered.setItemFilter(getItemFilter(stack));
            filtered.setVoidingItems(isVoiding(stack));
        }
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull Class<? extends IAugmentable> klass) {
        return IFilterAugmentable.class.isAssignableFrom(klass);
    }

    @Override
    public boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull IAugmentable augmentable) {
        return augmentable instanceof IFilterAugmentable;
    }
}
