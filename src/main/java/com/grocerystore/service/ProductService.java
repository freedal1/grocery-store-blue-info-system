package com.grocerystore.service;

import com.grocerystore.entity.Category;
import com.grocerystore.entity.Product;
import com.grocerystore.entity.Supplier;
import com.grocerystore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с товарами
 */
@Service
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * Создание нового товара
     */
    public Product createProduct(Product product) {
        if (product.getBarcode() != null && productRepository.existsByBarcode(product.getBarcode())) {
            throw new RuntimeException("Товар с таким штрих-кодом уже существует");
        }
        return productRepository.save(product);
    }
    
    /**
     * Получение товара по ID
     */
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * Получение товара по штрих-коду
     */
    @Transactional(readOnly = true)
    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }
    
    /**
     * Получение всех товаров
     */
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
    
    /**
     * Получение товаров с пагинацией
     */
    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    /**
     * Получение доступных товаров
     */
    @Transactional(readOnly = true)
    public Page<Product> findAvailable(Pageable pageable) {
        return productRepository.findByAvailableTrue(pageable);
    }
    
    /**
     * Получение товаров по категории
     */
    @Transactional(readOnly = true)
    public Page<Product> findByCategory(Category category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }
    
    /**
     * Получение товаров по поставщику
     */
    @Transactional(readOnly = true)
    public Page<Product> findBySupplier(Supplier supplier, Pageable pageable) {
        return productRepository.findBySupplier(supplier, pageable);
    }
    
    /**
     * Поиск товаров
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchProducts(search.trim(), pageable);
    }
    
    /**
     * Поиск товаров в категории
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProductsInCategory(String search, Category category, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return productRepository.findByCategory(category, pageable);
        }
        return productRepository.searchProductsInCategory(search.trim(), category, pageable);
    }
    
    /**
     * Фильтрация товаров по цене
     */
    @Transactional(readOnly = true)
    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }
    
    /**
     * Комплексный поиск с фильтрами
     */
    @Transactional(readOnly = true)
    public Page<Product> findWithFilters(String search, Long categoryId, Long supplierId,
                                         BigDecimal minPrice, BigDecimal maxPrice, 
                                         Boolean available, Pageable pageable) {
        return productRepository.findWithFilters(search, categoryId, supplierId, 
                                                 minPrice, maxPrice, available, pageable);
    }
    
    /**
     * Обновление товара
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
        
        // Проверка уникальности штрих-кода при изменении
        if (updatedProduct.getBarcode() != null 
            && !updatedProduct.getBarcode().equals(existingProduct.getBarcode())
            && productRepository.existsByBarcode(updatedProduct.getBarcode())) {
            throw new RuntimeException("Товар с таким штрих-кодом уже существует");
        }
        
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setUnit(updatedProduct.getUnit());
        existingProduct.setBarcode(updatedProduct.getBarcode());
        existingProduct.setExpirationDate(updatedProduct.getExpirationDate());
        existingProduct.setManufactureDate(updatedProduct.getManufactureDate());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setSupplier(updatedProduct.getSupplier());
        existingProduct.setAvailable(updatedProduct.isAvailable());
        
        return productRepository.save(existingProduct);
    }
    
    /**
     * Обновление количества товара
     */
    public Product updateQuantity(Long id, int quantityChange) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
        
        int newQuantity = product.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new RuntimeException("Недостаточно товара на складе");
        }
        
        product.setQuantity(newQuantity);
        return productRepository.save(product);
    }
    
    /**
     * Активация/деактивация товара
     */
    public Product toggleAvailable(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
        
        product.setAvailable(!product.isAvailable());
        return productRepository.save(product);
    }
    
    /**
     * Удаление товара
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Товар не найден");
        }
        productRepository.deleteById(id);
    }
    
    /**
     * Получение товаров с низким остатком
     */
    @Transactional(readOnly = true)
    public List<Product> findLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }
    
    /**
     * Получение товаров без остатка
     */
    @Transactional(readOnly = true)
    public List<Product> findOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }
    
    /**
     * Получение истекающих товаров
     */
    @Transactional(readOnly = true)
    public List<Product> findExpiringSoonProducts(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return productRepository.findExpiringSoonProducts(futureDate);
    }
    
    /**
     * Получение просроченных товаров
     */
    @Transactional(readOnly = true)
    public List<Product> findExpiredProducts() {
        return productRepository.findExpiredProducts();
    }
    
    /**
     * Расчет общей стоимости инвентаря
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalInventoryValue() {
        BigDecimal value = productRepository.calculateTotalInventoryValue();
        return value != null ? value : BigDecimal.ZERO;
    }
    
    /**
     * Получение количества товаров
     */
    @Transactional(readOnly = true)
    public long count() {
        return productRepository.count();
    }
}


