package com.lordmau5.wirelessutils.utils;

import com.lordmau5.wirelessutils.tile.condenser.TileEntityBaseCondenser;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Map;

import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemFluidGenAugment;
import static com.lordmau5.wirelessutils.utils.mod.ModItems.itemRangeAugment;

public class ColorHandler {

    public static Map<String, Integer> fluidColorMap = new Object2IntOpenHashMap<>();

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

    public static class Machine {
        public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
            if ( tintIndex == 2 ) {
                Level level = Level.getMinLevel();
                if ( !stack.isEmpty() )
                    level = Level.fromInt(stack.getMetadata());

                return level.color;
            }

            return 0xFFFFFF;
        };

        public static final IBlockColor handleBlockColor = (IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) -> {
            if ( tintIndex == 2 ) {
                Level level = Level.fromInt(state.getValue(Properties.LEVEL));
                return level.color;
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
                Level level = Level.fromInt(state.getValue(Properties.LEVEL));
                return level.color;
            }

            return 0xFFFFFF;
        };
    }

    public static class RedstoneWire {
        public static final IBlockColor handleBlockColor = (IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) ->
                BlockRedstoneWire.colorMultiplier(state.getValue(BlockRedstoneWire.POWER));
    }

    public static class Augment {
        public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
            if ( tintIndex == 0 || tintIndex == 1 )
                return 0xFFFFFF;

            return Level.fromAugment(stack).color;
        };

        public static class FluidGen {
            public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
                if ( tintIndex == 0 || tintIndex == 1 )
                    return 0xFFFFFF;

                NBTTagCompound tag = stack.getTagCompound();
                if ( tag != null && tag.hasKey("Color") )
                    return tag.getInteger("Color");

                FluidStack fluidStack = itemFluidGenAugment.getFluid(stack);
                return getFluidColor(fluidStack);
            };
        }

        public static class Range {
            public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
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

    public static class LevelUpgrade {
        public static final IItemColor handleItemColor = (ItemStack stack, int tintIndex) -> {
            if ( tintIndex == 0 )
                return 0xFFFFFF;

            Level level = Level.getMinLevel();
            if ( !stack.isEmpty() )
                level = Level.fromInt(stack.getMetadata());

            return level.color;
        };
    }

}
