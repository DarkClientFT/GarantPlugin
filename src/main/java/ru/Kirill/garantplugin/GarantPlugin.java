package ru.Kirill.garantplugin;

import ru.Kirill.garantplugin.command.GarantCommand;
import ru.Kirill.garantplugin.config.ConfigManager;
import ru.Kirill.garantplugin.deal.DealManager;
import ru.Kirill.garantplugin.gui.MenuManager;
import ru.Kirill.garantplugin.listener.ChatListener;
import ru.Kirill.garantplugin.listener.MenuListener;
import ru.Kirill.garantplugin.listener.PlayerListener;
import ru.Kirill.garantplugin.manager.BossBarManager;
import ru.Kirill.garantplugin.manager.EconomyManager;
import ru.Kirill.garantplugin.manager.StatsManager;
import ru.Kirill.garantplugin.storage.Storage;
import ru.Kirill.garantplugin.storage.StorageFactory;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class GarantPlugin extends JavaPlugin {

    @Getter
    private static GarantPlugin instance;

    private ConfigManager configManager;
    private EconomyManager economyManager;
    private DealManager dealManager;
    private MenuManager menuManager;
    private BossBarManager bossBarManager;
    private StatsManager statsManager;
    private Storage storage;

    @Override
    public void onEnable() {
        instance = this;
        //// new
        // конфиг
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        // инитиализация хранилищя
        storage = StorageFactory.createStorage(this);
        storage.init();
        // инитиализация менеджеров
        economyManager = new EconomyManager(this);
        bossBarManager = new BossBarManager(this);
        dealManager = new DealManager(this);
        menuManager = new MenuManager(this);
        statsManager = new StatsManager(this);
        // команда
        getCommand("garant").setExecutor(new GarantCommand(this));
        getCommand("garant").setTabCompleter(new GarantCommand(this));
        // слушатели
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        //// old
        // initConfig();
        // initStorage();
        // initManagers();
        // registerCommands();
        // registerListeners();
    }

    @Override
    public void onDisable() {
        if (dealManager != null) {
            dealManager.shutdown();
        }
        if (bossBarManager != null) {
            bossBarManager.removeAll();
        }
        if (storage != null) {
            storage.shutdown();
        }
    }

        /*private void initConfig() {
        }

        private void initStorage() {
        }

        private void initManagers() {
        }

        private void registerCommands() {
        }

        private void registerListeners() {
        }*/

    public void reload() {
        reloadConfig();
        configManager.reload();
    }
}