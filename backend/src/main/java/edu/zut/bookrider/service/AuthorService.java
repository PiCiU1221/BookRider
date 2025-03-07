package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.model.Author;
import edu.zut.bookrider.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<FilterResponseDTO> searchAuthors(String name, Pageable pageable) {
        List<Author> authors = authorRepository.findByNameLike(name, pageable);

        return authors.stream()
                .map(author -> new FilterResponseDTO(author.getName()))
                .collect(Collectors.toList());
    }
}
