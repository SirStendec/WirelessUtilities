package com.lordmau5.wirelessutils.utils.mod;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("CanBeFinal")
@Config(modid = WirelessUtils.MODID)
public class ModConfig {
    @Config.Name("Items")
    @Config.Comment("Various settings for different items added by the mod.")
    public static final Items items = new Items();

    @Config.Name("Augments")
    @Config.Comment("Augments are used to upgrade the working capacities of machines in various ways.")
    public static final Augments augments = new Augments();

    @Config.Name("Upgrades")
    @Config.Comment("Upgrades increase the capabilities of machines.")
    public static final Upgrades upgrades = new Upgrades();

    @Config.Name("Chargers")
    @Config.Comment("Chargers are machines that transfer energy into other blocks.")
    public static final Chargers chargers = new Chargers();

    @Config.Name("Desublimators")
    @Config.Comment("Desublimators are machines that transfer items into other blocks.")
    public static final Desublimators desublimators = new Desublimators();

    @Config.Name("Condensers")
    @Config.Comment("Condensers are machines that transfer fluids into other blocks.")
    public static final Condensers condensers = new Condensers();

    @Config.Name("Vaporizers")
    @Config.Comment("Vaporizers are machines that do things to living entities.")
    public static final Vaporizers vaporizers = new Vaporizers();

    @Config.Name("Performance")
    @Config.Comment("Various settings for limiting the mod's performance impact.")
    public static final Performance performance = new Performance();

    @Config.Name("Rendering")
    @Config.Comment("Various settings for adjusting the mod's rendering.")
    public static final Rendering rendering = new Rendering();

    @Config.Name("Plugins")
    @Config.Comment("Configuration for Plugins.")
    public static final Plugins plugins = new Plugins();

    @Config.Name("Positional Machines - Allow Front/Top Connections")
    @Config.Comment("Whether or not to allow connections to Positional Machines from their tops or fronts.")
    @Config.RequiresWorldRestart
    public static boolean positionalConnections = false;

    public static class Items {
        @Config.Name("Charged Pearl")
        @Config.Comment({
                "Charged Pearls can be thrown like Ender Pearls.",
                "Upon hitting a machine, they impart some of their potential energy into the machine."
        })
        public final ChargedPearl chargedPearl = new ChargedPearl();

        @Config.Name("Fluxed Pearl")
        @Config.Comment({
                "Fluxed Pearls can be thrown like Ender Pearls.",
                "Throwing a Fluxed Pearl up into a storm can attract lightning, electrifying the pearl into a Charged Pearl."
        })
        public final FluxedPearl fluxedPearl = new FluxedPearl();

        @Config.Name("Scorched Pearl")
        @Config.Comment({
                "Scorched Pearls can be thrown like Ender Pearls.",
                "Upon hitting a target, they can start a fire."
        })
        public final ScorchedPearl scorchedPearl = new ScorchedPearl();

        @Config.Name("Stabilized Ender Pearl")
        @Config.Comment({
                "Stabilized Ender Pearls behave similarly to other pearls added by the mod. They bounce on slime, etc.",
                "When fired from a Dispenser rather than thrown by an entity, the pearl will, upon landing, teleport the closest entity to where it struck."
        })
        public final StabilizedEnderPearl stabilizedEnderPearl = new StabilizedEnderPearl();

        @Config.Name("Void Pearl")
        @Config.Comment({
                "Void Pearls can be thrown like Ender Pearls.",
                "Upon hitting a living entity, they trap it inside. They can be thrown to release the entity, or used in special machines."
        })
        public final VoidPearl voidPearl = new VoidPearl();
    }

    public static class FluxedPearl {
        @Config.Name("Enable Lightning Crafting")
        @Config.Comment("When enabled, Fluxed Pearls that are damaged by lightning will transform into Charged Pearls.")
        public boolean enableLightning = true;

        @Config.Name("Attract Lightning")
        @Config.Comment({
                "When enabled, throwing a Fluxed Pearl into the open sky will spawn a lightning bolt.",
                "0=Disabled, 1=Fluxed Pearls Only, 2=Stabilized Only, 3=Either"
        })
        @Config.RangeInt(min = 0, max = 3)
        public int attractLightning = 3;

        @Config.Name("Minimum Required Weather")
        @Config.Comment({
                "This type of weather is required to attract lightning.",
                "0=Clear Skies, 1=Raining, 2=Thundering"
        })
        @Config.RangeInt(min = 0, max = 2)
        public int requiredWeather = 1;

        @Config.Name("Thunderstorm Seeding Chance")
        @Config.Comment("When a thrown Fluxed Pearl spawns a lightning bolt and it isn't already thundering, there's this percentage chance of the weather changing to thundering.")
        @Config.RangeDouble(min = 0, max = 1)
        public double thunderingChance = 0.05;

        @Config.Name("Enable Machine Discharging")
        @Config.Comment("Enable crafting a Charged Pearl from a Fluxed Pearl by throwing one against an energy providing block.")
        public boolean enableDischargeMachines = true;

        @Config.Name("Energy to Charge")
        @Config.Comment("This much energy is required to charge a Fluxed Pearl into a Charged Pearl.")
        @Config.RangeInt(min = 1)
        public int chargeEnergy = 8000;

        @Config.Name("Enable Redstone Pulse")
        @Config.Comment("When a Fluxed Pearl impacts a block (and isn't charged from the experience), it releases a brief pulse of redstone energy into the block it hits.")
        public boolean enableRedstonePulse = true;
    }

    public static class ChargedPearl {
        @Config.Name("Enable Charging")
        @Config.Comment("Enable charging machines with thrown Charged Pearls")
        public boolean enableChargeMachines = true;

        @Config.Name("Energy Per Pearl")
        @Config.Comment("A Charged Pearl should insert up to this much energy into a machine when hitting one.")
        @Config.RangeInt(min = 1)
        public int chargeEnergy = 16000;

        @Config.Name("Attempts Per Pearl")
        @Config.Comment("Machines are commonly designed to only accept up to a certain amount of energy per tick. This can end up wasting some of the Charged Pearl's energy. To compensate, we attempt to insert energy up to this many times.")
        @Config.RangeInt(min = 1, max = 500)
        public int chargeAttempts = 20;

        @Config.Name("Enable Quenching")
        @Config.Comment("Enable crafting Quenched Pearls by throwing Charged Pearls into water source blocks.")
        public boolean enableQuenching = true;

        @Config.Name("Enable Scorching")
        @Config.Comment("Enable crafting Scorched Pearls by throwing Charged Pearls into lava source blocks.")
        public boolean enableScorching = true;
    }

    public static class ScorchedPearl {
        @Config.Name("Create Fire on Impact")
        @Config.Comment("Enable Scorched Pearls to create fires when thrown at a block.")
        public boolean fireOnImpact = true;

        @Config.Name("Light Players on Fire")
        @Config.Comment("Enable setting players holding Scorched Pearls on fire.")
        public boolean fireUpPlayers = false;
    }

    public static class StabilizedEnderPearl {
        @Config.Name("Search Radius")
        @Config.Comment("Search this many blocks in each direction for a living entity to teleport if the pearl wasn't thrown by one.")
        @Config.RangeInt(min = 0, max = 128)
        public int radius = 16;
    }

    public static class VoidPearl {
        @Config.Name("Enable Voiding")
        @Config.Comment("Enable crafting Void Pearls by throwing Stabilized Ender Pearls into an end portal.")
        public boolean enableVoiding = true;

        @Config.Name("Entity Blacklist")
        @Config.Comment("A list of entities that cannot be placed within Void Pearls.")
        public String[] blacklist = {};
    }

    public static class Upgrades {
        @Config.Name("Allow Upgrade Crafting")
        @Config.Comment("When enabled, machines can be upgraded by crafting them together with an upgrade kit or conversion kit.")
        public boolean allowCrafting = true;

        @Config.Name("Allow In-World Upgrading")
        @Config.Comment("When enabled, machines can be upgraded by using an upgrade kit or conversion kit on them in the world with right-click.")
        public boolean allowInWorld = true;
    }

    public static class Augments {
        @Config.Name("Require Previous Tiers")
        @Config.Comment("When enabled, installing an augment into a machine requires that the machine also has the previous tier augments.")
        @Config.RequiresWorldRestart
        public boolean requirePreviousTiers = false;

        @Config.Name("Require Machine Level")
        @Config.Comment("When enabled, installing an augment into a machine requires that the machine is at least the same level as that augment.")
        @Config.RequiresWorldRestart
        public boolean requireMachineLevel = false;

        @Config.Name("Sided I/O Control Augments")
        @Config.Comment("Sided I/O Control Augments allow machines to automatically import or export via their sides.")
        public final SidedTransferAugments sidedTransfer = new SidedTransferAugments();

        @Config.Name("Capacity Augments")
        @Config.Comment("Capacity Augments increase the maximum capacity of machines.")
        public final CapacityAugments capacity = new CapacityAugments();

        @Config.Name("Invert Augments")
        @Config.Comment("Invert Augments reverse the operational flow of a machine. Chargers will drain power, etc.")
        public final InvertAugments invert = new InvertAugments();

        @Config.Name("Chunk Load Augments")
        @Config.Comment("Chunk Load Augments cause a machine to keep its target blocks loaded whenever the machine itself is loaded.")
        public final ChunkLoadAugments chunkLoad = new ChunkLoadAugments();

        @Config.Name("Transfer Augments")
        @Config.Comment("Transfer Augments increase the maximum transfer rate of machines.")
        public final TransferAugments transfer = new TransferAugments();

        @Config.Name("Slot Augments")
        @Config.Comment("Slot Augments unlock additional slots in machines with item slots.")
        public final SlotAugments slot = new SlotAugments();

        @Config.Name("Range Augments")
        @Config.Comment("Range Augments are used to upgrade the working radius of machines.")
        public final RangeAugments range = new RangeAugments();

        @Config.Name("Inventory Augments")
        @Config.Comment("Inventory Augments allow machines to scan for processable items within block inventories.")
        public final InventoryAugments inventory = new InventoryAugments();

        @Config.Name("Crop Augments")
        @Config.Comment("Crop Augments allow machines to interact with crops. This includes: planting, fertilizing, and (with an Invert Augment) harvesting.")
        public final CropAugments crop = new CropAugments();

        @Config.Name("Block Augments")
        @Config.Comment("Block Augments allow Desublimators to place and break blocks.")
        public final BlockAugments block = new BlockAugments();

        @Config.Name("World Augments")
        @Config.Comment("World Augments allow machines to directly interact with the world, such as by placing or sucking up fluids.")
        public final WorldAugments world = new WorldAugments();

        @Config.Name("Dispenser Augments")
        @Config.Comment("Dispenser Augments allow Desublimators to dispense items into the world, rather than just dropping them.")
        public final DispenserAugments dispenser = new DispenserAugments();

        @Config.Name("Auxiliary Condenser Augments")
        @Config.Comment("Auxiliary Condenser Augments allow machines to generate fluid with energy.")
        public final FluidGenAugments fluidGen = new FluidGenAugments();
    }

    public static class WorldAugments {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with a World Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;
    }

    public static class ChunkLoadAugments {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with a Chunk Load Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;
    }

    public static class InvertAugments {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with an Invert Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;
    }

    public static class SidedTransferAugments {
        @Config.Name("Require for Sided I/O")
        @Config.Comment("When enabled, an installed augment is required to configure a machine's sided transfer settings.")
        @Config.RequiresWorldRestart
        public boolean required = false;

        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with a Sided I/O Control Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;
    }

    public static class FluidGenAugments {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with a Fluid Gen Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Default Fluid")
        @Config.Comment("Generate this fluid.")
        @Config.RequiresWorldRestart
        public String fluidName = "water";

        @Config.Name("Default Fluid Rate")
        @Config.Comment("Generate this many mB of fluid per tick.")
        @Config.RequiresWorldRestart
        @Config.RangeInt(min = 0)
        public int fluidRate = 1000;

        @Config.Name("Default Energy Cost")
        @Config.Comment("Cost this many RF per tick to generate fluid.")
        @Config.RequiresWorldRestart
        @Config.RangeInt(min = 0)
        public int energyCost = 0;

        @Config.Name("Allow Trapping Cows")
        @Config.Comment("Right-clicking a cow with an Auxiliary Condenser Augment will cause the augment to produce milk while removing the cow from the world. Up to this many cows can be trapped in a single augment.")
        @Config.RangeInt(min = 0, max = Byte.MAX_VALUE)
        public int allowCows = 10;

        @Config.Name("Cow Milk Rate")
        @Config.Comment("Trapped Cows each produce milk at this mB/t.")
        @Config.RangeInt(min = 1)
        @Config.RequiresWorldRestart
        public int milkRate = 100;

        @Config.Name("Allow Drinking Milk")
        @Config.Comment("When milk production is at at least this rate of mB/t, players can drink from the augment like it's an infinite bucket of milk. Set to zero to disable.")
        @Config.RangeInt(min = 0)
        public int milkDrink = 1000;

        @Config.Name("Allow Trapping Mooshrooms")
        @Config.Comment("Right-clicking a Mooshroom with an Auxiliary Condenser Augment will cause the augment to produce Mushroom Stew while removing the mooshroom from the world. Up to this many mooshrooms can be trapped in a single augment.")
        @Config.RangeInt(min = 0, max = Byte.MAX_VALUE)
        public int allowMooshrooms = 10;

        @Config.Name("Mooshroom Stew Rate")
        @Config.Comment("Trapped Mooshrooms each produce Mushroom Stew at this mB/t.")
        @Config.RangeInt(min = 1)
        @Config.RequiresWorldRestart
        public int stewRate = 25;

        @Config.Name("Allow Eating Stew")
        @Config.Comment("When Mushroom Stew production is at at least this rate of mB/t, players can eat from the augment like it's an infinite bowl of mushroom stew. Set to zero to disable. Recommended: 250")
        @Config.RangeInt(min = 0)
        public int stewEat = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;
    }

    public static class CropAugments {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with a Crop Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Allow Silk Touch")
        @Config.Comment("Allow Crop Augments to be enchanted with Silk Touch, changing harvesting behavior to use Silk Touch when possible.")
        @Config.RequiresWorldRestart
        public boolean allowSilkTouch = true;

        @Config.Name("Allow Fortune")
        @Config.Comment("Allow Crop Augments to be enchanted with Fortune, to be applied when harvesting crops.")
        @Config.RequiresWorldRestart
        public boolean allowFortune = true;

        @Config.Name("Silk Touch Block Activation")
        @Config.Comment("Activate crop blocks (right-click) to harvest them with Silk Touch, rather than breaking them. This behavior will not work correctly if there is no other mod present that adds activation behavior to crops.")
        public boolean useActivation = true;

        @Config.Name("Use Block Breaking Effects")
        @Config.Comment("Whether or not particles and sound effects should be played when a Crop Augment breaks a block.")
        public boolean useEffects = true;

        @Config.Name("Process Trees")
        @Config.Comment("Whether or not Crop Augments should be able to harvest trees.")
        public boolean processTrees = true;

        @Config.Name("Tree: Use Block Breaking Effects")
        @Config.Comment("Whether or not particles and sound effects should be played when a Crop Augment breaks a tree. This will affect multiple blocks at a time.")
        public boolean treeEffects = false;

        @Config.Name("Tree: Blocks per Tick")
        @Config.Comment("How many tree blocks should be harvested per tick. Includes leaves and logs.")
        @Config.RangeInt(min = 1)
        public int treeBlocksPerTick = 20;

        @Config.Name("Tree: Scan Depth")
        @Config.Comment("How many blocks from the origin block should be scanned while discovering a tree's layout.")
        @Config.RangeInt(min = 1)
        public int treeScanDepth = 150;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;

        @Config.Name("Automatically Till Ground")
        @Config.Comment("Whether or not Crop Augments should automatically till dirt and grass.")
        public boolean automaticallyTill = false;

        @Config.Name("Additional Fertilizers")
        @Config.Comment("Any items listed here will be treated as fertilizers, even if they aren't automatically detected as such.")
        @Config.RequiresWorldRestart
        public String[] extraFertilizers = {};
    }

    public static class BlockAugments {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with a Block Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Allow Silk Touch")
        @Config.Comment("Allow Block Augments to be enchanted with Silk Touch, so that blocks are harvested with Silk Touch.")
        @Config.RequiresWorldRestart
        public boolean allowSilkTouch = true;

        @Config.Name("Allow Fortune")
        @Config.Comment("Allow Block Augments to be enchanted with Fortune, to be applied when harvesting blocks.")
        @Config.RequiresWorldRestart
        public boolean allowFortune = true;

        @Config.Name("Maximum Harvest Level")
        @Config.Comment("The maximum harvest level for blocks that should be harvestable via a Block Augment. Defaults to diamond.")
        @Config.RangeInt(min = 0)
        public int harvestLevel = 3;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;
    }

    public static class CapacityAugments {
        @Config.Name("Available Tiers")
        @Config.Comment("Capacity Augments will be made available in this many tiers. The maximum is limited by the available levels.")
        @Config.RangeInt(min = 0, max = Short.MAX_VALUE - 1)
        @Config.RequiresMcRestart
        public int availableTiers = 5;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] energyMultiplier = {1, 1, 1, 1, 1};

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] energyAddition = {0, 0, 0, 0, 0};

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int[] energyDrain = {0, 0, 0, 0, 0};
    }

    public static class TransferAugments {
        @Config.Name("Available Tiers")
        @Config.Comment("Transfer Augments will be made available in this many tiers. The maximum is limited by the available levels.")
        @Config.RangeInt(min = 0, max = Short.MAX_VALUE - 1)
        @Config.RequiresMcRestart
        public int availableTiers = 5;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] energyMultiplier = {1, 1, 1, 1, 1};

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] energyAddition = {0, 0, 0, 0, 0};

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int[] energyDrain = {0, 0, 0, 0, 0};
    }

    public static class SlotAugments {
        @Config.Name("Available Tiers")
        @Config.Comment("Slot Augments will be made available in this many tiers. The maximum is limited by the available levels.")
        @Config.RangeInt(min = 0, max = Short.MAX_VALUE - 1)
        @Config.RequiresMcRestart
        public int availableTiers = 3;

        @Config.Name("Slots per Tier")
        @Config.Comment("Each additional tier unlocks this many slots.")
        @Config.RangeInt(min = 1)
        public int slotsPerTier = 3;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] energyMultiplier = {1, 1, 1};

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] energyAddition = {0, 0, 0};

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int[] energyDrain = {0, 0, 0};
    }

    public static class RangeAugments {
        @Config.Name("Available Tiers")
        @Config.Comment("Range Augments will be made available in this many tiers. The maximum is limited by the available levels.")
        @Config.RangeInt(min = 0, max = Short.MAX_VALUE - 1)
        @Config.RequiresMcRestart
        public int availableTiers = 5;

        @Config.Name("Enable Interdimensional Tier")
        @Config.Comment("An interdimensional range augment will allow Positional machines to work across dimensions.")
        @Config.RequiresWorldRestart
        public boolean enableInterdimensional = true;

        @Config.Name("Required Level for Interdimensional Tier")
        @Config.Comment("Machines must be at least this level in order to be augmented with an Interdimensional Range Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int interdimensionalLevel = 0;

        @Config.Name("Maximum Directional Tier")
        @Config.Comment("This is the maximum tier that can be placed in Directional machines. Due to how they work, allowing them to scan too large an area can be a significant source of server lag.")
        @Config.RangeInt(min = 0)
        public int maxTierDirectional = 5;

        @Config.Name("Blocks Per Tier")
        @Config.Comment("Each additional tier adds this many blocks to the range of the charger.")
        @Config.RangeInt(min = 1, max = 128)
        public int blocksPerTier = 16;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int[] energyDrain = {0, 0, 0, 0, 0};
    }

    public static class InventoryAugments {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with an Inventory Augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Maximum Scan Slots")
        @Config.Comment("Scan up to this many slots when searching inventories for chargeable items. Slots after this number will be ignored to minimize performance impact.")
        public int maximumScanSlots = 54;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;
    }

    public static class DispenserAugments {
        @Config.Name("Allow Item Collection")
        @Config.Comment("When enabled, inverted Desublimators with this augment will pick up items in their work area.")
        @Config.RequiresWorldRestart
        public boolean collectItems = true;

        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with this augment type.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This augment will drain this amount of RF/t from non-disabled machines they are in.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;
    }

    public static class Chargers {
        @Config.Name("Positional Charger")
        public PositionalCharger positionalCharger = new PositionalCharger();

        @Config.Name("Directional Charger")
        public DirectionalCharger directionalCharger = new DirectionalCharger();
    }

    public static class PositionalCharger {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to power a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to power a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum cost to power a distant block will never exceed this value. Also the cost for interdimensional charging.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class DirectionalCharger {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to power a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to power a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum Cost")
        @Config.Comment("The cost to power a distant block will never exceed this value.")
        @Config.RangeInt(min = 0)
        public int maximumCost = 1500;
    }

    public static class Vaporizers {
        @Config.Name("Use Experience Fluid")
        @Config.Comment("Whether or not Vaporizers should use some form of fluid experience for modules that require fuel.")
        @Config.RequiresWorldRestart
        public boolean useFluid = true;

        @Config.Name("Fluid mB per Experience Point")
        @Config.Comment("Fluid experience should convert to experience points at this ratio.")
        @Config.RangeInt(min = 1)
        public int mbPerPoint = 20;

        @Config.Name("Custom Fluid Name")
        @Config.Comment("If set, this fluid is used rather than an auto-discovered fluid.")
        @Config.RequiresWorldRestart
        public String customFluid = "";

        @Config.Name("Maximum Entities Per Tick")
        @Config.Comment("The maximum number of entities to handle in a work tick.")
        public int entitiesPerTick = 100;

        @Config.Name("Compatibility")
        @Config.Comment("Compatibility with other mods!")
        public VaporizerCompatibility compatibility = new VaporizerCompatibility();

        @Config.Name("Modules")
        @Config.Comment("Modules give a Vaporizer purpose. Without an installed module, they have no behavior.")
        public Modules modules = new Modules();
    }

    public static class VaporizerCompatibility {
        @Config.Name("Use Morbs")
        @Config.Comment("Allow the use of Morbs from Thermal Expansion in Vaporizers.")
        @Config.RequiresMcRestart
        public boolean useMorbs = true;

        @Config.Name("Use Mob Imprisonment Tools")
        @Config.Comment("Allow the use of Mob Imprisonment Tools from Industrial Foregoing in Vaporizers.")
        @Config.RequiresMcRestart
        public boolean useMITs = true;
    }

    public static class Modules {
        @Config.Name("Capture Module")
        @Config.Comment("Capture Modules will capture entities within the Vaporizer's working area within Void Pearls.")
        public CaptureModule capture = new CaptureModule();

        @Config.Name("Duplication Module")
        @Config.Comment("Duplication Modules will create duplicates of a captured entity within the Vaporizer's working area.")
        public CloneModule clone = new CloneModule();

        @Config.Name("Slaughter Module")
        @Config.Comment("Slaughter Modules will kill all living entities within the Vaporizer's working area.")
        public SlaughterModule slaughter = new SlaughterModule();

        @Config.Name("Teleportation Module")
        @Config.Comment("Teleportation Modules transport all entities within the Vaporizer's working area to a set location.")
        public TeleportModule teleport = new TeleportModule();
    }

    public static class CaptureModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for vaporizers using this module.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for vaporizers using this module.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This module will drain this amount of RF/t from non-disabled vaporizers using them.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;
    }

    public static class CloneModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for vaporizers using this module.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for vaporizers using this module.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This module will drain this amount of RF/t from non-disabled vaporizers using them.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;

        @Config.Name("Exact Copy Factor")
        @Config.Comment({
                "When greater than zero, exact copies of mobs can be spawned for an increased cost.",
                "The formula will be Cost = Ceiling(Exact Copy Factor * (Base Experience * Experience Factor + Max Health * Health Factor))"
        })
        @Config.RangeDouble(min = 0)
        public double exactFactor = 2;

        @Config.Name("Animal Base Experience")
        @Config.Comment("Animal entities do not expose experience in a standard way. Because of that, each one will be worth this number of experience points to spawn.")
        @Config.RangeInt(min = 0)
        @Config.RequiresMcRestart
        public int animalBaseExp = 3;

        @Config.Name("Experience Factor")
        @Config.Comment("The cost to spawn an entity is calculated as Cost = Ceiling(Base Experience * Experience Factor + Max Health * Health Factor)")
        @Config.RangeDouble(min = 0)
        public double expFactor = 2;

        @Config.Name("Health Factor")
        @Config.Comment("The cost to spawn an entity is calculated as Cost = Ceiling(Base Experience * Experience Factor + Max Health * Health Factor)")
        @Config.RangeDouble(min = 0)
        public double healthFactor = 0;

        @Config.Name("Maximum Entity - Count")
        @Config.Comment("No more than this number of the target entity should exist in the immediate area. If they do, temporarily stop spawning.")
        @Config.RangeInt(min = 1)
        public int maxCount = 20;

        @Config.Name("Maximum Entity - Range")
        @Config.Comment("Check within this many blocks of the target spawn location.")
        @Config.RangeInt(min = 0)
        public int maxRange = 10;
    }

    public static class TeleportModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for vaporizers using this module.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for vaporizers using this module.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This module will drain this amount of RF/t from non-disabled vaporizers using them.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;

        @Config.Name("Limit to Living Entities")
        @Config.Comment("When enabled, only living entities will be transported. Otherwise, ALL entities can be targetted.")
        @Config.RequiresWorldRestart
        public boolean livingOnly = false;

        @Config.Name("Target Players")
        @Config.Comment("When enabled, players can be transported.")
        @Config.RequiresWorldRestart
        public boolean targetPlayers = true;

        @Config.Name("Cost per Block")
        @Config.Comment("The cost to teleport entities to a distant block goes up by this amount per block, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to teleport entities to a distant block goes up by this amount per block as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum cost to teleport entities to a distant block will never exceed this value. Also the cost for interdimensional teleportation.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class SlaughterModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for vaporizers using this module.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for vaporizers using this module.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This module will drain this amount of RF/t from non-disabled vaporizers using them.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;

        @Config.Name("Attack Bosses")
        @Config.Comment("When enabled, the Slaughter Module will kill enemies marked as Bosses, such as Withers.")
        @Config.RequiresWorldRestart
        public boolean attackBosses = true;

        @Config.Name("Target Players")
        @Config.Comment("When enabled, players can be attacked.")
        @Config.RequiresWorldRestart
        public boolean targetPlayers = false;

        @Config.Name("Enable Weapons")
        @Config.Comment("When enabled, the Slaughter Module will use a weapon placed in the Input slot of the Vaporizer.")
        @Config.RequiresWorldRestart
        public boolean enableWeapon = true;

        @Config.Name("Enable Use Weapon Mode")
        @Config.Comment("In Use Weapon Mode, the Slaughter Module will use its weapon in the same way a player does, allowing normal weapon things to happen, such as Tinker Tools gaining experience.")
        @Config.RequiresWorldRestart
        public boolean enableUseWeapon = true;

        @Config.Name("Weapon Durability Use")
        @Config.Comment("The amount of durability damage to apply to weapons for each successful use.")
        @Config.RangeInt(min = 0)
        public int damageWeapon = 1;

        @Config.Name("Damage Mode")
        @Config.Comment("0=Entity's Health + Absorption, 1=Entity's Max Health + Absorption, 2=Maximum Damage")
        @Config.RangeInt(min = 0, max = 2)
        public int damageMode = 1;

        @Config.Name("Maximum Damage")
        @Config.Comment("Slaughter Modules should do up to this much damage when they attack. 0 for Maximum.")
        @Config.RangeDouble(min = 0, max = 1000000000)
        public double maxDamage = 0D;

        @Config.Name("Damage is Unblockable")
        @Config.Comment("When enabled, damage cannot be blocked by shields and armor.")
        public boolean unblockable = true;

        @Config.Name("Damage is Absolute")
        @Config.Comment("When enabled, damage cannot be mitigated through effects and enchantments.")
        public boolean absolute = true;

        @Config.Name("Collect Drops - Default")
        @Config.Comment("0 = Ignore Drops, 1 = Collect Drops till Full, 2 = Collect All Drops, 3 = Void Drops")
        public int collectDrops = 1;

        @Config.Name("Collect Drops - Minimum")
        @Config.Comment("0 = Ignore Drops, 1 = Collect Drops till Full, 2 = Collect All Drops, 3 = Void Drops")
        public int collectDropsMinimum = 0;

        @Config.Name("Collect Experience - Default")
        @Config.Comment("0 = Ignore Experience, 1 = Collect Experience till Full, 2 = Collect All Experience, 3 = Void Experience")
        public int collectExperience = 2;

        @Config.Name("Collect Experience - Minimum")
        @Config.Comment("0 = Ignore Experience, 1 = Collect Experience till Full, 2 = Collect All Experience, 3 = Void Experience")
        public int collectExperienceMinimum = 0;
    }

    public static class Condensers {
        @Config.Name("Positional Condenser")
        public PositionalCondenser positionalCondenser = new PositionalCondenser();

        @Config.Name("Directional Condenser")
        public DirectionalCondenser directionalCondenser = new DirectionalCondenser();

        @Config.Name("Allow World Augment")
        @Config.Comment("A World Augment allows condensers to place / retrieve fluids in the world.")
        public boolean allowWorldAugment = true;
    }

    public static class PositionalCondenser {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to transfer fluids into a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to transfer fluids into a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum cost to transfer fluids into a distant block will never exceed this value. Also the cost for interdimensional charging.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class DirectionalCondenser {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to transfer fluids into a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to transfer fluids into a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum Cost")
        @Config.Comment("The cost to transfer fluids into a distant block will never exceed this value.")
        @Config.RangeInt(min = 0)
        public int maximumCost = 1500;
    }


    public static class Desublimators {
        @Config.Name("Positional Desublimator")
        public PositionalDesublimator positionalDesublimator = new PositionalDesublimator();

        @Config.Name("Directional Desublimator")
        public DirectionalDesublimator directionalDesublimator = new DirectionalDesublimator();

        @Config.Name("Base Slots")
        @Config.Comment("A Desublimator's buffer should have this many slots without any augments.")
        @Config.RangeInt(min = 1, max = 18)
        public int minimumSlots = 3;

        @Config.Name("Slots per Augment")
        @Config.Comment("A Desublimator's buffer should gain this many slots per tier of Capacity Augment.")
        @Config.RangeInt(min = 1, max = 18)
        public int slotsPerTier = 3;

        @Config.Name("Allow World Augment")
        @Config.Comment("A World Augment allows Desublimators to place blocks in the world.")
        public boolean allowWorldAugment = true;

        @Config.Name("Block Blacklist")
        @Config.Comment("Desublimators should not interact with blocks in this list.")
        public String[] blockBlacklist = {};
    }

    public static class PositionalDesublimator {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to transfer items into a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to transfer items into a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum cost to transfer items into a distant block will never exceed this value. Also the cost for interdimensional charging.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class DirectionalDesublimator {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to transfer items into a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to transfer items into a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum Cost")
        @Config.Comment("The cost to transfer items into a distant block will never exceed this value.")
        @Config.RangeInt(min = 0)
        public int maximumCost = 1500;
    }


    public static class AppliedEnergistics {
        @Config.Name("Positional AE Network")
        public PositionalAENetwork positionalAENetwork = new PositionalAENetwork();

        @Config.Name("Directional AE Network")
        public DirectionalAENetwork directionalAENetwork = new DirectionalAENetwork();

        @Config.Name("Dense Cable Connection")
        @Config.Comment("Whether or not the AE Network machines should act like a Dense Cable (32 channels) or not (8 channels)")
        public boolean denseCableConnection = true;
    }

    public static class PositionalAENetwork {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 1;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum cost to connect a distant block will never exceed this value. Also the cost for interdimensional connections.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class DirectionalAENetwork {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 1;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum Cost")
        @Config.Comment("The cost to connect a distant block will never exceed this value.")
        @Config.RangeInt(min = 0)
        public int maximumCost = 1000;
    }

    public static class RefinedStorage {
        @Config.Name("Positional RS Network")
        public PositionalRSNetwork positionalRSNetwork = new PositionalRSNetwork();

        @Config.Name("Directional RS Network")
        public DirectionalRSNetwork directionalRSNetwork = new DirectionalRSNetwork();
    }

    public static class PositionalRSNetwork {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 1;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum cost to connect a distant block will never exceed this value. Also the cost for interdimensional connections.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class DirectionalRSNetwork {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 1;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum Cost")
        @Config.Comment("The cost to connect a distant block will never exceed this value.")
        @Config.RangeInt(min = 0)
        public int maximumCost = 1000;
    }

    public static class Performance {
        @Config.Name("Steps Per Tick")
        @Config.Comment("Machines should processEntity this many tile entities and/or items per tick, until they run out of energy.")
        @Config.RangeInt(min = 1)
        public int stepsPerTick = 20;

        @Config.Name("Scan Frequency")
        @Config.Comment("Machines should scan for new tile entities and items that they can charge once per this many ticks.")
        @Config.RangeInt(min = 0)
        public int scanRate = 40;
    }

    public static class Rendering {
        @Config.Name("Enable Area Renderer")
        @Config.Comment("Work Areas and Positional Card targets are displayed with a render event listener. This setting disables the event listener.")
        @Config.RequiresMcRestart
        public boolean enableAreaRenderer = true;
    }

    public static class Plugins {
        @Config.Name("Applied Energistics")
        @Config.Comment("Configuration for the Applied Energistics Plugin.")
        public AppliedEnergistics appliedEnergistics = new AppliedEnergistics();

        @Config.Name("Refined Storage")
        @Config.Comment("Configuration for the Refined Storage Plugin.")
        public RefinedStorage refinedStorage = new RefinedStorage();
    }

    @Mod.EventBusSubscriber(modid = WirelessUtils.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if ( event.getModID().equals(WirelessUtils.MODID) ) {
                ConfigManager.sync(WirelessUtils.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
