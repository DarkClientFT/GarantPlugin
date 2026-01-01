package ru.Kirill.garantplugin.manager;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.model.DealStats;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {

    private final GarantPlugin plugin;
    private final Map<UUID, DealStats> statsCache;

    public StatsManager(GarantPlugin plugin) {
        this.plugin = plugin;
        this.statsCache = new ConcurrentHashMap<>();
    }

    public DealStats getStats(UUID moderatorId) {
        return statsCache.computeIfAbsent(moderatorId, id -> {
            DealStats stats = plugin.getStorage().getStats(id);
            if (stats == null) {
                stats = DealStats.builder()
                        .moderatorId(id)
                        .successCount(0)
                        .cancelledCount(0)
                        .totalEarned(0)
                        .build();
            }
            return stats;
        });
    }

    public void addSuccess(UUID moderatorId, double amount) {
        DealStats stats = getStats(moderatorId);
        stats.addSuccess(amount);
        saveStats(stats);
    }

    public void addCancelled(UUID moderatorId) {
        DealStats stats = getStats(moderatorId);
        stats.addCancelled();
        saveStats(stats);
    }

    private void saveStats(DealStats stats) {
        plugin.getStorage().saveStats(stats);
    }

    public void clearCache() {
        statsCache.clear();
    }
}