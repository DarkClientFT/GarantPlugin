package ru.Kirill.garantplugin.storage.sqlite;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.storage.sql.SQLStorage;
import com.zaxxer.hikari.HikariConfig;

import java.io.File;
import java.io.IOException;

public class SQLiteStorage extends SQLStorage {

    private final File databaseFile;

    public SQLiteStorage(GarantPlugin plugin) {
        super(plugin);
        this.databaseFile = new File(plugin.getDataFolder(), "database.db");
    }

    @Override
    public void init() {
        plugin.getDataFolder().mkdirs();
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.init();
    }

    @Override
    protected HikariConfig createHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        config.setMaximumPoolSize(1);
        return config;
    }

    @Override
    protected void createTables() {
        String dealsTable = "CREATE TABLE IF NOT EXISTS deals (" +
                "id TEXT PRIMARY KEY, " +
                "player_id TEXT NOT NULL, " +
                "player_name TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "created_at TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "status TEXT NOT NULL, " +
                "moderator_id TEXT, " +
                "moderator_name TEXT, " +
                "accepted_at TEXT" +
                ")";

        String statsTable = "CREATE TABLE IF NOT EXISTS stats (" +
                "moderator_id TEXT PRIMARY KEY, " +
                "success_count INTEGER DEFAULT 0, " +
                "cancelled_count INTEGER DEFAULT 0, " +
                "total_earned REAL DEFAULT 0" +
                ")";

        String indexPlayer = "CREATE INDEX IF NOT EXISTS idx_deals_player ON deals(player_id)";
        String indexModerator = "CREATE INDEX IF NOT EXISTS idx_deals_moderator ON deals(moderator_id)";
        String indexStatus = "CREATE INDEX IF NOT EXISTS idx_deals_status ON deals(status)";

        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(dealsTable);
            stmt.executeUpdate(statsTable);
            stmt.executeUpdate(indexPlayer);
            stmt.executeUpdate(indexModerator);
            stmt.executeUpdate(indexStatus);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}