package com.carrental.dao;

import com.carrental.model.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDAO {

    public List<Location> findAll() throws SQLException {
        String sql = "SELECT * FROM locations WHERE is_active = TRUE ORDER BY name";
        List<Location> locations = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                locations.add(new Location(
                        rs.getInt("location_id"), rs.getString("name"), rs.getString("address"),
                        rs.getString("city"), rs.getString("state"), rs.getString("zip_code"),
                        rs.getString("phone"), rs.getBoolean("is_active")));
            }
        }
        return locations;
    }
}
