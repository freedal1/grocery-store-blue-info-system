package com.grocerystore.controller;

import com.grocerystore.entity.Role;
import com.grocerystore.entity.User;
import com.grocerystore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для администрирования системы
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    
    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Панель администратора
     */
    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("customerCount", userService.countByRole(Role.CUSTOMER));
        model.addAttribute("sellerCount", userService.countByRole(Role.SELLER));
        model.addAttribute("managerCount", userService.countByRole(Role.MANAGER));
        model.addAttribute("adminCount", userService.countByRole(Role.ADMIN));
        return "admin/panel";
    }
    
    /**
     * Список пользователей
     */
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> users;
        if (role != null && !role.isEmpty()) {
            users = userService.findByRole(Role.valueOf(role), pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search, pageable);
        } else {
            users = userService.findAll(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("roles", Role.values());
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentRole", role);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDir", sortDir);
        
        return "admin/users";
    }
    
    /**
     * Просмотр пользователя
     */
    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin/user-view";
    }
    
    /**
     * Изменение роли пользователя
     */
    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id,
                                @RequestParam String role,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.changeRole(id, Role.valueOf(role));
            redirectAttributes.addFlashAttribute("success", "Роль пользователя изменена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }
    
    /**
     * Активация/деактивация пользователя
     */
    @PostMapping("/users/{id}/toggle-enabled")
    public String toggleUserEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.toggleEnabled(id);
            String status = user.isEnabled() ? "активирован" : "деактивирован";
            redirectAttributes.addFlashAttribute("success", "Пользователь " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }
    
    /**
     * Удаление пользователя
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Пользователь удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}


