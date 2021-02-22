package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;

class RegelBeregningsgrunnlagInaktivTest {

	private final LocalDate STP = LocalDate.of(2018, Month.JANUARY, 15);

	@Test
	void skal_beregne_inaktiv_uten_inntektsmelding() {

		// Arrange
		Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(STP,
				årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
		BeregningsgrunnlagPrStatus brukers_andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medAndelNr(1L)
				.build();
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(STP, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.build();
		Beregningsgrunnlag bg = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.MIDL_INAKTIV, null)))
				.medSkjæringstidspunkt(STP)
				.medBeregningsgrunnlagPeriode(periode)
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medGrunnbeløpSatser(GRUNNBELØPLISTE)
				.build();

		// Act
		kjørRegel(periode);

		// Assert
		double forventet_bg = 4.0d * GRUNNBELØP_2017;
		assertThat(brukers_andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(forventet_bg));
		assertThat(bg.getAktivitetStatuser().get(0).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV);
	}


	@Test
	void skal_beregne_inaktiv_med_inntektsmelding() {

		// Arrange
		Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(STP,
				årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
		Arbeidsforhold arbeidsforhold = Arbeidsforhold.builder()
				.medOrgnr("23472342")
				.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.build();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntekt(BigDecimal.valueOf(20_000))
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
				.medArbeidsgiver(arbeidsforhold)
				.build());
		BeregningsgrunnlagPrStatus atfl_status = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.ATFL)
				.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
						.medArbeidsforhold(arbeidsforhold)
						.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
						.medAndelNr(1L)
						.build())
				.build();
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(STP, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(atfl_status)
				.build();
		Beregningsgrunnlag bg = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.MIDL_INAKTIV, null)))
				.medSkjæringstidspunkt(STP)
				.medBeregningsgrunnlagPeriode(periode)
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medGrunnbeløpSatser(GRUNNBELØPLISTE)
				.build();

		// Act
		kjørRegel(periode);

		// Assert
		double forventet_bg = 240_000;
		assertThat(atfl_status.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(forventet_bg));
		assertThat(bg.getAktivitetStatuser().get(0).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV);
	}

	private Evaluation kjørRegel(BeregningsgrunnlagPeriode periode) {
		return new RegelBeregningsgrunnlagInaktiv(periode).getSpecification().evaluate(periode);
	}

}