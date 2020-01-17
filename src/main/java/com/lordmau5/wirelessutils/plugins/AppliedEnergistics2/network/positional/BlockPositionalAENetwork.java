package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.positional;

import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AEColorHelpers;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.tile.TilePositionalAENetwork;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockPositionalAENetwork extends BlockBaseMachine {

    public BlockPositionalAENetwork() {
        super();

        setName("positional_ae_network");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TilePositionalAENetwork.class;
    }

    public boolean isStackDense(@Nonnull ItemStack stack) {
        boolean[] dense = ModConfig.plugins.appliedEnergistics.positionalAENetwork.dense;
        int idx = Level.fromItemStack(stack).toInt();
        if ( idx < 0 )
            idx = 0;
        else if ( idx >= dense.length )
            idx = dense.length - 1;

        return dense[idx];
    }

    @Override
    public void addItemStackInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addItemStackInformation(stack, worldIn, tooltip, flagIn);

        if ( AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) )
            tooltip.add(new TextComponentTranslation(
                    "info." + WirelessUtils.MODID + ".ae2.max_channels",
                    TextHelpers.getComponent(isStackDense(stack) ? 32 : 8)
            ).getFormattedText());

        if ( ModConfig.plugins.appliedEnergistics.enableColor ) {
            AEColor color = AEColorHelpers.fromItemStack(stack);
            tooltip.add(new TextComponentTranslation("btn." + WirelessUtils.MODID + ".ae_color", color.toString()).getFormattedText());
        }
    }


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        // TODO: Call unlink?

        super.breakBlock(world, pos, state);
    }
}
