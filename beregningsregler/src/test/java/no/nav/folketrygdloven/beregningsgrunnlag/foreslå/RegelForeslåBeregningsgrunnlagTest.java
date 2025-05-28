package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import static java.util.Collections.singletonList;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekterPrStatus;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settOppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel.F_14_7;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå.RegelFortsettForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;

class RegelForeslåBeregningsgrunnlagTest {

	private LocalDate skjæringstidspunkt;
	private String orgnr;
	private Arbeidsforhold arbeidsforhold;

	@BeforeEach
	void setup() {
		skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
		orgnr = "987";
        var arbeidsforholdStartdato = skjæringstidspunkt.minusYears(2);
		arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholdStartdato, orgnr);
	}

	@Test
	void skalBeregneGrunnlagAGVedSammeFrilansInntektSisteTreMåneder() { // NOSONAR
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
	}

	@Test
	void skalBeregneGrunnlagAGVedSammeInntektSisteTreMåneder() { // NOSONAR
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
	}

	@Test
	void skalBeregneGrunnlagAGVedKombinasjonATFLogSN() { // NOSONAR
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
				List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
		verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);
        var beløpSN = ((4.0d * GRUNNBELØP_2017) - (12 * månedsinntekt.doubleValue())); // Differanse siden SN > ATFL: SN - ATFL
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, beløpSN, 4.0d * GRUNNBELØP_2017);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, beløpSN + 12 * månedsinntekt.doubleValue());
	}

	@Test
	void skalBeregneGrunnlagAGVedKombinasjonATFLogSNHvorATFLStørreEnnSNMedAvkorting() { // NOSONAR
		// ATFL > 6G, SN < ATFL: ATFL blir avkortet til 6G og SN blir satt til 0.
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 4, 6), Inntektskilde.SIGRUN);

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
				List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        var forventetPGI = 5.0d * GRUNNBELØP_2017;
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0, forventetPGI);
	}

	@Test
	void BeregningsgrunnlagKombinasjonATFLStørreEnnSNMedAvkorting() { // NOSONAR
		// SN > 6G, SN > ATFL: Både ATFL og SN blir avkortet.
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 3);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 3);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(7, 8, 6), Inntektskilde.SIGRUN);

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
				List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
        var forventetATFL = 12 * månedsinntekt.doubleValue();
        var forventetPGI = 593015.333333;
        var forventetSN = forventetPGI - forventetATFL;
		verifiserBeregningsgrunnlagBeregnet(grunnlag, forventetATFL + forventetSN);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, forventetATFL);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, forventetSN, forventetPGI);
	}

	@Test
	void skalBeregneGrunnlagMedInntektsmeldingMedNaturalYtelser() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(40000);
        var refusjonskrav = BigDecimal.valueOf(10000);
        var naturalytelse = BigDecimal.valueOf(2000);
        var naturalytelseOpphørFom = skjæringstidspunkt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt, refusjonskrav, naturalytelse, naturalytelseOpphørFom);
		opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(30000));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(resultat.merknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
		assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isEqualTo(BigDecimal.valueOf(24000));
		assertThat(grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.AT_FL).getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(400));
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
	}

	@Test
	void skalBeregneGrunnlagForTilstøtendeYtelseDagpenger() { // NOSONAR
		// Arrange
        var dagsats = BigDecimal.valueOf(716);
        var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
				.medMåned(skjæringstidspunkt)
				.medInntekt(dagsats)
				.medUtbetalingsfaktor(BigDecimal.ZERO)
				.build());
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.DP));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7_8_49, AktivitetStatus.DP, 260 * dagsats.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 260 * dagsats.doubleValue());
	}

	@Test
	void skalBeregneGrunnlagForKombinasjonSNOgDagpenger() { // NOSONAR
		// Arrange
        var utbetalingsfaktor = BigDecimal.valueOf(0.75);
        var dagsats = BigDecimal.valueOf(900);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 5, 5), Inntektskilde.SIGRUN);
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
				.medMåned(skjæringstidspunkt)
				.medInntekt(dagsats)
				.medUtbetalingsfaktor(utbetalingsfaktor)
				.build());
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
				List.of(AktivitetStatus.SN, AktivitetStatus.DP));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert
        var expectedbruttoDP = dagsats.doubleValue() * 260 * utbetalingsfaktor.doubleValue();
        var expectedPGIsnitt = 5.0 * GRUNNBELØP_2017;
        var expectedBruttoSN = expectedPGIsnitt - expectedbruttoDP;
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7_8_49, AktivitetStatus.DP, expectedbruttoDP);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoDP + expectedBruttoSN);
	}


	@Test
	void skalBeregneGrunnlagForKombinasjonATFL_SNOgAAP() { // NOSONAR
		// Arrange
        var utbetalingsfaktor = new BigDecimal("1");
        var dagsatsAAP = BigDecimal.valueOf(700);
        var månedsinntektATFL = BigDecimal.valueOf(20000);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(6, 6, 6), Inntektskilde.SIGRUN);

		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING).medArbeidsgiver(arbeidsforhold)
				.medInntekt(månedsinntektATFL).medMåned(skjæringstidspunkt).build());
		inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP).medUtbetalingsfaktor(utbetalingsfaktor)
				.medInntekt(dagsatsAAP).medMåned(skjæringstidspunkt).build());
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
				List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP), Collections.singletonList(arbeidsforhold),
				Collections.singletonList(månedsinntektATFL.multiply(BigDecimal.valueOf(12))));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert
        var expectedbruttoAAP = dagsatsAAP.doubleValue() * 260 * utbetalingsfaktor.doubleValue();
        var expectedPGIsnitt = 6.0 * GRUNNBELØP_2017;
        var expectedBruttoATFL = 12 * månedsinntektATFL.doubleValue();
        var expectedBruttoSN = expectedPGIsnitt - expectedbruttoAAP - expectedBruttoATFL;
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.AAP, expectedbruttoAAP);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, expectedBruttoATFL);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoAAP + expectedBruttoSN + expectedBruttoATFL);
	}


	@Test
	void skalTesteNyoppstartetFrilanser() {
        var inntektsgrunnlag = new Inntektsgrunnlag();
		opprettSammenligningsgrunnlag(inntektsgrunnlag, skjæringstidspunkt, BigDecimal.valueOf(25000));
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
				Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(Arbeidsforhold.frilansArbeidsforhold()));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(BigDecimal.valueOf(300000))
				.build();

		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(resultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        var fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        var tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		assertThat(af.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
		assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
	}

	@Test
	void skalTesteArbeidsforholdInntektSattAvSaksbehandlerNårIkkeInntektsmelding() {
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
				Collections.singletonList(AktivitetStatus.ATFL), Collections.singletonList(arbeidsforhold));
		opprettSammenligningsgrunnlag(inntektsgrunnlag, skjæringstidspunkt, BigDecimal.valueOf(18000));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(BigDecimal.valueOf(200000))
				.build();

		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(resultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        var fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        var tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		assertThat(af.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
		assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(200000));
		assertThat(beregningsgrunnlag.getSammenligningsgrunnlagPrStatus()).isNotEmpty();
	}

	@Test
	void skalTesteKjøringAvKunYtelse() {
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, new Inntektsgrunnlag(),
				Collections.singletonList(AktivitetStatus.KUN_YTELSE));
		Beregningsgrunnlag.builder(beregningsgrunnlag).medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(false));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var prStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.BA);
		BeregningsgrunnlagPrStatus.builder(prStatus).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(100000));
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(resultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		assertThat(grunnlag.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(100000));
		assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.KUN_YTELSE).getHjemmel()).isEqualTo(F_14_7);
	}


	@Test
	void skalTåleUkjentStatustype() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, true);
		leggtilStatus(beregningsgrunnlag, AktivitetStatus.UDEFINERT);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert
		verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
	}

	@Test
	void skalFastsetteBeregningsperiondenUtenInntektDeTreSisteMånederAT() {
		// arbeidstaker uten inntektsmelding OG det finnes ikke inntekt i de tre siste månedene
		// før skjæringstidspunktet (beregningsperioden)
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt.minusMonths(3), List.of(månedsinntekt, månedsinntekt, månedsinntekt),
				Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
				Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
				List.of(arbeidsforhold), Collections.singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12))));

        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		assertThat(resultat.sporing().sporing()).isNotBlank();
		//SÅ skal brutto beregningsgrunnlag i beregningsperioden settes til 0
		assertThat(grunnlag.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
		// skal beregningsperioden settes til de tre siste månedene før skjæringstidspunktet for beregning
        var fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        var tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0)
				.getBeregningsperiode()).isEqualTo(Periode.of(fom, tom));
	}

	@Test
	void skalIkkeSetteAksjonspunktForATNårBrukerIkkeHarSøkt() {
		// Arrange
        var månedsinntektGammel = BigDecimal.valueOf(GRUNNBELØP_2017 / 12);
        var månedsinntektNy = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var månedsinntektInntektsmelding = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskravPrÅr = BigDecimal.valueOf(GRUNNBELØP_2017 / 2);
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var månedsinntekter = List.of(månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel,
				månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntektInntektsmelding), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        var grunnlag = settOppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, AktivitetStatus.ATFL,
				List.of(arbeidsforhold), Collections.singletonList(refusjonskravPrÅr), true).getBeregningsgrunnlagPerioder().get(0);

		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert
		assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).isEmpty();
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K9_HJEMMEL_BARE_ARBEIDSTAKER_REFUSJON, AktivitetStatus.ATFL, 12 * månedsinntektInntektsmelding.doubleValue());
		verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_9_8_8_28);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntektInntektsmelding.doubleValue());
	}

	@Test
	void skalSetteAksjonspunktForATMedVarierendeInntekterNårRefusjonLikBeregnetOgIkkeOmsorgspenger() {
		// Arrange
        var månedsinntektGammel = BigDecimal.valueOf(GRUNNBELØP_2017 / 12);
        var månedsinntektNy = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var månedsinntektInntektsmelding = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskravPrÅr = BigDecimal.valueOf(GRUNNBELØP_2017 / 2);
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var månedsinntekter = List.of(månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel, månedsinntektGammel,
				månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy, månedsinntektNy);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold, AktivitetStatus.AT);
		leggTilMånedsinntekterPrStatus(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(månedsinntektInntektsmelding), Inntektskilde.INNTEKTSMELDING, arbeidsforhold, AktivitetStatus.AT);
        var grunnlag = settOppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, AktivitetStatus.ATFL,
				List.of(arbeidsforhold), Collections.singletonList(refusjonskravPrÅr), false).getBeregningsgrunnlagPerioder().get(0);

		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert
		assertThat(resultat.merknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektInntektsmelding.doubleValue());
		verifiserBeregningsgrunnlagHjemmel(grunnlag, AktivitetStatus.ATFL, BeregningsgrunnlagHjemmel.F_14_7_8_30);
		verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntektInntektsmelding.doubleValue());
	}

	@Test
	void skalBeregneMilitærKombinertMedNæringOgArbeid() { // NOSONAR
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, orgnr);
        var refusjonskrav = BigDecimal.valueOf(4.0d * GSNITT_2017 / 12);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Arrays.asList(AktivitetStatus.ATFL_SN, AktivitetStatus.MS),
				singletonList(arbeidsforhold), singletonList(refusjonskrav.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);

		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		@SuppressWarnings("unused") var resultat2 = new RegelFortsettForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.MS, 0);
	}

	@Test
	void skalBeregneAvvikPåArbeidNårPassertFomdatoForIndividuellSammenligning() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(20000);
        var arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, orgnr);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);

		List<BigDecimal> årsinntekter = new ArrayList<>();
		for(var i = 0; i<12; i++) {
			årsinntekter.add(månedsinntekt.add(månedsinntekt));
		}

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, årsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
				singletonList(arbeidsforhold));
		var grunnlagMedFomForIndividuellRegelendring = Beregningsgrunnlag.builder(grunnlag).medFomDatoForIndividuellSammenligningATFLSN(skjæringstidspunkt.minusMonths(1)).build();
		var førstePeriode = grunnlagMedFomForIndividuellRegelendring.getBeregningsgrunnlagPerioder().get(0);

		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(førstePeriode).evaluerRegel(førstePeriode);

		assertThat(resultat.merknader().stream().map(RegelMerknad::getMerknadKode).collect(Collectors.toList())).containsExactly("5038");
		assertThat(førstePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(240000));
		assertThat(førstePeriode.getSammenligningsGrunnlagForType(SammenligningGrunnlagType.AT_FL).orElseThrow().getAvvikPromilleUtenAvrunding()).isEqualByComparingTo(BigDecimal.valueOf(500));
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(førstePeriode, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(førstePeriode, 12 * månedsinntekt.doubleValue());
	}

	@Test
	void skalIkkeBeregneAvvikPåArbeidNårPassertFomdatoForIndividuellSammenligning() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(20000);
        var arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
        var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, orgnr);
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
				årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);

		List<BigDecimal> årsinntekter = new ArrayList<>();
		for(var i = 0; i<12; i++) {
			årsinntekter.add(månedsinntekt.add(månedsinntekt));
		}

		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, årsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
				singletonList(arbeidsforhold));
		var grunnlagMedFomForIndividuellRegelendring = Beregningsgrunnlag.builder(grunnlag).medFomDatoForIndividuellSammenligningATFLSN(skjæringstidspunkt.plusDays(1)).build();
		var førstePeriode = grunnlagMedFomForIndividuellRegelendring.getBeregningsgrunnlagPerioder().get(0);

		// Act
		@SuppressWarnings("unused") var resultat = new RegelForeslåBeregningsgrunnlag(førstePeriode).evaluerRegel(førstePeriode);

		assertThat(resultat.merknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).isEmpty();
		assertThat(førstePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(240000));
		assertThat(førstePeriode.getSammenligningsGrunnlagForType(SammenligningGrunnlagType.AT_FL)).isEmpty();
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(førstePeriode, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
		verifiserBeregningsgrunnlagBeregnet(førstePeriode, 12 * månedsinntekt.doubleValue());
	}

	private void verifiserBeregningsgrunnlagHjemmel(BeregningsgrunnlagPeriode grunnlag, AktivitetStatus aktivitetStatus,
	                                                BeregningsgrunnlagHjemmel hjemmel) {
		assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(aktivitetStatus).getHjemmel()).isEqualTo(hjemmel);
	}

	private void leggtilStatus(Beregningsgrunnlag beregningsgrunnlag, AktivitetStatus aktivitetStatus) {
        var periode = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPeriode.builder(periode)
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(aktivitetStatus)
						.medAndelNr(periode.getBeregningsgrunnlagPrStatus().size() + 1L)
						.build())
				.build();
		Beregningsgrunnlag.builder(beregningsgrunnlag).medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(aktivitetStatus, null))).build();
	}
}
