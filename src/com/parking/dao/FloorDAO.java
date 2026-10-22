package com.parking.dao;

import com.parking.config.DBConnection;
import com.parking.model.Floor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FloorDAO {

    public List<Floor> getAllFloors() throws SQLException {
        String sql = "SELECT floor_id, floor_number FROM Floors ORDER BY floor_id";
        List<Floor> floors = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                floors.add(new Floor(rs.getInt("floor_id"), rs.getString("floor_number")));
            }
        }
        return floors;
    }

    public Floor getFloorById(int floorId) throws SQLException {
        String sql = "SELECT floor_id, floor_number FROM Floors WHERE floor_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, floorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Floor(rs.getInt("floor_id"), rs.getString("floor_number")) : null;
            }
        }
    }
}
