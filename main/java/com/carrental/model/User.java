package com.carrental.model;

import java.time.LocalDateTime;

/**
 * Abstract base class for anyone who can log into the system.
 * Customer and Admin both extend this — matches the User -> {Customer, Admin}
 * inheritance in the UML class diagram.
 */
public abstract class User {

    protected int userId;
    protected String email;
    protected String passwordHash;
    protected String firstName;
    protected String lastName;
    protected String phone;
    protected boolean isActive;
    protected LocalDateTime lastLogin;

    protected User(int userId, String email, String passwordHash,
                    String firstName, String lastName, String phone) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.isActive = true;
    }

    /** Polymorphic hook: each subclass reports its own role. */
    public abstract String getRole();

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}
