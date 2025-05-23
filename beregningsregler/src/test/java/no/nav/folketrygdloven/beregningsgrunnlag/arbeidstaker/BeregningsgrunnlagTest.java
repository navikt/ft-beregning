package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.kopierOgLeggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilArbeidsforholdMedInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilArbeidsforholdUtenInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserRegelmerknad;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

class BeregningsgrunnlagTest {

	private static final String ORGNR2 = "999999999";
	private final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);


	@Test
	void skalGiRegelmerknadVedNullFrilansInntektSisteTreMåneder() {
		// Arrange
        var månedsinntekt = BigDecimal.ZERO;
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var månedsinntektSammenligning = BigDecimal.valueOf(5000);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektSammenligning);
		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert
		assertThat(regelResultat.versjon()).isNotNull();
		assertThat(regelResultat.sporing().versjon()).isNotNull();
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserRegelmerknad(regelResultat, "5038");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagAGVedSammeFrilansInntektSisteTreMåneder() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert

		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagUtenInntektsmeldingN1N2N3() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false, 3);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagUtenInntektsmeldingN1N3() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var skjæringstidspunkt2 = LocalDate.of(2018, Month.APRIL, 26);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt2, månedsinntekt, null, true, 1);

        var inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt2.minusMonths(2), List.of(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt2, månedsinntekt);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserRegelmerknad(regelResultat, "5038");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue() * 2 / 3);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagUtenInntektsmeldingN1N2() {
		// Arrange
        var månedsinntektForSG = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 3);
        var månedsinntektForBG = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = månedsinntektForBG;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntektForBG, refusjonskrav, false, 2);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektForSG);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		//Beløp er satt presis slik at det blir (beregnet verdi)-0.01<beløp<(beregnet verdi)+0.01
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * 2600.66666);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagUtenInntektsmeldingN1() {
		// Arrange
        var månedsinntektForBG = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var månedsinntektForSG = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 6);
        var refusjonskrav = månedsinntektForSG;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntektForBG, refusjonskrav, false, 1);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektForSG);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		//Beløp er satt presis slik at det blir (beregnet verdi)-0.01<beløp<(beregnet verdi)+0.01
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * 1300.3333);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagAGUtenInntektsmeldingMedLønnsendring1Måned() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        var månedsinntektFraSaksbehandler = BigDecimal.valueOf(28000);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
        var beregningsgrunnlagPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(månedsinntektFraSaksbehandler.multiply(BigDecimal.valueOf(12)))
				.build();

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagAGUtenInntektsmeldingMedLønnsendring2Måneder() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        var inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
		opprettSammenligningsgrunnlag(inntektsgrunnlag, skjæringstidspunkt, månedsinntekt);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        var månedsinntektFraSaksbehandler = BigDecimal.valueOf(28000);
        var beregningsgrunnlagPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(månedsinntektFraSaksbehandler.multiply(BigDecimal.valueOf(12)))
				.build();

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagAGUtenInntektsmeldingMedLønnsendring3Måneder() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        var månedsinntektFraSaksbehandler = BigDecimal.valueOf(28000);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
        var beregningsgrunnlagPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(månedsinntektFraSaksbehandler.multiply(BigDecimal.valueOf(12)))
				.build();

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalGiRegelmerknadVedAvvikVedLønnsøkning() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        var månedsinntektFraSaksbehandler = BigDecimal.valueOf(35000);  // 40% avvik, dvs > 25% avvik
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);
        var beregningsgrunnlagPrArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(beregningsgrunnlagPrArbeidsforhold)
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(månedsinntektFraSaksbehandler.multiply(BigDecimal.valueOf(12)))
				.build();

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		verifiserRegelmerknad(regelResultat, "5038");
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagAGMedInntektsmelding() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        var inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0).getArbeidsforhold();

        var månedsinntektFraInntektsmelding = månedsinntekt.multiply(BigDecimal.valueOf(1.1));
		leggTilMånedsinntekter(inntektsgrunnlag, beregningsgrunnlag.getSkjæringstidspunkt(),
				List.of(månedsinntektFraInntektsmelding), Inntektskilde.INNTEKTSMELDING, arbeidsforhold);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraInntektsmelding.doubleValue());
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void toArbeidsforholdMedInntektsmelding() {
		// Arrange
        var månedsinntekt1 = BigDecimal.valueOf(15000);
        var månedsinntekt2 = BigDecimal.valueOf(25000);
        var refusjonskrav = BigDecimal.valueOf(25000);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntekt1, refusjonskrav);

		kopierOgLeggTilMånedsinntekter(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt1.add(månedsinntekt2), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, null, 12);

        var arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
        var arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, ORGNR2);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		leggTilArbeidsforholdMedInntektsmelding(grunnlag, skjæringstidspunkt, månedsinntekt2, refusjonskrav, arbeidsforhold2, BigDecimal.ZERO, null);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt1.add(månedsinntekt2).doubleValue());
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void toFrilansArbeidsforhold() {
		// Arrange
        var månedsinntekt1 = BigDecimal.valueOf(15000);
        var månedsinntekt2 = BigDecimal.valueOf(25000);
        var refusjonskrav = BigDecimal.valueOf(25000);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt1, refusjonskrav, true);

		kopierOgLeggTilMånedsinntekter(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt1.add(månedsinntekt2), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, null, 12);

        var arbeidsforhold2 = Arbeidsforhold.frilansArbeidsforhold();
		kopierOgLeggTilMånedsinntekter(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntekt2, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold2, 12);
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * (månedsinntekt1.add(månedsinntekt2)).doubleValue());
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		assertThat(af.getBeregningsperiode()).isNotNull();
	}

	@Test
	void kombinasjonArbeidstakerOgFrilans() {
		// Arrange
        var månedsinntektFrilans = BigDecimal.valueOf(15000);
        var månedsinntektArbeidstaker = BigDecimal.valueOf(25000);
        var refusjonskravArbeidstaker = BigDecimal.valueOf(25000);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntektArbeidstaker, refusjonskravArbeidstaker);

		kopierOgLeggTilMånedsinntekter(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektFrilans.add(månedsinntektArbeidstaker), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, null, 12);

        var frilans = Arbeidsforhold.frilansArbeidsforhold();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		leggTilArbeidsforholdUtenInntektsmelding(grunnlag, skjæringstidspunkt, månedsinntektFrilans, null, frilans);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER, AktivitetStatus.ATFL, 12 * (månedsinntektFrilans.add(månedsinntektArbeidstaker)).doubleValue());
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void kombinasjonArbeidstakerOgFrilansDerFrilansinntektErNull() {
		// Arrange
        var månedsinntektFrilans = BigDecimal.ZERO;
        var månedsinntektArbeidstaker = BigDecimal.valueOf(25000);
        var refusjonskravFrilans = BigDecimal.ZERO;
        var refusjonskravArbeidstaker = BigDecimal.valueOf(25000);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, månedsinntektArbeidstaker, refusjonskravArbeidstaker);

		kopierOgLeggTilMånedsinntekter(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektFrilans.add(månedsinntektArbeidstaker), Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING, null, 12);

        var frilans = Arbeidsforhold.frilansArbeidsforhold();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		leggTilArbeidsforholdUtenInntektsmelding(grunnlag, skjæringstidspunkt, månedsinntektFrilans, refusjonskravFrilans, frilans);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_FRILANSER, AktivitetStatus.ATFL, 12 * (månedsinntektFrilans.add(månedsinntektArbeidstaker)).doubleValue());
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalTesteManueltFastsattMånedsinntekt() {
        var beregnetPrÅr = 250000;
        var arbeidsforholdList = Collections.singletonList(Arbeidsforhold.builder().medOrgnr("123").medStartdato(skjæringstidspunkt.minusYears(2)).medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build());
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, new Inntektsgrunnlag(),
				Collections.singletonList(AktivitetStatus.ATFL), arbeidsforholdList);
		opprettSammenligningsgrunnlag(beregningsgrunnlag.getInntektsgrunnlag(), skjæringstidspunkt, BigDecimal.valueOf(22500));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		BeregningsgrunnlagPrArbeidsforhold.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0))
				.medFastsattAvSaksbehandler(true)
				.medBeregnetPrÅr(BigDecimal.valueOf(beregnetPrÅr))
				.build();

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.sporing().sporing()).contains(BeregnRapportertInntektVedManuellFastsettelse.ID);

		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		assertThat(af.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(beregnetPrÅr));
	}
}
