package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØPLISTE;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2016;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2018;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2019;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilSøknadsinntekt;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBeregnet;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static org.assertj.core.api.Assertions.assertThat;

class RegelForeslåBeregningsgrunnlagFRISINNTest {

    private LocalDate skjæringstidspunkt;
    private String orgnr;
    private Arbeidsforhold arbeidsforhold;

    @BeforeEach
    public void setup() {
        skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);
        orgnr = "987";
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
    }

    @Test
    public void skalBeregneGrunnlagAGVedSammeFrilansInntektSisteTreMåneder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, true);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_FRILANSER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedSammeInntektSisteTreMåneder() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(skjæringstidspunkt, månedsinntekt, refusjonskrav, false);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_ARBEIDSTAKER, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedKombinasjonATFLogSN() {
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor2SisteÅr(5, 3), Inntektskilde.SIGRUN);


        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntekt);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019));
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        LocalDate FRISINN_FOM = LocalDate.of(2017, 1, 1);
        LocalDate FRISINN_TOM = LocalDate.of(2019, 12, 31);
        Periode beregningsperiode = Periode.of(FRISINN_FOM, FRISINN_TOM);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);
        double beløpSN = GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, beløpSN, 3.0 * GSNITT_2019);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, beløpSN + 12 * månedsinntekt.doubleValue());
    }

    @Test
    public void skalBeregneGrunnlagAGVedKombinasjonATFLogSNHvorATFLStørreEnnSNMedAvkorting() {
        // ATFL > 6G, SN < ATFL: ATFL blir avkortet til 6G og SN blir satt til 0.
        // Arrange
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 1.5);
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor2SisteÅr(2, 2), Inntektskilde.SIGRUN);
        BigDecimal inntektSn = BigDecimal.valueOf(GSNITT_2019).multiply(BigDecimal.valueOf(2));
        leggTilSøknadsinntekt(inntektsgrunnlag, inntektSn);

        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntekt);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);

        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN),
            List.of(arbeidsforhold), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        verifiserBeregningsgrunnlagBeregnet(grunnlag, 12 * månedsinntekt.doubleValue() + inntektSn.intValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * månedsinntekt.doubleValue());
        double forventetPGI = 2 * GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, inntektSn.intValue(), forventetPGI);
    }

    @Test
    public void skalBeregneGrunnlagForKombinasjonSNOgDagpenger() {
        // Arrange
        BigDecimal utbetalingsgrad = new BigDecimal("150");
        BigDecimal dagsats = BigDecimal.valueOf(900);
        Inntektsgrunnlag inntektsgrunnlag = lagSnInntekter(5, 5, 5);
        leggTilYtelse36MånederFør(utbetalingsgrad, dagsats, inntektsgrunnlag, LocalDate.of(2020, 4, 1));
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            List.of(AktivitetStatus.SN, AktivitetStatus.DP));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Endre på periode slik at den varer en måned og ikkje går til uendelig
        BeregningsgrunnlagPeriode.builder(grunnlag)
            .medPeriode(Periode.of(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30)));

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);

        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        double expectedbruttoDP = dagsats.doubleValue() * 260 * utbetalingsgrad.intValue()/200;
        double expectedPGIsnitt = 5.0 * GSNITT_2019;
        double expectedBruttoSN = 5.0 * GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.DP, expectedbruttoDP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoDP + expectedBruttoSN);
    }


    @Test
    public void skalBeregneGrunnlagForKombinasjonATFL_SNOgAAP() {
        // Arrange
        BigDecimal utbetalingsgrad = new BigDecimal("100");
        BigDecimal dagsatsAAP = BigDecimal.valueOf(700);
        BigDecimal månedsinntektATFL = BigDecimal.valueOf(20000);
        Inntektsgrunnlag inntektsgrunnlag = lagSnInntekter(6, 6, 6);
        List<BigDecimal> månedsinntekter = Collections.nCopies(12, månedsinntektATFL);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter, Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilYtelse36MånederFør(utbetalingsgrad, dagsatsAAP, inntektsgrunnlag, LocalDate.of(2020, 4, 1));
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP), Collections.singletonList(arbeidsforhold),
            Collections.emptyList());
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        // Endre på periode slik at den varer en måned og ikkje går til uendelig
        BeregningsgrunnlagPeriode.builder(grunnlag)
            .medPeriode(Periode.of(LocalDate.of(2020, 4, 1), LocalDate.of(2020, 4, 30)));

        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        double expectedbruttoAAP = dagsatsAAP.doubleValue() * 260 * utbetalingsgrad.intValue()/200;
        double expectedPGIsnitt = 6.0 * GSNITT_2019;
        double expectedBruttoATFL = 12 * månedsinntektATFL.doubleValue();
        double expectedBruttoSN = 6.0 * GSNITT_2019;
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.KORONALOVEN_3, AktivitetStatus.AAP, expectedbruttoAAP);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, expectedBruttoATFL);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, expectedBruttoSN, expectedPGIsnitt);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, expectedbruttoAAP + expectedBruttoSN + expectedBruttoATFL);
    }

    @Test
    public void skalFastsetteBeregningsperiondenUtenInntektDeTreSisteMånederAT(){
        // arbeidstaker uten inntektsmelding OG det finnes ikke inntekt i de tre siste månedene
        // før skjæringstidspunktet (beregningsperioden)
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        BigDecimal refusjonskrav = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);

        List<BigDecimal> månedsinntekter18 = Collections.nCopies(12, månedsinntekt);
        List<BigDecimal> månedsinntekter19 = Collections.nCopies(12, BigDecimal.ZERO);

        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt.minusMonths(12), månedsinntekter18,
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, månedsinntekter19,
            Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING,arbeidsforhold);
        Beregningsgrunnlag beregningsgrunnlag =  settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
            List.of(arbeidsforhold), Collections.emptyList());

        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);

        String sporing = EvaluationSerializer.asJson(evaluation);
        assertThat(sporing).isNotBlank();
        //SÅ skal brutto beregningsgrunnlag i beregningsperioden settes til 0
        assertThat(grunnlag.getBruttoPrÅr().compareTo(BigDecimal.ZERO)).isZero();
    }

    @Test
    public void skalIkkeTaMedÅrsinntekterUnder3kvartG() {
        // Arrange
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor2SisteÅr(0, 1), Inntektskilde.SIGRUN);


        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019));
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.SN),
            Collections.emptyList(), Collections.emptyList()).getBeregningsgrunnlagPerioder().get(0);
        // Act
        Evaluation evaluation = new RegelForeslåBeregningsgrunnlagFRISINN(grunnlag).evaluer(grunnlag);
        // Assert
        @SuppressWarnings("unused")
        String sporing = EvaluationSerializer.asJson(evaluation);

        LocalDate FRISINN_FOM = LocalDate.of(2017, 1, 1);
        LocalDate FRISINN_TOM = LocalDate.of(2019, 12, 31);
        Periode beregningsperiode = Periode.of(FRISINN_FOM, FRISINN_TOM);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_BARE_SELVSTENDIG, AktivitetStatus.SN, GSNITT_2019, GSNITT_2019);
        verifiserBeregningsgrunnlagBeregnet(grunnlag, GSNITT_2019);
    }

    private static List<BigDecimal> årsinntekterFor2SisteÅr(double pgi2, double pgi1) {
        return Arrays.asList(BigDecimal.valueOf(pgi2 * GSNITT_2017), BigDecimal.valueOf(pgi1 * GSNITT_2018), BigDecimal.valueOf(pgi1 * GSNITT_2016));
    }

    private Inntektsgrunnlag lagSnInntekter(int g2017, int g2018, int g2019) {
        Inntektsgrunnlag inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt,
            årsinntekterFor2SisteÅr(g2017, g2018), Inntektskilde.SIGRUN);
        leggTilSøknadsinntekt(inntektsgrunnlag, BigDecimal.valueOf(GSNITT_2019).multiply(BigDecimal.valueOf(g2019)));
        return inntektsgrunnlag;
    }

    private void leggTilYtelse36MånederFør(BigDecimal utbetalingsgrad, BigDecimal dagsats, Inntektsgrunnlag inntektsgrunnlag, LocalDate dato) {
        for (int i = 0; i < 36; i++) {
            inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
                .medMåned(dato.minusMonths(i))
                .medInntekt(dagsats)
                .medUtbetalingsgrad(utbetalingsgrad)
                .build());
        }
    }

}
