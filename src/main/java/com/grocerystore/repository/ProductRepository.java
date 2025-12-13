package com.grocerystore.repository;

import com.grocerystore.entity.Category;
import com.grocerystore.entity.Product;
import com.grocerystore.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с товарами
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByBarcode(String barcode);
    
    boolean existsByBarcode(String barcode);
    
    List<Product> findByCategory(Category category);
    
    Page<Product> findByCategory(Category category, Pageable pageable);
    
    List<Product> findBySupplier(Supplier supplier);
    
    Page<Product> findBySupplier(Supplier supplier, Pageable pageable);
    
    List<Product> findByAvailableTrue();
    
    Page<Product> findByAvailableTrue(Pageable pageable);
    
    // Поиск по названию и описанию
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);
    
    // Поиск в категории
    @Query("SELECT p FROM Product p WHERE p.category = :category AND (" +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProductsInCategory(@Param("search") String search, 
                                           @Param("category") Category category, 
                                           Pageable pageable);
    
    // Фильтрация по цене
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice, 
                                   Pageable pageable);
    
    // Товары с низким остатком
    @Query("SELECT p FROM Product p WHERE p.quantity <= :threshold AND p.quantity > 0")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
    
    // Товары без остатка
    @Query("SELECT p FROM Product p WHERE p.quantity = 0 OR p.quantity IS NULL")
    List<Product> findOutOfStockProducts();
    
    // Истекающие товары
    @Query("SELECT p FROM Product p WHERE p.expirationDate <= :date AND p.expirationDate >= CURRENT_DATE")
    List<Product> findExpiringSoonProducts(@Param("date") LocalDate date);
    
    // Просроченные товары
    @Query("SELECT p FROM Product p WHERE p.expirationDate < CURRENT_DATE")
    List<Product> findExpiredProducts();
    
    // Комплексный поиск с фильтрами
    @Query("SELECT p FROM Product p WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:supplierId IS NULL OR p.supplier.id = :supplierId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:available IS NULL OR p.available = :available)")
    Page<Product> findWithFilters(@Param("search") String search,
                                  @Param("categoryId") Long categoryId,
                                  @Param("supplierId") Long supplierId,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  @Param("available") Boolean available,
                                  Pageable pageable);
    
    // Статистика
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
    long countByCategory(@Param("category") Category category);
    
    @Query("SELECT SUM(p.quantity * p.price) FROM Product p WHERE p.available = true")
    BigDecimal calculateTotalInventoryValue();
}


