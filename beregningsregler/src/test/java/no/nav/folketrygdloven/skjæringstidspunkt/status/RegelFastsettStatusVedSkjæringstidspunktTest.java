package no.nav.folketrygdloven.skjæringstidspunkt.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

class RegelFastsettStatusVedSkjæringstidspunktTest {


    public static final LocalDate STP_BEREGNING = LocalDate.now();

    @Test
    void skal_inkludere_aktivitet_som_slutter_dagen_før_stp() {
        //
        AktivitetStatusModell regelModell = new AktivitetStatusModell();
        regelModell.setSkjæringstidspunktForBeregning(STP_BEREGNING);
        regelModell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(
            Periode.of(STP_BEREGNING.minusMonths(12), STP_BEREGNING.minusDays(1)),
            "12356794", null));
        kjørRegel(regelModell);

        assertThat(regelModell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
    }

    @Test
    void skal_inkludere_aktivitet_som_slutter_på_stp() {
        //
        AktivitetStatusModell regelModell = new AktivitetStatusModell();
        regelModell.setSkjæringstidspunktForBeregning(STP_BEREGNING);
        regelModell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(
            Periode.of(STP_BEREGNING.minusMonths(12), STP_BEREGNING),
            "12356794", null));
        kjørRegel(regelModell);

        assertThat(regelModell.getBeregningsgrunnlagPrStatusListe()).hasSize(1);
    }

    private RegelResultat kjørRegel(AktivitetStatusModell regelModell) {
        return new RegelFastsettStatusVedSkjæringstidspunkt().evaluerRegel(regelModell);
    }
}
