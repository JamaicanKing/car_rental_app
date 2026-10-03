package com.carrental.dao;

import com.carrental.model.Booking;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDAO {

    public Booking create(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (customer_id, vehicle_id, pickup_location_id, " +
                "dropoff_location_id, start_date, end_date, status, total_cost) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getCustomerId());
            ps.setInt(2, booking.getVehicleId());
            ps.setInt(3, booking.getPickupLocationId());
            ps.setInt(4, booking.getDropoffLocationId());
            ps.setDate(5, Date.valueOf(booking.getStartDate()));
            ps.setDate(6, Date.valueOf(booking.getEndDate()));
            ps.setString(7, booking.getStatus().name());
            ps.setBigDecimal(8, booking.getTotalCost());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                booking.setBookingId(keys.getInt(1));
            }
        }
        return booking;
    }

    public List<Booking> findByCustomer(int customerId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE customer_id = ? ORDER BY created_at DESC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        }
        return bookings;
    }

    public Optional<Booking> findById(int bookingId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public void updateStatus(int bookingId, Booking.Status status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking(
                rs.getInt("customer_id"),
                rs.getInt("vehicle_id"),
                rs.getInt("pickup_location_id"),
                rs.getInt("dropoff_location_id"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date").toLocalDate()
        );
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setStatus(Booking.Status.valueOf(rs.getString("status")));
        booking.setTotalCost(rs.getBigDecimal("total_cost"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            booking.setCreatedAt(createdAt.toLocalDateTime());
        }
        return booking;
    }
}
