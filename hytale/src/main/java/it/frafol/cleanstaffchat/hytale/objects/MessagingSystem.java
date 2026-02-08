package it.frafol.cleanstaffchat.hytale.objects;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.frafol.cleanstaffchat.hytale.enums.HytaleConfig;

import java.sql.*;
import java.util.function.BiConsumer;

public class MessagingSystem {

    private final HikariDataSource dataSource;
    private final String serverID;
    private long lastMessageId = -1;

    public MessagingSystem(String host, int port, String db, String user, String pass, String serverID) {
        this.serverID = serverID;

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(5);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);

        setupDatabase();
        this.lastMessageId = fetchMaxId();
    }

    private void setupDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS hytale_multichat (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "sender_server VARCHAR(50) NOT NULL," +
                "chat_channel VARCHAR(20) NOT NULL," + // STAFF, ADMIN, DONOR
                "content TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "INDEX (id, sender_server)" +
                ") ENGINE=InnoDB;";
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendToChannel(String channel, String message) {
        if (Boolean.FALSE.equals(HytaleConfig.MYSQL_ENABLED.get(Boolean.class))) return;
        String sql = "INSERT INTO hytale_multichat (sender_server, chat_channel, content) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, this.serverID);
            ps.setString(2, channel.toUpperCase());
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void pollMessages(BiConsumer<String, String> action) {
        String sql = "SELECT id, chat_channel, content FROM hytale_multichat WHERE id > ? AND sender_server != ? ORDER BY id ASC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, lastMessageId);
            ps.setString(2, this.serverID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lastMessageId = rs.getLong("id");
                    String channel = rs.getString("chat_channel");
                    String content = rs.getString("content");

                    action.accept(channel, content);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private long fetchMaxId() {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(id) FROM hytale_multichat")) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {}
        return 0;
    }
}