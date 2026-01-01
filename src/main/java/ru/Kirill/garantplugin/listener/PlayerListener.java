package ru.Kirill.garantplugin.listener;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.deal.DealStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class PlayerListener implements Listener {

    private final GarantPlugin plugin;

    public PlayerListener(GarantPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getDealManager().handlePlayerQuit(player.getUniqueId());
        plugin.getBossBarManager().remove(player);
        plugin.getMenuManager().removeMenu(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Optional<Deal> playerDeal = plugin.getDealManager().getActiveDealByPlayer(player.getUniqueId());
        if (playerDeal.isPresent()) {
            Deal deal = playerDeal.get();
            if (deal.getStatus() == DealStatus.WAITING) {
                plugin.getBossBarManager().showSearchBar(player, plugin.getConfigManager().getSearchTime());
            } else if (deal.getStatus() == DealStatus.IN_PROGRESS) {
                plugin.getBossBarManager().showDealBar(player);
            }
        }

        Optional<Deal> moderatorDeal = plugin.getDealManager().getActiveDealByModerator(player.getUniqueId());
        if (moderatorDeal.isPresent()) {
            Deal deal = moderatorDeal.get();
            if (deal.getStatus() == DealStatus.IN_PROGRESS) {
                plugin.getBossBarManager().showModeratorBar(player);
            }
        }
    }
}