package com.lordmau5.wirelessutils.plugins.RefinedStorage;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.ItemBlockMachine;
import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.plugins.RefinedStorage.augment.ItemRSBusAugment;
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
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import com.lordmau5.wirelessutils.utils.Level;
import com.raoulvdberge.refinedstorage.api.IRSAPI;
import com.raoulvdberge.refinedstorage.api.RSAPIInject;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import com.raoulvdberge.refinedstorage.apiimpl.network.node.NetworkNode;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RefinedStoragePlugin implements IPlugin {
    @RSAPIInject
    public static IRSAPI RSAPI;

    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(INetworkNodeProxy.class)
    public static Capability<INetworkNodeProxy> NETWORK_NODE_PROXY_CAPABILITY = null;

    public static ItemRSBusAugment itemRSBusAugment;

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

        CommonProxy.registerBlock(event, blockInfiniteWirelessTransmitter);
        CommonProxy.registerBlock(event, blockDirectionalRSNetwork);
        CommonProxy.registerBlock(event, blockPositionalRSNetwork);

        GameRegistry.registerTileEntity(TileInfiniteWirelessTransmitter.class, new ResourceLocation(WirelessUtils.MODID, "infinite_wireless_transmitter"));

        CommonProxy.registerMachineTile(TileDirectionalRSNetwork.class);
        CommonProxy.registerMachineTile(TilePositionalRSNetwork.class);
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        itemRSBusAugment = new ItemRSBusAugment();

        CommonProxy.registerItem(event, itemRSBusAugment);

        CommonProxy.registerItem(event, new ItemBlock(blockInfiniteWirelessTransmitter).setRegistryName(blockInfiniteWirelessTransmitter.getRegistryName()));
        CommonProxy.registerItem(event, new ItemBlockMachine(blockDirectionalRSNetwork).setRegistryName(blockDirectionalRSNetwork.getRegistryName()));
        CommonProxy.registerItem(event, new ItemBlockMachine(blockPositionalRSNetwork).setRegistryName(blockPositionalRSNetwork.getRegistryName()));
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        blockInfiniteWirelessTransmitter.initModel();
        blockDirectionalRSNetwork.initModel();
        blockPositionalRSNetwork.initModel();

        itemRSBusAugment.initModel();
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
        itemColors.registerItemColorHandler(handleRSBusColor, itemRSBusAugment);
    }

    @SideOnly(Side.CLIENT)
    public static final IItemColor handleRSBusColor = (ItemStack stack, int tintIndex) -> {
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
}
