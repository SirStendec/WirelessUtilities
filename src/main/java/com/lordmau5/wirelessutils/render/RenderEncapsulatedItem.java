package com.lordmau5.wirelessutils.render;

import com.lordmau5.wirelessutils.entity.pearl.EntityEncapsulatedItem;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderEncapsulatedItem extends Render<EntityEncapsulatedItem> {

    private final RenderItem itemRenderer;
    private final ItemStack itemPearl;

    public RenderEncapsulatedItem(RenderManager manager, RenderItem renderItem) {
        super(manager);
        itemPearl = new ItemStack(ModItems.itemEncapsulatedItem);
        this.itemRenderer = renderItem;
    }

    @Nonnull
    public ItemStack getStackToRender(@Nonnull EntityEncapsulatedItem entity) {
        return entity.getRenderStack();
    }

    @Override
    public void doRender(@Nonnull EntityEncapsulatedItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        final ItemStack render = getStackToRender(entity);
        final boolean slimed = ModConfig.rendering.slimedItems;

        // Item
        if ( !render.isEmpty() ) {
            // TODO: Fix this logic so that it renders perfectly centered behind the bubble.

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.enableRescaleNormal();
            GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) (renderManager.options.thirdPersonView == 2 ? -1 : 1) * renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

            if ( slimed )
                GlStateManager.translate(0, 0, 0.1);

            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

            if ( slimed )
                GlStateManager.scale(0.75, 0.75, 0.75);

            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            if ( !slimed && renderOutlines ) {
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(getTeamColor(entity));
            }

            itemRenderer.renderItem(render, ItemCameraTransforms.TransformType.GROUND);

            if ( !slimed && renderOutlines ) {
                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }

        // Bubble
        if ( slimed ) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.enableRescaleNormal();
            GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate((float) (renderManager.options.thirdPersonView == 2 ? -1 : 1) * renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            if ( renderOutlines ) {
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(getTeamColor(entity));
            }

            itemRenderer.renderItem(itemPearl, ItemCameraTransforms.TransformType.GROUND);

            if ( renderOutlines ) {
                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityEncapsulatedItem entityEncapsulatedItem) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
