package me.realized.advancedrepair.commands;

import me.realized.advancedrepair.Core;
import me.realized.advancedrepair.configuration.Config;
import me.realized.advancedrepair.management.CooldownManager;
import me.realized.advancedrepair.utilities.RepairType;
import me.realized.advancedrepair.utilities.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RepairCommand implements CommandExecutor {

    private final Config config;
    private final CooldownManager manager;

    public RepairCommand(Core instance) {
        config = instance.getConfiguration();
        manager = instance.getManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            pm(sender, "&cThis command can only be executed in-game.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            try {
                repairHand(player);
            } catch (Exception e) {
                pm(player, e.getMessage());
            }

            return true;

        }

        else if (args[0].equalsIgnoreCase("hand")) {
            try {
                repairHand(player);
            } catch (Exception e) {
                pm(player, e.getMessage());
            }

            return true;

        }

        else if (args[0].equalsIgnoreCase("all")) {
            try {
                repairAll(player);
            } catch (Exception e) {
                pm(player, e.getMessage());
            }

            return true;
        }

        pm(player, config.getString("usage"));
        return true;
    }

    private void pm(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void repair(ItemStack item) throws Exception {
        Material type = item.getType();

        if (type.isBlock() || type.getMaxDurability() < 1) {
            throw new Exception(config.getString("invalid-item"));
        }

        if (item.getDurability() == 0) {
            throw new Exception(config.getString("already-maxed"));
        }

        item.setDurability((short) 0);
    }

    private void repair(Player player, ItemStack[] items, List<String> repaired) {
        for (ItemStack item : items) {
            if (item == null || item.getType().isBlock() || item.getDurability() == 0) {
                continue;
            }

            if (!item.getEnchantments().isEmpty() && !config.isEnchantRepair() && !player.hasPermission("arepair.repair.enchanted")) {
                continue;
            }

            if (item.getItemMeta().hasDisplayName() && !config.isRenameRepair() && !player.hasPermission("arepair.repair.renamed")) {
                continue;
            }

            try {
                repair(item);
            } catch (Exception e) {
                continue;
            }

            String name = item.getType().name().toLowerCase().replace("_", " ");

            repaired.add(name);
        }
    }

    private boolean repairHand(Player player) throws Exception {
        if (!player.hasPermission("arepair.repair.hand")) {
            throw new Exception(config.getString("not-authorized").replace("%permission%", "arepair.repair.hand"));
        }

        if (manager.has(player, RepairType.HAND)) {
            throw new Exception(config.getString("on-cooldown").replace("%remaining%", manager.remaining(player, RepairType.HAND)));
        }

        ItemStack held = player.getItemInHand();

        if (held == null || held.getType() == Material.AIR) {
            throw new Exception(config.getString("not-holding"));
        }

        if (!held.getEnchantments().isEmpty() && !config.isEnchantRepair() && !player.hasPermission("arepair.repair.enchanted")) {
            throw new Exception(config.getString("not-authorized").replace("%permission%", "arepair.repair.enchanted"));
        }

        if (held.getItemMeta().hasDisplayName() && !config.isRenameRepair() && !player.hasPermission("arepair.repair.renamed")) {
            throw new Exception(config.getString("not-authorized").replace("%permission%", "arepair.repair.renamed"));
        }

        try {
            repair(held);
        } catch (Exception e) {
            pm(player, e.getMessage());
            return false;
        }

        player.updateInventory();
        manager.apply(player, RepairType.HAND);
        pm(player, config.getString("on-repair-hand").replace("%held%", held.getType().name().replace("_", " ").toLowerCase()));
        return true;
    }

    private boolean repairAll(Player player) throws Exception {
        if (!player.hasPermission("arepair.repair.all")) {
            throw new Exception(config.getString("not-authorized").replace("%permission%", "arepair.repair.all"));
        }

        if (manager.has(player, RepairType.ALL)) {
            throw new Exception(config.getString("on-cooldown").replace("%remaining%", manager.remaining(player, RepairType.ALL)));
        }

        List<String> repaired = new ArrayList<>();

        if (player.hasPermission("arepair.repair.armor")) {
            repair(player, player.getInventory().getArmorContents(), repaired);
        }

        repair(player, player.getInventory().getContents(), repaired);
        player.updateInventory();

        if (repaired.isEmpty()) {
            throw new Exception(config.getString("no-needed"));
        }

        manager.apply(player, RepairType.ALL);
        pm(player, config.getString("on-repair-all").replace("%repaired_items%", StringUtil.join(repaired)));
        return true;
    }
}
