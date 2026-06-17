package com.stepcore.business.operations.service;

import com.stepcore.business.exception.InvalidStateTransitionException;
import com.stepcore.business.operations.domain.model.OsiVehicleState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Encodes all valid state transitions for an OSI–vehicle assignment.
 * Any call with an invalid (from, to) pair throws {@link InvalidStateTransitionException}.
 */
@Component
public class OsiVehicleStateMachine {

    private static final Map<OsiVehicleState, Set<OsiVehicleState>> TRANSITIONS = Map.of(
            OsiVehicleState.PLANNED,           Set.of(OsiVehicleState.EN_RUTA),
            OsiVehicleState.EN_RUTA,           Set.of(OsiVehicleState.EN_DESTINO, OsiVehicleState.INCIDENTE),
            OsiVehicleState.EN_DESTINO,        Set.of(OsiVehicleState.DESCARGANDO, OsiVehicleState.INCIDENTE),
            OsiVehicleState.DESCARGANDO,       Set.of(OsiVehicleState.CERRADO_TRACKING, OsiVehicleState.INCIDENTE),
            OsiVehicleState.INCIDENTE,         Set.of(OsiVehicleState.EN_RUTA),
            OsiVehicleState.CERRADO_TRACKING,  Set.of()
    );

    public void validate(final OsiVehicleState from, final OsiVehicleState to) {
        final Set<OsiVehicleState> allowed = TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition from " + from + " to " + to);
        }
    }
}
