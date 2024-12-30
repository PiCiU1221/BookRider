package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AddressService {

    private final GeocodeService geocodeService;
    private final AddressRepository addressRepository;

    public Address createAddress(CreateAddressDTO createAddressDTO) {
        Address address = new Address();

        address.setStreet(createAddressDTO.getStreet());
        address.setCity(createAddressDTO.getCity());
        address.setPostalCode(createAddressDTO.getPostalCode());

        CoordinateDTO coordinateDTO = geocodeService.getCoordinatesFromAddress(
                createAddressDTO.getStreet(),
                createAddressDTO.getCity(),
                createAddressDTO.getPostalCode()
        );

        address.setLatitude(BigDecimal.valueOf(coordinateDTO.getLatitude()));
        address.setLongitude(BigDecimal.valueOf(coordinateDTO.getLongitude()));

        return addressRepository.save(address);
    }

    public Optional<Address> findExistingAddress(CreateAddressDTO createAddressDTO) {
        return addressRepository.findByStreetAndCityAndPostalCode(
                createAddressDTO.getStreet(),
                createAddressDTO.getCity(),
                createAddressDTO.getPostalCode()
        );
    }
}
