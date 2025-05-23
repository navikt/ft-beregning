package no.nav.folketrygdloven.kalkulus.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class KontraktTest {

    @Test
    void skal_teste_at_alle_dtoer_har_nødvendig_validering() throws IOException, ClassNotFoundException {
           assertThat(ValiderKontraktDtoer.validerAlleDtoerIKontraken()).isTrue();
    }
}
