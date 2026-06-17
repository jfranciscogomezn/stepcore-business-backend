package com.stepcore.business.operations.service;

import com.stepcore.business.exception.InvalidStateTransitionException;
import com.stepcore.business.operations.domain.model.OsiVehicleState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OsiVehicleStateMachineTest {

    private final OsiVehicleStateMachine machine = new OsiVehicleStateMachine();

    @ParameterizedTest(name = "{0} → {1} should be VALID")
    @CsvSource({
            "PLANNED, EN_RUTA",
            "EN_RUTA, EN_DESTINO",
            "EN_RUTA, INCIDENTE",
            "EN_DESTINO, DESCARGANDO",
            "EN_DESTINO, INCIDENTE",
            "DESCARGANDO, CERRADO_TRACKING",
            "DESCARGANDO, INCIDENTE",
            "INCIDENTE, EN_RUTA"
    })
    void validTransitions(final String from, final String to) {
        assertThatCode(() -> machine.validate(
                OsiVehicleState.valueOf(from), OsiVehicleState.valueOf(to)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0} → {1} should be INVALID")
    @CsvSource({
            "PLANNED, CERRADO_TRACKING",
            "PLANNED, EN_DESTINO",
            "CERRADO_TRACKING, EN_RUTA",
            "CERRADO_TRACKING, PLANNED",
            "EN_DESTINO, EN_RUTA",
            "DESCARGANDO, PLANNED"
    })
    void invalidTransitions(final String from, final String to) {
        assertThatThrownBy(() -> machine.validate(
                OsiVehicleState.valueOf(from), OsiVehicleState.valueOf(to)))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cerradoTracking_hasNoValidTransitions() {
        for (final OsiVehicleState target : OsiVehicleState.values()) {
            assertThatThrownBy(() -> machine.validate(OsiVehicleState.CERRADO_TRACKING, target))
                    .isInstanceOf(InvalidStateTransitionException.class);
        }
    }
}
