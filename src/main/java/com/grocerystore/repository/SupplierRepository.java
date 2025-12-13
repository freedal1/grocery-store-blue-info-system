package com.grocerystore.repository;

import com.grocerystore.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с поставщиками
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    Optional<Supplier> findByCompanyName(String companyName);
    
    boolean existsByCompanyName(String companyName);
    
    List<Supplier> findByActiveTrue();
    
    Page<Supplier> findByActiveTrue(Pageable pageable);
    
    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Supplier> searchSuppliers(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT s FROM Supplier s ORDER BY s.companyName ASC")
    List<Supplier> findAllOrderByName();
    
    @Query("SELECT s FROM Supplier s WHERE s.active = true ORDER BY s.companyName ASC")
    List<Supplier> findAllActiveOrderByName();
}


