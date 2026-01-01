package ru.Kirill.garantplugin.storage;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.storage.mysql.MySQLStorage;
import ru.Kirill.garantplugin.storage.sqlite.SQLiteStorage;
import ru.Kirill.garantplugin.storage.type.StorageType;
import ru.Kirill.garantplugin.storage.yaml.YamlStorage;
import org.bukkit.configuration.ConfigurationSection;

public class StorageFactory {

    public static Storage createStorage(GarantPlugin plugin) {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("storage");
        if (config == null) {
            plugin.getLogger().warning("не найдена настройка хранилищя в конфиге");
            return new YamlStorage(plugin);
        }

        String type = config.getString("type", "YAML");
        StorageType storageType = StorageType.fromString(type);

        plugin.getLogger().info("использую хранилище: " + storageType.name());

        switch (storageType) {
            case MYSQL:
                ConfigurationSection mysql = config.getConfigurationSection("mysql");
                if (mysql == null) {
                    plugin.getLogger().warning("конфиг mysql не найден, использую YAML");
                    return new YamlStorage(plugin);
                }
                return new MySQLStorage(
                        plugin,
                        mysql.getString("host", "localhost"),
                        mysql.getInt("port", 3306),
                        mysql.getString("database", "unigarant"),
                        mysql.getString("username", "root"),
                        mysql.getString("password", ""),
                        mysql.getBoolean("useSSL", false)
                );

            case SQLITE:
                return new SQLiteStorage(plugin);

            case YAML:
            default:
                return new YamlStorage(plugin);
        }
    }
}