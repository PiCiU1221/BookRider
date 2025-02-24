package edu.zut.bookrider.mapper.coordinate;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CoordinateMapper implements Mapper<Address, CoordinateDTO> {

    @Override
    public CoordinateDTO map(Address address) {
        return new CoordinateDTO(
                address.getLatitude().doubleValue(),
                address.getLongitude().doubleValue()
        );
    }
}
