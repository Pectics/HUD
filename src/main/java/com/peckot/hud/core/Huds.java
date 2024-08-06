package com.peckot.hud.core;

import com.peckot.hud.HUD;
import com.peckot.hud.core.data.Hud;
import com.peckot.hud.core.data.MoneyHud;
import com.peckot.hud.core.data.PlayerData;
import com.peckot.hud.core.data.PointsHud;
import com.peckot.hud.core.settings.MoneySettings;
import com.peckot.hud.core.settings.PointsSettings;
import dev.lone.itemsadder.api.FontImages.PlayerHudsHolderWrapper;
import dev.lone.itemsadder.api.ItemsAdder;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Huds {
    private static Huds instance;

    static final String WARNING = "Please don't forget to regen your resourcepack using /iazip command.";

    private final HUD plugin;
    private final HashMap<Player, PlayerData> datasByPlayer = new HashMap<>();
    private final List<PlayerData> datas = new ArrayList<>();
    private final List<BukkitTask> refreshTasks = new ArrayList<>();

    boolean needsIaZip;
    boolean notifyIazip;
    private boolean allPlayersInitialized;

    //TODO: recode this shit. Very dirty
    private final List<String> hudsNames = Arrays.asList("hud:money", "hud:compass", "hud:quiver", "hud:arrow_target");


    public Huds(HUD plugin)
    {
        instance = this;

        this.plugin = plugin;

        new EventsListener(plugin, this).registerListener();

        extractDefaultAssets();

        if (ItemsAdder.areItemsLoaded() && !needsIaZip)
            initAllPlayers();
    }

    public static Huds inst()
    {
        return instance;
    }

    public List<String> getHudsNames()
    {
        return hudsNames;
    }

    @Nullable
    public Hud<?> getPlayerHud(Player player, String namespacedID)
    {
        PlayerData playerData = datasByPlayer.get(player);
        if (playerData == null)
            return null;
        return playerData.allHuds_byNamespacedId.get(namespacedID);
    }

    public void initAllPlayers()
    {
        if(allPlayersInitialized)
        {
            plugin.getLogger().severe("Error: players already initialized! Be sure to first call Huds#cleanup().");
            return;
        }
        try
        {
            for (Player player : Bukkit.getServer().getOnlinePlayers())
                initPlayer(player);
            scheduleRefresh();
            allPlayersInitialized = true;
        }
        catch (NullPointerException e)
        {
            plugin.getLogger().warning(WARNING);
        }
    }

    void initPlayer(Player player)
    {
        PlayerData playerData;
        try
        {
            playerData = new PlayerData(new PlayerHudsHolderWrapper(player));

            //TODO: recode this shit. Very dirty
            if (HUD.settings.moneyEnabled)
            {
                playerData.registerHud(new MoneyHud(
                    HUD.settings.moneyPapi,
                    playerData.getHolder(),
                    new MoneySettings(
                        "hud:money",
                        "hud:money_icon",
                        "hud:money_digit_0",
                        "hud:money_digit_1",
                        "hud:money_digit_2",
                        "hud:money_digit_3",
                        "hud:money_digit_4",
                        "hud:money_digit_5",
                        "hud:money_digit_6",
                        "hud:money_digit_7",
                        "hud:money_digit_8",
                        "hud:money_digit_9",
                        "hud:money_char_unknown",
                        "hud:money_char_k",
                        "hud:money_char_m",
                        "hud:money_char_b",
                        "hud:money_char_t",
                        "hud:money_char_dot",
                        "hud:money_char_comma",
                        "hud:money_char_arrow_up",
                        "hud:money_char_arrow_down",
                        HUD.settings.moneyOffset,
                        HUD.settings.moneyWorlds
                    )
                ), false);
            }
            
            if (HUD.settings.pointsEnabled)
            {
                playerData.registerHud(new PointsHud(
                    HUD.settings.pointsPapi,
                    playerData.getHolder(),
                    new PointsSettings(
                        "hud:points",
                        "hud:points_icon",
                        "hud:points_digit_0",
                        "hud:points_digit_1",
                        "hud:points_digit_2",
                        "hud:points_digit_3",
                        "hud:points_digit_4",
                        "hud:points_digit_5",
                        "hud:points_digit_6",
                        "hud:points_digit_7",
                        "hud:points_digit_8",
                        "hud:points_digit_9",
                        "hud:points_char_unknown",
                        "hud:points_char_arrow_up",
                        "hud:points_char_arrow_down",
                        HUD.settings.pointsOffset,
                        HUD.settings.pointsWorlds
                    )
                ), false);
            }

            datasByPlayer.put(player, playerData);
            datas.add(playerData);
        }
        catch (NullPointerException exc)
        {
            HUD.inst().getLogger().severe(ChatColor.RED + "Failed to load PlayerData: " + exc.getMessage());
        }
    }

    //TODO: implement animated icons.
    // Warning: make sure to increment the refresh rate only when it's actually needed by the animation.
    // I don't want the plugin to become heavy just for a stupid animation.
    private void scheduleRefresh()
    {
        refreshTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PlayerData data : datas)
                data.refreshAllHuds();
        }, HUD.settings.refreshIntervalTicks, HUD.settings.refreshIntervalTicks));

        refreshTasks.add(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PlayerData data : datas)
                data.refreshHighFrequency();
        }, HUD.settings.refreshHighFrequencyIntervalTicks, HUD.settings.refreshHighFrequencyIntervalTicks));
    }

    void unregisterAllPlayers()
    {
        for (BukkitTask task : refreshTasks)
            task.cancel();
        refreshTasks.clear();
        allPlayersInitialized = false;
    }

    public void cleanup()
    {
        unregisterAllPlayers();

        for (PlayerData data : datas)
            data.cleanup();

        datas.clear();
        datasByPlayer.clear();
    }

    private void extractDefaultAssets()
    {
        CodeSource src = HUD.class.getProtectionDomain().getCodeSource();
        if (src != null)
        {
            File itemsadderRoot = new File(plugin.getDataFolder().getParent() + "/ItemsAdder");

            URL jar = src.getLocation();
            ZipInputStream zip;
            try
            {
                plugin.getLogger().info(ChatColor.AQUA + "Extracting assets...");

                zip = new ZipInputStream(jar.openStream());
                while (true)
                {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null)
                        break;

                    String name = e.getName();
                    if (!e.isDirectory())
                    {
                        if (name.startsWith("contents/hud"))
                        {
                            doExtractFile(itemsadderRoot, name, name);
                        }
                    }
                }

                plugin.getLogger().info(ChatColor.GREEN + "DONE extracting assets!");
            }
            catch (IOException e)
            {
                plugin.getLogger().severe("        ERROR EXTRACTING assets! StackTrace:");
                e.printStackTrace();
            }
        }

        notifyIazip = needsIaZip;
        if (needsIaZip)
            plugin.getLogger().warning(WARNING);
    }

    private void doExtractFile(File itemsadderRoot, String name, String destName) throws IOException
    {
        File dest = new File(itemsadderRoot, destName);
        if (!dest.exists())
        {
            FileUtils.copyInputStreamToFile(plugin.getResource(name), dest);
            plugin.getLogger().info(ChatColor.AQUA + "       - Extracted " + destName);
            needsIaZip = true;
        }
    }
}
