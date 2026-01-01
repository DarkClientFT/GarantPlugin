package ru.Kirill.garantplugin.config;

import ru.Kirill.garantplugin.util.ColorUtil;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class Messages {

    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, List<String>> listMessages = new HashMap<>();
    private final Map<String, ConfigurationSection> sections = new HashMap<>();

    public Messages(ConfigurationSection section) {
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            if (section.isString(key)) {
                messages.put(key, section.getString(key));
            } else if (section.isList(key)) {
                listMessages.put(key, section.getStringList(key));
            } else if (section.isConfigurationSection(key)) {
                sections.put(key, section.getConfigurationSection(key));
            }
        }
    }

    public String get(String key) {
        return ColorUtil.colorize(messages.getOrDefault(key, "сообщение не нашлось: " + key));
    }

    public String get(String key, Object... replacements) {
        String message = get(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
            }
        }
        return message;
    }

    public List<String> getList(String key) {
        List<String> result = listMessages.get(key);
        if (result == null) {
            return new ArrayList<>();
        }
        return result;
    }

    public ConfigurationSection getSection(String key) {
        return sections.get(key);
    }

    public String getBossBarText(String type) {
        ConfigurationSection bossbar = sections.get("bossbar");
        if (bossbar != null) {
            ConfigurationSection typeSection = bossbar.getConfigurationSection(type);
            if (typeSection != null) {
                return ColorUtil.colorize(typeSection.getString("text", ""));
            }
        }
        return "";
    }

    public String getBossBarColor(String type) {
        ConfigurationSection bossbar = sections.get("bossbar");
        if (bossbar != null) {
            ConfigurationSection typeSection = bossbar.getConfigurationSection(type);
            if (typeSection != null) {
                return typeSection.getString("color", "WHITE");
            }
        }
        return "WHITE";
    }

    public String getBossBarStyle(String type) {
        ConfigurationSection bossbar = sections.get("bossbar");
        if (bossbar != null) {
            ConfigurationSection typeSection = bossbar.getConfigurationSection(type);
            if (typeSection != null) {
                return typeSection.getString("style", "SOLID");
            }
        }
        return "SOLID";
    }
}