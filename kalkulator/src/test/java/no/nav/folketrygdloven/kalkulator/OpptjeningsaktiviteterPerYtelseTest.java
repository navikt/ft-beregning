package no.nav.folketrygdloven.kalkulator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.input.OpptjeningsaktiviteterPerYtelse;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

class OpptjeningsaktiviteterPerYtelseTest {

    @Test
    void aap_relevant_for_foreldrepenger() {
        // Act
        var opptjeningsaktiviteterPerYtelse =  new OpptjeningsaktiviteterPerYtelse(FagsakYtelseType.FORELDREPENGER);
        var relevant = opptjeningsaktiviteterPerYtelse.erRelevantAktivitet(OpptjeningAktivitetType.AAP, null);

        // Assert
        assertThat(relevant).isTrue();
    }

    @Test
    void dp_relevant_for_foreldrepenger() {
        // Act
        var opptjeningsaktiviteterPerYtelse =  new OpptjeningsaktiviteterPerYtelse(FagsakYtelseType.FORELDREPENGER);
        var relevant = opptjeningsaktiviteterPerYtelse.erRelevantAktivitet(OpptjeningAktivitetType.DAGPENGER, null);

        // Assert
        assertThat(relevant).isTrue();
    }

    @Test
    void aap_ikke_relevant_for_svp() {
        // Act
        var opptjeningsaktiviteterPerYtelse = new OpptjeningsaktiviteterPerYtelse(FagsakYtelseType.SVANGERSKAPSPENGER);
        var relevant = opptjeningsaktiviteterPerYtelse.erRelevantAktivitet(OpptjeningAktivitetType.AAP, null);

        // Assert
        assertThat(relevant).isFalse();
    }

    @Test
    void dp_ikke_relevant_for_svp() {
        // Act
        var opptjeningsaktiviteterPerYtelse = new OpptjeningsaktiviteterPerYtelse(FagsakYtelseType.SVANGERSKAPSPENGER);
        var relevant = opptjeningsaktiviteterPerYtelse.erRelevantAktivitet(OpptjeningAktivitetType.DAGPENGER, null);

        // Assert
        assertThat(relevant).isFalse();
    }
}
