package com.lordmau5.wirelessutils.plugins.IndustrialForegoing;

import com.lordmau5.wirelessutils.plugins.IPlugin;
import com.lordmau5.wirelessutils.plugins.IndustrialForegoing.items.ItemAnimalSlaughterModule;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.utils.EntityUtilities;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IFPlugin implements IPlugin {

    @GameRegistry.ObjectHolder("wirelessutils:animal_slaughter_module")
    public static ItemAnimalSlaughterModule itemAnimalSlaughterModule;

    @GameRegistry.ObjectHolder("industrialforegoing:mob_imprisonment_tool")
    public static Item itemMobImprisonmentTool;

    @Override
    public void registerItems(RegistryEvent.Register<Item> event) {
        if ( ModConfig.plugins.industrialForegoing.animalSlaughterModule.enabled )
            CommonProxy.registerItem(event, new ItemAnimalSlaughterModule());
    }

    @Override
    public void registerModels(ModelRegistryEvent event) {
        if ( ModConfig.plugins.industrialForegoing.animalSlaughterModule.enabled )
            itemAnimalSlaughterModule.initModel();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        if ( ModConfig.vaporizers.compatibility.useMITs && itemMobImprisonmentTool != null ) {
            EntityUtilities.registerHandler(itemMobImprisonmentTool, new EntityUtilities.IEntityBall() {
                @Nullable
                public Entity getEntity(@Nonnull ItemStack stack, @Nonnull World world, boolean withData, @Nullable EntityPlayer player) {
                    if ( !isFilledBall(stack) )
                        return null;

                    NBTTagCompound tag = stack.getTagCompound();
                    Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(tag.getString("entity")), world);
                    if ( entity != null && withData )
                        entity.readFromNBT(tag);

                    return entity;
                }

                @Nullable
                public Class<? extends Entity> getEntityClass(@Nonnull ItemStack stack) {
                    if ( !isFilledBall(stack) )
                        return null;

                    NBTTagCompound tag = stack.getTagCompound();
                    return EntityList.getClass(new ResourceLocation(tag.getString("entity")));
                }

                @Nullable
                public ResourceLocation getEntityId(@Nonnull ItemStack stack) {
                    if ( !isFilledBall(stack) )
                        return null;

                    NBTTagCompound tag = stack.getTagCompound();
                    return new ResourceLocation(tag.getString("entity"));
                }

                @Nonnull
                public ItemStack removeEntity(@Nonnull ItemStack stack) {
                    if ( !isValidBall(stack) )
                        return ItemStack.EMPTY;

                    ItemStack out = stack.copy();
                    out.setTagCompound(null);
                    return out;
                }

                @Nonnull
                public ItemStack saveEntity(@Nonnull ItemStack stack, @Nonnull Entity entity, @Nullable EntityPlayer player) {
                    if ( stack.isEmpty() || stack.getItem() != itemMobImprisonmentTool || isFilledBall(stack) )
                        return ItemStack.EMPTY;

                    if ( !(entity instanceof EntityLiving) || !entity.isEntityAlive() || !entity.isNonBoss() )
                        return ItemStack.EMPTY;

                    ResourceLocation key = EntityList.getKey(entity);
                    if ( key == null || EntityUtilities.isBlacklisted(key) )
                        return ItemStack.EMPTY;

                    ItemStack out = stack.copy();
                    if ( out.getCount() > 1 )
                        out.setCount(1);

                    NBTTagCompound tag = new NBTTagCompound();

                    // This is hot garbage, just saying.
                    entity.writeToNBT(tag);
                    tag.setString("entity", key.toString());

                    out.setTagCompound(tag);
                    return out;
                }

                public boolean isValidBall(@Nonnull ItemStack stack) {
                    return stack.getItem() == itemMobImprisonmentTool;
                }

                public boolean isFilledBall(@Nonnull ItemStack stack) {
                    if ( !isValidBall(stack) )
                        return false;

                    NBTTagCompound tag = stack.getTagCompound();
                    return tag != null && tag.hasKey("entity");
                }

                public boolean isBabyEntity(@Nonnull ItemStack stack) {
                    if ( !isFilledBall(stack) )
                        return false;

                    NBTTagCompound tag = stack.getTagCompound();
                    return tag != null && (tag.getBoolean("IsBaby") || tag.getInteger("Age") < 0);
                }

                public boolean canFillBall(@Nonnull ItemStack stack) {
                    return isValidBall(stack) && !isFilledBall(stack);
                }

                @Override
                public boolean canEmptyBall(@Nonnull ItemStack stack) {
                    return isFilledBall(stack);
                }
            });
        }
    }
}
