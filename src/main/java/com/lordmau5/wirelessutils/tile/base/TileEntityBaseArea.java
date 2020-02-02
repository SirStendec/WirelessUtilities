package com.lordmau5.wirelessutils.tile.base;

import com.lordmau5.wirelessutils.render.IAreaProvider;
import com.lordmau5.wirelessutils.render.RenderManager;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockArea;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TileEntityBaseArea extends TileEntityBase implements IAreaProvider, IAreaVisibilityControllable {
    private Set<Integer> liveAreas = null;
    private List<BlockArea> renderedAreas = null;

    private boolean showRenderAreas = false;
    private boolean isLive = false;
    //private long lastTouch = 0;

    private int defaultColor = -1;

    public boolean usesDefaultColor() {
        return false;
    }

    public int getDefaultColor() {
        if ( defaultColor == -1 ) {
            if ( world != null )
                defaultColor = world.rand.nextInt(NiceColors.COLORS.length);
            else
                return 0;
        }

        return defaultColor;
    }

    public void setDefaultColor(int color) {
        int count = NiceColors.COLORS.length;
        if ( count == 0 )
            return;

        while ( color < 0 )
            color += count;
        while ( color >= count )
            color -= count;

        if ( color < 0 )
            color = 0;
        if ( color >= count )
            color = count - 1;

        defaultColor = color;
    }

    public void onRenderAreasEnabled() {

    }

    public void onRenderAreasDisabled() {

    }

    public void onRenderAreasCleared() {

    }

    private void enableRendering() {
        if ( isLive )
            return;

        if ( liveAreas == null )
            liveAreas = new HashSet<>();

        setWatchUnload();
        isLive = true;
        //lastTouch = Minecraft.getSystemTime();

        onRenderAreasEnabled();

        if ( renderedAreas == null || renderedAreas.isEmpty() )
            return;

        for (BlockArea area : renderedAreas) {
            int id = RenderManager.INSTANCE.addArea(area, false);
            liveAreas.add(id);
        }
    }

    private void disableRendering() {
        isLive = false;

        onRenderAreasDisabled();

        if ( liveAreas == null || liveAreas.isEmpty() ) {
            liveAreas = null;
            return;
        }

        for (int id : liveAreas)
            RenderManager.INSTANCE.removeArea(id);

        isLive = false;
        liveAreas = null;
    }

    // There isn't really a good method for a tile entity unloading, so
    // we just kind of throw events at a wall to see what sticks.
    @Override
    public void onDestroy() {
        super.onDestroy();
        disableRendering();
    }

    @Override
    public List<BlockArea> getRenderedAreas() {
        return renderedAreas;
    }

    @Override
    public boolean shouldRenderAreas() {
        return showRenderAreas;
    }

    public void enableRenderAreas() {
        enableRenderAreas(true);
    }

    public void disableRenderAreas() {
        enableRenderAreas(false);
    }

    public void enableRenderAreas(boolean enabled) {
        if ( enabled == showRenderAreas )
            return;

        showRenderAreas = enabled;
        if ( enabled )
            enableRendering();
        else
            disableRendering();
    }

    public void addRenderArea(BlockArea area) {
        if ( renderedAreas == null ) {
            renderedAreas = new ArrayList<>();
        }

        renderedAreas.add(area);

        if ( isLive && liveAreas != null ) {
            int id = RenderManager.INSTANCE.addArea(area, false);
            liveAreas.add(id);
        }
    }

    public void addRenderArea(BlockPosDimension cornerA, BlockPos cornerB) {
        addRenderArea(cornerA, cornerB, NiceColors.COLORS[getDefaultColor()]);
    }

    public void addRenderArea(BlockPosDimension cornerA, BlockPos cornerB, int color) {
        addRenderArea(new BlockArea(cornerA, cornerB, color));
    }

    public void addRenderArea(BlockPosDimension pos) {
        addRenderArea(pos, NiceColors.COLORS[getDefaultColor()]);
    }

    public void addRenderArea(BlockPosDimension pos, int color) {
        addRenderArea(new BlockArea(pos, color));
    }

    public void addRenderArea(BlockPosDimension cornerA, BlockPos cornerB, String name) {
        addRenderArea(cornerA, cornerB, NiceColors.COLORS[getDefaultColor()], name);
    }

    public void addRenderArea(BlockPosDimension cornerA, BlockPos cornerB, String name, Vec3d vector) {
        addRenderArea(cornerA, cornerB, NiceColors.COLORS[getDefaultColor()], name, vector);
    }

    public void addRenderArea(BlockPosDimension cornerA, BlockPos cornerB, int color, String name) {
        addRenderArea(new BlockArea(cornerA, cornerB, color, name));
    }

    public void addRenderArea(BlockPosDimension cornerA, BlockPos cornerB, int color, String name, Vec3d vector) {
        addRenderArea(new BlockArea(cornerA, cornerB, color, name, vector));
    }

    public void addRenderArea(BlockPosDimension pos, String name) {
        addRenderArea(pos, NiceColors.COLORS[getDefaultColor()], name);
    }

    public void addRenderArea(BlockPosDimension pos, String name, Vec3d vector) {
        addRenderArea(pos, NiceColors.COLORS[getDefaultColor()], name, vector);
    }

    public void addRenderArea(BlockPosDimension pos, int color, String name) {
        addRenderArea(new BlockArea(pos, color, name));
    }

    public void addRenderArea(BlockPosDimension pos, int color, String name, Vec3d vector) {
        addRenderArea(new BlockArea(pos, color, name, vector));
    }

    public void clearRenderAreas() {
        if ( isLive && liveAreas != null ) {
            for (int id : liveAreas)
                RenderManager.INSTANCE.removeArea(id);

            liveAreas.clear();
        }

        if ( renderedAreas != null ) {
            renderedAreas.clear();
        }

        onRenderAreasCleared();
    }
}
