package com.lordmau5.wirelessutils.proxy;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.commands.ProfileCommand;
import com.lordmau5.wirelessutils.item.module.ItemSlaughterModule;
import com.lordmau5.wirelessutils.item.module.ItemTheoreticalSlaughterModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.Predicate;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber
public class ServerProxy extends CommonProxy {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if ( event.phase == TickEvent.Phase.START ) {
            if ( ProfileCommand.wantsToStart ) {
                ProfileCommand.wantsToStart = false;
                WirelessUtils.profiler.profilingEnabled = true;
                WirelessUtils.profiler.clearProfiling();
            }

            WirelessUtils.profiler.startSection("root");
        } else if ( event.phase == TickEvent.Phase.END )
            WirelessUtils.profiler.endSection();
    }

    @SubscribeEvent
    public static void onPlaceBlock(BlockEvent.PlaceEvent event) {
        if ( event.getWorld().isRemote )
            return;
        EventDispatcher.PLACE_BLOCK.dispatchEvent(event);
    }

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if ( event.getWorld().isRemote )
            return;
        EventDispatcher.BREAK_BLOCK.dispatchEvent(event);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if ( event.getWorld().isRemote )
            return;
        EventDispatcher.CHUNK_LOAD.dispatchEvent(event);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if ( event.getWorld().isRemote )
            return;
        EventDispatcher.CHUNK_UNLOAD.dispatchEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerPickup(EntityItemPickupEvent event) {
        if ( !ModConfig.augments.filter.enableOffHand )
            return;

        EntityPlayer player = event.getEntityPlayer();
        EntityItem entity = event.getItem();
        Predicate<ItemStack> predicate = ModItems.itemFilterAugment.getHeldFilter(player);

        if ( predicate != null && !predicate.test(entity.getItem()) ) {
            event.setCanceled(true);
            if ( ModItems.itemFilterAugment.isVoiding(player.getHeldItemOffhand()) && entity.world instanceof WorldServer ) {
                entity.setDead();

                WorldServer ws = (WorldServer) entity.world;
                if ( ModConfig.augments.filter.offhandParticles )
                    ws.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, false, entity.posX, entity.posY + (entity.height / 3), entity.posZ, 1, 0D, 0D, 0D, 0D);

                if ( ModConfig.augments.filter.offhandVolume != 0 )
                    ws.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, (float) ModConfig.augments.filter.offhandVolume, 1F);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingFall(LivingFallEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if ( living != null ) {
            NBTTagCompound data = living.getEntityData();
            if ( data != null && data.getBoolean("WUFallProtect") ) {
                data.removeTag("WUFallProtect");
                event.setDistance(0);
                event.setDamageMultiplier(0);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if ( !ModConfig.items.quenchedPearl.douseEntities )
            return;

        DamageSource source = event.getSource();
        if ( source != null && source.isFireDamage() ) {
            if ( !ModConfig.items.quenchedPearl.douseEntityLava && source == DamageSource.LAVA )
                return;

            EntityLivingBase entity = event.getEntityLiving();
            if ( entity.getHeldItemMainhand().getItem() == ModItems.itemQuenchedPearl || entity.getHeldItemOffhand().getItem() == ModItems.itemQuenchedPearl )
                event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        if ( source instanceof ItemSlaughterModule.VaporizerDamage )
            ((ItemSlaughterModule.VaporizerDamage) source).getVaporizer().onItemDrops(event);
        else if ( source instanceof ItemTheoreticalSlaughterModule.TheoreticalDamage )
            ((ItemTheoreticalSlaughterModule.TheoreticalDamage) source).getVaporizer().onItemDrops(event);
        else if ( source instanceof EntityDamageSource ) {
            Entity entity = source.getTrueSource();
            if ( entity instanceof TileBaseVaporizer.WUVaporizerPlayer )
                ((TileBaseVaporizer.WUVaporizerPlayer) entity).getVaporizer().onItemDrops(event);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        EntityPlayer player = event.getAttackingPlayer();
        if ( player instanceof TileBaseVaporizer.WUVaporizerPlayer )
            ((TileBaseVaporizer.WUVaporizerPlayer) player).getVaporizer().onExperienceDrops(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLootingLevel(LootingLevelEvent event) {
        DamageSource source = event.getDamageSource();
        if ( source instanceof ItemTheoreticalSlaughterModule.TheoreticalDamage )
            event.setLootingLevel(((ItemTheoreticalSlaughterModule.TheoreticalDamage) source).getLooting());
    }
}