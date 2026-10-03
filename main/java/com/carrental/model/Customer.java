package com.carrental.model;

/**
 * A renter. Extends User (inherits login credentials/profile) and adds
 * the customer-specific fields from the UML diagram.
 */
public class Customer extends User {

    private int customerId;
    private String address;
    private boolean isVerified;

    public Customer(int userId, String email, String passwordHash,
                     String firstName, String lastName, String phone, String address) {
        super(userId, email, passwordHash, firstName, lastName, phone);
        this.address = address;
        this.isVerified = false;
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
}
