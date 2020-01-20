package com.lordmau5.wirelessutils.utils.mod;

import cofh.api.util.ThermalExpansionHelper;
import cofh.core.util.helpers.RecipeHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.item.ItemEnderCoil;
import com.lordmau5.wirelessutils.item.ItemGlasses;
import com.lordmau5.wirelessutils.item.ItemMachinePanel;
import com.lordmau5.wirelessutils.item.augment.ItemBaseAugment;
import com.lordmau5.wirelessutils.item.augment.ItemBlockAugment;
import com.lordmau5.wirelessutils.item.augment.ItemCapacityAugment;
import com.lordmau5.wirelessutils.item.augment.ItemChunkLoadAugment;
import com.lordmau5.wirelessutils.item.augment.ItemCropAugment;
import com.lordmau5.wirelessutils.item.augment.ItemDispenserAugment;
import com.lordmau5.wirelessutils.item.augment.ItemFacingAugment;
import com.lordmau5.wirelessutils.item.augment.ItemFilterAugment;
import com.lordmau5.wirelessutils.item.augment.ItemFluidGenAugment;
import com.lordmau5.wirelessutils.item.augment.ItemInventoryAugment;
import com.lordmau5.wirelessutils.item.augment.ItemInvertAugment;
import com.lordmau5.wirelessutils.item.augment.ItemRangeAugment;
import com.lordmau5.wirelessutils.item.augment.ItemSidedTransferAugment;
import com.lordmau5.wirelessutils.item.augment.ItemSlotAugment;
import com.lordmau5.wirelessutils.item.augment.ItemTransferAugment;
import com.lordmau5.wirelessutils.item.augment.ItemWorldAugment;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.item.cards.ItemAbsoluteAreaCard;
import com.lordmau5.wirelessutils.item.cards.ItemAbsolutePositionalCard;
import com.lordmau5.wirelessutils.item.cards.ItemPlayerPositionalCard;
import com.lordmau5.wirelessutils.item.cards.ItemRelativeAreaCard;
import com.lordmau5.wirelessutils.item.cards.ItemRelativePositionalCard;
import com.lordmau5.wirelessutils.item.module.ItemBaseModule;
import com.lordmau5.wirelessutils.item.module.ItemCaptureModule;
import com.lordmau5.wirelessutils.item.module.ItemCloneModule;
import com.lordmau5.wirelessutils.item.module.ItemFishingModule;
import com.lordmau5.wirelessutils.item.module.ItemLaunchModule;
import com.lordmau5.wirelessutils.item.module.ItemSlaughterModule;
import com.lordmau5.wirelessutils.item.module.ItemTeleportModule;
import com.lordmau5.wirelessutils.item.module.ItemTheoreticalSlaughterModule;
import com.lordmau5.wirelessutils.item.pearl.ItemChargedPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemCrystallizedVoidPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemFluxedPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemQuenchedPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemScorchedPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemStabilizedEnderPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemVoidPearl;
import com.lordmau5.wirelessutils.item.upgrade.ItemConversionUpgrade;
import com.lordmau5.wirelessutils.item.upgrade.ItemLevelUpgrade;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.crafting.CopyNBTShapelessRecipeFactory;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ModItems {

    public final static ItemArmor.ArmorMaterial GLASSES_MATERIAL = EnumHelper.addArmorMaterial(WirelessUtils.MODID + ":glasses", new ResourceLocation(WirelessUtils.MODID, "glasses").toString(), 10, new int[]{1, 3, 2, 1}, 20, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0F);

    @GameRegistry.ObjectHolder("wirelessutils:positional_card")
    public static ItemAbsolutePositionalCard itemAbsolutePositionalCard;

    @GameRegistry.ObjectHolder("wirelessutils:relative_positional_card")
    public static ItemRelativePositionalCard itemRelativePositionalCard;

    @GameRegistry.ObjectHolder("wirelessutils:player_positional_card")
    public static ItemPlayerPositionalCard itemPlayerPositionalCard;

    @GameRegistry.ObjectHolder("wirelessutils:area_card")
    public static ItemAbsoluteAreaCard itemAbsoluteAreaCard;

    @GameRegistry.ObjectHolder("wirelessutils:relative_area_card")
    public static ItemRelativeAreaCard itemRelativeAreaCard;

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

    @GameRegistry.ObjectHolder("wirelessutils:void_pearl")
    public static ItemVoidPearl itemVoidPearl;

    @GameRegistry.ObjectHolder("wirelessutils:crystallized_void_pearl")
    public static ItemCrystallizedVoidPearl itemCrystallizedVoidPearl;

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

    @GameRegistry.ObjectHolder("wirelessutils:dispenser_augment")
    public static ItemDispenserAugment itemDispenserAugment;

    @GameRegistry.ObjectHolder("wirelessutils:filter_augment")
    public static ItemFilterAugment itemFilterAugment;

    @GameRegistry.ObjectHolder("wirelessutils:facing_augment")
    public static ItemFacingAugment itemFacingAugment;

    @GameRegistry.ObjectHolder("wirelessutils:base_module")
    public static ItemBaseModule itemBaseModule;

    @GameRegistry.ObjectHolder("wirelessutils:slaughter_module")
    public static ItemSlaughterModule itemSlaughterModule;

    @GameRegistry.ObjectHolder("wirelessutils:theoretical_slaughter_module")
    public static ItemTheoreticalSlaughterModule itemTheoreticalSlaughterModule;

    @GameRegistry.ObjectHolder("wirelessutils:teleport_module")
    public static ItemTeleportModule itemTeleportModule;

    @GameRegistry.ObjectHolder("wirelessutils:capture_module")
    public static ItemCaptureModule itemCaptureModule;

    @GameRegistry.ObjectHolder("wirelessutils:clone_module")
    public static ItemCloneModule itemCloneModule;

    @GameRegistry.ObjectHolder("wirelessutils:launch_module")
    public static ItemLaunchModule itemLaunchModule;

    @GameRegistry.ObjectHolder("wirelessutils:fishing_module")
    public static ItemFishingModule itemFishingModule;

    public static void initLootTables() {
        LootTableList.register(new ResourceLocation(WirelessUtils.MODID, "charged_pearl_drops"));
    }

    public static void initRecipes(RegistryEvent.Register<IRecipe> event) {
        // Upgrades for Machines
        if ( ModConfig.upgrades.enableCrafting )
            generateUpgradeRecipes(event);

        // Convenience Recipes for Positional Cards
        generateCardRecipes(event);

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

    public static void generateCardRecipes(RegistryEvent.Register<IRecipe> event) {
        for (Item item : CommonProxy.ITEMS) {
            if ( item instanceof ItemBasePositionalCard ) {
                final ResourceLocation registryName = item.getRegistryName();
                if ( registryName == null )
                    continue;

                NonNullList<ItemStack> stacks = NonNullList.create();
                if ( item.getCreativeTab() == null || !item.getHasSubtypes() )
                    stacks.add(new ItemStack(item));
                else
                    item.getSubItems(item.getCreativeTab(), stacks);

                final ResourceLocation group = new ResourceLocation(WirelessUtils.MODID, "card_convenience");

                for (ItemStack stack : stacks) {
                    final String location = registryName.getPath() + "_" + stack.getMetadata();

                    // Clear Item
                    GameRegistry.addShapelessRecipe(
                            new ResourceLocation(WirelessUtils.MODID, location + "_clear"),
                            group,
                            stack,
                            CraftingHelper.getIngredient(stack)
                    );

                    // Copy NBT
                    ItemStack copyOut = stack.copy();
                    copyOut.setCount(2);
                    ShapelessOreRecipe recipe = new CopyNBTShapelessRecipeFactory.CopyNBTShapelessRecipe(group, RecipeHelper.buildInput(new Object[]{stack, stack}), copyOut);
                    recipe.setRegistryName(new ResourceLocation(WirelessUtils.MODID, location + "_clone"));
                    event.getRegistry().register(recipe);
                }
            }
        }
    }

    public static void generateUpgradeRecipes(RegistryEvent.Register<IRecipe> event) {
        Level[] levels = Level.values();
        // We need at least 2 levels to be able to upgrade things.
        if ( levels.length <= 1 )
            return;

        for (Block block : CommonProxy.BLOCKS) {
            if ( !(block instanceof BlockBaseMachine) )
                continue;

            ResourceLocation baseLocation = block.getRegistryName();
            if ( baseLocation == null )
                continue;

            for (int i = 1; i < levels.length; i++) {
                // Upgrade Kit
                ResourceLocation upgradeLocation = new ResourceLocation(WirelessUtils.MODID, baseLocation.getPath() + "_upgrade_kit_" + i);
                ShapelessOreRecipe upgradeRecipe = new CopyNBTShapelessRecipeFactory.CopyNBTShapelessRecipe(upgradeLocation, RecipeHelper.buildInput(new Object[]{
                        new ItemStack(itemLevelUpgrade, 1, i),
                        new ItemStack(block, 1, i - 1)
                }), new ItemStack(block, 1, i));

                upgradeRecipe.setRegistryName(upgradeLocation);
                event.getRegistry().register(upgradeRecipe);

                // Conversion kits don't exist until the third level.
                if ( i == 1 )
                    continue;

                // Conversion Kit
                ItemStack[] stacks = new ItemStack[i];
                for (int j = 0; j < stacks.length; j++) {
                    stacks[j] = new ItemStack(block, 1, j);
                }

                ResourceLocation conversionLocation = new ResourceLocation(WirelessUtils.MODID, baseLocation.getPath() + "_conversion_kit_" + i);
                ShapelessOreRecipe conversionRecipe = new CopyNBTShapelessRecipeFactory.CopyNBTShapelessRecipe(conversionLocation, RecipeHelper.buildInput(new Object[]{
                        new ItemStack(itemConversionUpgrade, 1, i),
                        Ingredient.fromStacks(stacks)
                }), new ItemStack(block, 1, i));

                conversionRecipe.setRegistryName(conversionLocation);
                event.getRegistry().register(conversionRecipe);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        itemFluxedPearl.initModel();
        itemChargedPearl.initModel();
        itemQuenchedPearl.initModel();
        itemScorchedPearl.initModel();
        itemStabilizedEnderPearl.initModel();
        itemVoidPearl.initModel();
        itemCrystallizedVoidPearl.initModel();

        itemAbsolutePositionalCard.initModel();
        itemRelativePositionalCard.initModel();
        itemPlayerPositionalCard.initModel();
        itemAbsoluteAreaCard.initModel();
        itemRelativeAreaCard.initModel();

        itemGlasses.initModel();
        itemEnderCoil.initModel();
        itemMachinePanel.initModel();

        itemLevelUpgrade.initModel();
        itemConversionUpgrade.initModel();

        itemBaseAugment.initModel();
        itemRangeAugment.initModel();
        itemSlotAugment.initModel();
        itemInventoryAugment.initModel();
        itemCapacityAugment.initModel();
        itemTransferAugment.initModel();
        itemWorldAugment.initModel();
        itemInvertAugment.initModel();
        itemBlockAugment.initModel();
        itemCropAugment.initModel();
        itemChunkLoadAugment.initModel();
        itemFluidGenAugment.initModel();
        itemFacingAugment.initModel();
        itemSidedTransferAugment.initModel();
        itemDispenserAugment.initModel();
        itemFilterAugment.initModel();

        itemBaseModule.initModel();
        itemSlaughterModule.initModel();
        itemTeleportModule.initModel();
        itemCaptureModule.initModel();
        itemCloneModule.initModel();
        itemLaunchModule.initModel();
        itemTheoreticalSlaughterModule.initModel();
        itemFishingModule.initModel();
    }

    @SideOnly(Side.CLIENT)
    public static void initColors(ItemColors itemColors) {
        itemColors.registerItemColorHandler(ColorHandler.LevelUpgrade.handleItemColor, itemLevelUpgrade);
        itemColors.registerItemColorHandler(ColorHandler.LevelUpgrade.handleItemColor, itemConversionUpgrade);

        itemColors.registerItemColorHandler(ColorHandler.AreaCard.handleItemColor, itemAbsoluteAreaCard);
        itemColors.registerItemColorHandler(ColorHandler.AreaCard.handleItemColor, itemRelativeAreaCard);

        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemBaseAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.Range.handleItemColor, itemRangeAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemSlotAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemInventoryAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemCapacityAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemTransferAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemWorldAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemInvertAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemBlockAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemCropAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemChunkLoadAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.FluidGen.handleItemColor, itemFluidGenAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemFacingAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemSidedTransferAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.handleItemColor, itemDispenserAugment);
        itemColors.registerItemColorHandler(ColorHandler.Augment.Filter.handleItemColor, itemFilterAugment);

        itemColors.registerItemColorHandler(ColorHandler.VoidPearl.handleItemColor, itemVoidPearl);
        itemColors.registerItemColorHandler(ColorHandler.VoidPearl.handleItemColor, itemCrystallizedVoidPearl);

        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockDirectionalCharger));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockPositionalCharger));
        //itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockChunkCharger));

        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockDirectionalCondenser));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockPositionalCondenser));

        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockDirectionalDesublimator));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockPositionalDesublimator));

        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockDirectionalVaporizer));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(ModBlocks.blockPositionalVaporizer));
    }
}
