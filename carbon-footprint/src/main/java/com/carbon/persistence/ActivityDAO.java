package com.carbon.persistence;

import com.carbon.model.ActivityEntry;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {
    private final Connection conn;

    public ActivityDAO(Connection conn) {
        this.conn = conn;
    }

    public void insert(ActivityEntry e) throws SQLException {
        String sql = "INSERT INTO activities(date,category,subtype,value,unit,notes) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement p = conn.prepareStatement(sql)) {
            p.setString(1, e.getDate().toString());
            p.setString(2, e.getCategory());
            p.setString(3, e.getSubtype());
            p.setDouble(4, e.getValue());
            p.setString(5, e.getUnit());
            p.setString(6, e.getNotes());
            p.executeUpdate();
        }
    }

    public List<ActivityEntry> listAll() throws SQLException {
        List<ActivityEntry> out = new ArrayList<>();
        String q = "SELECT * FROM activities ORDER BY date";
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(q)) {
            while (rs.next()) {
                ActivityEntry a = new ActivityEntry(
                        rs.getLong("id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("category"),
                        rs.getString("subtype"),
                        rs.getDouble("value"),
                        rs.getString("unit"),
                        rs.getString("notes")
                );
                out.add(a);
            }
        }
        return out;
    }
}
