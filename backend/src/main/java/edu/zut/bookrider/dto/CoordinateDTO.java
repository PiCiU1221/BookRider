package edu.zut.bookrider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoordinateDTO {
    private double latitude;
    private double longitude;
}
