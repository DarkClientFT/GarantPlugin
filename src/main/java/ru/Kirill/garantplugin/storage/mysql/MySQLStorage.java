package ru.Kirill.garantplugin.storage.mysql;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.storage.sql.SQLStorage;
import com.zaxxer.hikari.HikariConfig;

public class MySQLStorage extends SQLStorage {

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final boolean useSSL;

    public MySQLStorage(GarantPlugin plugin, String host, int port, String database,
                        String username, String password, boolean useSSL) {
        super(plugin);
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
    }

    @Override
    protected HikariConfig createHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=" + useSSL + "&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
        config.setUsername(username);
        config.setPassword(password);
        return config;
    }

    @Override
    protected void createTables() {
        String dealsTable = "CREATE TABLE IF NOT EXISTS deals (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "player_id VARCHAR(36) NOT NULL, " +
                "player_name VARCHAR(64) NOT NULL, " +
                "description TEXT NOT NULL, " +
                "created_at VARCHAR(64) NOT NULL, " +
                "price DOUBLE NOT NULL, " +
                "status VARCHAR(32) NOT NULL, " +
                "moderator_id VARCHAR(36), " +
                "moderator_name VARCHAR(64), " +
                "accepted_at VARCHAR(64), " +
                "INDEX idx_player_id (player_id), " +
                "INDEX idx_moderator_id (moderator_id), " +
                "INDEX idx_status (status)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        String statsTable = "CREATE TABLE IF NOT EXISTS stats (" +
                "moderator_id VARCHAR(36) PRIMARY KEY, " +
                "success_count INT DEFAULT 0, " +
                "cancelled_count INT DEFAULT 0, " +
                "total_earned DOUBLE DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(dealsTable);
            stmt.executeUpdate(statsTable);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}