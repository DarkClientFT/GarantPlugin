package ru.Kirill.garantplugin.storage;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.deal.DealStatus;
import ru.Kirill.garantplugin.model.DealStats;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class AbstractStorage implements Storage {

    protected static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    protected final GarantPlugin plugin;

    protected Deal buildDeal(UUID id, UUID playerId, String playerName, String description,
                             String createdAt, double price, String status,
                             String moderatorId, String moderatorName, String acceptedAt) {
        Deal.DealBuilder builder = Deal.builder()
                .id(id)
                .playerId(playerId)
                .playerName(playerName)
                .description(description)
                .createdAt(LocalDateTime.parse(createdAt, FORMATTER))
                .price(price)
                .status(DealStatus.valueOf(status));

        if (moderatorId != null && !moderatorId.isEmpty()) {
            builder.moderatorId(UUID.fromString(moderatorId))
                    .moderatorName(moderatorName);
            if (acceptedAt != null && !acceptedAt.isEmpty()) {
                builder.acceptedAt(LocalDateTime.parse(acceptedAt, FORMATTER));
            }
        }

        return builder.build();
    }

    protected DealStats buildStats(UUID moderatorId, int success, int cancelled, double earned) {
        return DealStats.builder()
                .moderatorId(moderatorId)
                .successCount(success)
                .cancelledCount(cancelled)
                .totalEarned(earned)
                .build();
    }
}