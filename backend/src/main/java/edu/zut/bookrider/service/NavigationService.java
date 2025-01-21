package edu.zut.bookrider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.NavigationResponseDTO;
import edu.zut.bookrider.exception.ExternalApiException;
import edu.zut.bookrider.exception.InvalidCoordinatesException;
import edu.zut.bookrider.service.enums.TransportProfile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NavigationService {

    @Value("${OPENROUTESERVICE_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public NavigationResponseDTO getDirectionsFromCoordinates(@Valid CoordinateDTO startCoordinates, @Valid CoordinateDTO endCoordinates, TransportProfile transportProfile) {
        String jsonResponse = callApiForDirections(startCoordinates, endCoordinates, transportProfile);
        return parseApiResponseIntoNavigationDTO(jsonResponse);
    }

    private String callApiForDirections(@Valid CoordinateDTO startCoordinates, @Valid CoordinateDTO endCoordinates, TransportProfile transportProfile) {
        String start = startCoordinates.getLongitude() + "," + startCoordinates.getLatitude();
        String end = endCoordinates.getLongitude() + "," + endCoordinates.getLatitude();

        String transportProfileString = transportProfileEnumToString(transportProfile);

        String baseUrl = "https://api.openrouteservice.org";

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/v2/directions/" + transportProfileString)
                .queryParam("api_key", apiKey)
                .queryParam("start", start)
                .queryParam("end", end)
                .buildAndExpand(start, end)
                .toUriString();

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            validateApiResponse(jsonResponse);
            return jsonResponse;
        } catch (HttpClientErrorException ex) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(ex.getResponseBodyAsString());
                String errorMessage = rootNode.path("error").path("message").asText();

                throw new InvalidCoordinatesException(errorMessage);
            } catch (JsonProcessingException e) {
                throw new InvalidCoordinatesException("An error occurred while processing the response.");
            }
        } catch (HttpServerErrorException ex) {
            throw new ExternalApiException("External routing service is currently unavailable");
        }
    }

    private void validateApiResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            JsonNode features = rootNode.path("features").get(0);
            JsonNode segments = features.path("properties").path("segments");

            JsonNode firstSegment = segments.get(0);
            double distance = firstSegment.path("distance").asDouble();

            if (distance > 0) {
                return;
            }

            throw new InvalidCoordinatesException("No valid route found. Please check the coordinates.");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("An error occurred while processing the response.");
        }
    }

    private String transportProfileEnumToString(TransportProfile transportProfile) {
        switch (transportProfile) {
            case CAR:
                return "driving-car";
            case CYCLING:
                return "cycling-regular";
            case FOOT:
                return "foot-walking";
            default:
                return "driving-car";
        }
    }

    private NavigationResponseDTO parseApiResponseIntoNavigationDTO(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse API response", e);
        }

        NavigationResponseDTO navigationResponseDTO = new NavigationResponseDTO();

        JsonNode summaryNode = rootNode.at("/features/0/properties/summary");
        double distanceInMeters = summaryNode.get("distance").asDouble();
        double distanceInKilometers = Math.round((distanceInMeters / 1000) * 10.0) / 10.0;
        navigationResponseDTO.setTotalDistance(distanceInKilometers);
        navigationResponseDTO.setTotalDuration(summaryNode.get("duration").asDouble());

        List<NavigationResponseDTO.RouteStep> steps = new ArrayList<>();
        JsonNode stepsNode = rootNode.at("/features/0/properties/segments/0/steps");
        JsonNode coordinatesNode = rootNode.at("/features/0/geometry/coordinates");

        for (JsonNode stepNode : stepsNode) {
            NavigationResponseDTO.RouteStep step = new NavigationResponseDTO.RouteStep();
            step.setStepDistance(stepNode.get("distance").asDouble());
            step.setStepDuration(stepNode.get("duration").asDouble());
            step.setInstruction(stepNode.get("instruction").asText());

            List<CoordinateDTO> wayPoints = new ArrayList<>();
            JsonNode wayPointsNode = stepNode.get("way_points");
            if (wayPointsNode != null && wayPointsNode.size() == 2) {
                int startIndex = wayPointsNode.get(0).asInt();
                int endIndex = wayPointsNode.get(1).asInt();

                for (int i = startIndex; i <= endIndex; i++) {
                    JsonNode coordinateNode = coordinatesNode.get(i);
                    double longitude = coordinateNode.get(0).asDouble();
                    double latitude = coordinateNode.get(1).asDouble();
                    wayPoints.add(new CoordinateDTO(latitude, longitude));
                }
            }

            step.setWayPoints(wayPoints);
            steps.add(step);
        }

        navigationResponseDTO.setSteps(steps);

        return navigationResponseDTO;
    }
}
