package com.parking.dao;

import com.parking.config.DBConnection;
import com.parking.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean insertUser(User user) throws SQLException {
        String sql = "INSERT INTO Users (user_id, name, password) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getPassword());
            return ps.executeUpdate() == 1;
        }
    }

    public User getUserById(String userId) throws SQLException {
        String sql = "SELECT user_id, name, password FROM Users WHERE user_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public User authenticate(String userId, String password) throws SQLException {
        String sql = "SELECT user_id, name, password FROM Users WHERE user_id = ? AND password = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean userIdExists(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE user_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getString("user_id"), rs.getString("name"), rs.getString("password"));
    }
}
