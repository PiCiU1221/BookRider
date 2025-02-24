package edu.zut.bookrider.unit.utils;

import edu.zut.bookrider.model.Rental;
import edu.zut.bookrider.util.LateFeeCalculatorUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LateFeeCalculatorUtilTest {

    @Test
    void calculateLateFeeWhenReturnedOnTime_thenReturnZero() {

        Rental rental = new Rental();
        rental.setReturnDeadline(LocalDateTime.now().plusDays(1));

        BigDecimal lateFee = LateFeeCalculatorUtil.calculateLateFee(rental);

        assertEquals(BigDecimal.valueOf(0.00).setScale(2, RoundingMode.CEILING), lateFee, "Late fee should be zero if returned on time");
    }

    @Test
    void calculateLateFeeWhenReturnedExactlyOnDeadline_thenReturnZero() {

        Rental rental = new Rental();
        rental.setReturnDeadline(LocalDateTime.now());

        BigDecimal lateFee = LateFeeCalculatorUtil.calculateLateFee(rental);

        assertEquals(BigDecimal.valueOf(0.00).setScale(2, RoundingMode.CEILING), lateFee, "Late fee should be zero if returned exactly on the deadline");
    }

    @Test
    void calculateLateFeeWhenReturnedOneDayLate_thenReturnCorrectFee() {

        Rental rental = new Rental();
        rental.setReturnDeadline(LocalDateTime.now().minusDays(1));

        BigDecimal lateFee = LateFeeCalculatorUtil.calculateLateFee(rental);

        assertEquals(BigDecimal.valueOf(1.00).setScale(2, RoundingMode.CEILING), lateFee, "Late fee should be 1.00 zł for 1 day late");
    }

    @Test
    void calculateLateFeeWhenReturnedThirtyDaysLate_thenReturnCorrectFee() {

        Rental rental = new Rental();
        rental.setReturnDeadline(LocalDateTime.now().minusDays(30));

        BigDecimal lateFee = LateFeeCalculatorUtil.calculateLateFee(rental);

        assertEquals(BigDecimal.valueOf(30.00).setScale(2, RoundingMode.CEILING), lateFee, "Late fee should be 30.00 zł for 30 days late");
    }
}
