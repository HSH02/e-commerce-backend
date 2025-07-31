package com.ecommerce.domain.product.service;

import com.ecommerce.domain.product.entity.Category;
import com.ecommerce.domain.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Set<Category> findOrCreateCategories(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return Collections.emptySet();
        }

        return categoryNames.stream()
                .map(this::findOrCreateCategoryByName)
                .collect(Collectors.toSet());
    }

    private Category findOrCreateCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .name(name)
                            .build();
                    return categoryRepository.save(newCategory);
                });
    }
}