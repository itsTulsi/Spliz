package com.fairshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;

@SpringBootApplication
public class FairshareBackendApplication {

    public static void main(String[] args) {
        normalizeCloudDatabaseEnvironment();
        SpringApplication.run(FairshareBackendApplication.class, args);
    }

    /**
     * Translates cloud standard URI connection strings (postgres://user:pass@host:port/db)
     * provided by Render, Railway, or Heroku into Spring Data JDBC format.
     */
    public static void normalizeCloudDatabaseEnvironment() {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        }

        if (dbUrl != null && (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://"))) {
            try {
                String parseable = dbUrl.startsWith("postgres://")
                        ? "http://" + dbUrl.substring("postgres://".length())
                        : "http://" + dbUrl.substring("postgresql://".length());

                URI uri = new URI(parseable);
                String username = null;
                String password = null;

                if (uri.getUserInfo() != null) {
                    String[] parts = uri.getUserInfo().split(":", 2);
                    username = parts[0];
                    if (parts.length > 1) {
                        password = parts[1];
                    }
                }

                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }

                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + path;
                if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                    jdbcUrl += "?" + uri.getQuery();
                } else {
                    jdbcUrl += "?sslmode=prefer";
                }

                System.setProperty("spring.datasource.url", jdbcUrl);
                if (username != null) {
                    System.setProperty("spring.datasource.username", username);
                }
                if (password != null) {
                    System.setProperty("spring.datasource.password", password);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
