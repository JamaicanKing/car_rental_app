package com.carrental.dao;

import com.carrental.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleDAO {

    private static final String SELECT_BASE =
            "SELECT v.*, ct.type_name FROM vehicles v " +
            "JOIN car_types ct ON ct.car_type_id = v.car_type_id";

    /** All vehicles currently marked AVAILABLE, optionally narrowed by car type and/or location. */
    public List<Vehicle> findAvailable(Integer carTypeId, Integer locationId) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE v.status = 'AVAILABLE'");
        List<Object> params = new ArrayList<>();

        if (carTypeId != null) {
            sql.append(" AND v.car_type_id = ?");
            params.add(carTypeId);
        }
        if (locationId != null) {
            sql.append(" AND v.location_id = ?");
            params.add(locationId);
        }

        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(mapRow(rs));
                }
            }
        }
        return vehicles;
    }

    public Optional<Vehicle> findById(int vehicleId) throws SQLException {
        String sql = SELECT_BASE + " WHERE v.vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /**
     * True if no CONFIRMED/PENDING booking for this vehicle overlaps the requested date range.
     * Called by BookingService before confirming a new booking.
     */
    public boolean isAvailableForDates(int vehicleId, java.time.LocalDate start, java.time.LocalDate end)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE vehicle_id = ? AND status IN ('PENDING','CONFIRMED') " +
                "AND start_date < ? AND end_date > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            ps.setDate(2, Date.valueOf(end));
            ps.setDate(3, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0;
            }
        }
    }

    public void updateStatus(int vehicleId, Vehicle.Status status) throws SQLException {
        String sql = "UPDATE vehicles SET status = ? WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, vehicleId);
            ps.executeUpdate();
        }
    }

    private Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle(
                rs.getInt("vehicle_id"),
                rs.getString("make"),
                rs.getString("model"),
                rs.getInt("year"),
                rs.getInt("car_type_id"),
                rs.getString("license_plate"),
                rs.getBigDecimal("daily_rate"),
                Vehicle.Status.valueOf(rs.getString("status")),
                rs.getInt("location_id"),
                rs.getString("image_url")
        );
        vehicle.setCarTypeName(rs.getString("type_name"));
        return vehicle;
    }
}
