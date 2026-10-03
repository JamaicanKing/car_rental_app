package com.carrental.service;

import com.carrental.dao.VehicleDAO;
import com.carrental.model.Vehicle;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class VehicleService {

    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public List<Vehicle> listAvailableVehicles(Integer carTypeId, Integer locationId) throws SQLException {
        return vehicleDAO.findAvailable(carTypeId, locationId);
    }

    public Optional<Vehicle> getVehicle(int vehicleId) throws SQLException {
        return vehicleDAO.findById(vehicleId);
    }
}
