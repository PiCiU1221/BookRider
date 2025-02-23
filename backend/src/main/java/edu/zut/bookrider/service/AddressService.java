package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.model.Address;
import edu.zut.bookrider.repository.AddressRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AddressService {

    private final GeocodeService geocodeService;
    private final AddressRepository addressRepository;

    public Address findOrCreateAddress(@Valid CreateAddressDTO createAddressDTO) {

        Optional<Address> existingAddress = addressRepository.findByStreetAndCityAndPostalCode(
                createAddressDTO.getStreet(),
                createAddressDTO.getCity(),
                createAddressDTO.getPostalCode()
        );

        if (existingAddress.isPresent()) {
            return existingAddress.get();
        }

        CoordinateDTO coordinateDTO = geocodeService.getCoordinatesFromAddress(
                createAddressDTO.getStreet(),
                createAddressDTO.getCity(),
                createAddressDTO.getPostalCode()
        );

        Address address = new Address();
        address.setStreet(createAddressDTO.getStreet());
        address.setCity(createAddressDTO.getCity());
        address.setPostalCode(createAddressDTO.getPostalCode());
        address.setLatitude(BigDecimal.valueOf(coordinateDTO.getLatitude()));
        address.setLongitude(BigDecimal.valueOf(coordinateDTO.getLongitude()));

        return addressRepository.save(address);
    }
}
