package edu.zut.bookrider.mapper.library;

import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AddressMapper implements Mapper<Address, CreateAddressDTO> {

    @Override
    public CreateAddressDTO map(Address address) {
        return new CreateAddressDTO(
                address.getStreet(),
                address.getCity(),
                address.getPostalCode()
        );
    }
}
