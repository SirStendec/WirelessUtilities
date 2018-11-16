package com.lordmau5.wirelessutils.render;

import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.location.BlockArea;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RenderManager {
    public static final RenderManager INSTANCE = new RenderManager();

    // Slightly larger than it strictly needs to be to minimize z fighting due to rounding
    // errors with the player's position.
    public static final double OUTLINE_OFFSET = 0.005;
    public static final double MAX_DISTANCE = 65536;

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private BufferBuilder buffer;

    private float cR;
    private float cG;
    private float cB;
    private float cA;

    private float aNormal = 0.3f;
    private float aFacing = 0f;

    private long lastClean = 0;
    private int lastID = -1;

    private final boolean enabled;

    private Map<Integer, Set<BlockArea>> areas;
    private Map<Integer, BlockArea> areaIDs;
    private Map<Integer, Long> areaTouch;
    private Map<BlockArea, Integer> areaUsers;

    private ItemStack[] heldItemStack = {ItemStack.EMPTY, ItemStack.EMPTY};
    private int[] heldID = {-1, -1};

    public RenderManager() {
        enabled = ModConfig.rendering.enableAreaRenderer;
        if ( enabled ) {
            areas = new HashMap<>();
            areaIDs = new HashMap<>();
            areaTouch = new HashMap<>();
            areaUsers = new HashMap<>();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void cleanAreas() {
        long now = Minecraft.getSystemTime();
        lastClean = now;
        if ( areaTouch == null || areaTouch.isEmpty() || minecraft.isGamePaused() )
            return;

        Set<Map.Entry<Integer, Long>> entries = new HashSet<>(areaTouch.entrySet());
        for (Map.Entry<Integer, Long> entry : entries) {
            if ( now - entry.getValue() > 5000 )
                removeArea(entry.getKey());
        }
    }

    public int addArea(BlockArea area) {
        return addArea(area, true);
    }

    public int addArea(BlockArea area, boolean canExpire) {
        if ( !enabled )
            return -1;

        Set<BlockArea> dimensionAreas = areas.get(area.dimension);
        if ( dimensionAreas == null ) {
            dimensionAreas = new HashSet<>();
            areas.put(area.dimension, dimensionAreas);
        }

        int id = ++lastID;

        dimensionAreas.add(area);
        areaIDs.put(id, area);

        if ( canExpire )
            areaTouch.put(id, Minecraft.getSystemTime());

        areaUsers.put(area, areaUsers.getOrDefault(area, 0) + 1);

        return id;
    }

    public void touchArea(int id) {
        if ( areaTouch == null || areaTouch.isEmpty() )
            return;

        if ( areaTouch.containsKey(id) )
            areaTouch.put(id, Minecraft.getSystemTime());
    }

    public void touchAreas(Iterable<Integer> ids) {
        if ( ids == null || areaTouch == null || areaTouch.isEmpty() )
            return;

        long now = Minecraft.getSystemTime();
        for (int id : ids) {
            if ( areaTouch.containsKey(id) )
                areaTouch.put(id, now);
        }
    }

    public void removeArea(int id) {
        removeArea(id, true);
    }

    private void removeArea(int id, boolean removeTouch) {
        if ( !enabled )
            return;

        BlockArea area = areaIDs.get(id);
        if ( area == null )
            return;

        if ( removeTouch )
            areaTouch.remove(id);

        areaIDs.remove(id);
        if ( areaIDs.isEmpty() )
            lastID = -1;

        int users = areaUsers.getOrDefault(area, 1) - 1;
        if ( users > 0 ) {
            areaUsers.put(area, users);
            return;
        }

        areaUsers.remove(area);

        Set<BlockArea> dimensionAreas = areas.get(area.dimension);
        if ( dimensionAreas == null )
            return;

        dimensionAreas.remove(area);
        if ( dimensionAreas.isEmpty() )
            areas.remove(area.dimension);
    }

    @SubscribeEvent
    public void renderHand(RenderSpecificHandEvent event) {
        int idx = event.getHand() == EnumHand.MAIN_HAND ? 0 : 1;

        ItemStack newStack = event.getItemStack();
        if ( newStack == heldItemStack[idx] )
            return;

        if ( heldID[idx] >= 0 ) {
            removeArea(heldID[idx]);
            heldID[idx] = -1;
        }

        heldItemStack[idx] = newStack;
        if ( newStack.isEmpty() )
            return;

        if ( newStack.getItem() == ModItems.itemPositionalCard && newStack.hasTagCompound() ) {
            BlockPosDimension target = BlockPosDimension.fromTag(newStack.getTagCompound());
            if ( target != null )
                heldID[idx] = addArea(new BlockArea(target, NiceColors.HANDY_COLORS[idx], newStack.hasDisplayName() ? newStack.getDisplayName() : null), false);
        }
    }

    @SubscribeEvent
    public void renderWorldLast(RenderWorldLastEvent event) {
        if ( areas == null || areas.isEmpty() )
            return;

        EntityPlayer player = minecraft.player;
        int dimension = player.world.provider.getDimension();

        Set<BlockArea> dimensionAreas = areas.get(dimension);
        if ( dimensionAreas == null || areas.isEmpty() )
            return;

        boolean disableDepth = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == ModItems.itemGlasses;

        long now = Minecraft.getSystemTime();
        if ( now - lastClean > 5000 )
            cleanAreas();

        float ticks = event.getPartialTicks();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        // Calculate the player's exact position so that we can negate it with a transform.
        // This isn't as precise as we'd like, but it's what everyone does, so
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * ticks;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * ticks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * ticks;

        GlStateManager.translate(-x, -y, -z);

        if ( disableDepth ) {
            GlStateManager.disableDepth();
            render(dimensionAreas, x, y, z, 0.11f, 0.11f, false);
            GlStateManager.enableDepth();
            render(dimensionAreas, x, y, z, 0.19f, 0.19f, true);

        } else
            render(dimensionAreas, x, y, z, 0.25f, 0.3f, true);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void render(Iterable<BlockArea> areas, double x, double y, double z, float opacity, float facingOpacity, boolean drawNames) {
        if ( areas == null )
            return;

        aNormal = opacity;
        aFacing = aNormal + MathHelper.abs(MathHelper.sin((float) (Minecraft.getSystemTime() % 2500L) / 2500.0F * ((float) Math.PI * 2F))) * facingOpacity;

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth(2f);
        GlStateManager.color(1f, 1f, 1f);

        for (BlockArea area : areas) {
            double dX = (double) area.minX - x;
            double dZ = (double) area.minZ - z;

            double distance = dX * dX + dZ * dZ;
            if ( distance > MAX_DISTANCE )
                continue;

            cR = (float) (area.color >> 16 & 0xFF) / 255.0f;
            cG = (float) (area.color >> 8 & 0xFF) / 255.0f;
            cB = (float) (area.color & 0xFF) / 255.0f;

            double minX = area.minX - OUTLINE_OFFSET;
            double minY = area.minY - OUTLINE_OFFSET;
            double minZ = area.minZ - OUTLINE_OFFSET;
            double maxX = area.maxX + OUTLINE_OFFSET;
            double maxY = area.maxY + OUTLINE_OFFSET;
            double maxZ = area.maxZ + OUTLINE_OFFSET;

            drawOutline(minX, minY, minZ, maxX, maxY, maxZ);

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);

            drawFaces(minX, minY, minZ, maxX, maxY, maxZ, area.activeSide);

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }

        GlStateManager.glLineWidth(1f);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        if ( drawNames ) {
            for (BlockArea area : areas) {
                if ( area.name != null )
                    EntityRenderer.drawNameplate(
                            minecraft.fontRenderer,
                            area.name,
                            area.minX + (area.maxX - area.minX) / 2F,
                            area.minY + (area.maxY - area.minY) / 2F,
                            area.minZ + (area.maxZ - area.minZ) / 2F,
                            0,
                            minecraft.player.rotationYaw,
                            minecraft.player.rotationPitch,
                            false,
                            false);
            }
        }
    }

    private RenderManager posEx(double x, double y, double z) {
        buffer.pos(x, y, z).color(cR, cG, cB, cA).endVertex();
        return this;
    }

    public void drawOutline(double x1, double y1, double z1, double x2, double y2, double z2) {
        Tessellator tessellator = Tessellator.getInstance();
        buffer = tessellator.getBuffer();

        cA = 1f;
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        posEx(x1, y1, z1).posEx(x2, y1, z1);
        posEx(x1, y1, z1).posEx(x1, y2, z1);
        posEx(x1, y1, z1).posEx(x1, y1, z2);
        posEx(x2, y2, z2).posEx(x1, y2, z2);

        posEx(x2, y2, z2).posEx(x2, y1, z2);
        posEx(x2, y2, z2).posEx(x2, y2, z1);
        posEx(x1, y2, z1).posEx(x1, y2, z2);
        posEx(x1, y2, z1).posEx(x2, y2, z1);

        posEx(x2, y1, z1).posEx(x2, y1, z2);
        posEx(x2, y1, z1).posEx(x2, y2, z1);
        posEx(x1, y1, z2).posEx(x2, y1, z2);
        posEx(x1, y1, z2).posEx(x1, y2, z2);

        tessellator.draw();
        buffer = null;
    }

    public void drawFaces(double x1, double y1, double z1, double x2, double y2, double z2, EnumFacing facing) {
        Tessellator tessellator = Tessellator.getInstance();
        buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        cA = facing == EnumFacing.NORTH ? aFacing : aNormal;
        posEx(x1, y1, z1).posEx(x1, y2, z1).posEx(x2, y2, z1).posEx(x2, y1, z1);
        posEx(x2, y1, z1).posEx(x2, y2, z1).posEx(x1, y2, z1).posEx(x1, y1, z1);

        cA = facing == EnumFacing.SOUTH ? aFacing : aNormal;
        posEx(x1, y1, z2).posEx(x2, y1, z2).posEx(x2, y2, z2).posEx(x1, y2, z2);
        posEx(x1, y2, z2).posEx(x2, y2, z2).posEx(x2, y1, z2).posEx(x1, y1, z2);

        cA = facing == EnumFacing.DOWN ? aFacing : aNormal;
        posEx(x1, y1, z1).posEx(x2, y1, z1).posEx(x2, y1, z2).posEx(x1, y1, z2);
        posEx(x1, y1, z2).posEx(x2, y1, z2).posEx(x2, y1, z1).posEx(x1, y1, z1);

        cA = facing == EnumFacing.UP ? aFacing : aNormal;
        posEx(x1, y2, z1).posEx(x1, y2, z2).posEx(x2, y2, z2).posEx(x2, y2, z1);
        posEx(x2, y2, z1).posEx(x2, y2, z2).posEx(x1, y2, z2).posEx(x1, y2, z1);

        cA = facing == EnumFacing.WEST ? aFacing : aNormal;
        posEx(x1, y1, z1).posEx(x1, y1, z2).posEx(x1, y2, z2).posEx(x1, y2, z1);
        posEx(x1, y2, z1).posEx(x1, y2, z2).posEx(x1, y1, z2).posEx(x1, y1, z1);

        cA = facing == EnumFacing.EAST ? aFacing : aNormal;
        posEx(x2, y1, z1).posEx(x2, y2, z1).posEx(x2, y2, z2).posEx(x2, y1, z2);
        posEx(x2, y1, z2).posEx(x2, y2, z2).posEx(x2, y2, z1).posEx(x2, y1, z1);

        tessellator.draw();
        buffer = null;
    }

}
