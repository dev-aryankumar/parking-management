package com.parking.dao;

import com.parking.config.DBConnection;
import com.parking.model.ActiveTicketView;
import com.parking.model.ParkingTicket;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParkingTicketDAO {

    /** Inserts a ticket and returns the generated ticket_id. */
    public int insertTicket(ParkingTicket ticket) throws SQLException {
        String sql = "INSERT INTO ParkingTickets (vehicle_number, slot_id, employee_id, entry_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ticket.getVehicleNumber());
            ps.setInt(2, ticket.getSlotId());
            ps.setInt(3, ticket.getEmployeeId());
            ps.setTimestamp(4, Timestamp.valueOf(ticket.getEntryTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    ticket.setTicketId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Ticket was inserted but no generated key was returned.");
    }

    public ParkingTicket getActiveTicketByVehicle(String vehicleNumber) throws SQLException {
        String sql = "SELECT * FROM ParkingTickets WHERE vehicle_number = ? AND exit_time IS NULL "
                + "ORDER BY entry_time DESC LIMIT 1";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public ParkingTicket getMostRecentActiveTicketByUserId(String userId) throws SQLException {
        String sql = "SELECT pt.* FROM ParkingTickets pt "
                + "JOIN Vehicles v ON pt.vehicle_number = v.vehicle_number "
                + "WHERE v.user_id = ? AND pt.exit_time IS NULL "
                + "ORDER BY pt.entry_time DESC LIMIT 1";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean updateTicketExit(int ticketId, LocalDateTime exitTime, BigDecimal totalFee) throws SQLException {
        String sql = "UPDATE ParkingTickets SET exit_time = ?, total_fee = ? WHERE ticket_id = ? AND exit_time IS NULL";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(exitTime));
            ps.setBigDecimal(2, totalFee);
            ps.setInt(3, ticketId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean hasActiveTicket(String vehicleNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ParkingTickets WHERE vehicle_number = ? AND exit_time IS NULL";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, vehicleNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Dashboard query: joins vehicle type, floor and pillar for display. */
    public List<ActiveTicketView> getActiveTicketViews(String userId) throws SQLException {
        String sql = "SELECT pt.ticket_id, pt.vehicle_number, v.type AS vehicle_type, "
                + "ps.pillar_section, f.floor_number, pt.entry_time "
                + "FROM ParkingTickets pt "
                + "JOIN Vehicles v ON pt.vehicle_number = v.vehicle_number "
                + "JOIN ParkingSlots ps ON pt.slot_id = ps.slot_id "
                + "JOIN Floors f ON ps.floor_id = f.floor_id "
                + "WHERE v.user_id = ? AND pt.exit_time IS NULL "
                + "ORDER BY pt.entry_time DESC";
        List<ActiveTicketView> rows = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ActiveTicketView(
                            rs.getInt("ticket_id"),
                            rs.getString("vehicle_number"),
                            rs.getString("vehicle_type"),
                            rs.getString("floor_number"),
                            rs.getString("pillar_section"),
                            toLocalDateTime(rs.getTimestamp("entry_time"))));
                }
            }
        }
        return rows;
    }

    private ParkingTicket map(ResultSet rs) throws SQLException {
        ParkingTicket ticket = new ParkingTicket();
        ticket.setTicketId(rs.getInt("ticket_id"));
        ticket.setVehicleNumber(rs.getString("vehicle_number"));
        ticket.setSlotId(rs.getInt("slot_id"));
        ticket.setEmployeeId(rs.getInt("employee_id"));
        ticket.setEntryTime(toLocalDateTime(rs.getTimestamp("entry_time")));
        ticket.setExitTime(toLocalDateTime(rs.getTimestamp("exit_time")));
        ticket.setTotalFee(rs.getBigDecimal("total_fee"));
        return ticket;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
