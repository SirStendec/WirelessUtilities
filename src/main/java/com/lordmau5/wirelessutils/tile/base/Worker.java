package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaDoubleChestItemHandler;

import java.util.List;
import java.util.Random;

public class Worker {

    private final IWorkProvider provider;

    private short cacheTTL = 0;

    private List<TargetInfo> cacheList;
    private int cachePosition = -1;
    private boolean cacheInInventory = false;
    private int cacheInvPosition = -1;

    private Random random;

    public Worker(IWorkProvider provider) {
        this.provider = provider;
        random = new Random();
    }

    public void debugPrint() {
        System.out.println("     Cache TTL: " + cacheTTL);
        System.out.println("Cache Position: " + cachePosition);
        System.out.println("  Cache In Inv: " + cacheInInventory);
        System.out.println(" Cache Inv Pos: " + cacheInvPosition);
        System.out.println("    Cache List: " + (cacheList == null ? "NULL" : "[" + cacheList.size() + "]"));
        if ( cacheList != null )
            for (int i = 0; i < cacheList.size(); i++)
                System.out.println("  " + i + ": " + cacheList.get(i));
    }

    public void clearTargetCache() {
        cacheTTL = 0;
        cachePosition = -1;
        cacheInInventory = false;
        cacheInvPosition = -1;
    }

    public void updateTargetCache() {
        if ( cacheList != null && cacheTTL > 0 )
            return;

        BlockPosDimension oldPos = null;
        if ( cacheList == null )
            cacheList = new ObjectArrayList<>();
        else {
            if ( cachePosition >= 0 && cachePosition < cacheList.size() ) {
                TargetInfo target = cacheList.get(cachePosition);
                if ( target != null )
                    oldPos = target.pos;
            }

            cacheList.clear();
        }

        boolean wasInInventory = cacheInInventory;
        int oldInvPosition = cacheInvPosition;

        cacheTTL = (short) ModConfig.performance.scanRate;
        cacheInInventory = false;
        cachePosition = -1;
        cacheInvPosition = -1;

        boolean passedOldPos = false;
        boolean processBlocks = provider.shouldProcessBlocks();
        boolean processTiles = provider.shouldProcessTiles();
        boolean processItems = provider.shouldProcessItems();

        int[] tempSlots = new int[ModConfig.augments.inventory.maximumScanSlots];

        Iterable<BlockPosDimension> targets = provider.getTargets();
        if ( targets == null )
            return;

        BlockPosDimension worker = provider.getPosition();

        for (BlockPosDimension target : targets) {
            if ( target == oldPos )
                passedOldPos = true;

            World world = DimensionManager.getWorld(target.getDimension());
            if ( world == null || !world.isBlockLoaded(target) )
                continue;

            IBlockState state = world.getBlockState(target);
            TileEntity tile = world.getTileEntity(target);
            TargetInfo info = null;
            boolean useSingleChest = false;

            if ( !processBlocks && tile == null )
                continue;

            if ( processBlocks || (processTiles && tile != null) ) {
                info = provider.canWork(target, world, state, tile);
                if ( info != null )
                    info.processBlock = true;

            }
            if ( processItems && tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.getFacing()) ) {
                int inventoryTarget = -1;
                if ( target == oldPos && wasInInventory )
                    inventoryTarget = oldInvPosition;

                IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.getFacing());
                if ( handler != null ) {
                    if ( handler instanceof VanillaDoubleChestItemHandler && tile instanceof TileEntityChest ) {
                        handler = ((TileEntityChest) tile).getSingleChestHandler();
                        useSingleChest = true;
                    }

                    int totalSlots = Math.min(handler.getSlots(), ModConfig.augments.inventory.maximumScanSlots);
                    int count = 0;

                    boolean hitTarget = false;

                    for (int i = 0; i < totalSlots; i++) {
                        if ( i == inventoryTarget && inventoryTarget >= 0 )
                            hitTarget = true;

                        ItemStack stack = handler.getStackInSlot(i);
                        if ( provider.canWork(stack, i, handler, target, world, state, tile) ) {
                            if ( hitTarget && cacheInvPosition == -1 ) {
                                cacheInvPosition = count;
                                cacheInInventory = true;
                            }

                            tempSlots[count] = i;
                            count++;
                        }
                    }

                    if ( count > 0 ) {
                        if ( info == null )
                            info = provider.createInfo(target);

                        info.processInventory = true;
                        info.useSingleChest = useSingleChest;
                        info.slots = new int[count];
                        info.liveSlots = count;
                        System.arraycopy(tempSlots, 0, info.slots, 0, count);
                    }
                }
            }

            if ( info != null ) {
                cacheList.add(info);
                if ( passedOldPos && cachePosition == -1 )
                    cachePosition = cacheList.size() - 1;
            }
        }
    }

    /**
     * Perform tasks that should happen once per tick, even if we don't do work.
     */
    public void tickDown() {
        cacheTTL--;
    }

    public boolean performWork() {
        return performWork(ModConfig.performance.stepsPerTick);
    }

    public void incrementTarget(IWorkProvider.IterationMode mode, boolean didWork) {
        if ( cacheList == null || cacheList.isEmpty() )
            return;

        int size = cacheList.size();
        if ( mode == IWorkProvider.IterationMode.RANDOM ) {
            if ( didWork )
                cachePosition = random.nextInt(cacheList.size());
            else
                cachePosition++;

        } else if ( mode == IWorkProvider.IterationMode.FURTHEST_FIRST ) {
            if ( didWork )
                cachePosition = size - 1;
            else
                cachePosition--;

        } else if ( mode == IWorkProvider.IterationMode.NEAREST_FIRST ) {
            if ( didWork )
                cachePosition = 0;
            else
                cachePosition++;
        } else
            cachePosition++;

        if ( cachePosition < 0 )
            cachePosition = size - 1;
        if ( cachePosition >= size )
            cachePosition = 0;
    }

    public boolean performWork(int steps) {
        updateTargetCache();
        boolean worked = false;

        if ( cacheList == null || cacheList.isEmpty() )
            return worked;

        IWorkProvider.IterationMode mode = provider.getIterationMode();
        if ( !cacheInInventory ) {
            if ( mode == IWorkProvider.IterationMode.RANDOM )
                cachePosition = random.nextInt(cacheList.size());

            else if ( mode == IWorkProvider.IterationMode.NEAREST_FIRST )
                cachePosition = 0;

            else if ( mode == IWorkProvider.IterationMode.FURTHEST_FIRST )
                cachePosition = cacheList.size() - 1;

            else if ( cachePosition < 0 )
                cachePosition = 0;
        }

        int startingPosition = cachePosition;
        int loops = 0;
        boolean didWork = false;
        boolean started = false;

        while ( steps > 0 ) {
            loops++;
            if ( loops > 1000000000 )
                throw new IllegalStateException("Infinite loop in Worker.");

            if ( !cacheInInventory ) {
                if ( started ) {
                    incrementTarget(mode, didWork);

                    if ( cachePosition == startingPosition )
                        return worked;
                } else
                    started = true;
            }

            didWork = false;

            TargetInfo target = cacheList.get(cachePosition);
            if ( cacheInInventory && (target == null || !target.processInventory) ) {
                cacheInInventory = false;
                cacheInvPosition = -1;
            }

            if ( target == null || (!target.processInventory && !target.processBlock) )
                continue;

            World world = DimensionManager.getWorld(target.pos.getDimension());
            if ( world == null || !world.isBlockLoaded(target.pos) ) {
                if ( cacheInInventory ) {
                    cacheInInventory = false;
                    cacheInvPosition = -1;
                }
                continue;
            }

            IBlockState state = world.getBlockState(target.pos);
            TileEntity tile = world.getTileEntity(target.pos);
            boolean keepWorking = true;

            if ( !cacheInInventory && target.processBlock ) {
                IWorkProvider.WorkResult result = provider.performWork(target, world, state, tile);
                if ( result == null )
                    result = IWorkProvider.WorkResult.SKIPPED;

                steps -= result.cost;
                if ( result.success ) {
                    worked = true;
                    didWork = true;
                }

                if ( !result.keepProcessing )
                    keepWorking = false;

                if ( result.remove )
                    target.processBlock = false;
            }

            if ( target.processInventory && tile != null ) {
                if ( steps <= 0 || !keepWorking ) {
                    cacheInInventory = true;
                    return worked;
                }

                IItemHandler handler = (target.useSingleChest && tile instanceof TileEntityChest) ? ((TileEntityChest) tile).getSingleChestHandler() : tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.pos.getFacing());
                if ( handler == null ) {
                    cacheInInventory = false;
                    cacheInvPosition = -1;
                    continue;
                }

                int start = 0;
                if ( cacheInInventory )
                    start = cacheInvPosition;

                int totalSlots = handler.getSlots();
                int slots = target.slots.length;

                if ( slots > start ) {
                    for (int i = 0; i < slots; i++) {
                        int slot = target.slots[i];
                        if ( slot < 0 || slot >= totalSlots )
                            continue;

                        ItemStack stack = handler.getStackInSlot(slot);
                        IWorkProvider.WorkResult result = provider.performWork(stack, slot, handler, target, world, state, tile);
                        if ( result == null )
                            result = IWorkProvider.WorkResult.SKIPPED;

                        steps -= result.cost;
                        if ( result.success ) {
                            worked = true;
                            didWork = true;
                        }

                        if ( result.remove ) {
                            target.slots[i] = -1;
                            target.liveSlots--;
                            if ( target.liveSlots <= 0 )
                                target.processInventory = false;
                        }

                        if ( steps < 1 || !result.keepProcessing ) {
                            if ( target.processInventory ) {
                                cacheInInventory = true;
                                cacheInvPosition = i + 1;
                            } else
                                cacheInInventory = false;

                            return worked;
                        }
                    }
                }

                cacheInInventory = false;
            }

            if ( !keepWorking )
                return worked;
        }

        return worked;
    }
}
