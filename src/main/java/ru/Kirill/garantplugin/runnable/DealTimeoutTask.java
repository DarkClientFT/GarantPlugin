package ru.Kirill.garantplugin.runnable;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.deal.DealStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.UUID;

public class DealTimeoutTask extends BukkitRunnable {

    private final GarantPlugin plugin;
    private final UUID dealId;
    private int remainingSeconds;

    public DealTimeoutTask(GarantPlugin plugin, UUID dealId, int seconds) {
        this.plugin = plugin;
        this.dealId = dealId;
        this.remainingSeconds = seconds;
    }

    @Override
    public void run() {
        Optional<Deal> dealOpt = plugin.getDealManager().getDealById(dealId);
        if (!dealOpt.isPresent() || dealOpt.get().getStatus() != DealStatus.WAITING) {
            cancel();
            return;
        }

        Deal deal = dealOpt.get();
        remainingSeconds--;

        Player player = Bukkit.getPlayer(deal.getPlayerId());
        if (player != null && player.isOnline()) {
            plugin.getBossBarManager().showSearchBar(player, remainingSeconds);
        }

        if (remainingSeconds <= 0) {
            plugin.getDealManager().handleTimeout(dealId);
            cancel();
        }
    }
}