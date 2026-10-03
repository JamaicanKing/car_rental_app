package com.carrental.dao;

import com.carrental.model.Admin;
import com.carrental.model.Customer;
import com.carrental.model.User;

import java.sql.*;
import java.util.Optional;

/**
 * Handles both the shared `users` table and the per-role tables
 * (`customers` / `admins`), and reassembles the right subclass.
 */
public class UserDAO {

    public Customer createCustomer(Customer customer) throws SQLException {
        String userSql = "INSERT INTO users (email, password_hash, first_name, last_name, " +
                "phone, role) VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";
        String customerSql = "INSERT INTO customers (user_id, address) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, customer.getEmail());
                    ps.setString(2, customer.getPasswordHash());
                    ps.setString(3, customer.getFirstName());
                    ps.setString(4, customer.getLastName());
                    ps.setString(5, customer.getPhone());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        userId = keys.getInt(1);
                    }
                }

                int customerId;
                try (PreparedStatement ps = conn.prepareStatement(customerSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setString(2, customer.getAddress());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        customerId = keys.getInt(1);
                    }
                }

                conn.commit();
                customer.setUserId(userId);
                customer.setCustomerId(customerId);
                return customer;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Looks up a user by email for login, returning the concrete Customer/Admin subtype. */
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT u.*, c.customer_id, c.address, c.is_verified, a.admin_id " +
                "FROM users u " +
                "LEFT JOIN customers c ON c.user_id = u.user_id " +
                "LEFT JOIN admins a ON a.user_id = u.user_id " +
                "WHERE u.email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRow(rs));
            }
        }
    }

    public void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        int userId = rs.getInt("user_id");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String phone = rs.getString("phone");

        User user;
        if ("ADMIN".equals(role)) {
            Admin admin = new Admin(userId, email, passwordHash, firstName, lastName, phone);
            admin.setAdminId(rs.getInt("admin_id"));
            user = admin;
        } else {
            Customer customer = new Customer(userId, email, passwordHash, firstName, lastName,
                    phone, rs.getString("address"));
            customer.setCustomerId(rs.getInt("customer_id"));
            customer.setVerified(rs.getBoolean("is_verified"));
            user = customer;
        }
        user.setActive(rs.getBoolean("is_active"));
        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }
        return user;
    }
}
