package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk.MapOpptjeningAktivitetTypeFraVLTilRegel;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class AktivitetKodeverkMappingTest {

    @Test
    public void skal_verifisere_at_beregningsreglene_kjenner_alle_opptjeningsaktiviteter_i_kodeverk() {
        for (OpptjeningAktivitetType kode : EnumSet.allOf(OpptjeningAktivitetType.class)) {
            //TODO(OJR) skal fjerne UTDANNINGSPERMISJON fra kodeverk
            if (!OpptjeningAktivitetType.UDEFINERT.equals(kode) && !kode.getKode().equals("UTDANNINGSPERMISJON")) {
                MapOpptjeningAktivitetTypeFraVLTilRegel.map(kode);
            }
        }
    }
}
