package ru.Kirill.garantplugin.storage.yaml;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.model.DealStats;
import ru.Kirill.garantplugin.storage.AbstractStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlStorage extends AbstractStorage {

    private File dealsFile;
    private File statsFile;
    private FileConfiguration dealsConfig;
    private FileConfiguration statsConfig;

    public YamlStorage(GarantPlugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        plugin.getDataFolder().mkdirs();
        this.dealsFile = new File(plugin.getDataFolder(), "deals.yml");
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");

        createFileIfNotExists(dealsFile);
        createFileIfNotExists(statsFile);

        load();
    }

    private void createFileIfNotExists(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("ошибка создания файла: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void shutdown() {
        save();
    }

    @Override
    public void load() {
        dealsConfig = YamlConfiguration.loadConfiguration(dealsFile);
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    @Override
    public void save() {
        try {
            if (dealsConfig != null) {
                dealsConfig.save(dealsFile);
            }
            if (statsConfig != null) {
                statsConfig.save(statsFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("ошибка: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void saveDeal(Deal deal) {
        String path = "deals." + deal.getId().toString();
        dealsConfig.set(path + ".playerId", deal.getPlayerId().toString());
        dealsConfig.set(path + ".playerName", deal.getPlayerName());
        dealsConfig.set(path + ".description", deal.getDescription());
        dealsConfig.set(path + ".createdAt", deal.getCreatedAt().format(FORMATTER));
        dealsConfig.set(path + ".price", deal.getPrice());
        dealsConfig.set(path + ".status", deal.getStatus().name());

        if (deal.getModeratorId() != null) {
            dealsConfig.set(path + ".moderatorId", deal.getModeratorId().toString());
            dealsConfig.set(path + ".moderatorName", deal.getModeratorName());
            if (deal.getAcceptedAt() != null) {
                dealsConfig.set(path + ".acceptedAt", deal.getAcceptedAt().format(FORMATTER));
            }
        }

        save();
    }

    @Override
    public Optional<Deal> getDeal(UUID dealId) {
        String path = "deals." + dealId.toString();
        if (!dealsConfig.contains(path)) {
            return Optional.empty();
        }
        return Optional.of(loadDealFromSection(dealId, dealsConfig.getConfigurationSection(path)));
    }

    @Override
    public List<Deal> getActiveDeals() {
        List<Deal> deals = new ArrayList<>();
        ConfigurationSection section = dealsConfig.getConfigurationSection("deals");
        if (section == null) return deals;

        for (String key : section.getKeys(false)) {
            ConfigurationSection dealSection = section.getConfigurationSection(key);
            if (dealSection != null) {
                Deal deal = loadDealFromSection(UUID.fromString(key), dealSection);
                if (deal.getStatus().isActive()) {
                    deals.add(deal);
                }
            }
        }
        return deals;
    }

    @Override
    public List<Deal> getDealsByPlayer(UUID playerId) {
        List<Deal> deals = new ArrayList<>();
        ConfigurationSection section = dealsConfig.getConfigurationSection("deals");
        if (section == null) return deals;

        for (String key : section.getKeys(false)) {
            ConfigurationSection dealSection = section.getConfigurationSection(key);
            if (dealSection != null && playerId.toString().equals(dealSection.getString("playerId"))) {
                deals.add(loadDealFromSection(UUID.fromString(key), dealSection));
            }
        }
        return deals;
    }

    @Override
    public List<Deal> getDealsByModerator(UUID moderatorId) {
        List<Deal> deals = new ArrayList<>();
        ConfigurationSection section = dealsConfig.getConfigurationSection("deals");
        if (section == null) return deals;

        for (String key : section.getKeys(false)) {
            ConfigurationSection dealSection = section.getConfigurationSection(key);
            if (dealSection != null && moderatorId.toString().equals(dealSection.getString("moderatorId"))) {
                deals.add(loadDealFromSection(UUID.fromString(key), dealSection));
            }
        }
        return deals;
    }

    @Override
    public void removeDeal(UUID dealId) {
        dealsConfig.set("deals." + dealId.toString(), null);
        save();
    }

    @Override
    public DealStats getStats(UUID moderatorId) {
        String path = "stats." + moderatorId.toString();
        if (!statsConfig.contains(path)) {
            return null;
        }
        return buildStats(
                moderatorId,
                statsConfig.getInt(path + ".success", 0),
                statsConfig.getInt(path + ".cancelled", 0),
                statsConfig.getDouble(path + ".earned", 0)
        );
    }

    @Override
    public void saveStats(DealStats stats) {
        String path = "stats." + stats.getModeratorId().toString();
        statsConfig.set(path + ".success", stats.getSuccessCount());
        statsConfig.set(path + ".cancelled", stats.getCancelledCount());
        statsConfig.set(path + ".earned", stats.getTotalEarned());
        save();
    }

    private Deal loadDealFromSection(UUID id, ConfigurationSection section) {
        return buildDeal(
                id,
                UUID.fromString(section.getString("playerId")),
                section.getString("playerName"),
                section.getString("description"),
                section.getString("createdAt"),
                section.getDouble("price"),
                section.getString("status"),
                section.getString("moderatorId"),
                section.getString("moderatorName"),
                section.getString("acceptedAt")
        );
    }
}