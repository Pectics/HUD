package com.peckot.hud;

import com.peckot.hud.core.Commands;
import com.peckot.hud.core.Huds;
import com.peckot.hud.core.Settings;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class HUD extends JavaPlugin implements Listener
{
    private static HUD instance;
    public static Settings settings;
    private static Huds huds;

    @Nullable
    public static Economy econ = null;

    public static HUD inst()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        instance = this;

        initVaultEconomy();
        initConfig();

        huds = new Huds(this);

        new Commands().register();
    }

    @Override
    public void onDisable()
    {
        huds.cleanup();
    }

    public void initConfig()
    {
        reloadConfig();

        try
        {
            File configFile = new File(getDataFolder(), "config.yml");
            FileConfiguration config = getConfig();
            InputStream configResource = getResource("config.yml");
            if(configResource == null)
            {
                getLogger().severe("Error. Missing config.yml inside the JAR file.");
                return;
            }

            // Load the default file from JAR resources
            if (!configFile.exists())
            {
                FileUtils.copyInputStreamToFile(configResource, configFile);
            }
            else // Add missing properties
            {
                FileConfiguration tmp = YamlConfiguration.loadConfiguration((new InputStreamReader(configResource, StandardCharsets.UTF_8)));
                for (String k : tmp.getKeys(true))
                {
                    if (!config.contains(k))
                        config.set(k, tmp.get(k));
                }
                config.save(configFile);
            }
            config.load(configFile);
        }
        catch (IOException | InvalidConfigurationException e)
        {
            getLogger().severe("Error loading config.yml file.");
            e.printStackTrace();
        }

        settings = new Settings(getConfig());
    }

    private void initVaultEconomy()
    {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
            econ = rsp.getProvider();
    }

    public void reloadPlugin()
    {
        huds.cleanup();
        initVaultEconomy();
        initConfig();
        huds.initAllPlayers();
    }
}
