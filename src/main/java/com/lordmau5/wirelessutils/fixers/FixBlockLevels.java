package com.lordmau5.wirelessutils.fixers;

import com.lordmau5.wirelessutils.WirelessUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.Constants;

public class FixBlockLevels implements IFixableData {

    public int getFixVersion() {
        return 1;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
        try {
            NBTTagCompound levelTag = compound.getCompoundTag("Level");
            NBTTagList teList = levelTag.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);
            NBTTagList sections = levelTag.getTagList("Sections", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < sections.tagCount(); i++) {
                NBTTagCompound section = sections.getCompoundTagAt(i);
                NBTTagList palette = section.getTagList("Palette", Constants.NBT.TAG_COMPOUND);

                for (int j = 0; j < palette.tagCount(); j++) {
                    // TODO: Whatever we need to do to keep our machines working.
                }
            }

        } catch (Exception err) {
            WirelessUtils.logger.warning("Unable to datafix machine metadata.");
        }

        return compound;
    }
}
