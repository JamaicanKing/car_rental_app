package com.carrental.service;

import com.carrental.exception.AuthenticationException;
import com.carrental.model.Customer;
import com.carrental.model.User;
import com.carrental.dao.UserDAO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;

/**
 * Registration and login. Passwords are salted + SHA-256 hashed here with
 * java.security (zero extra dependencies for the prototype). Swap in BCrypt
 * for a production build.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public Customer register(String email, String rawPassword, String firstName,
                              String lastName, String phone, String address)
            throws AuthenticationException {
        try {
            if (userDAO.findByEmail(email).isPresent()) {
                throw new AuthenticationException("An account with this email already exists.");
            }
            String passwordHash = hashPassword(rawPassword);
            Customer customer = new Customer(0, email, passwordHash, firstName, lastName, phone, address);
            return userDAO.createCustomer(customer);
        } catch (SQLException e) {
            throw new AuthenticationException("Registration failed: " + e.getMessage());
        }
    }

    public User login(String email, String rawPassword) throws AuthenticationException {
        try {
            Optional<User> found = userDAO.findByEmail(email);
            if (found.isEmpty()) {
                throw new AuthenticationException("Invalid email or password.");
            }
            User user = found.get();
            if (!user.isActive()) {
                throw new AuthenticationException("This account has been deactivated.");
            }
            if (!verifyPassword(rawPassword, user.getPasswordHash())) {
                throw new AuthenticationException("Invalid email or password.");
            }
            userDAO.updateLastLogin(user.getUserId());
            return user;
        } catch (SQLException e) {
            throw new AuthenticationException("Login failed: " + e.getMessage());
        }
    }

    private String hashPassword(String rawPassword) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = sha256(salt, rawPassword);
        // Store as salt:hash, both base64, so verify() can pull the salt back out.
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String rawPassword, String stored) {
        String[] parts = stored.split(":");
        if (parts.length != 2) return false;
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = sha256(salt, rawPassword);
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private byte[] sha256(byte[] salt, String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(rawPassword.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
