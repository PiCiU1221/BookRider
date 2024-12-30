package edu.zut.bookrider.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.exception.AddressNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class GeocodeService {

    @Value("${OPENROUTESERVICE_API_KEY}")
    private String apiKey;

    private final String baseUrl = "https://api.openrouteservice.org";

    private final RestTemplate restTemplate;

    public CoordinateDTO getCoordinatesFromAddress (String street, String city, String postalCode) {
        String jsonResponse = callApiForGeocodeResponse(street, city, postalCode);
        return parseApiResponseIntoCoordinates(jsonResponse);
    }

    private String callApiForGeocodeResponse(String street, String city, String postalCode) {
        String text = street + " " + city + " " + postalCode;

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/geocode/search")
                .queryParam("api_key", apiKey)
                .queryParam("text", text)
                .buildAndExpand(text)
                .toUriString();

        return restTemplate.getForObject(url, String.class);
    }

    private CoordinateDTO parseApiResponseIntoCoordinates(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        JsonNode features = rootNode.path("features");

        CoordinateDTO coordinateDTO = new CoordinateDTO();

        if (features.isEmpty()) {
            throw new AddressNotFoundException("No valid address found for the provided coordinates.");
        } else {
            JsonNode firstFeature = features.get(0);

            JsonNode geometry = firstFeature.path("geometry");
            JsonNode coordinates = geometry.path("coordinates");

            coordinateDTO.setLatitude(coordinates.get(1).asDouble());
            coordinateDTO.setLongitude(coordinates.get(0).asDouble());
        }
        return coordinateDTO;
    }
}
