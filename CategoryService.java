package com.library.service;

import com.library.dto.category.CategoryRequest;
import com.library.dto.category.CategoryResponse;
import com.library.entity.Category;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new BadRequestException("A category with name '" + request.getName() + "' already exists.");
        }

        Category category = new Category(request.getName(), request.getDescription(), request.getIcon());
        category.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        long count = bookRepository.countByCategoryId(id);
        if (count > 0) {
            // Soft deactivate if books reference it
            category.setIsActive(false);
            categoryRepository.save(category);
        } else {
            categoryRepository.delete(category);
        }
    }

    public CategoryResponse mapToResponse(Category c) {
        long count = bookRepository.countByCategoryId(c.getId());
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getIcon(), c.getIsActive(), count);
    }
}

