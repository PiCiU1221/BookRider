package edu.zut.bookrider.controller;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.NavigationResponseDTO;
import edu.zut.bookrider.exception.InvalidTransportProfileException;
import edu.zut.bookrider.service.NavigationService;
import edu.zut.bookrider.service.enums.TransportProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/navigation")
public class NavigationController {

    private final NavigationService navigationService;

    @Autowired
    public NavigationController(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @GetMapping("/directions")
    public ResponseEntity<?> getNavigationSteps(
            @RequestParam @NotBlank @Pattern(regexp = "^[-+]?\\d*\\.\\d+,[-+]?\\d*\\.\\d+$", message = "Start coordinates must be in 'double,double' format") String start,
            @RequestParam @NotBlank @Pattern(regexp = "^[-+]?\\d*\\.\\d+,[-+]?\\d*\\.\\d+$", message = "End coordinates must be in 'double,double' format") String end,
            @RequestParam @NotBlank String transportProfile) {

        String[] startParsed = start.split(",");
        double startLongitude = Double.parseDouble(startParsed[0]);
        double startLatitude = Double.parseDouble(startParsed[1]);

        String[] endParsed = end.split(",");
        double endLongitude = Double.parseDouble(endParsed[0]);
        double endLatitude = Double.parseDouble(endParsed[1]);

        TransportProfile profile;
        try {
            profile = TransportProfile.valueOf(transportProfile.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTransportProfileException("Invalid transport profile: " + transportProfile);
        }

        CoordinateDTO startCoordinates = new CoordinateDTO(startLatitude, startLongitude);
        CoordinateDTO endCoordinates = new CoordinateDTO(endLatitude, endLongitude);

        NavigationResponseDTO response = navigationService.getDirectionsFromCoordinates(
                startCoordinates,
                endCoordinates,
                profile
        );
        return ResponseEntity.ok(response);
    }
}
