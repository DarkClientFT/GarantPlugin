package ru.Kirill.garantplugin.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {
    /// тоже не ебу что тут делать это тоже ИИ писал
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private TimeUtil() {
    }

    public static String formatDuration(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes > 0) {
            return String.format("%d мин. %d сек.", minutes, secs);
        }
        return String.format("%d сек.", secs);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMATTER);
    }

    public static long getSecondsBetween(LocalDateTime from, LocalDateTime to) {
        return Duration.between(from, to).getSeconds();
    }

    public static String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public static long secondsUntil(LocalDateTime target) {
        return Duration.between(LocalDateTime.now(), target).getSeconds();
    }
}