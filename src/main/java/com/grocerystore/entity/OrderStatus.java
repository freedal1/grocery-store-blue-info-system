package com.grocerystore.entity;

/**
 * Перечисление статусов заказа
 */
public enum OrderStatus {
    PENDING("Ожидает обработки"),
    CONFIRMED("Подтвержден"),
    PROCESSING("В обработке"),
    READY("Готов к выдаче"),
    COMPLETED("Завершен"),
    CANCELLED("Отменен");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}


