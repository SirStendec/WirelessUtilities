package com.lordmau5.wirelessutils.plugins.XNet;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.ItemBlockMachine;
import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.plugins.XNet.network.base.TileXNetNetworkBase;
import com.lordmau5.wirelessutils.plugins.XNet.network.directional.BlockDirectionalXNetNetwork;
import com.lordmau5.wirelessutils.plugins.XNet.network.directional.TileDirectionalXNetNetwork;
import com.lordmau5.wirelessutils.plugins.XNet.network.positional.BlockPositionalXNetNetwork;
import com.lordmau5.wirelessutils.plugins.XNet.network.positional.TilePositionalXNetNetwork;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import mcjty.xnet.api.IXNet;
import mcjty.xnet.api.channels.IConsumerProvider;
import mcjty.xnet.api.keys.NetworkId;
import mcjty.xnet.api.net.IWorldBlob;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class XNetPlugin implements IPlugin {
    public static IXNet XNetAPI;

    public static BlockDirectionalXNetNetwork blockDirectionalXNetNetwork;

    public static BlockPositionalXNetNetwork blockPositionalXNetNetwork;

    @Override
    public void init(FMLInitializationEvent event) {
        FMLInterModComms.sendFunctionMessage("xnet", "getXNet", GetXNet.class.getName());
    }

    public static class GetXNet implements Function<IXNet, Void> {
        @Nullable
        @Override
        public Void apply(IXNet input) {
            XNetAPI = input;
            setup();
            return null;
        }

        private void setup() {
            XNetAPI.registerConsumerProvider(new IConsumerProvider() {
                @Nonnull
                @Override
                public Set<BlockPos> getConsumers(World world, IWorldBlob worldBlob, NetworkId networkId) {
                    if ( world == null || worldBlob == null )
                        return Collections.emptySet();

                    Set<BlockPos> posSet = worldBlob.getConsumers(networkId);
                    Set<BlockPos> toAdd = new HashSet<>();
                    for (BlockPos pos : posSet) {
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            TileEntity tile = world.getTileEntity(pos.offset(facing));
                            if ( tile instanceof TileXNetNetworkBase ) {
                                toAdd.addAll(((TileXNetNetworkBase) tile).getConsumers());
                            }
                        }
                    }

                    posSet.addAll(toAdd);

                    return posSet;
                }
            });
        }
    }

    @Override

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        blockDirectionalXNetNetwork = new BlockDirectionalXNetNetwork();
        blockPositionalXNetNetwork = new BlockPositionalXNetNetwork();

        event.getRegistry().register(blockDirectionalXNetNetwork);
        event.getRegistry().register(blockPositionalXNetNetwork);

        GameRegistry.registerTileEntity(TileDirectionalXNetNetwork.class, new ResourceLocation(WirelessUtils.MODID, "directional_xnet_network"));
        GameRegistry.registerTileEntity(TilePositionalXNetNetwork.class, new ResourceLocation(WirelessUtils.MODID, "positional_xnet_network"));
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlockMachine(blockDirectionalXNetNetwork).setRegistryName(blockDirectionalXNetNetwork.getRegistryName()));
        event.getRegistry().register(new ItemBlockMachine(blockPositionalXNetNetwork).setRegistryName(blockPositionalXNetNetwork.getRegistryName()));
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        blockDirectionalXNetNetwork.initModel();
        blockPositionalXNetNetwork.initModel();
    }

    @Override
    public void initColors(BlockColors blockColors) {
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockDirectionalXNetNetwork);
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockPositionalXNetNetwork);
    }

    @Override
    public void initColors(ItemColors itemColors) {
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(blockDirectionalXNetNetwork));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(blockPositionalXNetNetwork));
    }
}
