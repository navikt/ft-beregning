package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class AktivitetKodeverkMappingTest {

    @Test
    void skal_verifisere_at_beregningsreglene_kjenner_alle_opptjeningsaktiviteter_i_kodeverk() {
        for (var kode : EnumSet.allOf(OpptjeningAktivitetType.class)) {
            if (!OpptjeningAktivitetType.UDEFINERT.equals(kode)) {
                assertThat(MapOpptjeningAktivitetTypeFraVLTilRegel.map(kode)).isNotNull();
            }
        }
    }
}
