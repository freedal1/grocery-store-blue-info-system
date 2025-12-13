package com.grocerystore.controller;

import com.grocerystore.entity.Product;
import com.grocerystore.service.CategoryService;
import com.grocerystore.service.ProductService;
import com.grocerystore.service.SupplierService;
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

import java.math.BigDecimal;

/**
 * Контроллер для работы с товарами
 */
@Controller
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    
    @Autowired
    public ProductController(ProductService productService,
                            CategoryService categoryService,
                            SupplierService supplierService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
    }
    
    /**
     * Список всех товаров с фильтрацией, поиском и сортировкой
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products;
        
        // Если есть фильтры - используем комплексный поиск
        if (search != null || categoryId != null || supplierId != null || 
            minPrice != null || maxPrice != null || available != null) {
            products = productService.findWithFilters(search, categoryId, supplierId, 
                                                      minPrice, maxPrice, available, pageable);
        } else {
            products = productService.findAll(pageable);
        }
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findAllActive());
        
        // Параметры для сохранения состояния фильтров
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentSupplierId", supplierId);
        model.addAttribute("currentMinPrice", minPrice);
        model.addAttribute("currentMaxPrice", maxPrice);
        model.addAttribute("currentAvailable", available);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDir", sortDir);
        
        return "products/list";
    }
    
    /**
     * Просмотр товара
     */
    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
        model.addAttribute("product", product);
        return "products/view";
    }
    
    /**
     * Форма создания товара
     */
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "products/form";
    }
    
    /**
     * Создание товара
     */
    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String createProduct(@Valid @ModelAttribute("product") Product product,
                               BindingResult result,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) Long supplierId,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
        
        try {
            if (categoryId != null) {
                product.setCategory(categoryService.findById(categoryId).orElse(null));
            }
            if (supplierId != null) {
                product.setSupplier(supplierService.findById(supplierId).orElse(null));
            }
            
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("success", "Товар успешно создан");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
    }
    
    /**
     * Форма редактирования товара
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findAllActive());
        return "products/form";
    }
    
    /**
     * Обновление товара
     */
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String updateProduct(@PathVariable Long id,
                               @Valid @ModelAttribute("product") Product product,
                               BindingResult result,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) Long supplierId,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
        
        try {
            if (categoryId != null) {
                product.setCategory(categoryService.findById(categoryId).orElse(null));
            }
            if (supplierId != null) {
                product.setSupplier(supplierService.findById(supplierId).orElse(null));
            }
            
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("success", "Товар успешно обновлен");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findAllActive());
            return "products/form";
        }
    }
    
    /**
     * Удаление товара
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Товар успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }
    
    /**
     * Переключение доступности товара
     */
    @PostMapping("/toggle-available/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String toggleAvailable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.toggleAvailable(id);
            String status = product.isAvailable() ? "доступен" : "недоступен";
            redirectAttributes.addFlashAttribute("success", "Товар теперь " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }
}


