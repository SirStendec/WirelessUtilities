package com.lordmau5.wirelessutils.tile.base;

import cofh.core.init.CoreProps;
import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.TimeTracker;
import cofh.core.util.helpers.SecurityHelper;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.utils.ChunkManager;
import com.lordmau5.wirelessutils.utils.ItemStackHandler;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import joptsimple.internal.Strings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.ITickable;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class TileEntityBaseMachine extends TileEntityBaseArea implements ITileInfoProvider, IUpgradeable, ILevellingBlock, ITickable {

    /* Security */
    protected GameProfile owner = CoreProps.DEFAULT_OWNER;
    private Map<Integer, Tuple<Integer, ChunkPos>> loadedChunks = new Int2ObjectOpenHashMap<>();
    private Map<BlockPosDimension, Integer> loadedByPos = new Object2IntOpenHashMap<>();

    /* Inventory */
    protected ItemStackHandler itemStackHandler;
    protected ItemStack[] augments = new ItemStack[0];

    /* Levels */
    protected Level level = Level.getMinLevel();
    protected boolean isCreative = false;
    protected boolean wasDismantled = false;

    /* Throttling */
    protected final TimeTracker tracker = new TimeTracker();
    private boolean activeCooldown = false;
    protected byte inactiveTicks = 0;


    /* Energy */
    protected int baseEnergy = 0;
    protected double augmentMultiplier = 1;
    protected int augmentEnergy = 0;
    protected int augmentDrain = 0;

    /* Budget */
    protected double augmentBudgetMult = 1;
    protected int augmentBudgetAdd = 0;


    /* Comparator-ing */
    private int comparatorState = 0;

    /* 1.7 to 1.8 placeholder */
    private boolean resetLevelState = false;

    @Override
    public void update() {
        if ( world != null && !world.isRemote && !resetLevelState ) {
            IBlockState state = world.getBlockState(pos);
            if ( state.getValue(Properties.LEVEL) != 0 ) {
                world.setBlockState(pos, state.withProperty(Properties.LEVEL, 0));
            }

            resetLevelState = true;
        }
    }

    /* --- */

    @Override
    public String getTileName() {
        Machine machine = this.getClass().getAnnotation(Machine.class);
        if ( machine != null )
            return "tile." + WirelessUtils.MODID + "." + machine.name() + ".name";

        return null;
    }

    /* Comparator Logic */

    @Override
    public int getComparatorInputOverride() {
        return comparatorState;
    }

    public int calculateComparatorInput() {
        return 0;
    }

    public void runTrackers() {
        int comparatorState = calculateComparatorInput();
        if ( comparatorState != this.comparatorState ) {
            this.comparatorState = comparatorState;
            callNeighborTileChange();
        }
    }

    public void updateTrackers() {
        if ( timeCheck() )
            runTrackers();
    }

    /* Levels and Augments */

    public Level getLevel() {
        return level;
    }

    public boolean isCreative() {
        return isCreative;
    }

    public void setLevel(Level level) {
        this.level = level;
        this.isCreative = level.isCreative;

        int slots = getNumAugmentSlots();
        if ( augments == null || slots != augments.length ) {
            ItemStack[] oldAugments = augments;

            augments = new ItemStack[slots];
            Arrays.fill(augments, ItemStack.EMPTY);

            if ( oldAugments != null && oldAugments.length > 0 )
                System.arraycopy(oldAugments, 0, augments, 0, Math.min(slots, oldAugments.length));
        }

        if ( world != null && !world.isRemote ) {
            IBlockState state = world.getBlockState(pos);
            if ( state.getValue(Properties.LEVEL) != 0 ) {
                world.setBlockState(pos, state.withProperty(Properties.LEVEL, 0));
            }
        }

        updateLevel();
    }

    @Override
    public boolean canUpgrade(ItemStack upgrade) {
        Item item = upgrade.getItem();
        if ( item == ModItems.itemLevelUpgrade ) {
            Level currentLevel = getLevel();
            Level newLevel = ModItems.itemLevelUpgrade.getLevel(upgrade);

            return newLevel.ordinal() == currentLevel.ordinal() + 1;

        } else if ( item == ModItems.itemConversionUpgrade ) {
            Level currentLevel = getLevel();
            Level newLevel = ModItems.itemLevelUpgrade.getLevel(upgrade);

            return newLevel.ordinal() > currentLevel.ordinal();

        } else if ( item instanceof ItemAugment ) {
            if ( !isValidAugment(upgrade) )
                return false;

            ItemStack[] augments = getAugmentSlots();
            if ( augments == null )
                return false;

            for (int i = 0; i < augments.length; i++) {
                if ( augments[i].isEmpty() && isValidAugment(i, upgrade) )
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean installUpgrade(ItemStack upgrade) {
        Item item = upgrade.getItem();
        if ( item == ModItems.itemLevelUpgrade ) {
            Level currentLevel = getLevel();
            Level newLevel = ModItems.itemLevelUpgrade.getLevel(upgrade);

            if ( newLevel.ordinal() != currentLevel.ordinal() + 1 )
                return false;

            setLevel(newLevel);

        } else if ( item == ModItems.itemConversionUpgrade ) {
            Level currentLevel = getLevel();
            Level newLevel = ModItems.itemLevelUpgrade.getLevel(upgrade);

            if ( newLevel.ordinal() <= currentLevel.ordinal() )
                return false;

            setLevel(newLevel);

        } else if ( item instanceof ItemAugment ) {
            if ( !isValidAugment(upgrade) )
                return false;

            if ( upgrade.getCount() > 1 ) {
                upgrade = upgrade.copy();
                upgrade.setCount(1);
            }

            if ( !installAugment(upgrade) )
                return false;

            updateAugmentStatus();

        } else
            return false;

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        return true;
    }

    @Override
    public ItemStack[] getAugmentSlots() {
        return augments;
    }

    public void updateAugmentStatus() {
        Map<ItemAugment, ItemStack> augments = new Object2ObjectOpenHashMap<>();

        augmentMultiplier = 1;
        augmentEnergy = 0;

        augmentBudgetMult = 1;
        augmentBudgetAdd = 0;

        int oldDrain = augmentDrain;
        augmentDrain = 0;

        for (ItemStack stack : getAugmentSlots()) {
            if ( stack.isEmpty() )
                continue;

            Item itemIn = stack.getItem();
            if ( itemIn instanceof ItemAugment ) {
                ItemAugment item = (ItemAugment) itemIn;
                ItemStack other = augments.getOrDefault(item, ItemStack.EMPTY);
                if ( item.isUpgrade(other, stack) )
                    augments.put(item, stack);
            }
        }

        for (ItemAugment item : ItemAugment.AUGMENT_TYPES) {
            ItemStack stack = augments.getOrDefault(item, ItemStack.EMPTY);
            item.apply(stack, this);
            augmentMultiplier *= item.getEnergyMultiplier(stack, this);
            augmentEnergy += item.getEnergyAddition(stack, this);
            augmentDrain += item.getEneryDrain(stack, this);

            augmentBudgetMult *= item.getBudgetMultiplier(stack, this);
            augmentBudgetAdd += item.getBudgetAddition(stack, this);
        }

        if ( !updateBaseEnergy() && augmentDrain != oldDrain )
            energyChanged();
    }

    public boolean updateBaseEnergy() {
        int newEnergy = (int) ((level.baseEnergyPerOperation + augmentEnergy) * augmentMultiplier);
        if ( newEnergy < 0 )
            newEnergy = 0;

        if ( newEnergy == baseEnergy )
            return false;

        baseEnergy = newEnergy;
        energyChanged();
        return true;
    }

    /**
     * This is called when baseEnergy is updated.
     */
    public void energyChanged() {

    }

    @Override
    public void updateLevel() {
        updateBaseEnergy();
    }

    /* Inventory */

    public void onContentsChanged(int slot) {
        markChunkDirty();
    }

    public int getStackLimit(int slot) {
        return 64;
    }

    public int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(getStackLimit(slot), stack.getMaxStackSize());
    }

    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    public boolean shouldVoidItem(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    public void readInventoryFromNBT(NBTTagCompound tag) {
        if ( itemStackHandler != null && tag.hasKey("Inventory") )
            itemStackHandler.deserializeNBT(tag.getCompoundTag("Inventory"));
    }

    public void writeInventoryToNBT(NBTTagCompound tag) {
        if ( itemStackHandler == null )
            return;

        NBTTagCompound inventory = itemStackHandler.serializeNBT();
        if ( inventory.getInteger("Size") > 0 && !inventory.getTagList("Items", Constants.NBT.TAG_COMPOUND).isEmpty() )
            tag.setTag("Inventory", inventory);
    }

    protected void initializeItemStackHandler(int size) {
        itemStackHandler = new ItemStackHandler(size) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                TileEntityBaseMachine.this.onContentsChanged(slot);
                TileEntityBaseMachine.this.markChunkDirty();
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if ( !isItemValid(slot, stack) )
                    return stack;

                if ( shouldVoidItem(slot, stack) )
                    return ItemStack.EMPTY;

                return super.insertItem(slot, stack, simulate);
            }

            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                return TileEntityBaseMachine.this.getStackLimit(slot, stack);
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return isItemValidForSlot(slot, stack);
            }
        };
    }

    public ItemStackHandler getInventory() {
        return itemStackHandler;
    }

    @Override
    public int getInvSlotCount() {
        if ( itemStackHandler == null )
            return 0;

        return itemStackHandler.getSlots();
    }

    /* Life Cycle */

    public void dropContents() {
        ItemStack[] augments = getAugmentSlots();
        if ( augments != null ) {
            for (int i = 0; i < augments.length; i++)
                CoreUtils.dropItemStackIntoWorldWithVelocity(augments[i], world, pos);
        }

        if ( itemStackHandler != null ) {
            int length = itemStackHandler.getSlots();
            for (int i = 0; i < length; i++)
                CoreUtils.dropItemStackIntoWorldWithVelocity(itemStackHandler.getStackInSlot(i), world, pos);
        }
    }

    @Override
    public void blockBroken() {
        if ( world != null && pos != null && !wasDismantled )
            dropContents();

        super.blockBroken();
    }

    @Override
    public void blockDismantled() {
        wasDismantled = true;
        super.blockDismantled();
    }

    /* ITileInfoProvider */

    @Override
    public List<String> getInfoTooltips(@Nullable NBTTagCompound tag) {
        List<String> tooltip = new ArrayList<>();

        tooltip.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".owner",
                CoreProps.DEFAULT_OWNER.equals(owner) ?
                        StringHelper.localize("info." + WirelessUtils.MODID + ".no_owner") :
                        owner.getName()
        ).getFormattedText());

        return tooltip;
    }

    @Override
    public NBTTagCompound getInfoNBT(NBTTagCompound tag) {
        return new NBTTagCompound();
        //return null;
    }

    /* Chunk Loading */

    public void loadChunk(BlockPosDimension pos) {
        if ( loadedByPos.containsKey(pos) )
            return;

        int id = loadChunk(pos.getDimension(), new ChunkPos(pos));
        if ( id == -1 )
            return;

        loadedByPos.put(pos, id);
    }

    public void unloadChunk(BlockPosDimension pos) {
        if ( !loadedByPos.containsKey(pos) )
            return;

        unloadChunk(loadedByPos.get(pos));
        loadedByPos.remove(pos);
    }

    private int loadChunk(int dimension, ChunkPos chunk) {
        if ( world == null || world.isRemote )
            return -1;

        ChunkManager.ChunkRequests requests = ChunkManager.getChunk(dimension, chunk);
        int id = requests.load(owner);
        if ( id == -1 )
            return -1;

        setWatchUnload();
        loadedChunks.put(id, new Tuple<>(dimension, chunk));
        return id;
    }

    private void unloadChunk(int id) {
        Tuple<Integer, ChunkPos> posTuple = loadedChunks.get(id);
        if ( posTuple == null )
            return;

        ChunkManager.ChunkRequests requests = ChunkManager.getChunk(posTuple);
        loadedChunks.remove(id);
        requests.unload(id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unloadAllChunks();
    }

    public void unloadAllChunks() {
        loadedByPos.clear();
        Set<Integer> ids = new IntArraySet(loadedChunks.keySet());
        for (int id : ids)
            unloadChunk(id);
    }

    /* Security */
    // A lot of this is copied from CoFH Core, but we don't want everything they do.

    public void onOwnerChanged(GameProfile oldOwner) {
        Set<BlockPosDimension> positions = new ObjectOpenHashSet<>(loadedByPos.keySet());
        unloadAllChunks();
        for (BlockPosDimension pos : positions)
            loadChunk(pos);
    }

    public GameProfile getOwner() {
        return owner;
    }

    public boolean setOwnerName(String name) {
        if ( !CoreProps.DEFAULT_OWNER.equals(owner) )
            return false;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if ( server == null )
            return false;

        if ( Strings.isNullOrEmpty(name) || CoreProps.DEFAULT_OWNER.getName().equalsIgnoreCase(name) )
            return false;

        String uuid = PreYggdrasilConverter.convertMobOwnerIfNeeded(server, name);
        if ( Strings.isNullOrEmpty(uuid) )
            return false;

        return setOwner(new GameProfile(UUID.fromString(uuid), name));
    }

    public boolean setOwner(GameProfile profile) {
        if ( !CoreProps.DEFAULT_OWNER.equals(owner) )
            return false;

        if ( SecurityHelper.isDefaultUUID(owner.getId()) ) {
            owner = profile;
            if ( !SecurityHelper.isDefaultUUID(owner.getId()) ) {
                if ( FMLCommonHandler.instance().getMinecraftServerInstance() != null ) {
                    new Thread("CoFH User Loader") {
                        @Override
                        public void run() {
                            owner = SecurityHelper.getProfile(owner.getId(), owner.getName());
                        }
                    }.start();
                }

                if ( world != null && !world.isRemote ) {
                    markChunkDirty();
                    sendTilePacket(Side.CLIENT);
                }

                return true;
            }
        }

        return false;
    }

    public void readOwnerFromNBT(NBTTagCompound tag) {
        GameProfile oldOwner = owner;
        owner = CoreProps.DEFAULT_OWNER;

        String name = tag.getString("Owner");
        UUID uuid = tag.hasKey("OwnerUUID") ? tag.getUniqueId("OwnerUUID") : null;

        if ( uuid != null || !Strings.isNullOrEmpty(name) ) {
            GameProfile profile = new GameProfile(uuid, name);
            if ( !owner.equals(profile) ) {
                if ( uuid != null )
                    setOwner(profile);
                else if ( !Strings.isNullOrEmpty(name) )
                    setOwnerName(name);
            }
        }

        if ( !owner.equals(oldOwner) )
            onOwnerChanged(oldOwner);
    }

    public void writeOwnerToNBT(NBTTagCompound tag) {
        if ( CoreProps.DEFAULT_OWNER.equals(owner) )
            return;

        tag.setString("Owner", owner.getName());
        tag.setUniqueId("OwnerUUID", owner.getId());
    }


    /* NBT Save and Load */

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        // These need to happen before we read extra, so do
        // these before the super call.
        readLevelFromNBT(tag);
        readOwnerFromNBT(tag);
        readAugmentsFromNBT(tag);
        updateAugmentStatus();

        inactiveTicks = tag.getByte("InactiveTicks");

        super.readFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeLevelToNBT(tag);
        writeOwnerToNBT(tag);
        writeAugmentsToNBT(tag);

        tag.setByte("InactiveTicks", inactiveTicks);

        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);
        readInventoryFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);
        writeInventoryToNBT(tag);
        return tag;
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    /* Inactivity */

    public void tickInactive() {
        if ( inactiveTicks < ModConfig.performance.scanRate ) {
            inactiveTicks++;
            if ( inactiveTicks >= ModConfig.performance.scanRate )
                onInactive();
        }
    }

    public void tickActive() {
        inactiveTicks = 0;
    }

    public void onInactive() {
        unloadAllChunks();
    }

    /* Block State */

    public void setActive(boolean active) {
        isActive = active;
        if ( world == null || world.isRemote )
            return;

        if ( isActive == wasActive )
            return;

        if ( !activeCooldown ) {
            activeCooldown = true;
            tracker.markTime(world);

        } else if ( tracker.hasDelayPassed(world, 10) )
            activeCooldown = false;
        else
            return;

        wasActive = isActive;
        sendTilePacket(Side.CLIENT);
    }


    /* Packets */

    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();
        payload.addByte(level.ordinal());
        payload.addUUID(owner.getId());
        payload.addString(owner.getName());
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);
        setLevel(payload.getByte());

        GameProfile oldOwner = owner;
        owner = CoreProps.DEFAULT_OWNER;
        setOwner(new GameProfile(payload.getUUID(), payload.getString()));
        if ( !owner.equals(oldOwner) )
            onOwnerChanged(oldOwner);
    }

    /* Debugging */

    public void debugPrint() {
        System.out.println("Class: " + getClass().getCanonicalName());
        System.out.println("Level: " + level.toInt() + ": " + level.getName());
        System.out.println("Active: " + isActive);
        System.out.println("Owner: " + owner.getName() + "#" + owner.getId());
        System.out.println("Comparator: " + comparatorState);
        System.out.println("Dismantled: " + wasDismantled);
        System.out.println("Inactive Ticks: " + inactiveTicks);

        ItemStack[] augments = getAugmentSlots();
        System.out.println("Augments: " + (augments == null ? "NULL" : "[" + augments.length + "]"));
        if ( augments != null )
            for (int i = 0; i < augments.length; i++)
                System.out.println("  " + i + ": " + augments[i].toString());

        System.out.println("Inventory: " + (itemStackHandler == null ? "NULL" : "[" + itemStackHandler.getSlots() + "]"));
        if ( itemStackHandler != null ) {
            int slots = itemStackHandler.getSlots();
            for (int i = 0; i < slots; i++)
                System.out.println("  " + i + ": " + itemStackHandler.getStackInSlot(i).toString());
        }
    }
}
