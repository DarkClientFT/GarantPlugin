package ru.Kirill.garantplugin.gui;

import ru.Kirill.garantplugin.GarantPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager {

    private final GarantPlugin plugin;
    private final Map<UUID, AbstractMenu> openMenus;

    public MenuManager(GarantPlugin plugin) {
        this.plugin = plugin;
        this.openMenus = new HashMap<>();
    }

    public void openCreateDealMenu(Player player) {
        CreateDealMenu menu = new CreateDealMenu(plugin, player);
        menu.open();
        openMenus.put(player.getUniqueId(), menu);
    }

    public void openDealListMenu(Player player) {
        DealListMenu menu = new DealListMenu(plugin, player);
        menu.open();
        openMenus.put(player.getUniqueId(), menu);
    }

    public void openDealListMenu(Player player, int page) {
        DealListMenu menu = new DealListMenu(plugin, player, page);
        menu.open();
        openMenus.put(player.getUniqueId(), menu);
    }

    public AbstractMenu getOpenMenu(UUID playerId) {
        return openMenus.get(playerId);
    }

    public void removeMenu(UUID playerId) {
        openMenus.remove(playerId);
    }

    public boolean hasOpenMenu(UUID playerId) {
        return openMenus.containsKey(playerId);
    }
}