package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi;

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
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.psb.PleiepengerGrunnlagFastsettGrenseverdi;
import no.nav.fpsak.nare.evaluation.Resultat;

class SkalFinneGrenseverdiUtenFordelingTest {

	@Test
	void skal_finne_grenseverdi_med_fordeling_dersom_toggle_er_av() {


		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(100_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("123456789")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", false)
				.medBeregningsgrunnlagPeriode(periode)
				.medYtelsesSpesifiktGrunnlag(PleiepengerGrunnlagFastsettGrenseverdi.forSyktBarn())
				.medGrunnbeløp(BigDecimal.valueOf(100_000));


		//Act
		var evaluate = new SkalFinneGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.NEI);

	}


	@Test
	void skal_finne_grenseverdi_med_fordeling_dersom_toggle_er_på_og_ikke_pleiepenger() {

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(100_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("123456789")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));


		//Act
		var evaluate = new SkalFinneGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.NEI);

	}


	@Test
	void skal_finne_grenseverdi_med_fordeling_dersom_toggle_er_på_og_fom_dato_før_dato_for_nye_regler() {

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), LocalDate.now().plusDays(9)))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(100_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("123456789")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		var ytelsesSpesifiktGrunnlag = PleiepengerGrunnlagFastsettGrenseverdi.forSyktBarn();
		ytelsesSpesifiktGrunnlag.setStartdatoNyeGraderingsregler(LocalDate.now().plusDays(10));
		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medYtelsesSpesifiktGrunnlag(ytelsesSpesifiktGrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));


		//Act
		var evaluate = new SkalFinneGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.NEI);

	}

	@Test
	void skal_finne_grenseverdi_uten_fordeling_dersom_toggle_er_på_og_fom_dato_lik_dato_for_nye_regler() {

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), LocalDate.now().plusDays(9)))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(100_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("123456789")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		var ytelsesSpesifiktGrunnlag = PleiepengerGrunnlagFastsettGrenseverdi.forSyktBarn();
		ytelsesSpesifiktGrunnlag.setStartdatoNyeGraderingsregler(LocalDate.now());
		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medYtelsesSpesifiktGrunnlag(ytelsesSpesifiktGrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));


		//Act
		var evaluate = new SkalFinneGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.JA);

	}

	@Test
	void skal_finne_grenseverdi_uten_fordeling_dersom_toggle_er_på_og_fom_dato_etter_dato_for_nye_regler() {

		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), LocalDate.now().plusDays(9)))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus
						.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medAndelNr(1L)
								.medBruttoPrÅr(BigDecimal.valueOf(100_000))
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medOrgnr("123456789")
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
						.build()).build();

		var ytelsesSpesifiktGrunnlag = PleiepengerGrunnlagFastsettGrenseverdi.forSyktBarn();
		ytelsesSpesifiktGrunnlag.setStartdatoNyeGraderingsregler(LocalDate.now().minusDays(1));
		Beregningsgrunnlag.builder()
				.leggTilToggle("GRADERING_MOT_INNTEKT", true)
				.medBeregningsgrunnlagPeriode(periode)
				.medYtelsesSpesifiktGrunnlag(ytelsesSpesifiktGrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(100_000));


		//Act
		var evaluate = new SkalFinneGrenseverdiUtenFordeling().evaluate(periode);

		assertThat(evaluate.result()).isEqualTo(Resultat.JA);

	}


}