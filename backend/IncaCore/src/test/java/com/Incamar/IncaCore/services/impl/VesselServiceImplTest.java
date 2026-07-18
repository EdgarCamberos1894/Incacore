package com.Incamar.IncaCore.services.impl;

import com.Incamar.IncaCore.dtos.vessels.VesselRequestDto;
import com.Incamar.IncaCore.dtos.vessels.VesselResponseDto;
import com.Incamar.IncaCore.enums.VesselStatus;
import com.Incamar.IncaCore.exceptions.BadRequestException;
import com.Incamar.IncaCore.mappers.VesselMapper;
import com.Incamar.IncaCore.models.Vessel;
import com.Incamar.IncaCore.repositories.VesselRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VesselServiceImplTest {

    @Mock
    private VesselRepository vesselRepository;

    private VesselServiceImpl vesselService;

    @BeforeEach
    void setUp() {
        vesselService = new VesselServiceImpl(vesselRepository, new VesselMapper());
    }

    @Test
    void createsVesselWhenNameIsAvailable() {
        VesselRequestDto request = vesselRequest();
        when(vesselRepository.existsByName(request.getName())).thenReturn(false);
        when(vesselRepository.save(any(Vessel.class))).thenAnswer(invocation -> {
            Vessel vessel = invocation.getArgument(0);
            vessel.setId(42L);
            return vessel;
        });

        VesselResponseDto response = vesselService.createVessel(request);

        ArgumentCaptor<Vessel> savedVessel = ArgumentCaptor.forClass(Vessel.class);
        verify(vesselRepository).save(savedVessel.capture());
        assertEquals(42L, response.getId());
        assertEquals("Pacific Star", savedVessel.getValue().getName());
        assertEquals(VesselStatus.OPERATIONAL.name(), response.getStatus());
    }

    @Test
    void rejectsDuplicateVesselName() {
        VesselRequestDto request = vesselRequest();
        when(vesselRepository.existsByName(request.getName())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> vesselService.createVessel(request));

        verify(vesselRepository, never()).save(any(Vessel.class));
    }

    private VesselRequestDto vesselRequest() {
        return new VesselRequestDto(
                "Pacific Star", "ABC-1234", "ISMM-123456", "Panama", "PS-01",
                "Puerto Cabello", "J-12345678-9", "Cargo", "Steel", "Open stern",
                "Diesel", 12500.5, VesselStatus.OPERATIONAL
        );
    }
}
