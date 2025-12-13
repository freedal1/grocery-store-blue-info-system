package com.grocerystore.service;

import com.grocerystore.entity.Category;
import com.grocerystore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с категориями товаров
 */
@Service
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    /**
     * Создание новой категории
     */
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Категория с таким названием уже существует");
        }
        return categoryRepository.save(category);
    }
    
    /**
     * Получение категории по ID
     */
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }
    
    /**
     * Получение категории по названию
     */
    @Transactional(readOnly = true)
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    /**
     * Получение категории с товарами
     */
    @Transactional(readOnly = true)
    public Optional<Category> findByIdWithProducts(Long id) {
        return categoryRepository.findByIdWithProducts(id);
    }
    
    /**
     * Получение всех категорий
     */
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
    
    /**
     * Получение категорий с пагинацией
     */
    @Transactional(readOnly = true)
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
    /**
     * Поиск категорий по названию
     */
    @Transactional(readOnly = true)
    public Page<Category> searchByName(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return categoryRepository.findAll(pageable);
        }
        return categoryRepository.searchByName(search.trim(), pageable);
    }
    
    /**
     * Обновление категории
     */
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        
        // Проверка уникальности имени при изменении
        if (!existingCategory.getName().equals(updatedCategory.getName()) 
            && categoryRepository.existsByName(updatedCategory.getName())) {
            throw new RuntimeException("Категория с таким названием уже существует");
        }
        
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        
        return categoryRepository.save(existingCategory);
    }
    
    /**
     * Удаление категории
     */
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Невозможно удалить категорию с товарами");
        }
        
        categoryRepository.deleteById(id);
    }
    
    /**
     * Проверка существования категории
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
    
    /**
     * Получение количества категорий
     */
    @Transactional(readOnly = true)
    public long count() {
        return categoryRepository.count();
    }
}


