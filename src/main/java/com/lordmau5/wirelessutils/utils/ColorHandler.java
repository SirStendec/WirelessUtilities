package com.lordmau5.wirelessutils.utils;

import com.lordmau5.wirelessutils.item.base.ItemBaseVoidPearl;
import com.lordmau5.wirelessutils.tile.base.ILevellingBlock;
import com.lordmau5.wirelessutils.tile.condenser.TileEntityBaseCondenser;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemFilterAugment;
import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemFluidGenAugment;
import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemRangeAugment;

public class ColorHandler {

    public static final Map<String, Integer> fluidColorMap = new Object2IntOpenHashMap<>();

    static {
        fluidColorMap.put("water", 0x3043D9);
        fluidColorMap.put("lava", 0xFF6512);
        fluidColorMap.put("mushroom_stew", 0xB48F6E);
    }

    public static int getFluidColor(@Nullable FluidStack stack) {
        Fluid fluid = stack == null ? null : stack.getFluid();
        if ( fluid == null )
            return 0xFFFFFF;

        String name = fluid.getName();
        if ( fluidColorMap.containsKey(name) )
            return fluidColorMap.get(name);

        return fluid.getColor(stack);
    }

    // TODO: Convert to model_properties system from LayeredTemplateModel?
    public static class Machine {
        public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
            if ( tintIndex == 2 ) {
                Level level = Level.getMinLevel();
                if ( !stack.isEmpty() )
                    level = Level.fromItemStack(stack);

                return level.color;
            }

            return 0xFFFFFF;
        };

        public static final IBlockColor handleBlockColor = (IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) -> {
            if ( tintIndex == 2 ) {
                if ( worldIn != null && pos != null ) {
                    TileEntity tile = worldIn.getTileEntity(pos);
                    if ( tile instanceof ILevellingBlock ) {
                        return ((ILevellingBlock) tile).getLevel().color;
                    }
                }
            }

            return 0xFFFFFF;
        };
    }

    public static class Condenser {
        public static final IBlockColor handleBlockColor = (IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) -> {
            if ( tintIndex == 1 && worldIn != null && pos != null ) {
                TileEntity tile = worldIn.getTileEntity(pos);
                if ( tile instanceof TileEntityBaseCondenser ) {
                    TileEntityBaseCondenser condenser = (TileEntityBaseCondenser) tile;
                    FluidStack stack = condenser.getTankFluid();
                    if ( stack != null )
                        return getFluidColor(stack);
                }
            }

            if ( tintIndex == 2 ) {
                if ( worldIn != null && pos != null ) {
                    TileEntity tile = worldIn.getTileEntity(pos);
                    if ( tile instanceof ILevellingBlock ) {
                        return ((ILevellingBlock) tile).getLevel().color;
                    }
                }
            }

            return 0xFFFFFF;
        };
    }

    public static class RedstoneWire {
        public static final IBlockColor handleBlockColor = (IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) ->
                BlockRedstoneWire.colorMultiplier(state.getValue(BlockRedstoneWire.POWER));
    }

    public static class AreaCard {
        public static final IItemColor handleItemColor = (@Nonnull ItemStack stack, int tintIndex) -> {
            final NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("WUTint:" + tintIndex, Constants.NBT.TAG_INT) )
                return tag.getInteger("WUTint:" + tintIndex);

            if ( tintIndex == 0 || tintIndex == 1 )
                return 0xFFFFFF;

            return Level.fromItemStack(stack).color;
        };
    }

    public static class Augment {
        public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("WUTint:" + tintIndex, Constants.NBT.TAG_INT) )
                return tag.getInteger("WUTint:" + tintIndex);

            if ( tintIndex == 0 || tintIndex == 1 )
                return 0xFFFFFF;

            return Level.fromAugment(stack).color;
        };

        public static class Filter {
            public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
                final NBTTagCompound tag = stack.getTagCompound();
                if ( tag != null && tag.hasKey("WUTint:" + tintIndex, Constants.NBT.TAG_INT) )
                    return tag.getInteger("WUTint:" + tintIndex);

                final boolean whitelist = itemFilterAugment.isWhitelist(stack);
                if ( tintIndex == 0 && !whitelist )
                    return 0x333333;

                if ( tintIndex == 0 || tintIndex == 1 )
                    return 0xFFFFFF;

                return Level.fromAugment(stack).color;
            };
        }

        public static class FluidGen {
            public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
                NBTTagCompound tag = stack.getTagCompound();
                if ( tag != null && tag.hasKey("WUTint:" + tintIndex, Constants.NBT.TAG_INT) )
                    return tag.getInteger("WUTint:" + tintIndex);

                if ( tintIndex == 0 || tintIndex == 1 )
                    return 0xFFFFFF;

                if ( tag != null && tag.hasKey("Color") )
                    return tag.getInteger("Color");

                FluidStack fluidStack = itemFluidGenAugment.getFluid(stack);
                return getFluidColor(fluidStack);
            };
        }

        public static class Range {
            public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
                NBTTagCompound tag = stack.getTagCompound();
                if ( tag != null && tag.hasKey("WUTint:" + tintIndex, Constants.NBT.TAG_INT) )
                    return tag.getInteger("WUTint:" + tintIndex);

                if ( tintIndex == 0 ) {
                    if ( itemRangeAugment.isInterdimensional(stack) )
                        return 0x4DA34D;

                    return 0xFFFFFF;
                }

                if ( tintIndex == 1 )
                    return 0xFFFFFF;

                if ( itemRangeAugment.isInterdimensional(stack) )
                    return 0x004000;

                return Level.fromAugment(stack).color;
            };
        }
    }

    public static class VoidPearl {
        public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
            if ( tintIndex == 0 )
                return 0xFFFFFF;

            EntityList.EntityEggInfo info = null;
            Item item = stack.getItem();
            if ( item instanceof ItemBaseVoidPearl && ((ItemBaseVoidPearl) item).isFilledBall(stack) )
                info = EntityList.ENTITY_EGGS.get(((ItemBaseVoidPearl) item).getEntityId(stack));
            else if ( stack.getItemDamage() != 0 ) {
                Class<? extends Entity> klass = EntityList.getClassFromID(stack.getItemDamage());
                ResourceLocation key = klass == null ? null : EntityList.getKey(klass);
                info = key == null ? null : EntityList.ENTITY_EGGS.get(key);
            }

            if ( info == null )
                return 0x002f45;

            return info.primaryColor;
        };
    }

    public static class LevelUpgrade {
        public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("WUTint:" + tintIndex, Constants.NBT.TAG_INT) )
                return tag.getInteger("WUTint:" + tintIndex);

            if ( tintIndex == 0 )
                return 0xFFFFFF;

            Level level = Level.getMinLevel();
            if ( !stack.isEmpty() )
                level = Level.fromInt(stack.getMetadata());

            return level.color;
        };
    }

}
