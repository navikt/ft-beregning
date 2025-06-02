package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2019;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilSøknadsinntekt;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlagFRISINN.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlagFRISINN.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlagFRISINN.verifiserBeregningsperiode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnPeriode;

class RegelForeslåBeregningsgrunnlagFRISINNTest {

    private LocalDate skjæringstidspunkt;
    private String orgnr;
    private Arbeidsforhold arbeidsforhold;
    private FrisinnGrunnlag frisinnGrunnlag;

    @BeforeEach
    void setup() {
        skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);
        orgnr = "987";
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
        frisinnGrunnlag = lagFrisinnGrunnlag();
    }

    @Test
    void skalBeregneGrunnlagAGVedSammeFrilansInntektSisteTreMåneder() { // NOSONAR
        // Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true);
        beregningsgrunnlag = Beregningsgrunnlag.builder(beregningsgrunnlag).medYtelsesSpesifiktGrunnlag(frisinnGrunnlag).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
	    @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluerRegel(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    void skalBeregneGrunnlagAGVedSammeInntektSisteTreMåneder() { // NOSONAR
        // Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        beregningsgrunnlag = Beregningsgrunnlag.builder(beregningsgrunnlag).medYtelsesSpesifiktGrunnlag(frisinnGrunnlag).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
	    @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluerRegel(grunnlag);
        // Assert

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    void skalBeregneGrunnlagAGVedKombinasjonATFLogSN() { // NOSONAR
        // Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        var inntektsgrunnlag = new Inntektsgrunnlag();

        var månedsinntekter = Collections.nCopies(12, månedsinntekt);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019));
        var grunnlag = settoppGrunnlagMedEnPeriode(
            skjæringstidspunkt,
            inntektsgrunnlag,
            List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold),
            Collections.emptyList(),
            Optional.of(frisinnGrunnlag)
        ).getBeregningsgrunnlagPerioder().get(0);

        // Act
	    @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluerRegel(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        var FRISINN_FOM = LocalDate.of(2017, 1, 1);
        var FRISINN_TOM = LocalDate.of(2019, 12, 31);
        var beregningsperiode = Periode.of(FRISINN_FOM, FRISINN_TOM);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.KORONALOVEN_3, grunnlag, beregningsperiode);
        double beløpSN = GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, beløpSN);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, beløpSN + 12 * månedsinntekt.doubleValue());
    }

    @Test
    void skalBeregneGrunnlagAGVedKombinasjonATFLogSNHvorATFLStørreEnnSNMedAvkorting() { // NOSONAR
        // ATFL > 6G, SN < ATFL: ATFL blir avkortet til 6G og SN blir satt til 0.
        // Arrange
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
        var inntektsgrunnlag = new Inntektsgrunnlag();
        var inntektSn = BigDecimal.valueOf(GSNITT_2019).multiply(BigDecimal.valueOf(2));
        leggTilSøknadsinntekt(inntektsgrunnlag, inntektSn);

        var månedsinntekter = Collections.nCopies(12, månedsinntekt);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold), Collections.emptyList(), Optional.of(frisinnGrunnlag)).getBeregningsgrunnlagPerioder().get(0);
        // Act
	    @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluerRegel(grunnlag);
        // Assert
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue() + inntektSn.intValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, inntektSn.intValue());
    }

    @Test
    void skalBeregneGrunnlagForKombinasjonSNOgDagpenger() { // NOSONAR
        // Arrange
        var utbetalingsfaktor = new BigDecimal("0.75");
        var dagsats = BigDecimal.valueOf(900);
        var inntektsgrunnlag = lagSnInntekter(5);
        leggTilYtelseMånederFør(utbetalingsfaktor, dagsats, inntektsgrunnlag, LocalDate.of(2020, 4, 1), 38);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            List.of(AktivitetStatus.SN, AktivitetStatus.DP), Optional.of(frisinnGrunnlag));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Endre på periode slik at den varer en måned og ikkje går til uendelig
        BeregningsgrunnlagPeriode.builder(grunnlag)
            .medPeriode(Periode.of(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30)));

        // Act
	    @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluerRegel(grunnlag);

        // Assert
        var expectedbruttoDP = dagsats.doubleValue() * 260 * utbetalingsfaktor.doubleValue();
        var expectedBruttoSN = 5.0 * GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.DP, expectedbruttoDP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, expectedBruttoSN);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoDP + expectedBruttoSN);
    }


    @Test
    void skalBeregneGrunnlagForKombinasjonATFL_SNOgAAP_ytelse_36_måneder() { // NOSONAR
        // Arrange
        var utbetalingsfaktor = new BigDecimal("0.5");
        var dagsatsAAP = BigDecimal.valueOf(700);
        var månedsinntektATFL = BigDecimal.valueOf(20000);
        var inntektsgrunnlag = lagSnInntekter(6);
        var månedsinntekter = Collections.nCopies(12, månedsinntektATFL);
        leggTilMånedsinntekter(inntektsgrunnlag, LocalDate.of(2020, 5, 1), månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilYtelseMånederFør(utbetalingsfaktor, dagsatsAAP, inntektsgrunnlag, LocalDate.of(2020, 4, 1), 38);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP), Collections.singletonList(arbeidsforhold),
            Collections.emptyList(), Optional.of(frisinnGrunnlag));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Endre på periode slik at den varer en måned og ikkje går til uendelig
        BeregningsgrunnlagPeriode.builder(grunnlag)
            .medPeriode(Periode.of(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30)));

        // Act
	    @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluerRegel(grunnlag);
        // Assert
        var expectedbruttoAAP = dagsatsAAP.doubleValue() * 260 * utbetalingsfaktor.doubleValue();
        var expectedBruttoATFL = 12 * månedsinntektATFL.doubleValue();
        var expectedBruttoSN = 6.0 * GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.AAP, expectedbruttoAAP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.ATFL, expectedBruttoATFL);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, expectedBruttoSN);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoAAP + expectedBruttoSN + expectedBruttoATFL);
    }

    @Test
    void skalFastsetteBeregningsperiondenUtenInntektDeTreSisteMånederAT() {
        // arbeidstaker uten inntektsmelding OG det finnes ikke inntekt i de tre siste månedene
        // før skjæringstidspunktet (beregningsperioden)
        var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);

        var månedsinntekter18 = Collections.nCopies(12, månedsinntekt);
        var månedsinntekter19 = Collections.nCopies(12, BigDecimal.ZERO);

        var inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt.minusMonths(12), månedsinntekter18,
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter19,
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), Collections.emptyList(), Optional.of(frisinnGrunnlag));

        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        var regelResultat = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluerRegel(grunnlag);

        assertThat(regelResultat.sporing().sporing()).isNotBlank();
        //SÅ skal brutto beregningsgrunnlag i beregningsperioden settes til 0
        assertThat(grunnlag.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void skalIkkeTaMedÅrsinntekterUnder3kvartG() { // NOSONAR
        // Arrange
        var inntektsgrunnlag = new Inntektsgrunnlag();

        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019));
        var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.SN),
            Collections.emptyList(), Collections.emptyList(), Optional.of(frisinnGrunnlag)).getBeregningsgrunnlagPerioder().get(0);
        // Act
	    @SuppressWarnings("unused") var regelResultat = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluerRegel(grunnlag);
        // Assert
        var FRISINN_FOM = LocalDate.of(2017, 1, 1);
        var FRISINN_TOM = LocalDate.of(2019, 12, 31);
        var beregningsperiode = Periode.of(FRISINN_FOM, FRISINN_TOM);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.KORONALOVEN_3, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.SN, GSNITT_2019);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, GSNITT_2019);
    }

    private Inntektsgrunnlag lagSnInntekter(int g2019) {
        var inntektsgrunnlag = new Inntektsgrunnlag();
        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019).multiply(BigDecimal.valueOf(g2019)));
        return inntektsgrunnlag;
    }

    private void leggTilYtelseMånederFør(BigDecimal utbetalingsgrad, BigDecimal dagsats, Inntektsgrunnlag inntektsgrunnlag, LocalDate dato, int månederFør) {
        for (var i = 0; i < månederFør; i++) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
                .medMåned(dato.minusMonths(i))
                .medInntekt(dagsats)
                .medUtbetalingsfaktor(utbetalingsgrad)
                .build());
        }
    }

    private static List<FrisinnPeriode> lagFrisinnperioder(Periode periode) {
        return Collections.singletonList(new FrisinnPeriode(periode, true, true));
    }

    private FrisinnGrunnlag lagFrisinnGrunnlag() {
        return new FrisinnGrunnlag(lagFrisinnperioder(Periode.of(skjæringstidspunkt, null)), List.of(Periode.of(skjæringstidspunkt, null)), skjæringstidspunkt);
    }
}
