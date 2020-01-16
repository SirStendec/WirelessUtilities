package com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.network.directional;

import appeng.api.util.AEColor;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.plugins.AppliedEnergistics2.AEColorHelpers;
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

public class BlockDirectionalAENetwork extends BlockBaseMachine {

    public BlockDirectionalAENetwork() {
        super();

        setName("directional_ae_network");
    }

    @Nullable
    @Override
    public Class<? extends TileEntity> getTileEntityClass() {
        return TileDirectionalAENetwork.class;
    }

    @Override
    public void addItemStackInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addItemStackInformation(stack, worldIn, tooltip, flagIn);

        if ( ModConfig.plugins.appliedEnergistics.enableColor ) {
            AEColor color = AEColorHelpers.fromItemStack(stack);
            tooltip.add(new TextComponentTranslation("btn." + WirelessUtils.MODID + ".ae_color", color.toString()).getFormattedText());
        }
    }


    //    @Override
//    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
//        super.onBlockPlacedBy(world, pos, state, placer, stack);
//
//        if ( !world.isRemote ) {
//            TileAENetworkBase tile = (TileAENetworkBase) world.getTileEntity(pos);
//
//            if ( tile != null ) {
//                tile.setNeedsRecalculation();
//
//                if ( placer instanceof EntityPlayer ) {
//                    GameProfile profile = ((EntityPlayer) placer).getGameProfile();
//                    int playerID = AEApi.instance().registries().players().getID(profile);
//
//                    tile.getNode().setPlayerID(playerID);
//                }
//            }
//        }
//    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        // TODO: Call unlink?

        super.breakBlock(world, pos, state);
    }
}
