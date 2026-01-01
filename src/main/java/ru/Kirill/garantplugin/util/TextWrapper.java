package ru.Kirill.garantplugin.util;

import java.util.ArrayList;
import java.util.List;

public class TextWrapper {

    private final int maxLineLength;
    private final String firstLineFormat;
    private final String continuationLineFormat;

    public TextWrapper(int maxLineLength, String firstLineFormat, String continuationLineFormat) {
        this.maxLineLength = maxLineLength;
        this.firstLineFormat = firstLineFormat;
        this.continuationLineFormat = continuationLineFormat;
    }

    public List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        boolean isFirstLine = true;

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                if (currentLine.length() > 0) {
                    String format = isFirstLine ? firstLineFormat : continuationLineFormat;
                    String placeholder = isFirstLine ? "%messages_sdelki%" : "%messages_sdelki_new%";
                    lines.add(format.replace(placeholder, currentLine.toString().trim()));
                    isFirstLine = false;
                    currentLine = new StringBuilder();
                }
            }
            currentLine.append(word).append(" ");
        }

        if (currentLine.length() > 0) {
            String format = isFirstLine ? firstLineFormat : continuationLineFormat;
            String placeholder = isFirstLine ? "%messages_sdelki%" : "%messages_sdelki_new%";
            lines.add(format.replace(placeholder, currentLine.toString().trim()));
        }

        return lines;
    }

    public static TextWrapper fromConfig(int maxLength, String firstFormat, String continuationFormat) {
        return new TextWrapper(maxLength, firstFormat, continuationFormat);
    }
}