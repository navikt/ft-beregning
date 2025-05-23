package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlag;
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
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

class BeregningsgrunnlagTidsbegrensetArbeidsforholdTest {

	private final LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);

	@Test
	void skalBeregneGrunnlagUtenInntektsmeldingN1N3MedTidsbegrensetArbeidsforhold() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var skjæringstidspunkt2 = LocalDate.of(2018, Month.APRIL, 26);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt2, månedsinntekt, null, true, 1, List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET));

        var inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt2.minusMonths(2), List.of(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt2, månedsinntekt);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserRegelmerknad(regelResultat, "5047");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue() * 2 / 3);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalBeregneGrunnlagUtenInntektsmeldingN1N3MedTidsbegrensetArbeidsforholdSammenfallerMedBortfaltNaturalytelse() {
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var skjæringstidspunkt2 = LocalDate.of(2018, Month.APRIL, 26);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt2, månedsinntekt, null, true, 1, List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET, PeriodeÅrsak.NATURALYTELSE_BORTFALT));

        var inntektsgrunnlag = beregningsgrunnlag.getInntektsgrunnlag();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var arbeidsforhold = Arbeidsforhold.frilansArbeidsforhold();
		leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt2.minusMonths(2), List.of(månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt2, månedsinntekt);

		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

		// Assert
		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserRegelmerknad(regelResultat, "5047");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue() * 2 / 3);
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalGiRegelmerknadVedNullFrilansInntektSisteTreMånederOgTidsbegrensetArbeidsforhold() {
		// Arrange
        var månedsinntekt = BigDecimal.ZERO;
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var månedsinntektSammenligning = BigDecimal.valueOf(5000);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, Collections.singletonList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektSammenligning);
		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert

		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserRegelmerknad(regelResultat, "5047");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalGiRegelmerknadVedNullFrilansInntektSisteTreMånederOgTidsbegrensetArbeidsforholdSammenfallendeMedNaturalytelse() {
		// Arrange
        var månedsinntekt = BigDecimal.ZERO;
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var månedsinntektSammenligning = BigDecimal.valueOf(5000);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true, List.of(PeriodeÅrsak.NATURALYTELSE_BORTFALT, PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
		opprettSammenligningsgrunnlag(grunnlag.getInntektsgrunnlag(), skjæringstidspunkt, månedsinntektSammenligning);
		// Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);
		// Assert

		assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
		verifiserRegelmerknad(regelResultat, "5047");
		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalGiRegelmerknadVedAvvikVedLønnsøkningOgTidsbegrensetArbeidsforhold() { // NOSONAR
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false, Collections.singletonList(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET));
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
		verifiserRegelmerknad(regelResultat, "5047");

		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}

	@Test
	void skalGiRegelmerknadVedAvvikVedLønnsøkningOgTidsbegrensetArbeidsforholdSammenfallerMedBortfaltNaturalytelse() { // NOSONAR
		// Arrange
        var månedsinntekt = BigDecimal.valueOf(25000);
        var refusjonskrav = månedsinntekt;
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false, List.of(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET, PeriodeÅrsak.NATURALYTELSE_BORTFALT));
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
		verifiserRegelmerknad(regelResultat, "5047");

		verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntektFraSaksbehandler.doubleValue());
        var beregningsperiode = Periode.månederFør(beregningsgrunnlag.getSkjæringstidspunkt(), 3);
        var af = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
		verifiserBeregningsperiode(af, beregningsperiode);
	}
}
