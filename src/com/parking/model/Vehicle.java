package com.parking.model;

public class Vehicle {

    private String vehicleNumber;
    private String userId;
    private String type;

    public Vehicle() {
    }

    public Vehicle(String vehicleNumber, String userId, String type) {
        this.vehicleNumber = vehicleNumber;
        this.userId = userId;
        this.type = type;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /** Used directly by JComboBox so the gate shows "KA-01-AB-1234 (4W)". */
    @Override
    public String toString() {
        return vehicleNumber + " (" + type + ")";
    }
}
