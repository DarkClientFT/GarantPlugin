package ru.Kirill.garantplugin.manager;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    private final GarantPlugin plugin;
    private final Map<UUID, BossBar> playerBossBars;

    public BossBarManager(GarantPlugin plugin) {
        this.plugin = plugin;
        this.playerBossBars = new ConcurrentHashMap<>();
    }

    public void showSearchBar(Player player, int remainingSeconds) {
        String text = plugin.getConfigManager().getMessages().getBossBarText("search")
                .replace("%time_search%", formatTime(remainingSeconds));
        String color = plugin.getConfigManager().getMessages().getBossBarColor("search");
        String style = plugin.getConfigManager().getMessages().getBossBarStyle("search");

        createOrUpdateBar(player, text, color, style, (double) remainingSeconds / plugin.getConfigManager().getSearchTime());
    }

    public void showDealBar(Player player) {
        String text = plugin.getConfigManager().getMessages().getBossBarText("deal");
        String color = plugin.getConfigManager().getMessages().getBossBarColor("deal");
        String style = plugin.getConfigManager().getMessages().getBossBarStyle("deal");

        createOrUpdateBar(player, text, color, style, 1.0);
    }

    public void showModeratorBar(Player player) {
        String text = plugin.getConfigManager().getMessages().getBossBarText("moderator");
        String color = plugin.getConfigManager().getMessages().getBossBarColor("moderator");
        String style = plugin.getConfigManager().getMessages().getBossBarStyle("moderator");

        createOrUpdateBar(player, text, color, style, 1.0);
    }

    private void createOrUpdateBar(Player player, String text, String color, String style, double progress) {
        BossBar bar = playerBossBars.get(player.getUniqueId());

        if (bar == null) {
            bar = Bukkit.createBossBar(
                    ColorUtil.colorize(text),
                    BarColor.valueOf(color),
                    BarStyle.valueOf(style)
            );
            bar.addPlayer(player);
            playerBossBars.put(player.getUniqueId(), bar);
        } else {
            bar.setTitle(ColorUtil.colorize(text));
            bar.setColor(BarColor.valueOf(color));
            bar.setStyle(BarStyle.valueOf(style));
        }

        bar.setProgress(Math.max(0, Math.min(1, progress)));
        bar.setVisible(true);
    }

    public void updateProgress(Player player, double progress) {
        BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar != null) {
            bar.setProgress(Math.max(0, Math.min(1, progress)));
        }
    }

    public void remove(Player player) {
        BossBar bar = playerBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removePlayer(player);
            bar.setVisible(false);
        }
    }

    public void removeAll() {
        for (Map.Entry<UUID, BossBar> entry : playerBossBars.entrySet()) {
            BossBar bar = entry.getValue();
            bar.removeAll();
            bar.setVisible(false);
        }
        playerBossBars.clear();
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }

    public boolean hasBar(UUID playerId) {
        return playerBossBars.containsKey(playerId);
    }
}