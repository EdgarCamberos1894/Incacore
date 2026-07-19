package com.Incamar.IncaCore.config;

import com.Incamar.IncaCore.enums.MaintenanceOrderStatus;
import com.Incamar.IncaCore.enums.MaintenanceType;
import com.Incamar.IncaCore.enums.VesselStatus;
import com.Incamar.IncaCore.models.ItemWarehouse;
import com.Incamar.IncaCore.models.MaintenanceOrder;
import com.Incamar.IncaCore.models.ServiceTicket;
import com.Incamar.IncaCore.models.ServiceTicketDetail;
import com.Incamar.IncaCore.models.Stock;
import com.Incamar.IncaCore.models.User;
import com.Incamar.IncaCore.models.Vessel;
import com.Incamar.IncaCore.models.Warehouse;
import com.Incamar.IncaCore.repositories.ItemWarehouseRepository;
import com.Incamar.IncaCore.repositories.MaintenanceOrderRepository;
import com.Incamar.IncaCore.repositories.ServiceTicketDetailRepository;
import com.Incamar.IncaCore.repositories.ServiceTicketRepository;
import com.Incamar.IncaCore.repositories.StockRepository;
import com.Incamar.IncaCore.repositories.UserRepository;
import com.Incamar.IncaCore.repositories.VesselRepository;
import com.Incamar.IncaCore.repositories.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

    private final VesselRepository vesselRepository;
    private final WarehouseRepository warehouseRepository;
    private final ItemWarehouseRepository itemWarehouseRepository;
    private final StockRepository stockRepository;
    private final MaintenanceOrderRepository maintenanceOrderRepository;
    private final ServiceTicketRepository serviceTicketRepository;
    private final ServiceTicketDetailRepository serviceTicketDetailRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (vesselRepository.count() > 0) {
            return;
        }

        User administrator = userRepository.findByEmail("juan.perez@example.com")
                .orElseThrow(() -> new IllegalStateException("Demo administrator was not created by Flyway"));

        Vessel coastalStar = vessel("Estrella Costera", "INC-1001", "ISMM-100001", VesselStatus.OPERATIONAL, 12840.5);
        Vessel pacificRunner = vessel("Pacific Runner", "INC-1002", "ISMM-100002", VesselStatus.UNDER_MAINTENANCE, 9850.0);
        Vessel deltaMariner = vessel("Delta Mariner", "INC-1003", "ISMM-100003", VesselStatus.OUT_OF_SERVICE, 16120.0);
        vesselRepository.saveAll(List.of(coastalStar, pacificRunner, deltaMariner));

        Warehouse mainWarehouse = new Warehouse();
        mainWarehouse.setName("Almacen Principal");
        mainWarehouse.setLocation("Muelle operativo");
        warehouseRepository.save(mainWarehouse);

        ItemWarehouse oilFilter = item("Filtro de aceite", "Filtro para mantenimiento preventivo de motores");
        ItemWarehouse impeller = item("Impulsor de bomba", "Repuesto critico para sistema de enfriamiento");
        ItemWarehouse safetyKit = item("Kit de seguridad", "Equipo de proteccion y respuesta a bordo");
        itemWarehouseRepository.saveAll(List.of(oilFilter, impeller, safetyKit));

        stockRepository.saveAll(List.of(
                stock(mainWarehouse, oilFilter, 18L, 8L),
                stock(mainWarehouse, impeller, 2L, 4L),
                stock(mainWarehouse, safetyKit, 0L, 3L)
        ));

        maintenanceOrderRepository.saveAll(List.of(
                maintenance(coastalStar, administrator, MaintenanceType.PREVENTIVO, MaintenanceOrderStatus.FINALIZADO,
                        "Revision programada de motor principal", LocalDate.now().minusDays(14), LocalDate.now().minusDays(12)),
                maintenance(pacificRunner, administrator, MaintenanceType.CORRECTIVO, MaintenanceOrderStatus.EN_PROCESO,
                        "Inspeccion de sistema de enfriamiento", LocalDate.now().minusDays(2), null)
        ));

        ServiceTicket ticket = new ServiceTicket();
        ticket.setTravelNro(1001L);
        ticket.setTravelDate(LocalDate.now().minusDays(1));
        ticket.setVesselAttended(coastalStar.getName());
        ticket.setSolicitedBy("Operaciones Maritimas");
        ticket.setReportTravelNro("REP-1001");
        ticket.setStatus(true);
        ticket.setVessel(coastalStar);
        ticket.setResponsible(administrator);
        serviceTicketRepository.save(ticket);

        ServiceTicketDetail ticketDetail = new ServiceTicketDetail();
        ticketDetail.setServiceTicket(ticket);
        ticketDetail.setServiceArea("Operacion de flota");
        ticketDetail.setServiceType("Revision operativa");
        ticketDetail.setDescription("Boleta de demostracion para el recorrido del sistema");
        ticketDetail.setPatronFullName("Marco Salazar");
        ticketDetail.setMarinerFullName("Elena Ruiz");
        ticketDetail.setCaptainFullName("Carlos Mendez");
        serviceTicketDetailRepository.save(ticketDetail);
    }

    private Vessel vessel(String name, String registrationNumber, String ismm, VesselStatus status, double navigationHours) {
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
        return vessel;
    }

    private ItemWarehouse item(String name, String description) {
        ItemWarehouse item = new ItemWarehouse();
        item.setName(name);
        item.setDescription(description);
        return item;
    }

    private Stock stock(Warehouse warehouse, ItemWarehouse item, long currentStock, long minimumStock) {
        Stock stock = new Stock();
        stock.setWarehouse(warehouse);
        stock.setItemWarehouse(item);
        stock.setStock(currentStock);
        stock.setStockMin(minimumStock);
        return stock;
    }

    private MaintenanceOrder maintenance(
            Vessel vessel,
            User manager,
            MaintenanceType type,
            MaintenanceOrderStatus status,
            String reason,
            LocalDate issuedAt,
            LocalDate finishedAt
    ) {
        MaintenanceOrder order = new MaintenanceOrder();
        order.setVessel(vessel);
        order.setMaintenanceManager(manager);
        order.setMaintenanceType(type);
        order.setStatus(status);
        order.setMaintenanceReason(reason);
        order.setIssuedAt(issuedAt);
        order.setScheduledAt(issuedAt.plusDays(1));
        order.setStartedAt(issuedAt.plusDays(1));
        order.setFinishedAt(finishedAt);
        return order;
    }
}
