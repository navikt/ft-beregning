package no.nav.folketrygdloven.kalkulus.mappers;


import static no.nav.folketrygdloven.kalkulus.mappers.JsonMapperUtil.READER_JSON;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;

public class KalkulatorLegacyMapperTest {

    private record MedBeløp(Beløp beløp) {}

    @Test
    void skal_deserialisere_legacy_beløp() throws Exception {

        var beløpMedHeltallsVerdi = """
                {
                   "beløp": 101
                }
                """;

        var beløpMedDesimalVerdi = """
                {
                   "beløp": 101.23
                }
                """;

        var legacyBeløpMedHeltallsVerdi = """
                {
                   "beløp": {
                      "verdi": 101
                   }
                }
                """;

        var legacyBeløpMedDesimalVerdi = """
                {
                   "beløp": {"verdi": 987.123}
                }
                """;

        var beløpReader = READER_JSON.forType(MedBeløp.class);

        var heltall = beløpReader.readValue(beløpMedHeltallsVerdi);
        var desimal = beløpReader.readValue(beløpMedDesimalVerdi);
        var legacyHeltall = beløpReader.readValue(legacyBeløpMedHeltallsVerdi);
        var legacyDesimal = beløpReader.readValue(legacyBeløpMedDesimalVerdi);

        assertThat(heltall).isEqualTo(new MedBeløp(Beløp.fra(101)));
        assertThat(desimal).isEqualTo(new MedBeløp(Beløp.fra(new BigDecimal("101.23"))));
        assertThat(legacyHeltall).isEqualTo(new MedBeløp(Beløp.fra(101)));
        assertThat(legacyDesimal).isEqualTo(new MedBeløp(Beløp.fra(BigDecimal.valueOf(987.123d))));
    }

}
