package com.peckot.hud.core;

import com.peckot.hud.HUD;
import com.peckot.hud.core.data.Hud;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter
{
    private static final List<String> EMPTY_LIST = Collections.singletonList("");

    public void register()
    {
        Bukkit.getPluginCommand("hud").setExecutor(this);
        Bukkit.getPluginCommand("hud").setTabCompleter(this);
    }

    private boolean hasPerm(CommandSender sender, Player target, String perm)
    {
        if (sender == target)
        {
            if (!sender.hasPermission(perm))
            {
                sender.sendMessage(ChatColor.RED + "No permission " + perm);
                return false;
            }
        }

        if (sender != target)
        {
            if (!sender.hasPermission(perm + ".others"))
            {
                sender.sendMessage(ChatColor.RED + "No permission " + perm + ".others");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args)
    {
        // Recode this shit
        if (command.getName().equals("hud")) {
            if (args.length == 1 && args[0].equals("reload")) {
                if (sender.hasPermission("hud.reload")) {
                    HUD.inst().reloadPlugin();
                    HUD.inst().getLogger().info(ChatColor.GREEN + "Reloaded");
                    if (sender instanceof Player)
                        sender.sendMessage(ChatColor.GREEN + "Reloaded");
                } else {
                    sender.sendMessage(ChatColor.RED + "No permission " + "hud.reload");
                }
                return true;
            }
            
            if (args.length < 2) {
                sender.sendMessage(HUD.settings.msgWrongUsage);
                return true;
            }
            
            Player player;
            if (args.length == 3) {
                player = Bukkit.getPlayer(args[2]);
            } else {
                if (sender instanceof Player)
                    player = (Player) sender;
                else {
                    sender.sendMessage(ChatColor.RED + "Please specify a player.");
                    return true;
                }
            }
            
            Hud<?> playerHud = Huds.inst().getPlayerHud(player, args[1]);
            if (playerHud == null) {
                sender.sendMessage(HUD.settings.msgHudNotFound);
                return true;
            }
            
            switch (args[0]) {
                case "show":
                    if (hasPerm(sender, player, "hud.show"))
                        playerHud.hide(false);
                    break;
                case "hide":
                    if (hasPerm(sender, player, "hud.hide"))
                        playerHud.hide(true);
                    break;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, @NotNull String[] args)
    {
        if (command.getName().equals("hud")) {
            if (args.length == 1) {
                if (sender.hasPermission("hud.reload"))
                    return Arrays.asList("reload", "show", "hide");
                return Arrays.asList("show", "hide");
            }
            
            if (args.length == 2)
                return Huds.inst().getHudsNames();
            if (args.length == 3) {
                List<String> names = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers())
                    names.add(p.getName());
                return names;
            }
        }
        return EMPTY_LIST;
    }
}
