package com.carrental.exception;

/** Thrown when a booking can't be created as requested (vehicle unavailable, bad dates, etc). */
public class BookingException extends Exception {
    public BookingException(String message) {
        super(message);
    }
}
