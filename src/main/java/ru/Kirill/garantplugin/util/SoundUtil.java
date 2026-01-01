package ru.Kirill.garantplugin.util;

import ru.Kirill.garantplugin.GarantPlugin;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public final class SoundUtil {

    private SoundUtil() {
    }
    ///  хз такого на ХВ вроде нету, добавил для красоты
    public static void playSound(Player player, String soundKey) {
        if (player == null || !player.isOnline()) {
            return;
        }

        GarantPlugin plugin = GarantPlugin.getInstance();
        ConfigurationSection soundsSection = plugin.getConfig().getConfigurationSection("sounds");

        if (soundsSection == null || !soundsSection.getBoolean("enabled", true)) {
            return;
        }

        ConfigurationSection soundConfig = soundsSection.getConfigurationSection(soundKey);
        if (soundConfig == null) {
            return;
        }

        String soundName = soundConfig.getString("sound", "BLOCK_NOTE_BLOCK_PLING");
        float volume = (float) soundConfig.getDouble("volume", 1.0);
        float pitch = (float) soundConfig.getDouble("pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundName + " for key: " + soundKey);
        }
    }

    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        if (player == null || !player.isOnline()) {
            return;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}