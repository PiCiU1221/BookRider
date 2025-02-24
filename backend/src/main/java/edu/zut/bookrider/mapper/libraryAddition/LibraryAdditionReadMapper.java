package edu.zut.bookrider.mapper.libraryAddition;

import edu.zut.bookrider.dto.LibraryAdditionSummaryDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.LibraryAdditionRequest;
import org.springframework.stereotype.Component;

@Component
public class LibraryAdditionReadMapper implements Mapper<LibraryAdditionRequest, LibraryAdditionSummaryDTO> {
    @Override
    public LibraryAdditionSummaryDTO map(LibraryAdditionRequest object) {
        return new LibraryAdditionSummaryDTO(
                object.getId(),
                object.getCreatedBy().getEmail(),
                object.getLibraryName(),
                object.getStatus(),
                object.getSubmittedAt()
        );
    }
}