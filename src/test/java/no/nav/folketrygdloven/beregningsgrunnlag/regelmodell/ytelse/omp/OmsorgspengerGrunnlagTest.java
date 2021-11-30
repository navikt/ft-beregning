package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

class OmsorgspengerGrunnlagTest {



	@Test
	void skal_gi_direkte_utbetaling_om_det_ikkje_er_søkt_refusjon_fra_start() {
		OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(BigDecimal.ZERO, false, false, false);
		assertThat(omsorgspengerGrunnlag.erDirekteUtbetaling()).isTrue();
	}

	@Test
	void skal_gi_direkte_utbetaling_om_søkt_frilans_eller_sn() {
		OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(BigDecimal.ONE, true, false, true);
		assertThat(omsorgspengerGrunnlag.erDirekteUtbetaling()).isTrue();
	}

	@Test
	void skal_gi_direkte_utbetaling_om_arbeidstakerandeler_ikke_søkt_om() {
		OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(BigDecimal.ONE, false, true, true);
		assertThat(omsorgspengerGrunnlag.erDirekteUtbetaling()).isTrue();
	}

	@Test
	void skal_gi_direkte_utbetaling_refusjon_er_lavere_enn_brutto() {
		OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(BigDecimal.ONE, false, false, true);
		lagBeregningsgrunnlag(omsorgspengerGrunnlag, BigDecimal.TEN);
		assertThat(omsorgspengerGrunnlag.erDirekteUtbetaling()).isTrue();
	}

	@Test
	void skal_ikke_gi_direkte_utbetaling_refusjon_høyere_enn_brutto() {
		OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(BigDecimal.valueOf(100), false, false, true);
		lagBeregningsgrunnlag(omsorgspengerGrunnlag, BigDecimal.TEN);
		assertThat(omsorgspengerGrunnlag.erDirekteUtbetaling()).isFalse();
	}

	@Test
	void skal_ikke_gi_direkte_utbetaling_refusjon_lik_enn_brutto() {
		OmsorgspengerGrunnlag omsorgspengerGrunnlag = new OmsorgspengerGrunnlag(BigDecimal.valueOf(10), false, false, true);
		lagBeregningsgrunnlag(omsorgspengerGrunnlag, BigDecimal.TEN);
		assertThat(omsorgspengerGrunnlag.erDirekteUtbetaling()).isFalse();
	}

	private void lagBeregningsgrunnlag(OmsorgspengerGrunnlag omsorgspengerGrunnlag, BigDecimal beregnetPrÅr) {
		Beregningsgrunnlag.builder()
				.medGrunnbeløp(BigDecimal.valueOf(100_000))
				.medBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode.builder()
						.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
						.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
								.medAktivitetStatus(AktivitetStatus.ATFL)
								.medArbeidsforhold(lagBgForArbeid(beregnetPrÅr)).build()).build())
				.medYtelsesSpesifiktGrunnlag(omsorgspengerGrunnlag);
	}

	private BeregningsgrunnlagPrArbeidsforhold lagBgForArbeid(BigDecimal beregnetPrÅr) {
		return BeregningsgrunnlagPrArbeidsforhold.builder().medAndelNr(1L)
				.medArbeidsforhold(lagArbeidsforhold())
				.medBeregnetPrÅr(beregnetPrÅr).build();
	}

	private Arbeidsforhold lagArbeidsforhold() {
		return Arbeidsforhold.builder()
		.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
		.medOrgnr("349832423").build();
	}

}