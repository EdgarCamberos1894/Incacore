package com.Incamar.IncaCore.repositories;

import com.Incamar.IncaCore.models.ItemWarehouse;
import com.Incamar.IncaCore.models.Stock;
import com.Incamar.IncaCore.models.Warehouse;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Page<Stock> findByItemWarehouse_NameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByItemWarehouseAndWarehouse(ItemWarehouse itemWarehouse, Warehouse warehouse);

    List<Stock> findByItemWarehouse(ItemWarehouse itemWarehouse);

    // Compare associations by reference so Hibernate uses the foreign keys directly,
    // without joins that would force a separate (stale) follow-on locking query.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.warehouse = :warehouse AND s.itemWarehouse = :itemWarehouse")
    Optional<Stock> findForUpdate(@Param("warehouse") Warehouse warehouse,
                                  @Param("itemWarehouse") ItemWarehouse itemWarehouse);
}
