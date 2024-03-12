package no.nav.folketrygdloven.kalkulus.mappers;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class KontraktTest {


    @Test
    void skal_teste_at_alle_dtoer_har_nødvendig_validering() throws IOException, ClassNotFoundException {
           ValiderKontraktDtoer.validerAlleDtoerIKontraken();
    }
}
