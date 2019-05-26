package com.lordmau5.wirelessutils.gui.client.modules.base;

import cofh.core.gui.element.ElementTextField;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiContainer;
import com.lordmau5.wirelessutils.gui.client.elements.ElementDynamicContainedButton;
import com.lordmau5.wirelessutils.gui.client.vaporizer.GuiBaseVaporizer;
import com.lordmau5.wirelessutils.item.module.ItemFilteringModule;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public abstract class ElementFilterableModule extends ElementModuleBase {

    private final ElementDynamicContainedButton btnSneakMode;
    private final ElementDynamicContainedButton btnChildMode;
    private final ElementDynamicContainedButton btnPlayerMode;
    private final ElementDynamicContainedButton btnNamedMode;
    private final ElementDynamicContainedButton btnListMode;

    private final ElementTextField txtList;

    private final ItemFilteringModule.FilteredBehavior behavior;

    private String[] cachedBlacklist = null;

    public ElementFilterableModule(GuiBaseVaporizer gui, ItemFilteringModule.FilteredBehavior behavior) {
        super(gui);
        this.behavior = behavior;

        String name = "item." + WirelessUtils.MODID + ".filtered_module";

        btnSneakMode = new ElementDynamicContainedButton(this, "SneakMode", 8, 50, 78, 16, "");
        btnSneakMode.setToolTipLines(name + ".sneak_info");

        btnChildMode = new ElementDynamicContainedButton(this, "ChildMode", 91, 50, 78, 16, "");
        btnChildMode.setToolTipLines(name + ".age_info");

        btnPlayerMode = new ElementDynamicContainedButton(this, "PlayerMode", 8, 71, 78, 16, "");
        btnPlayerMode.setToolTipLines(name + ".player_info");

        btnNamedMode = new ElementDynamicContainedButton(this, "NamedMode", 91, 71, 78, 16, "");
        btnNamedMode.setToolTipLines(name + ".named_info");

        btnListMode = new ElementDynamicContainedButton(this, "ListMode", 8, 92, 78, 16, "");
        btnListMode.setToolTipLines(name + ".list_info");

        txtList = new ElementTextField(gui, 9, 114, 158, 51) {
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

        btnPlayerMode.setEnabled(behavior.allowPlayers());
        btnPlayerMode.setVisible(behavior.allowPlayers());

        txtList.setMultiline(true);

        addElement(btnSneakMode);
        addElement(btnChildMode);

        addElement(btnPlayerMode);
        addElement(btnNamedMode);

        addElement(btnListMode);
        addElement(txtList);
    }

    public abstract int getContentHeight();

    @Override
    public void updateElementInformation() {
        super.updateElementInformation();

        int yPos = getContentHeight();

        btnSneakMode.setPosition(btnSneakMode.getPosX(), yPos);
        btnChildMode.setPosition(btnChildMode.getPosX(), yPos);

        yPos += 21;

        btnPlayerMode.setPosition(btnPlayerMode.getPosX(), yPos);
        btnNamedMode.setPosition(btnNamedMode.getPosX(), yPos);

        yPos += 21;

        btnListMode.setPosition(btnListMode.getPosX(), yPos);

        yPos += 22;

        txtList.setPosition(txtList.getPosX(), yPos);
        txtList.setSize(txtList.getWidth(), sizeY - (yPos + 10));

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

        btnPlayerMode.setText(getButtonLabel("player", behavior.getPlayerMode()));
        btnSneakMode.setText(getButtonLabel("sneak", behavior.getSneakMode()));
        btnChildMode.setText(getButtonLabel("age", behavior.getChildMode()));
        btnNamedMode.setText(getButtonLabel("named", behavior.getNamedMode()));
    }

    public static String getButtonLabel(String key, int mode) {
        return new TextComponentTranslation(
                "item." + WirelessUtils.MODID + ".filtered_module." + key,
                StringHelper.localize("btn." + WirelessUtils.MODID + "." + (key.equals("age") ? "age_mode." : "mode.") + mode)
        ).getFormattedText();
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
                if ( item.setSneakMode(stack, item.getSneakMode(stack) + amount).isEmpty() )
                    return;

                break;

            case "ChildMode":
                if ( item.setChildMode(stack, item.getChildMode(stack) + amount).isEmpty() )
                    return;

                break;

            case "PlayerMode":
                if ( item.setPlayerMode(stack, item.getPlayerMode(stack) + amount).isEmpty() )
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
