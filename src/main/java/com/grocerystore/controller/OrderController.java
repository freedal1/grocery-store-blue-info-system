package com.grocerystore.controller;

import com.grocerystore.entity.Order;
import com.grocerystore.entity.OrderStatus;
import com.grocerystore.entity.User;
import com.grocerystore.security.CustomUserDetails;
import com.grocerystore.service.OrderService;
import com.grocerystore.service.ProductService;
import com.grocerystore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для работы с заказами
 */
@Controller
@RequestMapping("/orders")
public class OrderController {
    
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;
    
    @Autowired
    public OrderController(OrderService orderService, 
                          ProductService productService,
                          UserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }
    
    /**
     * Список всех заказов (для менеджеров и админов)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SELLER', 'MANAGER', 'ADMIN')")
    public String listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.findByStatus(OrderStatus.valueOf(status), pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            orders = orderService.searchOrders(search, pageable);
        } else {
            orders = orderService.findAll(pageable);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDir", sortDir);
        
        return "orders/list";
    }
    
    /**
     * Мои заказы (для покупателей)
     */
    @GetMapping("/my")
    public String myOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        User customer = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.findByCustomer(customer, pageable);
        
        model.addAttribute("orders", orders);
        
        return "orders/my-orders";
    }
    
    /**
     * Просмотр заказа
     */
    @GetMapping("/view/{id}")
    public String viewOrder(@PathVariable Long id, 
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        // Проверка доступа - покупатель видит только свои заказы
        User currentUser = userDetails.getUser();
        if (currentUser.getRole().name().equals("CUSTOMER") && 
            !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Доступ запрещен");
        }
        
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        
        return "orders/view";
    }
    
    /**
     * Создание нового заказа
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String showCreateForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("products", productService.findAvailable(PageRequest.of(0, 100)));
        return "orders/create";
    }
    
    /**
     * Создание заказа
     */
    @PostMapping("/new")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String createOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.createOrder(userDetails.getUser());
            redirectAttributes.addFlashAttribute("success", "Заказ создан. Добавьте товары.");
            return "redirect:/orders/edit/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/my";
        }
    }
    
    /**
     * Редактирование заказа (добавление товаров)
     */
    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        // Проверка доступа
        if (!order.getCustomer().getId().equals(userDetails.getId()) &&
            userDetails.getUser().getRole().name().equals("CUSTOMER")) {
            throw new RuntimeException("Доступ запрещен");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Заказ нельзя редактировать");
        }
        
        model.addAttribute("order", order);
        model.addAttribute("products", productService.findAvailable(PageRequest.of(0, 100)));
        
        return "orders/edit";
    }
    
    /**
     * Добавление товара в заказ
     */
    @PostMapping("/{orderId}/add-item")
    public String addItemToOrder(@PathVariable Long orderId,
                                @RequestParam Long productId,
                                @RequestParam int quantity,
                                RedirectAttributes redirectAttributes) {
        try {
            orderService.addItemToOrder(orderId, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Товар добавлен в заказ");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/edit/" + orderId;
    }
    
    /**
     * Удаление товара из заказа
     */
    @PostMapping("/{orderId}/remove-item/{itemId}")
    public String removeItemFromOrder(@PathVariable Long orderId,
                                     @PathVariable Long itemId,
                                     RedirectAttributes redirectAttributes) {
        try {
            orderService.removeItemFromOrder(orderId, itemId);
            redirectAttributes.addFlashAttribute("success", "Товар удален из заказа");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/edit/" + orderId;
    }
    
    /**
     * Обновление адреса доставки
     */
    @PostMapping("/{orderId}/update-address")
    public String updateDeliveryAddress(@PathVariable Long orderId,
                                       @RequestParam String address,
                                       RedirectAttributes redirectAttributes) {
        try {
            orderService.updateDeliveryAddress(orderId, address);
            redirectAttributes.addFlashAttribute("success", "Адрес обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/edit/" + orderId;
    }
    
    /**
     * Подтверждение заказа покупателем
     */
    @PostMapping("/{orderId}/submit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String submitOrder(@PathVariable Long orderId,
                             RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
            
            if (order.getItems().isEmpty()) {
                throw new RuntimeException("Заказ пуст. Добавьте товары.");
            }
            
            // Покупатель отправляет заказ (статус остается PENDING, ждет подтверждения продавца)
            redirectAttributes.addFlashAttribute("success", "Заказ отправлен на обработку");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/my";
    }
    
    /**
     * Изменение статуса заказа (для продавцов/менеджеров)
     */
    @PostMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'MANAGER', 'ADMIN')")
    public String updateOrderStatus(@PathVariable Long orderId,
                                   @RequestParam String status,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(orderId, OrderStatus.valueOf(status), userDetails.getUser());
            redirectAttributes.addFlashAttribute("success", "Статус заказа обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/view/" + orderId;
    }
    
    /**
     * Отмена заказа
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(orderId, OrderStatus.CANCELLED, userDetails.getUser());
            redirectAttributes.addFlashAttribute("success", "Заказ отменен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/view/" + orderId;
    }
    
    /**
     * Удаление заказа
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", "Заказ удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders";
    }
}


