package ru.Kirill.garantplugin.storage.sql;

import ru.Kirill.garantplugin.GarantPlugin;
import ru.Kirill.garantplugin.deal.Deal;
import ru.Kirill.garantplugin.model.DealStats;
import ru.Kirill.garantplugin.storage.AbstractStorage;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class SQLStorage extends AbstractStorage {

    protected HikariDataSource dataSource;

    public SQLStorage(GarantPlugin plugin) {
        super(plugin);
    }

    protected abstract HikariConfig createHikariConfig();

    @Override
    public void init() {
        HikariConfig config = createHikariConfig();
        config.setPoolName("unigarant-Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(600000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        this.dataSource = new HikariDataSource(config);
        createTables();
    }

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public void load() {
    }

    @Override
    public void save() {
    }

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
                "accepted_at VARCHAR(64)" +
                ")";

        String statsTable = "CREATE TABLE IF NOT EXISTS stats (" +
                "moderator_id VARCHAR(36) PRIMARY KEY, " +
                "success_count INT DEFAULT 0, " +
                "cancelled_count INT DEFAULT 0, " +
                "total_earned DOUBLE DEFAULT 0" +
                ")";

        String indexPlayer = "CREATE INDEX IF NOT EXISTS idx_deals_player ON deals(player_id)";
        String indexModerator = "CREATE INDEX IF NOT EXISTS idx_deals_moderator ON deals(moderator_id)";
        String indexStatus = "CREATE INDEX IF NOT EXISTS idx_deals_status ON deals(status)";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(dealsTable);
            stmt.executeUpdate(statsTable);
            stmt.executeUpdate(indexPlayer);
            stmt.executeUpdate(indexModerator);
            stmt.executeUpdate(indexStatus);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDeal(Deal deal) {
        String sql = "REPLACE INTO deals (id, player_id, player_name, description, created_at, " +
                "price, status, moderator_id, moderator_name, accepted_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, deal.getId().toString());
            stmt.setString(2, deal.getPlayerId().toString());
            stmt.setString(3, deal.getPlayerName());
            stmt.setString(4, deal.getDescription());
            stmt.setString(5, deal.getCreatedAt().format(FORMATTER));
            stmt.setDouble(6, deal.getPrice());
            stmt.setString(7, deal.getStatus().name());
            stmt.setString(8, deal.getModeratorId() != null ? deal.getModeratorId().toString() : null);
            stmt.setString(9, deal.getModeratorName());
            stmt.setString(10, deal.getAcceptedAt() != null ? deal.getAcceptedAt().format(FORMATTER) : null);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Deal> getDeal(UUID dealId) {
        String sql = "SELECT * FROM deals WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dealId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(dealFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Deal> getActiveDeals() {
        String sql = "SELECT * FROM deals WHERE status IN ('WAITING', 'IN_PROGRESS')";
        List<Deal> deals = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                deals.add(dealFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return deals;
    }

    @Override
    public List<Deal> getDealsByPlayer(UUID playerId) {
        String sql = "SELECT * FROM deals WHERE player_id = ?";
        List<Deal> deals = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deals.add(dealFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return deals;
    }

    @Override
    public List<Deal> getDealsByModerator(UUID moderatorId) {
        String sql = "SELECT * FROM deals WHERE moderator_id = ?";
        List<Deal> deals = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, moderatorId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deals.add(dealFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return deals;
    }

    @Override
    public void removeDeal(UUID dealId) {
        String sql = "DELETE FROM deals WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dealId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DealStats getStats(UUID moderatorId) {
        String sql = "SELECT * FROM stats WHERE moderator_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, moderatorId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return buildStats(
                            moderatorId,
                            rs.getInt("success_count"),
                            rs.getInt("cancelled_count"),
                            rs.getDouble("total_earned")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void saveStats(DealStats stats) {
        String sql = "REPLACE INTO stats (moderator_id, success_count, cancelled_count, total_earned) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, stats.getModeratorId().toString());
            stmt.setInt(2, stats.getSuccessCount());
            stmt.setInt(3, stats.getCancelledCount());
            stmt.setDouble(4, stats.getTotalEarned());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Deal dealFromResultSet(ResultSet rs) throws SQLException {
        return buildDeal(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("player_id")),
                rs.getString("player_name"),
                rs.getString("description"),
                rs.getString("created_at"),
                rs.getDouble("price"),
                rs.getString("status"),
                rs.getString("moderator_id"),
                rs.getString("moderator_name"),
                rs.getString("accepted_at")
        );
    }
}