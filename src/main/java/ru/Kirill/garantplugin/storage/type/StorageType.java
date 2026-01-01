package ru.Kirill.garantplugin.storage.type;

public enum StorageType {
    YAML,
    MYSQL,
    SQLITE;

    public static StorageType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return YAML;
        }
    }
}