package com.lordmau5.wirelessutils.utils.mod;

import com.lordmau5.wirelessutils.block.BlockDirectionalAir;
import com.lordmau5.wirelessutils.block.charger.BlockChunkCharger;
import com.lordmau5.wirelessutils.block.charger.BlockDirectionalCharger;
import com.lordmau5.wirelessutils.block.charger.BlockPositionalCharger;
import com.lordmau5.wirelessutils.block.condenser.BlockDirectionalCondenser;
import com.lordmau5.wirelessutils.block.condenser.BlockPositionalCondenser;
import com.lordmau5.wirelessutils.block.desublimator.BlockDirectionalDesublimator;
import com.lordmau5.wirelessutils.block.desublimator.BlockPositionalDesublimator;
import com.lordmau5.wirelessutils.block.redstone.BlockPoweredAir;
import com.lordmau5.wirelessutils.block.redstone.BlockPoweredRedstoneWire;
import com.lordmau5.wirelessutils.block.slime.BlockAngledSlime;
import com.lordmau5.wirelessutils.block.vaporizer.BlockDirectionalVaporizer;
import com.lordmau5.wirelessutils.block.vaporizer.BlockPositionalVaporizer;
import com.lordmau5.wirelessutils.utils.ColorHandler;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {

    @GameRegistry.ObjectHolder("wirelessutils:directional_charger")
    public static BlockDirectionalCharger blockDirectionalCharger;

    @GameRegistry.ObjectHolder("wirelessutils:positional_charger")
    public static BlockPositionalCharger blockPositionalCharger;

    @GameRegistry.ObjectHolder("wirelessutils:chunk_charger")
    public static BlockChunkCharger blockChunkCharger;

    @GameRegistry.ObjectHolder("wirelessutils:directional_condenser")
    public static BlockDirectionalCondenser blockDirectionalCondenser;

    @GameRegistry.ObjectHolder("wirelessutils:positional_condenser")
    public static BlockPositionalCondenser blockPositionalCondenser;

    @GameRegistry.ObjectHolder("wirelessutils:directional_desublimator")
    public static BlockDirectionalDesublimator blockDirectionalDesublimator;

    @GameRegistry.ObjectHolder("wirelessutils:positional_desublimator")
    public static BlockPositionalDesublimator blockPositionalDesublimator;

    @GameRegistry.ObjectHolder("wirelessutils:directional_vaporizer")
    public static BlockDirectionalVaporizer blockDirectionalVaporizer;

    @GameRegistry.ObjectHolder("wirelessutils:positional_vaporizer")
    public static BlockPositionalVaporizer blockPositionalVaporizer;

    @GameRegistry.ObjectHolder("wirelessutils:powered_air")
    public static BlockPoweredAir blockPoweredAir;

    @GameRegistry.ObjectHolder("wirelessutils:powered_redstone_wire")
    public static BlockPoweredRedstoneWire blockPoweredRedstoneWire;

    @GameRegistry.ObjectHolder("wirelessutils:directional_air")
    public static BlockDirectionalAir blockDirectionalAir;

    @GameRegistry.ObjectHolder("wirelessutils:angled_slime")
    public static BlockAngledSlime blockAngledSlime;

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        blockDirectionalCharger.initModel();
        blockPositionalCharger.initModel();
        //blockChunkCharger.initModel();

        blockDirectionalCondenser.initModel();
        blockPositionalCondenser.initModel();

        blockDirectionalDesublimator.initModel();
        blockPositionalDesublimator.initModel();

        blockDirectionalVaporizer.initModel();
        blockPositionalVaporizer.initModel();

        blockAngledSlime.initModel();
    }

    @SideOnly(Side.CLIENT)
    public static void initColors(BlockColors blockColors) {
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockDirectionalCharger);
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockPositionalCharger);
        //blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockChunkCharger);

        blockColors.registerBlockColorHandler(ColorHandler.Condenser.handleBlockColor, blockDirectionalCondenser);
        blockColors.registerBlockColorHandler(ColorHandler.Condenser.handleBlockColor, blockPositionalCondenser);

        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockDirectionalDesublimator);
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockPositionalDesublimator);

        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockDirectionalVaporizer);
        blockColors.registerBlockColorHandler(ColorHandler.Machine.handleBlockColor, blockPositionalVaporizer);

        blockColors.registerBlockColorHandler(ColorHandler.RedstoneWire.handleBlockColor, blockPoweredRedstoneWire);
    }
}
