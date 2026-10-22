package com.parking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Billing rules from the PRD:
 *   - Under 60 minutes        -> free
 *   - 60 minutes and above    -> rounded UP to the next completed hour
 *   - 2W = Rs.40/hour, 4W = Rs.80/hour
 */
public final class FeeCalculator {

    public static final BigDecimal RATE_2W = new BigDecimal("40.00");
    public static final BigDecimal RATE_4W = new BigDecimal("80.00");

    private FeeCalculator() {
    }

    public static BigDecimal calculateFee(LocalDateTime entryTime, LocalDateTime exitTime, String vehicleType) {
        if (entryTime == null || exitTime == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long totalMinutes = ChronoUnit.MINUTES.between(entryTime, exitTime);
        if (totalMinutes < 60) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        long completedHours = (long) Math.ceil(totalMinutes / 60.0);
        BigDecimal rate = "2W".equalsIgnoreCase(vehicleType) ? RATE_2W : RATE_4W;
        return rate.multiply(BigDecimal.valueOf(completedHours)).setScale(2, RoundingMode.HALF_UP);
    }

    public static long getCompletedHours(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            return 0;
        }
        long totalMinutes = ChronoUnit.MINUTES.between(entryTime, exitTime);
        if (totalMinutes < 60) {
            return 0;
        }
        return (long) Math.ceil(totalMinutes / 60.0);
    }

    public static BigDecimal getRate(String vehicleType) {
        return "2W".equalsIgnoreCase(vehicleType) ? RATE_2W : RATE_4W;
    }

    public static String formatDuration(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            return "0 mins";
        }
        long totalMinutes = Math.max(0, ChronoUnit.MINUTES.between(entryTime, exitTime));
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours == 0) {
            return minutes + " mins";
        }
        return hours + " hrs " + minutes + " mins";
    }
}
