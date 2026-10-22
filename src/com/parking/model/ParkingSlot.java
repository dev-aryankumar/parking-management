package com.parking.model;

public class ParkingSlot {

    private int slotId;
    private int floorId;
    private String pillarSection;
    private String slotType;
    private boolean occupied;

    public ParkingSlot() {
    }

    public ParkingSlot(int slotId, int floorId, String pillarSection, String slotType, boolean occupied) {
        this.slotId = slotId;
        this.floorId = floorId;
        this.pillarSection = pillarSection;
        this.slotType = slotType;
        this.occupied = occupied;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public int getFloorId() {
        return floorId;
    }

    public void setFloorId(int floorId) {
        this.floorId = floorId;
    }

    public String getPillarSection() {
        return pillarSection;
    }

    public void setPillarSection(String pillarSection) {
        this.pillarSection = pillarSection;
    }

    public String getSlotType() {
        return slotType;
    }

    public void setSlotType(String slotType) {
        this.slotType = slotType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}
