package no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GSNITT_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.leggTilMånedsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppÅrsinntekter;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.årsinntekterFor3SisteÅr;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsperiode;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak.FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET;
import static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak.VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT;
import static no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter.getRegelResultat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.RegelBeregningsgrunnlagSN;

class RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNTest {

    private LocalDate skjæringstidspunkt;
    private Arbeidsforhold arbeidsforhold;
    private Arbeidsforhold arbeidsforhold2;
    private BigDecimal Gverdi;
    private static final BigDecimal TOLV = new BigDecimal("12");

    @BeforeEach
    void setup() {
        skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
        var arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
        arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato,"123");
        arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, "frilans");
        Gverdi = BigDecimal.valueOf(GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonUtenVarigEndringOppgitt() {
        // Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 3, 4),
            Inntektskilde.SIGRUN);

        var inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert

        var regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        var beløp = Gverdi.multiply(new BigDecimal("4"));
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, inntektATFL.multiply(TOLV), beløp.subtract(TOLV.multiply(inntektATFL)));
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringOppgittUnder25ProsentAvvik() {

        // Arrange
        var sigrun = new BigDecimal("5").multiply(Gverdi);
        var inntektATFL = new BigDecimal("18000");
        var soknadInntekt = new BigDecimal("369120");
        var sammenligningsgrunnlag = inntektATFL.multiply(TOLV).add(soknadInntekt);
        //Avvik = 24.98%
        var avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();


        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        var regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, inntektATFL.multiply(TOLV), sigrun.subtract(TOLV.multiply(inntektATFL)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringOppgittOver25ProsentAvvik() {

        // Arrange
        var sigrun = new BigDecimal("5").multiply(Gverdi);
        var inntektATFL = new BigDecimal("80000");
        var soknadInntekt = new BigDecimal("309240");
        var sammenligningsgrunnlag = inntektATFL.multiply(TOLV).add(soknadInntekt);
        //Avvik = 25.01%
        var avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();


        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert

        var regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.merknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).containsExactly(VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT);


        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.01)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedNaturalYtelserBortfalt() {
        // Arrange
        var sigrun = new BigDecimal("5").multiply(Gverdi);
        var inntektATFL = new BigDecimal("80000");
        var soknadInntekt = new BigDecimal("309240");
        var naturalytelseBeløp = new BigDecimal("5000");
        var sammenligningsgrunnlag = inntektATFL.add(naturalytelseBeløp).multiply(TOLV).add(soknadInntekt);

        var avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var naturalYtelser = Collections.singletonList(new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt.minusDays(1)));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medMåned(skjæringstidspunkt)
            .medInntekt(inntektATFL)
            .medNaturalYtelser(naturalYtelser)
            .build());

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert

        var regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.merknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).containsExactly(VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT);


        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedNaturalYtelserBortfaltMedFlerePerioder() {
        // Arrange
        var sigrun = new BigDecimal("5").multiply(Gverdi);
        var inntektATFL = new BigDecimal("80000");
        var soknadInntekt = new BigDecimal("309240");
        var naturalytelseBeløp = new BigDecimal("5000");
        var sammenligningsgrunnlag = inntektATFL.add(naturalytelseBeløp).multiply(TOLV).add(soknadInntekt);
        var naturalytelseOpphørFom = skjæringstidspunkt.plusMonths(4);

        var avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 5, 5),
            Inntektskilde.SIGRUN);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);

        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var naturalYtelser = List.of(new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt.minusDays(1)),
            new NaturalYtelse(naturalytelseBeløp, null, skjæringstidspunkt.plusMonths(4).minusDays(1)));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medMåned(skjæringstidspunkt)
            .medInntekt(inntektATFL)
            .medNaturalYtelser(naturalYtelser)
            .build());

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.ZERO));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        var andrePeriode = BeregningsgrunnlagPeriode.builder()
            .medPeriode(Periode.of(naturalytelseOpphørFom, null))
            .build();

        kopierBeregningsgrunnlagPeriode(grunnlag, andrePeriode);

        Beregningsgrunnlag.builder(beregningsgrunnlag)
            .medBeregningsgrunnlagPeriode(andrePeriode)
            .build();

        // Act
        var evaluationp1 = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2p1 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        var evaluation1p2 = new RegelBeregningsgrunnlagATFL(andrePeriode).getSpecification().evaluate(andrePeriode);
        var evaluation2p2 = new RegelBeregningsgrunnlagSN().evaluer(andrePeriode);


        // Assert

        var regelResultat1 = getRegelResultat(evaluation2p1, "input");
        var regelResultat2 = getRegelResultat(evaluation2p2, "input");
        assertThat(regelResultat1.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat1.merknader().stream().map(RegelMerknad::utfallÅrsak).collect(Collectors.toList())).containsExactly(VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT);


        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.ATFL, 12 * inntektATFL.doubleValue());
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, AktivitetStatus.SN, 0.0d, 5d * GRUNNBELØP_2017);

        //Verifiser andre periode
        assertThat(andrePeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).samletNaturalytelseBortfaltMinusTilkommetPrÅr()).isCloseTo(naturalytelseBeløp.multiply(TOLV).multiply(BigDecimal.valueOf(2)), within(BigDecimal.valueOf(0.001)));
        verifiserSammenligningsgrunnlag(andrePeriode, sammenligningsgrunnlag, avvik);
        assertThat(regelResultat2.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedDagpenger() {
        // Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 3, 4),
            Inntektskilde.SIGRUN);
        var inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.DP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var bruttoDP = BigDecimal.valueOf(100000);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        var regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        var gjennomsnittligPGI = Gverdi.multiply(new BigDecimal("4"));
        var beløpATFL = inntektATFL.multiply(TOLV);
        var beløpSN = gjennomsnittligPGI.subtract(beløpATFL).subtract(bruttoDP);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringMedAAP() {
        // Arrange
        var sigrun = BigDecimal.valueOf(540996.444434);
        var inntektATFL = new BigDecimal("18000");
        var soknadInntekt = new BigDecimal("600000");
        var bruttoAAP = BigDecimal.valueOf(49500);
        var sammenligningsgrunnlag = inntektATFL.multiply(TOLV).add(soknadInntekt).add(bruttoAAP);
        var avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(5, 6, 7),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.AAP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP)).medBeregnetPrÅr(bruttoAAP).build();

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        var regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        var beløpATFL = inntektATFL.multiply(TOLV);
        var beløpSN = sigrun.subtract(beløpATFL).subtract(bruttoAAP);
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    void skalBeregneBeregningsgrunnlagForKombinasjonMedVarigEndringMedDP() {
        // Arrange
        var sigrun = BigDecimal.valueOf(655438);
        var inntektATFL = new BigDecimal("18000");
        var soknadInntekt = new BigDecimal("420000");
        var bruttoDP = BigDecimal.valueOf(74880);
        var sammenligningsgrunnlag = inntektATFL.multiply(TOLV).add(soknadInntekt).add(bruttoDP);
        var avvik = sammenligningsgrunnlag.subtract(sigrun).divide(sigrun, 20, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).abs();

        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(8, 9, 10),
            Inntektskilde.SIGRUN);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(soknadInntekt),
            Inntektskilde.SØKNAD, arbeidsforhold2);
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL_SN, AktivitetStatus.DP),
            Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).medBeregnetPrÅr(bruttoDP).build();

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        var regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);

        var beløpATFL = inntektATFL.multiply(TOLV);
        var beløpSN = sigrun.subtract(beløpATFL).subtract(bruttoDP);
        verifiserSammenligningsgrunnlag(grunnlag, sammenligningsgrunnlag, avvik);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, beløpATFL, beløpSN);
    }

    @Test
    void skalGiRegelmerknadForSNSomErNyIArbeidslivet() {
        // Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(
            skjæringstidspunkt,
            årsinntekterFor3SisteÅr(6, 3, 0),
            Inntektskilde.SIGRUN);
        var inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt,
            Collections.singletonList(inntektATFL),
            Inntektskilde.INNTEKTSMELDING, arbeidsforhold);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Collections.singletonList(AktivitetStatus.ATFL_SN), Collections.singletonList(arbeidsforhold),
            Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017).multiply(BigDecimal.valueOf(12)))));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN)).medErNyIArbeidslivet(true);

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        var regelResultat = getRegelResultat(evaluation2, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.IKKE_BEREGNET);
        assertThat(regelResultat.merknader().get(0).utfallÅrsak()).isEqualTo(FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getBeregnetPrÅr()).isCloseTo(inntektATFL.multiply(TOLV), within(BigDecimal.valueOf(0.01)));
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getGjennomsnittligPGI()).isNotNull();
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN).getPgiListe()).hasSize(3);
    }

    @Test
    void skalIkkeBeregneSNNårFastsattAvSaksbehandler() {
        // Arrange
        var inntektsgrunnlag = settoppÅrsinntekter(skjæringstidspunkt, årsinntekterFor3SisteÅr(5, 3, 4), Inntektskilde.SIGRUN);
        var inntektATFL = new BigDecimal("15000");
        leggTilMånedsinntekter(inntektsgrunnlag, skjæringstidspunkt, Collections.singletonList(inntektATFL), Inntektskilde.INNTEKTSMELDING, arbeidsforhold);

        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL_SN),
            Collections.singletonList(arbeidsforhold), Collections.singletonList(BigDecimal.valueOf(4).multiply(BigDecimal.valueOf(GSNITT_2017*12))));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var statusAT = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        var statusSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrArbeidsforhold.builder(statusAT).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(232323));
        BeregningsgrunnlagPrStatus.builder(statusSN).medFastsattAvSaksbehandler(true).medBeregnetPrÅr(BigDecimal.valueOf(323232));

        // Act
        var evaluation = new RegelBeregningsgrunnlagATFL(grunnlag).getSpecification().evaluate(grunnlag);
        var evaluation2 = new RegelBeregningsgrunnlagSN().evaluer(grunnlag);

        // Assert
        var regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var beregningsperiode = Periode.heleÅrFør(skjæringstidspunkt, 3);
        verifiserBeregningsperiode(AktivitetStatus.SN, BeregningsgrunnlagHjemmel.K14_HJEMMEL_ARBEIDSTAKER_OG_SELVSTENDIG, grunnlag, beregningsperiode);
        verifiserBeregningsgrunnlagBruttoATFL_SN(grunnlag, BigDecimal.valueOf(232323), BigDecimal.valueOf(323232));
    }


    private void verifiserBeregningsgrunnlagBruttoATFL_SN(BeregningsgrunnlagPeriode grunnlag, BigDecimal beløpATFL, BigDecimal beløpSN) {
        var bgpsaATFL = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        assertThat(bgpsaATFL).isNotNull();
        assertThat(bgpsaATFL.getBeregnetPrÅr()).isCloseTo(beløpATFL, within(BigDecimal.valueOf(0.01)));

        var bgpsaSN = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        assertThat(bgpsaSN.getBeregnetPrÅr()).isCloseTo(beløpSN, within(BigDecimal.valueOf(0.01)));
    }

    private void verifiserSammenligningsgrunnlag(BeregningsgrunnlagPeriode grunnlag, BigDecimal beløp, BigDecimal prosent) {
        var sg = grunnlag.getSammenligningsGrunnlagForTypeEllerFeil(SammenligningGrunnlagType.SN);
        assertThat(sg).isNotNull();
        assertThat(sg.getRapportertPrÅr()).isCloseTo(beløp, within(BigDecimal.valueOf(0.01)));
        assertThat(sg.getAvvikProsent()).isCloseTo(prosent, within(BigDecimal.valueOf(0.01)));
        assertThat(sg.getAvvikPromilleUtenAvrunding().setScale(0, RoundingMode.HALF_UP))
		        .isEqualByComparingTo(prosent.movePointRight(1).setScale(0, RoundingMode.HALF_UP));
    }

    private void kopierBeregningsgrunnlagPeriode(BeregningsgrunnlagPeriode grunnlag, BeregningsgrunnlagPeriode kopi) {
        for (var forrigeStatus : grunnlag.getBeregningsgrunnlagPrStatus()) {
            if (forrigeStatus.erArbeidstakerEllerFrilanser()) {
                var ny = BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .build();
                for (var kopierFraArbeidsforhold : forrigeStatus.getArbeidsforhold()) {
                    var kopiertArbeidsforhold = BeregningsgrunnlagPrArbeidsforhold.builder()
                        .medArbeidsforhold(kopierFraArbeidsforhold.getArbeidsforhold())
                        .medAndelNr(kopierFraArbeidsforhold.getAndelNr())
                        .build();
                    BeregningsgrunnlagPrStatus.builder(ny).medArbeidsforhold(kopiertArbeidsforhold).build();
                }
            } else {
                BeregningsgrunnlagPrStatus.builder()
                    .medAktivitetStatus(forrigeStatus.getAktivitetStatus())
                    .medBeregnetPrÅr(forrigeStatus.getBeregnetPrÅr())
                    .medBeregningsgrunnlagPeriode(kopi)
                    .medAndelNr(forrigeStatus.getAndelNr())
                    .build();
            }
        }
    }
}
