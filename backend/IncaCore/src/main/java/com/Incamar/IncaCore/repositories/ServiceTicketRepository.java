package com.Incamar.IncaCore.repositories;

import com.Incamar.IncaCore.models.ServiceTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceTicketRepository extends JpaRepository<ServiceTicket,Long> {

    Optional<ServiceTicket> findByReportTravelNro(String reportTravelNro);

    Page<ServiceTicket> findBySolicitedByContainingIgnoreCase(String solicitor, Pageable pageable);
    Page<ServiceTicket> findByVesselAttendedContainingIgnoreCase(String vesselAttended, Pageable pageable);
    Page<ServiceTicket> findByVessel_NameContainingIgnoreCase(String name, Pageable pageable);

}
