package no.nav.folketrygdloven.besteberegning;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.input.BesteberegningInput;
import no.nav.folketrygdloven.besteberegning.modell.output.BeregnetMånedsgrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetAndel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;

class RegelForeslåBesteberegningTest {

	public static final List<Grunnbeløp> GRUNNBELØP_SATSER = GRUNNBELØPLISTE;
	public static final BigDecimal G_VERDI = BigDecimal.valueOf(100_000);
	public static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2019, 11, 1);
	public static final String ORGNR = "123467890";
	public static final String ORGNR2 = "434643243";

	@Test
	void skal_finne_besteberegnet_grunnlag_for_eit_arbeidsforhold_like_inntekter() {
		// Arrange
		List<Periodeinntekt> periodeinntekter = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(10000), ORGNR));
		}
		BesteberegningRegelmodell regelmodell = lagRegelmodell(List.of(), periodeinntekter);

		// Act
		evaluer(regelmodell);

		// Assert
		BesteberegnetGrunnlag besteberegnetGrunnlag = regelmodell.getOutput().getBesteberegnetGrunnlag();
		List<BesteberegnetAndel> andeler = besteberegnetGrunnlag.getBesteberegnetAndelList();
		assertThat(andeler.size()).isEqualTo(1);
		assertThat(andeler.get(0).getBesteberegnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(120_000));
	}

	@Test
	void skal_finne_6_siste_måneder_med_to_arbeidsforhold() {
		// Arrange
		List<Periodeinntekt> periodeinntekter = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(10000), ORGNR));
		}
		for (int i = 1; i < 12; i++) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(20000), ORGNR2));
		}
		BesteberegningRegelmodell regelmodell = lagRegelmodell(List.of(), periodeinntekter);

		// Act
		evaluer(regelmodell);

		// Assert
		BesteberegnetGrunnlag besteberegnetGrunnlag = regelmodell.getOutput().getBesteberegnetGrunnlag();
		List<BesteberegnetAndel> andeler = besteberegnetGrunnlag.getBesteberegnetAndelList();
		andeler.sort(Comparator.comparing(BesteberegnetAndel::getBesteberegnetPrÅr));
		assertThat(andeler.size()).isEqualTo(2);
		assertThat(andeler.get(0).getBesteberegnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(120_000));
		assertThat(andeler.get(1).getBesteberegnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(240_000));
	}

	private Evaluation evaluer(BesteberegningRegelmodell regelmodell) {
		return new RegelForeslåBesteberegning().getSpecification().evaluate(regelmodell);
	}

	private Periodeinntekt lagPeriodeInntektArbeidstaker(int månaderFørStpFom, BigDecimal inntekt, String orgnr) {
		return Periodeinntekt.builder()
				.medPeriode(Periode.of(
						SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(månaderFørStpFom).withDayOfMonth(1),
						SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(månaderFørStpFom).with(TemporalAdjusters.lastDayOfMonth())))
				.medAktivitetStatus(AktivitetStatus.AT)
				.medInntekt(inntekt)
				.medArbeidsgiver(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr))
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
				.build();
	}

	private BesteberegningRegelmodell lagRegelmodell(List<Periode> perioderMedNæring, List<Periodeinntekt> periodeinntekter) {
		return new BesteberegningRegelmodell(new BesteberegningInput(
				lagInntektsgrunnlag(periodeinntekter),
				GRUNNBELØP_SATSER,
				G_VERDI,
				SKJÆRINGSTIDSPUNKT_OPPTJENING,
				perioderMedNæring
		));
	}

	private Inntektsgrunnlag lagInntektsgrunnlag(List<Periodeinntekt> inntekter) {
		Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
		inntekter.forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
		return inntektsgrunnlag;
	}

}