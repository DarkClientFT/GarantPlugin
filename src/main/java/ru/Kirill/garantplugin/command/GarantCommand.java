package ru.Kirill.garantplugin.command;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.Kirill.garantplugin.deal.DealStatus;
import ru.Kirill.garantplugin.model.DealStats;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GarantCommand implements CommandExecutor, TabCompleter {
    /// короче команды писал один гкодер @klever726
    private final GarantPlugin plugin;

    public GarantCommand(GarantPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return cmd_menu(sender);
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                return cmd_list(sender);
            case "reload":
                return cmd_reload(sender);
            case "help":
                return cmd_help(sender);
            case "stats":
                return cmd_stats(sender);
            case "cancel":
                return cmd_cancel(sender);
            default:
                return cmd_menu(sender);
        }
    }

    private boolean cmd_menu(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessages().get("only-player"));
            return true;
        }

        Player player = (Player) sender;
        plugin.getMenuManager().openCreateDealMenu(player);
        return true;
    }

    private boolean cmd_list(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessages().get("only-player"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("unigarant.moderator")) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("no-permission"));
            return true;
        }

        plugin.getMenuManager().openDealListMenu(player);
        return true;
    }

    private boolean cmd_reload(CommandSender sender) {
        if (!sender.hasPermission("unigarant.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessages().get("no-permission"));
            return true;
        }

        plugin.reload();
        sender.sendMessage(plugin.getConfigManager().getMessages().get("reload"));
        return true;
    }

    private boolean cmd_help(CommandSender sender) {
        List<String> helpMessages = plugin.getConfigManager().getMessages().getList("help");
        if (helpMessages.isEmpty()) {
            sendhelp(sender);
        } else {
            for (String line : helpMessages) {
                sender.sendMessage(ColorUtil.colorize(line));
            }
        }
        return true;
    }

    private boolean cmd_stats(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessages().get("only-player"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("unigarant.moderator")) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("no-permission"));
            return true;
        }

        DealStats stats = plugin.getStatsManager().getStats(player.getUniqueId());

        List<String> statsMessages = plugin.getConfigManager().getMessages().getList("stats-info");
        if (statsMessages.isEmpty()) {
            player.sendMessage(ColorUtil.colorize("&6▶ &fВаша статистика:"));
            player.sendMessage(ColorUtil.colorize("&7├ &fУспешных сделок: &a" + stats.getSuccessCount()));
            player.sendMessage(ColorUtil.colorize("&7├ &fОтменённых сделок: &c" + stats.getCancelledCount()));
            player.sendMessage(ColorUtil.colorize("&7└ &fЗаработано: &e" + (int) stats.getTotalEarned() + " монет"));
        } else {
            for (String line : statsMessages) {
                String formatted = line
                        .replace("%success%", String.valueOf(stats.getSuccessCount()))
                        .replace("%cancelled%", String.valueOf(stats.getCancelledCount()))
                        .replace("%earned%", String.valueOf((int) stats.getTotalEarned()));
                player.sendMessage(ColorUtil.colorize(formatted));
            }
        }

        return true;
    }

    private boolean cmd_cancel(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessages().get("only-player"));
            return true;
        }

        Player player = (Player) sender;

        java.util.Optional<Deal> dealOpt = plugin.getDealManager().getActiveDealByPlayer(player.getUniqueId());
        if (!dealOpt.isPresent()) {
            player.sendMessage(plugin.getConfigManager().getMessages().get("no-active-deal"));
            return true;
        }

        Deal deal = dealOpt.get();
        if (deal.getStatus() == DealStatus.WAITING) {
            plugin.getDealManager().cancelDeal(deal.getId(), "Отменено игроком");
            player.sendMessage(plugin.getConfigManager().getMessages().get("deal-cancelled-by-player"));
        } else {
            player.sendMessage(plugin.getConfigManager().getMessages().get("cannot-cancel-in-progress"));
        }

        return true;
    }

    private void sendhelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&e&lUnigarant &7- &fСистема гарантов"));
        sender.sendMessage(ColorUtil.colorize(""));
        sender.sendMessage(ColorUtil.colorize("&e/garant &7- &fОткрыть меню создания заявки"));
        sender.sendMessage(ColorUtil.colorize("&e/garant cancel &7- &fОтменить свою заявку"));
        if (sender.hasPermission("unigarant.moderator")) {
            sender.sendMessage(ColorUtil.colorize("&e/garant list &7- &fСписок активных заявок"));
            sender.sendMessage(ColorUtil.colorize("&e/garant stats &7- &fВаша статистика"));
        }
        if (sender.hasPermission("unigarant.admin")) {
            sender.sendMessage(ColorUtil.colorize("&e/garant reload &7- &fПерезагрузить конфиг"));
        }
        sender.sendMessage(ColorUtil.colorize(""));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            subCommands.add("help");
            subCommands.add("cancel");
            if (sender.hasPermission("unigarant.moderator")) {
                subCommands.add("list");
                subCommands.add("stats");
            }
            if (sender.hasPermission("unigarant.admin")) {
                subCommands.add("reload");
            }
            String input = args[0].toLowerCase();
            completions = subCommands.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }
        return completions;
    }
}