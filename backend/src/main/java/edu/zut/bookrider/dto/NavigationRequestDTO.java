package edu.zut.bookrider.dto;

import edu.zut.bookrider.service.enums.TransportProfile;
import lombok.Data;

@Data
public class NavigationRequestDTO {
    private CoordinateDTO startCoordinates;
    private CoordinateDTO endCoordinates;
    private TransportProfile transportProfile;
}
