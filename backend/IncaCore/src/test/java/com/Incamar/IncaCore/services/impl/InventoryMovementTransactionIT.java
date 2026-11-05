package com.Incamar.IncaCore.services.impl;

import com.Incamar.IncaCore.dtos.inventory.InventoryMovementRequestDto;
import com.Incamar.IncaCore.dtos.inventory.InventoryMovementResponseDto;
import com.Incamar.IncaCore.dtos.inventory.MovementDetailsRequestDto;
import com.Incamar.IncaCore.enums.AccountStatus;
import com.Incamar.IncaCore.enums.MovementType;
import com.Incamar.IncaCore.enums.Role;
import com.Incamar.IncaCore.exceptions.ResourceNotFoundException;
import com.Incamar.IncaCore.mappers.InventoryMovementMapper;
import com.Incamar.IncaCore.mappers.StockMapper;
import com.Incamar.IncaCore.models.InventoryMovement;
import com.Incamar.IncaCore.models.ItemWarehouse;
import com.Incamar.IncaCore.models.MovementDetails;
import com.Incamar.IncaCore.models.Stock;
import com.Incamar.IncaCore.models.User;
import com.Incamar.IncaCore.models.Warehouse;
import com.Incamar.IncaCore.repositories.InventoryMovementRepository;
import com.Incamar.IncaCore.repositories.ItemWarehouseRepository;
import com.Incamar.IncaCore.repositories.MovementDetailsRepository;
import com.Incamar.IncaCore.repositories.StockRepository;
import com.Incamar.IncaCore.repositories.UserRepository;
import com.Incamar.IncaCore.repositories.WarehouseRepository;
import com.Incamar.IncaCore.services.AuthService;
import com.Incamar.IncaCore.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Tests real repository commits and rollbacks against PostgreSQL, not mocked JPA calls.
 * The test itself must NOT be transactional: otherwise @DataJpaTest would mask a
 * missing transaction on the service under test.
 */
@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({InventoryMovementServiceImp.class, StockServiceImp.class})
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class InventoryMovementTransactionIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("incacore_integration")
            .withInitScript("db/test-schema.sql");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private InventoryMovementServiceImp inventoryMovementService;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private MovementDetailsRepository movementDetailsRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ItemWarehouseRepository itemWarehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private InventoryMovementMapper inventoryMovementMapper;

    @MockitoBean
    private StockMapper stockMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailService emailService;

    private Warehouse warehouse;
    private ItemWarehouse firstItem;
    private ItemWarehouse secondItem;
    private Stock firstStock;
    private User responsible;

    @BeforeEach
    void setUp() {
        String unique = UUID.randomUUID().toString();

        responsible = new User();
        responsible.setEmail("integration-" + unique + "@example.test");
        responsible.setPassword("unused-in-integration-test");
        responsible.setRole(Role.ADMIN);
        responsible.setAccountStatus(AccountStatus.ACTIVE);
        responsible = userRepository.saveAndFlush(responsible);

        warehouse = new Warehouse();
        warehouse.setName("Warehouse-" + unique);
        warehouse.setLocation("Integration test");
        warehouse = warehouseRepository.saveAndFlush(warehouse);

        firstItem = itemWarehouseRepository.saveAndFlush(new ItemWarehouse(
                null, "First-" + unique, "Has stock"));
        secondItem = itemWarehouseRepository.saveAndFlush(new ItemWarehouse(
                null, "Second-" + unique, "Stock deliberately absent"));

        firstStock = new Stock();
        firstStock.setWarehouse(warehouse);
        firstStock.setItemWarehouse(firstItem);
        firstStock.setStock(10L);
        firstStock.setStockMin(0L);
        firstStock = stockRepository.saveAndFlush(firstStock);
    }

    @Test
    void createRollsBackMovementAndFirstStockUpdateWhenLaterStockIsMissing() {
        mockCreationMapping();
        InventoryMovementRequestDto request = requestWithDetails(
                new MovementDetailsRequestDto(firstItem.getId(), 4L),
                new MovementDetailsRequestDto(secondItem.getId(), 2L));
        long detailCountBefore = movementDetailsRepository.count();

        assertThatThrownBy(() -> inventoryMovementService.createInventory(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Stock no encontrado");

        assertThat(reloadFirstStock()).isEqualTo(10L);
        assertThat(inventoryMovementRepository.existsByReason(request.getReason())).isFalse();
        assertThat(movementDetailsRepository.count()).isEqualTo(detailCountBefore);
    }

    @Test
    void createCommitsMovementDetailsAndStockWhenEverythingExists() {
        mockCreationMapping();
        InventoryMovementRequestDto request = requestWithDetails(
                new MovementDetailsRequestDto(firstItem.getId(), 4L));

        inventoryMovementService.createInventory(request);

        assertThat(reloadFirstStock()).isEqualTo(14L);
        InventoryMovement movement = inventoryMovementRepository.findAll().stream()
                .filter(candidate -> candidate.getReason().equals(request.getReason()))
                .findFirst().orElseThrow();
        assertThat(movementDetailsRepository.findByInventoryMovementId(movement.getId()))
                .singleElement()
                .satisfies(detail -> assertThat(detail.getQuantity()).isEqualTo(4L));
    }

    @Test
    void deleteRollsBackFirstStockReversalWhenLaterStockIsMissing() {
        InventoryMovement movement = persistedMovement(MovementType.SALIDA, firstItem, secondItem);

        assertThatThrownBy(() -> inventoryMovementService.deleteById(movement.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Stock no encontrado");

        assertThat(reloadFirstStock()).isEqualTo(10L);
        assertThat(inventoryMovementRepository.existsById(movement.getId())).isTrue();
        assertThat(movementDetailsRepository.findByInventoryMovementId(movement.getId())).hasSize(2);
    }

    @Test
    void deleteCommitsStockReversalAndRemovesMovementAndDetails() {
        InventoryMovement movement = persistedMovement(MovementType.SALIDA, firstItem);

        inventoryMovementService.deleteById(movement.getId());

        assertThat(reloadFirstStock()).isEqualTo(14L);
        assertThat(inventoryMovementRepository.existsById(movement.getId())).isFalse();
        assertThat(movementDetailsRepository.findByInventoryMovementId(movement.getId())).isEmpty();
    }

    private Long reloadFirstStock() {
        return stockRepository.findById(firstStock.getId()).orElseThrow().getStock();
    }

    private InventoryMovementRequestDto requestWithDetails(MovementDetailsRequestDto... details) {
        return new InventoryMovementRequestDto(warehouse.getId(), MovementType.ENTRADA,
                LocalDate.now(), "PostgreSQL transaction test " + UUID.randomUUID(), List.of(details));
    }

    private void mockCreationMapping() {
        when(authService.getAuthenticatedUser()).thenReturn(Optional.of(responsible));
        when(inventoryMovementMapper.toEntity(any(), any(), any())).thenAnswer(invocation -> {
            InventoryMovementRequestDto request = invocation.getArgument(0);
            InventoryMovement movement = new InventoryMovement();
            movement.setWarehouse(invocation.getArgument(1));
            movement.setResponsible(invocation.getArgument(2));
            movement.setMovementType(request.getMovementType());
            movement.setDate(request.getDate());
            movement.setReason(request.getReason());
            return movement;
        });
        when(inventoryMovementMapper.toResponseDto(any(), anyList()))
                .thenReturn(new InventoryMovementResponseDto());
    }

    private InventoryMovement persistedMovement(MovementType type, ItemWarehouse... items) {
        InventoryMovement movement = new InventoryMovement();
        movement.setWarehouse(warehouse);
        movement.setResponsible(responsible);
        movement.setMovementType(type);
        movement.setDate(LocalDate.now());
        movement.setReason("PostgreSQL delete transaction test");
        movement = inventoryMovementRepository.saveAndFlush(movement);

        for (ItemWarehouse item : items) {
            MovementDetails detail = new MovementDetails();
            detail.setInventoryMovement(movement);
            detail.setItemWarehouse(item);
            detail.setQuantity(4L);
            movementDetailsRepository.saveAndFlush(detail);
        }
        return movement;
    }
}
