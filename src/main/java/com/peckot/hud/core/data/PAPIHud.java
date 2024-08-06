package com.peckot.hud.core.data;

import com.peckot.hud.core.settings.HudSettings;
import dev.lone.itemsadder.api.FontImages.PlayerHudsHolderWrapper;

public abstract class PAPIHud<T extends HudSettings> extends Hud<T>
{
    public String placeholder;

    public PAPIHud(String placeholder, PlayerHudsHolderWrapper holder, T settings)
    {
        super(holder, settings);
        this.placeholder = placeholder;
    }
}
