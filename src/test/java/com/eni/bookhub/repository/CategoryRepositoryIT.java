package com.eni.bookhub.repository;

import com.eni.bookhub.AbstractIntegrationTest;
import com.eni.bookhub.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void save_andFindAll_returnsCategories() {
        categoryRepository.save(Category.builder().nom("Roman").build());
        categoryRepository.save(Category.builder().nom("Science").build());

        List<Category> result = categoryRepository.findAll();

        assertThat(result).extracting(Category::getNom)
                .contains("Roman", "Science");
    }

    @Test
    void findById_existingId_returnsCategory() {
        Category saved = categoryRepository.save(Category.builder().nom("Policier").build());

        assertThat(categoryRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Category::getNom)
                .isEqualTo("Policier");
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertThat(categoryRepository.findById(9999)).isEmpty();
    }

    @Test
    void delete_removesCategory() {
        Category saved = categoryRepository.save(Category.builder().nom("Fantaisie").build());
        categoryRepository.delete(saved);

        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }
}
