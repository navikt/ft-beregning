package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserRegelmerknad;
import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter.getRegelResultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

class RegelBeregningsgrunnlagInaktivTest {

	private final LocalDate STP = LocalDate.of(2018, Month.JANUARY, 15);

	@Test
	void skal_beregne_inaktiv_uten_inntektsmelding() {

		// Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(STP,
				årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        var brukers_andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medAndelNr(1L)
				.build();
        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(STP, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.build();
        var bg = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.MIDL_INAKTIV, null)))
				.medSkjæringstidspunkt(STP)
				.medBeregningsgrunnlagPeriode(periode)
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medGrunnbeløpSatser(GRUNNBELØPLISTE)
				.build();

		// Act
		var evaluation = kjørRegel(periode);

		// Assert
        var forventet_bg = 4.0d * GRUNNBELØP_2017;

        var regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

		assertThat(brukers_andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(forventet_bg));
		assertThat(bg.getAktivitetStatuser().get(0).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV);
	}


	@Test
	void skal_få_skjønnsfastsette_ved_inaktiv_med_inntektsmelding_og_25_prosent_avvik() {

		// Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(STP,
				årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        var arbeidsforhold = Arbeidsforhold.builder()
				.medOrgnr("23472342")
				.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medAnsettelsesPeriode(Periode.of(STP.minusDays(10), STP.plusDays(100)))
				.build();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntekt(BigDecimal.valueOf(20_000))
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
				.medArbeidsgiver(arbeidsforhold)
				.build());
        var brukers_andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medAndelNr(1L)
				.build();
        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(STP, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.build();
        var bg = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.MIDL_INAKTIV, null)))
				.medSkjæringstidspunkt(STP)
				.medBeregningsgrunnlagPeriode(periode)
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medGrunnbeløpSatser(GRUNNBELØPLISTE)
				.build();

		// Act
		var evaluation = kjørRegel(periode);

		// Assert
        var forventet_bg = 4.0d * GRUNNBELØP_2017;

		EvaluationSerializer.asJson(evaluation);
        var regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserRegelmerknad(regelResultat, "5054");

		assertThat(brukers_andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(forventet_bg));
		assertThat(bg.getAktivitetStatuser().get(0).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV);

		var sg = bg.getSammenligningsgrunnlagForStatus(SammenligningGrunnlagType.MIDLERTIDIG_INAKTIV).orElseThrow();
		assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(240_000));
	}

	@Test
	void skal_ikke_beregne_avvik_dersom_inntektsmelding_fra_arbeidsforhold_med_start_på_stp() {

		// Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(STP,
				årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        var arbeidsforhold = Arbeidsforhold.builder()
				.medOrgnr("23472342")
				.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medAnsettelsesPeriode(Periode.of(STP, STP.plusDays(100)))
				.build();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntekt(BigDecimal.valueOf(20_000))
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
				.medArbeidsgiver(arbeidsforhold)
				.build());
        var brukers_andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medAndelNr(1L)
				.build();
        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(STP, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.build();
        var bg = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.MIDL_INAKTIV, null)))
				.medSkjæringstidspunkt(STP)
				.medBeregningsgrunnlagPeriode(periode)
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medGrunnbeløpSatser(GRUNNBELØPLISTE)
				.build();

		// Act
		var evaluation = kjørRegel(periode);

		// Assert
        var forventet_bg = 4.0d * GRUNNBELØP_2017;

		EvaluationSerializer.asJson(evaluation);
        var regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

		assertThat(brukers_andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(forventet_bg));
		assertThat(bg.getAktivitetStatuser().get(0).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV);
		assertThat(bg.getSammenligningsgrunnlagPrStatus()).isEmpty();
	}


	@Test
	void skal_få_skjønnsfastsette_ved_inaktiv_med_inntekt_i_aordningen_og_25_prosent_avvik() {

		// Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(STP,
				årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        var arbeidsforhold = Arbeidsforhold.builder()
				.medOrgnr("23472342")
				.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medAnsettelsesPeriode(Periode.of(STP.withDayOfMonth(5), STP.plusDays(100)))
				.build();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntekt(BigDecimal.valueOf(19_000))
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
				.medPeriode(Periode.of(STP.withDayOfMonth(1), STP.with(TemporalAdjusters.lastDayOfMonth())))
				.medArbeidsgiver(arbeidsforhold)
				.build());
        var brukers_andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medAndelNr(1L)
				.build();
        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(STP, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.build();
        var bg = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.MIDL_INAKTIV, null)))
				.medSkjæringstidspunkt(STP)
				.medBeregningsgrunnlagPeriode(periode)
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medGrunnbeløpSatser(GRUNNBELØPLISTE)
				.build();

		// Act
		var evaluation = kjørRegel(periode);

		// Assert
        var forventet_bg = 4.0d * GRUNNBELØP_2017;

		EvaluationSerializer.asJson(evaluation);
        var regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserRegelmerknad(regelResultat, "5054");

		assertThat(brukers_andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(forventet_bg));
		assertThat(bg.getAktivitetStatuser().get(0).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV);

		var sg = bg.getSammenligningsgrunnlagForStatus(SammenligningGrunnlagType.MIDLERTIDIG_INAKTIV).orElseThrow();
		assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(260_000));
	}

	@Test
	void skal_ikke_få_skjønnsfastsette_ved_inaktiv_med_inntekt_i_aordningen_uten_25_prosent_avvik() {

		// Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(STP,
				årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        var arbeidsforhold = Arbeidsforhold.builder()
				.medOrgnr("23472342")
				.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medAnsettelsesPeriode(Periode.of(STP.withDayOfMonth(5), STP.plusDays(100)))
				.build();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntekt(BigDecimal.valueOf(24_000))
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
				.medPeriode(Periode.of(STP.withDayOfMonth(1), STP.with(TemporalAdjusters.lastDayOfMonth())))
				.medArbeidsgiver(arbeidsforhold)
				.build());
        var brukers_andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medAndelNr(1L)
				.build();
        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(STP, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.build();
        var bg = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.MIDL_INAKTIV, null)))
				.medSkjæringstidspunkt(STP)
				.medBeregningsgrunnlagPeriode(periode)
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medGrunnbeløpSatser(GRUNNBELØPLISTE)
				.build();

		// Act
		var evaluation = kjørRegel(periode);

		// Assert
        var forventet_bg = 4.0d * GRUNNBELØP_2017;
		var antallVirkedagerMedInntekt = BigDecimal.valueOf(19);
		var inntekt = BigDecimal.valueOf(24_000);
		var forventet_rapportert = inntekt.divide(antallVirkedagerMedInntekt, 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(260));
		EvaluationSerializer.asJson(evaluation);
        var regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

		assertThat(brukers_andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(forventet_bg));
		assertThat(bg.getAktivitetStatuser().get(0).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV);

		var sg = bg.getSammenligningsgrunnlagForStatus(SammenligningGrunnlagType.MIDLERTIDIG_INAKTIV).orElseThrow();
		assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(forventet_rapportert);
	}

	@Test
	void skal_ikke_få_skjønnsfastsette_ved_inaktiv_med_inntekt_i_aordningen_arbeidsforhold_som_starter_på_stp() {

		// Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(STP,
				årsinntekterFor3SisteÅr(5.0d, 3.0d, 4.0d), Inntektskilde.SIGRUN);
        var arbeidsforhold = Arbeidsforhold.builder()
				.medOrgnr("23472342")
				.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
				.medAnsettelsesPeriode(Periode.of(STP, STP.plusDays(100)))
				.build();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntekt(BigDecimal.valueOf(19_000))
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
				.medPeriode(Periode.of(STP.withDayOfMonth(1), STP.with(TemporalAdjusters.lastDayOfMonth())))
				.medArbeidsgiver(arbeidsforhold)
				.build());
        var brukers_andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medAndelNr(1L)
				.build();
        var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(STP, TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.build();
        var bg = Beregningsgrunnlag.builder()
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.MIDL_INAKTIV, null)))
				.medSkjæringstidspunkt(STP)
				.medBeregningsgrunnlagPeriode(periode)
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medUregulertGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP_2017))
				.medGrunnbeløpSatser(GRUNNBELØPLISTE)
				.build();

		// Act
		var evaluation = kjørRegel(periode);

		// Assert
        var forventet_bg = 4.0d * GRUNNBELØP_2017;

		EvaluationSerializer.asJson(evaluation);
        var regelResultat = getRegelResultat(evaluation, "input");
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

		assertThat(brukers_andel.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(forventet_bg));
		assertThat(bg.getAktivitetStatuser().get(0).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.K9_HJEMMEL_MIDLERTIDIG_INAKTIV);

		assertThat(bg.getSammenligningsgrunnlagPrStatus()).isEmpty();
	}

	private Evaluation kjørRegel(BeregningsgrunnlagPeriode periode) {
		return new RegelBeregningsgrunnlagInaktivMedAvviksvurdering().getSpecification().evaluate(periode);
	}

}
