package com.grocerystore.controller;

import com.grocerystore.entity.User;
import com.grocerystore.security.CustomUserDetails;
import com.grocerystore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для работы с профилем пользователя
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private final UserService userService;
    
    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Просмотр профиля
     */
    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("user", user);
        return "profile/view";
    }
    
    /**
     * Форма редактирования профиля
     */
    @GetMapping("/edit")
    public String editProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("user", user);
        return "profile/edit";
    }
    
    /**
     * Обновление профиля
     */
    @PostMapping("/edit")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @Valid @ModelAttribute("user") User updatedUser,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            return "profile/edit";
        }
        
        try {
            userService.updateUser(userDetails.getId(), updatedUser);
            redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
            return "redirect:/profile";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "profile/edit";
        }
    }
    
    /**
     * Форма смены пароля
     */
    @GetMapping("/change-password")
    public String changePasswordForm() {
        return "profile/change-password";
    }
    
    /**
     * Смена пароля
     */
    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Новые пароли не совпадают");
            return "profile/change-password";
        }
        
        if (newPassword.length() < 4) {
            model.addAttribute("error", "Пароль должен содержать минимум 4 символа");
            return "profile/change-password";
        }
        
        try {
            userService.changePassword(userDetails.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Пароль успешно изменен");
            return "redirect:/profile";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "profile/change-password";
        }
    }
}


