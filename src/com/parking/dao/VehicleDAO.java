package com.parking.dao;

import com.parking.config.DBConnection;
import com.parking.model.Vehicle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public boolean insertVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO Vehicles (vehicle_number, user_id, type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, vehicle.getVehicleNumber());
            ps.setString(2, vehicle.getUserId());
            ps.setString(3, vehicle.getType());
            return ps.executeUpdate() == 1;
        }
    }

    public List<Vehicle> getVehiclesByUserId(String userId) throws SQLException {
        String sql = "SELECT vehicle_number, user_id, type FROM Vehicles WHERE user_id = ? ORDER BY vehicle_number";
        List<Vehicle> vehicles = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(map(rs));
                }
            }
        }
        return vehicles;
    }

    public boolean vehicleExists(String vehicleNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vehicles WHERE vehicle_number = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public Vehicle getVehicleByNumber(String vehicleNumber) throws SQLException {
        String sql = "SELECT vehicle_number, user_id, type FROM Vehicles WHERE vehicle_number = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(rs.getString("vehicle_number"), rs.getString("user_id"), rs.getString("type"));
    }
}
