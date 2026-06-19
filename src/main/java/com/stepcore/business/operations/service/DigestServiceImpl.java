package com.stepcore.business.operations.service;

import com.stepcore.business.operations.controller.dto.OsiResponse;
import com.stepcore.business.operations.controller.dto.PortalEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DigestServiceImpl implements DigestService {

    private static final ZoneId COT = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter EVENT_TS =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm", Locale.forLanguageTag("es-CO"));
    private static final DateTimeFormatter GEN_TS =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm", Locale.forLanguageTag("es-CO"));
    private static final String SEPARATOR = "─".repeat(48);

    private final OsiService osiService;
    private final OsiEventService osiEventService;

    @Override
    @Transactional(readOnly = true)
    public String generate(final Long osiId) {
        final OsiResponse osi = osiService.findById(osiId);
        final List<PortalEventResponse> events = osiEventService.listForPortal(osiId);

        final StringBuilder sb = new StringBuilder();
        sb.append("Seguimiento OSI #").append(osi.osiNumber()).append("\n");
        sb.append("Cliente: ").append(osi.clientName()).append("\n");
        sb.append("Ruta: ").append(osi.origin()).append(" → ").append(osi.destination()).append("\n");
        sb.append("Estado: ").append(translateStatus(osi.status())).append("\n");
        sb.append("\n");
        sb.append(SEPARATOR).append("\n");

        if (events.isEmpty()) {
            sb.append("(Sin eventos disponibles para el cliente aún.)\n");
        } else {
            for (final PortalEventResponse e : events) {
                final ZonedDateTime ts = (e.capturedAtLocal() != null ? e.capturedAtLocal() : e.receivedAt())
                        .atZoneSameInstant(COT);
                sb.append(ts.format(EVENT_TS)).append(" COT  |  ").append(e.eventTypeName()).append("\n");
                sb.append(e.text()).append("\n");
                if (e.attachments() != null && !e.attachments().isEmpty()) {
                    for (final var att : e.attachments()) {
                        sb.append("  📎 ").append(att.filename()).append(": ").append(att.uri()).append("\n");
                    }
                }
                sb.append("\n");
            }
        }

        sb.append(SEPARATOR).append("\n");
        sb.append("Generado: ")
          .append(ZonedDateTime.now(COT).format(GEN_TS))
          .append(" COT\n");

        return sb.toString();
    }

    private String translateStatus(final String status) {
        return switch (status) {
            case "DRAFT"   -> "Borrador";
            case "ACTIVE"  -> "En curso";
            case "CLOSED"  -> "Cerrada";
            default        -> status;
        };
    }
}
