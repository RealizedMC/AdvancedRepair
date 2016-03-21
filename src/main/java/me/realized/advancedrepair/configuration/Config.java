package me.realized.advancedrepair.configuration;

import me.realized.advancedrepair.Core;
import me.realized.advancedrepair.management.Cooldown;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private final Core instance;
    private Map<String, Cooldown> cooldowns = new HashMap<>();
    private Map<String, String> messages = new HashMap<>();
    private boolean enchant = true;
    private boolean rename = true;

    public Config(Core instance) {
        this.instance = instance;
    }

    public void load() {
        File file = new File(instance.getDataFolder(), "config.yml");

        if (!file.exists()) {
            instance.saveResource("config.yml", true);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.isBoolean("repair-enchanted")) {
            this.enchant = config.getBoolean("repair-enchanted");
        } else {
            instance.warn("Error in boolean value 'repair-enchanted': Value does not exist or invalid.");
        }

        if (config.isBoolean("repair-renamed")) {
            this.rename = config.getBoolean("repair-renamed");
        } else {
            instance.warn("Error in boolean value 'repair-renamed': Value does not exist or invalid.");
        }

        if (config.isConfigurationSection("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, config.getString("messages." + key));
            }
        }

        if (config.isConfigurationSection("group-cooldowns")) {
            List<String> groups = Arrays.asList(instance.getPermission().getGroups());

            for (String name : config.getConfigurationSection("group-cooldowns").getKeys(false)) {
                if (!groups.contains(name)) {
                    instance.warn("Failed to load cooldown for group '" + name + "': Not an existing group or not capitalized correctly.");
                    continue;
                }

                String path = "group-cooldowns." + name + ".";
                int hand = 0;

                if (config.isInt(path + "repair-hand")) {
                    hand = config.getInt(path + "repair-hand");
                } else {
                    instance.warn("Error occurred while loading cooldown for group '" + name + "': config value 'repair-hand' not found or not a valid number, using 0 by default.");
                }

                int all = 0;

                if (config.isInt(path + "repair-all")) {
                    all= config.getInt(path + "repair-all");
                } else {
                    instance.warn("Error occurred while loading cooldown for group '" + name + "': config value 'repair-all' not found or not a valid number, using 0 by default.");
                }

                cooldowns.put(name, new Cooldown(hand, all));
            }

            instance.info("Loaded cooldowns for " + cooldowns.size() + " groups.");
        }
    }

    public boolean isEnchantRepair() {
        return enchant;
    }

    public boolean isRenameRepair() {
        return rename;
    }

    public String getString(String key) {
        return messages.get(key);
    }

    public Cooldown getByName(String group) {
        return cooldowns.get(group);
    }
}
