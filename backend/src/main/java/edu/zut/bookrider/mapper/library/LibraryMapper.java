package edu.zut.bookrider.mapper.library;

import edu.zut.bookrider.dto.CreateAddressDTO;
import edu.zut.bookrider.dto.LibraryDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.Library;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LibraryMapper implements Mapper<Library, LibraryDTO> {

    private final AddressMapper addressMapper;

    @Override
    public LibraryDTO map(Library library) {

        CreateAddressDTO createAddressDTO = addressMapper.map(library.getAddress());

        return new LibraryDTO(
                library.getId(),
                createAddressDTO,
                library.getName(),
                library.getPhoneNumber(),
                library.getEmail(),
                library.getCreatedAt()
        );
    }
}
