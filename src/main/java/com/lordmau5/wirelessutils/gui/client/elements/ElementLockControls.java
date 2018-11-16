package com.lordmau5.wirelessutils.gui.client.elements;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.BaseGuiContainer;
import com.lordmau5.wirelessutils.tile.desublimator.TileBaseDesublimator;
import com.lordmau5.wirelessutils.utils.Textures;
import net.minecraft.util.ResourceLocation;

public class ElementLockControls extends ElementContainer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    private final BaseGuiContainer gui;
    private final TileBaseDesublimator desublimator;

    private final ElementDynamicContainedButton btnLock;
    private final ElementDynamicContainedButton btnUnlock;

    public ElementLockControls(BaseGuiContainer gui, TileBaseDesublimator desublimator, int posX, int posY) {
        super(gui, posX, posY, 34, 16);
        this.gui = gui;
        this.desublimator = desublimator;

        btnLock = new ElementDynamicContainedButton(this, "Lock", 0, 0, 16, 16, Textures.LOCK);
        btnUnlock = new ElementDynamicContainedButton(this, "Unlock", 18, 0, 16, 16, Textures.UNLOCK);

        btnLock.setToolTip("btn." + WirelessUtils.MODID + ".slot_locks.lock");
        btnUnlock.setToolTip("btn." + WirelessUtils.MODID + ".slot_locks.unlock");

        addElement(btnLock);
        addElement(btnUnlock);
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {

        float pitch = 0.7F;

        switch (buttonName) {
            case "Lock":
                desublimator.setLocks();
                pitch = 1;
                break;
            case "Unlock":
                desublimator.clearLocks();
                break;
            default:
                return;
        }

        gui.playClickSound(pitch);
        desublimator.sendModePacket();
    }
}
