package com.grocerystore.controller;

import com.grocerystore.entity.Supplier;
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

/**
 * Контроллер для работы с поставщиками
 */
@Controller
@RequestMapping("/suppliers")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class SupplierController {
    
    private final SupplierService supplierService;
    
    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }
    
    /**
     * Список всех поставщиков
     */
    @GetMapping
    public String listSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Supplier> suppliers;
        if (search != null && !search.trim().isEmpty()) {
            suppliers = supplierService.searchSuppliers(search, pageable);
        } else {
            suppliers = supplierService.findAll(pageable);
        }
        
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDir", sortDir);
        
        return "suppliers/list";
    }
    
    /**
     * Просмотр поставщика
     */
    @GetMapping("/view/{id}")
    public String viewSupplier(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.findById(id)
            .orElseThrow(() -> new RuntimeException("Поставщик не найден"));
        model.addAttribute("supplier", supplier);
        return "suppliers/view";
    }
    
    /**
     * Форма создания поставщика
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }
    
    /**
     * Создание поставщика
     */
    @PostMapping("/new")
    public String createSupplier(@Valid @ModelAttribute("supplier") Supplier supplier,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }
        
        try {
            supplierService.createSupplier(supplier);
            redirectAttributes.addFlashAttribute("success", "Поставщик успешно создан");
            return "redirect:/suppliers";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "suppliers/form";
        }
    }
    
    /**
     * Форма редактирования поставщика
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.findById(id)
            .orElseThrow(() -> new RuntimeException("Поставщик не найден"));
        model.addAttribute("supplier", supplier);
        return "suppliers/form";
    }
    
    /**
     * Обновление поставщика
     */
    @PostMapping("/edit/{id}")
    public String updateSupplier(@PathVariable Long id,
                                @Valid @ModelAttribute("supplier") Supplier supplier,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            return "suppliers/form";
        }
        
        try {
            supplierService.updateSupplier(id, supplier);
            redirectAttributes.addFlashAttribute("success", "Поставщик успешно обновлен");
            return "redirect:/suppliers";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "suppliers/form";
        }
    }
    
    /**
     * Удаление поставщика
     */
    @PostMapping("/delete/{id}")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("success", "Поставщик успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suppliers";
    }
    
    /**
     * Переключение активности поставщика
     */
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Supplier supplier = supplierService.toggleActive(id);
            String status = supplier.isActive() ? "активен" : "неактивен";
            redirectAttributes.addFlashAttribute("success", "Поставщик теперь " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suppliers";
    }
}


