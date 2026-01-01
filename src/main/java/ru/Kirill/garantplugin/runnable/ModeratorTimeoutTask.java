package ru.Kirill.garantplugin.runnable;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.deal.DealStatus;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.UUID;

public class ModeratorTimeoutTask extends BukkitRunnable {

    private final GarantPlugin plugin;
    private final UUID dealId;
    private int remainingSeconds;

    public ModeratorTimeoutTask(GarantPlugin plugin, UUID dealId, int seconds) {
        this.plugin = plugin;
        this.dealId = dealId;
        this.remainingSeconds = seconds;
    }

    @Override
    public void run() {
        Optional<Deal> dealOpt = plugin.getDealManager().getDealById(dealId);
        if (!dealOpt.isPresent() || dealOpt.get().getStatus() != DealStatus.IN_PROGRESS) {
            cancel();
            return;
        }

        remainingSeconds--;

        if (remainingSeconds <= 0) {
            plugin.getDealManager().handleModeratorTimeout(dealId);
            cancel();
        }
    }
}