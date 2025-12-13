package com.grocerystore.service;

import com.grocerystore.entity.*;
import com.grocerystore.repository.OrderItemRepository;
import com.grocerystore.repository.OrderRepository;
import com.grocerystore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с заказами
 */
@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    
    @Autowired
    public OrderService(OrderRepository orderRepository, 
                       OrderItemRepository orderItemRepository,
                       ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }
    
    /**
     * Создание нового заказа
     */
    public Order createOrder(User customer) {
        Order order = new Order(customer);
        order.setDeliveryAddress(customer.getAddress());
        return orderRepository.save(order);
    }
    
    /**
     * Получение заказа по ID
     */
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    /**
     * Получение заказа по номеру
     */
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    /**
     * Получение всех заказов
     */
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    /**
     * Получение заказов с пагинацией
     */
    @Transactional(readOnly = true)
    public Page<Order> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    /**
     * Получение заказов клиента
     */
    @Transactional(readOnly = true)
    public Page<Order> findByCustomer(User customer, Pageable pageable) {
        return orderRepository.findByCustomer(customer, pageable);
    }
    
    /**
     * Получение заказов по статусу
     */
    @Transactional(readOnly = true)
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }
    
    /**
     * Поиск заказов
     */
    @Transactional(readOnly = true)
    public Page<Order> searchOrders(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.searchOrders(search.trim(), pageable);
    }
    
    /**
     * Добавление товара в заказ
     */
    public Order addItemToOrder(Long orderId, Long productId, int quantity) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Невозможно изменить заказ в текущем статусе");
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
        
        if (!product.isAvailable()) {
            throw new RuntimeException("Товар недоступен");
        }
        
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Недостаточно товара на складе");
        }
        
        OrderItem item = new OrderItem(product, quantity);
        order.addItem(item);
        
        return orderRepository.save(order);
    }
    
    /**
     * Удаление товара из заказа
     */
    public Order removeItemFromOrder(Long orderId, Long itemId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Невозможно изменить заказ в текущем статусе");
        }
        
        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Позиция не найдена"));
        
        order.removeItem(item);
        orderItemRepository.delete(item);
        
        return orderRepository.save(order);
    }
    
    /**
     * Изменение количества товара в заказе
     */
    public Order updateItemQuantity(Long orderId, Long itemId, int newQuantity) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Невозможно изменить заказ в текущем статусе");
        }
        
        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Позиция не найдена"));
        
        if (newQuantity <= 0) {
            order.removeItem(item);
            orderItemRepository.delete(item);
        } else {
            if (item.getProduct().getQuantity() < newQuantity) {
                throw new RuntimeException("Недостаточно товара на складе");
            }
            item.setQuantity(newQuantity);
            orderItemRepository.save(item);
        }
        
        order.recalculateTotal();
        return orderRepository.save(order);
    }
    
    /**
     * Изменение статуса заказа
     */
    public Order updateStatus(Long orderId, OrderStatus newStatus, User seller) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        
        if (seller != null && order.getSeller() == null) {
            order.setSeller(seller);
        }
        
        // При подтверждении заказа - списываем товары со склада
        if (newStatus == OrderStatus.CONFIRMED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Недостаточно товара: " + product.getName());
                }
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        }
        
        // При отмене заказа - возвращаем товары на склад (если уже были списаны)
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() == OrderStatus.CONFIRMED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
        
        return orderRepository.save(order);
    }
    
    /**
     * Проверка допустимости перехода между статусами
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // Определяем допустимые переходы
        switch (current) {
            case PENDING:
                if (next != OrderStatus.CONFIRMED && next != OrderStatus.CANCELLED) {
                    throw new RuntimeException("Недопустимый переход статуса");
                }
                break;
            case CONFIRMED:
                if (next != OrderStatus.PROCESSING && next != OrderStatus.CANCELLED) {
                    throw new RuntimeException("Недопустимый переход статуса");
                }
                break;
            case PROCESSING:
                if (next != OrderStatus.READY && next != OrderStatus.CANCELLED) {
                    throw new RuntimeException("Недопустимый переход статуса");
                }
                break;
            case READY:
                if (next != OrderStatus.COMPLETED && next != OrderStatus.CANCELLED) {
                    throw new RuntimeException("Недопустимый переход статуса");
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new RuntimeException("Заказ уже завершен или отменен");
        }
    }
    
    /**
     * Обновление адреса доставки
     */
    public Order updateDeliveryAddress(Long orderId, String address) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Невозможно изменить адрес в текущем статусе");
        }
        
        order.setDeliveryAddress(address);
        return orderRepository.save(order);
    }
    
    /**
     * Добавление примечания к заказу
     */
    public Order updateNotes(Long orderId, String notes) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        order.setNotes(notes);
        return orderRepository.save(order);
    }
    
    /**
     * Удаление заказа
     */
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CANCELLED) {
            throw new RuntimeException("Невозможно удалить заказ в текущем статусе");
        }
        
        orderRepository.deleteById(id);
    }
    
    /**
     * Получение последних заказов
     */
    @Transactional(readOnly = true)
    public List<Order> findRecentOrders(int limit) {
        return orderRepository.findRecentOrders(PageRequest.of(0, limit));
    }
    
    /**
     * Получение заказов требующих обработки
     */
    @Transactional(readOnly = true)
    public List<Order> findOrdersRequiringProcessing() {
        return orderRepository.findOrdersRequiringProcessing();
    }
    
    /**
     * Получение заказов за период
     */
    @Transactional(readOnly = true)
    public Page<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }
    
    /**
     * Расчет выручки за период
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal revenue = orderRepository.calculateRevenueBetweenDates(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    /**
     * Получение количества заказов по статусу
     */
    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
    
    /**
     * Получение общего количества заказов
     */
    @Transactional(readOnly = true)
    public long count() {
        return orderRepository.count();
    }
}


