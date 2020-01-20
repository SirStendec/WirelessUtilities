package com.lordmau5.wirelessutils.gui.client.item;

import cofh.core.gui.element.tab.TabBase;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.elements.TabContainedButton;
import com.lordmau5.wirelessutils.gui.container.items.ContainerFilterAugment;
import com.lordmau5.wirelessutils.gui.slot.SlotFilter;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

public class GuiFilterAugment extends BaseGuiItem {

    public static final ItemStack DAMAGE_ON = new ItemStack(Items.BOW, 1, 195);
    public static final ItemStack DAMAGE_OFF = new ItemStack(Items.BOW, 1, 0);

    public static final ItemStack NBT_ON = new ItemStack(Items.REDSTONE);
    public static final ItemStack NBT_OFF = new ItemStack(Items.GUNPOWDER);

    public static final ItemStack WHITELIST = new ItemStack(Blocks.CONCRETE, 1, 0);
    public static final ItemStack BLACKLIST = new ItemStack(Blocks.CONCRETE, 1, 15);

    public static final ItemStack ORE_ON = new ItemStack(Items.WRITTEN_BOOK);
    public static final ItemStack ORE_OFF = new ItemStack(Items.BOOK);

    public static final ItemStack VOID_ON = new ItemStack(Items.LAVA_BUCKET);
    public static final ItemStack VOID_OFF = new ItemStack(Items.BUCKET);

    public static final ItemStack MATCH_ON = new ItemStack(ModItems.itemFluxedPearl);
    public static final ItemStack MATCH_OFF = new ItemStack(Items.ENDER_PEARL);

    public static final ItemStack ARMOR_ON = new ItemStack(Items.DIAMOND_CHESTPLATE);
    public static final ItemStack ARMOR_OFF = new ItemStack(Items.CHAINMAIL_CHESTPLATE);

    private final ContainerFilterAugment container;

    private TabContainedButton tabWhitelist;
    //private ElementDynamicContainedButton btnWhitelist;

    private ElementDynamicContainedButton btnFilterArmor;
    private ElementDynamicContainedButton btnIgnoreMeta;
    private ElementDynamicContainedButton btnIgnoreNBT;
    private ElementDynamicContainedButton btnUseOre;
    private ElementDynamicContainedButton btnVoiding;
    private ElementDynamicContainedButton btnMatchMod;

    public GuiFilterAugment(ContainerFilterAugment container) {
        super(container);
        this.container = container;
        name = container.getItemStack().getDisplayName();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        container.sendUpdateIfChanged();
    }

    @Override
    public void initGui() {
        super.initGui();

        tabWhitelist = new TabContainedButton(this, "Whitelist", TabBase.LEFT, new ItemStack(Items.PAPER));
        //btnWhitelist = new ElementDynamicContainedButton(this, "Whitelist", xSize - 49, 21, 18, 18, "");

        btnIgnoreMeta = new ElementDynamicContainedButton(this, "IgnoreMeta", xSize - 49, 21, 18, 18, "");
        btnIgnoreNBT = new ElementDynamicContainedButton(this, "IgnoreNBT", xSize - 29, 21, 18, 18, "");

        btnFilterArmor = new ElementDynamicContainedButton(this, "FilterArmor", xSize - 49, 41, 18, 18, "");
        btnUseOre = new ElementDynamicContainedButton(this, "UseOre", xSize - 29, 41, 18, 18, "");

        btnVoiding = new ElementDynamicContainedButton(this, "Voiding", xSize - 49, 61, 18, 18, "");
        btnMatchMod = new ElementDynamicContainedButton(this, "MatchMod", xSize - 29, 61, 18, 18, "");

        btnIgnoreMeta
                .setToolTipExtra("btn." + WirelessUtils.MODID + ".ignore_meta.info")
                .setToolTipLocalized(true);

        btnIgnoreNBT
                .setToolTipExtra("btn." + WirelessUtils.MODID + ".ignore_nbt.info")
                .setToolTipLocalized(true);

        btnUseOre
                .setToolTipExtra("btn." + WirelessUtils.MODID + ".use_ore.info")
                .setToolTipLocalized(true);

        btnVoiding
                .setToolTipExtra("btn." + WirelessUtils.MODID + ".voiding.info")
                .setToolTipLocalized(true);

        btnMatchMod
                .setToolTipExtra("btn." + WirelessUtils.MODID + ".match_mod.info")
                .setToolTipLocalized(true);

        btnFilterArmor
                .setToolTipExtra("btn." + WirelessUtils.MODID + ".filter_armor.info")
                .setToolTipLocalized(true);

        if ( container.canWhitelist() )
            addTab(tabWhitelist);

        if ( container.canIgnoreMetadata() )
            addElement(btnIgnoreMeta);

        if ( container.canIgnoreNBT() )
            addElement(btnIgnoreNBT);

        if ( container.canOreDict() )
            addElement(btnUseOre);

        if ( container.canVoid() )
            addElement(btnVoiding);

        if ( container.canMatchMod() )
            addElement(btnMatchMod);

        if ( container.canFilterArmor() )
            addElement(btnFilterArmor);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTick, x, y);

        drawFilterSlots();
    }

    protected void drawFilterSlots() {
        bindTexture(TEXTURE);

        float colorR = (backgroundColor >> 16 & 255) / 255.0F;
        float colorG = (backgroundColor >> 8 & 255) / 255.0F;
        float colorB = (backgroundColor & 255) / 255.0F;

        GlStateManager.color(colorR, colorG, colorB, 1F);
        for (Slot slot : inventorySlots.inventorySlots)
            if ( slot instanceof SlotFilter ) {
                drawSizedTexturedModalRect(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 29, 93, 18, 18, 256, 256);
            }

        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        switch (buttonName) {
            case "IgnoreMeta":
                container.setIgnoreMetadata(!container.getIgnoreMetadata());
                break;
            case "IgnoreNBT":
                container.setIgnoreNBT(!container.getIgnoreNBT());
                break;
            case "UseOre":
                container.setUseOreDict(!container.getUseOreDict());
                break;
            case "Whitelist":
                container.setWhitelist(!container.isWhitelist());
                break;
            case "Voiding":
                container.setVoiding(!container.isVoiding());
                break;
            case "MatchMod":
                container.setMatchMod(!container.getMatchMod());
                break;
            case "FilterArmor":
                container.setFilterArmor(!container.getFilterArmor());
                break;
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        BaseGuiContainer.playClickSound(1F);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        boolean locked = container.isLocked();
        boolean ignoreMetadata = container.getIgnoreMetadata();
        boolean ignoreNBT = container.getIgnoreNBT();
        boolean useOre = container.getUseOreDict();
        boolean whitelist = container.isWhitelist();
        boolean voiding = container.isVoiding();
        boolean matchMod = container.getMatchMod();
        boolean filterArmor = container.getFilterArmor();

        btnIgnoreMeta.setEnabled(!locked && !matchMod);
        btnIgnoreNBT.setEnabled(!locked && !matchMod);
        btnUseOre.setEnabled(!locked && !matchMod);
        btnFilterArmor.setEnabled(!locked && !matchMod);

        btnMatchMod.setEnabled(!locked);
        btnVoiding.setEnabled(!locked);
        tabWhitelist.setEnabled(!locked);

        btnIgnoreMeta.setItem(ignoreMetadata ? DAMAGE_OFF : DAMAGE_ON);
        btnIgnoreNBT.setItem(ignoreNBT ? NBT_OFF : NBT_ON);
        btnUseOre.setItem(useOre ? ORE_ON : ORE_OFF);
        btnFilterArmor.setItem(filterArmor ? ARMOR_ON : ARMOR_OFF);

        if ( matchMod ) {
            String msg = new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".match_mod.disabled"
            ).setStyle(TextHelpers.RED).getFormattedText();

            btnIgnoreNBT.setToolTip(msg);
            btnIgnoreMeta.setToolTip(msg);
            btnUseOre.setToolTip(msg);
            btnFilterArmor.setToolTip(msg);

        } else {
            btnIgnoreMeta.setToolTip(new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".ignore_meta",
                    getMode(ignoreMetadata)
            ).getFormattedText());

            btnIgnoreNBT.setToolTip(new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".ignore_nbt",
                    getMode(ignoreNBT)
            ).getFormattedText());

            btnUseOre.setToolTip(new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".use_ore",
                    getMode(useOre)
            ).getFormattedText());

            btnFilterArmor.setToolTip(new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".filter_armor",
                    getMode(filterArmor)
            ).getFormattedText());
        }

        setBackgroundColor(whitelist ? 0xFFFFFFFF : 0xFF333333);
        setTextColor(whitelist ? 0x404040 : 0xCCCCCC);
        tabWhitelist.setBackgroundColor(whitelist ? 0xFFFFFFFF : 0xFF333333);
        tabWhitelist.setForegroundColor(whitelist ? 0x404040 : 0xCCCCCC);

        //tabWhitelist.setItem(whitelist ? WHITELIST : BLACKLIST);
        tabWhitelist.setToolTip(new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + "." + (whitelist ? "whitelist" : "blacklist")
        ).getFormattedText());

        btnVoiding.setItem(voiding ? VOID_ON : VOID_OFF);
        btnVoiding.setToolTip(new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + ".voiding",
                getMode(voiding)
        ).getFormattedText());

        btnMatchMod.setItem(matchMod ? MATCH_ON : MATCH_OFF);
        btnMatchMod.setToolTip(new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + ".match_mod",
                getMode(matchMod)
        ).getFormattedText());
    }

    private String getMode(boolean mode) {
        return StringHelper.localize("btn." + WirelessUtils.MODID + ".mode." + (mode ? 2 : 1));
    }

    @Override
    public List<String> getItemToolTip(ItemStack stack) {
        List<String> list = super.getItemToolTip(stack);

        Slot slot = getSlotUnderMouse();
        if ( slot instanceof SlotFilter ) {
            if ( container.getUseOreDict() && !container.getMatchMod() ) {
                int[] ores = OreDictionary.getOreIDs(stack);
                if ( ores != null && ores.length > 0 ) {
                    list.add(new TextComponentTranslation(
                            "btn." + WirelessUtils.MODID + ".use_ore.list"
                    ).setStyle(TextHelpers.GRAY).getFormattedText());
                    for (int oreId : ores) {
                        list.add(new TextComponentTranslation(
                                "item." + WirelessUtils.MODID + ".filter_augment.entry",
                                new TextComponentString(OreDictionary.getOreName(oreId)).setStyle(TextHelpers.WHITE)
                        ).setStyle(TextHelpers.GRAY).getFormattedText());
                    }
                }
            }
        }

        return list;
    }
}
