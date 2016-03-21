package me.realized.advancedrepair;

import me.realized.advancedrepair.commands.ARCommand;
import me.realized.advancedrepair.commands.RepairCommand;
import me.realized.advancedrepair.configuration.Config;
import me.realized.advancedrepair.management.CooldownManager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Core extends JavaPlugin {

    private CooldownManager manager;
    private Config config;
    private Permission permission;

    private static final Logger LOGGER = Bukkit.getLogger();

    @Override
    public void onEnable() {
        if (!hookVault()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        config = new Config(this);
        config.load();

        manager = new CooldownManager(this);
        manager.load();

        getCommand("repair").setExecutor(new RepairCommand(this));
        getCommand("ar").setExecutor(new ARCommand(this));
    }

    @Override
    public void onDisable() {
        manager.save();
    }

    public void warn(String msg) {
        LOGGER.warning("[AdvancedRepair] " + msg);
    }

    public void info(String msg) {
        LOGGER.info("[AdvancedRepair] " + msg);
    }

    public Permission getPermission() {
        return permission;
    }

    public Config getConfiguration() {
        return config;
    }

    public CooldownManager getManager() {
        return manager;
    }

    private boolean hookVault() {
        RegisteredServiceProvider<Permission> rspPerm = getServer().getServicesManager().getRegistration(Permission.class);
        permission = rspPerm.getProvider();

        if (permission == null) {
            warn("Vault hook failed. Please check if it's installed.");
            return false;
        }

        return true;
    }
}
