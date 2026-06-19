package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.OsiResponse;
import com.stepcore.business.operations.controller.dto.PortalAttachmentResponse;
import com.stepcore.business.operations.controller.dto.PortalEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigestServiceTest {

    @Mock
    private OsiService osiService;

    @Mock
    private OsiEventService osiEventService;

    private DigestService digestService;

    private static final Long OSI_ID = 1L;

    private OsiResponse buildOsi() {
        return new OsiResponse(
                OSI_ID, "OSI-2026-000001", 10L, "Acme S.A.",
                "Bogotá", "Medellín",
                null, null, null, null,
                null, null, "ACTIVE", 5L,
                OffsetDateTime.now(ZoneOffset.UTC), null, List.of());
    }

    @BeforeEach
    void setUp() {
        digestService = new DigestServiceImpl(osiService, osiEventService);
    }

    @Test
    void generateIncludesOsiHeader() {
        when(osiService.findById(OSI_ID)).thenReturn(buildOsi());
        when(osiEventService.listForPortal(OSI_ID)).thenReturn(List.of());

        final String digest = digestService.generate(OSI_ID);

        assertThat(digest).contains("OSI-2026-000001");
        assertThat(digest).contains("Acme S.A.");
        assertThat(digest).contains("Bogotá → Medellín");
        assertThat(digest).contains("En curso");
    }

    @Test
    void generateWithNoEventsShowsPlaceholder() {
        when(osiService.findById(OSI_ID)).thenReturn(buildOsi());
        when(osiEventService.listForPortal(OSI_ID)).thenReturn(List.of());

        final String digest = digestService.generate(OSI_ID);

        assertThat(digest).contains("Sin eventos disponibles");
    }

    @Test
    void generateIncludesClientVisibleEventText() {
        when(osiService.findById(OSI_ID)).thenReturn(buildOsi());

        final PortalEventResponse event = new PortalEventResponse(
                42L, "Salida desde origen",
                "El vehículo partió en buen estado.",
                OffsetDateTime.of(2026, 6, 19, 13, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 6, 19, 13, 31, 0, 0, ZoneOffset.UTC),
                BigDecimal.valueOf(4.7110), BigDecimal.valueOf(-74.0721),
                List.of());
        when(osiEventService.listForPortal(OSI_ID)).thenReturn(List.of(event));

        final String digest = digestService.generate(OSI_ID);

        assertThat(digest).contains("Salida desde origen");
        assertThat(digest).contains("El vehículo partió en buen estado.");
        assertThat(digest).doesNotContain("Sin eventos disponibles");
    }

    @Test
    void generateIncludesAttachmentUris() {
        when(osiService.findById(OSI_ID)).thenReturn(buildOsi());

        final PortalAttachmentResponse att = new PortalAttachmentResponse(1L, "foto.jpg", "https://cdn.example.com/foto.jpg", "image/jpeg");
        final PortalEventResponse event = new PortalEventResponse(
                43L, "Entrega", "Descarga completa.",
                null,
                OffsetDateTime.of(2026, 6, 19, 20, 0, 0, 0, ZoneOffset.UTC),
                null, null, List.of(att));
        when(osiEventService.listForPortal(OSI_ID)).thenReturn(List.of(event));

        final String digest = digestService.generate(OSI_ID);

        assertThat(digest).contains("foto.jpg");
        assertThat(digest).contains("https://cdn.example.com/foto.jpg");
    }

    @Test
    void generateContainsGenerationTimestamp() {
        when(osiService.findById(OSI_ID)).thenReturn(buildOsi());
        when(osiEventService.listForPortal(OSI_ID)).thenReturn(List.of());

        final String digest = digestService.generate(OSI_ID);

        assertThat(digest).contains("Generado:");
        assertThat(digest).contains("COT");
    }
}
