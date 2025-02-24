package edu.zut.bookrider.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class NavigationResponseDTO {
    private double totalDistance;
    private double totalDuration;
    private List<RouteStep> steps;

    @Data
    public static class RouteStep {
        private double stepDistance;
        private double stepDuration;
        private String instruction;
        private List<CoordinateDTO> wayPoints;
    }
}