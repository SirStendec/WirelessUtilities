package com.lordmau5.wirelessutils.gui.client.item;

import cofh.api.core.IAugmentable;
import cofh.core.gui.element.ElementTextField;
import cofh.core.gui.element.ElementTextFieldLimited;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.gui.client.base.BaseGuiItem;
import com.lordmau5.wirelessutils.gui.client.elements.TabLock;
import com.lordmau5.wirelessutils.gui.client.elements.TabOpenGui;
import com.lordmau5.wirelessutils.gui.client.pages.PageItemColor;
import com.lordmau5.wirelessutils.gui.client.pages.base.PageBase;
import com.lordmau5.wirelessutils.gui.container.items.ContainerAdminAugment;
import com.lordmau5.wirelessutils.item.augment.ItemAugment;
import com.lordmau5.wirelessutils.proxy.CommonProxy;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class GuiAdminAugment extends BaseGuiItem {

    public final static ResourceLocation TEXTURE = new ResourceLocation(WirelessUtils.MODID, "textures/gui/directional_machine.png");

    public final static String INT_CHARACTERS = "-1234567890";
    public final static String DOUBLE_CHARACTERS = ",." + INT_CHARACTERS;
    public final static String HEX_CHARACTERS = "ABCDEFabcdef" + INT_CHARACTERS;

    private final ContainerAdminAugment container;

    private ElementTextField txtEnergyAdd;
    private ElementTextField txtEnergyMult;
    private ElementTextField txtEnergyDrain;

    private ElementTextField txtBudgetAdd;
    private ElementTextField txtBudgetMult;

    private ElementTextField txtTierName;
    private ElementTextField txtLevel;
    private ElementTextField txtMachines;

    public GuiAdminAugment(ContainerAdminAugment container) {
        super(container, TEXTURE);
        this.container = container;

        xSize = 176;
        drawTitle = true;
        drawOwnInventory = false;
        drawName = false;
    }

    public ContainerAdminAugment getContainer() {
        return container;
    }

    @Override
    public boolean wantsSlots() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();

        setMainPageTabLabel("btn." + WirelessUtils.MODID + ".main");

        addPage(new PageItemColor(this));

        ItemStack stack = container.getItemStack();
        Item item = stack.getItem();
        if ( item instanceof ItemAugment ) {
            PageBase page = ((ItemAugment) item).getAdminGuiPage(this);
            if ( page != null )
                addPage(page);
        }

        addTab(new TabLock(this, container));
        addTab(new TabOpenGui(this));

        txtEnergyAdd = new ElementTextFieldLimited(this, xSize - 83, 18, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.clearEnergyAddition();
                    else
                        container.setEnergyAddition(Integer.parseInt(text));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(INT_CHARACTERS, true);

        txtEnergyDrain = new ElementTextFieldLimited(this, xSize - 83, 33, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.clearEnergyDrain();
                    else
                        container.setEnergyDrain(Integer.parseInt(text));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(INT_CHARACTERS, true);

        txtEnergyMult = new ElementTextFieldLimited(this, xSize - 83, 48, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.clearEnergyMultiplier();
                    else
                        container.setEnergyMultiplier(Double.parseDouble(text));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(DOUBLE_CHARACTERS, true);

        txtBudgetAdd = new ElementTextFieldLimited(this, xSize - 83, 63, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.clearBudgetAddition();
                    else
                        container.setBudgetAddition(Integer.parseInt(text));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(INT_CHARACTERS, true);

        txtBudgetMult = new ElementTextFieldLimited(this, xSize - 83, 78, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.clearBudgetMultiplier();
                    else
                        container.setBudgetMultiplier(Double.parseDouble(text));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(DOUBLE_CHARACTERS, true);

        txtTierName = new ElementTextField(this, xSize - 83, 93, 75, 10) {
            boolean changed = false;

            void updateValue() {
                String text = getText();
                if ( text == null || text.isEmpty() )
                    container.setTierName(null);
                else
                    container.setTierName(text);

                changed = false;
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        };

        txtLevel = new ElementTextFieldLimited(this, xSize - 83, 108, 75, 10, (short) 50) {
            boolean changed = false;

            void updateValue() {
                try {
                    String text = getText();
                    if ( text == null || text.isEmpty() )
                        container.setRequiredLevel(null);
                    else
                        container.setRequiredLevel(Level.fromInt(Integer.parseInt(text)));

                    changed = false;
                } catch (NumberFormatException ex) {
                    /* do nothing */
                }
            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setFilter(INT_CHARACTERS, true);

        txtMachines = new ElementTextField(this, xSize - 83, 123, 75, 45) {
            boolean changed = false;

            void updateValue() {
                String text = getText();
                if ( text == null || text.isEmpty() )
                    container.setAllowedMachines(null);
                else
                    container.setAllowedMachines(text.split("[ \t\n]*\n[ \t\n]*"));

                changed = false;
            }

            @Override
            public void addTooltip(List<String> list) {
                super.addTooltip(list);

                ItemStack stack = container.getItemStack();
                ItemAugment item = (ItemAugment) stack.getItem();
                if ( item == null )
                    return;

                list.add(StringHelper.localize("item." + WirelessUtils.MODID + ".augment_ctrl.active"));
                for (Class<? extends TileEntity> klass : CommonProxy.MACHINES) {
                    if ( !IAugmentable.class.isAssignableFrom(klass) )
                        continue;

                    Class<? extends IAugmentable> augClass = klass.asSubclass(IAugmentable.class);
                    Machine machine = klass.getAnnotation(Machine.class);
                    if ( machine != null && item.canApplyTo(stack, augClass) ) {
                        list.add(new TextComponentTranslation(
                                "item." + WirelessUtils.MODID + ".augment_ctrl.entry",
                                new TextComponentString(machine.name()).setStyle(TextHelpers.WHITE)
                        ).setStyle(TextHelpers.GRAY).getFormattedText());
                    }
                }

            }

            @Override
            protected void onCharacterEntered(boolean success) {
                super.onCharacterEntered(success);

                if ( success && isFocused() )
                    changed = true;
            }

            @Override
            protected void onFocusLost() {
                super.onFocusLost();
                if ( changed )
                    updateValue();
            }
        }.setMultiline(true).setMaxLength(Short.MAX_VALUE);

        addElement(txtEnergyAdd);
        addElement(txtEnergyMult);
        addElement(txtEnergyDrain);

        addElement(txtBudgetAdd);
        addElement(txtBudgetMult);

        addElement(txtTierName);
        addElement(txtLevel);
        addElement(txtMachines);

        String[] machines = container.getAllowedMachines();
        if ( machines == null )
            txtMachines.setText("");
        else
            txtMachines.setText(String.join("\n", machines));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        container.sendUpdate();
    }

    public boolean hasNormalGui() {
        return container.hasNormalGui();
    }

    public void openNormalGui() {
        container.setWantNormalGui(true);
        mc.player.closeScreen();
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();

        if ( isMainPage() ) {
            final boolean locked = container.isLocked();

            txtBudgetAdd.setEnabled(!locked);
            txtBudgetMult.setEnabled(!locked);

            txtEnergyAdd.setEnabled(!locked);
            txtEnergyMult.setEnabled(!locked);
            txtEnergyDrain.setEnabled(!locked);

            txtTierName.setEnabled(!locked);
            txtLevel.setEnabled(!locked);
            txtMachines.setEnabled(!locked);

            if ( !txtEnergyAdd.isFocused() )
                txtEnergyAdd.setText(String.valueOf(container.getEnergyAddition()));

            if ( !txtEnergyMult.isFocused() )
                txtEnergyMult.setText(String.valueOf(container.getEnergyMultiplier()));

            if ( !txtEnergyDrain.isFocused() )
                txtEnergyDrain.setText(String.valueOf(container.getEnergyDrain()));

            if ( !txtBudgetAdd.isFocused() )
                txtBudgetAdd.setText(String.valueOf(container.getBudgetAddition()));

            if ( !txtBudgetMult.isFocused() )
                txtBudgetMult.setText(String.valueOf(container.getBudgetMultiplier()));

            if ( !txtTierName.isFocused() )
                txtTierName.setText(container.getTierName());

            if ( !txtLevel.isFocused() ) {
                Level required = container.getRequiredLevel();
                if ( required == null )
                    required = Level.getMinLevel();

                txtLevel.setText(String.valueOf(required.toInt()));
            }
        }

        name = container.getItemStack().getDisplayName();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        if ( isMainPage() ) {
            int x = xSize - 91;

            drawRightAlignedText("Energy Add:", x, 19, container.hasEnergyAddition() ? 0x008000 : 0x404040);
            drawRightAlignedText("Energy Drain:", x, 34, container.hasEnergyDrain() ? 0x008000 : 0x404040);
            drawRightAlignedText("Energy Mult:", x, 49, container.hasEnergyMultiplier() ? 0x008000 : 0x404040);

            drawRightAlignedText("Budget Add:", x, 64, container.hasBudgetAddition() ? 0x008000 : 0x404040);
            drawRightAlignedText("Budget Mult:", x, 79, container.hasBudgetMultiplier() ? 0x008000 : 0x404040);

            drawRightAlignedText("Tier Name:", x, 94, container.hasTierName() ? 0x008000 : 0x404040);
            drawRightAlignedText("Required Lvl:", x, 109, container.hasRequiredLevel() ? 0x008000 : 0x404040);
            drawRightAlignedText("Machines:", x, 124, container.hasAllowedMachines() ? 0x008000 : 0x404040);
        }
    }
}
