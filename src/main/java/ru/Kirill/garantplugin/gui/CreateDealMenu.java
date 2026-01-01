package ru.Kirill.garantplugin.gui;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.config.ConfigManager;
import ru.Kirill.garantplugin.model.PlayerState;
import ru.Kirill.garantplugin.util.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreateDealMenu extends AbstractMenu {

    public CreateDealMenu(GarantPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    protected String getTitle() {
        return plugin.getConfigManager().getMenuName();
    }

    @Override
    protected int getSize() {
        return plugin.getConfigManager().getMenuSize();
    }

    @Override
    protected void setupItems() {
        ConfigManager config = plugin.getConfigManager();

        ItemStack rulesItem = itemBuilder()
                .material(config.getRulesMaterial())
                .name(config.getRulesName())
                .lore(config.getRulesLore())
                .build();
        setItem(config.getRulesPos(), rulesItem);

        ItemStack sendItem = itemBuilder()
                .material(config.getSendMaterial())
                .name(config.getSendName())
                .build();

        setItem(config.getSendPos(), sendItem, event -> {
            SoundUtil.playSound(player, "menu-click");

            if (player.hasPermission("unigarant.moderator")) {
                player.sendMessage(plugin.getConfigManager().getMessages().get("garant-cannot-create"));
                SoundUtil.playSound(player, "error");
                return;
            }

            if (plugin.getDealManager().hasActiveDeal(player.getUniqueId())) {
                player.sendMessage(plugin.getConfigManager().getMessages().get("already-created"));
                SoundUtil.playSound(player, "error");
                return;
            }

            double price = config.getPrice();
            if (!plugin.getEconomyManager().hasEnough(player, price)) {
                player.sendMessage(plugin.getConfigManager().getMessages().get("no-money",
                        "%amount%", plugin.getEconomyManager().format(price)));
                SoundUtil.playSound(player, "error");
                return;
            }

            plugin.getDealManager().setPlayerState(player.getUniqueId(), PlayerState.ENTERING_DESCRIPTION);
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getMessages().get("enter-description"));
        });
    }
}