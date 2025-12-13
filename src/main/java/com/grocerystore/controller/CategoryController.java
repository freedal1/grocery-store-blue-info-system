package com.grocerystore.controller;

import com.grocerystore.entity.Category;
import com.grocerystore.service.CategoryService;
import com.grocerystore.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для работы с категориями товаров
 */
@Controller
@RequestMapping("/categories")
public class CategoryController {
    
    private final CategoryService categoryService;
    private final ProductService productService;
    
    @Autowired
    public CategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }
    
    /**
     * Список всех категорий
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Category> categories;
        if (search != null && !search.trim().isEmpty()) {
            categories = categoryService.searchByName(search, pageable);
        } else {
            categories = categoryService.findAll(pageable);
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDir", sortDir);
        
        return "categories/list";
    }
    
    /**
     * Просмотр категории с товарами
     */
    @GetMapping("/view/{id}")
    public String viewCategory(@PathVariable Long id,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size,
                              Model model) {
        Category category = categoryService.findById(id)
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        
        model.addAttribute("category", category);
        model.addAttribute("products", productService.findByCategory(category, pageable));
        
        return "categories/view";
    }
    
    /**
     * Форма создания категории
     */
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        return "categories/form";
    }
    
    /**
     * Создание категории
     */
    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String createCategory(@Valid @ModelAttribute("category") Category category,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            return "categories/form";
        }
        
        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("success", "Категория успешно создана");
            return "redirect:/categories";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "categories/form";
        }
    }
    
    /**
     * Форма редактирования категории
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id)
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        model.addAttribute("category", category);
        return "categories/form";
    }
    
    /**
     * Обновление категории
     */
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String updateCategory(@PathVariable Long id,
                                @Valid @ModelAttribute("category") Category category,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            return "categories/form";
        }
        
        try {
            categoryService.updateCategory(id, category);
            redirectAttributes.addFlashAttribute("success", "Категория успешно обновлена");
            return "redirect:/categories";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "categories/form";
        }
    }
    
    /**
     * Удаление категории
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Категория успешно удалена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categories";
    }
}


