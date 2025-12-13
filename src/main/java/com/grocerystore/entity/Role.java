package com.grocerystore.entity;

/**
 * Перечисление ролей пользователей системы
 */
public enum Role {
    CUSTOMER("Покупатель"),
    SELLER("Продавец"),
    MANAGER("Менеджер"),
    ADMIN("Администратор");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}


