package com.parking.dao;

import com.parking.config.DBConnection;
import com.parking.model.Employee;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmployeeDAO {

    public Employee authenticate(String username, String password) throws SQLException {
        String sql = "SELECT employee_id, username, password, role FROM Employees WHERE username = ? AND password = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Employee(
                        rs.getInt("employee_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"));
            }
        }
    }
}
