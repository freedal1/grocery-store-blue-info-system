package com.grocerystore.service;

import com.grocerystore.entity.Supplier;
import com.grocerystore.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с поставщиками
 */
@Service
@Transactional
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    
    @Autowired
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }
    
    /**
     * Создание нового поставщика
     */
    public Supplier createSupplier(Supplier supplier) {
        if (supplierRepository.existsByCompanyName(supplier.getCompanyName())) {
            throw new RuntimeException("Поставщик с таким названием уже существует");
        }
        return supplierRepository.save(supplier);
    }
    
    /**
     * Получение поставщика по ID
     */
    @Transactional(readOnly = true)
    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }
    
    /**
     * Получение поставщика по названию компании
     */
    @Transactional(readOnly = true)
    public Optional<Supplier> findByCompanyName(String companyName) {
        return supplierRepository.findByCompanyName(companyName);
    }
    
    /**
     * Получение всех поставщиков
     */
    @Transactional(readOnly = true)
    public List<Supplier> findAll() {
        return supplierRepository.findAll(Sort.by(Sort.Direction.ASC, "companyName"));
    }
    
    /**
     * Получение поставщиков с пагинацией
     */
    @Transactional(readOnly = true)
    public Page<Supplier> findAll(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }
    
    /**
     * Получение только активных поставщиков
     */
    @Transactional(readOnly = true)
    public List<Supplier> findAllActive() {
        return supplierRepository.findAllActiveOrderByName();
    }
    
    /**
     * Поиск поставщиков
     */
    @Transactional(readOnly = true)
    public Page<Supplier> searchSuppliers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return supplierRepository.findAll(pageable);
        }
        return supplierRepository.searchSuppliers(search.trim(), pageable);
    }
    
    /**
     * Обновление поставщика
     */
    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        Supplier existingSupplier = supplierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Поставщик не найден"));
        
        // Проверка уникальности названия при изменении
        if (!existingSupplier.getCompanyName().equals(updatedSupplier.getCompanyName()) 
            && supplierRepository.existsByCompanyName(updatedSupplier.getCompanyName())) {
            throw new RuntimeException("Поставщик с таким названием уже существует");
        }
        
        existingSupplier.setCompanyName(updatedSupplier.getCompanyName());
        existingSupplier.setContactPerson(updatedSupplier.getContactPerson());
        existingSupplier.setPhone(updatedSupplier.getPhone());
        existingSupplier.setEmail(updatedSupplier.getEmail());
        existingSupplier.setAddress(updatedSupplier.getAddress());
        existingSupplier.setDescription(updatedSupplier.getDescription());
        
        return supplierRepository.save(existingSupplier);
    }
    
    /**
     * Активация/деактивация поставщика
     */
    public Supplier toggleActive(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Поставщик не найден"));
        
        supplier.setActive(!supplier.isActive());
        return supplierRepository.save(supplier);
    }
    
    /**
     * Удаление поставщика
     */
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Поставщик не найден"));
        
        if (supplier.getProducts() != null && !supplier.getProducts().isEmpty()) {
            throw new RuntimeException("Невозможно удалить поставщика с товарами");
        }
        
        supplierRepository.deleteById(id);
    }
    
    /**
     * Получение количества поставщиков
     */
    @Transactional(readOnly = true)
    public long count() {
        return supplierRepository.count();
    }
}


