package ru.Kirill.garantplugin.listener;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.model.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public class ChatListener implements Listener {

    private final GarantPlugin plugin;

    public ChatListener(GarantPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerState state = plugin.getDealManager().getPlayerState(player.getUniqueId());

        if (state == PlayerState.NONE) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            switch (state) {
                case ENTERING_DESCRIPTION:
                    handleDescriptionInput(player, message);
                    break;
                case ENTERING_CANCEL_REASON:
                    handleCancelReasonInput(player, message);
                    break;
            }
        });
    }

    private void handleDescriptionInput(Player player, String description) {
        plugin.getDealManager().setPlayerState(player.getUniqueId(), PlayerState.NONE);

        if (plugin.getDealManager().hasActiveDeal(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("already-created"));
            return;
        }

        double price = plugin.getConfigManager().getPrice();
        if (!plugin.getEconomyManager().hasEnough(player, price)) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("no-money",
                    "%amount%", plugin.getEconomyManager().format(price)));
            return;
        }

        Optional<Deal> deal = plugin.getDealManager().createDeal(player.getUniqueId(), description);
        if (deal.isPresent()) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("wait"));
        }
    }

    private void handleCancelReasonInput(Player player, String reason) {
        plugin.getDealManager().setCancelReason(player.getUniqueId(), reason);
    }
}