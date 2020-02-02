package com.lordmau5.wirelessutils.tile;

import cofh.core.network.PacketBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.TimeTracker;
import cofh.core.util.helpers.MathHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.entity.base.EntityBaseThrowable;
import com.lordmau5.wirelessutils.entity.pearl.EntityEncapsulatedItem;
import com.lordmau5.wirelessutils.entity.pearl.EntityTracer;
import com.lordmau5.wirelessutils.gui.client.GuiSlimeCannon;
import com.lordmau5.wirelessutils.gui.container.ContainerSlimeCannon;
import com.lordmau5.wirelessutils.item.base.ItemBasePearl;
import com.lordmau5.wirelessutils.tile.base.IAreaVisibilityControllable;
import com.lordmau5.wirelessutils.tile.base.ITileInfoProvider;
import com.lordmau5.wirelessutils.tile.base.IUnlockableSlots;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.base.TileEntityBase;
import com.lordmau5.wirelessutils.utils.RenderUtils;
import com.lordmau5.wirelessutils.utils.constants.NiceColors;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Machine(name = "slime_cannon")
public class TileSlimeCannon extends TileEntityBase implements ITickable, IUnlockableSlots, ITileInfoProvider, IAreaVisibilityControllable {

    public static final int MAX_BARREL_SPEED = 360;
    public static final int MIN_BARREL_SPEED = 90;
    public static final int SPEED_BARREL_STEP = 18;
    public static final int SLOW_BARREL_STEP = 6;

    private boolean[] slotIsFull;
    private boolean[] slotIsEmpty;
    private int fullSlots = 0;
    private int emptySlots = 0;

    private byte cooldown = 0;
    private byte ticks = 10;

    private byte count = 0;
    private byte speed = 0;
    private float velocity = 1F;
    private boolean accurate = true;

    private float yaw = 0F;
    private float pitch = 0F;
    private boolean wrapPearls = true;

    protected final TimeTracker tracker = new TimeTracker();
    private boolean activeCooldown = false;

    private int defaultColor = -1;
    private boolean tracing = false;
    private EntityTracer tracer;

    // Rendering Stuff
    public int rotationSpeed = MIN_BARREL_SPEED;
    public float rotation = 0;

    public TileSlimeCannon() {
        super();
        initializeItemStackHandler(ModConfig.blocks.slimeCannon.slots);
    }

    /* ITileInfoProvider */

    @Override
    public void getInfoTooltip(@Nonnull List<String> tooltip, @Nullable NBTTagCompound tag) {
        float velocity = this.velocity;
        if ( tag != null && tag.hasKey("Velocity", Constants.NBT.TAG_FLOAT) )
            velocity = tag.getFloat("Velocity");

        boolean accurate = this.accurate;
        if ( tag != null && tag.hasKey("Accurate") )
            accurate = tag.getBoolean("Accurate");

        tooltip.add(new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + ".accurate_pearls",
                new TextComponentTranslation(
                        "btn." + WirelessUtils.MODID + ".mode." + (accurate ? 2 : 1)
                )
        ).getFormattedText());

        tooltip.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".velocity",
                new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".bpt",
                        TextHelpers.getComponent(String.format("%.2f", velocity))
                )
        ).getFormattedText());

        tooltip.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".yaw",
                new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".degrees",
                        TextHelpers.getComponent(Math.round(yaw))
                )
        ).getFormattedText());

        tooltip.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".pitch",
                new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".degrees",
                        TextHelpers.getComponent(Math.round(pitch))
                )
        ).getFormattedText());
    }

    @Nonnull
    @Override
    public NBTTagCompound getInfoNBT(@Nonnull NBTTagCompound tag, @Nullable EntityPlayerMP player) {
        tag.setFloat("Velocity", velocity);
        tag.setBoolean("Accurate", accurate);
        return tag;
    }

    /* IAreaVisibilityControllable */

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyTracer();
    }

    private void destroyTracer() {
        if ( tracer != null ) {
            tracer.setDead();
            tracer = null;
        }
    }

    private void createTracer() {
        destroyTracer();
        if ( !tracing || !world.isRemote )
            return;

        tracer = new EntityTracer(world, NiceColors.COLORS[getDefaultColor()]);
        if ( accurate )
            tracer.setDragless(true);

        tracer.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        tracer.shootAngle(-pitch, yaw, 0F, velocity, accurate ? 0F : 2F);
        world.spawnEntity(tracer);
    }

    @Override
    public boolean hasClientUpdate() {
        return true;
    }

    @Override
    public boolean usesDefaultColor() {
        return true;
    }

    @Override
    public boolean shouldRenderAreas() {
        return tracing;
    }

    @Override
    public void enableRenderAreas(boolean enabled) {
        tracing = enabled;
        if ( !enabled )
            destroyTracer();
    }

    @Override
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

        if ( tracer != null )
            tracer.setColor(NiceColors.COLORS[color]);
    }

    @Override
    public int getDefaultColor() {
        if ( defaultColor == -1 ) {
            if ( world != null )
                defaultColor = world.rand.nextInt(NiceColors.COLORS.length);
            else
                return 0;
        }

        return defaultColor;
    }

    /* --- */

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
    }

    @Override
    protected String getTileName() {
        return "tile." + WirelessUtils.MODID + ".slime_cannon.name";
    }

    public void setActive(boolean active) {
        isActive = active;
        if ( world == null || world.isRemote )
            return;

        if ( isActive == wasActive )
            return;

        if ( !activeCooldown ) {
            activeCooldown = true;
            tracker.markTime(world);

        } else if ( tracker.hasDelayPassed(world, 10) )
            activeCooldown = false;
        else
            return;

        wasActive = isActive;
        sendTilePacket(Side.CLIENT);
    }

    /* GUI */

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public Object getGuiClient(InventoryPlayer inventory) {
        return new GuiSlimeCannon(inventory, this);
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerSlimeCannon(inventory, this);
    }

    /* Inventory */

    @Override
    public boolean isSlotUnlocked(int slotIndex) {
        return true;
    }

    public void dropCountMod() {
        int j = getCount();
        if ( j == -1 )
            CoreUtils.dropItemStackIntoWorldWithVelocity(new ItemStack(Items.SLIME_BALL), world, pos);
        else if ( j > 0 )
            CoreUtils.dropItemStackIntoWorldWithVelocity(new ItemStack(Items.GLOWSTONE_DUST, j), world, pos);
    }

    public void dropSpeedMod() {
        int i = getSpeed();
        if ( i > 0 )
            CoreUtils.dropItemStackIntoWorldWithVelocity(new ItemStack(Items.REDSTONE, i), world, pos);
    }

    @Override
    public void dropContents() {
        super.dropContents();

        dropCountMod();
        dropSpeedMod();
    }

    @Override
    protected void initializeItemStackHandler(int size) {
        super.initializeItemStackHandler(size);
        slotIsFull = new boolean[size];
        slotIsEmpty = new boolean[size];
        Arrays.fill(slotIsFull, false);
        Arrays.fill(slotIsEmpty, false);
        fullSlots = 0;
        emptySlots = 0;
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        updateItemCache(slot);
    }

    private void updateItemCache(int slot) {
        ItemStack stack = itemStackHandler.getStackInSlot(slot);
        boolean full, empty;
        if ( stack.isEmpty() ) {
            full = false;
            empty = true;
        } else {
            empty = false;
            full = stack.getCount() >= itemStackHandler.getSlotLimit(slot);
        }

        if ( full != slotIsFull[slot] ) {
            slotIsFull[slot] = full;
            if ( full )
                fullSlots++;
            else
                fullSlots--;
        }

        if ( empty != slotIsEmpty[slot] ) {
            slotIsEmpty[slot] = empty;
            if ( empty )
                emptySlots++;
            else
                emptySlots--;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        cooldown = tag.getByte("Tick");
        count = tag.getByte("Count");
        speed = tag.getByte("Speed");
        accurate = !tag.hasKey("Accurate") || tag.getBoolean("Accurate");

        updateTicks();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        tag.setByte("Tick", cooldown);
        tag.setByte("Count", count);
        tag.setByte("Speed", speed);
        tag.setBoolean("Accurate", accurate);
        return tag;
    }

    @Override
    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        tag = super.writeExtraToNBT(tag);

        tag.setBoolean("Wrap", wrapPearls);
        tag.setFloat("Velocity", velocity);
        tag.setFloat("Pitch", pitch);
        tag.setFloat("Yaw", yaw);

        return tag;
    }

    @Override
    public void readExtraFromNBT(NBTTagCompound tag) {
        super.readExtraFromNBT(tag);

        wrapPearls = tag.getBoolean("Wrap");
        velocity = tag.getFloat("Velocity");
        pitch = tag.getFloat("Pitch");
        yaw = tag.getFloat("Yaw");
    }

    @Override
    public void readInventoryFromNBT(NBTTagCompound tag) {
        super.readInventoryFromNBT(tag);
        int slots = itemStackHandler.getSlots();
        for (int i = 0; i < slots; i++)
            updateItemCache(i);
    }

    @Override
    public int calculateComparatorInput() {
        if ( itemStackHandler == null )
            return 0;

        int slots = itemStackHandler.getSlots();
        if ( fullSlots == slots )
            return 15;
        else if ( emptySlots == slots )
            return 0;

        int total = 0;
        int capacity = 0;

        for (int i = 0; i < slots; i++) {
            ItemStack stack = itemStackHandler.getStackInSlot(i);

            int slot_max = itemStackHandler.getSlotLimit(i);
            capacity += Math.min(slot_max, stack.getMaxStackSize());

            if ( !stack.isEmpty() )
                total += stack.getCount();
        }

        if ( total == 0 )
            return 0;

        return 1 + MathHelper.round(total * 14 / (double) capacity);
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    public boolean setCount(int count) {
        if ( count < -1 )
            count = -1;
        else if ( count > 4 )
            count = 4;

        if ( this.count == count )
            return false;

        this.count = (byte) count;
        callBlockUpdate();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        return true;
    }

    public int getCount() {
        return count;
    }


    public int getShotCount() {
        if ( count == -1 )
            return 1;
        else if ( count == 0 )
            return 8;
        else
            return 16 * count;
    }

    public boolean incrementSpeed() {
        return setSpeed(getSpeed() + 1);
    }

    public boolean decrementSpeed() {
        return setSpeed(getSpeed() - 1);
    }

    public int getSpeed() {
        return speed;
    }

    public boolean setSpeed(int speed) {
        if ( speed < 0 )
            speed = 0;
        else if ( speed >= ModConfig.blocks.slimeCannon.tickRates.length )
            speed = ModConfig.blocks.slimeCannon.tickRates.length - 1;

        if ( speed == this.speed )
            return false;

        this.speed = (byte) speed;
        updateTicks();
        callBlockUpdate();

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        return true;
    }

    private void updateTicks() {
        int[] rates = ModConfig.blocks.slimeCannon.tickRates;
        if ( speed >= rates.length )
            ticks = (byte) rates[rates.length - 1];
        else
            ticks = (byte) rates[speed];
    }

    public boolean isAccurate() {
        return accurate;
    }

    public void setAccurate(boolean accurate) {
        if ( this.accurate == accurate )
            return;

        this.accurate = accurate;
        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        destroyTracer();
    }

    public boolean isWrappingPearls() {
        return wrapPearls;
    }

    public void setWrapPearls(boolean wrap) {
        if ( wrap == wrapPearls )
            return;

        wrapPearls = wrap;
        if ( !world.isRemote )
            markChunkDirty();
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float value) {
        if ( value < 0.15F )
            value = 0.15F;
        else if ( value > ModConfig.blocks.slimeCannon.maxVelocity )
            value = (float) ModConfig.blocks.slimeCannon.maxVelocity;

        if ( velocity == value )
            return;

        velocity = value;
        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        destroyTracer();
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setPitch(float pitch) {
        if ( pitch < -20F )
            pitch = -20F;
        else if ( pitch > 90 )
            pitch = 90F;

        if ( this.pitch == pitch )
            return;

        this.pitch = pitch;

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        destroyTracer();
    }

    public void setYaw(float yaw) {
        if ( yaw < 0 )
            yaw = (yaw % 360F) + 360F;
        else if ( yaw >= 360F )
            yaw = yaw % 360F;
        if ( yaw < 0 || yaw >= 360 )
            yaw = 0;

        if ( this.yaw == yaw )
            return;

        this.yaw = yaw;

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        destroyTracer();
    }

    public void setAngles(float pitch, float yaw) {
        if ( yaw < 0 )
            yaw = (yaw % 360F) + 360F;
        else if ( yaw >= 360F )
            yaw = yaw % 360F;
        if ( yaw < 0 || yaw >= 360 )
            yaw = 0;

        if ( pitch < -20F )
            pitch = -20F;
        else if ( pitch > 90F )
            pitch = 90F;

        if ( this.pitch == pitch && this.yaw == yaw )
            return;

        this.pitch = pitch;
        this.yaw = yaw;

        if ( !world.isRemote ) {
            markChunkDirty();
            sendTilePacket(Side.CLIENT);
        }

        destroyTracer();
    }


    @Override
    public PacketBase getTilePacket() {
        PacketBase payload = super.getTilePacket();

        payload.addByte(count);
        payload.addByte(speed);
        payload.addBool(accurate);
        payload.addFloat(pitch);
        payload.addFloat(yaw);

        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleTilePacket(PacketBase payload) {
        super.handleTilePacket(payload);

        count = payload.getByte();
        speed = payload.getByte();
        setAccurate(payload.getBool());
        setAngles(payload.getFloat(), payload.getFloat());
        updateTicks();
    }

    @Override
    public PacketBase getModePacket() {
        PacketBase payload = super.getModePacket();

        payload.addBool(accurate);
        payload.addBool(wrapPearls);
        payload.addFloat(velocity);
        payload.addFloat(pitch);
        payload.addFloat(yaw);

        return payload;
    }

    @Override
    protected void handleModePacket(PacketBase payload) {
        super.handleModePacket(payload);

        setAccurate(payload.getBool());
        setWrapPearls(payload.getBool());
        setVelocity(payload.getFloat());
        setAngles(payload.getFloat(), payload.getFloat());
    }

    @Override
    public PacketBase getGuiPacket() {
        PacketBase payload = super.getGuiPacket();
        payload.addBool(wrapPearls);
        payload.addFloat(velocity);
        return payload;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleGuiPacket(PacketBase payload) {
        super.handleGuiPacket(payload);
        setWrapPearls(payload.getBool());
        setVelocity(payload.getFloat());
    }

    public float getCountRotationFactor() {
        switch (count) {
            case -1:
                return 0.5F;
            case 1:
                return 1.1F;
            case 2:
                return 1.2F;
            case 3:
                return 1.3F;
            case 4:
                return 1.4F;
            default:
                return 1F;
        }
    }

    public float getSpeedRotationFactor() {
        switch (speed) {
            case 1:
                return 1.25F;
            case 2:
                return 1.5F;
            case 3:
                return 1.75F;
            case 4:
                return 2F;
            default:
                return 1F;
        }
    }

    public void updateAnimation() {
        final float factor = getSpeedRotationFactor() * getCountRotationFactor();
        final int min = Math.round(MIN_BARREL_SPEED * factor);
        final int max = Math.round(MAX_BARREL_SPEED * factor);

        if ( isActive && rotationSpeed < max ) {
            // If we're active, speed up till we hit max speed.
            rotationSpeed = Math.min(max, rotationSpeed + SPEED_BARREL_STEP);

        } else if ( isActive && rotationSpeed > max ) {
            // If we changed our speed modifier, we might be going too fast.
            // Slow it down a bit.
            rotationSpeed = Math.max(max, rotationSpeed - SLOW_BARREL_STEP);

        } else if ( !isActive && rotationSpeed > min )
            // If we're inactive, slow down until we hit min speed.
            rotationSpeed = Math.max(min, rotationSpeed - SLOW_BARREL_STEP);

        if ( isActive || rotationSpeed > min ) {
            // If the machine is active or the barrel is spinning down, add rotation.
            rotation += (float) rotationSpeed / 20F;
            if ( rotation >= 360F )
                rotation -= 360F;

        } else if ( rotation % 90 != 0 ) {
            // If we have stopped but we aren't at a 90 degree angle, keep adding rotation but don't go past
            // the next 90.
            final float target = (float) Math.ceil(rotation / 90) * 90F;
            rotation += (float) rotationSpeed / 20F;
            if ( rotation > target ) {
                rotation = target;
                rotationSpeed = 0;
            }
        } else
            rotationSpeed = 0;
    }

    public void update() {
        if ( world.isRemote ) {
            updateAnimation();

            if ( tracing && (tracer == null || tracer.isDead) )
                createTracer();

            return;
        }

        if ( cooldown < ticks )
            cooldown++;

        if ( !redstoneControlOrDisable() || velocity == 0 ) {
            setActive(false);
            updateTrackers();
            return;
        }

        if ( cooldown < ticks ) {
            // We specifically don't want to set inactive because of throttling
            // to avoid a weird rendering effect.
            updateTrackers();
            return;
        }

        cooldown = (byte) 0;

        int slots = itemStackHandler.getSlots();
        if ( emptySlots == slots ) {
            setActive(false);
            updateTrackers();
            return;
        }

        boolean unwrapped = ModConfig.blocks.slimeCannon.allowUnwrapped && !wrapPearls;
        boolean active = false;
        int count = getShotCount();
        for (int i = 0; i < slots; i++) {
            if ( slotIsEmpty[i] )
                continue;

            ItemStack stack = itemStackHandler.getStackInSlot(i);
            if ( stack.isEmpty() )
                continue;

            boolean pearlMode = false;
            int c = count;
            if ( unwrapped && stack.getItem() instanceof ItemBasePearl ) {
                pearlMode = true;
                c = 1;
            }

            boolean reduce = stack.getCount() > c;
            if ( reduce ) {
                stack = stack.copy();
                stack.setCount(c);
            }

            EntityBaseThrowable pearl;

            float velocity = this.velocity;
            float inaccuracy = accurate ? 0F : 2F;

            if ( pearlMode ) {
                ItemBasePearl item = (ItemBasePearl) stack.getItem();
                if ( item == null )
                    continue;

                EntityThrowable entity = item.getProjectileEntity(world, null, null, stack);
                if ( entity instanceof EntityBaseThrowable )
                    pearl = (EntityBaseThrowable) entity;
                else
                    continue;

                inaccuracy = item.getProjectileInaccuracy(stack);
                velocity = Math.min(velocity, item.getProjectileVelocity(stack));

            } else {
                ItemStack encapsulated = new ItemStack(ModItems.itemEncapsulatedItem);
                NBTTagCompound tag = new NBTTagCompound();
                tag.setTag("Held", stack.serializeNBT());
                encapsulated.setTagCompound(tag);

                pearl = new EntityEncapsulatedItem(world, encapsulated);
            }

            if ( inaccuracy == 0 )
                pearl.setDragless(true);

            pearl.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            pearl.shootAngle(-pitch, yaw, 0, velocity, inaccuracy);
            world.spawnEntity(pearl);

            if ( world instanceof WorldServer ) {
                WorldServer ws = (WorldServer) world;
                //ws.spawnParticle(EnumParticleTypes.SLIME, pearl.posX, pearl.posY, pearl.posZ, 2, 0, 0, 0);
                ws.playSound(null, pearl.posX, pearl.posY, pearl.posZ, SoundEvents.BLOCK_SLIME_FALL, SoundCategory.BLOCKS, 0.2F, 1F);
            }

            if ( reduce )
                itemStackHandler.extractItem(i, c, false);
            else
                itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);

            active = true;
            break;
        }

        setActive(active);
        updateTrackers();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if ( facing != EnumFacing.DOWN && ModConfig.blocks.slimeCannon.onlyBottom )
            return false;

        if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
            return true;

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if ( facing != EnumFacing.DOWN && ModConfig.blocks.slimeCannon.onlyBottom )
            return null;

        if ( capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);

        return super.getCapability(capability, facing);
    }


    // Trajectory Calculation

    @Nullable
    public Vec2f calculateTrajectory(@Nonnull BlockPos target, @Nullable EnumFacing side) {
        return calculateTrajectory(target, side, getVelocity());
    }

    @Nullable
    public Vec2f calculateTrajectory(@Nonnull BlockPos target, @Nullable EnumFacing side, boolean lower) {
        return calculateTrajectory(target, side, getVelocity(), lower);
    }

    @Nullable
    public Vec2f calculateTrajectory(@Nonnull BlockPos target, @Nullable EnumFacing side, float velocity) {
        return calculateTrajectory(target, side, velocity, true);
    }

    @Nullable
    public Vec2f calculateTrajectory(@Nonnull BlockPos target, @Nullable EnumFacing side, float velocity, boolean lower) {
        Vec3d dest = new Vec3d(target);
        if ( side == null )
            dest = dest.add(0.5D, 0.5D, 0.5D);
        else {
            final boolean positive = side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE;
            switch (side.getAxis()) {
                case X:
                    dest = dest.add(positive ? 1 : 0, 0.25D, 0.5D);
                    break;
                case Y:
                    dest = dest.add(0.5D, positive ? 0.75 : 0, 0.5D);
                    break;
                default:
                    dest = dest.add(0.5D, 0.25D, positive ? 1 : 0);
            }
        }

        return calculateTrajectory(dest, velocity, lower);
    }

    @Nullable
    public Vec2f calculateTrajectory(@Nonnull Vec3d target) {
        return calculateTrajectory(target, getVelocity());
    }

    @Nullable
    public Vec2f calculateTrajectory(@Nonnull Vec3d target, float velocity) {
        return calculateTrajectory(target, velocity, true);
    }

    @Nullable
    public Vec2f calculateTrajectory(@Nonnull Vec3d target, float velocity, boolean lower) {
        final Vec3d origin = new Vec3d(pos).add(0.5, 0.5, 0.5);

        float yaw = (float) Math.toDegrees(-RenderUtils.getYaw(origin, target));
        if ( yaw < 0 )
            yaw += 360F;

        final float distance = (float) new Vec3d(origin.x - target.x, 0, origin.z - target.z).length();
        final float altitude = (float) (target.y - origin.y);
        final float gravity = 0.03F;

        final float angle = (float) Math.atan(
                (Math.pow(velocity, 2) + (lower ? -1 : 1) *
                        Math.sqrt(Math.pow(velocity, 4) - gravity * (gravity * Math.pow(distance, 2) + 2 * altitude * Math.pow(velocity, 2))))
                        / (gravity * distance)
        );

        if ( Float.isNaN(angle) )
            return null;

        return new Vec2f(yaw, (float) Math.toDegrees(angle));
    }

}
