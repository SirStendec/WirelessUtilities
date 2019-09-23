package com.lordmau5.wirelessutils.tile.base;

public interface IConfigurableRange {

    boolean isFacingY();

    void saveRanges();

    int getRange();

    int getRangeLength();

    int getRangeWidth();

    int getRangeHeight();

    void setRangeLength(int length);

    void setRangeWidth(int width);

    void setRangeHeight(int height);

    void setRanges(int height, int length, int width);

    int getOffsetHorizontal();

    int getOffsetVertical();

    void setOffsetHorizontal(int offset);

    void setOffsetVertical(int offset);

}
