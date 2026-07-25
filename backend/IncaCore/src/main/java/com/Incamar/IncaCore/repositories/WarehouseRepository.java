package com.Incamar.IncaCore.repositories;

import com.Incamar.IncaCore.models.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse,Long> {
    boolean existsById(Long id);
    boolean existsByName(String name);
    Optional<Warehouse> findByName(String name);
    Page<Warehouse> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
