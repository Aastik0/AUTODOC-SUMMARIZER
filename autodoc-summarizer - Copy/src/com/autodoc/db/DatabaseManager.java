package com.autodoc.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:autodoc_glossary.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Step 1: Create the glossary table if it doesn't exist
            String createTableSql = "CREATE TABLE IF NOT EXISTS glossary (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                    "term TEXT NOT NULL UNIQUE," +
                                    "definition TEXT NOT NULL" +
                                    ");";
            stmt.execute(createTableSql);

            // Step 2: Load data from glossary.csv
            loadDataFromCSV(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    private static void loadDataFromCSV(Connection conn) {
        String csvFile = "glossary.csv";
        String sql = "INSERT OR IGNORE INTO glossary (term, definition) VALUES (?, ?);";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false); // Start transaction

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", 2); // Split only on the first comma
                if (values.length == 2) {
                    pstmt.setString(1, values[0].trim()); // Term
                    pstmt.setString(2, values[1].trim()); // Definition
                    pstmt.addBatch();
                }
            }
            
            pstmt.executeBatch(); // Execute all inserts at once
            conn.commit(); // Commit transaction

        } catch (IOException | SQLException e) {
            System.err.println("Error loading data from CSV: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error on rollback: " + ex.getMessage());
            }
        } finally {
            try {
                 if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.err.println("Error resetting auto-commit: " + ex.getMessage());
            }
        }
    }
}

