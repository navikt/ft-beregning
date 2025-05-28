package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;

class FinnRapporterteInntekterForInaktivTest {


	private static final String ORGNR = "999999999";
	private final FinnRapporterteInntekterForInaktiv finnRapporterteInntekter = new FinnRapporterteInntekterForInaktiv();

	@Test
	void skal_brukes_forrige_måned_med_inntekt_dersom_tilgjengelig() {
		// Arrange
        var forrigeMåned = BigDecimal.valueOf(8000);
        var nesteMåned = BigDecimal.valueOf(9000);
		var skjæringstidspunkt = LocalDate.of(2022, 11, 11);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
		Arbeidsforhold.builder(arbeidsforhold).medAnsettelsesPeriode(Periode.of(LocalDate.of(2022, 10, 20), null));
        var inntektsgrunnlag = new Inntektsgrunnlag();
		leggTilMånedsinntekt(nesteMåned, skjæringstidspunkt, arbeidsforhold, inntektsgrunnlag, 0);
		leggTilMånedsinntekt(forrigeMåned, skjæringstidspunkt, arbeidsforhold, inntektsgrunnlag, 1);

		var bg = settoppGrunnlagMedEnPeriode(skjæringstidspunkt,
				inntektsgrunnlag,
				singletonList(AktivitetStatus.ATFL),
				singletonList(arbeidsforhold),
				emptyList(),
				emptyList());
        var grunnlag = bg.getBeregningsgrunnlagPerioder().get(0);

		// Act
		var periodeinntekt = finnRapporterteInntekter.finnRapportertInntekt(grunnlag);

		assertThat(periodeinntekt.orElseThrow().getInntekt()).isEqualByComparingTo(BigDecimal.valueOf(260_000));

	}

	@Test
	void skal_bruke_siste_måned_med_inntekt_dersom_ikke_forrige_er_tilgjengelig() {
		// Arrange
        var nesteMåned = BigDecimal.valueOf(22_000);
		var skjæringstidspunkt = LocalDate.of(2022, 11, 11);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
		Arbeidsforhold.builder(arbeidsforhold).medAnsettelsesPeriode(Periode.of(LocalDate.of(2022, 10, 20), null));
        var inntektsgrunnlag = new Inntektsgrunnlag();
		leggTilMånedsinntekt(nesteMåned, skjæringstidspunkt, arbeidsforhold, inntektsgrunnlag, 0);

		var bg = settoppGrunnlagMedEnPeriode(skjæringstidspunkt,
				inntektsgrunnlag,
				singletonList(AktivitetStatus.ATFL),
				singletonList(arbeidsforhold),
				emptyList(),
				emptyList());
        var grunnlag = bg.getBeregningsgrunnlagPerioder().get(0);

		// Act
		var periodeinntekt = finnRapporterteInntekter.finnRapportertInntekt(grunnlag);

		assertThat(periodeinntekt.orElseThrow().getInntekt()).isEqualByComparingTo(BigDecimal.valueOf(260_000));

	}

	@Test
	void skal_returnere_empty_når_ingen_månedsinntekter() {
		// Arrange
		var skjæringstidspunkt = LocalDate.of(2022, 11, 11);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
		Arbeidsforhold.builder(arbeidsforhold).medAnsettelsesPeriode(Periode.of(LocalDate.of(2022, 10, 20), null));
        var inntektsgrunnlag = new Inntektsgrunnlag();

		var bg = settoppGrunnlagMedEnPeriode(skjæringstidspunkt,
				inntektsgrunnlag,
				singletonList(AktivitetStatus.ATFL),
				singletonList(arbeidsforhold),
				emptyList(),
				emptyList());
        var grunnlag = bg.getBeregningsgrunnlagPerioder().get(0);

		// Act
		var periodeinntekt = finnRapporterteInntekter.finnRapportertInntekt(grunnlag);

		assertThat(periodeinntekt).isEmpty();

	}

	@Test
	void skal_returnere_empty_når_inntekt_eldre_enn_4_måneder() {
		// Arrange
        var nesteMåned = BigDecimal.valueOf(22_000);
		var skjæringstidspunkt = LocalDate.of(2022, 11, 11);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
		Arbeidsforhold.builder(arbeidsforhold).medAnsettelsesPeriode(Periode.of(LocalDate.of(2022, 10, 20), null));
        var inntektsgrunnlag = new Inntektsgrunnlag();
		leggTilMånedsinntekt(nesteMåned, skjæringstidspunkt, arbeidsforhold, inntektsgrunnlag, 5);

		var bg = settoppGrunnlagMedEnPeriode(skjæringstidspunkt,
				inntektsgrunnlag,
				singletonList(AktivitetStatus.ATFL),
				singletonList(arbeidsforhold),
				emptyList(),
				emptyList());
        var grunnlag = bg.getBeregningsgrunnlagPerioder().get(0);

		// Act
		var periodeinntekt = finnRapporterteInntekter.finnRapportertInntekt(grunnlag);

		assertThat(periodeinntekt).isEmpty();

	}

	@Test
	void skal_ikke_bruke_ansettelsesperiode_som_starter_etter_inntektsperiode() {
		// Arrange
        var forrigeMåned = BigDecimal.valueOf(8000);
        var nesteMåned = BigDecimal.valueOf(9000);
		var skjæringstidspunkt = LocalDate.of(2022, 11, 11);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR);
		Arbeidsforhold.builder(arbeidsforhold).medAnsettelsesPeriode(Periode.of(LocalDate.of(2022, 11, 9), LocalDate.of(2023, 3, 11)));
        var inntektsgrunnlag = new Inntektsgrunnlag();
		leggTilMånedsinntekt(nesteMåned, skjæringstidspunkt, arbeidsforhold, inntektsgrunnlag, 0);
		leggTilMånedsinntekt(forrigeMåned, skjæringstidspunkt, arbeidsforhold, inntektsgrunnlag, 1);

		var bg = settoppGrunnlagMedEnPeriode(skjæringstidspunkt,
				inntektsgrunnlag,
				singletonList(AktivitetStatus.ATFL),
				singletonList(arbeidsforhold),
				emptyList(),
				emptyList());
        var grunnlag = bg.getBeregningsgrunnlagPerioder().get(0);

		// Act
		var periodeinntekt = finnRapporterteInntekter.finnRapportertInntekt(grunnlag);

		assertThat(periodeinntekt.get().getInntekt()).isEqualByComparingTo(BigDecimal.valueOf(146_250));
	}

	private void leggTilMånedsinntekt(BigDecimal nesteMåned, LocalDate skjæringstidspunkt, Arbeidsforhold arbeidsforhold, Inntektsgrunnlag inntektsgrunnlag, int månederFørStp) {
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
				.medArbeidsgiver(arbeidsforhold)
				.medMåned(skjæringstidspunkt.minusMonths(månederFørStp))
				.medInntekt(nesteMåned)
				.build());
	}

}
