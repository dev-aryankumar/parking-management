package com.parking.dao;

import com.parking.config.DBConnection;
import com.parking.model.ParkingSlot;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParkingSlotDAO {

    public ParkingSlot findFirstAvailableSlot(String slotType) throws SQLException {
        String sql = "SELECT slot_id, floor_id, pillar_section, slot_type, is_occupied "
                + "FROM ParkingSlots "
                + "WHERE slot_type = ? AND is_occupied = FALSE "
                + "ORDER BY floor_id ASC, slot_id ASC LIMIT 1";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, slotType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<ParkingSlot> getSlotsByFloorAndPillar(int floorId, String pillarSection, String slotType) throws SQLException {
        String sql = "SELECT slot_id, floor_id, pillar_section, slot_type, is_occupied "
                + "FROM ParkingSlots "
                + "WHERE floor_id = ? AND pillar_section = ? AND slot_type = ? AND is_occupied = FALSE "
                + "ORDER BY slot_id";
        List<ParkingSlot> slots = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, floorId);
            ps.setString(2, pillarSection);
            ps.setString(3, slotType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    slots.add(map(rs));
                }
            }
        }
        return slots;
    }

    public int countAvailableSlots(int floorId, String pillarSection, String slotType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ParkingSlots "
                + "WHERE floor_id = ? AND pillar_section = ? AND slot_type = ? AND is_occupied = FALSE";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, floorId);
            ps.setString(2, pillarSection);
            ps.setString(3, slotType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean updateSlotOccupancy(int slotId, boolean occupied) throws SQLException {
        String sql = "UPDATE ParkingSlots SET is_occupied = ? WHERE slot_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, occupied);
            ps.setInt(2, slotId);
            return ps.executeUpdate() == 1;
        }
    }

    public ParkingSlot getSlotById(int slotId) throws SQLException {
        String sql = "SELECT slot_id, floor_id, pillar_section, slot_type, is_occupied FROM ParkingSlots WHERE slot_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<String> getPillarSectionsByFloor(int floorId) throws SQLException {
        String sql = "SELECT DISTINCT pillar_section FROM ParkingSlots WHERE floor_id = ? ORDER BY pillar_section";
        List<String> sections = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, floorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sections.add(rs.getString("pillar_section"));
                }
            }
        }
        return sections;
    }

    private ParkingSlot map(ResultSet rs) throws SQLException {
        return new ParkingSlot(
                rs.getInt("slot_id"),
                rs.getInt("floor_id"),
                rs.getString("pillar_section"),
                rs.getString("slot_type"),
                rs.getBoolean("is_occupied"));
    }
}
