package no.nav.folketrygdloven.besteberegning;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2019;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2016;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2018;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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
import no.nav.fpsak.nare.evaluation.Evaluation;

class FinnBesteMånederTest {

	public static final List<Grunnbeløp> GRUNNBELØP_SATSER = GRUNNBELØPLISTE;

	public static final BigDecimal G_VERDI = BigDecimal.valueOf(100_000);
	public static final LocalDate SKJÆRINGSTIDSPUNKT_OPPTJENING = LocalDate.of(2019, 11, 1);
	public static final String ORGNR = "123467890";
	public static final String ORGNR2 = "434643243";

	@Test
    void skal_finne_6_siste_måneder_med_eit_arbeidsforhold_like_inntekter() {
		// Arrange
		List<Periodeinntekt> periodeinntekter = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(10000), ORGNR));
		}
		BesteberegningRegelmodell regelmodell = lagRegelmodell(List.of(), periodeinntekter);

		// Act
	    evaluer(regelmodell);

	    // Assert
		List<BeregnetMånedsgrunnlag> besteMåneder = regelmodell.getOutput().getBesteMåneder();
		assertThat(besteMåneder.size()).isEqualTo(6);
		assertThat(besteMåneder.get(0).getMåned()).isEqualTo(YearMonth.of(2019, 10));
		assertThat(besteMåneder.get(1).getMåned()).isEqualTo(YearMonth.of(2019, 9));
		assertThat(besteMåneder.get(2).getMåned()).isEqualTo(YearMonth.of(2019, 8));
		assertThat(besteMåneder.get(3).getMåned()).isEqualTo(YearMonth.of(2019, 7));
		assertThat(besteMåneder.get(4).getMåned()).isEqualTo(YearMonth.of(2019, 6));
		assertThat(besteMåneder.get(5).getMåned()).isEqualTo(YearMonth.of(2019, 5));
	}

	@Test
	void skal_finne_6_første_måneder_med_eit_arbeidsforhold() {
		// Arrange
		List<Periodeinntekt> periodeinntekter = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(10000), ORGNR));
		}
		for (int i = 5; i < 12; i++) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(20000), ORGNR));
		}
		BesteberegningRegelmodell regelmodell = lagRegelmodell(List.of(), periodeinntekter);

		// Act
		evaluer(regelmodell);

		// Assert
		List<BeregnetMånedsgrunnlag> besteMåneder = regelmodell.getOutput().getBesteMåneder();
		assertThat(besteMåneder.size()).isEqualTo(6);
		assertThat(besteMåneder.get(0).getMåned()).isEqualTo(YearMonth.of(2019, 6));
		assertThat(besteMåneder.get(1).getMåned()).isEqualTo(YearMonth.of(2019, 5));
		assertThat(besteMåneder.get(2).getMåned()).isEqualTo(YearMonth.of(2019, 4));
		assertThat(besteMåneder.get(3).getMåned()).isEqualTo(YearMonth.of(2019, 3));
		assertThat(besteMåneder.get(4).getMåned()).isEqualTo(YearMonth.of(2019, 2));
		assertThat(besteMåneder.get(5).getMåned()).isEqualTo(YearMonth.of(2019, 1));
	}

	@Test
	void skal_finne_annenkvar_måneder_med_eit_arbeidsforhold() {
		// Arrange
		List<Periodeinntekt> periodeinntekter = new ArrayList<>();
		for (int i = 0; i < 12; i = i + 2) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(10000), ORGNR));
		}
		for (int i = 1; i < 12; i = i + 2) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(20000), ORGNR));
		}
		BesteberegningRegelmodell regelmodell = lagRegelmodell(List.of(), periodeinntekter);

		// Act
		evaluer(regelmodell);

		// Assert
		List<BeregnetMånedsgrunnlag> besteMåneder = regelmodell.getOutput().getBesteMåneder();
		assertThat(besteMåneder.size()).isEqualTo(6);
		assertThat(besteMåneder.get(0).getMåned()).isEqualTo(YearMonth.of(2019, 10));
		assertThat(besteMåneder.get(1).getMåned()).isEqualTo(YearMonth.of(2019, 8));
		assertThat(besteMåneder.get(2).getMåned()).isEqualTo(YearMonth.of(2019, 6));
		assertThat(besteMåneder.get(3).getMåned()).isEqualTo(YearMonth.of(2019, 4));
		assertThat(besteMåneder.get(4).getMåned()).isEqualTo(YearMonth.of(2019, 2));
		assertThat(besteMåneder.get(5).getMåned()).isEqualTo(YearMonth.of(2019, 9));
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
		List<BeregnetMånedsgrunnlag> besteMåneder = regelmodell.getOutput().getBesteMåneder();
		assertThat(besteMåneder.size()).isEqualTo(6);
		assertThat(besteMåneder.get(0).getMåned()).isEqualTo(YearMonth.of(2019, 10));
		assertThat(besteMåneder.get(1).getMåned()).isEqualTo(YearMonth.of(2019, 9));
		assertThat(besteMåneder.get(2).getMåned()).isEqualTo(YearMonth.of(2019, 8));
		assertThat(besteMåneder.get(3).getMåned()).isEqualTo(YearMonth.of(2019, 7));
		assertThat(besteMåneder.get(4).getMåned()).isEqualTo(YearMonth.of(2019, 6));
		assertThat(besteMåneder.get(5).getMåned()).isEqualTo(YearMonth.of(2019, 5));
	}

	@Test
	void skal_finne_6_siste_måneder_med_kun_dagpenger() {
		// Arrange
		List<Periodeinntekt> periodeinntekter = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			periodeinntekter.add(lagPeriodeInntektDagpenger(i, BigDecimal.valueOf(10000)));
		}
		BesteberegningRegelmodell regelmodell = lagRegelmodell(List.of(), periodeinntekter);

		// Act
		evaluer(regelmodell);

		// Assert
		List<BeregnetMånedsgrunnlag> besteMåneder = regelmodell.getOutput().getBesteMåneder();
		assertThat(besteMåneder.size()).isEqualTo(6);
		assertThat(besteMåneder.get(0).getMåned()).isEqualTo(YearMonth.of(2019, 10));
		assertThat(besteMåneder.get(1).getMåned()).isEqualTo(YearMonth.of(2019, 9));
		assertThat(besteMåneder.get(2).getMåned()).isEqualTo(YearMonth.of(2019, 8));
		assertThat(besteMåneder.get(3).getMåned()).isEqualTo(YearMonth.of(2019, 7));
		assertThat(besteMåneder.get(4).getMåned()).isEqualTo(YearMonth.of(2019, 6));
		assertThat(besteMåneder.get(5).getMåned()).isEqualTo(YearMonth.of(2019, 5));
	}

	@Test
	void skal_finne_6_siste_måneder_med_kun_dagpenger_med_femti_prosent_utbetaling() {
		// Arrange
		List<Periodeinntekt> periodeinntekter = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			periodeinntekter.add(lagPeriodeInntektDagpenger(i, BigDecimal.valueOf(13000)));
		}
		BesteberegningRegelmodell regelmodell = lagRegelmodell(List.of(), periodeinntekter);

		// Act
		evaluer(regelmodell);

		// Assert
		List<BeregnetMånedsgrunnlag> besteMåneder = regelmodell.getOutput().getBesteMåneder();
		assertThat(besteMåneder.size()).isEqualTo(6);
		assertThat(besteMåneder.get(0).getMåned()).isEqualTo(YearMonth.of(2019, 10));
		assertThat(besteMåneder.get(0).getInntekter().get(0).getInntektPrMåned()).isEqualByComparingTo(BigDecimal.valueOf(13000));
		assertThat(besteMåneder.get(1).getMåned()).isEqualTo(YearMonth.of(2019, 9));
		assertThat(besteMåneder.get(1).getInntekter().get(0).getInntektPrMåned()).isEqualByComparingTo(BigDecimal.valueOf(13000));
		assertThat(besteMåneder.get(2).getMåned()).isEqualTo(YearMonth.of(2019, 8));
		assertThat(besteMåneder.get(2).getInntekter().get(0).getInntektPrMåned()).isEqualByComparingTo(BigDecimal.valueOf(13000));
		assertThat(besteMåneder.get(3).getMåned()).isEqualTo(YearMonth.of(2019, 7));
		assertThat(besteMåneder.get(3).getInntekter().get(0).getInntektPrMåned()).isEqualByComparingTo(BigDecimal.valueOf(13000));
		assertThat(besteMåneder.get(4).getMåned()).isEqualTo(YearMonth.of(2019, 6));
		assertThat(besteMåneder.get(4).getInntekter().get(0).getInntektPrMåned()).isEqualByComparingTo(BigDecimal.valueOf(13000));
		assertThat(besteMåneder.get(5).getMåned()).isEqualTo(YearMonth.of(2019, 5));
		assertThat(besteMåneder.get(5).getInntekter().get(0).getInntektPrMåned()).isEqualByComparingTo(BigDecimal.valueOf(13000));
	}

	@Test
	void skal_finne_6_siste_måneder_med_eit_arbeidsforhold_like_inntekter_og_næring() {
		// Arrange
		List<Periodeinntekt> periodeinntekter = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			periodeinntekter.add(lagPeriodeInntektArbeidstaker(i, BigDecimal.valueOf(10000), ORGNR));
		}
		periodeinntekter.add(lagSigrunInntekt2018());
		periodeinntekter.add(lagSigrunInntekt2017());
		periodeinntekter.add(lagSigrunInntekt2016());

		BesteberegningRegelmodell regelmodell = lagRegelmodell(List.of(Periode.of(SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(12), SKJÆRINGSTIDSPUNKT_OPPTJENING)), periodeinntekter);

		// Act
		evaluer(regelmodell);

		// Assert
		List<BeregnetMånedsgrunnlag> besteMåneder = regelmodell.getOutput().getBesteMåneder();
		assertThat(besteMåneder.size()).isEqualTo(6);
		assertThat(besteMåneder.get(0).getMåned()).isEqualTo(YearMonth.of(2019, 10));
		assertThat(besteMåneder.get(0).finnSum()).isEqualByComparingTo(BigDecimal.valueOf(2*GRUNNBELØP_2019/12));
		assertThat(besteMåneder.get(1).getMåned()).isEqualTo(YearMonth.of(2019, 9));
		assertThat(besteMåneder.get(1).finnSum()).isEqualByComparingTo(BigDecimal.valueOf(2*GRUNNBELØP_2019/12));
		assertThat(besteMåneder.get(2).getMåned()).isEqualTo(YearMonth.of(2019, 8));
		assertThat(besteMåneder.get(2).finnSum()).isEqualByComparingTo(BigDecimal.valueOf(2*GRUNNBELØP_2019/12));
		assertThat(besteMåneder.get(3).getMåned()).isEqualTo(YearMonth.of(2019, 7));
		assertThat(besteMåneder.get(3).finnSum()).isEqualByComparingTo(BigDecimal.valueOf(2*GRUNNBELØP_2019/12));
		assertThat(besteMåneder.get(4).getMåned()).isEqualTo(YearMonth.of(2019, 6));
		assertThat(besteMåneder.get(4).finnSum()).isEqualByComparingTo(BigDecimal.valueOf(2*GRUNNBELØP_2019/12));
		assertThat(besteMåneder.get(5).getMåned()).isEqualTo(YearMonth.of(2019, 5));
		assertThat(besteMåneder.get(5).finnSum()).isEqualByComparingTo(BigDecimal.valueOf(2*GRUNNBELØP_2019/12));

	}

	private Periodeinntekt lagSigrunInntekt2018() {
		return Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(2*GSNITT_2018))
				.medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
				.medPeriode(Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 12, 31))).build();
	}

	private Periodeinntekt lagSigrunInntekt2017() {
		return Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(2*GSNITT_2017))
				.medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
				.medPeriode(Periode.of(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 12, 31))).build();
	}

	private Periodeinntekt lagSigrunInntekt2016() {
		return Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(2*GSNITT_2016))
				.medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
				.medPeriode(Periode.of(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31))).build();
	}


	private Evaluation evaluer(BesteberegningRegelmodell regelmodell) {
		return new FinnBesteMåneder().evaluate(regelmodell);
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

	private Periodeinntekt lagPeriodeInntektDagpenger(int månaderFørStpFom, BigDecimal inntekt) {
		return Periodeinntekt.builder()
				.medPeriode(Periode.of(
						SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(månaderFørStpFom).withDayOfMonth(1),
						SKJÆRINGSTIDSPUNKT_OPPTJENING.minusMonths(månaderFørStpFom).with(TemporalAdjusters.lastDayOfMonth())))
				.medAktivitetStatus(AktivitetStatus.DP)
				.medInntekt(inntekt)
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
				.build();
	}

	private BesteberegningRegelmodell lagRegelmodell(List<Periode> perioderMedNæring, List<Periodeinntekt> periodeinntekter) {
		return new BesteberegningRegelmodell(new BesteberegningInput(
				lagInntektsgrunnlag(periodeinntekter),
				GRUNNBELØP_SATSER,
				BigDecimal.valueOf(GRUNNBELØP_2019),
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