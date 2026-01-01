package ru.Kirill.garantplugin.api;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.deal.DealStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GarantApi {
    static GarantApi getInstance() {
        return GarantPlugin.getInstance().getDealManager();
    }

    Optional<Deal> createDeal(UUID playerId, String description);

    boolean acceptDeal(UUID moderatorId, UUID dealId);

    boolean completeDeal(UUID dealId);

    boolean cancelDeal(UUID dealId, String reason);

    Optional<Deal> getDealById(UUID dealId);

    Optional<Deal> getActiveDealByPlayer(UUID playerId);

    Optional<Deal> getActiveDealByModerator(UUID moderatorId);

    List<Deal> getAllActiveDeals();

    List<Deal> getDealsByStatus(DealStatus status);

    boolean hasActiveDeal(UUID playerId);

    boolean isModerating(UUID moderatorId);

}