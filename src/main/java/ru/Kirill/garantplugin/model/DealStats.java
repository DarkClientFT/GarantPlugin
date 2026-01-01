package ru.Kirill.garantplugin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealStats {

    private UUID moderatorId;
    private int successCount;
    private int cancelledCount;
    private double totalEarned;

    public void addSuccess(double amount) {
        successCount++;
        totalEarned += amount;
    }

    public void addCancelled() {
        cancelledCount++;
    }
}