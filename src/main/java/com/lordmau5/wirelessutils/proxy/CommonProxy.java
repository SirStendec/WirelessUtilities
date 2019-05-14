package com.lordmau5.wirelessutils.proxy;

import com.google.common.collect.ImmutableSet;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.BlockDirectionalAir;
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
import com.lordmau5.wirelessutils.commands.FluidGenCommand;
import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import com.lordmau5.wirelessutils.entity.pearl.EntityChargedPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityFluxedPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityQuenchedPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityScorchedPearl;
import com.lordmau5.wirelessutils.entity.pearl.EntityStabilizedEnderPearl;
import com.lordmau5.wirelessutils.fixers.InventoryWalker;
import com.lordmau5.wirelessutils.fixers.NullableItemListWalker;
import com.lordmau5.wirelessutils.item.ItemAbsolutePositionalCard;
import com.lordmau5.wirelessutils.item.ItemBlockExplainable;
import com.lordmau5.wirelessutils.item.ItemBlockMachine;
import com.lordmau5.wirelessutils.item.ItemEnderCoil;
import com.lordmau5.wirelessutils.item.ItemGlasses;
import com.lordmau5.wirelessutils.item.ItemMachinePanel;
import com.lordmau5.wirelessutils.item.ItemPlayerPositionalCard;
import com.lordmau5.wirelessutils.item.ItemRelativePositionalCard;
import com.lordmau5.wirelessutils.item.augment.ItemBaseAugment;
import com.lordmau5.wirelessutils.item.augment.ItemBlockAugment;
import com.lordmau5.wirelessutils.item.augment.ItemCapacityAugment;
import com.lordmau5.wirelessutils.item.augment.ItemChunkLoadAugment;
import com.lordmau5.wirelessutils.item.augment.ItemCropAugment;
import com.lordmau5.wirelessutils.item.augment.ItemDispenserAugment;
import com.lordmau5.wirelessutils.item.augment.ItemFluidGenAugment;
import com.lordmau5.wirelessutils.item.augment.ItemInventoryAugment;
import com.lordmau5.wirelessutils.item.augment.ItemInvertAugment;
import com.lordmau5.wirelessutils.item.augment.ItemRangeAugment;
import com.lordmau5.wirelessutils.item.augment.ItemSidedTransferAugment;
import com.lordmau5.wirelessutils.item.augment.ItemSlotAugment;
import com.lordmau5.wirelessutils.item.augment.ItemTransferAugment;
import com.lordmau5.wirelessutils.item.augment.ItemWorldAugment;
import com.lordmau5.wirelessutils.item.base.IEnhancedItem;
import com.lordmau5.wirelessutils.item.pearl.ItemChargedPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemFluxedPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemQuenchedPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemScorchedPearl;
import com.lordmau5.wirelessutils.item.pearl.ItemStabilizedEnderPearl;
import com.lordmau5.wirelessutils.item.upgrade.ItemConversionUpgrade;
import com.lordmau5.wirelessutils.item.upgrade.ItemLevelUpgrade;
import com.lordmau5.wirelessutils.packet.PacketParticleLine;
import com.lordmau5.wirelessutils.plugins.PluginRegistry;
import com.lordmau5.wirelessutils.tile.TileAngledSlime;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.base.TileEntityBaseMachine;
import com.lordmau5.wirelessutils.tile.charger.TileEntityDirectionalCharger;
import com.lordmau5.wirelessutils.tile.charger.TileEntityPositionalCharger;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityDirectionalCondenser;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityPositionalCondenser;
import com.lordmau5.wirelessutils.tile.desublimator.TileDirectionalDesublimator;
import com.lordmau5.wirelessutils.tile.desublimator.TilePositionalDesublimator;
import com.lordmau5.wirelessutils.utils.ChargerRecipeManager;
import com.lordmau5.wirelessutils.utils.ChunkManager;
import com.lordmau5.wirelessutils.utils.CondenserRecipeManager;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.WUFakePlayer;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.World;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber
public class CommonProxy {

    public static final List<Item> ITEMS = new ArrayList<>();
    public static final List<Class<? extends TileEntity>> MACHINES = new ArrayList<>();

    public void preInit(FMLPreInitializationEvent e) {
        PluginRegistry.preInit(e);
    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(WirelessUtils.instance, new GuiHandler());

        PacketParticleLine.initialize();

        if ( ModItems.itemStabilizedEnderPearl != null )
            OreDictionary.registerOre("enderpearl", ModItems.itemStabilizedEnderPearl);

        EntityFluxedPearl.initReactions();
        ModAdvancements.initTriggers();

        ModItems.initLootTables();

        PluginRegistry.init(e);

        initFixers();
    }

    public void postInit(FMLPostInitializationEvent e) {
        PluginRegistry.postInit(e);
    }

    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new DebugCommand());
        event.registerServerCommand(new FluidGenCommand());
    }

    public void handleIdMapping(FMLModIdMappingEvent event) {
        ChargerRecipeManager.refresh();
        CondenserRecipeManager.refresh();
    }

    @SuppressWarnings("deprecation")
    public void initFixers() {
        CompoundDataFixer global_fixer = FMLCommonHandler.instance().getDataFixer();

        /*ModFixs fixer = global_fixer.init(WirelessUtils.MODID, WirelessUtils.DATA_VERSION);
        fixer.registerFix(FixTypes.CHUNK, new FixBlockLevels());*/

        Set<Class<?>> machines = new HashSet<>();
        for (Class<? extends TileEntity> machine : MACHINES) {
            if ( TileEntityBaseMachine.class.isAssignableFrom(machine) )
                machines.add(machine);
        }

        global_fixer.registerWalker(FixTypes.BLOCK_ENTITY, new InventoryWalker(machines, "Inventory"));
        global_fixer.registerWalker(FixTypes.BLOCK_ENTITY, new NullableItemListWalker(ImmutableSet.of(
                TileDirectionalDesublimator.class,
                TilePositionalDesublimator.class
        ), "Locks"));
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if ( world != null ) {
            WUFakePlayer.removeFakePlayer(world);
            ChunkManager.unloadWorld(world.provider.getDimension());
        }

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
        event.getRegistry().register(new BlockDirectionalAir());

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

    private static void registerTile(Class<? extends TileEntity> klass) {
        Machine machine = klass.getAnnotation(Machine.class);
        if ( machine == null )
            return;

        MACHINES.add(klass);
        GameRegistry.registerTileEntity(klass, new ResourceLocation(WirelessUtils.MODID, "tile_" + machine.name()));
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        ModItems.initRecipes();
        PluginRegistry.registerRecipes();
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        registerItem(event, new ItemBlockMachine(ModBlocks.blockDirectionalCharger).setRegistryName(ModBlocks.blockDirectionalCharger.getRegistryName()));
        registerItem(event, new ItemBlockMachine(ModBlocks.blockPositionalCharger).setRegistryName(ModBlocks.blockPositionalCharger.getRegistryName()));
        //registerItem(event, new ItemBlockMachine(ModBlocks.blockChunkCharger).setRegistryName(ModBlocks.blockChunkCharger.getRegistryName()));

        registerItem(event, new ItemBlockMachine(ModBlocks.blockDirectionalCondenser).setRegistryName(ModBlocks.blockDirectionalCondenser.getRegistryName()));
        registerItem(event, new ItemBlockMachine(ModBlocks.blockPositionalCondenser).setRegistryName(ModBlocks.blockPositionalCondenser.getRegistryName()));

        registerItem(event, new ItemBlockMachine(ModBlocks.blockDirectionalDesublimator).setRegistryName(ModBlocks.blockDirectionalDesublimator.getRegistryName()));
        registerItem(event, new ItemBlockMachine(ModBlocks.blockPositionalDesublimator).setRegistryName(ModBlocks.blockPositionalDesublimator.getRegistryName()));

        registerItem(event, new ItemBlockExplainable(ModBlocks.blockAngledSlime).setRegistryName(ModBlocks.blockAngledSlime.getRegistryName()));

        registerItem(event, new ItemFluxedPearl());
        registerItem(event, new ItemChargedPearl());
        registerItem(event, new ItemQuenchedPearl());
        registerItem(event, new ItemScorchedPearl());
        registerItem(event, new ItemStabilizedEnderPearl());

        registerItem(event, new ItemAbsolutePositionalCard());
        registerItem(event, new ItemRelativePositionalCard());
        registerItem(event, new ItemPlayerPositionalCard());
        registerItem(event, new ItemGlasses());

        registerItem(event, new ItemEnderCoil());

        registerItem(event, new ItemMachinePanel());
        registerItem(event, new ItemLevelUpgrade());
        registerItem(event, new ItemConversionUpgrade());

        registerItem(event, new ItemBaseAugment());
        registerItem(event, new ItemRangeAugment());
        registerItem(event, new ItemInventoryAugment());
        registerItem(event, new ItemSlotAugment());
        registerItem(event, new ItemCapacityAugment());
        registerItem(event, new ItemTransferAugment());
        registerItem(event, new ItemWorldAugment());
        registerItem(event, new ItemBlockAugment());
        registerItem(event, new ItemInvertAugment());
        registerItem(event, new ItemCropAugment());
        registerItem(event, new ItemChunkLoadAugment());
        registerItem(event, new ItemFluidGenAugment());
        registerItem(event, new ItemSidedTransferAugment());
        registerItem(event, new ItemDispenserAugment());

        PluginRegistry.registerItems(event);
    }

    public static void registerItem(RegistryEvent.Register<Item> event, Item item) {
        event.getRegistry().register(item);
        ITEMS.add(item);
    }

}
