package com.parking.model;

public class Floor {

    private int floorId;
    private String floorNumber;

    public Floor() {
    }

    public Floor(int floorId, String floorNumber) {
        this.floorId = floorId;
        this.floorNumber = floorNumber;
    }

    public int getFloorId() {
        return floorId;
    }

    public void setFloorId(int floorId) {
        this.floorId = floorId;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(String floorNumber) {
        this.floorNumber = floorNumber;
    }

    /** Used directly by JComboBox. */
    @Override
    public String toString() {
        return floorNumber;
    }
}
