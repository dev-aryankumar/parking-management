package com.parking.model;

import java.time.LocalDateTime;

/**
 * Read-only row used by the user dashboard table. It is the result of joining
 * ParkingTickets -> Vehicles -> ParkingSlots -> Floors.
 */
public class ActiveTicketView {

    private final int ticketId;
    private final String vehicleNumber;
    private final String vehicleType;
    private final String floorNumber;
    private final String pillarSection;
    private final LocalDateTime entryTime;

    public ActiveTicketView(int ticketId, String vehicleNumber, String vehicleType,
                            String floorNumber, String pillarSection, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.floorNumber = floorNumber;
        this.pillarSection = pillarSection;
        this.entryTime = entryTime;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public String getPillarSection() {
        return pillarSection;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }
}
