package com.eni.bookhub.service;

import com.eni.bookhub.entity.Category;
import com.eni.bookhub.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private CategoryService categoryService;

    @Test
    void getAllCategoryNames_returnsSortedNames() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                Category.builder().id(1).nom("Science").build(),
                Category.builder().id(2).nom("Art").build(),
                Category.builder().id(3).nom("Roman").build()
        ));

        List<String> result = categoryService.getAllCategoryNames();

        assertThat(result).containsExactly("Art", "Roman", "Science");
    }

    @Test
    void getAllCategoryNames_emptyRepository_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<String> result = categoryService.getAllCategoryNames();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllCategoryNames_singleCategory_returnsSingleElement() {
        when(categoryRepository.findAll()).thenReturn(
                List.of(Category.builder().id(1).nom("Policier").build())
        );

        List<String> result = categoryService.getAllCategoryNames();

        assertThat(result).containsExactly("Policier");
    }
}
