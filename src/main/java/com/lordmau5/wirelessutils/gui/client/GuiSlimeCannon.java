package com.lordmau5.wirelessutils.gui.client;

import cofh.core.gui.element.ElementSlider;
import cofh.core.gui.element.listbox.SliderHorizontal;
import cofh.core.gui.element.tab.TabBase;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementAreaButton;
import com.lordmau5.wirelessutils.gui.client.elements.TabContainedButton;
import com.lordmau5.wirelessutils.gui.container.ContainerSlimeCannon;
import com.lordmau5.wirelessutils.tile.TileSlimeCannon;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import com.lordmau5.wirelessutils.utils.mod.ModConfig;
import com.lordmau5.wirelessutils.utils.mod.ModItems;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class GuiSlimeCannon extends BaseGuiContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    private final ItemStack WRAPPED;
    private final ItemStack UNWRAPPED;
    private final ItemStack UNSTABLE;
    private final ItemStack STABLE;

    private final TileSlimeCannon tile;

    private TabContainedButton btnWrapped;
    private TabContainedButton btnAccurate;

    private ElementSlider ctlVelocity;
    private ElementSlider ctlYaw;
    private ElementSlider ctlPitch;
    private boolean updating = false;

    private int yaw;
    private int pitch;

    public GuiSlimeCannon(InventoryPlayer player, TileSlimeCannon tile) {
        super(new ContainerSlimeCannon(player, tile), tile, TEXTURE);
        this.tile = tile;

        WRAPPED = new ItemStack(ModItems.itemEncapsulatedItem);
        UNWRAPPED = new ItemStack(ModItems.itemFluxedPearl);
        UNSTABLE = new ItemStack(Items.ENDER_PEARL);
        STABLE = new ItemStack(ModItems.itemStabilizedEnderPearl);

        generateInfo("tab." + WirelessUtils.MODID + ".slime_cannon");
    }

    @Override
    public void initGui() {
        super.initGui();
        updating = true;

        if ( ModConfig.blocks.slimeCannon.allowUnwrapped ) {
            btnWrapped = new TabContainedButton(this, "Wrap", TabBase.LEFT, WRAPPED);
            btnWrapped.setTooltipExtra("btn." + WirelessUtils.MODID + ".wrap_pearls.info", TextHelpers.GRAY);
            btnWrapped.setToolTipLocalized(true);
            btnWrapped.setBackgroundColor(0x089e4c);

            addTab(btnWrapped);
        }

        btnAccurate = new TabContainedButton(this, "Accurate", TabBase.LEFT, STABLE);
        btnAccurate.setTooltipExtra("btn." + WirelessUtils.MODID + ".accurate_pearls.info", TextHelpers.GRAY);
        btnAccurate.setToolTipLocalized(true);
        btnAccurate.setBackgroundColor(0x0a76d0);
        addTab(btnAccurate);

        addTab(new TabInfo(this, myInfo));

        addTab(new TabRedstoneControl(this, tile));
        int maxVelocity = (int) Math.round(ModConfig.blocks.slimeCannon.maxVelocity * 100);

        ctlVelocity = new SliderHorizontal(this, xSize - 119, 39, 110, 10, maxVelocity) {
            @Override
            public void onValueChanged(int value) {
                super.onValueChanged(value);
                if ( updating )
                    return;

                float speed = (float) value / 100F;
                if ( tile.getVelocity() != speed ) {
                    tile.setVelocity(speed);
                    tile.sendModePacket();
                }
            }

            @Override
            public void addTooltip(List<String> list) {
                super.addTooltip(list);
                list.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".bpt",
                        TextHelpers.getComponent(String.format("%.2f", (float) getValue() / 100F))
                ).getFormattedText());
            }
        };

        ctlVelocity.setLimits(15, maxVelocity);

        ctlYaw = new SliderHorizontal(this, xSize - 119, 54, 110, 10, 359) {
            @Override
            public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
                setValue(getValue() + getSnapModifier() * (movement > 0 ? -1 : 1));
                return true;
            }

            @Override
            public void onValueChanged(int value) {
                super.onValueChanged(value);
                if ( updating )
                    return;

                value = snapValue(value);
                if ( value != yaw ) {
                    yaw = value;
                    updateAngle();
                }
            }

            @Override
            public void addTooltip(List<String> list) {
                super.addTooltip(list);
                list.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".degrees",
                        TextHelpers.getComponent(getValue())
                ).getFormattedText());

                addSnappingTips(list);
            }
        };

        ctlYaw.setLimits(0, 359);

        ctlPitch = new SliderHorizontal(this, xSize - 119, 69, 110, 10, 90) {
            @Override
            public boolean onMouseWheel(int mouseX, int mouseY, int movement) {
                setValue(getValue() + getSnapModifier() * (movement > 0 ? -1 : 1));
                return true;
            }

            @Override
            public void onValueChanged(int value) {
                super.onValueChanged(value);
                if ( updating )
                    return;

                value = snapValue(value);
                if ( value != pitch ) {
                    pitch = value;
                    updateAngle();
                }
            }

            @Override
            public void addTooltip(List<String> list) {
                super.addTooltip(list);
                list.add(new TextComponentTranslation(
                        "info." + WirelessUtils.MODID + ".degrees",
                        TextHelpers.getComponent(getValue())
                ).getFormattedText());

                addSnappingTips(list);
            }
        };

        ctlPitch.setLimits(-20, 90);

        yaw = Math.round(tile.getYaw());
        pitch = Math.round(tile.getPitch());

        ctlVelocity.setValue(Math.round(tile.getVelocity() * 100));
        ctlYaw.setValue(yaw);
        ctlPitch.setValue(pitch);

        addElement(ctlVelocity);
        addElement(ctlYaw);
        addElement(ctlPitch);

        addElement(new ElementAreaButton(this, tile, 152, 8));
        updating = false;
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        switch (buttonName) {
            case "Wrap":
                tile.setWrapPearls(!tile.isWrappingPearls());
                tile.sendModePacket();
                break;
            case "Accurate":
                tile.setAccurate(!tile.isAccurate());
                tile.sendModePacket();
                break;
            default:
                super.handleElementButtonClick(buttonName, mouseButton);
                return;
        }

        BaseGuiContainer.playClickSound(mouseButton == 1 ? 0.7F : 1F);
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        updating = true;

        if ( ModConfig.blocks.slimeCannon.allowUnwrapped ) {
            boolean wrapped = tile.isWrappingPearls();
            btnWrapped.setItem(wrapped ? WRAPPED : UNWRAPPED);
            btnWrapped.setToolTip(new TextComponentTranslation(
                    "btn." + WirelessUtils.MODID + ".wrap_pearls",
                    new TextComponentTranslation(
                            "btn." + WirelessUtils.MODID + ".mode." + (wrapped ? 2 : 1)
                    ).setStyle(TextHelpers.YELLOW)
            ).setStyle(TextHelpers.WHITE).getFormattedText());
        }

        boolean accurate = tile.isAccurate();
        btnAccurate.setItem(accurate ? STABLE : UNSTABLE);
        btnAccurate.setToolTip(new TextComponentTranslation(
                "btn." + WirelessUtils.MODID + ".accurate_pearls",
                new TextComponentTranslation(
                        "btn." + WirelessUtils.MODID + ".mode." + (accurate ? 2 : 1)
                ).setStyle(TextHelpers.YELLOW)
        ).setStyle(TextHelpers.WHITE).getFormattedText());

        ctlVelocity.setValue(Math.round(tile.getVelocity() * 100));
        ctlYaw.setValue(MathHelper.clamp(Math.round(tile.getYaw()), 0, 359));
        ctlPitch.setValue(MathHelper.clamp(Math.round(tile.getPitch()), -20, 90));

        updating = false;
    }

    protected void updateAngle() {
        tile.setAngles(pitch, yaw);
        tile.sendModePacket();
    }


    protected void drawSlots() {
        int slots = tile.getInvSlotCount();
        int width = slots * 18;
        int xPos = Math.floorDiv(xSize - width, 2) - 1;

        bindTexture(TEXTURE);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawSizedTexturedModalRect(guiLeft + xPos, guiTop + 17, 7, 93, width, 18, 256, 256);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        if ( isMainPage() ) {
            drawRightAlignedText(StringHelper.localizeFormat("info." + WirelessUtils.MODID + ".velocity", ""), xSize - 121, 41, textColor);
            drawRightAlignedText(StringHelper.localizeFormat("info." + WirelessUtils.MODID + ".yaw", ""), xSize - 121, 56, textColor);
            drawRightAlignedText(StringHelper.localizeFormat("info." + WirelessUtils.MODID + ".pitch", ""), xSize - 121, 71, textColor);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);

        if ( isMainPage() )
            drawSlots();
    }

    public static int getSnapModifier() {
        if ( StringHelper.isControlKeyDown() )
            return 90;
        else if ( StringHelper.isShiftKeyDown() )
            return 45;
        else if ( StringHelper.isAltKeyDown() )
            return 30;

        return 1;
    }

    public static int snapValue(int value) {
        int mod = getSnapModifier();
        if ( mod == 1 )
            return value;

        return Math.round(value / (float) mod) * mod;
    }

    public static void addSnappingTips(List<String> list) {
        list.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".angle_snap",
                TextHelpers.getComponent("Ctrl").setStyle(TextHelpers.ITALIC),
                TextHelpers.getComponent(90).setStyle(TextHelpers.WHITE)
        ).setStyle(TextHelpers.GRAY).getFormattedText());
        list.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".angle_snap",
                TextHelpers.getComponent("Shift").setStyle(TextHelpers.ITALIC),
                TextHelpers.getComponent(45).setStyle(TextHelpers.WHITE)
        ).setStyle(TextHelpers.GRAY).getFormattedText());
        list.add(new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".angle_snap",
                TextHelpers.getComponent("Alt").setStyle(TextHelpers.ITALIC),
                TextHelpers.getComponent(30).setStyle(TextHelpers.WHITE)
        ).setStyle(TextHelpers.GRAY).getFormattedText());
    }
}
