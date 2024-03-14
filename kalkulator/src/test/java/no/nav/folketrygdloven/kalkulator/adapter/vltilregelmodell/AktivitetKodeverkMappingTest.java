package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

import static org.assertj.core.api.Assertions.assertThat;

public class AktivitetKodeverkMappingTest {

    @Test
    void skal_verifisere_at_beregningsreglene_kjenner_alle_opptjeningsaktiviteter_i_kodeverk() {
        for (OpptjeningAktivitetType kode : EnumSet.allOf(OpptjeningAktivitetType.class)) {
            if (!OpptjeningAktivitetType.UDEFINERT.equals(kode)) {
                assertThat(MapOpptjeningAktivitetTypeFraVLTilRegel.map(kode)).isNotNull();
            }
        }
    }
}
