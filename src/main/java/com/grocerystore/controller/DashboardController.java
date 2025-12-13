package com.grocerystore.controller;

import com.grocerystore.entity.OrderStatus;
import com.grocerystore.entity.Role;
import com.grocerystore.security.CustomUserDetails;
import com.grocerystore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

/**
 * Контроллер для главных страниц и дашборда
 */
@Controller
public class DashboardController {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final SupplierService supplierService;
    
    @Autowired
    public DashboardController(ProductService productService,
                               CategoryService categoryService,
                               OrderService orderService,
                               UserService userService,
                               SupplierService supplierService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
        this.supplierService = supplierService;
    }
    
    /**
     * Главная страница
     */
    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }
    
    /**
     * О системе
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    /**
     * Дашборд (после входа)
     */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userDetails.getUser());
        
        Role role = userDetails.getUser().getRole();
        
        switch (role) {
            case ADMIN:
                return setupAdminDashboard(model);
            case MANAGER:
                return setupManagerDashboard(model);
            case SELLER:
                return setupSellerDashboard(model);
            case CUSTOMER:
            default:
                return setupCustomerDashboard(userDetails, model);
        }
    }
    
    private String setupAdminDashboard(Model model) {
        // Статистика для администратора
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("totalCategories", categoryService.count());
        model.addAttribute("totalSuppliers", supplierService.count());
        model.addAttribute("totalOrders", orderService.count());
        
        // Количество пользователей по ролям
        model.addAttribute("customerCount", userService.countByRole(Role.CUSTOMER));
        model.addAttribute("sellerCount", userService.countByRole(Role.SELLER));
        model.addAttribute("managerCount", userService.countByRole(Role.MANAGER));
        model.addAttribute("adminCount", userService.countByRole(Role.ADMIN));
        
        // Заказы по статусам
        model.addAttribute("pendingOrders", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("completedOrders", orderService.countByStatus(OrderStatus.COMPLETED));
        
        // Последние заказы
        model.addAttribute("recentOrders", orderService.findRecentOrders(5));
        
        return "dashboard/admin";
    }
    
    private String setupManagerDashboard(Model model) {
        // Статистика для менеджера
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("totalCategories", categoryService.count());
        model.addAttribute("totalSuppliers", supplierService.count());
        model.addAttribute("totalOrders", orderService.count());
        
        // Товары требующие внимания
        model.addAttribute("lowStockProducts", productService.findLowStockProducts(10));
        model.addAttribute("expiringSoonProducts", productService.findExpiringSoonProducts(7));
        model.addAttribute("outOfStockProducts", productService.findOutOfStockProducts());
        
        // Статистика заказов
        model.addAttribute("pendingOrders", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("processingOrders", orderService.countByStatus(OrderStatus.PROCESSING));
        
        // Выручка за текущий месяц
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        model.addAttribute("monthlyRevenue", orderService.calculateRevenue(startOfMonth, LocalDateTime.now()));
        
        // Общая стоимость инвентаря
        model.addAttribute("inventoryValue", productService.calculateTotalInventoryValue());
        
        // Последние заказы
        model.addAttribute("recentOrders", orderService.findRecentOrders(10));
        
        return "dashboard/manager";
    }
    
    private String setupSellerDashboard(Model model) {
        // Заказы требующие обработки
        model.addAttribute("ordersToProcess", orderService.findOrdersRequiringProcessing());
        
        // Статистика заказов
        model.addAttribute("pendingOrders", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("confirmedOrders", orderService.countByStatus(OrderStatus.CONFIRMED));
        model.addAttribute("processingOrders", orderService.countByStatus(OrderStatus.PROCESSING));
        model.addAttribute("readyOrders", orderService.countByStatus(OrderStatus.READY));
        
        // Выручка за сегодня
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        model.addAttribute("todayRevenue", orderService.calculateRevenue(startOfDay, LocalDateTime.now()));
        
        // Последние заказы
        model.addAttribute("recentOrders", orderService.findRecentOrders(10));
        
        return "dashboard/seller";
    }
    
    private String setupCustomerDashboard(CustomUserDetails userDetails, Model model) {
        // Категории товаров
        model.addAttribute("categories", categoryService.findAll());
        
        // Популярные товары (просто последние добавленные)
        model.addAttribute("featuredProducts", productService.findAll().stream().limit(8).toList());
        
        // Заказы пользователя (последние)
        model.addAttribute("myOrders", orderService.findRecentOrders(5).stream()
            .filter(o -> o.getCustomer().getId().equals(userDetails.getId()))
            .toList());
        
        return "dashboard/customer";
    }
}


