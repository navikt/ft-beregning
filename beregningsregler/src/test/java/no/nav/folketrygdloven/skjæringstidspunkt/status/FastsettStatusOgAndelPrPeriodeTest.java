package no.nav.folketrygdloven.skjæringstidspunkt.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;

class FastsettStatusOgAndelPrPeriodeTest {


    public static final LocalDate STP = LocalDate.now();

	private AktivitetStatusModell regelmodell;

	@BeforeEach
	void setup() {
		regelmodell = new AktivitetStatusModell();
		regelmodell.setSkjæringstidspunktForBeregning(STP);
		regelmodell.leggTilToggle("aap.praksisendring", true);
	}

    @Test
    void skal_lage_andel_for_enkelt_arbeidsforhold() {
        // Arrange
        regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(STP.minusMonths(36), STP.plusMonths(12)), "999999999", null));

        // Act
        var statusListe = kjørRegel(regelmodell);

        // Assert
        assertThat(statusListe).hasSize(1);
        assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
		verifiserArbeidsforhold(statusListe.get(0).getArbeidsforholdList(), "999999999");
    }

	@Test
	void skal_lage_andel_for_flere_arbeidsforhold() {
		// Arrange
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(STP.minusMonths(36), STP.plusMonths(12)), "999999999", null));
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forArbeidstakerHosPrivatperson(Periode.of(STP.minusMonths(36), STP.plusMonths(12)), "999999998"));

		// Act
        var statusListe = kjørRegel(regelmodell);

		// Assert
		assertThat(statusListe).hasSize(1);
		assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
		verifiserArbeidsforhold(statusListe.get(0).getArbeidsforholdList(), "999999999");
		verifiserArbeidsforhold(statusListe.get(0).getArbeidsforholdList(), "999999998");
	}

	@Test
	void skal_ikke_lage_andel_for_aktiviteter_som_ikke_var_aktiv_på_beregningstidspunkt() {
		// Arrange
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.NÆRINGSINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.FRILANSINNTEKT, Periode.of(STP.minusMonths(36), STP.minusDays(2))));

		// Act
        var statusListe = kjørRegel(regelmodell);

		// Assert
		assertThat(statusListe).hasSize(1);
		assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
	}

	@Test
	void skal_oversette_sykepenger_til_brukers_andel_når_det_er_eneste_aktivitet() {
		// Arrange
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.SYKEPENGER_MOTTAKER, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

		// Act
        var statusListe = kjørRegel(regelmodell);

		// Assert
		assertThat(statusListe).hasSize(1);
		assertThat(statusListe.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BA);
	}

	@Test
	void skal_lage_andel_for_arbeid_under_aap() {
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeinntektMedInntektOgOverlapp());
		regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.AAP_MOTTAKER, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        var statusListe = kjørRegel(regelmodell);

		assertThat(statusListe).hasSize(2);
		assertThat(statusListe.getFirst().getAktivitetStatus()).isEqualTo(AktivitetStatus.ATFL);
		assertThat(statusListe.getFirst().getArbeidsforholdList().getFirst().getAktivitet()).isEqualTo(Aktivitet.ARBEID_UNDER_AAP);
	}

	@Test
	void skal_ikke_lage_andel_for_arbeid_under_aap_hvis_ingen_inntektsgrunnlag() {
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.AAP_MOTTAKER, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        var statusListe = kjørRegel(regelmodell);

		assertThat(statusListe).hasSize(1);
		assertThat(statusListe.getFirst().getAktivitetStatus()).isNotEqualTo(AktivitetStatus.AT);
	}

	@Test
	void skal_ikke_lage_andel_for_arbeid_under_aap_hvis_ingen_inntekt() {
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeinntekt(BigDecimal.ZERO, new Periode(STP.minusMonths(10), STP), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING));
		regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.AAP_MOTTAKER, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        var statusListe = kjørRegel(regelmodell);

		assertThat(statusListe).hasSize(1);
		assertThat(statusListe.getFirst().getAktivitetStatus()).isNotEqualTo(AktivitetStatus.AT);
	}

	@Test
	void skal_ikke_lage_andel_for_arbeid_under_aap_hvis_ingen_overlapp() {
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeinntekt(BigDecimal.ONE, new Periode(STP.minusMonths(10), STP.minusMonths(4)), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING));
		regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.AAP_MOTTAKER, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        var statusListe = kjørRegel(regelmodell);

		assertThat(statusListe).hasSize(1);
		assertThat(statusListe.getFirst().getAktivitetStatus()).isNotEqualTo(AktivitetStatus.AT);
	}

	@Test
	void skal_ikke_lage_andel_for_arbeid_under_aap_hvis_ingen_aap() {
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeinntektMedInntektOgOverlapp());
		regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        var statusListe = kjørRegel(regelmodell);

		assertThat(statusListe).hasSize(1);
		assertThat(statusListe.getFirst().getAktivitetStatus()).isNotEqualTo(AktivitetStatus.AT);
	}

	@Test
	void skal_ikke_lage_andel_for_arbeid_under_aap_for_andre_inntektskilde_enn_inntektskomponenten_beregning() {
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(lagPeriodeinntekt(BigDecimal.ONE, new Periode(STP.minusMonths(10), STP), Inntektskilde.ANNEN_YTELSE));
		regelmodell.setInntektsgrunnlag(inntektsgrunnlag);
		regelmodell.leggTilEllerOppdaterAktivPeriode(AktivPeriode.forAndre(Aktivitet.ARBEIDSTAKERINNTEKT, Periode.of(STP.minusMonths(36), STP.plusMonths(12))));

        var statusListe = kjørRegel(regelmodell);

		assertThat(statusListe).hasSize(1);
		assertThat(statusListe.getFirst().getAktivitetStatus()).isNotEqualTo(AktivitetStatus.AT);
	}

	private void verifiserArbeidsforhold(List<Arbeidsforhold> arbeidsforholdList, String arbeidsgiverIdent) {
		var matchetAF = arbeidsforholdList.stream()
				.filter(af -> af.getArbeidsgiverId() != null && af.getArbeidsgiverId().equals(arbeidsgiverIdent)).findFirst();
		assertThat(matchetAF).isPresent();
	}

    private List<BeregningsgrunnlagPrStatus> kjørRegel(AktivitetStatusModell regelmodell) {
        var regel = new FastsettStatusOgAndelPrPeriode();
        regel.evaluate(regelmodell);
        return regelmodell.getBeregningsgrunnlagPrStatusListe();
    }

	private static Periodeinntekt lagPeriodeinntektMedInntektOgOverlapp() {
		return lagPeriodeinntekt(BigDecimal.ONE, new Periode(STP.minusMonths(10), STP), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING);
	}

	private static Periodeinntekt lagPeriodeinntekt(BigDecimal inntekt, Periode periode, Inntektskilde inntektskilde) {
		return Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(inntektskilde)
				.medPeriode(periode)
				.medInntekt(inntekt)
				.build();
	}
}
