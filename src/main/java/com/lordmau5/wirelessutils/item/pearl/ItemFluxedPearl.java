package com.lordmau5.wirelessutils.item.pearl;

import com.lordmau5.wirelessutils.entity.EntityItemEnhanced;
import com.lordmau5.wirelessutils.entity.pearl.EntityFluxedPearl;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemFluxedPearl extends ItemBasePearl {
    public ItemFluxedPearl() {
        super();

        setName("fluxed_pearl");
    }

    @Override
    @Nonnull
    public EntityThrowable getProjectileEntity(@Nonnull World worldIn, @Nullable EntityPlayer playerIn, @Nullable IPosition position, @Nonnull ItemStack stack) {
        if ( playerIn != null )
            return new EntityFluxedPearl(worldIn, playerIn, stack);

        if ( position != null )
            return new EntityFluxedPearl(worldIn, position.getX(), position.getY(), position.getZ(), stack);

        return new EntityFluxedPearl(worldIn, stack);
    }

    @Override
    public boolean shouldItemTakeDamage(EntityItemEnhanced entity, @Nonnull ItemStack stack, DamageSource source, float amount) {
        if ( source == DamageSource.LIGHTNING_BOLT && ModConfig.items.fluxedPearl.enableLightning ) {
            String thrower = entity.getThrower();
            World world = entity.getEntityWorld();
            if ( world != null && thrower != null ) {
                EntityPlayer player = world.getPlayerEntityByName(thrower);
                if ( player instanceof EntityPlayerMP )
                    ModAdvancements.STORM_CHASER.trigger((EntityPlayerMP) player);
            }

            entity.setItem(new ItemStack(ModItems.itemChargedPearl, stack.getCount(), stack.getMetadata()));
            return false;
        }

        return super.shouldItemTakeDamage(entity, stack, source, amount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if ( worldIn == null || !ModConfig.items.fluxedPearl.enableLightning )
            return;

        int requiredWeather = ModConfig.items.fluxedPearl.requiredWeather;
        if ( requiredWeather == 0 )
            return;

        EntityPlayer player = Minecraft.getMinecraft().player;
        boolean working;
        if ( player == null )
            working = worldIn.isRaining();
        else {
            working = EntityFluxedPearl.rainingOrSnowingAt(worldIn, player.getPosition());
        }

        if ( requiredWeather == 2 )
            working &= worldIn.isThundering();

        if ( working )
            addLocalizedLines(tooltip, getTranslationKey() + ".weather", TextHelpers.GOLD);
    }
}
