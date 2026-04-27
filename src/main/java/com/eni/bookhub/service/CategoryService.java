package com.eni.bookhub.service;

import com.eni.bookhub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<String> getAllCategoryNames() {
        return categoryRepository.findAll()
                .stream()
                .map(c -> c.getNom())
                .sorted()
                .toList();
    }
}
