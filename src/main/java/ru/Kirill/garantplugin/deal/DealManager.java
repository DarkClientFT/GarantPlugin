package ru.Kirill.garantplugin.deal;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.api.GarantApi;
import ru.Kirill.garantplugin.config.Messages;
import ru.Kirill.garantplugin.model.PlayerState;
import ru.Kirill.garantplugin.runnable.DealTimeoutTask;
import ru.Kirill.garantplugin.runnable.ModeratorTimeoutTask;
import ru.Kirill.garantplugin.util.ColorUtil;
import ru.Kirill.garantplugin.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class DealManager implements GarantApi {

    private final GarantPlugin plugin;
    private final Map<UUID, Deal> activeDeals;
    private final Map<UUID, Deal> playerDeals;
    private final Map<UUID, Deal> moderatorDeals;
    private final Map<UUID, PlayerState> playerStates;
    private final Map<UUID, String> pendingDescriptions;
    private final Map<UUID, BukkitTask> timeoutTasks;
    private final Map<UUID, BukkitTask> moderatorTimeoutTasks;

    public DealManager(GarantPlugin plugin) {
        this.plugin = plugin;
        this.activeDeals = new ConcurrentHashMap<>();
        this.playerDeals = new ConcurrentHashMap<>();
        this.moderatorDeals = new ConcurrentHashMap<>();
        this.playerStates = new ConcurrentHashMap<>();
        this.pendingDescriptions = new ConcurrentHashMap<>();
        this.timeoutTasks = new ConcurrentHashMap<>();
        this.moderatorTimeoutTasks = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<Deal> createDeal(UUID playerId, String description) {
        if (hasActiveDeal(playerId)) {
            return Optional.empty();
        }

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return Optional.empty();
        }

        double price = plugin.getConfigManager().getPrice();
        if (!plugin.getEconomyManager().withdraw(player, price)) {
            return Optional.empty();
        }

        Deal deal = Deal.create(playerId, player.getName(), description, price);
        activeDeals.put(deal.getId(), deal);
        playerDeals.put(playerId, deal);

        plugin.getBossBarManager().showSearchBar(player, plugin.getConfigManager().getSearchTime());
        startTimeoutTask(deal);

        notifyModerators(deal);

        SoundUtil.playSound(player, "deal-created");

        return Optional.of(deal);
    }

    @Override
    public boolean acceptDeal(UUID moderatorId, UUID dealId) {
        Deal deal = activeDeals.get(dealId);
        if (deal == null || deal.getStatus() != DealStatus.WAITING) {
            return false;
        }

        if (deal.isOwnedBy(moderatorId)) {
            return false;
        }

        if (isModerating(moderatorId)) {
            return false;
        }

        Player moderator = Bukkit.getPlayer(moderatorId);
        if (moderator == null) {
            return false;
        }

        cancelTimeoutTask(deal.getId());

        deal.accept(moderatorId, moderator.getName());
        moderatorDeals.put(moderatorId, deal);

        Player player = Bukkit.getPlayer(deal.getPlayerId());
        if (player != null) {
            plugin.getBossBarManager().remove(player);
            plugin.getBossBarManager().showDealBar(player);
            Messages messages = plugin.getConfigManager().getMessages();
            player.sendMessage(messages.get("moderator-accepted",
                    "%moderator%", moderator.getName(),
                    "%time%", deal.getFormattedAcceptedAt()));
            SoundUtil.playSound(player, "deal-accepted-player");
        }

        plugin.getBossBarManager().showModeratorBar(moderator);
        startModeratorTimeoutTask(deal);
        SoundUtil.playSound(moderator, "deal-accepted-moderator");

        return true;
    }

    @Override
    public boolean completeDeal(UUID dealId) {
        Deal deal = activeDeals.get(dealId);
        if (deal == null || deal.getStatus() != DealStatus.IN_PROGRESS) {
            return false;
        }

        deal.complete();

        cancelModeratorTimeoutTask(deal.getId());

        plugin.getStatsManager().addSuccess(deal.getModeratorId(), deal.getPrice());

        Player player = Bukkit.getPlayer(deal.getPlayerId());
        if (player != null) {
            plugin.getBossBarManager().remove(player);

            Messages messages = plugin.getConfigManager().getMessages();
            player.sendMessage(messages.get("deal-completed-player",
                    "%moderator%", deal.getModeratorName(),
                    "%price%", plugin.getEconomyManager().format(deal.getPrice())));
            SoundUtil.playSound(player, "deal-completed");
        }

        Player moderator = Bukkit.getPlayer(deal.getModeratorId());
        if (moderator != null) {
            plugin.getBossBarManager().remove(moderator);
            moderator.sendMessage(plugin.getConfigManager().getMessages().get("deal-completed-moderator",
                    "%player%", deal.getPlayerName()));
            SoundUtil.playSound(moderator, "deal-completed");
        }

        cleanupDeal(deal);
        plugin.getStorage().saveDeal(deal);

        return true;
    }

    @Override
    public boolean cancelDeal(UUID dealId, String reason) {
        Deal deal = activeDeals.get(dealId);
        if (deal == null || !deal.getStatus().isActive()) {
            return false;
        }

        boolean wasWaiting = deal.getStatus() == DealStatus.WAITING;
        deal.cancel();
        if (wasWaiting) {
            refundPlayer(deal);
        }

        Player player = Bukkit.getPlayer(deal.getPlayerId());
        if (player != null) {
            plugin.getBossBarManager().remove(player);
            if (deal.getModeratorName() != null) {
                player.sendMessage(plugin.getConfigManager().getMessages().get("deal-cancelled-by-moder",
                        "%moderator%", deal.getModeratorName(),
                        "%messages_rejected_sdelka%", reason != null ? reason : "Не указана"));
            } else if (wasWaiting && reason != null && reason.equals("Отменено игроком")) {
                player.sendMessage(plugin.getConfigManager().getMessages().get("deal-cancelled-by-player"));
            }
            SoundUtil.playSound(player, "deal-cancelled");
        }

        if (deal.getModeratorId() != null) {
            Player moderator = Bukkit.getPlayer(deal.getModeratorId());
            if (moderator != null) {
                plugin.getBossBarManager().remove(moderator);
                moderator.sendMessage(plugin.getConfigManager().getMessages().get("deal-cancelled-moderator",
                        "%player%", deal.getPlayerName(),
                        "%reason%", reason != null ? reason : "Не указана"));
                SoundUtil.playSound(moderator, "deal-cancelled");
            }
            plugin.getStatsManager().addCancelled(deal.getModeratorId());
        }

        cleanupDeal(deal);
        plugin.getStorage().saveDeal(deal);

        return true;
    }

    public void handleTimeout(UUID dealId) {
        Deal deal = activeDeals.get(dealId);
        if (deal == null || deal.getStatus() != DealStatus.WAITING) {
            return;
        }

        deal.timeout();
        refundPlayer(deal);

        Player player = Bukkit.getPlayer(deal.getPlayerId());
        if (player != null) {
            plugin.getBossBarManager().remove(player);
            player.sendMessage(plugin.getConfigManager().getMessages().get("timeout"));
            SoundUtil.playSound(player, "deal-timeout");
        }

        cleanupDeal(deal);
    }

    public void handleModeratorTimeout(UUID dealId) {
        Deal deal = activeDeals.get(dealId);
        if (deal == null || deal.getStatus() != DealStatus.IN_PROGRESS) {
            return;
        }

        deal.timeout();
        refundPlayer(deal);

        Player player = Bukkit.getPlayer(deal.getPlayerId());
        if (player != null) {
            plugin.getBossBarManager().remove(player);
            player.sendMessage(plugin.getConfigManager().getMessages().get("moderator-timeout"));
            SoundUtil.playSound(player, "deal-timeout");
        }

        if (deal.getModeratorId() != null) {
            Player moderator = Bukkit.getPlayer(deal.getModeratorId());
            if (moderator != null) {
                plugin.getBossBarManager().remove(moderator);
                moderator.sendMessage(plugin.getConfigManager().getMessages().get("moderator-timeout-moder"));
                SoundUtil.playSound(moderator, "deal-timeout");
            }
        }

        cleanupDeal(deal);
    }

    private void refundPlayer(Deal deal) {
        Player player = Bukkit.getPlayer(deal.getPlayerId());
        if (player != null && player.isOnline()) {
            plugin.getEconomyManager().deposit(player, deal.getPrice());
        }
    }

    private void startTimeoutTask(Deal deal) {
        int searchTime = plugin.getConfigManager().getSearchTime();
        BukkitTask task = new DealTimeoutTask(plugin, deal.getId(), searchTime)
                .runTaskTimer(plugin, 20L, 20L);
        timeoutTasks.put(deal.getId(), task);
    }

    private void cancelTimeoutTask(UUID dealId) {
        BukkitTask task = timeoutTasks.remove(dealId);
        if (task != null) {
            task.cancel();
        }
    }

    private void startModeratorTimeoutTask(Deal deal) {
        int visitTime = plugin.getConfigManager().getVisitTime();
        BukkitTask task = new ModeratorTimeoutTask(plugin, deal.getId(), visitTime)
                .runTaskTimer(plugin, 20L, 20L);
        moderatorTimeoutTasks.put(deal.getId(), task);
    }

    private void cancelModeratorTimeoutTask(UUID dealId) {
        BukkitTask task = moderatorTimeoutTasks.remove(dealId);
        if (task != null) {
            task.cancel();
        }
    }

    private void cleanupDeal(Deal deal) {
        activeDeals.remove(deal.getId());
        playerDeals.remove(deal.getPlayerId());
        if (deal.getModeratorId() != null) {
            moderatorDeals.remove(deal.getModeratorId());
        }
        cancelTimeoutTask(deal.getId());
        cancelModeratorTimeoutTask(deal.getId());
    }

    private void notifyModerators(Deal deal) {
        Messages messages = plugin.getConfigManager().getMessages();
        List<String> moderMsgList = messages.getList("moder-msg");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("unigarant.moderator")) {
                for (String line : moderMsgList) {
                    player.sendMessage(ColorUtil.colorize(line.replace("%playername%", deal.getPlayerName())));
                }

                SoundUtil.playSound(player, "new-deal-notify");
            }
        }
    }

    @Override
    public Optional<Deal> getDealById(UUID dealId) {
        return Optional.ofNullable(activeDeals.get(dealId));
    }

    @Override
    public Optional<Deal> getActiveDealByPlayer(UUID playerId) {
        return Optional.ofNullable(playerDeals.get(playerId));
    }

    @Override
    public Optional<Deal> getActiveDealByModerator(UUID moderatorId) {
        return Optional.ofNullable(moderatorDeals.get(moderatorId));
    }

    @Override
    public List<Deal> getAllActiveDeals() {
        return new ArrayList<>(activeDeals.values());
    }

    @Override
    public List<Deal> getDealsByStatus(DealStatus status) {
        return activeDeals.values().stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasActiveDeal(UUID playerId) {
        return playerDeals.containsKey(playerId);
    }

    @Override
    public boolean isModerating(UUID moderatorId) {
        return moderatorDeals.containsKey(moderatorId);
    }

    public void setPlayerState(UUID playerId, PlayerState state) {
        if (state == PlayerState.NONE) {
            playerStates.remove(playerId);
        } else {
            playerStates.put(playerId, state);
        }
    }

    public PlayerState getPlayerState(UUID playerId) {
        return playerStates.getOrDefault(playerId, PlayerState.NONE);
    }

    public void setPendingDescription(UUID playerId, String description) {
        pendingDescriptions.put(playerId, description);
    }

    public String getPendingDescription(UUID playerId) {
        return pendingDescriptions.remove(playerId);
    }

    public void handlePlayerQuit(UUID playerId) {
        Deal playerDeal = playerDeals.get(playerId);
        if (playerDeal != null) {
            if (playerDeal.getModeratorId() != null) {
                Player moderator = Bukkit.getPlayer(playerDeal.getModeratorId());
                if (moderator != null) {
                    moderator.sendMessage(plugin.getConfigManager().getMessages().get("player-left",
                            "%playername%", playerDeal.getPlayerName()));
                    plugin.getBossBarManager().remove(moderator);

                    SoundUtil.playSound(moderator, "player-left");
                }
            }

            playerDeal.cancel();
            cleanupDeal(playerDeal);
        }

        Deal moderatorDeal = moderatorDeals.get(playerId);
        if (moderatorDeal != null) {
            Player player = Bukkit.getPlayer(moderatorDeal.getPlayerId());
            if (player != null) {
                player.sendMessage(plugin.getConfigManager().getMessages().get("moderator-left"));
                plugin.getBossBarManager().remove(player);

                SoundUtil.playSound(player, "moderator-left");
            }
            refundPlayer(moderatorDeal);
            moderatorDeal.cancel();
            cleanupDeal(moderatorDeal);
        }

        playerStates.remove(playerId);
        pendingDescriptions.remove(playerId);
    }

    public void shutdown() {
        for (Deal deal : new ArrayList<>(activeDeals.values())) {
            if (deal.getStatus() == DealStatus.WAITING) {
                refundPlayer(deal);
            }
        }
        activeDeals.clear();
        playerDeals.clear();
        moderatorDeals.clear();
        for (BukkitTask task : timeoutTasks.values()) {
            task.cancel();
        }
        for (BukkitTask task : moderatorTimeoutTasks.values()) {
            task.cancel();
        }
    }

    public void setCancelReason(UUID moderatorId, String reason) {
        Deal deal = moderatorDeals.get(moderatorId);
        if (deal != null) {
            cancelDeal(deal.getId(), reason);
        }
        setPlayerState(moderatorId, PlayerState.NONE);
    }
}