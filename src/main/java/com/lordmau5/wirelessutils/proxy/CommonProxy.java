package com.lordmau5.wirelessutils.proxy;

import cofh.core.gui.GuiHandler;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.charger.BlockDirectionalCharger;
import com.lordmau5.wirelessutils.block.charger.BlockPositionalCharger;
import com.lordmau5.wirelessutils.block.condenser.BlockDirectionalCondenser;
import com.lordmau5.wirelessutils.block.condenser.BlockPositionalCondenser;
import com.lordmau5.wirelessutils.block.desublimator.BlockDirectionalDesublimator;
import com.lordmau5.wirelessutils.block.desublimator.BlockPositionalDesublimator;
import com.lordmau5.wirelessutils.block.redstone.BlockPoweredAir;
import com.lordmau5.wirelessutils.block.redstone.BlockPoweredRedstoneWire;
import com.lordmau5.wirelessutils.block.slime.BlockAngledSlime;
import com.lordmau5.wirelessutils.commands.DebugCommand;
import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import com.lordmau5.wirelessutils.entity.pearl.*;
import com.lordmau5.wirelessutils.item.*;
import com.lordmau5.wirelessutils.item.augment.*;
import com.lordmau5.wirelessutils.item.base.IEnhancedItem;
import com.lordmau5.wirelessutils.item.pearl.*;
import com.lordmau5.wirelessutils.item.upgrade.ItemConversionUpgrade;
import com.lordmau5.wirelessutils.item.upgrade.ItemLevelUpgrade;
import com.lordmau5.wirelessutils.plugins.PluginRegistry;
import com.lordmau5.wirelessutils.tile.TileAngledSlime;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.charger.TileEntityDirectionalCharger;
import com.lordmau5.wirelessutils.tile.charger.TileEntityPositionalCharger;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityDirectionalCondenser;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityPositionalCondenser;
import com.lordmau5.wirelessutils.tile.desublimator.TileDirectionalDesublimator;
import com.lordmau5.wirelessutils.tile.desublimator.TilePositionalDesublimator;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class CommonProxy {

    public static List<Class<? extends TileEntity>> MACHINES = new ArrayList<>();

    public void preInit(FMLPreInitializationEvent e) {
        PluginRegistry.preInit(e);
    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(WirelessUtils.instance, new GuiHandler());

        if ( ModItems.itemStabilizedEnderPearl != null )
            OreDictionary.registerOre("enderpearl", ModItems.itemStabilizedEnderPearl);

        EntityFluxedPearl.initReactions();
        ModAdvancements.initTriggers();

        ModItems.initLootTables();
        ModItems.initRecipes();

        PluginRegistry.init(e);
    }

    public void postInit(FMLPostInitializationEvent e) {
        PluginRegistry.postInit(e);
    }

    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new DebugCommand());
    }

    public void handleIdMapping(FMLModIdMappingEvent event) {
        ChargerRecipeManager.refresh();
        CondenserRecipeManager.refresh();
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        EventDispatcher.WORLD_UNLOAD.dispatchEvent(event);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityAdded(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        if ( world.isRemote )
            return;

        final Entity entity = event.getEntity();
        if ( entity instanceof EntityItem && !(entity instanceof EntityItemEnhanced) ) {
            EntityItem entityItem = (EntityItem) entity;
            ItemStack stack = entityItem.getItem();
            if ( stack.isEmpty() )
                return;

            Item item = stack.getItem();
            if ( item instanceof IEnhancedItem ) {
                EntityItemEnhanced fireproofItem = new EntityItemEnhanced(entityItem);
                entityItem.setDead();

                world.spawnEntity(fireproofItem);
                event.setCanceled(true);
            }
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        int id = 0;

        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntityChargedPearl.class)
                .id(new ResourceLocation(WirelessUtils.MODID, "charged_pearl"), id++)
                .name("charged_pearl")
                .tracker(64, 20, true)
                .build());

        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntityFluxedPearl.class)
                .id(new ResourceLocation(WirelessUtils.MODID, "fluxed_pearl"), id++)
                .name("fluxed_pearl")
                .tracker(64, 20, true)
                .build());

        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntityQuenchedPearl.class)
                .id(new ResourceLocation(WirelessUtils.MODID, "quenched_pearl"), id++)
                .name("quenched_pearl")
                .tracker(64, 20, true)
                .build());

        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntityScorchedPearl.class)
                .id(new ResourceLocation(WirelessUtils.MODID, "scorched_pearl"), id++)
                .name("scorched_pearl")
                .tracker(64, 20, true)
                .build());

        event.getRegistry().register(EntityEntryBuilder.create()
                .entity(EntityStabilizedEnderPearl.class)
                .id(new ResourceLocation(WirelessUtils.MODID, "stabilized_ender_pearl"), id++)
                .name("stabilized_ender_pearl")
                .tracker(64, 20, true)
                .build());
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BlockDirectionalCharger());
        event.getRegistry().register(new BlockPositionalCharger());
        //event.getRegistry().register(new BlockChunkCharger());

        event.getRegistry().register(new BlockDirectionalCondenser());
        event.getRegistry().register(new BlockPositionalCondenser());

        event.getRegistry().register(new BlockDirectionalDesublimator());
        event.getRegistry().register(new BlockPositionalDesublimator());

        event.getRegistry().register(new BlockPoweredAir());
        event.getRegistry().register(new BlockPoweredRedstoneWire());

        event.getRegistry().register(new BlockAngledSlime());

        GameRegistry.registerTileEntity(TileAngledSlime.class, new ResourceLocation(WirelessUtils.MODID, "tile_angled_slime"));

        registerTile(TileEntityDirectionalCharger.class);
        registerTile(TileEntityPositionalCharger.class);
        //registerTile(TileEntityChunkCharger.class);

        registerTile(TileEntityDirectionalCondenser.class);
        registerTile(TileEntityPositionalCondenser.class);

        registerTile(TileDirectionalDesublimator.class);
        registerTile(TilePositionalDesublimator.class);

        PluginRegistry.registerBlocks(event);
    }

    public static void registerTile(Class<? extends TileEntity> klass) {
        Machine machine = klass.getAnnotation(Machine.class);
        if ( machine == null )
            return;

        MACHINES.add(klass);
        GameRegistry.registerTileEntity(klass, new ResourceLocation(WirelessUtils.MODID, "tile_" + machine.name()));
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlockMachine(ModBlocks.blockDirectionalCharger).setRegistryName(ModBlocks.blockDirectionalCharger.getRegistryName()));
        event.getRegistry().register(new ItemBlockMachine(ModBlocks.blockPositionalCharger).setRegistryName(ModBlocks.blockPositionalCharger.getRegistryName()));
        //event.getRegistry().register(new ItemBlockMachine(ModBlocks.blockChunkCharger).setRegistryName(ModBlocks.blockChunkCharger.getRegistryName()));

        event.getRegistry().register(new ItemBlockMachine(ModBlocks.blockDirectionalCondenser).setRegistryName(ModBlocks.blockDirectionalCondenser.getRegistryName()));
        event.getRegistry().register(new ItemBlockMachine(ModBlocks.blockPositionalCondenser).setRegistryName(ModBlocks.blockPositionalCondenser.getRegistryName()));

        event.getRegistry().register(new ItemBlockMachine(ModBlocks.blockDirectionalDesublimator).setRegistryName(ModBlocks.blockDirectionalDesublimator.getRegistryName()));
        event.getRegistry().register(new ItemBlockMachine(ModBlocks.blockPositionalDesublimator).setRegistryName(ModBlocks.blockPositionalDesublimator.getRegistryName()));

        event.getRegistry().register(new ItemBlock(ModBlocks.blockAngledSlime).setRegistryName(ModBlocks.blockAngledSlime.getRegistryName()));

        event.getRegistry().register(new ItemFluxedPearl());
        event.getRegistry().register(new ItemChargedPearl());
        event.getRegistry().register(new ItemQuenchedPearl());
        event.getRegistry().register(new ItemScorchedPearl());
        event.getRegistry().register(new ItemStabilizedEnderPearl());

        event.getRegistry().register(new ItemPositionalCard());
        event.getRegistry().register(new ItemGlasses());

        event.getRegistry().register(new ItemEnderCoil());

        event.getRegistry().register(new ItemMachinePanel());
        event.getRegistry().register(new ItemLevelUpgrade());
        event.getRegistry().register(new ItemConversionUpgrade());

        event.getRegistry().register(new ItemBaseAugment());
        event.getRegistry().register(new ItemRangeAugment());
        event.getRegistry().register(new ItemInventoryAugment());
        event.getRegistry().register(new ItemSlotAugment());
        event.getRegistry().register(new ItemCapacityAugment());
        event.getRegistry().register(new ItemTransferAugment());
        event.getRegistry().register(new ItemWorldAugment());
        event.getRegistry().register(new ItemInvertAugment());

        PluginRegistry.registerItems(event);
    }

}
