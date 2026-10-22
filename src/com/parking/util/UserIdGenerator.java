package com.parking.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/** Generates collision-free IDs of the form USR-XXXX. */
public final class UserIdGenerator {

    private static final int MAX_ATTEMPTS = 10;
    private static final Random RANDOM = new Random();

    private UserIdGenerator() {
    }

    public static String generate(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                int number = 1000 + RANDOM.nextInt(9000);
                String candidate = "USR-" + number;
                ps.setString(1, candidate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return candidate;
                    }
                }
            }
        }
        throw new SQLException("Could not generate a unique User ID after " + MAX_ATTEMPTS + " attempts.");
    }
}
