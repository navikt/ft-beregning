package no.nav.folketrygdloven.skjæringstidspunkt.status;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;

class FastsettStatusOgAndelPrPeriodeTest {


    public static final LocalDate STP = LocalDate.now();

    @Test
    void skal_lage_andel_for_enkelt_arbeidsforhold() {
        // Arrange
        AktivitetStatusModell regelmodell = new AktivitetStatusModell();
        regelmodell.setSkjæringstidspunktForBeregning(STP);
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(STP.minusMonths(36), STP.plusMonths(12)), "999999999", null));

        // Act
        List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

        // Assert
        assertThat(statusListe.size()).isEqualTo(1);
        assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
		verifiserArbeidsforhold(statusListe.get(0).getArbeidsforholdList(), "999999999");
    }

	@Test
	void skal_lage_andel_for_flere_arbeidsforhold() {
		// Arrange
		AktivitetStatusModell regelmodell = new AktivitetStatusModell();
		regelmodell.setSkjæringstidspunktForBeregning(STP);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(STP.minusMonths(36), STP.plusMonths(12)), "999999999", null));
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosPrivatperson(Periode.of(STP.minusMonths(36), STP.plusMonths(12)), "8888888888888"));

		// Act
		List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

		// Assert
		assertThat(statusListe.size()).isEqualTo(1);
		assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
		verifiserArbeidsforhold(statusListe.get(0).getArbeidsforholdList(), "999999999");
		verifiserArbeidsforhold(statusListe.get(0).getArbeidsforholdList(), "8888888888888");
	}

	@Test
	void skal_ikke_lage_andel_for_aktiviteter_som_ikke_var_aktiv_på_beregningstidspunkt() {
		// Arrange
		AktivitetStatusModell regelmodell = new AktivitetStatusModell();
		regelmodell.setSkjæringstidspunktForBeregning(STP);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.FRILANSINNTEKT, Periode.of(STP.minusMonths(36), STP.minusDays(2))));

		// Act
		List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

		// Assert
		assertThat(statusListe.size()).isEqualTo(1);
		assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
	}

	@Test
	void skal_oversette_sykepenger_til_brukers_andel_når_det_er_eneste_aktivitet() {
		// Arrange
		AktivitetStatusModell regelmodell = new AktivitetStatusModell();
		regelmodell.setSkjæringstidspunktForBeregning(STP);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.SYKEPENGER_MOTTAKER, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

		// Act
		List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

		// Assert
		assertThat(statusListe.size()).isEqualTo(1);
		assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BA);
	}

	@Test
	void skal_oversette_aktivitet_til_dagpenger() {
		// Arrange
		AktivitetStatusModell regelmodell = new AktivitetStatusModell();
		regelmodell.setSkjæringstidspunktForBeregning(STP);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.PLEIEPENGER_AV_DAGPENGER_MOTTAKER, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.SYKEPENGER_AV_DAGPENGER_MOTTAKER, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

		// Act
		List<BeregningsgrunnlagPrStatus> statusListe = kjørRegel(regelmodell);

		// Assert
		assertThat(statusListe.size()).isEqualTo(1);
		assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.DP);
	}

	private void verifiserArbeidsforhold(List<Arbeidsforhold> arbeidsforholdList, String arbeidsgiverIdent) {
		var matchetAF = arbeidsforholdList.stream()
				.filter(af -> af.getArbeidsgiverId() != null && af.getArbeidsgiverId().equals(arbeidsgiverIdent)).findFirst();
		assertThat(matchetAF).isPresent();
	}

    private List<BeregningsgrunnlagPrStatus> kjørRegel(AktivitetStatusModell regelmodell) {
        FastsettStatusOgAndelPrPeriode regel = new FastsettStatusOgAndelPrPeriode();
        regel.evaluate(regelmodell);
        return regelmodell.getBeregningsgrunnlagPrStatusListe();
    }
}
