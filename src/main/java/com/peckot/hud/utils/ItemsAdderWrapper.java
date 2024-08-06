package com.peckot.hud.utils;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;

public class ItemsAdderWrapper
{
    public static FontImageWrapper getFontImage(String id) throws NullPointerException
    {
        FontImageWrapper fontImageWrapper = new FontImageWrapper(id);
        if (!fontImageWrapper.exists())
            throw new NullPointerException("Can't find font_image: " + id);
        return fontImageWrapper;
    }
}
