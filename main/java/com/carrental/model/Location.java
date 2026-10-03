package com.carrental.model;

public class Location {

    private int locationId;
    private String name;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private boolean isActive;

    public Location(int locationId, String name, String address, String city,
                     String state, String zipCode, String phone, boolean isActive) {
        this.locationId = locationId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.phone = phone;
        this.isActive = isActive;
    }

    public int getLocationId() { return locationId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getPhone() { return phone; }
    public boolean isActive() { return isActive; }
}
