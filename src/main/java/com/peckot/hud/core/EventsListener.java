package com.peckot.hud.core;

import com.peckot.hud.HUD;
import com.peckot.hud.utils.EventsUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

class EventsListener implements Listener
{
    private final Plugin plugin;
    private final Huds huds;
    private ItemsAdderLoadListener itemsAdderLoadListener;

    EventsListener(Plugin plugin, Huds huds)
    {
        this.plugin = plugin;
        this.huds = huds;
        this.itemsAdderLoadListener = new ItemsAdderLoadListener(plugin, huds);
    }

    public void registerListener()
    {
        EventsUtil.registerEventOnce(this, plugin);
        itemsAdderLoadListener.registerListener();
    }

    @EventHandler
    private void onItemsAdderLoad(PluginEnableEvent e)
    {
        if (!e.getPlugin().getName().equals("ItemsAdder"))
            return;

        if (HUD.settings.debug)
            plugin.getLogger().log(Level.INFO, "HUD - detected ItemsAdder loading...");

        if (this.itemsAdderLoadListener != null)
            this.itemsAdderLoadListener = new ItemsAdderLoadListener(plugin, huds);

        huds.initAllPlayers();
    }

    @EventHandler
    private void onItemsAdderUnload(PluginDisableEvent e)
    {
        if (!e.getPlugin().getName().equals("ItemsAdder"))
            return;

        if (HUD.settings.debug)
            plugin.getLogger().log(Level.INFO, "HUD - detected ItemsAdder unload...");

        EventsUtil.unregisterEvent(itemsAdderLoadListener);
        itemsAdderLoadListener = null;

        huds.cleanup();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e)
    {
        if (huds.notifyIazip)
        {
            if (e.getPlayer().isOp())
            {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    e.getPlayer().sendMessage(ChatColor.RED + Huds.WARNING);
                }, 60L);
                huds.notifyIazip = false;
            }
        }

        if (!huds.needsIaZip)
            huds.initPlayer(e.getPlayer());
    }
}
