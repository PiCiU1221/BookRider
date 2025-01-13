package edu.zut.bookrider.util;

import edu.zut.bookrider.dto.CoordinateDTO;

public class LocationUtils {
    private static final double EARTH_RADIUS_KM = 6371.0;

    public static double calculateDistance(CoordinateDTO start, CoordinateDTO end) {
        double lat1Rad = Math.toRadians(start.getLatitude());
        double lon1Rad = Math.toRadians(start.getLongitude());
        double lat2Rad = Math.toRadians(end.getLatitude());
        double lon2Rad = Math.toRadians(end.getLongitude());

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // returns meters
        return EARTH_RADIUS_KM * c * 1000;
    }
}
