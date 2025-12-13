package com.grocerystore.controller;

import com.grocerystore.dto.RegistrationDto;
import com.grocerystore.entity.Role;
import com.grocerystore.entity.User;
import com.grocerystore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для аутентификации и регистрации
 */
@Controller
public class AuthController {
    
    private final UserService userService;
    
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Страница входа
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    /**
     * Форма регистрации
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new RegistrationDto());
        return "auth/register";
    }
    
    /**
     * Обработка регистрации
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") RegistrationDto dto,
                               BindingResult result,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        // Проверка валидации
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        // Проверка совпадения паролей
        if (!dto.getPassword().equals(confirmPassword)) {
            model.addAttribute("passwordError", "Пароли не совпадают");
            return "auth/register";
        }
        
        // Проверка уникальности username
        if (userService.existsByUsername(dto.getUsername())) {
            model.addAttribute("usernameError", "Пользователь с таким именем уже существует");
            return "auth/register";
        }
        
        // Проверка уникальности email
        if (userService.existsByEmail(dto.getEmail())) {
            model.addAttribute("emailError", "Пользователь с таким email уже существует");
            return "auth/register";
        }
        
        try {
            // Создаем пользователя из DTO
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setPassword(dto.getPassword());
            user.setEmail(dto.getEmail());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setPhone(dto.getPhone());
            user.setAddress(dto.getAddress());
            user.setRole(Role.CUSTOMER);
            
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Регистрация успешна! Теперь вы можете войти в систему.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "auth/register";
        }
    }
    
    /**
     * Страница отказа в доступе
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}


