package com.fairshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

@SpringBootApplication
public class FairshareBackendApplication {

    public static void main(String[] args) {
        normalizeCloudDatabaseEnvironment();
        configureLocalFallbackIfNeeded();
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

    /**
     * Checks if a PostgreSQL server is reachable on the configured host/port.
     * If not reachable, automatically configures a local persistent H2 database in PostgreSQL mode
     * so that the application runs locally out of the box without requiring Docker.
     */
    public static void configureLocalFallbackIfNeeded() {
        if (System.getProperty("spring.datasource.url") != null ||
            System.getenv("SPRING_DATASOURCE_URL") != null ||
            System.getenv("DATABASE_URL") != null) {
            return;
        }

        String host = System.getenv("DB_HOST") != null && !System.getenv("DB_HOST").isBlank()
                ? System.getenv("DB_HOST")
                : "127.0.0.1";
        int port = 5433;
        try {
            if (System.getenv("DB_PORT") != null && !System.getenv("DB_PORT").isBlank()) {
                port = Integer.parseInt(System.getenv("DB_PORT"));
            }
        } catch (NumberFormatException ignored) {}

        boolean postgresAvailable = false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 600);
            postgresAvailable = true;
        } catch (Exception ignored) {
            postgresAvailable = false;
        }

        if (!postgresAvailable) {
            System.out.println("==================================================================");
            System.out.println(">>> FairShare: PostgreSQL not detected on " + host + ":" + port);
            System.out.println(">>> Using local persistent H2 database (PostgreSQL mode) for local run.");
            System.out.println(">>> Local database file: ./data/fairshare_db.mv.db");
            System.out.println("==================================================================");

            File dir = new File("./data");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            System.setProperty("spring.datasource.url", "jdbc:h2:file:./data/fairshare_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
            System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");
            System.setProperty("SPRING_DATASOURCE_DRIVER_CLASS_NAME", "org.h2.Driver");
            System.setProperty("spring.datasource.username", "sa");
            System.setProperty("spring.datasource.password", "");
            System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
            System.setProperty("SPRING_JPA_DATABASE_PLATFORM", "org.hibernate.dialect.H2Dialect");
        }
    }
}
