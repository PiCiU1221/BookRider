package edu.zut.bookrider.service;

import edu.zut.bookrider.model.Library;
import edu.zut.bookrider.model.LibraryAdditionRequest;
import edu.zut.bookrider.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LibraryService {

    private final LibraryRepository libraryRepository;

    public Library createLibrary(LibraryAdditionRequest libraryAdditionRequest) {

        Library library = new Library();

        library.setAddress(libraryAdditionRequest.getAddress());
        library.setName(libraryAdditionRequest.getLibraryName());
        library.setPhoneNumber(libraryAdditionRequest.getPhoneNumber());
        library.setEmail(libraryAdditionRequest.getEmail());

        return libraryRepository.save(library);
    }

    public List<Library> getNearestLibrariesWithBookLimit5(Integer bookId, BigDecimal latitude, BigDecimal longitude) {
        return libraryRepository.findNearestLibrariesWithBook(bookId, latitude, longitude);
    }
}