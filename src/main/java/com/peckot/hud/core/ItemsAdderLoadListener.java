package com.peckot.hud.core;

import com.peckot.hud.utils.EventsUtil;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class ItemsAdderLoadListener implements Listener
{
    private final Plugin plugin;
    private final Huds huds;

    ItemsAdderLoadListener(Plugin plugin, Huds huds)
    {
        this.plugin = plugin;
        this.huds = huds;
    }

    public void registerListener()
    {
        EventsUtil.registerEventOnce(this, plugin);
    }

    @EventHandler
    private void onItemsAdderLoadData(ItemsAdderLoadDataEvent e)
    {
        plugin.getLogger().log(Level.INFO, "HUD - ItemsAdder finished loading");
        huds.needsIaZip = false;

        huds.cleanup();
        huds.initAllPlayers();
    }
}
