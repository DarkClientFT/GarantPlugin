package ru.Kirill.garantplugin.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {
    ///// писал гпт, я не ебу все эти ваши паттерны для цветов
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:([A-Fa-f0-9]{6}):([A-Fa-f0-9]{6})>(.+?)</gradient>");

    private ColorUtil() {
    }

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        message = applyGradient(message);
        message = applyHex(message);
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    private static String applyHex(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            ChatColor color = ChatColor.of("#" + hex);
            matcher.appendReplacement(buffer, color.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String applyGradient(String message) {
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String text = matcher.group(3);
            String gradient = createGradient(text, startHex, endHex);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(gradient));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String createGradient(String text, String startHex, String endHex) {
        int[] startRgb = hexToRgb(startHex);
        int[] endRgb = hexToRgb(endHex);
        StringBuilder result = new StringBuilder();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            double ratio = length > 1 ? (double) i / (length - 1) : 0;
            int r = (int) (startRgb[0] + ratio * (endRgb[0] - startRgb[0]));
            int g = (int) (startRgb[1] + ratio * (endRgb[1] - startRgb[1]));
            int b = (int) (startRgb[2] + ratio * (endRgb[2] - startRgb[2]));
            ChatColor color = ChatColor.of(String.format("#%02x%02x%02x", r, g, b));
            result.append(color).append(text.charAt(i));
        }
        return result.toString();
    }

    private static int[] hexToRgb(String hex) {
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(colorize(message));
    }
}