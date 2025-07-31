package com.ecommerce.domain.product.service;

import com.ecommerce.domain.product.entity.Category;
import com.ecommerce.domain.product.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("[findOrCreateCategories][성공] - 새로운 카테고리 생성")
    void findOrCreateCategories_CreateNew() {
        // given
        List<String> categoryNames = List.of("전자제품", "신상");
        when(categoryRepository.findByName("전자제품")).thenReturn(Optional.empty());
        when(categoryRepository.findByName("신상")).thenReturn(Optional.empty());

        // categoryRepository.save가 호출되면, 빌더로 생성된 Category 객체를 반환하도록 설정
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            return Category.builder().name(category.getName()).build();
        });

        // when
        Set<Category> result = categoryService.findOrCreateCategories(categoryNames);

        // then
        assertThat(result).hasSize(2);
        verify(categoryRepository, times(2)).save(any(Category.class));
    }

    @Test
    @DisplayName("[findOrCreateCategories][성공] - 기존 카테고리 조회")
    void findOrCreateCategories_FindExisting() {
        // given
        List<String> categoryNames = List.of("의류");
        Category existingCategory = Category.builder().id(1L).name("의류").build();
        when(categoryRepository.findByName("의류")).thenReturn(Optional.of(existingCategory));

        // when
        Set<Category> result = categoryService.findOrCreateCategories(categoryNames);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getName()).isEqualTo("의류");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("[findOrCreateCategories][성공] - 기존 카테고리와 신규 카테고리 혼합")
    void findOrCreateCategories_Mixed() {
        // given
        List<String> categoryNames = List.of("도서", "신간");
        Category existingCategory = Category.builder().id(1L).name("도서").build();

        when(categoryRepository.findByName("도서")).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByName("신간")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            return Category.builder().name(category.getName()).build();
        });

        // when
        Set<Category> result = categoryService.findOrCreateCategories(categoryNames);

        // then
        assertThat(result).hasSize(2);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("[findOrCreateCategories][성공] - 빈 리스트 또는 null 입력")
    void findOrCreateCategories_EmptyOrNull() {
        // when
        Set<Category> resultForNull = categoryService.findOrCreateCategories(null);
        Set<Category> resultForEmpty = categoryService.findOrCreateCategories(List.of());

        // then
        assertThat(resultForNull).isNotNull().isEmpty();
        assertThat(resultForEmpty).isNotNull().isEmpty();
        verify(categoryRepository, never()).findByName(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }
}