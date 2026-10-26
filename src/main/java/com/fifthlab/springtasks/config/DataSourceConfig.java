package com.fifthlab.springtasks.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${spring.datasource.url:#{null}}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String username;

    @Value("${spring.datasource.password:#{null}}")
    private String password;

    @Value("${spring.datasource.driver-class-name:#{null}}")
    private String driverClassName;

    @Value("${DB_HOST:#{null}}")
    private String dbHost;

    @Value("${DB_PORT:5432}")
    private String dbPort;

    @Value("${DB_NAME:#{null}}")
    private String dbName;

    @Value("${DB_USERNAME:#{null}}")
    private String dbUsername;

    @Value("${DB_PASSWORD:#{null}}")
    private String dbPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // 1. Check Render / Cloud DATABASE_URL environment variable
        String rawDbUrl = databaseUrl != null && !databaseUrl.isBlank() ? databaseUrl : System.getenv("DATABASE_URL");
        if (rawDbUrl != null && !rawDbUrl.isBlank()) {
            try {
                log.info("Configuring DataSource from DATABASE_URL");
                URI uri = new URI(rawDbUrl.replace("postgres://", "postgresql://"));
                String userInfo = uri.getUserInfo();
                String dbUser = null;
                String dbPass = null;
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    dbUser = parts[0];
                    dbPass = parts[1];
                }

                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                String path = uri.getPath();

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                    jdbcUrl += "?" + uri.getQuery();
                } else {
                    jdbcUrl += "?sslmode=require";
                }

                config.setJdbcUrl(jdbcUrl);
                if (dbUser != null) config.setUsername(dbUser);
                if (dbPass != null) config.setPassword(dbPass);
                config.setDriverClassName("org.postgresql.Driver");
                config.setMaximumPoolSize(5);
                config.setMinimumIdle(1);
                config.setConnectionTimeout(20000);
                return new HikariDataSource(config);
            } catch (Exception e) {
                log.error("Failed to parse DATABASE_URL: {}, falling back to property config", rawDbUrl, e);
            }
        }

        // 2. Check spring.datasource.url or DB_HOST
        if (springDatasourceUrl != null && !springDatasourceUrl.isBlank()) {
            config.setJdbcUrl(springDatasourceUrl);
            if (username != null) config.setUsername(username);
            if (password != null) config.setPassword(password);
            if (driverClassName != null && !driverClassName.isBlank()) {
                config.setDriverClassName(driverClassName);
            }
            config.setMaximumPoolSize(5);
            return new HikariDataSource(config);
        }

        if (dbHost != null && !dbHost.isBlank() && dbName != null && !dbName.isBlank()) {
            String jdbcUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName + "?sslmode=require";
            config.setJdbcUrl(jdbcUrl);
            if (dbUsername != null) config.setUsername(dbUsername);
            if (dbPassword != null) config.setPassword(dbPassword);
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(5);
            return new HikariDataSource(config);
        }

        // 3. Fallback to in-memory H2 database
        log.info("No external database configured. Using in-memory H2 database.");
        config.setJdbcUrl("jdbc:h2:mem:taskdb;DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("password");
        config.setMaximumPoolSize(5);
        return new HikariDataSource(config);
    }
}
