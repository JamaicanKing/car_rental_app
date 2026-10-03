package com.carrental.service;

import com.carrental.dao.BookingDAO;
import com.carrental.dao.VehicleDAO;
import com.carrental.exception.BookingException;
import com.carrental.model.Booking;
import com.carrental.model.Vehicle;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public Booking createBooking(int customerId, int vehicleId, int pickupLocationId,
                                  int dropoffLocationId, LocalDate startDate, LocalDate endDate)
            throws BookingException {
        if (!startDate.isBefore(endDate)) {
            throw new BookingException("End date must be after start date.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BookingException("Start date can't be in the past.");
        }

        try {
            Optional<Vehicle> vehicleOpt = vehicleDAO.findById(vehicleId);
            if (vehicleOpt.isEmpty()) {
                throw new BookingException("Vehicle not found.");
            }
            Vehicle vehicle = vehicleOpt.get();
            if (!vehicle.isAvailable()) {
                throw new BookingException("This vehicle is not currently available.");
            }
            if (!vehicleDAO.isAvailableForDates(vehicleId, startDate, endDate)) {
                throw new BookingException("This vehicle is already booked for part of that date range.");
            }

            Booking booking = new Booking(customerId, vehicleId, pickupLocationId, dropoffLocationId,
                    startDate, endDate);
            BigDecimal totalCost = vehicle.getDailyRate().multiply(BigDecimal.valueOf(booking.getRentalDays()));
            booking.setTotalCost(totalCost);
            booking.setStatus(Booking.Status.CONFIRMED);

            Booking saved = bookingDAO.create(booking);
            vehicleDAO.updateStatus(vehicleId, Vehicle.Status.RENTED);
            return saved;
        } catch (SQLException e) {
            throw new BookingException("Booking failed: " + e.getMessage());
        }
    }

    public List<Booking> getBookingsForCustomer(int customerId) throws SQLException {
        return bookingDAO.findByCustomer(customerId);
    }

    public void cancelBooking(int bookingId, int vehicleId) throws SQLException {
        bookingDAO.updateStatus(bookingId, Booking.Status.CANCELLED);
        vehicleDAO.updateStatus(vehicleId, Vehicle.Status.AVAILABLE);
    }

    /** Looks the booking up itself so callers (the API layer) don't need to know its vehicleId. */
    public void cancelBooking(int bookingId) throws BookingException, SQLException {
        Optional<Booking> bookingOpt = bookingDAO.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new BookingException("Booking not found.");
        }
        cancelBooking(bookingId, bookingOpt.get().getVehicleId());
    }
}
