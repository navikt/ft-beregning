package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.utenfordeling;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.fpsak.nare.evaluation.Resultat;

class SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdiUtenFordelingTest {

	@Test
	void skal_gi_resultat_JA_for_inntektsgrunnlag_over_grenseverdi() {
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(100_000))
								.medInntektsgrunnlagPrÅr(BigDecimal.valueOf(1_000_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("999999999")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		var evaluate = new SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.JA);
	}

	@Test
	void skal_gi_resultat_NEI_for_inntektsgrunnlag_under_grenseverdi() {
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(800_000))
								.medInntektsgrunnlagPrÅr(BigDecimal.valueOf(500_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("999999999")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		var evaluate = new SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.NEI);
	}

	@Test
	void skal_gi_resultat_NEI_for_inntektsgrunnlag_ikke_satt() {
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(800_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("999999999")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		var evaluate = new SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.NEI);
	}

	@Test
	void skal_gi_resultat_NEI_for_inntektsgrunnlag_lik_grenseverdi() {
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(800_000))
								.medInntektsgrunnlagPrÅr(BigDecimal.valueOf(600_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("999999999")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		Beregningsgrunnlag.builder()
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));

		var evaluate = new SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.NEI);
	}

}