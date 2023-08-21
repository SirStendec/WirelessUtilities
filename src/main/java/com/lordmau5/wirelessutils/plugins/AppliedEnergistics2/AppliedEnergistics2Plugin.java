package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2;

import appeng.api.util.AEColor;
import com.lordmau5.wirelessutils.item.ItemBlockMachine;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.augment.ItemAEBusAugment;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.directional.BlockDirectionalAENetwork;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional.BlockPositionalAENetwork;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.tile.TileAENetworkBase;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.tile.TileDirectionalAENetwork;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.tile.TilePositionalAENetwork;
import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class AppliedEnergistics2Plugin implements IPlugin {
    public static BlockDirectionalAENetwork blockDirectionalAENetwork;
    public static BlockPositionalAENetwork blockPositionalAENetwork;
    public static ItemAEBusAugment itemAEBusAugment;

    @GameRegistry.ObjectHolder("appliedenergistics2:quartz_ore")
    public static Item itemQuartzOre;

    @GameRegistry.ObjectHolder("appliedenergistics2:charged_quartz_ore")
    public static Item itemChargedQuartzOre;

    @GameRegistry.ObjectHolder("appliedenergistics2:material")
    public static Item itemMaterial;

    @GameRegistry.ObjectHolder("appliedenergistics2:part")
    public static Item itemPart;

    @Override
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        blockDirectionalAENetwork = new BlockDirectionalAENetwork();
        blockPositionalAENetwork = new BlockPositionalAENetwork();

        CommonProxy.registerBlock(event, blockDirectionalAENetwork);
        CommonProxy.registerBlock(event, blockPositionalAENetwork);

        CommonProxy.registerMachineTile(TileDirectionalAENetwork.class);
        CommonProxy.registerMachineTile(TilePositionalAENetwork.class);
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        itemAEBusAugment = new ItemAEBusAugment();

        CommonProxy.registerItem(event, itemAEBusAugment);

        CommonProxy.registerItem(event, new ItemBlockMachine(blockDirectionalAENetwork).setRegistryName(blockDirectionalAENetwork.getRegistryName()));
        CommonProxy.registerItem(event, new ItemBlockMachine(blockPositionalAENetwork).setRegistryName(blockPositionalAENetwork.getRegistryName()));
    }

    @Override
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if ( itemQuartzOre != null && itemChargedQuartzOre != null )
            ChargerRecipeManager.addRecipe(itemQuartzOre, itemChargedQuartzOre, 8000);

        if ( itemMaterial != null )
            ChargerRecipeManager.addRecipe(new ItemStack(itemMaterial, 1, 0), new ItemStack(itemMaterial, 1, 1), 8000);
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        blockDirectionalAENetwork.initModel();
        blockPositionalAENetwork.initModel();

        itemAEBusAugment.initModel();
    }

    @Override
    public void initColors(BlockColors blockColors) {
        blockColors.registerBlockColorHandler(Client.handleBlockColor, blockDirectionalAENetwork);
        blockColors.registerBlockColorHandler(Client.handleBlockColor, blockPositionalAENetwork);
    }

    @Override
    public void initColors(ItemColors itemColors) {
        itemColors.registerItemColorHandler(Client.handleItemColor, Item.getItemFromBlock(blockDirectionalAENetwork));
        itemColors.registerItemColorHandler(Client.handleItemColor, Item.getItemFromBlock(blockPositionalAENetwork));
        itemColors.registerItemColorHandler(Client.handleAEBusColor, itemAEBusAugment);
    }

    static class Client {
        @SideOnly(Side.CLIENT)
        public static final IBlockColor handleBlockColor = (IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) -> {
            if ( worldIn != null && pos != null ) {
                TileEntity tile = worldIn.getTileEntity(pos);
                if ( tile instanceof TileAENetworkBase ) {
                    TileAENetworkBase base = (TileAENetworkBase) tile;

                    if ( tintIndex == 2 )
                        return base.getLevel().color;

                    if ( tintIndex == 1 ) {
                        AEColor color = base.getAEColor();
                        return color.getVariantByTintIndex(AEColor.TINTINDEX_MEDIUM);
                    }
                }
            }

            return 0xFFFFFF;
        };

        @SideOnly(Side.CLIENT)
        public static final IItemColor handleAEBusColor = (ItemStack stack, int tintIndex) -> {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("WUTint:" + tintIndex, Constants.NBT.TAG_INT) )
                return tag.getInteger("WUTint:" + tintIndex);

            if ( tintIndex == 1 ) {
                Level level = Level.getMinLevel();
                if ( !stack.isEmpty() )
                    level = Level.fromItemStack(stack);

                return level.color;
            }

            return 0xFFFFFF;
        };

        @SideOnly(Side.CLIENT)
        public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
            if ( tintIndex == 2 ) {
                Level level = Level.getMinLevel();
                if ( !stack.isEmpty() )
                    level = Level.fromItemStack(stack);

                return level.color;
            }

            if ( tintIndex == 1 ) {
                AEColor color = AEColor.TRANSPARENT;
                if ( ModConfig.plugins.appliedEnergistics.enableColor )
                    color = AEColorHelpers.fromItemStack(stack);

                return color.getVariantByTintIndex(AEColor.TINTINDEX_MEDIUM);
            }

            return 0xFFFFFF;
        };
    }
}
