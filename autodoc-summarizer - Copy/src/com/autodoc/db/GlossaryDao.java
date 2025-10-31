package com.autodoc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GlossaryDao {

    /**
     * Finds the definition for a given term from the database.
     * @param term The word to look for.
     * @return The definition string, or null if not found.
     */
    public String findDefinitionByTerm(String term) {
        String sql = "SELECT definition FROM glossary WHERE term = ?";
        
        // CORRECTED: The method is getConnection(), not connect().
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, term.toLowerCase());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("definition");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database query error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

