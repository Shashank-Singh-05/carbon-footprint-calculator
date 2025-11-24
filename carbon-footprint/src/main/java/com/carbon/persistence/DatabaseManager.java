package com.carbon.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:carboncalc.db";
    private Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection(URL);
        initTables();
    }

    private void initTables() throws SQLException {
        try (Statement s = conn.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS activities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "date TEXT," +
                    "category TEXT," +
                    "subtype TEXT," +
                    "value REAL," +
                    "unit TEXT," +
                    "notes TEXT" +
                    ")");
        }
    }

    public Connection getConn() { return conn; }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}
