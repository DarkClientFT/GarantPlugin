package ru.Kirill.garantplugin.deal;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@Builder
public class Deal {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final UUID id;
    private final UUID playerId;
    private final String playerName;
    private final String description;
    private final LocalDateTime createdAt;
    private final double price;
    private UUID moderatorId;
    private String moderatorName;
    private LocalDateTime acceptedAt;
    private DealStatus status;

    public static Deal create(UUID playerId, String playerName, String description, double price) {
        return Deal.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .playerName(playerName)
                .description(description)
                .createdAt(LocalDateTime.now())
                .price(price)
                .status(DealStatus.WAITING)
                .build();
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(FORMATTER);
    }

    public String getFormattedAcceptedAt() {
        return acceptedAt != null ? acceptedAt.format(FORMATTER) : "N/A";
    }

    public void accept(UUID moderatorId, String moderatorName) {
        this.moderatorId = moderatorId;
        this.moderatorName = moderatorName;
        this.acceptedAt = LocalDateTime.now();
        this.status = DealStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = DealStatus.COMPLETED;
    }

    public void cancel() {
        this.status = DealStatus.CANCELLED;
    }

    public void timeout() {
        this.status = DealStatus.TIMEOUT;
    }

    public boolean isOwnedBy(UUID uuid) {
        return playerId.equals(uuid);
    }

    public boolean isModeratedBy(UUID uuid) {
        return moderatorId != null && moderatorId.equals(uuid);
    }
}