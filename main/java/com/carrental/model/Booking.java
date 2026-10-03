package com.carrental.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {

    public enum Status { PENDING, CONFIRMED, CANCELLED, COMPLETED }

    private int bookingId;
    private int customerId;
    private int vehicleId;
    private int pickupLocationId;
    private int dropoffLocationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Status status;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;

    public Booking(int customerId, int vehicleId, int pickupLocationId, int dropoffLocationId,
                    LocalDate startDate, LocalDate endDate) {
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.pickupLocationId = pickupLocationId;
        this.dropoffLocationId = dropoffLocationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = Status.PENDING;
    }

    public long getRentalDays() {
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        return Math.max(days, 1); // minimum one day
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public int getVehicleId() { return vehicleId; }
    public int getPickupLocationId() { return pickupLocationId; }
    public int getDropoffLocationId() { return dropoffLocationId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
