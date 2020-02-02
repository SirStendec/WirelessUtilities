package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderAsItem;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModBlocks;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityFluxedPearl extends EntityBaseThrowable {

    private boolean spawnedLightning = false;
    private boolean didRise = false;

    public EntityFluxedPearl(World world) {
        super(world);
    }

    public EntityFluxedPearl(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityFluxedPearl(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityFluxedPearl(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityFluxedPearl(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityFluxedPearl(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    public boolean canAttractLightning() {
        int setting = ModConfig.items.fluxedPearl.attractLightning;
        if ( setting == 0 )
            return false;

        boolean isStabilized = getStack().getMetadata() == 1;

        if ( setting == 1 )
            return !isStabilized;

        else if ( setting == 2 )
            return isStabilized;

        return true;
    }

    public static boolean rainingOrSnowingAt(World world, BlockPos pos) {
        if ( !world.isRaining() || !world.canSeeSky(pos) || world.getPrecipitationHeight(pos).getY() > pos.getY() )
            return false;

        Biome biome = world.getBiome(pos);
        return biome.canRain() || biome.isSnowyBiome();
    }

    @Override
    public void renderTrail() {
        world.spawnParticle(EnumParticleTypes.REDSTONE, posX, posY, posZ, 0, 0, 0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if ( isDead || world.isRemote || spawnedLightning || !canAttractLightning() )
            return;

        if ( motionY > 0 )
            didRise = true;

        if ( ticksExisted < 40 )
            return;

        if ( didRise && motionY < 0 ) {
            spawnedLightning = true;

            BlockPos pos = getPosition();
            int weather = ModConfig.items.fluxedPearl.requiredWeather;
            if ( weather == 2 && !world.isThundering() )
                return;
            if ( weather > 0 && !rainingOrSnowingAt(world, pos) )
                return;

            int i = (int) posX >> 4;
            int j = (int) posZ >> 4;

            int maxHeight = 0;

            for (int chunkX = i - 1; chunkX < i + 2; chunkX++) {
                for (int chunkZ = j - 1; chunkZ < j + 2; chunkZ++) {
                    Chunk chunk = world.getChunk(chunkX, chunkZ);
                    if ( chunk == null || !chunk.isLoaded() || chunk.isEmpty() )
                        continue;

                    int[] heightMap = chunk.getHeightMap();
                    if ( heightMap == null )
                        continue;

                    for (int value : heightMap) {
                        if ( value > maxHeight )
                            maxHeight = value;
                    }
                }
            }

            if ( lastTickPosY > (maxHeight + 10) ) {
                world.addWeatherEffect(new EntityLightningBolt(world, posX, posY, posZ, false));

                if ( !world.isThundering() && rand.nextDouble() < ModConfig.items.fluxedPearl.thunderingChance ) {
                    WorldInfo info = world.getWorldInfo();
                    info.setCleanWeatherTime(0);
                    info.setRainTime(0);
                    info.setThunderTime(rand.nextInt(12000) + 3600);
                    info.setRaining(true);
                    info.setThundering(true);
                }
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if ( source == DamageSource.LIGHTNING_BOLT ) {
            if ( !world.isRemote ) {
                if ( ModConfig.items.fluxedPearl.enableLightning ) {
                    EntityLivingBase thrower = getThrower();
                    if ( thrower instanceof EntityPlayerMP )
                        ModAdvancements.STORM_CHASER.trigger((EntityPlayerMP) thrower);

                    dropItemWithMeta(ModItems.itemChargedPearl, 1);
                } else
                    dropItemWithMeta(ModItems.itemFluxedPearl, 1);
            }

            setDead();
            return false;
        }

        return super.attackEntityFrom(source, amount);
    }

    protected boolean tryExtract(IEnergyStorage storage) {
        if ( storage == null || !storage.canExtract() )
            return false;

        final int energy = ModConfig.items.fluxedPearl.chargeEnergy;
        int extracted = storage.extractEnergy(energy, true);
        if ( extracted != energy )
            return false;

        storage.extractEnergy(energy, false);
        return true;
    }

    private boolean tryPlaceRedstonePulse(BlockPos pos, EnumFacing facing) {
        BlockPos offset = pos.offset(facing);
        IBlockState state = world.getBlockState(offset);
        Block block = state != null ? state.getBlock() : null;

        if ( world.isAirBlock(offset) || block == ModBlocks.blockPoweredAir ) {
            world.setBlockState(offset, ModBlocks.blockPoweredAir.getDefaultState().withProperty(Properties.FACING, facing));

        } else if ( block == Blocks.REDSTONE_WIRE ) {
            world.setBlockState(offset, ModBlocks.blockPoweredRedstoneWire.getDefaultState().withProperty(Properties.FACING, facing));
        } else
            return false;

        if ( !world.isRemote ) {
            IBlockState hitState = world.getBlockState(pos);
            Block hitBlock = hitState.getBlock();
            if ( hitBlock == Blocks.REDSTONE_LAMP || hitBlock == Blocks.LIT_REDSTONE_LAMP ) {
                EntityLivingBase thrower = getThrower();
                if ( thrower instanceof EntityPlayerMP )
                    ModAdvancements.ENLIGHTENED.trigger((EntityPlayerMP) thrower);
            }
        }

        return true;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return false;
    }

    @Override
    public HitReaction hitBlock(IBlockState state, RayTraceResult result) {
        HitReaction reaction = super.hitBlock(state, result);
        if ( reaction != null && reaction.type == HitReactionType.BOUNCE ) {
            if ( !world.isRemote && ModConfig.items.fluxedPearl.enableRedstonePulse ) {
                if ( tryPlaceRedstonePulse(result.getBlockPos(), result.sideHit) ) {
                    if ( world instanceof WorldServer ) {
                        WorldServer ws = (WorldServer) world;
                        ws.spawnParticle(EnumParticleTypes.REDSTONE, result.hitVec.x, result.hitVec.y, result.hitVec.z, 3, 0D, 0D, 0D, 0D);
                    }
                }
            }
        }

        return reaction;
    }

    protected void onImpact(RayTraceResult result) {
        if ( result.entityHit != null ) {
            if ( result.entityHit == getThrower() )
                return;
        }

        if ( result.typeOfHit == RayTraceResult.Type.BLOCK ) {
            BlockPos pos = result.getBlockPos();
            IBlockState block = world.getBlockState(pos);

            block.getBlock().onEntityCollision(world, pos, block, this);
            if ( isDead )
                return;

            if ( world.isRemote ) {
//                setDead();
                return;
            }

            if ( ModConfig.items.fluxedPearl.enableDischargeMachines ) {
                TileEntity tile = world.getTileEntity(pos);
                if ( tile != null ) {
                    boolean extracted = false;
                    if ( tile.hasCapability(CapabilityEnergy.ENERGY, result.sideHit) )
                        extracted = tryExtract(tile.getCapability(CapabilityEnergy.ENERGY, result.sideHit));

                    if ( !extracted && tile.hasCapability(CapabilityEnergy.ENERGY, null) )
                        extracted = tryExtract(tile.getCapability(CapabilityEnergy.ENERGY, null));

                    if ( extracted ) {
                        if ( world instanceof WorldServer ) {
                            WorldServer ws = (WorldServer) world;
                            ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, result.hitVec.x, result.hitVec.y, result.hitVec.z, 3, 0D, 0D, 0D, 0D);
                        }

                        dropItemWithMeta(ModItems.itemChargedPearl, 1);
                        setDead();
                        return;
                    }
                }
            }

            if ( ModConfig.items.fluxedPearl.enableRedstonePulse ) {
                if ( tryPlaceRedstonePulse(pos, result.sideHit) ) {
                    if ( world instanceof WorldServer ) {
                        WorldServer ws = (WorldServer) world;
                        ws.spawnParticle(EnumParticleTypes.REDSTONE, result.hitVec.x, result.hitVec.y, result.hitVec.z, 3, 0D, 0D, 0D, 0D);
                    }
                }
            }

            dropItemWithMeta(ModItems.itemFluxedPearl, 1);
            setDead();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityFluxedPearl> {
        @Override
        public Render<? super EntityFluxedPearl> createRenderFor(RenderManager manager) {
            return new RenderAsItem<EntityFluxedPearl>(manager, ModItems.itemFluxedPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }
}
