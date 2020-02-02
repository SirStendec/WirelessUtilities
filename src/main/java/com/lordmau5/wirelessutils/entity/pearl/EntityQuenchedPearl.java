package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderAsItem;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityQuenchedPearl extends EntityBaseThrowable {

    public EntityQuenchedPearl(World world) {
        super(world);
    }

    public EntityQuenchedPearl(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityQuenchedPearl(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityQuenchedPearl(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityQuenchedPearl(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityQuenchedPearl(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    @Override
    public boolean shouldHitLiquids() {
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    public void renderTrail() {
        world.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX, posY, posZ, rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        final int maxRange = Math.max(ModConfig.items.quenchedPearl.extinguishFire, ModConfig.items.quenchedPearl.quenchLava);
        Iterable<BlockPos.MutableBlockPos> positions = maxRange == 0 ? null : BlockPos.getAllInBoxMutable(
                (int) result.hitVec.x - maxRange, (int) result.hitVec.y - maxRange, (int) result.hitVec.z - maxRange,
                (int) result.hitVec.x + maxRange, (int) result.hitVec.y + maxRange, (int) result.hitVec.z + maxRange
        );

        if ( world.isRemote && positions != null ) {
            final int hitX = (int) result.hitVec.x;
            final int hitY = (int) result.hitVec.y;
            final int hitZ = (int) result.hitVec.z;

            playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);

            final int setting = Minecraft.getMinecraft().gameSettings.particleSetting;
            if ( setting != 2 ) {
                final byte skip = setting == 0 ? (byte) 2 : (byte) 4;
                byte skipped = (byte) (skip + 1);

                for (BlockPos.MutableBlockPos pos : positions) {
                    if ( pos.distanceSq(result.hitVec.x, result.hitVec.y, result.hitVec.z) <= 64D ) {
                        final int x = pos.getX();
                        final int y = pos.getY();
                        final int z = pos.getZ();

                        if ( skipped >= skip ) {
                            IBlockState state = world.getBlockState(pos);
                            if ( state.isOpaqueCube() ) {
                                skipped++;
                            } else {
                                skipped = 0;
                                world.spawnParticle(
                                        EnumParticleTypes.WATER_SPLASH,
                                        x, y, z,
                                        x > hitX ? 10F : x == hitX ? 0F : -10F,
                                        y > hitY ? 10F : y == hitY ? 0F : -10F,
                                        z > hitZ ? 10F : z == hitZ ? 0F : -10F
                                );
                            }

                        } else
                            skipped++;
                    }
                }
            }

        } else if ( !world.isRemote ) {
            boolean consumed = ModConfig.items.quenchedPearl.alwaysConsumed;

            if ( positions != null ) {
                final double fireRange = Math.pow(ModConfig.items.quenchedPearl.extinguishFire, 2);
                final double lavaRange = Math.pow(ModConfig.items.quenchedPearl.quenchLava, 2);
                final double maxDistance = Math.max(fireRange, lavaRange);

                boolean sound = false;

                for (BlockPos.MutableBlockPos pos : positions) {
                    if ( !world.isBlockLoaded(pos) )
                        continue;

                    final double distance = pos.distanceSq(result.hitVec.x, result.hitVec.y, result.hitVec.z);
                    if ( distance > maxDistance )
                        continue;

                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    if ( distance <= fireRange && block instanceof BlockFire ) {
                        // Extinguish Fire
                        world.setBlockToAir(pos);
                        sound = true;

                        if ( ModConfig.items.quenchedPearl.fireConsumes )
                            consumed = true;

                    } else if ( distance <= lavaRange && block instanceof BlockLiquid && state.getMaterial() == Material.LAVA ) {
                        // Quench Lava
                        boolean exposed = false;

                        BlockPos.PooledMutableBlockPos facing = BlockPos.PooledMutableBlockPos.retain();
                        for (EnumFacing face : EnumFacing.VALUES) {
                            facing.setPos(pos).move(face);
                            if ( world.isAirBlock(facing) ) {
                                exposed = true;
                                break;
                            }
                        }
                        facing.release();

                        if ( exposed ) {
                            final int level = state.getValue(BlockLiquid.LEVEL);
                            if ( level == 0 )
                                world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                            else if ( level < 4 )
                                world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                            else
                                world.setBlockToAir(pos);

                            sound = true;
                            if ( ModConfig.items.quenchedPearl.lavaConsumes )
                                consumed = true;
                        }
                    }
                }

                if ( sound )
                    world.playSound(null, result.getBlockPos(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
            }

            if ( !consumed )
                dropItemWithMeta(ModItems.itemQuenchedPearl, 1);

            setDead();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityQuenchedPearl> {
        @Override
        public Render<? super EntityQuenchedPearl> createRenderFor(RenderManager manager) {
            return new RenderAsItem<EntityQuenchedPearl>(manager, ModItems.itemQuenchedPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }
}
