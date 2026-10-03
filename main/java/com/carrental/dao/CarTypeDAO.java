package com.carrental.dao;

import com.carrental.model.CarType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarTypeDAO {

    public List<CarType> findAll() throws SQLException {
        String sql = "SELECT * FROM car_types ORDER BY daily_rate";
        List<CarType> types = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                types.add(new CarType(
                        rs.getInt("car_type_id"), rs.getString("type_name"),
                        rs.getString("description"), rs.getBigDecimal("daily_rate")));
            }
        }
        return types;
    }
}
