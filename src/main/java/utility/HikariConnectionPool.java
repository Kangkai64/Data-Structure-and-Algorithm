package utility;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class HikariConnectionPool {
    private static HikariConnectionPool instance;
    private final HikariDataSource dataSource;

    private HikariConnectionPool() {
        HikariConfig config = new HikariConfig();

        // Database configuration
        config.setJdbcUrl("jdbc:mysql://localhost:3306/clinic_management_system");
        config.setUsername("root");
        config.setPassword("");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool configuration
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setLeakDetectionThreshold(60000); // 1 minute

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        this.dataSource = new HikariDataSource(config);
    }

    public static synchronized HikariConnectionPool getInstance() {
        if (instance == null) {
            instance = new HikariConnectionPool();
        }
        return instance;
    }

    /**
     * Get a connection from the pool
     * @return Connection object
     * @throws SQLException if connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Get pool statistics
     */
    public void printPoolStats() {
        System.out.println("=== HikariCP Pool Statistics ===");
        System.out.println("Active connections: " + dataSource.getHikariPoolMXBean().getActiveConnections());
        System.out.println("Idle connections: " + dataSource.getHikariPoolMXBean().getIdleConnections());
        System.out.println("Total connections: " + dataSource.getHikariPoolMXBean().getTotalConnections());
        System.out.println("Threads waiting: " + dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }

    /**
     * Shutdown the connection pool
     */
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}