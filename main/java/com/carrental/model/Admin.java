package com.carrental.model;

/**
 * Staff account with management privileges. Extends User the same way
 * Customer does — this is the polymorphism in the diagram's User hierarchy.
 */
public class Admin extends User {

    private int adminId;

    public Admin(int userId, String email, String passwordHash,
                 String firstName, String lastName, String phone) {
        super(userId, email, passwordHash, firstName, lastName, phone);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }
}
