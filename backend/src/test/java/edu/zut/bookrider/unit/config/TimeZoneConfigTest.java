package edu.zut.bookrider.unit.config;

import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeZoneConfigTest {

    @Test
    void testSystemTimezoneIsWarsaw() {
        String systemTimezone = TimeZone.getDefault().getID();

        assertEquals("Europe/Warsaw", systemTimezone, "The system timezone is not Europe/Warsaw");
    }
}
