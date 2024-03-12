package no.nav.folketrygdloven.kalkulator.modell.typer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class StillingsprosentTest {

    @Test
    void name() {
        var stillingsprosent = new Stillingsprosent(BigDecimal.ZERO);

        assertThat(stillingsprosent).isEqualTo(Stillingsprosent.ZERO);
    }
}
