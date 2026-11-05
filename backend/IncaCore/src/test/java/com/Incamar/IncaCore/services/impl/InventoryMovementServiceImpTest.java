package com.Incamar.IncaCore.services.impl;

import com.Incamar.IncaCore.enums.MovementType;
import com.Incamar.IncaCore.exceptions.ResourceNotFoundException;
import com.Incamar.IncaCore.mappers.InventoryMovementMapper;
import com.Incamar.IncaCore.models.InventoryMovement;
import com.Incamar.IncaCore.models.ItemWarehouse;
import com.Incamar.IncaCore.models.MovementDetails;
import com.Incamar.IncaCore.models.User;
import com.Incamar.IncaCore.models.Warehouse;
import com.Incamar.IncaCore.repositories.InventoryMovementRepository;
import com.Incamar.IncaCore.repositories.ItemWarehouseRepository;
import com.Incamar.IncaCore.repositories.MovementDetailsRepository;
import com.Incamar.IncaCore.repositories.WarehouseRepository;
import com.Incamar.IncaCore.services.AuthService;
import com.Incamar.IncaCore.services.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceImpTest {

    @Mock private InventoryMovementRepository movements;
    @Mock private ItemWarehouseRepository items;
    @Mock private InventoryMovementMapper mapper;
    @Mock private AuthService auth;
    @Mock private WarehouseRepository warehouses;
    @Mock private MovementDetailsRepository details;
    @Mock private StockService stock;

    private InventoryMovementServiceImp service;

    @BeforeEach
    void setUp() {
        service = new InventoryMovementServiceImp(
                movements, items, mapper, auth, warehouses, details, stock
        );
    }

    @ParameterizedTest
    @EnumSource(MovementType.class)
    void deletingMovementReversesStockBeforeDeletingRecords(MovementType originalType) {
        MovementType reversal = originalType == MovementType.ENTRADA
                ? MovementType.SALIDA : MovementType.ENTRADA;
        User responsible = new User();
        InventoryMovement movement = movement(originalType, responsible);
        MovementDetails detail = detail(movement);
        when(movements.findById(5L)).thenReturn(Optional.of(movement));
        when(details.findByInventoryMovementIdOrderByIdAsc(5L)).thenReturn(List.of(detail));

        service.deleteById(5L);

        var order = org.mockito.Mockito.inOrder(stock, details, movements);
        order.verify(stock).updateStock(10L, 20L, 4L, reversal, responsible);
        order.verify(details).deleteAll(List.of(detail));
        order.verify(movements).delete(movement);
    }

    @Test
    void unknownMovementDoesNotTouchStockOrDeleteRecords() {
        when(movements.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteById(404L));

        verifyNoInteractions(stock, details);
        verify(movements, never()).deleteById(404L);
    }

    @Test
    void stockUpdateFailureStopsDeletion() {
        User responsible = new User();
        InventoryMovement movement = movement(MovementType.SALIDA, responsible);
        MovementDetails detail = detail(movement);
        when(movements.findById(5L)).thenReturn(Optional.of(movement));
        when(details.findByInventoryMovementIdOrderByIdAsc(5L)).thenReturn(List.of(detail));
        doThrow(new IllegalStateException("Stock no disponible"))
                .when(stock).updateStock(10L, 20L, 4L, MovementType.ENTRADA, responsible);

        assertThrows(IllegalStateException.class, () -> service.deleteById(5L));

        verify(details, never()).deleteAll(List.of(detail));
        verify(movements, never()).delete(movement);
    }

    private static InventoryMovement movement(MovementType type, User responsible) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(10L);
        InventoryMovement movement = new InventoryMovement();
        movement.setWarehouse(warehouse);
        movement.setMovementType(type);
        movement.setResponsible(responsible);
        return movement;
    }

    private static MovementDetails detail(InventoryMovement movement) {
        ItemWarehouse item = new ItemWarehouse();
        item.setId(20L);
        MovementDetails detail = new MovementDetails();
        detail.setInventoryMovement(movement);
        detail.setItemWarehouse(item);
        detail.setQuantity(4L);
        return detail;
    }
}
