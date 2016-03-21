package me.realized.advancedrepair.commands;

import me.realized.advancedrepair.Core;
import me.realized.advancedrepair.configuration.Config;
import me.realized.advancedrepair.management.CooldownManager;
import me.realized.advancedrepair.utilities.RepairType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARCommand implements CommandExecutor {

    private final Core instance;
    private final Config config;
    private final CooldownManager manager;

    public ARCommand(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
        this.manager = instance.getManager();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("arepair.admin")) {
            pm(sender, "&c[" + instance.getDescription().getFullName() + "] " + config.getString("not-authorized").replace("%permission%", "arepair.admin"));
            return true;
        }

        if (args.length < 1) {
            pm(sender, "&3AdvancedRepair");
            pm(sender, "&b/ar [view:reset] <player>");
            pm(sender, "&b/ar reload");
            return true;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("view")) {
                Player target = Bukkit.getPlayerExact(args[1]);

                if (target == null) {
                    pm(sender, "&cThat player was not found.");
                    return true;
                }

                pm(sender, "&b" + target.getName() + "'s cooldowns:");

                if (manager.has(target, RepairType.ALL)) {
                    pm(sender, "&a/repair all: " + manager.remaining(target, RepairType.ALL));
                }

                if (manager.has(target, RepairType.HAND)) {
                    pm(sender, "&a/repair hand: " + manager.remaining(target, RepairType.HAND));
                }

                return true;
            }

            if (args[0].equalsIgnoreCase("reset")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                if (target == null || !target.hasPlayedBefore()) {
                    pm(sender, "&cThat player was not found.");
                    return true;
                }

                manager.reset(target.getUniqueId());
                pm(sender, "&bAll cooldowns were reset for " + args[1] + ".");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            long start = System.currentTimeMillis();
            manager.save();
            manager.load();
            config.load();
            long end = System.currentTimeMillis();
            pm(sender, "&bReload complete! Took " + (end - start) + "ms.");
            return true;
        }
        return true;
    }

    private void pm(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }
}
