package me.realized.advancedrepair.management;

import me.realized.advancedrepair.Core;
import me.realized.advancedrepair.configuration.Config;
import me.realized.advancedrepair.utilities.DateUtil;
import me.realized.advancedrepair.utilities.RepairType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Core instance;
    private final Config config;

    private File file;
    private FileConfiguration dataConfig;

    private Map<UUID, Long> hand = new HashMap<>();
    private Map<UUID, Long> all = new HashMap<>();

    public CooldownManager(Core instance) {
        this.instance = instance;
        this.config = instance.getConfiguration();
    }

    public void load() {
        file = new File(instance.getDataFolder(), "data.yml");

        try {
            boolean exists = !file.createNewFile();

            if (!exists) {
                instance.info("Generated 'data.yml'.");
            }
        } catch (IOException e) {
            instance.warn("Failed to generate 'data.yml'! Error: " + e.getMessage());
        }

        dataConfig = YamlConfiguration.loadConfiguration(file);

        if (dataConfig.isConfigurationSection("cooldowns")) {
            for (String key : dataConfig.getConfigurationSection("cooldowns").getKeys(false)) {
                String path = "cooldowns." + key + ".";
                UUID uuid = UUID.fromString(key);

                if (dataConfig.isLong(path + "ALL")) {
                    all.put(uuid, dataConfig.getLong(path + "ALL"));
                }

                if (dataConfig.isLong(path + "HAND")) {
                    hand.put(uuid, dataConfig.getLong(path + "HAND"));
                }
            }

            instance.info("Loaded " + (hand.size() + all.size()) + " cooldowns.");
        }
    }

    public void save() {
        if (hand.isEmpty() && all.isEmpty()) {
            instance.info("No cooldowns loaded, skipping save.");
            return;
        }

        dataConfig.set("cooldowns", null);

        for (Map.Entry<UUID, Long> entry : hand.entrySet()) {
            dataConfig.set("cooldowns." + entry.getKey().toString() + ".HAND", entry.getValue());
        }

        for (Map.Entry<UUID, Long> entry : all.entrySet()) {
            dataConfig.set("cooldowns." + entry.getKey().toString() + ".ALL", entry.getValue());
        }

        try {
            dataConfig.save(file);
        } catch (IOException e) {
            instance.warn("Failed to save 'data.yml'! Error: " + e.getMessage());
        }

        instance.info("Saved " + (hand.size() + all.size()) + " cooldowns.");
    }

    public boolean has(Player player, RepairType type) {
        if (player.hasPermission("arepair.bypass")) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        Cooldown cooldown = config.getByName(getGroup(player));

        if (cooldown == null) {
            return false;
        }

        long now = new GregorianCalendar().getTimeInMillis();
        long last;
        boolean expired = true;

        switch (type) {
            case HAND:
                if (hand.get(uuid) == null) {
                    return false;
                }

                last = hand.get(uuid);
                expired = last + (cooldown.getHandCooldown() * 1000) - now <= 0;

                if (expired) {
                    hand.remove(uuid);
                }

                break;
            case ALL:
                if (all.get(uuid) == null) {
                    return false;
                }

                last = all.get(uuid);
                expired = last + (cooldown.getAllCooldown() * 1000) - now <= 0;

                if (expired) {
                    all.remove(uuid);
                }

                break;
        }

        return !expired;
    }

    public String remaining(Player player, RepairType type) {
        UUID uuid = player.getUniqueId();
        String group = getGroup(player);
        Cooldown cooldown = config.getByName(group);
        long now = new GregorianCalendar().getTimeInMillis();
        long last;
        long remaining = 0;

        switch (type) {
            case HAND:
                if (hand.get(uuid) == null) {
                    return null;
                }

                last = hand.get(uuid);
                remaining = last + (cooldown.getHandCooldown() * 1000) - now;
                break;
            case ALL:
                if (all.get(uuid) == null) {
                    return null;
                }

                last = all.get(uuid);
                remaining = last + (cooldown.getAllCooldown() * 1000) - now;
                break;
        }

        return DateUtil.formatDate(remaining / 1000 + (remaining % 1000 > 0 ? 1 : 0));
    }

    public void apply(Player player, RepairType type) {
        if (player.hasPermission("arepair.bypass")) {
            return;
        }

        String group = getGroup(player);
        long now = new GregorianCalendar().getTimeInMillis();

        switch (type) {
            case HAND:
                if (config.getByName(group).getHandCooldown() <= 0) {
                    return;
                }

                hand.put(player.getUniqueId(), now);
                break;

            case ALL:
                if (config.getByName(group).getAllCooldown() <= 0) {
                    return;
                }

                all.put(player.getUniqueId(), now);
                break;
        }
    }

    public void reset(UUID uuid) {
        hand.remove(uuid);
        all.remove(uuid);
    }

    private String getGroup(Player player) {
        String group = instance.getPermission().getPrimaryGroup(player);

        if (group == null || config.getByName(group) == null) {
            for (String groups : instance.getPermission().getPlayerGroups(player)) {
                if (config.getByName(groups) != null) {
                    group = groups;
                    break;
                }
            }
        }

        return group;
    }
}
