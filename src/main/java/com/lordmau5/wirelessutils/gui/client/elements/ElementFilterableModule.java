package com.lordmau5.wirelessutils.gui.client.elements;

import cofh.core.gui.element.ElementTextField;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemFilteringModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import net.minecraft.item.ItemStack;

public abstract class ElementFilterableModule extends ElementModuleBase {

    private final ElementDynamicContainedButton btnSneakMode;
    private final ElementDynamicContainedButton btnChildMode;
    private final ElementDynamicContainedButton btnNamedMode;
    private final ElementDynamicContainedButton btnListMode;

    private final ElementTextField txtList;

    private final ItemFilteringModule.FilteredBehavior behavior;

    private String[] cachedBlacklist = null;

    public ElementFilterableModule(GuiBaseVaporizer gui, ItemFilteringModule.FilteredBehavior behavior) {
        super(gui);
        this.behavior = behavior;

        btnSneakMode = new ElementDynamicContainedButton(this, "SneakMode", 8, 50, 78, 16, "");
        btnChildMode = new ElementDynamicContainedButton(this, "ChildMode", 91, 50, 78, 16, "");

        btnListMode = new ElementDynamicContainedButton(this, "ListMode", 8, 71, 61, 16, "");
        btnNamedMode = new ElementDynamicContainedButton(this, "NamedMode", 74, 71, 95, 16, "");

        txtList = new ElementTextField(gui, 9, 93, 158, 51) {
            @Override
            public ElementTextField setFocused(boolean focused) {
                if ( isFocused() && !focused ) {
                    TileBaseVaporizer vaporizer = behavior.vaporizer;
                    ItemStack stack = vaporizer.getModule();
                    ItemFilteringModule item = (ItemFilteringModule) stack.getItem();

                    if ( !item.setBlacklist(stack, getText()).isEmpty() ) {
                        vaporizer.setModule(stack);
                        vaporizer.sendModePacket();
                    }
                }

                return super.setFocused(focused);
            }
        };

        txtList.setMultiline(true);

        addElement(btnSneakMode);
        addElement(btnChildMode);

        addElement(btnListMode);
        addElement(btnNamedMode);

        addElement(txtList);
    }

    abstract int getContentHeight();

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        int yPos = getContentHeight();

        btnSneakMode.setPosition(btnSneakMode.getPosX(), yPos);
        btnChildMode.setPosition(btnChildMode.getPosX(), yPos);

        yPos += 21;

        btnListMode.setPosition(btnListMode.getPosX(), yPos);
        btnNamedMode.setPosition(btnNamedMode.getPosX(), yPos);

        yPos += 22;

        txtList.setPosition(txtList.getPosX(), yPos);
        txtList.setSize(txtList.getWidth(), sizeY - (yPos + 20));

        if ( !txtList.isFocused() ) {
            String[] blacklist = behavior.getBlacklist();
            if ( blacklist != cachedBlacklist ) {
                cachedBlacklist = blacklist;
                if ( blacklist == null || blacklist.length == 0 )
                    txtList.setText("");
                else
                    txtList.setText(String.join("\n", blacklist));
            }
        }

        btnListMode.setText(StringHelper.localize("btn." + WirelessUtils.MODID + "." +
                (behavior.isWhitelist() ? "whitelist" : "blacklist")));

        btnSneakMode.setText(StringHelper.localize("btn." + WirelessUtils.MODID + ".sneak_mode." + behavior.getSneakMode()));
        btnChildMode.setText(StringHelper.localize("btn." + WirelessUtils.MODID + ".child_mode." + behavior.getChildMode()));
        btnNamedMode.setText(StringHelper.localize("btn." + WirelessUtils.MODID + ".named_mode." + behavior.getNamedMode()));
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        float pitch = mouseButton == 1 ? 1F : 0.7F;
        int amount = mouseButton == 1 ? -1 : 1;

        TileBaseVaporizer vaporizer = behavior.vaporizer;
        ItemStack stack = vaporizer.getModule();
        ItemFilteringModule item = (ItemFilteringModule) stack.getItem();

        switch (buttonName) {
            case "ListMode":
                if ( item.setWhitelist(stack, !behavior.isWhitelist()).isEmpty() )
                    return;

                break;

            case "SneakMode":
                if ( item.setSneakingMode(stack, item.getSneakingMode(stack) + amount).isEmpty() )
                    return;

                break;

            case "ChildMode":
                if ( item.setChildMode(stack, item.getChildMode(stack) + amount).isEmpty() )
                    return;

                break;

            case "NamedMode":
                if ( item.setNamedMode(stack, item.getNamedMode(stack) + amount).isEmpty() )
                    return;

                break;

            default:
                return;
        }

        BaseGuiContainer.playClickSound(pitch);
        vaporizer.setModule(stack);
        vaporizer.sendModePacket();
    }
}
