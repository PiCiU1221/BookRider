package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.AttributeAddRequestDto;
import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.model.Language;
import edu.zut.bookrider.repository.LanguageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    public List<FilterResponseDTO> getAllLanguages() {
        List<Language> languages = languageRepository.findAll(Sort.by(Sort.Order.asc("name")));

        return languages.stream()
                .map(language -> new FilterResponseDTO(language.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public FilterResponseDTO addLanguage(@Valid AttributeAddRequestDto attributeAddRequestDto) {
        if (languageRepository.existsByName(attributeAddRequestDto.getName())) {
            throw new IllegalStateException("Provided language already exists");
        }

        Language language = Language.builder()
                .name(attributeAddRequestDto.getName())
                .build();

        Language savedLanguage = languageRepository.save(language);
        return new FilterResponseDTO(savedLanguage.getName());
    }
}
