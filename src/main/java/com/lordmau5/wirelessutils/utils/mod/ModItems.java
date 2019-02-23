package com.lordmau5.wirelessutils.utils.mod;

import cofh.api.util.ThermalExpansionHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.*;
import com.lordmau5.wirelessutils.item.augment.*;
import com.lordmau5.wirelessutils.item.pearl.*;
import com.lordmau5.wirelessutils.item.upgrade.ItemConversionUpgrade;
import com.lordmau5.wirelessutils.item.upgrade.ItemLevelUpgrade;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {

    public final static ItemArmor.ArmorMaterial GLASSES_MATERIAL = EnumHelper.addArmorMaterial(WirelessUtils.MODID + ":glasses", new ResourceLocation(WirelessUtils.MODID, "glasses").toString(), 10, new int[]{1, 3, 2, 1}, 20, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F);

    @GameRegistry.ObjectHolder("wirelessutils:positional_card")
    public static ItemAbsolutePositionalCard itemAbsolutePositionalCard;

    @GameRegistry.ObjectHolder("wirelessutils:relative_positional_card")
    public static ItemRelativePositionalCard itemRelativePositionalCard;

    @GameRegistry.ObjectHolder("wirelessutils:player_positional_card")
    public static ItemPlayerPositionalCard itemPlayerPositionalCard;

    @GameRegistry.ObjectHolder("wirelessutils:fluxed_pearl")
    public static ItemFluxedPearl itemFluxedPearl;

    @GameRegistry.ObjectHolder("wirelessutils:charged_pearl")
    public static ItemChargedPearl itemChargedPearl;

    @GameRegistry.ObjectHolder("wirelessutils:quenched_pearl")
    public static ItemQuenchedPearl itemQuenchedPearl;

    @GameRegistry.ObjectHolder("wirelessutils:scorched_pearl")
    public static ItemScorchedPearl itemScorchedPearl;

    @GameRegistry.ObjectHolder("wirelessutils:stabilized_ender_pearl")
    public static ItemStabilizedEnderPearl itemStabilizedEnderPearl;

    @GameRegistry.ObjectHolder("wirelessutils:base_augment")
    public static ItemBaseAugment itemBaseAugment;

    @GameRegistry.ObjectHolder("wirelessutils:range_augment")
    public static ItemRangeAugment itemRangeAugment;

    @GameRegistry.ObjectHolder("wirelessutils:slot_augment")
    public static ItemSlotAugment itemSlotAugment;

    @GameRegistry.ObjectHolder("wirelessutils:inventory_augment")
    public static ItemInventoryAugment itemInventoryAugment;

    @GameRegistry.ObjectHolder("wirelessutils:capacity_augment")
    public static ItemCapacityAugment itemCapacityAugment;

    @GameRegistry.ObjectHolder("wirelessutils:transfer_augment")
    public static ItemTransferAugment itemTransferAugment;

    @GameRegistry.ObjectHolder("wirelessutils:glasses")
    public static ItemGlasses itemGlasses;

    @GameRegistry.ObjectHolder("wirelessutils:ender_coil")
    public static ItemEnderCoil itemEnderCoil;

    @GameRegistry.ObjectHolder("wirelessutils:level_upgrade")
    public static ItemLevelUpgrade itemLevelUpgrade;

    @GameRegistry.ObjectHolder("wirelessutils:conversion_upgrade")
    public static ItemConversionUpgrade itemConversionUpgrade;

    @GameRegistry.ObjectHolder("wirelessutils:machine_panel")
    public static ItemMachinePanel itemMachinePanel;

    @GameRegistry.ObjectHolder("wirelessutils:world_augment")
    public static ItemWorldAugment itemWorldAugment;

    @GameRegistry.ObjectHolder("wirelessutils:block_augment")
    public static ItemBlockAugment itemBlockAugment;

    @GameRegistry.ObjectHolder("wirelessutils:invert_augment")
    public static ItemInvertAugment itemInvertAugment;

    @GameRegistry.ObjectHolder("wirelessutils:crop_augment")
    public static ItemCropAugment itemCropAugment;

    @GameRegistry.ObjectHolder("wirelessutils:chunk_load_augment")
    public static ItemChunkLoadAugment itemChunkLoadAugment;

    @GameRegistry.ObjectHolder("wirelessutils:fluid_gen_augment")
    public static ItemFluidGenAugment itemFluidGenAugment;

    @GameRegistry.ObjectHolder("wirelessutils:sided_transfer_augment")
    public static ItemSidedTransferAugment itemSidedTransferAugment;

    public static void initLootTables() {
        LootTableList.register(new ResourceLocation(WirelessUtils.MODID, "charged_pearl_drops"));
    }

    public static void initRecipes() {
        int cost = ModConfig.items.fluxedPearl.chargeEnergy;

        // Charged Pearls

        ChargerRecipeManager.addRecipe(new ItemStack(itemFluxedPearl, 1, 0), new ItemStack(itemChargedPearl, 1, 0), cost);
        ChargerRecipeManager.addRecipe(new ItemStack(itemFluxedPearl, 1, 1), new ItemStack(itemChargedPearl, 1, 1), cost);

        ThermalExpansionHelper.addChargerRecipe(cost, new ItemStack(itemFluxedPearl, 1, 0), new ItemStack(itemChargedPearl, 1, 0));
        ThermalExpansionHelper.addChargerRecipe(cost, new ItemStack(itemFluxedPearl, 1, 1), new ItemStack(itemChargedPearl, 1, 1));

        // Quenched Pearls

        FluidStack water = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);

        CondenserRecipeManager.addRecipe(water, new ItemStack(itemChargedPearl, 1, 0), new ItemStack(itemQuenchedPearl, 1, 0), 400);
        CondenserRecipeManager.addRecipe(water, new ItemStack(itemChargedPearl, 1, 1), new ItemStack(itemQuenchedPearl, 1, 1), 400);

        ThermalExpansionHelper.addTransposerFill(400, new ItemStack(itemChargedPearl, 1, 0), new ItemStack(itemQuenchedPearl, 1, 0), water, false);
        ThermalExpansionHelper.addTransposerFill(400, new ItemStack(itemChargedPearl, 1, 1), new ItemStack(itemQuenchedPearl, 1, 1), water, false);

        // Scorched Pearls

        FluidStack lava = new FluidStack(FluidRegistry.LAVA, Fluid.BUCKET_VOLUME);

        CondenserRecipeManager.addRecipe(lava, new ItemStack(itemChargedPearl, 1, 0), new ItemStack(itemScorchedPearl, 1, 0), 400);
        CondenserRecipeManager.addRecipe(lava, new ItemStack(itemChargedPearl, 1, 1), new ItemStack(itemScorchedPearl, 1, 1), 400);

        ThermalExpansionHelper.addTransposerFill(400, new ItemStack(itemChargedPearl, 1, 0), new ItemStack(itemScorchedPearl, 1, 0), lava, false);
        ThermalExpansionHelper.addTransposerFill(400, new ItemStack(itemChargedPearl, 1, 1), new ItemStack(itemScorchedPearl, 1, 1), lava, false);

        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        ItemStack xp_bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);

        FluidStack xpjuice = FluidRegistry.getFluidStack("xpjuice", 250);
        if ( xpjuice != null )
            CondenserRecipeManager.addRecipe(xpjuice, bottle, xp_bottle, 400);

        xpjuice = FluidRegistry.getFluidStack("essence", 250);
        if ( xpjuice != null )
            CondenserRecipeManager.addRecipe(xpjuice, bottle, xp_bottle, 400);

        xpjuice = FluidRegistry.getFluidStack("experience", 250);
        if ( xpjuice != null )
            CondenserRecipeManager.addRecipe(xpjuice, bottle, xp_bottle, 400);

        // Concrete
        for (int i = 0; i < 16; i++)
            CondenserRecipeManager.addRecipe(water, new ItemStack(Blocks.CONCRETE_POWDER, 1, i), new ItemStack(Blocks.CONCRETE, 1, i), 400);

        // Sponge
        CondenserRecipeManager.addRecipe(water, new ItemStack(Blocks.SPONGE, 1, 0), new ItemStack(Blocks.SPONGE, 1, 1), 400);

    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        itemAbsolutePositionalCard.initModel();
        itemRelativePositionalCard.initModel();
        itemFluxedPearl.initModel();
        itemChargedPearl.initModel();
        itemQuenchedPearl.initModel();
        itemScorchedPearl.initModel();
        itemStabilizedEnderPearl.initModel();
        itemRangeAugment.initModel();
        itemSlotAugment.initModel();
        itemInventoryAugment.initModel();
        itemBaseAugment.initModel();
        itemCapacityAugment.initModel();
        itemTransferAugment.initModel();
        itemGlasses.initModel();
        itemEnderCoil.initModel();
        itemLevelUpgrade.initModel();
        itemConversionUpgrade.initModel();
        itemMachinePanel.initModel();
        itemWorldAugment.initModel();
        itemInvertAugment.initModel();
        itemBlockAugment.initModel();
        itemCropAugment.initModel();
        itemChunkLoadAugment.initModel();
        itemFluidGenAugment.initModel();
        itemPlayerPositionalCard.initModel();
        itemSidedTransferAugment.initModel();
    }

    @SideOnly(Side.CLIENT)
    public static void initColors(ItemColors itemColors) {
        itemColors.registerItemColorHandler(ColorHandler.LevelUpgrade.handleItemColor, itemLevelUpgrade);
        itemColors.registerItemColorHandler(ColorHandler.LevelUpgrade.handleItemColor, itemConversionUpgrade);
        itemColors.registerItemColorHandler(ColorHandler.Augment.Range.handleItemColor, itemRangeAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemSlotAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemCapacityAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemTransferAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemWorldAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemInvertAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.FluidGen.handleItemColor, itemFluidGenAugment);

        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockDirectionalCharger));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockPositionalCharger));
        //itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockChunkCharger));

        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockDirectionalCondenser));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockPositionalCondenser));

        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockDirectionalDesublimator));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockPositionalDesublimator));
    }
}
