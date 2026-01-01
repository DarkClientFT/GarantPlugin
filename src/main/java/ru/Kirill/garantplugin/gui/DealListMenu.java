package ru.Kirill.garantplugin.gui;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.config.ConfigManager;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.deal.DealStatus;
import ru.Kirill.garantplugin.model.DealStats;
import ru.Kirill.garantplugin.model.PlayerState;
import ru.Kirill.garantplugin.util.SoundUtil;
import ru.Kirill.garantplugin.util.TextWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DealListMenu extends AbstractMenu {

    private static final int ITEMS_PER_PAGE = 45;
    private int currentPage;

    public DealListMenu(GarantPlugin plugin, Player player) {
        this(plugin, player, 0);
    }

    public DealListMenu(GarantPlugin plugin, Player player, int page) {
        super(plugin, player);
        this.currentPage = page;
    }

    @Override
    protected String getTitle() {
        return plugin.getConfigManager().getListTitle();
    }

    @Override
    protected int getSize() {
        return 54;
    }

    @Override
    protected void setupItems() {
        inventory.clear();
        clickActions.clear();

        setupDecoration();
        setupNavigationItems();
        setupStatsItem();
        setupActiveDealItem();
        setupDealItems();
    }

    private void setupDecoration() {
        ConfigManager config = plugin.getConfigManager();
        String material = config.getDecorationMaterial();
        List<Integer> slots = config.getDecorationSlots();

        ItemStack decorItem = itemBuilder()
                .material(material)
                .name("&r")
                .build();

        for (int slot : slots) {
            setItem(slot, decorItem);
        }
    }

    private void setupNavigationItems() {
        ConfigurationSection nextSection = plugin.getConfigManager().getItemSection("next_page");
        ConfigurationSection prevSection = plugin.getConfigManager().getItemSection("prev_page");

        if (nextSection != null) {
            ItemStack nextItem = itemBuilder()
                    .material(nextSection.getString("material", "NETHER_STAR"))
                    .name(nextSection.getString("display_name", "&bслед страница"))
                    .build();
            setItem(nextSection.getInt("slot", 53), nextItem, event -> {
                SoundUtil.playSound(player, "menu-click");
                List<Deal> deals = getWaitingDeals();
                int maxPage = (deals.size() - 1) / ITEMS_PER_PAGE;
                if (currentPage < maxPage) {
                    plugin.getMenuManager().openDealListMenu(player, currentPage + 1);
                }
            });
        }

        if (prevSection != null) {
            ItemStack prevItem = itemBuilder()
                    .material(prevSection.getString("material", "NETHER_STAR"))
                    .name(prevSection.getString("display_name", "&bпрошлая страцина"))
                    .build();
            setItem(prevSection.getInt("slot", 45), prevItem, event -> {
                SoundUtil.playSound(player, "menu-click");
                if (currentPage > 0) {
                    plugin.getMenuManager().openDealListMenu(player, currentPage - 1);
                }
            });
        }
    }

    private void setupStatsItem() {
        ConfigurationSection section = plugin.getConfigManager().getItemSection("stats_item");
        if (section == null) return;

        DealStats stats = plugin.getStatsManager().getStats(player.getUniqueId());

        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            lore.add(line
                    .replace("%success%", String.valueOf(stats.getSuccessCount()))
                    .replace("%canceled%", String.valueOf(stats.getCancelledCount()))
                    .replace("%earned%", String.valueOf((int) stats.getTotalEarned())));
        }

        ItemStack item = itemBuilder()
                .material(section.getString("material", "PLAYER_HEAD"))
                .name(section.getString("display_name", "&r"))
                .lore(lore)
                .skullOwner(player.getUniqueId())
                .build();

        setItem(section.getInt("slot", 49), item);
    }

    private void setupActiveDealItem() {
        Optional<Deal> activeDeal = plugin.getDealManager().getActiveDealByModerator(player.getUniqueId());

        if (activeDeal.isPresent()) {
            setupActiveActiveDealItem(activeDeal.get());
        } else {
            setupNoActiveDealItem();
        }
    }

    private void setupActiveActiveDealItem(Deal deal) {
        ConfigurationSection section = plugin.getConfigManager().getItemSection("active_deal");
        if (section == null) return;

        ConfigManager config = plugin.getConfigManager();
        TextWrapper wrapper = TextWrapper.fromConfig(
                config.getMaxLineLength(),
                config.getFirstLineFormatActive(),
                config.getContinuationLineFormatActive()
        );

        List<String> wrappedDesc = wrapper.wrap(deal.getDescription());

        List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("lore")) {
            if (line.contains("%wrapped_description%")) {
                lore.addAll(wrappedDesc);
            } else {
                lore.add(line
                        .replace("%player_name%", deal.getPlayerName())
                        .replace("%time_data%", deal.getFormattedCreatedAt())
                        .replace("%player_name_moder%", player.getName()));
            }
        }

        ItemStack item = itemBuilder()
                .material(section.getString("material", "ZOMBIE_HEAD"))
                .name(section.getString("display_name", "&fактивная сделка")
                        .replace("%player_name%", deal.getPlayerName()))
                .lore(lore)
                .skullOwner(deal.getPlayerId())
                .build();

        setItem(section.getInt("slot", 46), item, event -> {
            SoundUtil.playSound(player, "menu-click");

            if (event.getClick() == ClickType.RIGHT) {
                plugin.getDealManager().setPlayerState(player.getUniqueId(), PlayerState.ENTERING_CANCEL_REASON);
                player.closeInventory();
                player.sendMessage(plugin.getConfigManager().getMessages().get("cancel-prompt"));
            } else if (event.getClick() == ClickType.LEFT) {
                plugin.getDealManager().completeDeal(deal.getId());
                player.closeInventory();
            }
        });
    }

    private void setupNoActiveDealItem() {
        ConfigurationSection section = plugin.getConfigManager().getItemSection("no_active_deal");
        if (section == null) return;

        ItemStack item = itemBuilder()
                .material(section.getString("material", "BARRIER"))
                .name(section.getString("display_name", "&cне активная сделка"))
                .lore(section.getStringList("lore"))
                .build();

        setItem(section.getInt("slot", 46), item);
    }

    private void setupDealItems() {
        List<Deal> deals = getWaitingDeals();
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, deals.size());

        ConfigurationSection section = plugin.getConfigManager().getItemSection("deal_item");
        if (section == null) return;

        ConfigManager config = plugin.getConfigManager();
        TextWrapper wrapper = TextWrapper.fromConfig(
                config.getMaxLineLength(),
                config.getFirstLineFormatDeal(),
                config.getContinuationLineFormatDeal()
        );

        int slot = 0;
        for (int i = startIndex; i < endIndex && slot < 45; i++) {
            Deal deal = deals.get(i);

            while (isReservedSlot(slot) && slot < 45) {
                slot++;
            }

            if (slot >= 45) break;

            List<String> wrappedDesc = wrapper.wrap(deal.getDescription());

            List<String> lore = new ArrayList<>();
            for (String line : section.getStringList("lore")) {
                if (line.contains("%wrapped_description%")) {
                    lore.addAll(wrappedDesc);
                } else {
                    lore.add(line
                            .replace("%player_name%", deal.getPlayerName())
                            .replace("%time_data%", deal.getFormattedCreatedAt()));
                }
            }

            ItemStack item = itemBuilder()
                    .material(section.getString("material", "ZOMBIE_HEAD"))
                    .name(section.getString("display_name", "&fтипо сделка")
                            .replace("%player_name%", deal.getPlayerName()))
                    .lore(lore)
                    .skullOwner(deal.getPlayerId())
                    .build();

            final int currentSlot = slot;
            final Deal currentDeal = deal;
            setItem(slot, item, event -> {
                SoundUtil.playSound(player, "menu-click");

                if (event.getClick() == ClickType.LEFT) {
                    if (currentDeal.isOwnedBy(player.getUniqueId())) {
                        player.sendMessage(plugin.getConfigManager().getMessages().get("cannot-accept-own"));
                        SoundUtil.playSound(player, "error");
                        return;
                    }

                    if (plugin.getDealManager().isModerating(player.getUniqueId())) {
                        SoundUtil.playSound(player, "error");
                        return;
                    }

                    boolean accepted = plugin.getDealManager().acceptDeal(player.getUniqueId(), currentDeal.getId());
                    if (accepted) {
                        player.sendMessage(plugin.getConfigManager().getMessages().get("deal-accepted",
                                "%player%", currentDeal.getPlayerName()));
                        refresh();
                    }
                }
            });

            slot++;
        }
    }

    private boolean isReservedSlot(int slot) {
        List<Integer> decorSlots = plugin.getConfigManager().getDecorationSlots();
        if (decorSlots.contains(slot)) return true;
        if (slot == 45 || slot == 46 || slot == 49 || slot == 53) return true;
        return slot >= 45;
    }

    private List<Deal> getWaitingDeals() {
        return plugin.getDealManager().getDealsByStatus(DealStatus.WAITING);
    }
}