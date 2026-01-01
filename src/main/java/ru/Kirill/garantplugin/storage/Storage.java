package ru.Kirill.garantplugin.storage;

import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.model.DealStats;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Storage {

    void init();

    void shutdown();

    void saveDeal(Deal deal);

    Optional<Deal> getDeal(UUID dealId);

    List<Deal> getActiveDeals();

    List<Deal> getDealsByPlayer(UUID playerId);

    List<Deal> getDealsByModerator(UUID moderatorId);

    void removeDeal(UUID dealId);

    DealStats getStats(UUID moderatorId);

    void saveStats(DealStats stats);

    void load();

    void save();
}