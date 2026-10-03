package com.carrental.model;

import java.math.BigDecimal;

/**
 * A rentable car. status is kept as a Java enum for compile-time safety,
 * even though it's stored as a MySQL ENUM string.
 */
public class Vehicle {

    public enum Status { AVAILABLE, RENTED, MAINTENANCE }

    private int vehicleId;
    private String make;
    private String model;
    private int year;
    private int carTypeId;
    private String carTypeName;      // denormalized for easy display, filled by the DAO join
    private String licensePlate;
    private BigDecimal dailyRate;
    private Status status;
    private int locationId;
    private String imageUrl;

    public Vehicle(int vehicleId, String make, String model, int year, int carTypeId,
                    String licensePlate, BigDecimal dailyRate, Status status,
                    int locationId, String imageUrl) {
        this.vehicleId = vehicleId;
        this.make = make;
        this.model = model;
        this.year = year;
        this.carTypeId = carTypeId;
        this.licensePlate = licensePlate;
        this.dailyRate = dailyRate;
        this.status = status;
        this.locationId = locationId;
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    public int getVehicleId() { return vehicleId; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public int getCarTypeId() { return carTypeId; }

    public String getCarTypeName() { return carTypeName; }
    public void setCarTypeName(String carTypeName) { this.carTypeName = carTypeName; }

    public String getLicensePlate() { return licensePlate; }
    public BigDecimal getDailyRate() { return dailyRate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getLocationId() { return locationId; }
    public String getImageUrl() { return imageUrl; }
}
