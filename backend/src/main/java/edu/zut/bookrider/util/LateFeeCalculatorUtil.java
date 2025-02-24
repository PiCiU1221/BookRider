package edu.zut.bookrider.util;

import edu.zut.bookrider.model.Rental;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static edu.zut.bookrider.config.SystemConstants.DAILY_LATE_FEE;

public final class LateFeeCalculatorUtil {

    public static BigDecimal calculateLateFee(Rental rental) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(rental.getReturnDeadline())) {
            long daysLate = ChronoUnit.DAYS.between(rental.getReturnDeadline(), now);
            BigDecimal lateFee = DAILY_LATE_FEE.multiply(BigDecimal.valueOf(daysLate));
            return lateFee.setScale(2, RoundingMode.CEILING);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.CEILING);
    }
}
