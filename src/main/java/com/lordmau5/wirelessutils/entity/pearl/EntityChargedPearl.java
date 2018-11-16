package com.lordmau5.wirelessutils.entity.pearl;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.render.RenderPearl;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.BlockLiquidWrapper;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityChargedPearl extends EntityBaseThrowable {

    private BlockPos lastWaterCheck;

    public EntityChargedPearl(World world) {
        super(world);
    }

    public EntityChargedPearl(World world, ItemStack stack) {
        super(world, stack);
    }

    public EntityChargedPearl(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public EntityChargedPearl(World world, EntityLivingBase thrower, ItemStack stack) {
        super(world, thrower, stack);
    }

    public EntityChargedPearl(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityChargedPearl(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
    }

    protected boolean tryCharge(IEnergyStorage storage) {
        if ( storage == null || !storage.canReceive() )
            return false;

        final int energy = ModConfig.items.chargedPearl.chargeEnergy;
        int remaining = energy;

        for (int i = 0; i < ModConfig.items.chargedPearl.chargeAttempts; i++) {
            int accepted = storage.receiveEnergy(remaining, false);
            remaining -= accepted;
            if ( accepted == 0 )
                break;
        }

        return remaining != energy;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if ( world.isRemote || !(isInWater() || isInLava()) )
            return;

        BlockPos pos = getPosition();
        if ( pos == lastWaterCheck )
            return;

        lastWaterCheck = pos;
        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();

        boolean lava = false;
        if ( ModConfig.items.chargedPearl.enableScorching && material == Material.LAVA )
            lava = true;

        boolean water = false;
        if ( ModConfig.items.chargedPearl.enableQuenching && material == Material.WATER )
            water = true;

        if ( !lava && !water )
            return;

        IFluidHandler handler = new BlockLiquidWrapper((BlockLiquid) state.getBlock(), world, pos);
        IFluidTankProperties[] properties = handler.getTankProperties();
        if ( properties == null || properties.length != 1 )
            return;

        FluidStack fluid = properties[0].getContents();
        if ( fluid == null || fluid.amount != properties[0].getCapacity() )
            return;

        EntityLivingBase thrower = getThrower();
        ItemStack stack = getStack();
        EntityBaseThrowable newPearl;

        if ( water && fluid.getFluid() == FluidRegistry.WATER ) {
            stack = new ItemStack(ModItems.itemQuenchedPearl, 1, stack == null ? 0 : stack.getMetadata());

            if ( thrower != null )
                newPearl = new EntityQuenchedPearl(world, thrower, stack);
            else
                newPearl = new EntityQuenchedPearl(world, stack);

            if ( world instanceof WorldServer ) {
                WorldServer ws = (WorldServer) world;
                ws.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX, posY, posZ, 3, .2D, .2D, .2D, 0D);
            }

            playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 0.4F, 2.0F + rand.nextFloat() * 0.4F);

        } else if ( lava && fluid.getFluid() == FluidRegistry.LAVA ) {
            stack = new ItemStack(ModItems.itemScorchedPearl, 1, stack == null ? 0 : stack.getMetadata());

            if ( thrower != null )
                newPearl = new EntityScorchedPearl(world, thrower, stack);
            else
                newPearl = new EntityScorchedPearl(world, stack);

            if ( world instanceof WorldServer ) {
                WorldServer ws = (WorldServer) world;
                ws.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 3, .2D, .2D, .2D, 0D);
            }

            playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + rand.nextFloat() * 0.4F);

        } else
            return;

        newPearl.setPosition(posX, posY, posZ);
        newPearl.setVelocity(motionX, motionY, motionZ);
        world.spawnEntity(newPearl);
        world.setBlockToAir(pos);
        setDead();
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if ( result.entityHit != null ) {
            if ( result.entityHit == getThrower() )
                return;
        }

        if ( result.typeOfHit == RayTraceResult.Type.BLOCK ) {
            if ( world.isRemote ) {
//                setDead();
                return;
            }

            if ( ModConfig.items.chargedPearl.enableChargeMachines ) {
                BlockPos pos = result.getBlockPos();
                TileEntity tile = world.getTileEntity(pos);
                if ( tile != null ) {
                    boolean charged = false;
                    if ( tile.hasCapability(CapabilityEnergy.ENERGY, result.sideHit) )
                        charged = tryCharge(tile.getCapability(CapabilityEnergy.ENERGY, result.sideHit));
                    if ( !charged && tile.hasCapability(CapabilityEnergy.ENERGY, null) )
                        charged = tryCharge(tile.getCapability(CapabilityEnergy.ENERGY, null));

                    if ( charged ) {
                        if ( world instanceof WorldServer ) {
                            WorldServer ws = (WorldServer) world;
                            ws.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, result.hitVec.x, result.hitVec.y, result.hitVec.z, 3, 0D, 0D, 0D, 0D);
                        }

                        LootTable table = world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(WirelessUtils.MODID + ":charged_pearl_drops"));
                        LootContext ctx = new LootContext.Builder((WorldServer) world).build();

                        List<ItemStack> stacks = table.generateLootForPools(world.rand, ctx);

                        if ( stacks != null ) {
                            for (ItemStack stack : stacks) {
                                dropItemStack(stack);
                            }
                        }

                        setDead();
                        return;
                    }
                }
            }

            dropItemWithMeta(ModItems.itemChargedPearl, 1);
            setDead();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IRenderFactory<EntityChargedPearl> {
        @Override
        public Render<? super EntityChargedPearl> createRenderFor(RenderManager manager) {
            return new RenderPearl<EntityChargedPearl>(manager, ModItems.itemChargedPearl, Minecraft.getMinecraft().getRenderItem());
        }
    }
}
