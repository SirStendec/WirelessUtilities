package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.location.TargetInfo;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.VanillaDoubleChestItemHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Worker<T extends TargetInfo> {

    private final IWorkProvider<T> provider;

    private short cacheTTL = 0;

    private List<T> cacheList;
    private List<T> randomList;

    private int cachePosition = -1;
    private boolean cacheInInventory = false;
    private int cacheInvPosition = -1;

    private int remainingEffects;

    private final Random random;

    public Worker(IWorkProvider<T> provider) {
        this.provider = provider;
        random = new Random();
        remainingEffects = ModConfig.rendering.particlesMax;
    }

    public void debugPrint() {
        System.out.println(" Effect Budget: " + remainingEffects);
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
        if ( cacheList != null )
            cacheList.clear();

        cacheTTL = 0;
        cachePosition = -1;
        cacheInInventory = false;
        cacheInvPosition = -1;
    }

    @Nullable
    public List<T> getTargetCache() {
        if ( cacheList == null )
            return null;

        return Collections.unmodifiableList(cacheList);
    }

    public void updateTargetCache() {
        if ( cacheList != null && cacheTTL > 0 )
            return;

        final Profiler profiler = WirelessUtils.profiler;

        profiler.startSection("worker:updateTargetCache"); // 1
        profiler.startSection("init"); // 2

        BlockPosDimension oldPos = null;
        Entity oldEnt = null;

        if ( cacheList == null )
            cacheList = new ObjectArrayList<>();
        else {
            if ( cachePosition >= 0 && cachePosition < cacheList.size() ) {
                T target = cacheList.get(cachePosition);
                if ( target != null ) {
                    if ( target.entity != null )
                        oldEnt = target.entity;
                    else
                        oldPos = target.pos;
                }
            }

            cacheList.clear();
        }

        provider.onTargetCacheRebuild();

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
        boolean processEntities = provider.shouldProcessEntities();

        int[] tempSlots = new int[ModConfig.augments.inventory.maximumScanSlots];

        Iterable<Tuple<BlockPosDimension, ItemStack>> targets = provider.getTargets();
        Iterable<Tuple<Entity, ItemStack>> entityTargets = provider.getEntityTargets();

        profiler.endStartSection("entities"); // init - 2

        if ( entityTargets != null ) {
            for (Tuple<Entity, ItemStack> pair : entityTargets) {
                Entity entity = pair.getFirst();
                ItemStack source = pair.getSecond();

                if ( entity == null )
                    continue;

                if ( entity == oldEnt )
                    passedOldPos = true;

                World world = entity.getEntityWorld();
                if ( world == null )
                    continue;

                T info = null;

                if ( processEntities && provider.canWorkEntity(source, world, entity) ) {
                    info = provider.createInfo(null, source, world, null, null, entity);
                    info.processEntity = true;
                }

                if ( processItems && entity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) ) {
                    profiler.startSection("inventory"); // 3
                    int inventoryTarget = -1;
                    if ( entity == oldEnt && wasInInventory )
                        inventoryTarget = oldInvPosition;

                    IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    if ( handler != null ) {
                        int totalSlots = Math.min(handler.getSlots(), ModConfig.augments.inventory.maximumScanSlots);
                        int count = 0;

                        boolean hitTarget = false;

                        for (int i = 0; i < totalSlots; i++) {
                            if ( i == inventoryTarget && inventoryTarget >= 0 )
                                hitTarget = true;

                            ItemStack stack = handler.getStackInSlot(i);
                            if ( provider.canWorkItem(stack, i, handler, null, source, world, null, null, entity) ) {
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
                                info = provider.createInfo(null, source, world, null, null, entity);

                            info.processInventory = true;
                            info.slots = new int[count];
                            info.liveSlots = count;
                            System.arraycopy(tempSlots, 0, info.slots, 0, count);
                        }
                    }

                    profiler.endSection(); // inventory - 2
                }

                if ( info != null ) {
                    cacheList.add(info);
                    if ( passedOldPos && cachePosition == -1 )
                        cachePosition = cacheList.size() - 1;
                }
            }
        }

        profiler.endStartSection("blocks"); // entities - 2

        if ( targets != null ) {
            for (Tuple<BlockPosDimension, ItemStack> pair : targets) {
                profiler.startSection("check"); // 3

                BlockPosDimension target = pair.getFirst();
                ItemStack source = pair.getSecond();

                if ( target == oldPos )
                    passedOldPos = true;

                World world = DimensionManager.getWorld(target.getDimension(), false);
                if ( world == null || !world.isBlockLoaded(target) ) {
                    profiler.endSection(); // check - 2
                    continue;
                }

                IBlockState state = processBlocks ? world.getBlockState(target) : null;
                TileEntity tile = (processTiles || processItems) ? world.getTileEntity(target) : null;
                T info = null;
                boolean useSingleChest = false;

                if ( !processBlocks && tile == null ) {
                    profiler.endSection(); // check - 2
                    continue;
                }

                profiler.endStartSection("block"); // check - 3

                if ( processBlocks && state != null && provider.canWorkBlock(target, source, world, state, tile) ) {
                    info = provider.createInfo(target.toImmutable(), source, world, state, tile, null);
                    info.processBlock = true;
                }

                profiler.endStartSection("tile"); // block - 3

                if ( processTiles && tile != null && provider.canWorkTile(target, source, world, state, tile) ) {
                    if ( info == null )
                        info = provider.createInfo(target.toImmutable(), source, world, state, tile, null);
                    info.processTile = true;
                }

                profiler.endStartSection("inventory"); // tile - 3

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
                            if ( provider.canWorkItem(stack, i, handler, target, source, world, state, tile, null) ) {
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
                                info = provider.createInfo(target.toImmutable(), source, world, state, tile, null);

                            info.processInventory = true;
                            info.useSingleChest = useSingleChest;
                            info.slots = new int[count];
                            info.liveSlots = count;
                            System.arraycopy(tempSlots, 0, info.slots, 0, count);
                        }
                    }
                }

                profiler.endStartSection("save"); // inventory - 2

                if ( info != null ) {
                    cacheList.add(info);
                    if ( passedOldPos && cachePosition == -1 )
                        cachePosition = cacheList.size() - 1;
                }

                profiler.endSection(); // save - 2
            }
        }

        profiler.endStartSection("sorting"); // blocks - 2

        sortCacheList();

        if ( randomList != null ) {
            if ( provider.getIterationMode() == IWorkProvider.IterationMode.RANDOM ) {
                randomList.clear();
                randomList.addAll(cacheList);
                Collections.shuffle(randomList, random);
            } else
                randomList = null;
        }

        profiler.endSection(); // sorting - 1
        profiler.endSection(); // WU:update - 0
    }

    private void sortCacheList() {
        if ( cacheList == null )
            return;

        final BlockPosDimension origin = provider.getPosition();
        Map<BlockPosDimension, Long> distanceMap = new HashMap<>();

        cacheList.sort((val1, val2) -> {
            long d1, d2;

            if ( val1.pos == null )
                d1 = -1;
            else if ( distanceMap.containsKey(val1.pos) )
                d1 = distanceMap.get(val1.pos);
            else {
                d1 = ITargetProvider.calculateDistanceSquared(origin, val1.pos);
                distanceMap.put(val1.pos, d1);
            }

            if ( val2.pos == null )
                d2 = -1;
            else if ( distanceMap.containsKey(val2.pos) )
                d2 = distanceMap.get(val2.pos);
            else {
                d2 = ITargetProvider.calculateDistanceSquared(origin, val2.pos);
                distanceMap.put(val2.pos, d2);
            }

            if ( d1 < d2 )
                return -1;
            else if ( d1 > d2 )
                return 1;

            // If we don't have positions, we just can't sort.
            if ( d1 == -1 )
                return 0;

            // If the distance is the same, fall back to comparing the raw
            // positions so that sorting behavior remains consistent.
            return val1.pos.compareTo(val2.pos);
        });
    }

    /**
     * Perform tasks that should happen once per tick, even if we don't do work.
     */
    public void tickDown() {
        if ( remainingEffects < ModConfig.rendering.particlesMax ) {
            remainingEffects += ModConfig.rendering.particlePerTick;
            if ( remainingEffects > ModConfig.rendering.particlesMax )
                remainingEffects = ModConfig.rendering.particlesMax;
        }

        cacheTTL--;
    }

    public void shuffleRandom() {
        if ( cacheList == null )
            return;

        if ( randomList == null )
            randomList = new ObjectArrayList<>(cacheList);

        Collections.shuffle(randomList, random);
    }

    public boolean performWork() {
        return performWork(ModConfig.performance.stepsPerTick);
    }

    public void incrementTarget(IWorkProvider.IterationMode mode, boolean didWork, boolean didRemove) {
        if ( cacheList == null || cacheList.isEmpty() )
            return;

        int size = cacheList.size();
        if ( mode == IWorkProvider.IterationMode.FURTHEST_FIRST ) {
            if ( didWork && !didRemove )
                cachePosition = size - 1;
            else
                cachePosition--;

        } else if ( mode == IWorkProvider.IterationMode.NEAREST_FIRST ) {
            if ( didWork && !didRemove )
                cachePosition = 0;
            else
                cachePosition++;
        } else
            cachePosition++;

        if ( cachePosition < 0 )
            cachePosition = size - 1;

        if ( cachePosition >= size ) {
            if ( mode == IWorkProvider.IterationMode.RANDOM )
                shuffleRandom();

            cachePosition = 0;
        }
    }


    public boolean performWork(int steps) {
        final Profiler profiler = WirelessUtils.profiler;

        profiler.startSection("worker:performWork"); // 1
        updateTargetCache();

        profiler.startSection("init"); // 2

        boolean worked = false;

        if ( cacheList == null || cacheList.isEmpty() ) {
            profiler.endSection(); // 2 - init
            profiler.endSection(); // 1 - performWork
            return worked;
        }

        IWorkProvider.IterationMode mode = provider.getIterationMode();
        if ( !cacheInInventory ) {
            if ( mode == IWorkProvider.IterationMode.NEAREST_FIRST )
                cachePosition = 0;

            else if ( mode == IWorkProvider.IterationMode.FURTHEST_FIRST )
                cachePosition = cacheList.size() - 1;

            else if ( cachePosition < 0 )
                cachePosition = 0;
        }

        boolean isRandom = mode == IWorkProvider.IterationMode.RANDOM;
        if ( isRandom && randomList == null )
            shuffleRandom();

        int startingPosition = cachePosition;
        int loops = 0;
        boolean keepWorking = true;
        boolean didRemove = false;
        boolean didWork = false;
        boolean started = false;
        boolean noAdvance = false;

        profiler.endSection(); // 2 - init
        profiler.startSection("loop"); // 2

        while ( steps > 0 ) {
            loops++;
            if ( loops > 1000000000 )
                throw new IllegalStateException("Infinite loop in Worker.");

            profiler.startSection("check"); // 3

            if ( !cacheInInventory && !noAdvance ) {
                if ( started ) {
                    incrementTarget(mode, didWork, didRemove);

                    if ( !keepWorking || cachePosition == startingPosition ) {
                        profiler.endSection(); // 3 - check
                        profiler.endSection(); // 2 - loop
                        profiler.endSection(); // 1 - performWork
                        return worked;
                    }

                } else
                    started = true;
            }

            noAdvance = false;
            didWork = false;

            T target = (isRandom ? randomList : cacheList).get(cachePosition);
            if ( cacheInInventory && (target == null || !target.processInventory) ) {
                cacheInInventory = false;
                cacheInvPosition = -1;
            }

            if ( target == null ) {
                profiler.endSection(); // 3 - check
                continue;
            }

            if ( target.entity != null && target.entity.isDead ) {
                target.entity = null;
                target.processEntity = false;

            } else if ( target.entity == null && target.processEntity ) {
                target.processEntity = false;
            }

            if ( target.tile != null && target.tile.isInvalid() ) {
                target.tile = null;
                target.processTile = false;

            } else if ( target.tile == null && target.processTile ) {
                target.processTile = false;
            }

            World world = target.entity != null ? target.entity.getEntityWorld() :
                    target.tile != null ? target.tile.getWorld() :
                            target.pos != null ? DimensionManager.getWorld(target.pos.getDimension(), false) : null;

            if ( world == null || (target.pos != null && !world.isBlockLoaded(target.pos)) ) {
                target.processBlock = false;

                if ( target.tile != null ) {
                    target.tile = null;
                    target.processTile = false;
                }
            }

            if ( target.processInventory && target.tile == null && target.entity == null ) {
                target.processInventory = false;
                if ( cacheInInventory ) {
                    cacheInInventory = false;
                    cacheInvPosition = -1;
                }
            }

            if ( !target.processTile && !target.processInventory && !target.processBlock && !target.processEntity ) {
                profiler.endSection(); // 3 - check
                continue;
            }

            IBlockState state = null;
            BlockPosDimension pos = target.pos;
            TileEntity tile = target.tile;
            Entity entity = target.entity;

            keepWorking = true;
            boolean wasRemoved = false;

            profiler.endSection(); // 3 - check
            profiler.startSection("work"); // 3

            if ( !cacheInInventory && target.processEntity && entity != null ) {
                profiler.startSection("entity"); // 4
                IWorkProvider.WorkResult result = provider.performWorkEntity(target, world, entity);
                profiler.endSection(); // 4 - entity
                if ( result == null )
                    result = IWorkProvider.WorkResult.SKIPPED;

                steps -= result.cost;
                if ( result.success ) {
                    worked = true;
                    didWork = true;
                }

                if ( !result.keepProcessing )
                    keepWorking = false;

                if ( result.noAdvance )
                    noAdvance = true;

                if ( result.remove ) {
                    wasRemoved = true;
                    target.processEntity = false;
                }
            }

            if ( !cacheInInventory && target.processBlock && pos != null ) {
                profiler.startSection("block"); // 4
                IWorkProvider.WorkResult result = provider.performWorkBlock(target, world, state, tile);
                profiler.endSection(); // 4 - block
                if ( result == null )
                    result = IWorkProvider.WorkResult.SKIPPED;

                steps -= result.cost;
                if ( result.success ) {
                    worked = true;
                    didWork = true;
                }

                if ( !result.keepProcessing )
                    keepWorking = false;

                if ( result.noAdvance )
                    noAdvance = true;

                if ( result.remove ) {
                    wasRemoved = true;
                    target.processBlock = false;
                }
            }

            if ( !cacheInInventory && target.processTile && tile != null ) {
                profiler.startSection("tile"); // 4
                IWorkProvider.WorkResult result = provider.performWorkTile(target, world, state, tile);
                profiler.endSection(); // 4 - tile
                if ( result == null )
                    result = IWorkProvider.WorkResult.SKIPPED;

                steps -= result.cost;
                if ( result.success ) {
                    worked = true;
                    didWork = true;
                }

                if ( !result.keepProcessing )
                    keepWorking = false;

                if ( result.noAdvance )
                    noAdvance = true;

                if ( result.remove ) {
                    wasRemoved = true;
                    target.processTile = false;
                }
            }

            if ( (cacheInInventory || !noAdvance) && target.processInventory && (tile != null || entity != null) ) {
                if ( steps <= 0 || !keepWorking ) {
                    cacheInInventory = true;
                    profiler.endSection(); // 3 - work
                    profiler.endSection(); // 2 - loop
                    profiler.endSection(); // 1 - performWork
                    return worked;
                }

                profiler.startSection("inventory"); // 4
                profiler.startSection("check"); // 5

                IItemHandler handler = entity != null ? entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) :
                        (target.useSingleChest && tile instanceof TileEntityChest) ? ((TileEntityChest) tile).getSingleChestHandler() : tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, target.pos.getFacing());
                if ( handler == null ) {
                    target.processInventory = false;
                    cacheInInventory = false;
                    cacheInvPosition = -1;

                    profiler.endSection(); // 5 - check
                    profiler.endSection(); // 4 - inventory
                    profiler.endSection(); // 3 - work
                    continue;
                }

                int totalSlots = handler.getSlots();
                int slots = target.slots.length;

                int start, inc;
                if ( mode == IWorkProvider.IterationMode.FURTHEST_FIRST ) {
                    start = slots - 1;
                    inc = -1;
                } else if ( mode == IWorkProvider.IterationMode.RANDOM ) {
                    start = random.nextInt(slots);
                    inc = random.nextBoolean() ? 1 : -1;
                } else {
                    start = 0;
                    inc = 1;
                }

                if ( cacheInInventory )
                    start = cacheInvPosition;

                profiler.endSection(); // 5 - check
                profiler.startSection("loop"); // 5

                if ( slots > start ) {
                    boolean invStarted = false;
                    boolean invWorked = false;
                    int i = start;
                    while ( steps > 0 ) {
                        loops++;
                        if ( loops > 1000000000 )
                            throw new IllegalStateException("Infinite loop in Worker.");

                        if ( invStarted ) {
                            if ( invWorked && mode != IWorkProvider.IterationMode.ROUND_ROBIN )
                                break;

                            i += inc;
                            if ( i < 0 )
                                i = slots - 1;
                            else if ( i >= slots )
                                i = 0;

                            if ( i == start || i >= slots || i < 0 )
                                break;
                        } else
                            invStarted = true;

                        int slot = target.slots[i];
                        if ( slot < 0 || slot >= totalSlots )
                            continue;

                        invWorked = false;
                        ItemStack stack = handler.getStackInSlot(slot);
                        profiler.startSection("work"); // 6
                        IWorkProvider.WorkResult result = provider.performWorkItem(stack, slot, handler, target, world, state, tile, entity);
                        profiler.endSection(); // 6 - work
                        if ( result == null )
                            result = IWorkProvider.WorkResult.SKIPPED;

                        steps -= result.cost;
                        if ( result.success ) {
                            worked = true;
                            didWork = true;
                            invWorked = true;
                        }

                        if ( result.remove ) {
                            target.slots[i] = -1;
                            target.liveSlots--;
                            if ( target.liveSlots <= 0 ) {
                                wasRemoved = true;
                                target.processInventory = false;
                            }
                        }

                        if ( steps < 1 || !result.keepProcessing ) {
                            if ( didWork ) {
                                profiler.startSection("effect"); // 6
                                performEffect(target);
                                profiler.endSection(); // 6 - effect
                            }

                            if ( target.processInventory ) {
                                cacheInInventory = true;
                                cacheInvPosition = i + (result.noAdvance ? 0 : 1);
                            } else
                                cacheInInventory = false;

                            profiler.endSection(); // 5 - loop
                            profiler.endSection(); // 4 - inventory
                            profiler.endSection(); // 3 - work
                            profiler.endSection(); // 2 - loop
                            profiler.endSection(); // 1 - performWork

                            return worked;
                        }
                    }
                }

                profiler.endSection(); // 5 - loop
                profiler.endSection(); // 4 - inventory
                cacheInInventory = false;
            }

            profiler.endSection(); // 3 - work

            if ( wasRemoved && !target.processBlock && !target.processInventory && !target.processTile && !target.processEntity )
                didRemove = true;

            if ( didWork ) {
                profiler.startSection("effect"); // 3
                performEffect(target);
                profiler.endSection(); // 3 - effect
            }

            if ( !keepWorking && noAdvance ) {
                profiler.endSection(); // 2 - loop
                profiler.endSection(); // 1 - performWork
                return worked;
            }
        }

        profiler.endSection(); // 2 - loop
        profiler.endSection(); // 1 - performWork

        return worked;
    }

    public void performEffect(T target) {
        if ( !ModConfig.rendering.particlesEnabled )
            return;

        if ( remainingEffects < ModConfig.rendering.particlesCost )
            return;

        World world;
        BlockPos pos;
        boolean entity;

        if ( target.entity != null && !target.entity.isDead ) {
            world = target.entity.world;
            pos = target.entity.getPosition();
            entity = true;

        } else if ( target.pos != null ) {
            world = DimensionManager.getWorld(target.pos.getDimension(), false);
            pos = target.pos;
            entity = false;

        } else
            return;

        if ( world == null || pos == null || !world.isBlockLoaded(pos) )
            return;

        final int wait = provider.getEffectFrequency(target, world, entity);
        final long now = world.getTotalWorldTime();
        if ( now - target.lastEffect < wait )
            return;

        target.lastEffect = now;
        if ( provider.performEffect(target, world, entity) )
            remainingEffects -= ModConfig.rendering.particlesCost;
    }
}
