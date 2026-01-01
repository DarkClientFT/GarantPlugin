package ru.Kirill.garantplugin.config;

import ru.Kirill.garantplugin.GarantPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@Setter
@Getter
public class ConfigManager {

    private final GarantPlugin plugin;
    private String menuName;
    private int menuSize;
    private Material sendMaterial;
    private String sendName;
    private int sendPos;
    private double price;
    private Material rulesMaterial;
    private String rulesName;
    private List<String> rulesLore;
    private int rulesPos;
    private String listTitle;
    private boolean textWrappingEnabled;
    private int maxLineLength;
    private String firstLineFormatActive;
    private String continuationLineFormatActive;
    private String firstLineFormatDeal;
    private String continuationLineFormatDeal;
    private int searchTime;
    private int visitTime;
    private Messages messages;

    public ConfigManager(GarantPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection menuSection = config.getConfigurationSection("menu");
        if (menuSection != null) {
            menuName = menuSection.getString("name", "Найти Гаранта сделки");
            menuSize = menuSection.getInt("size", 27);
            sendMaterial = Material.valueOf(menuSection.getString("material-send", "LIME_WOOL"));
            sendName = menuSection.getString("name-send", "&#05FB00▶ Создать заявку");
            sendPos = menuSection.getInt("pos-send", 13);
            price = menuSection.getDouble("price", 125000);
            rulesMaterial = Material.valueOf(menuSection.getString("material-rules", "PAPER"));
            rulesName = menuSection.getString("name-rules", "&0 ");
            rulesLore = menuSection.getStringList("lore-rules");
            rulesPos = menuSection.getInt("pos-rule", 8);
        }

        ConfigurationSection listSection = config.getConfigurationSection("menu_list");
        if (listSection != null) {
            listTitle = listSection.getString("title", "Список заявок на сделку");
            ConfigurationSection wrapping = listSection.getConfigurationSection("text_wrapping");
            if (wrapping != null) {
                textWrappingEnabled = wrapping.getBoolean("enabled", true);
                maxLineLength = wrapping.getInt("max_line_length", 50);
                firstLineFormatActive = wrapping.getString("first_line_format_active");
                continuationLineFormatActive = wrapping.getString("continuation_line_format_active");
                firstLineFormatDeal = wrapping.getString("first_line_format_deal");
                continuationLineFormatDeal = wrapping.getString("continuation_line_format_deal");
            }
        }

        ConfigurationSection timeSection = config.getConfigurationSection("time");
        if (timeSection != null) {
            searchTime = timeSection.getInt("search-time", 600);
            visitTime = timeSection.getInt("visit-time", 600);
        }
        messages = new Messages(config.getConfigurationSection("messages"));
    }

    public String getDecorationMaterial() {
        return plugin.getConfig().getString("menu_list.items.decoration.material", "BLUE_STAINED_GLASS_PANE");
    }

    public List<Integer> getDecorationSlots() {
        return plugin.getConfig().getIntegerList("menu_list.items.decoration.slots");
    }

    public ConfigurationSection getItemSection(String path) {
        return plugin.getConfig().getConfigurationSection("menu_list.items." + path);
    }
}