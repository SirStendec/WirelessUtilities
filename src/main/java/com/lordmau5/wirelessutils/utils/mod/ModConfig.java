package com.lordmau5.wirelessutils.utils.mod;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.module.ItemSlaughterModule;
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

    @Config.Name("Common")
    @Config.Comment("Options that don't fit into other categories.")
    public static final Common common = new Common();

    public static class Common {
        @Config.Name("Positional Machines - Allow Front/Top Connections")
        @Config.Comment("Whether or not to allow connections to Positional Machines from their tops or fronts.")
        @Config.RequiresWorldRestart
        public boolean positionalConnections = false;

        @Config.Name("Display Work Budget GUI")
        @Config.Comment("Whether or not to display the Work Budget GUI elements for machines that use budget. When set to AUTO, machines will only display a Work Budget GUI when their maximum budget and budget gained per tick are not equal.")
        public WorkBudgetGUIState workBudgetGUI = WorkBudgetGUIState.AUTO;

        @Config.Name("Display Crafting Progress GUI")
        @Config.Comment("Whether or not to display the Inventory Crafting GUI elements for machines that do inventory crafting.")
        public boolean craftingGUI = true;

        public enum WorkBudgetGUIState {
            DISABLED, AUTO, ENABLED
        }

        @Config.Name("Directional Machines - Area Calculation")
        @Config.Comment({
                "Choose how the maximum area for a directional machine is calculated.",
                "Sum of Ranges (LEGACY) means the sum of the Length, Width, and Height cannot exceed 3 times the maximum range.",
                "So if you use a 7x7x7 Range Augment, for example, you could set the range to 1x1x19. 1+1+19 = 21 / 3 = 7",
                "Max Range means the range cannot exceed the maximum range in any direction. For 7x7x7, no number can exceed 7.",
                "Area means the maximum volume of blocks cannot exceed the volume indicated by the Range Augment, but the dimensions can differ. Additionally, the range in any direction cannot exceed three times the maximum range.",
                "For example, for a 7x7x7 Range Augment, you could set 7x5x9, or 3x3x21, but not 1x1x343."
        })
        public DirectionalArea area = DirectionalArea.AREA;

        public enum DirectionalArea {
            SUM_OF_RANGES,
            MAX_RANGE,
            AREA
        }
    }

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

        @Config.Name("Quenched Pearl")
        @Config.Comment({
                "Quenched Pearls can be thrown like Ender Pearls.",
                "Upon hitting a surface, they extinquish fires and quench lava."
        })
        public final QuenchedPearl quenchedPearl = new QuenchedPearl();

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

        @Config.Name("Positional Area Cards")
        @Config.Comment("Positional Area Cards can be installed into Positional Machines, and target an area of blocks rather than just one.")
        public final PositionalAreaCards positionalAreaCards = new PositionalAreaCards();

        @Config.Name("Relative Cards")
        @Config.Comment("Relative Cards can be installed into Positional Machines, and target a block or area at a certain position relative to the machine.")
        public final RelativeCards relativeCards = new RelativeCards();
    }

    public static class RelativeCards {
        @Config.Name("Allow Null Facing")
        @Config.Comment("When enabled, the \"null\" face will be one of the selectable faces.")
        public boolean allowNullFacing = false;
    }

    public static class PositionalAreaCards {
        @Config.Name("Available Tiers")
        @Config.Comment("Positional Area Cards are available in this many tiers.")
        @Config.RangeInt(min = 0)
        @Config.RequiresMcRestart
        public int availableTiers = 2;

        @Config.Name("Blocks per Tier")
        @Config.Comment("Positional Area Cards will be able to target this many blocks, per tier, in each direction. For example, a value of \"1\" results in a 3x3x3 area. One in each direction, including the origin block.")
        @Config.RangeInt(min = 0)
        // 3x3x3, 5x5x5, 7x7x7, 11x11x11, 17x17x17
        public int[] blocks = {1, 2};
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

    public static class QuenchedPearl {
        @Config.Name("Throwing consumes Pearl")
        @Config.Comment("When enabled, Quenched Pearls are always consumed on impact, whether they extinquish anything or not.")
        public boolean alwaysConsumed = false;

        @Config.Name("Extinguish Fire on Impact")
        @Config.Comment("When greater than zero, extinguish fire in an x block radius when thrown at a block.")
        @Config.RangeInt(min = 0)
        public int extinguishFire = 8;

        @Config.Name("Extinguishing Fire consumes Pearl")
        @Config.Comment("When enabled, extinguishing a fire block will consume the Quenched Pearl.")
        public boolean fireConsumes = false;

        @Config.Name("Quench Lava on Impact")
        @Config.Comment("When greater than zero, quench exposed lava in an x block radius when thrown at a block.")
        @Config.RangeInt(min = 0)
        public int quenchLava = 8;

        @Config.Name("Quenching Lava consumes Pearl")
        @Config.Comment("When enabled, quenching exposed lava will consume the Quenched Pearl.")
        public boolean lavaConsumes = true;

        @Config.Name("Protect Wielders from Burning")
        @Config.Comment("When enabled, entities holding a Quenched Pearl will not take fire damage. Players will also not remain lit on fire.")
        public boolean douseEntities = true;

        @Config.Name("Protect Wielders from Lava")
        @Config.Comment("When enabled, entities holding a Quenched Pearl will additionally not take damage from lava. Requires Protect Wielders from Burning")
        public boolean douseEntityLava = false;
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

        @Config.Name("Allow Capturing Bosses")
        @Config.Comment("When enabled, entities marked as bosses can be captured within Void Pearls.")
        public BossMode captureBosses = BossMode.DISABLED;

        @Config.Name("Enable Crystallization")
        @Config.Comment("Enable crafting Crystallized Void Pearls by throwing Void Pearls into an Ender Crystal.")
        public boolean enableCrystallization = true;

        @Config.Name("Display Filled Void Pearls in Creative")
        @Config.Comment("When enabled, Void Pearls will be listed in Creative's Misc. tab for all valid entities.")
        public boolean displayFilled = true;
    }

    public static class Upgrades {
        @Config.Name("Enable High-Tier Crafting Recipes")
        @Config.Comment("When enabled, the default unique recipes for crafting higher tiered machines will be loaded.")
        @Config.RequiresMcRestart
        public boolean enableHighTier = true;

        @Config.Name("Enable Upgrade Crafting Recipes")
        @Config.Comment("When enabled, recipes will be generated for crafting machines together with Upgrade Kits and Conversion Kits.")
        @Config.RequiresMcRestart
        public boolean enableCrafting = true;

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

        @Config.Name("Remote Side Augments")
        @Config.Comment("Remote Side Augments allow directional machines to target different sides of their targets.")
        public final FacingAugments facing = new FacingAugments();

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

        @Config.Name("Filter Augment")
        @Config.Comment("Filter Augments allow machines to filter items before allowing them into their internal buffers.")
        public final FilterAugments filter = new FilterAugments();
    }

    public static class FacingAugments {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with this augment.")
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;

        @Config.Name("Allow Null")
        @Config.Comment("When enabled, the \"null\" face will be one of the selectable faces.")
        public boolean allowNull = true;
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;

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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] budgetMultiplier = {1, 1, 1, 1, 1};

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] budgetAddition = {0, 0, 0, 0, 0};
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] budgetMultiplier = {1, 1, 1, 1, 1};

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] budgetAddition = {0, 0, 0, 0, 0};
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] budgetMultiplier = {1, 1, 1};

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] budgetAddition = {0, 0, 0};
    }

    public static class FilterAugments {
        @Config.Name("Available Tiers")
        @Config.Comment("Filter Augments will be made available in this many tiers. Limited by the number of available levels.")
        @Config.RangeInt(min = 0, max = Short.MAX_VALUE - 1)
        @Config.RequiresMcRestart
        public int availableTiers = 3;

        @Config.Name("Slots per Tier")
        @Config.Comment("Filter Augments will have this many available filter slots, per tier.")
        @Config.RangeInt(min = 1, max = 18)
        public int[] slotsPerTier = {6, 12, 18};

        @Config.Name("Allow Whitelist")
        @Config.Comment("Whether Filter Augments can be put into Whitelist mode, per tier.")
        public boolean[] allowWhitelist = {true, true, true};

        @Config.Name("Allow Ignore Metadata")
        @Config.Comment("Whether Filter Augments will have access to Ignore Metadata, per tier.")
        public boolean[] allowIgnoreMetadata = {true, true, true};

        @Config.Name("Allow Ore Dictionary")
        @Config.Comment("Whether Filter Augments will have access to Use Ore Dictionary, per tier.")
        public boolean[] allowOreDict = {false, true, true};

        @Config.Name("Allow Ignore NBT")
        @Config.Comment("Whether Filter Augments will have access to Ignore NBT, per tier.")
        public boolean[] allowIgnoreNBT = {false, true, true};

        @Config.Name("Allow Mod Matching")
        @Config.Comment("Whether Filter Augments will have access to Mod Matching, per tier.")
        public boolean[] allowMatchingMod = {false, false, true};

        @Config.Name("Allow Armor Filtering")
        @Config.Comment("Whether Filter Augments will have access to Armor Filtering, per tier.")
        public boolean[] allowArmor = {false, true, true};

        @Config.Name("Allow Voiding")
        @Config.Comment("Whether Filter Augments will have access to Voiding, per tier.")
        public boolean[] allowVoiding = {false, false, true};

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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] budgetMultiplier = {1, 1, 1};

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] budgetAddition = {0, 0, 0};

        @Config.Name("Off-Hand Filtering - Enabled")
        @Config.Comment("When this is enabled and you hold a filter in your off-hand, the filter will apply to items you pick up.")
        public boolean enableOffHand = true;

        @Config.Name("Off-Hand Filtering - Void Particle")
        @Config.Comment("When this is enabled and an item is voided due to a player holding a filter, a particle effect will appear where the item was.")
        public boolean offhandParticles = true;

        @Config.Name("Off-Hand Filtering - Void Sound Volume")
        @Config.Comment("When an item is voided due to a player holding a filter, a sound effect will play. Set to zero to disable. This is a server-side setting.")
        @Config.RangeDouble(min = 0)
        public double offhandVolume = 0D;
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

        @Config.Name("Blocks per Tier - Directional")
        @Config.Comment("Directional Machines will have their range increased by this many blocks in each direction. For example, a value of \"1\" results in a 3x3x3 area. One in each direction, including the origin block.")
        @Config.RangeInt(min = 0)
        // 3x3x3, 5x5x5, 7x7x7, 11x11x11, 17x17x17
        public int[] directionalBlocks = {1, 2, 3, 5, 8};

        @Config.Name("Blocks Per Tier")
        @Config.Comment("Each additional tier adds this many blocks to the range of positional machines.")
        @Config.RangeInt(min = 1, max = 128)
        public int blocksPerTier = 16;

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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] budgetMultiplier = {1, 1, 1, 1, 1};

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] budgetAddition = {0, 0, 0, 0, 0};
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;
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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double budgetMultiplier = 1;

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int budgetAddition = 0;
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
        @Config.Name("Positional Vaporizer")
        public PositionalVaporizer positional = new PositionalVaporizer();

        @Config.Name("Directional Vaporizer")
        public DirectionalVaporizer directional = new DirectionalVaporizer();

        @Config.Name("Use Experience Fluid")
        @Config.Comment("Whether or not Vaporizers should use some form of fluid experience for modules that require fuel.")
        @Config.RequiresWorldRestart
        public boolean useFluid = true;

        @Config.Name("Allow Inserting Fluid")
        @Config.Comment("Whether or not Vaporizers should accept input fluids. When disabled, Vaporizers can still produce fluid but not accept it.")
        public boolean acceptFluid = true;

        @Config.Name("Valid Fluids")
        @Config.Comment("These fluids can be used in Vaporizers. They convert into experience points and back at the rates below.")
        @Config.RequiresWorldRestart
        public String[] fluids = {
                "experience",
                "essence",
                "xpjuice"
        };

        @Config.Name("Fluid mB per Experience Point")
        @Config.Comment("Fluid experience should convert to experience points at this ratio.")
        @Config.RangeInt(min = 1)
        public int[] mbPerPoint = {20, 20, 20};

        @Config.Name("Allow Fluid Conversion")
        @Config.Comment("When enabled, clicking the fluid tank in Vaporizer will cycle between all the valid fluids as long as the converted fluid can be contained within the tank.")
        public boolean allowConversion = true;

        @Config.Name("Use Captured Entities as Fuel")
        @Config.Comment("When enabled, captured entities can be used as fuel for Vaporizer modules that require fuel.")
        public boolean useEntitiesFuel = true;

        @Config.Name("Captured Entity Fuel Addition")
        @Config.Comment("When burning a captured entity for fuel, this value will be added to their value to make up for the loss of drops.")
        @Config.RangeInt(min = 0)
        public int entityAddition = 1;

        @Config.Name("Baby Experience Multiplier")
        @Config.Comment("The base experience of baby entities is multiplied by this value when calculating the value of a captured mob. Set to zero to disable using baby entities as fuel.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double babyMultiplier = 0.5D;

        @Config.Name("Compatibility")
        @Config.Comment("Compatibility with other mods!")
        public VaporizerCompatibility compatibility = new VaporizerCompatibility();

        @Config.Name("Modules")
        @Config.Comment("Modules give a Vaporizer purpose. Without an installed module, they have no behavior.")
        public Modules modules = new Modules();
    }

    public static class PositionalVaporizer {
        @Config.Name("Cost per Block")
        @Config.Comment("The cost to work on entities at a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to work on entities at a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum cost to work on entities at a distant block will never exceed this value. Also the cost for interdimensional stuff.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class DirectionalVaporizer {
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

    public static class VaporizerCompatibility {
        @Config.Name("Allow Vanilla Spawn Eggs")
        @Config.Comment("Allow Vanilla Spawn Eggs to be used in Vaporizers for Duplication.")
        @Config.RequiresMcRestart
        public boolean useSpawnEggs = true;

        @Config.Name("Use Morbs")
        @Config.Comment("Allow the use of Morbs from Thermal Expansion in Vaporizers.")
        @Config.RequiresMcRestart
        public boolean useMorbs = true;

        @Config.Name("Use Mob Imprisonment Tools")
        @Config.Comment("Allow the use of Mob Imprisonment Tools from Industrial Foregoing in Vaporizers.")
        @Config.RequiresMcRestart
        public boolean useMITs = true;

        @Config.Name("Use Cyclic Monster Balls")
        @Config.Comment("Allow the use of Monster Balls from Cyclic in Vaporizers.")
        @Config.RequiresMcRestart
        public boolean useCyclicNets = true;
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

        @Config.Name("Fishing Module")
        @Config.Comment("Fishing Modules will fish for things if there is water within the Vaporizer's working area.")
        public FishingModule fishing = new FishingModule();

        @Config.Name("Theoretical Slaughter Module")
        @Config.Comment("The Theoretical Slaughter Module kills theoretical copies of an entity.")
        public TheoreticalSlaughterModule theoreticalSlaughter = new TheoreticalSlaughterModule();

        @Config.Name("Teleportation Module")
        @Config.Comment("Teleportation Modules transport all entities within the Vaporizer's working area to a set location.")
        public TeleportModule teleport = new TeleportModule();

        @Config.Name("Launch Module")
        @Config.Comment("Launch Modules impart motion to all entities within the Vaporizer's working area.")
        public LaunchModule launch = new LaunchModule();
    }

    public static class LaunchModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Budget per Entity")
        @Config.Comment("Use this much action budget for each entity launched.")
        @Config.RangeInt(min = 0)
        public int budget = 10;

        @Config.Name("Energy per Entity")
        @Config.Comment("Use this much energy for each entity launched.")
        @Config.RangeInt(min = 0)
        public int energy = 250;

        @Config.Name("Energy per 1 Velocity")
        @Config.Comment("Use this much energy per unit of velocity imparted upon an entity.")
        @Config.RangeDouble(min = 0)
        public double energyPerUnit = 25D;

        @Config.Name("Energy per Velocity Squared")
        @Config.Comment("Use this much energy per unity of velocity squared imparted upon an entity.")
        @Config.RangeDouble(min = 0)
        public double energyPerUnitSquared = 5D;

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
        @Config.Comment("When enabled, only living entities will be launched. Otherwise, ALL entities can be targeted.")
        @Config.RequiresWorldRestart
        public boolean livingOnly = false;

        @Config.Name("Target Players")
        @Config.Comment("When enabled, players can be launched.")
        @Config.RequiresWorldRestart
        public boolean targetPlayers = true;

        @Config.Name("Target Bosses")
        @Config.Comment("When enabled, entities tagged as bosses can be launched.")
        @Config.RequiresWorldRestart
        public boolean targetBosses = true;

        @Config.Name("Base Fuel")
        @Config.Comment("Launching an entity will use this much fuel, in addition to energy. The fuel value will be rounded up to the nearest integer.")
        @Config.RangeDouble(min = 0)
        public double baseFuel = 0;

        @Config.Name("Allow Fall Damage Negation")
        @Config.Comment("When enabled, players can enable a module setting to prevent fall damage to launched entities.")
        public boolean allowFallProtect = true;

        @Config.Name("Fall Negation Energy per Entity")
        @Config.Comment("When fall negation is enabled, this much extra energy will be used per entity.")
        @Config.RangeInt(min = 0)
        public int fallProtectEnergy = 1000;

        @Config.Name("Fall Negation Budget per Entity")
        @Config.Comment("When fall negation is enabled, this much extra budget will be used per entity.")
        @Config.RangeInt(min = 0)
        public int fallProtectBudget = 0;
    }

    public static class CaptureModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Budget per Capture")
        @Config.Comment("Use this much action budget to capture a single entity.")
        @Config.RangeInt(min = 0)
        public int budget = 20;

        @Config.Name("Energy per Capture")
        @Config.Comment("Use this much energy to capture a single entity.")
        @Config.RangeInt(min = 0)
        public int entityEnergy = 1000;

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

        @Config.Name("Require Crystallized Void Pearl")
        @Config.Comment("When enabled, this module will only accept Crystallized Void Pearls as input to set the target entity.")
        @Config.RequiresWorldRestart
        public boolean requireCrystallizedVoidPearls = false;

        @Config.Name("Use CheckSpawn Event")
        @Config.Comment("When enabled, we call ForgeEventFactory.canEntitySpawn before actually spawning an entity to make sure no other mods want to stop us.")
        public boolean useCheckSpawn = true;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for vaporizers using this module.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for vaporizers using this module.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This module will drain this amount of RF/t from non-disabled vaporizers using them.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;

        @Config.Name("Exact Copy - Energy Addition")
        @Config.Comment("Add this to the base cost per target for vaporizers using this module when Exact Copies is enabled.")
        @Config.RequiresWorldRestart
        public int exactEnergyAddition = 0;

        @Config.Name("Exact Copy - Energy Drain per Tick")
        @Config.Comment("This module will drain this amount of RF/t from non-disabled vaporizers using them when Exact Copies is enabled.")
        @Config.RequiresWorldRestart
        public int exactEnergyDrain = 0;

        @Config.Name("Exact Copy - Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for vaporizers using this module when Exact Copies is enabled.")
        @Config.RequiresWorldRestart
        @Config.RangeDouble(min = 0)
        public double exactEnergyMultiplier = 1;

        @Config.Name("Exact Copy - Enable")
        @Config.Comment("When enabled, exact copies of entities can be spawned.")
        @Config.RequiresWorldRestart
        public boolean allowExact = true;

        // Entity - Budget

        @Config.Name("Budget - Base per Entity")
        @Config.Comment("Use this much action budget to clone an entity.")
        @Config.RangeInt(min = 0)
        public int budgetBase = 20;

        @Config.Name("Budget - Exact Copy - Additional")
        @Config.Comment("Use this much additional action budget per entity when in Exact Copy mode.")
        @Config.RangeInt(min = 0)
        public int budgetExact = 40;

        @Config.Name("Budget - Exact Copy - Multiplier")
        @Config.Comment("Budget = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double budgetExactFactor = 0;

        @Config.Name("Budget - Experience Factor")
        @Config.Comment("Budget = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double budgetPerExp = 0;

        @Config.Name("Budget - Health Factor")
        @Config.Comment("Budget = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double budgetPerHealth = 0;

        // Entity - Energy

        @Config.Name("Energy - Base per Entity")
        @Config.Comment("Consume this amount of base energy per entity spawned.")
        @Config.RangeInt(min = 0)
        public int energyBase = 1000;

        @Config.Name("Energy - Exact Copy - Additional")
        @Config.Comment("Consume this amount of additional energy per entity when in Exact Copy mode.")
        @Config.RangeInt(min = 0)
        public int energyExact = 1000;

        @Config.Name("Energy - Exact Copy - Multiplier")
        @Config.Comment("Energy = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double energyExactFactor = 0;

        @Config.Name("Energy - Experience Factor")
        @Config.Comment("Energy = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double energyPerExp = 0;

        @Config.Name("Energy - Health Factor")
        @Config.Comment("Energy = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double energyPerHealth = 0;

        // Entity - Fuel

        @Config.Name("Require Fuel Cost")
        @Config.Comment({
                "When enabled, entities cannot be cloned if their fuel cost is calculated to zero, even if they have an energy cost as well.",
                "This does not include the base fuel per entity when considering if an entity can be spawned. Only the health and experience based value."
        })
        @Config.RequiresWorldRestart
        public boolean requireFuel = false;

        @Config.Name("Fuel - Base per Entity")
        @Config.Comment("Consume this amount of base fuel per entity spawned.")
        @Config.RangeInt(min = 0)
        public int fuelBase = 0;

        @Config.Name("Fuel - Exact Copy - Additional")
        @Config.Comment("Consume this amount of additional fuel per entity when in Exact Copy mode.")
        @Config.RangeInt(min = 0)
        public int fuelExact = 0;

        @Config.Name("Fuel - Exact Copy - Multiplier")
        @Config.Comment("Fuel = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double fuelExactFactor = 2;

        @Config.Name("Fuel - Experience Factor")
        @Config.Comment("Fuel = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double fuelPerExp = 2;

        @Config.Name("Fuel - Health Factor")
        @Config.Comment("Fuel = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double fuelPerHealth = 0;

        // Experience Values

        @Config.Name("Animal Base Experience")
        @Config.Comment("Animal entities do not expose experience in a standard way. Because of that, each one will be worth this number of experience points to spawn.")
        @Config.RangeInt(min = 0)
        @Config.RequiresMcRestart
        public int animalBaseExp = 3;

        @Config.Name("Exact Copy - Use Baby Multiplier")
        @Config.Comment("When enabled, the baby experience multiplier is applied when spawning exact copies of baby entities.")
        @Config.RequiresWorldRestart
        public boolean exactBaby = false;

        // Spawning

        @Config.Name("Maximum Entity - Count")
        @Config.Comment("No more than this number of the target entity should exist in the immediate area. If they do, temporarily stop spawning.")
        @Config.RangeInt(min = 1)
        public int maxCount = 20;

        @Config.Name("Maximum Entity - Range")
        @Config.Comment("Check within this many blocks of the target spawn location.")
        @Config.RangeInt(min = 0)
        public int maxRange = 10;

        @Config.Name("Randomize Spawn Position")
        @Config.Comment("When enabled, entities will be spawned at a random position within the target block.")
        public boolean randomSpawn = true;

        @Config.Name("Use Spawn Particles")
        @Config.Comment("When enabled, living entities will be instructed to create their spawn particles when spawned by the Vaporizer.")
        public boolean useSpawnParticles = true;

        @Config.Name("Allow Cloning Babies")
        @Config.Comment("Whether or not the module should accept baby entities as entities to clone. If set to BABY_CLONES, all spawned entites will be babies.")
        public BabyCloningMode babyMode = BabyCloningMode.BABY_CLONES;

        @Config.Name("Allow Cloning Bosses")
        @Config.Comment("Whether or not the module should accept boss entities for cloning.")
        public BossMode bossMode = BossMode.CREATIVE_ONLY;
    }

    public static class TeleportModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Offset Target if Solid")
        @Config.Comment("Check the target block before teleporting an entity and, if it's solid, offset the target by one block in the facing direction.")
        public boolean offsetTargetIfSolid = true;

        @Config.Name("Use Secondary Range Augment")
        @Config.Comment("By default, the Teleport Module uses the Range Augment installed into the Vaporizer's Augment slots. If this setting is enabled, it instead requires a Range Augment to be installed to the first input slot of the Vaporizer.")
        @Config.RequiresWorldRestart
        public boolean ownRangeAugment = false;

        @Config.Name("Budget per Entity")
        @Config.Comment("Use this much action budget for each entity teleported.")
        @Config.RangeInt(min = 0)
        public int budget = 20;

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

        @Config.Name("Target Bosses")
        @Config.Comment("When enabled, entities marked as bosses can be transported.")
        @Config.RequiresWorldRestart
        public boolean targetBosses = true;

        @Config.Name("Base Fuel")
        @Config.Comment("Teleporting an entity will use this much fuel, in addition to energy. The fuel value will be rounded up to the nearest integer.")
        @Config.RangeDouble(min = 0)
        public double baseFuel = 0;

        @Config.Name("Fuel per Block")
        @Config.Comment("The fuel cost to teleport entities to a distant block goes up by this amount per block, linearly with distance.")
        @Config.RangeDouble(min = 0)
        public double fuelPerBlock = 0;

        @Config.Name("Fuel per Block Squared")
        @Config.Comment("The fuel cost to teleport entities to a distant block goes up by this amount per block as a square with distance.")
        @Config.RangeDouble(min = 0)
        public double fuelPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Fuel")
        @Config.Comment("The maximum fuel cost to teleport entities to a distant block will never exceed this value. Also the cost for interdimensional teleportation.")
        @Config.RangeDouble(min = 0)
        public double fuelInterdimensional = 0;

        @Config.Name("Base Energy")
        @Config.Comment("Teleporting an entity will use this much base energy, no matter the distance.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int baseEnergy = 1000;

        @Config.Name("Cost per Block")
        @Config.Comment("The energy cost to teleport entities to a distant block goes up by this amount per block, linearly with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlock = 0;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The energy cost to teleport entities to a distant block goes up by this amount per block as a square with distance.")
        @Config.RangeInt(min = 0)
        public int costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum energy cost to teleport entities to a distant block will never exceed this value. Also the cost for interdimensional teleportation.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class AnimalSlaughterModule {
        @Config.Name("Enable Animal Slaughter Module")
        @Config.Comment("Whether or not the item should be added to the game.")
        @Config.RequiresMcRestart
        public boolean enabled = true;

        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Budget per Entity")
        @Config.Comment("Use this much action budget for each entity attacked.")
        @Config.RangeInt(min = 0)
        public int budget = 25;

        @Config.Name("Energy per Entity")
        @Config.Comment("Use this much base energy for each entity attacked.")
        @Config.RangeInt(min = 0)
        public int energy = 1000;

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

        @Config.Name("Pink Slime per HP - Animal")
        @Config.Comment("This many mB of Pink Slime will be created per point of health damage dealt to animals.")
        @Config.RangeDouble(min = 0)
        public double animalSlime = 8;

        @Config.Name("Pink Slime per HP - Other")
        @Config.Comment("This many mB of Pink Slime will be created per point of health damage dealt to non-animals.")
        @Config.RangeDouble(min = 0)
        public double otherSlime = 1;

        @Config.Name("Liquid Meat per HP - Animal")
        @Config.Comment("This many mB of Liquid Meat will be created per point of health damage dealt to animals.")
        @Config.RangeDouble(min = 0)
        public double animalMeat = 5;

        @Config.Name("Liquid Meat per HP - Other")
        @Config.Comment("This many mB of Liquid Meat will be created per point of health damage dealt to non-animals.")
        @Config.RangeDouble(min = 0)
        public double otherMeat = 5;
    }

    public static class SlaughterModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Budget per Entity")
        @Config.Comment("Use this much action budget for each entity attacked.")
        @Config.RangeInt(min = 0)
        public int budget = 25;

        @Config.Name("Budget per Entity - Using Weapon")
        @Config.Comment("Use this much additional action budget per entity when using a weapon.")
        @Config.RangeInt(min = 0)
        public int budgetWeapon = 0;

        @Config.Name("Budget per Entity - As Player")
        @Config.Comment("Use this much additional action budget per entity when simulating a player.")
        @Config.RangeInt(min = 0)
        public int budgetPlayer = 25;

        @Config.Name("Energy per Entity")
        @Config.Comment("Use this much base energy for each entity attacked.")
        @Config.RangeInt(min = 0)
        public int energy = 1000;

        @Config.Name("Energy per Entity - Use Weapon")
        @Config.Comment("Use this much additional energy per entity attacked when using a weapon.")
        @Config.RangeInt(min = 0)
        public int energyWeapon = 500;

        @Config.Name("Energy per Entity - As Player")
        @Config.Comment("Use this much additional energy per entity attacked when simulating a player.")
        @Config.RangeInt(min = 0)
        public int energyPlayer = 1000;

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

        @Config.Name("Require Weapons")
        @Config.Comment("When enabled, the Slaughter Module will not run without a weapon in its Input slot.")
        @Config.RequiresWorldRestart
        public boolean requireWeapon = false;

        @Config.Name("Weapon Blacklist")
        @Config.Comment("Items added to this list cannot be used as a weapon within a Vaporizer. All items from a mod can be filtered with \"modid:*\". Metadata can be specified after a @. Example: \"minecraft:wool@2\"")
        @Config.RequiresWorldRestart
        public String[] weaponList = {};

        @Config.Name("Weapon Whitelist")
        @Config.Comment("When true, the blacklist will instead be treated as a whitelist.")
        @Config.RequiresWorldRestart
        public boolean weaponIsWhitelist = false;

        @Config.Name("Enable As Player Mode")
        @Config.Comment("In As Player Mode, the Slaughter Module will simulate using its weapon in the same way a player does, allowing normal weapon things to happen, such as Tinker Tools gaining experience.")
        @Config.RequiresWorldRestart
        public boolean enableAsPlayer = true;

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

        @Config.Name("Never Void Player Drops")
        @Config.Comment("When enabled, items dropped by killed players will never be voided by a Vaporizer.")
        public boolean neverVoidPlayers = true;
    }

    public static class FishingModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Allow Fishing Rod")
        @Config.Comment("When enabled, the Fishing Module will allow a Fishing Rod to be placed in its input slot. It will use the fishing rod when fishing, applying looting and speed from the rod.")
        @Config.RequiresWorldRestart
        public boolean allowRod = true;

        @Config.Name("Require Fishing Rod")
        @Config.Comment("When enabled, the Fishing Module will not run without a Fishing Rod in its input slot.")
        @Config.RequiresWorldRestart
        public boolean requireRod = false;

        @Config.Name("Maximum Exp per Catch")
        @Config.Comment("At most this many experience points will be generated per catch. If set to zero, experience will not be generated.")
        @Config.RangeInt(min = 0)
        public int maxExp = 3;

        @Config.Name("Collect Drops - Default")
        @Config.Comment("0 = Ignore Drops, 1 = Collect Drops till Full, 2 = Collect All Drops, 3 = Void Drops")
        public int collectDrops = 1;

        @Config.Name("Collect Drops - Minimum")
        @Config.Comment("0 = Ignore Drops, 1 = Collect Drops till Full, 2 = Collect All Drops, 3 = Void Drops")
        public int collectDropsMinimum = 0;

        @Config.Name("Collect Experience - Default")
        @Config.Comment({
                "Experience is not collected when drops are not collected.",
                "0 = Ignore Experience, 1 = Collect Experience till Full, 2 = Collect All Experience, 3 = Void Experience"
        })
        public int collectExperience = 2;

        @Config.Name("Collect Experience - Minimum")
        @Config.Comment("0 = Ignore Experience, 1 = Collect Experience till Full, 2 = Collect All Experience, 3 = Void Experience")
        public int collectExperienceMinimum = 0;

        @Config.Name("Max Simultaneous Casts")
        @Config.Comment("Do not allow a Fishing Module to cast more than this many lines at one time.")
        @Config.RangeInt(min = 1)
        public int maxCasts = 50;

        @Config.Name("Budget per Cast")
        @Config.Comment("Use this much action budget per cast line.")
        @Config.RangeInt(min = 0)
        public int budget = 15;

        @Config.Name("Budget per Cast - Using Rod")
        @Config.Comment("Use this much additional action budget per cast line when a rod is present.")
        @Config.RangeInt(min = 0)
        public int budgetRod = 10;

        @Config.Name("Energy per Cast")
        @Config.Comment("Use this much base energy per cast line.")
        @Config.RangeInt(min = 0)
        public int energy = 1000;

        @Config.Name("Energy per Cast - Using Rod")
        @Config.Comment("Use this much additional energy per cast line when a rod is present.")
        @Config.RangeInt(min = 0)
        public int energyRod = 500;

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

    public static class TheoreticalSlaughterModule {
        @Config.Name("Required Level")
        @Config.Comment("Vaporizers must be at least this level in order to use this module.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

        @Config.Name("Require Crystallized Void Pearl")
        @Config.Comment("When enabled, this module will only accept Crystallized Void Pearls as input to set the target entity.")
        @Config.RequiresWorldRestart
        public boolean requireCrystallizedVoidPearls = true;

        @Config.Name("Allow Looting")
        @Config.Comment("Allow Theoretical Slaughter Modules to be enchanted with Looting. To be applied theoretically.")
        @Config.RequiresWorldRestart
        public boolean allowLooting = true;

        @Config.Name("Energy Addition")
        @Config.Comment("Add this to the base cost per target for vaporizers using this module.")
        @Config.RequiresWorldRestart
        public int energyAddition = 0;

        @Config.Name("Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for vaporizers using this module.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double energyMultiplier = 1;

        @Config.Name("Energy Drain per Tick")
        @Config.Comment("This module will drain this amount of RF/t from non-disabled vaporizers using them.")
        @Config.RequiresWorldRestart
        public int energyDrain = 0;

        @Config.Name("Exact Copy - Energy Addition")
        @Config.Comment("Add this to the base cost per target for vaporizers using this module when Exact Copies is enabled.")
        @Config.RequiresWorldRestart
        public int exactEnergyAddition = 0;

        @Config.Name("Exact Copy - Energy Multiplier")
        @Config.Comment("Multiply the base cost per target by this much for vaporizers using this module when Exact Copies is enabled.")
        @Config.RequiresWorldRestart
        @Config.RangeDouble(min = 0)
        public double exactEnergyMultiplier = 1;

        @Config.Name("Exact Copy - Energy Drain per Tick")
        @Config.Comment("This module will drain this amount of RF/t from non-disabled vaporizers using them when Exact Copies is enabled.")
        @Config.RequiresWorldRestart
        public int exactEnergyDrain = 0;

        @Config.Name("Exact Copy - Enable")
        @Config.Comment("When enabled, exact copies of entities can be spawned.")
        @Config.RequiresWorldRestart
        public boolean allowExact = true;

        // Entity - Budget

        @Config.Name("Budget - Base per Entity")
        @Config.Comment("Use this much action budget for each entity theoretically killed.")
        @Config.RangeInt(min = 0)
        public int budgetBase = 50;

        @Config.Name("Budget - Exact Copy - Additional")
        @Config.Comment("Use this much additional action budget per entity when in Exact Copy mode.")
        @Config.RangeInt(min = 0)
        public int budgetExact = 50;

        @Config.Name("Budget - Exact Copy - Multiplier")
        @Config.Comment("Budget = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double budgetExactFactor = 0;

        @Config.Name("Budget - Experience Factor")
        @Config.Comment("Budget = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double budgetPerExp = 0;

        @Config.Name("Budget - Health Factor")
        @Config.Comment("Budget = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double budgetPerHealth = 0;

        // Entity - Energy

        @Config.Name("Energy - Base per Entity")
        @Config.Comment("Use this much base energy for each entity theoretically killed.")
        @Config.RangeInt(min = 0)
        public int energyBase = 2000;

        @Config.Name("Energy - Exact Copy - Additional")
        @Config.Comment("Use this much additional energy per entity when in Exact Copy mode.")
        @Config.RangeInt(min = 0)
        public int energyExact = 1000;

        @Config.Name("Energy - Exact Copy - Multiplier")
        @Config.Comment("Energy = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double energyExactFactor = 0;

        @Config.Name("Energy - Experience Factor")
        @Config.Comment("Energy = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double energyPerExp = 0;

        @Config.Name("Energy - Health Factor")
        @Config.Comment("Energy = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double energyPerHealth = 0;

        // Entity - Fuel

        @Config.Name("Require Fuel Cost")
        @Config.Comment({
                "When enabled, entities cannot be theoretically killed if their fuel cost is calculated to zero, even if they have an energy cost as well.",
                "This does not include the base fuel per entity when considering if an entity can be theoretically killed. Only the health and experience based value."
        })
        @Config.RequiresWorldRestart
        public boolean requireFuel = false;

        @Config.Name("Fuel - Base per Entity")
        @Config.Comment("Consume this amount of base fuel per entity spawned.")
        @Config.RangeInt(min = 0)
        public int fuelBase = 0;

        @Config.Name("Fuel - Exact Copy - Additional")
        @Config.Comment("Consume this amount of additional fuel per entity when in Exact Copy mode.")
        @Config.RangeInt(min = 0)
        public int fuelExact = 0;

        @Config.Name("Fuel - Exact Copy - Multiplier")
        @Config.Comment("Fuel = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double fuelExactFactor = 2;

        @Config.Name("Fuel - Experience Factor")
        @Config.Comment("Fuel = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double fuelPerExp = 2;

        @Config.Name("Fuel - Health Factor")
        @Config.Comment("Fuel = Base + Exact Addition + Ceiling(Exact Multiplier * (Base Experience * Experience Factor + Max Health * Health Factor))")
        @Config.RangeDouble(min = 0)
        public double fuelPerHealth = 0;

        // Experience Values

        @Config.Name("Exact Copy - Use Baby Multiplier")
        @Config.Comment("When enabled, the baby experience multiplier is applied when theoretically killing exact copies of baby entities.")
        @Config.RequiresWorldRestart
        public boolean exactBaby = false;

        // Other

        @Config.Name("Allow Theoretically Killing Babies")
        @Config.Comment("Whether or not the module should accept baby entities as entities to theoretically kill. If set to BABY_CLONES, all theoretically killed entites will be babies.")
        public BabyCloningMode babyMode = BabyCloningMode.BABY_CLONES;

        @Config.Name("Allow Theoretically Killing Bosses")
        @Config.Comment("Whether or not the module should accept boss entities for theoretically killing.")
        public BossMode bossMode = BossMode.CREATIVE_ONLY;
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
        @Config.Comment({
                "Desublimators should not interact with blocks in this list. Example: minecraft:wool",
                "Entire mods can be blacklisted by using an asterisk. Example: refinedstorage:*"
        })
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

        @Config.Name("AE Bus Augment")
        @Config.Comment("The AE Bus Augment allows machines to directly interface with AE networks.")
        public AEBusAugment aeBusAugment = new AEBusAugment();

        @Config.Name("Colors - Enabled")
        @Config.Comment("When enabled, the AE Network machines will have configurable colors. When disabled, AE Network machines ignore color.")
        @Config.RequiresWorldRestart
        public boolean enableColor = true;

        @Config.Name("Colors - Wireless Only")
        @Config.Comment("When enabled, the AE Network machines will accept direct connections from any color but will only connect wirelessly to their set color.")
        @Config.RequiresWorldRestart
        public boolean colorsWireless = true;

        @Config.Name("Channels - Machines Use a Channel")
        @Config.Comment("When enabled, AE Network machines use a single channel. When disabled, AE Network machines use no channels.")
        @Config.RequiresWorldRestart
        public boolean requireChannels = false;
    }

    public static class AEBusAugment {
        @Config.Name("Required Level")
        @Config.Comment("Machines must be at least this level in order to be augmented with this augment.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int requiredLevel = 0;

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

        @Config.Name("Budget Multiplier")
        @Config.Comment("Multiply the base budget cost per action by this much for machines with this augment installed.")
        @Config.RangeDouble(min = 0)
        @Config.RequiresWorldRestart
        public double[] budgetMultiplier = {1, 1, 1, 1, 1};

        @Config.Name("Budget Addition")
        @Config.Comment("Add this to the base budget cost per action for machines with this augment installed.")
        @Config.RequiresWorldRestart
        public int[] budgetAddition = {0, 0, 0, 0, 0};

        @Config.Name("Available Tiers")
        @Config.Comment("AE Bus Augments should be available in this many tiers.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int tiers = 5;

        @Config.Name("Tick Rate per Level")
        @Config.Comment("AE Bus Augments should tick once every this many ticks.")
        @Config.RangeInt(min = 0, max = 100)
        @Config.RequiresWorldRestart
        public int[] tickRate = {20, 16, 10, 6, 4};

        @Config.Name("Items - Maximum Transfer per Level")
        @Config.Comment("Transfer up to this many items per tick per level.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int[] itemsPerTick = {8, 16, 32, 64, 96};

        @Config.Name("Fluid - Maximum Transfer per Level")
        @Config.Comment("Transfer up to this many mB of fluid per tick per level.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int[] fluidPerTick = {500, 2000, 4000, 8000, 12000};

        @Config.Name("Energy - Maximum RF per Level")
        @Config.Comment("Transfer up to this much RF per tick per level.")
        @Config.RangeInt(min = 0)
        @Config.RequiresWorldRestart
        public int[] energyPerTick = {500, 2000, 8000, 16000, 32000};
    }

    public static class PositionalAENetwork {
        @Config.Name("Channels - Dense Cable")
        @Config.Comment("When enabled, AE Network machines act like a Dense Cable providing 32 channels rather than merely 8.")
        @Config.RequiresWorldRestart
        public boolean[] dense = {false, false, true, true, true};

        @Config.Name("Base Energy")
        @Config.Comment("Positional AE Network machines should use this much energy per level.")
        @Config.RangeInt(min = 0)
        public int[] baseEnergy = {1, 1, 2, 2, 4};

        @Config.Name("Cost per Block")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeDouble(min = 0)
        public double costPerBlock = 1;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeDouble(min = 0)
        public double costPerBlockSquared = 0;

        @Config.Name("Maximum / Interdimensional Cost")
        @Config.Comment("The maximum cost to connect a distant block will never exceed this value. Also the cost for interdimensional connections.")
        @Config.RangeInt(min = 0)
        public int costInterdimensional = 1000;
    }

    public static class DirectionalAENetwork {
        @Config.Name("Channels - Dense Cable")
        @Config.Comment("When enabled, AE Network machines act like a Dense Cable providing 32 channels rather than merely 8.")
        @Config.RequiresWorldRestart
        public boolean[] dense = {false, false, true, true, true};

        @Config.Name("Base Energy")
        @Config.Comment("Directional AE Network machines should use this much energy per level.")
        @Config.RangeInt(min = 0)
        public int[] baseEnergy = {1, 1, 2, 2, 4};

        @Config.Name("Cost per Block")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick, linearly with distance.")
        @Config.RangeDouble(min = 0)
        public double costPerBlock = 1;

        @Config.Name("Cost per Block Squared")
        @Config.Comment("The cost to connect a distant block goes up by this amount per tick as a square with distance.")
        @Config.RangeDouble(min = 0)
        public double costPerBlockSquared = 0;

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
        @Config.Comment("Machines should process this many tile entities and/or items per tick, until they run out of energy.")
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

        @Config.Name("Allow Holding Work Glasses")
        @Config.Comment("When enabled, merely holding Work Glasses is enough for them to take effect. When disabled, Work Glasses must be equipped.")
        public boolean allowHoldingGlasses = true;

        @Config.Name("Work Particles - Enabled")
        @Config.Comment("When enabled, active machines will draw particles connecting them to their targets. These particles are only visible while wearing Work Glasses.")
        public boolean particlesEnabled = true;

        @Config.Name("Work Particles - Cost")
        @Config.Comment("Rendering a work effect costs this much budget.")
        @Config.RangeInt(min = 1)
        public int particlesCost = 5;

        @Config.Name("Work Particles - Maximum Budget")
        @Config.Comment("Machines can store up to this much budget for work effects.")
        @Config.RangeInt(min = 0)
        public int particlesMax = 100;

        @Config.Name("Work Particles - Budget Per Tick")
        @Config.Comment("Machines will generate this much budget per tick for work effects.")
        @Config.RangeInt(min = 0)
        public int particlePerTick = 1;
    }

    public static class Plugins {
        @Config.Name("Applied Energistics")
        @Config.Comment("Configuration for the Applied Energistics Plugin.")
        public AppliedEnergistics appliedEnergistics = new AppliedEnergistics();

        @Config.Name("Refined Storage")
        @Config.Comment("Configuration for the Refined Storage Plugin.")
        public RefinedStorage refinedStorage = new RefinedStorage();

        @Config.Name("Industrial Foregoing")
        @Config.Comment("Configuration for the Industrial Foregoing plugin.")
        public IndustrialForegoing industrialForegoing = new IndustrialForegoing();
    }

    public static class IndustrialForegoing {
        @Config.Name("Animal Slaughter Module")
        @Config.Comment("Configuration for the Animal Slaughter Module for Vaporizers.")
        public AnimalSlaughterModule animalSlaughterModule = new AnimalSlaughterModule();
    }

    public enum BabyCloningMode {
        DISALLOW, ALLOW, BABY_CLONES
    }

    public enum BossMode {
        DISABLED,
        ENABLED,
        CREATIVE_ONLY
    }

    @Mod.EventBusSubscriber(modid = WirelessUtils.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if ( event.getModID().equals(WirelessUtils.MODID) ) {
                ConfigManager.sync(WirelessUtils.MODID, Config.Type.INSTANCE);

                ItemSlaughterModule.weaponList.clear();
                ItemSlaughterModule.weaponList.addInput(ModConfig.vaporizers.modules.slaughter.weaponList);
            }
        }
    }
}
