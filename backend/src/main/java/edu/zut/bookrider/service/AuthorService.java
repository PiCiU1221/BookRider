package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.AttributeAddRequestDto;
import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.model.Author;
import edu.zut.bookrider.repository.AuthorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public FilterResponseDTO addAuthor(@Valid AttributeAddRequestDto attributeAddRequestDto) {
        if (authorRepository.existsByName(attributeAddRequestDto.getName())) {
            throw new IllegalStateException("Author with the provided name already exists");
        }

        Author author = Author.builder()
                .name(attributeAddRequestDto.getName())
                .build();

        Author savedAuthor = authorRepository.save(author);
        return new FilterResponseDTO(savedAuthor.getName());
    }
}
