package com.peckot.hud.core;

import com.peckot.hud.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;

public class Settings
{
    public long refreshIntervalTicks;
    public long refreshHighFrequencyIntervalTicks;
    
    public boolean moneyEnabled;
    public String moneyPapi;
    public int moneyOffset;
    public HashSet<String> moneyWorlds;
    
    public boolean pointsEnabled;
    public String pointsPapi;
    public int pointsOffset;
    public HashSet<String> pointsWorlds;

    public boolean debug;
    public String msgHudNotFound;
    public String msgWrongUsage;
    public String msgDestinationSet;
    public String msgDestinationRemoved;

    public Settings(FileConfiguration config)
    {
        this.refreshIntervalTicks = config.getLong("huds_refresh_interval_ticks", 30);
        this.refreshHighFrequencyIntervalTicks = config.getLong("huds_high_frequency_refresh_interval_ticks", 2);
        
        this.moneyEnabled = config.getBoolean("money.enabled", true);
        this.moneyPapi = config.getString("money.papi_placeholder", "%vault_eco_balance_fixed%");
        this.moneyOffset = config.getInt("money.offset", 88);
        this.moneyWorlds = new HashSet<>(config.getStringList("money.worlds"));
        
        this.pointsEnabled = config.getBoolean("points.enabled", true);
        this.pointsPapi = config.getString("points.papi_placeholder", "%playerpoints_points%");
        this.pointsOffset = config.getInt("points.offset", 88);
        this.pointsWorlds = new HashSet<>(config.getStringList("points.worlds"));

        this.debug = config.getBoolean("log.debug", false);

        this.msgHudNotFound = Utils.color(config.getString("lang.hud_not_found", "&a无效的HUD!"));
        this.msgWrongUsage = Utils.color(config.getString("lang.wrong_usage", "&c错误的命令使用"));
        this.msgDestinationSet = Utils.color(config.getString("lang.destination_set", "&aDestination set."));
        this.msgDestinationRemoved = Utils.color(config.getString("lang.destination_removed", "&7Destination removed."));
    }
}
