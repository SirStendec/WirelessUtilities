package com.lordmau5.wirelessutils.plugins.RefinedStorage;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.ItemBlockMachine;
import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.iwt.BlockInfiniteWirelessTransmitter;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.iwt.NetworkNodeInfiniteWirelessTransmitter;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.iwt.TileInfiniteWirelessTransmitter;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.base.NetworkNodeBase;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.directional.BlockDirectionalRSNetwork;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.directional.NetworkNodeDirectionalRSNetwork;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.directional.TileDirectionalRSNetwork;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.positional.BlockPositionalRSNetwork;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.positional.NetworkNodePositionalRSNetwork;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.network.positional.TilePositionalRSNetwork;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import com.raoulvdberge.refinedstorage.api.IRSAPI;
import com.raoulvdberge.refinedstorage.api.RSAPIInject;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNode;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RefinedStoragePlugin implements IPlugin {
    @RSAPIInject
    public static IRSAPI RSAPI;

    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(INetworkNodeProxy.class)
    public static Capability<INetworkNodeProxy> NETWORK_NODE_PROXY_CAPABILITY = null;

    public static BlockInfiniteWirelessTransmitter blockInfiniteWirelessTransmitter;

    public static BlockDirectionalRSNetwork blockDirectionalRSNetwork;

    public static BlockPositionalRSNetwork blockPositionalRSNetwork;

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        RSAPI.getNetworkNodeRegistry().add(NetworkNodeInfiniteWirelessTransmitter.ID, (tag, world, pos) -> {
            NetworkNode node = new NetworkNodeInfiniteWirelessTransmitter(world, pos);
            node.read(tag);
            return node;
        });

        RSAPI.getNetworkNodeRegistry().add(NetworkNodeDirectionalRSNetwork.ID, (tag, world, pos) -> {
            NetworkNodeBase node = new NetworkNodeDirectionalRSNetwork(world, pos);
            node.read(tag);
            return node;
        });

        RSAPI.getNetworkNodeRegistry().add(NetworkNodePositionalRSNetwork.ID, (tag, world, pos) -> {
            NetworkNodeBase node = new NetworkNodePositionalRSNetwork(world, pos);
            node.read(tag);
            return node;
        });
    }

    @Override
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        blockInfiniteWirelessTransmitter = new BlockInfiniteWirelessTransmitter();
        blockDirectionalRSNetwork = new BlockDirectionalRSNetwork();
        blockPositionalRSNetwork = new BlockPositionalRSNetwork();

        event.getRegistry().register(blockInfiniteWirelessTransmitter);
        event.getRegistry().register(blockDirectionalRSNetwork);
        event.getRegistry().register(blockPositionalRSNetwork);

        GameRegistry.registerTileEntity(TileInfiniteWirelessTransmitter.class, new ResourceLocation(WirelessUtils.MODID, "infinite_wireless_transmitter"));
        GameRegistry.registerTileEntity(TileDirectionalRSNetwork.class, new ResourceLocation(WirelessUtils.MODID, "directional_rs_network"));
        GameRegistry.registerTileEntity(TilePositionalRSNetwork.class, new ResourceLocation(WirelessUtils.MODID, "positional_rs_network"));
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(blockInfiniteWirelessTransmitter).setRegistryName(blockInfiniteWirelessTransmitter.getRegistryName()));
        event.getRegistry().register(new ItemBlockMachine(blockDirectionalRSNetwork).setRegistryName(blockDirectionalRSNetwork.getRegistryName()));
        event.getRegistry().register(new ItemBlockMachine(blockPositionalRSNetwork).setRegistryName(blockPositionalRSNetwork.getRegistryName()));
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        blockInfiniteWirelessTransmitter.initModel();
        blockDirectionalRSNetwork.initModel();
        blockPositionalRSNetwork.initModel();
    }

    @Override
    public void initColors(BlockColors blockColors) {
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockDirectionalRSNetwork);
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockPositionalRSNetwork);
    }

    @Override
    public void initColors(ItemColors itemColors) {
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(blockDirectionalRSNetwork));
        itemColors.registerItemColorHandler(ColorHandler.Machine.handleItemColor, Item.getItemFromBlock(blockPositionalRSNetwork));
    }
}
