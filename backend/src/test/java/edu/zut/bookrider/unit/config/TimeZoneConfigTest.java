package edu.zut.bookrider.unit.config;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeZoneConfigTest {

    @Test
    void testLocalDateTimeUsesWarsawTime() {

        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime warsawNow = ZonedDateTime.now(ZoneId.of("Europe/Warsaw"));
        ZonedDateTime convertedNow = now.atZone(ZoneId.systemDefault());

        assertEquals(warsawNow.getOffset(), convertedNow.getOffset(), "Timezone is incorrect!");
    }
}
