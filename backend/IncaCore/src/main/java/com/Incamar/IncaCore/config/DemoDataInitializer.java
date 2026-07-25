package com.Incamar.IncaCore.config;

import com.Incamar.IncaCore.enums.ControlType;
import com.Incamar.IncaCore.enums.MaintenanceOrderStatus;
import com.Incamar.IncaCore.enums.MaintenanceType;
import com.Incamar.IncaCore.enums.MaterialType;
import com.Incamar.IncaCore.enums.MovementType;
import com.Incamar.IncaCore.enums.VesselStatus;
import com.Incamar.IncaCore.models.InventoryMovement;
import com.Incamar.IncaCore.models.ItemWarehouse;
import com.Incamar.IncaCore.models.MaintenanceOrder;
import com.Incamar.IncaCore.models.MovementDetails;
import com.Incamar.IncaCore.models.ServiceTicket;
import com.Incamar.IncaCore.models.ServiceTicketDetail;
import com.Incamar.IncaCore.models.Stock;
import com.Incamar.IncaCore.models.Travel;
import com.Incamar.IncaCore.models.User;
import com.Incamar.IncaCore.models.Vessel;
import com.Incamar.IncaCore.models.VesselItem;
import com.Incamar.IncaCore.models.VesselItemHours;
import com.Incamar.IncaCore.models.VesselItemHoursDetails;
import com.Incamar.IncaCore.models.Warehouse;
import com.Incamar.IncaCore.repositories.InventoryMovementRepository;
import com.Incamar.IncaCore.repositories.ItemWarehouseRepository;
import com.Incamar.IncaCore.repositories.MaintenanceOrderRepository;
import com.Incamar.IncaCore.repositories.MovementDetailsRepository;
import com.Incamar.IncaCore.repositories.ServiceTicketDetailRepository;
import com.Incamar.IncaCore.repositories.ServiceTicketRepository;
import com.Incamar.IncaCore.repositories.StockRepository;
import com.Incamar.IncaCore.repositories.TravelRepository;
import com.Incamar.IncaCore.repositories.UserRepository;
import com.Incamar.IncaCore.repositories.VesselItemHoursRepository;
import com.Incamar.IncaCore.repositories.VesselItemRepository;
import com.Incamar.IncaCore.repositories.VesselRepository;
import com.Incamar.IncaCore.repositories.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    private final VesselRepository vesselRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemWarehouseRepository itemWarehouseRepository;
    private final StockRepository stockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final MovementDetailsRepository movementDetailsRepository;
    private final VesselItemRepository vesselItemRepository;
    private final VesselItemHoursRepository vesselItemHoursRepository;
    private final MaintenanceOrderRepository maintenanceOrderRepository;
    private final ServiceTicketRepository serviceTicketRepository;
    private final ServiceTicketDetailRepository serviceTicketDetailRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User administrator = userRepository.findByEmail("juan.perez@example.com")
                .orElseThrow(() -> new IllegalStateException("Demo administrator was not created by Flyway"));

        Vessel coastalStar = vessel("Estrella Costera", "INC-1001", "ISMM-100001",
                VesselStatus.OPERATIONAL, 12840.5);
        Vessel pacificRunner = vessel("Pacific Runner", "INC-1002", "ISMM-100002",
                VesselStatus.UNDER_MAINTENANCE, 9850.0);
        Vessel deltaMariner = vessel("Delta Mariner", "INC-1003", "ISMM-100003",
                VesselStatus.OUT_OF_SERVICE, 16120.0);
        Vessel harborSentinel = vessel("Harbor Sentinel", "INC-1004", "ISMM-100004",
                VesselStatus.OPERATIONAL, 7340.0);
        Vessel coralVoyager = vessel("Coral Voyager", "INC-1005", "ISMM-100005",
                VesselStatus.OPERATIONAL, 11275.0);

        Warehouse mainWarehouse = warehouse("Almacen Principal", "Muelle operativo");
        Warehouse eastWarehouse = warehouse("Almacen Muelle Este", "Terminal de carga este");
        Warehouse workshopWarehouse = warehouse("Taller de Mantenimiento", "Patio tecnico central");

        ItemWarehouse oilFilter = item("Filtro de aceite", "Filtro para mantenimiento preventivo de motores");
        ItemWarehouse impeller = item("Impulsor de bomba", "Repuesto critico para sistema de enfriamiento");
        ItemWarehouse safetyKit = item("Kit de seguridad", "Equipo de proteccion y respuesta a bordo");
        ItemWarehouse engineBelt = item("Correa de motor", "Correa reforzada para motores auxiliares");
        ItemWarehouse hydraulicHose = item("Manguera hidraulica", "Linea de alta presion para equipos de cubierta");
        ItemWarehouse marineBattery = item("Bateria marina", "Bateria sellada para sistemas de respaldo");
        ItemWarehouse lubricant = item("Lubricante marino 20L", "Lubricante para motores diesel de trabajo continuo");
        ItemWarehouse fuelFilter = item("Filtro de combustible", "Elemento filtrante para linea de combustible");
        ItemWarehouse bilgeSensor = item("Sensor de sentina", "Sensor de nivel para alarma de inundacion");
        ItemWarehouse navigationLamp = item("Lampara de navegacion", "Lampara LED homologada para cubierta");
        ItemWarehouse coolant = item("Refrigerante marino", "Refrigerante concentrado para circuito cerrado");
        ItemWarehouse gasketSet = item("Juego de empaques", "Empaques para intervenciones de motor y bombas");

        stock(mainWarehouse, oilFilter, 18L, 8L);
        stock(mainWarehouse, impeller, 2L, 4L);
        stock(mainWarehouse, safetyKit, 0L, 3L);
        stock(mainWarehouse, engineBelt, 6L, 4L);
        stock(mainWarehouse, hydraulicHose, 4L, 5L);
        stock(mainWarehouse, marineBattery, 3L, 2L);
        stock(mainWarehouse, lubricant, 24L, 10L);
        stock(mainWarehouse, fuelFilter, 12L, 6L);

        stock(eastWarehouse, oilFilter, 7L, 5L);
        stock(eastWarehouse, safetyKit, 2L, 3L);
        stock(eastWarehouse, navigationLamp, 0L, 4L);
        stock(eastWarehouse, coolant, 8L, 6L);
        stock(eastWarehouse, bilgeSensor, 2L, 2L);

        stock(workshopWarehouse, impeller, 5L, 3L);
        stock(workshopWarehouse, engineBelt, 1L, 3L);
        stock(workshopWarehouse, hydraulicHose, 0L, 2L);
        stock(workshopWarehouse, marineBattery, 1L, 2L);
        stock(workshopWarehouse, gasketSet, 6L, 4L);
        stock(workshopWarehouse, coolant, 3L, 5L);

        seedMovements(administrator, mainWarehouse, eastWarehouse, workshopWarehouse,
                oilFilter, impeller, safetyKit, engineBelt, hydraulicHose, marineBattery,
                lubricant, fuelFilter, bilgeSensor, navigationLamp, coolant, gasketSet);

        VesselItem coastalEngine = vesselItem(coastalStar, "Motor principal", "Motor diesel de propulsion",
                "920.00", 12000, 1000, ControlType.NAVIGATION, MaterialType.COMPONENTS);
        VesselItem coastalBilgePump = vesselItem(coastalStar, "Bomba de sentina", "Bomba electrica de achique",
                "760.00", 4000, 800, ControlType.OWN, MaterialType.COMPONENTS);
        VesselItem coastalGenerator = vesselItem(coastalStar, "Generador auxiliar", "Generador de servicios a bordo",
                "410.00", 6000, 600, ControlType.OWN, MaterialType.COMPONENTS);
        VesselItem pacificCoolingPump = vesselItem(pacificRunner, "Bomba de enfriamiento",
                "Bomba del circuito de agua de mar", "980.00", 5000, 1000,
                ControlType.OWN, MaterialType.COMPONENTS);
        VesselItem pacificAuxEngine = vesselItem(pacificRunner, "Motor auxiliar", "Motor para servicios de cubierta",
                "1250.00", 9000, 1500, ControlType.NAVIGATION, MaterialType.COMPONENTS);
        VesselItem deltaWinch = vesselItem(deltaMariner, "Cabrestante hidraulico", "Equipo de maniobra de cubierta",
                "620.00", 3500, 700, ControlType.OWN, MaterialType.COMPONENTS);
        vesselItem(deltaMariner, "Radar de navegacion", "Radar principal de puente", "480.00", 5000, 800,
                ControlType.NAVIGATION, MaterialType.COMPONENTS);
        VesselItem harborGenerator = vesselItem(harborSentinel, "Generador principal", "Generador para operacion portuaria",
                "300.00", 7000, 700, ControlType.OWN, MaterialType.COMPONENTS);
        VesselItem coralFirePump = vesselItem(coralVoyager, "Bomba contra incendio", "Bomba dedicada de emergencia",
                "550.00", 4200, 600, ControlType.OWN, MaterialType.COMPONENTS);

        seedHours(administrator, coastalStar, pacificRunner, harborSentinel, coralVoyager,
                coastalEngine, coastalBilgePump, coastalGenerator, pacificCoolingPump,
                pacificAuxEngine, harborGenerator, coralFirePump);

        seedMaintenances(administrator, coastalStar, pacificRunner, deltaMariner, harborSentinel, coralVoyager);
        seedServiceTickets(administrator, coastalStar, pacificRunner, deltaMariner, harborSentinel, coralVoyager);
    }

    private void seedMovements(
            User administrator,
            Warehouse mainWarehouse,
            Warehouse eastWarehouse,
            Warehouse workshopWarehouse,
            ItemWarehouse oilFilter,
            ItemWarehouse impeller,
            ItemWarehouse safetyKit,
            ItemWarehouse engineBelt,
            ItemWarehouse hydraulicHose,
            ItemWarehouse marineBattery,
            ItemWarehouse lubricant,
            ItemWarehouse fuelFilter,
            ItemWarehouse bilgeSensor,
            ItemWarehouse navigationLamp,
            ItemWarehouse coolant,
            ItemWarehouse gasketSet
    ) {
        movement(mainWarehouse, MovementType.ENTRADA, LocalDate.now().minusDays(12),
                "Demo: recepcion de repuestos para mantenimiento preventivo", administrator,
                Map.of(oilFilter, 20L, lubricant, 30L, fuelFilter, 15L));
        movement(mainWarehouse, MovementType.SALIDA, LocalDate.now().minusDays(10),
                "Demo: suministro para servicio de Estrella Costera", administrator,
                Map.of(oilFilter, 4L, lubricant, 6L, fuelFilter, 3L));
        movement(workshopWarehouse, MovementType.ENTRADA, LocalDate.now().minusDays(8),
                "Demo: ingreso de componentes al taller", administrator,
                Map.of(impeller, 6L, gasketSet, 8L, hydraulicHose, 4L));
        movement(eastWarehouse, MovementType.SALIDA, LocalDate.now().minusDays(6),
                "Demo: reposicion de seguridad para Harbor Sentinel", administrator,
                Map.of(safetyKit, 2L, navigationLamp, 2L));
        movement(workshopWarehouse, MovementType.SALIDA, LocalDate.now().minusDays(5),
                "Demo: correctivo del sistema de enfriamiento Pacific Runner", administrator,
                Map.of(impeller, 2L, coolant, 4L, gasketSet, 2L));
        movement(mainWarehouse, MovementType.SALIDA, LocalDate.now().minusDays(3),
                "Demo: cambio de correa y bateria auxiliar", administrator,
                Map.of(engineBelt, 2L, marineBattery, 1L));
        movement(eastWarehouse, MovementType.ENTRADA, LocalDate.now().minusDays(2),
                "Demo: abastecimiento de sensores y refrigerante", administrator,
                Map.of(bilgeSensor, 5L, coolant, 10L));
        movement(workshopWarehouse, MovementType.SALIDA, LocalDate.now().minusDays(1),
                "Demo: reparacion hidraulica de Delta Mariner", administrator,
                Map.of(hydraulicHose, 3L, gasketSet, 1L));
    }

    private void seedHours(
            User administrator,
            Vessel coastalStar,
            Vessel pacificRunner,
            Vessel harborSentinel,
            Vessel coralVoyager,
            VesselItem coastalEngine,
            VesselItem coastalBilgePump,
            VesselItem coastalGenerator,
            VesselItem pacificCoolingPump,
            VesselItem pacificAuxEngine,
            VesselItem harborGenerator,
            VesselItem coralFirePump
    ) {
        hours(coastalStar, administrator, LocalDate.now().minusDays(6), "Demo D1: guardia Estrella Costera",
                Map.of(coastalEngine, decimal("7.5"), coastalGenerator, decimal("4.0")));
        hours(harborSentinel, administrator, LocalDate.now().minusDays(5), "Demo D2: operacion Harbor Sentinel",
                Map.of(harborGenerator, decimal("6.0")));
        hours(coastalStar, administrator, LocalDate.now().minusDays(4), "Demo D3: navegacion Estrella Costera",
                Map.of(coastalEngine, decimal("8.0"), coastalBilgePump, decimal("1.5")));
        hours(coralVoyager, administrator, LocalDate.now().minusDays(3), "Demo D4: inspeccion Coral Voyager",
                Map.of(coralFirePump, decimal("3.0")));
        hours(pacificRunner, administrator, LocalDate.now().minusDays(2), "Demo D5: prueba Pacific Runner",
                Map.of(pacificCoolingPump, decimal("2.5"), pacificAuxEngine, decimal("3.5")));
        hours(coastalStar, administrator, LocalDate.now().minusDays(1), "Demo D6: ruta costera Estrella Costera",
                Map.of(coastalEngine, decimal("9.0"), coastalGenerator, decimal("5.0")));
        hours(harborSentinel, administrator, LocalDate.now(), "Demo D7: maniobra Harbor Sentinel",
                Map.of(harborGenerator, decimal("7.0")));
    }

    private void seedMaintenances(
            User administrator,
            Vessel coastalStar,
            Vessel pacificRunner,
            Vessel deltaMariner,
            Vessel harborSentinel,
            Vessel coralVoyager
    ) {
        maintenance(coastalStar, administrator, MaintenanceType.PREVENTIVO, MaintenanceOrderStatus.FINALIZADO,
                "Revision programada de motor principal", LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(13), LocalDate.now().minusDays(13), LocalDate.now().minusDays(12));
        maintenance(coastalStar, administrator, MaintenanceType.CORRECTIVO, MaintenanceOrderStatus.FINALIZADO,
                "Demo: sustitucion de sensor de sentina", LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(29), LocalDate.now().minusDays(29), LocalDate.now().minusDays(28));
        maintenance(pacificRunner, administrator, MaintenanceType.CORRECTIVO, MaintenanceOrderStatus.EN_PROCESO,
                "Inspeccion de sistema de enfriamiento", LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1), LocalDate.now().minusDays(1), null);
        maintenance(pacificRunner, administrator, MaintenanceType.PREVENTIVO, MaintenanceOrderStatus.ESPERANDO_INSUMOS,
                "Demo: servicio de motor auxiliar pendiente de filtros", LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(3), null, null);
        maintenance(deltaMariner, administrator, MaintenanceType.CORRECTIVO, MaintenanceOrderStatus.ESPERANDO_INSUMOS,
                "Demo: reparacion de cabrestante por falta de manguera", LocalDate.now().minusDays(9),
                LocalDate.now().minusDays(7), LocalDate.now().minusDays(7), null);
        maintenance(harborSentinel, administrator, MaintenanceType.PREVENTIVO, MaintenanceOrderStatus.SOLICITADO,
                "Demo: inspeccion programada de generador principal", LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(2), null, null);
        maintenance(coralVoyager, administrator, MaintenanceType.PREVENTIVO, MaintenanceOrderStatus.FINALIZADO,
                "Demo: prueba certificada del sistema contra incendio", LocalDate.now().minusDays(20),
                LocalDate.now().minusDays(19), LocalDate.now().minusDays(19), LocalDate.now().minusDays(18));
        maintenance(coralVoyager, administrator, MaintenanceType.CORRECTIVO, MaintenanceOrderStatus.ANULADO,
                "Demo: diagnostico duplicado de bomba de cubierta", LocalDate.now().minusDays(40),
                LocalDate.now().minusDays(38), null, null);
    }

    private void seedServiceTickets(
            User administrator,
            Vessel coastalStar,
            Vessel pacificRunner,
            Vessel deltaMariner,
            Vessel harborSentinel,
            Vessel coralVoyager
    ) {
        ticket(coastalStar, administrator, 1001L, LocalDate.now().minusDays(1), "REP-1001", true,
                "Operaciones Maritimas", "Operacion de flota", "Revision operativa",
                "Boleta de demostracion para el recorrido del sistema", "Marco Salazar", "Elena Ruiz",
                "Carlos Mendez", List.of(
                        travel("Muelle Principal", "Terminal Este", "07:30", "09:10"),
                        travel("Terminal Este", "Muelle Principal", "15:20", "17:05")
                ));
        ticket(pacificRunner, administrator, 1002L, LocalDate.now().minusDays(3), "REP-1002", true,
                "Coordinacion de Mantenimiento", "Mantenimiento", "Prueba tecnica",
                "Traslado controlado para validar el circuito de enfriamiento", "Luis Andrade", "Sofia Mora",
                "Ricardo Leon", List.of(
                        travel("Taller Naval", "Zona de Pruebas", "10:00", "11:15")
                ));
        ticket(coastalStar, administrator, 1003L, LocalDate.now().minusDays(10), "REP-1003", false,
                "Logistica Portuaria", "Carga", "Apoyo logistico",
                "Servicio completado de traslado de suministros", "Marco Salazar", "Diego Perez",
                "Carlos Mendez", List.of(
                        travel("Muelle Principal", "Deposito Costero", "06:40", "08:25"),
                        travel("Deposito Costero", "Muelle Principal", "13:10", "15:00")
                ));
        ticket(harborSentinel, administrator, 1004L, LocalDate.now().minusDays(2), "REP-1004", true,
                "Control Portuario", "Puerto", "Asistencia de maniobra",
                "Apoyo a maniobra de atraque y revision de generador", "Ana Torres", "Pablo Silva",
                "Miguel Rojas", List.of(
                        travel("Darsena Norte", "Muelle Este", "08:15", "09:00")
                ));
        ticket(coralVoyager, administrator, 1005L, LocalDate.now().minusDays(15), "REP-1005", false,
                "Seguridad Maritima", "Seguridad", "Simulacro de emergencia",
                "Prueba concluida del sistema contra incendio", "Rafael Soto", "Laura Gil",
                "Andrea Campos", List.of(
                        travel("Muelle Principal", "Bahia de Pruebas", "09:20", "10:30"),
                        travel("Bahia de Pruebas", "Muelle Principal", "12:00", "13:10")
                ));
        ticket(deltaMariner, administrator, 1006L, LocalDate.now().minusDays(6), "REP-1006", false,
                "Taller Naval", "Mantenimiento", "Traslado a taller",
                "Ingreso a patio tecnico por falla del cabrestante", "Jose Molina", "Nora Vargas",
                "Esteban Cruz", List.of(
                        travel("Muelle Sur", "Taller Naval", "07:00", "08:05")
                ));
    }

    private Vessel vessel(String name, String registrationNumber, String ismm, VesselStatus status, double navigationHours) {
        return vesselRepository.findByName(name).orElseGet(() -> {
            Vessel vessel = new Vessel();
            vessel.setName(name);
            vessel.setRegistrationNumber(registrationNumber);
            vessel.setIsmm(ismm);
            vessel.setFlagState("Panama");
            vessel.setCallSign(registrationNumber);
            vessel.setPortOfRegistry("Puerto Cabello");
            vessel.setRif("J-12345678-9");
            vessel.setServiceType("Carga costera");
            vessel.setConstructionMaterial("Acero");
            vessel.setSternType("Popa abierta");
            vessel.setFuelType("Diesel");
            vessel.setNavigationHours(navigationHours);
            vessel.setStatus(status);
            return vesselRepository.save(vessel);
        });
    }

    private Warehouse warehouse(String name, String location) {
        return warehouseRepository.findByName(name).orElseGet(() -> {
            Warehouse warehouse = new Warehouse();
            warehouse.setName(name);
            warehouse.setLocation(location);
            return warehouseRepository.save(warehouse);
        });
    }

    private ItemWarehouse item(String name, String description) {
        ItemWarehouse existing = itemWarehouseRepository.findItemWarehouseByName(name);
        if (existing != null) {
            return existing;
        }
        ItemWarehouse item = new ItemWarehouse();
        item.setName(name);
        item.setDescription(description);
        return itemWarehouseRepository.save(item);
    }

    private void stock(Warehouse warehouse, ItemWarehouse item, long currentStock, long minimumStock) {
        if (stockRepository.existsByItemWarehouseAndWarehouse(item, warehouse)) {
            return;
        }
        Stock stock = new Stock();
        stock.setWarehouse(warehouse);
        stock.setItemWarehouse(item);
        stock.setStock(currentStock);
        stock.setStockMin(minimumStock);
        stockRepository.save(stock);
    }

    private void movement(
            Warehouse warehouse,
            MovementType type,
            LocalDate date,
            String reason,
            User responsible,
            Map<ItemWarehouse, Long> quantities
    ) {
        if (inventoryMovementRepository.existsByReason(reason)) {
            return;
        }
        InventoryMovement movement = new InventoryMovement();
        movement.setWarehouse(warehouse);
        movement.setMovementType(type);
        movement.setDate(date);
        movement.setReason(reason);
        movement.setResponsible(responsible);
        inventoryMovementRepository.save(movement);

        List<MovementDetails> details = quantities.entrySet().stream().map(entry -> {
            MovementDetails detail = new MovementDetails();
            detail.setInventoryMovement(movement);
            detail.setItemWarehouse(entry.getKey());
            detail.setQuantity(entry.getValue());
            return detail;
        }).toList();
        movementDetailsRepository.saveAll(details);
    }

    private VesselItem vesselItem(
            Vessel vessel,
            String name,
            String description,
            String accumulatedHours,
            int usefulLifeHours,
            int alertHours,
            ControlType controlType,
            MaterialType materialType
    ) {
        return vesselItemRepository.findByVesselIdAndName(vessel.getId(), name).orElseGet(() -> {
            VesselItem item = new VesselItem();
            item.setVessel(vessel);
            item.setName(name);
            item.setDescription(description);
            item.setAccumulatedHours(decimal(accumulatedHours));
            item.setUsefulLifeHours(usefulLifeHours);
            item.setAlertHours(alertHours);
            item.setControlType(controlType);
            item.setMaterialType(materialType);
            return vesselItemRepository.save(item);
        });
    }

    private void hours(
            Vessel vessel,
            User responsible,
            LocalDate date,
            String description,
            Map<VesselItem, BigDecimal> assignedHours
    ) {
        if (vesselItemHoursRepository.existsByDescription(description)) {
            return;
        }
        VesselItemHours hours = new VesselItemHours();
        hours.setVessel(vessel);
        hours.setResponsable(responsible);
        hours.setDate(date);
        hours.setDescription(description);

        List<VesselItemHoursDetails> details = assignedHours.entrySet().stream().map(entry -> {
            VesselItemHoursDetails detail = new VesselItemHoursDetails();
            detail.setVesselItemHours(hours);
            detail.setVesselItem(entry.getKey());
            detail.setAssignedHours(entry.getValue());
            return detail;
        }).toList();
        hours.setItems(details);
        vesselItemHoursRepository.save(hours);
    }

    private void maintenance(
            Vessel vessel,
            User manager,
            MaintenanceType type,
            MaintenanceOrderStatus status,
            String reason,
            LocalDate issuedAt,
            LocalDate scheduledAt,
            LocalDate startedAt,
            LocalDate finishedAt
    ) {
        if (maintenanceOrderRepository.existsByMaintenanceReason(reason)) {
            return;
        }
        MaintenanceOrder order = new MaintenanceOrder();
        order.setVessel(vessel);
        order.setMaintenanceManager(manager);
        order.setMaintenanceType(type);
        order.setStatus(status);
        order.setMaintenanceReason(reason);
        order.setIssuedAt(issuedAt);
        order.setScheduledAt(scheduledAt);
        order.setStartedAt(startedAt);
        order.setFinishedAt(finishedAt);
        maintenanceOrderRepository.save(order);
    }

    private void ticket(
            Vessel vessel,
            User responsible,
            long travelNumber,
            LocalDate travelDate,
            String reportNumber,
            boolean open,
            String requestedBy,
            String serviceArea,
            String serviceType,
            String description,
            String patron,
            String mariner,
            String captain,
            List<TravelSeed> travelSeeds
    ) {
        ServiceTicket ticket = serviceTicketRepository.findByReportTravelNro(reportNumber).orElseGet(() -> {
            ServiceTicket created = new ServiceTicket();
            created.setTravelNro(travelNumber);
            created.setTravelDate(travelDate);
            created.setVesselAttended(vessel.getName());
            created.setSolicitedBy(requestedBy);
            created.setReportTravelNro(reportNumber);
            created.setStatus(open);
            created.setVessel(vessel);
            created.setResponsible(responsible);
            return serviceTicketRepository.save(created);
        });

        ServiceTicketDetail detail = serviceTicketDetailRepository.findByServiceTicket_Id(ticket.getId())
                .orElseGet(() -> {
                    ServiceTicketDetail created = new ServiceTicketDetail();
                    created.setServiceTicket(ticket);
                    created.setServiceArea(serviceArea);
                    created.setServiceType(serviceType);
                    created.setDescription(description);
                    created.setPatronFullName(patron);
                    created.setMarinerFullName(mariner);
                    created.setCaptainFullName(captain);
                    return serviceTicketDetailRepository.save(created);
                });

        if (travelRepository.existsByServiceTicketDetail_Id(detail.getId())) {
            return;
        }
        List<Travel> travels = travelSeeds.stream().map(seed -> Travel.builder()
                .origin(seed.origin())
                .destination(seed.destination())
                .departureTime(seed.departureTime())
                .arrivalTime(seed.arrivalTime())
                .serviceTicketDetail(detail)
                .build()).toList();
        travelRepository.saveAll(travels);
    }

    private TravelSeed travel(String origin, String destination, String departureTime, String arrivalTime) {
        return new TravelSeed(origin, destination, LocalTime.parse(departureTime), LocalTime.parse(arrivalTime));
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private record TravelSeed(
            String origin,
            String destination,
            LocalTime departureTime,
            LocalTime arrivalTime
    ) {
    }
}
