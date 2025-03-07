package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.model.Language;
import edu.zut.bookrider.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    public List<FilterResponseDTO> getAllLanguages() {
        List<Language> languages = languageRepository.findAll();

        return languages.stream()
                .map(language -> new FilterResponseDTO(language.getName()))
                .collect(Collectors.toList());
    }
}
