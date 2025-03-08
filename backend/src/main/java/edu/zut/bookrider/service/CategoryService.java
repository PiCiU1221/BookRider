package edu.zut.bookrider.service;

import edu.zut.bookrider.dto.FilterResponseDTO;
import edu.zut.bookrider.model.Category;
import edu.zut.bookrider.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<FilterResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Order.asc("name")));

        return categories.stream()
                .map(category -> new FilterResponseDTO(category.getName()))
                .collect(Collectors.toList());
    }
}
