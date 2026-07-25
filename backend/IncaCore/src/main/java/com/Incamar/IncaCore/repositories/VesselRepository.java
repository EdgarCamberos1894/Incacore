package com.Incamar.IncaCore.repositories;

import com.Incamar.IncaCore.models.Vessel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VesselRepository extends JpaRepository<Vessel,Long> {
    boolean existsById(Long id);
    boolean existsByName(String name);
    Optional<Vessel> findByName(String name);
    Page<Vessel> findByNameContainingIgnoreCase(String nameParcial, Pageable pageable);


}
