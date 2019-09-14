package com.lordmau5.wirelessutils.item;

import cofh.core.util.CoreUtils;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ItemBasePositionalCard;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.location.BlockPosDimension;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemAbsolutePositionalCard extends ItemBasePositionalCard {

    public ItemAbsolutePositionalCard() {
        setName("positional_card");

        addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            public float apply(@Nonnull ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
                if ( (entityIn == null && !stack.isOnItemFrame()) )
                    return -1F;

                boolean onEntity = entityIn != null;
                Entity entity = onEntity ? entityIn : stack.getItemFrame();
                if ( entity == null )
                    return -1F;

                if ( world == null )
                    world = entity.world;

                BlockPosDimension origin = new BlockPosDimension(entity.getPosition(), world.provider.getDimension());
                BlockPosDimension target = getTarget(stack, origin);
                if ( target == null )
                    return -1F;

                double d0;

                if ( world.provider.getDimension() != target.getDimension() ) {
                    d0 = Math.random();
                } else {
                    double d1 = onEntity ? (double) entity.rotationYaw : getFrameRotation((EntityItemFrame) entity);
                    d1 = MathHelper.positiveModulo(d1 / 360.0D, 1.0D);
                    double d2 = getTargetToAngle(entity, target) / (Math.PI * 2D);
                    d0 = 0.5D - (d1 - 0.25D - d2);
                }

                return MathHelper.positiveModulo((float) d0, 1.0F);
            }

            @SideOnly(Side.CLIENT)
            private double getFrameRotation(EntityItemFrame frame) {
                if ( frame == null || frame.facingDirection == null )
                    return -1D;

                return MathHelper.wrapDegrees(180 + frame.facingDirection.getHorizontalIndex() * 90);
            }

            @SideOnly(Side.CLIENT)
            private double getTargetToAngle(@Nonnull Entity entity, @Nonnull BlockPos target) {
                return Math.atan2((double) target.getZ() - entity.posZ, (double) target.getX() - entity.posX);
            }
        });
    }

    @Override
    public boolean isCardConfigured(@Nonnull ItemStack stack) {
        return stack.hasTagCompound() && BlockPosDimension.fromTag(stack.getTagCompound()) != null;
    }

    @Override
    public BlockPosDimension getTarget(@Nonnull ItemStack stack, @Nonnull BlockPosDimension origin) {
        if ( !stack.hasTagCompound() )
            return null;

        BlockPosDimension out = BlockPosDimension.fromTag(stack.getTagCompound());
        World world = DimensionManager.getWorld(out.getDimension(), false);
        if ( world != null && world.isOutsideBuildHeight(out) )
            return null;

        return out;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        if ( stack.hasTagCompound() ) {
            BlockPosDimension target = BlockPosDimension.fromTag(stack.getTagCompound());
            if ( target != null ) {
                DimensionType type;
                try {
                    type = DimensionManager.getProviderType(target.getDimension());
                } catch (IllegalArgumentException err) {
                    type = null;
                }

                tooltip.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".blockpos.basic",
                        TextHelpers.getComponent(target.getX()),
                        TextHelpers.getComponent(target.getY()),
                        TextHelpers.getComponent(target.getZ())
                ).setStyle(TextHelpers.GRAY).getFormattedText());

                if ( target.getFacing() != null )
                    tooltip.add(new TextComponentTranslation(
                            "info." + WirelessUtils.MODID + ".blockpos.side",
                            TextHelpers.getComponent(target.getFacing().getName())
                    ).setStyle(TextHelpers.GRAY).getFormattedText());

                tooltip.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".blockpos.dimension." + (type == null ? "simple" : "named"),
                        TextHelpers.getComponent(target.getDimension()),
                        type == null ? null : TextHelpers.getComponent(type.getName())
                ).setStyle(TextHelpers.GRAY).getFormattedText());
            }
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public String getHighlightTip(@Nonnull ItemStack item, @Nonnull String displayName) {
        displayName = super.getHighlightTip(item, displayName);

        if ( item.hasTagCompound() ) {
            BlockPosDimension target = BlockPosDimension.fromTag(item.getTagCompound());
            if ( target != null ) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                World world = player != null ? player.getEntityWorld() : null;
                int dimension = 0;
                if ( world != null )
                    dimension = world.provider.getDimension();

                displayName = new TextComponentTranslation(
                        getTranslationKey() + ".highlight",
                        displayName,
                        new TextComponentTranslation(
                                dimension == target.getDimension() ? "info." + WirelessUtils.MODID + ".blockpos.basic" : "info." + WirelessUtils.MODID + ".blockpos.full",
                                target.getX(), target.getY(), target.getZ(), target.getDimension()
                        ).setStyle(new Style().setColor(TextFormatting.AQUA).setItalic(true))
                ).getFormattedText();
            }
        }

        return displayName;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        if ( !world.isRemote && player.isSneaking() && hand == EnumHand.MAIN_HAND ) {
            ItemStack stack = player.getHeldItemMainhand();
            RayTraceResult ray = rayTrace(world, player, false);
            if ( ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK )
                return new ActionResult<>(EnumActionResult.PASS, stack);

            NBTTagCompound tag = stack.getTagCompound();
            if ( tag == null || tag.getBoolean("Locked") )
                return new ActionResult<>(EnumActionResult.PASS, stack);
            else if ( stack.getCount() > 1 )
                tag = tag.copy();

            BlockPosDimension.removeFromTag(tag);

            if ( tag.isEmpty() )
                tag = null;

            if ( stack.getCount() == 1 ) {
                stack.setTagCompound(tag);
            } else {
                ItemStack newStack = new ItemStack(this, 1);
                newStack.setTagCompound(tag);
                stack.shrink(1);
                if ( !player.addItemStackToInventory(newStack) )
                    CoreUtils.dropItemStackIntoWorldWithVelocity(newStack, world, player.getPositionVector());
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if ( !world.isRemote && player.isSneaking() && hand == EnumHand.MAIN_HAND ) {
            ItemStack stack = player.getHeldItemMainhand();
            BlockPosDimension target = new BlockPosDimension(pos, world.provider.getDimension(), side);
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag == null )
                tag = new NBTTagCompound();
            else if ( tag.getBoolean("Locked") )
                return EnumActionResult.PASS;
            else if ( stack.getCount() > 1 )
                tag = tag.copy();

            target.writeToTag(tag);

            if ( stack.getCount() == 1 ) {
                stack.setTagCompound(tag);
            } else {
                ItemStack newStack = new ItemStack(this, 1);
                newStack.setTagCompound(tag);
                stack.shrink(1);
                if ( !player.addItemStackToInventory(newStack) )
                    CoreUtils.dropItemStackIntoWorldWithVelocity(newStack, world, player.getPositionVector());
            }

            if ( player instanceof EntityPlayerMP )
                ModAdvancements.SET_POSITIONAL_CARD.trigger((EntityPlayerMP) player);

            return EnumActionResult.SUCCESS;
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }
}
