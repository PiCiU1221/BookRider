package edu.zut.bookrider.unit.config;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeZoneConfigTest {

    @Test
    void testLocalDateTimeUsesWarsawTime() {
        int warsawHour = ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).getHour();
        int systemHour = LocalDateTime.now().getHour();

        assertEquals(warsawHour, systemHour, "The system timezone is not Europe/Warsaw");
    }
}
