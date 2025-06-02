package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;


import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.VerifiserBeregningsgrunnlag.verifiserBeregningsgrunnlagBruttoPrPeriodeType;
import static no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter.getRegelResultat;
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
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

class RegelFastsettBeregningsgrunnlagDPellerAAPTest {

    private LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
    @Test
    void skalForeslåBeregningsgrunnlagForDagpenger() {
        //Arrange
        var dagsats = new BigDecimal("1142");
        var inntektsgrunnlag = lagInntektsgrunnlag(dagsats, skjæringstidspunkt, 0.75);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            Collections.singletonList(AktivitetStatus.DP));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert

        var regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

        var periode = new Periode(skjæringstidspunkt, null);
        assertThat(grunnlag.getBeregningsgrunnlagPeriode()).isEqualTo(periode);

        var brutto = BigDecimal.valueOf(296920).stripTrailingZeros();
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7_8_49, AktivitetStatus.DP, brutto.doubleValue());
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        assertThat(bgps.getBeregnetPrÅr()).isEqualByComparingTo(brutto);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(brutto);
        assertThat(bgps.getOrginalDagsatsFraTilstøtendeYtelse()).isEqualTo(dagsats.longValue());
    }

    @Test
    void skalForeslåBeregningsgrunnlagForAAP() {
        //Arrange
        var dagsats = new BigDecimal("1611");
        var inntektsgrunnlag = lagInntektsgrunnlag(dagsats, skjæringstidspunkt, 0.75);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.AAP));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert

        var regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

        var periode = new Periode(skjæringstidspunkt, null);
        assertThat(grunnlag.getBeregningsgrunnlagPeriode()).isEqualTo(periode);

        var brutto = BigDecimal.valueOf(418860);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.AAP, brutto.doubleValue());
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        assertThat(bgps.getBeregnetPrÅr()).isEqualByComparingTo(brutto);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(brutto);
        assertThat(bgps.getOrginalDagsatsFraTilstøtendeYtelse()).isEqualTo(dagsats.longValue());
    }

    @Test
    void skalForeslåBeregningsgrunnlagForAAPMedManueltFastsattBeløp() {
        //Arrange
        var dagsats = new BigDecimal("1611");
        var beregnetPrÅr = new BigDecimal(324423);
        var inntektsgrunnlag = lagInntektsgrunnlag(dagsats, skjæringstidspunkt, 0.75);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.AAP));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP))
            .medBeregnetPrÅr(beregnetPrÅr)
            .medFastsattAvSaksbehandler(true)
            .build();

        //Act
        var evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert

        var regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

        var periode = new Periode(skjæringstidspunkt, null);
        assertThat(grunnlag.getBeregningsgrunnlagPeriode()).isEqualTo(periode);

        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.AAP, beregnetPrÅr.doubleValue());
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        assertThat(bgps.getBeregnetPrÅr()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(beregnetPrÅr);
        assertThat(bgps.getOrginalDagsatsFraTilstøtendeYtelse()).isEqualTo(dagsats.longValue());
    }


    @Test
    void skalForeslåBeregningsgrunnlagForAAPMedKombinasjonsStatus() {
        //Arrange
        var dagsats = new BigDecimal("1400");
        var inntektsgrunnlag = lagInntektsgrunnlag(dagsats, skjæringstidspunkt, 0.75);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.AAP, AktivitetStatus.SN));
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert
        @SuppressWarnings("unused") var sporing = EvaluationSerializer.asJson(evaluation);

        var regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);

        var periode = new Periode(skjæringstidspunkt, null);
        assertThat(grunnlag.getBeregningsgrunnlagPeriode()).isEqualTo(periode);

        var brutto = BigDecimal.valueOf(273000);
        verifiserBeregningsgrunnlagBruttoPrPeriodeType(grunnlag, BeregningsgrunnlagHjemmel.F_14_7, AktivitetStatus.AAP, brutto.doubleValue());
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.AAP);
        assertThat(bgps.getBeregnetPrÅr()).isEqualByComparingTo(brutto);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(brutto);
        assertThat(bgps.getOrginalDagsatsFraTilstøtendeYtelse()).isEqualTo(dagsats.longValue());
    }

    @Test
    void skalForeslåBeregningsgrunnlagForDagpengerMedBesteberegningFødendeKvinne() {
        //Arrange
        var beregnetDagsats = BigDecimal.valueOf(600);
        var brutto = BigDecimal.valueOf(260000);
        var inntektsgrunnlag = lagInntektsgrunnlag(beregnetDagsats, skjæringstidspunkt, 0.75);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.DP));
        var bgPrStatus = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(p -> p.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).findFirst().get();//NOSONAR
        BeregningsgrunnlagPrStatus.builder(bgPrStatus).medFastsattAvSaksbehandler(true).medBesteberegningPrÅr(brutto).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert
        var regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        assertThat(bgps.getBruttoPrÅr()).isEqualByComparingTo(brutto);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(brutto);
        assertThat(bgps.getOrginalDagsatsFraTilstøtendeYtelse()).isEqualTo(beregnetDagsats.longValue());
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.DP).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    void skalForeslåBeregningsgrunnlagForDagpengerIKombinasjonSNOgMedBesteberegningFødendeKvinne() {
        //Arrange
        var beregnetDagsats = BigDecimal.valueOf(720);
        var brutto = BigDecimal.valueOf(240000);
        var inntektsgrunnlag = lagInntektsgrunnlag(beregnetDagsats, skjæringstidspunkt, 0.5);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.DP, AktivitetStatus.SN));
        var bgPrStatus = beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream().map(p -> p.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP)).findFirst().get();//NOSONAR
        BeregningsgrunnlagPrStatus.builder(bgPrStatus).medFastsattAvSaksbehandler(true).medBesteberegningPrÅr(brutto).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);

        //Act
        var evaluation = new RegelFastsettBeregningsgrunnlagDPellerAAP().evaluer(grunnlag);

        //Assert
        var regelResultat = getRegelResultat(evaluation, "input");
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        assertThat(bgps.getBruttoPrÅr()).isEqualByComparingTo(brutto);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(brutto);
        assertThat(bgps.getOrginalDagsatsFraTilstøtendeYtelse()).isEqualTo(beregnetDagsats.longValue());
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.DP).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    void skalForeslåBeregningsgrunnlagForDagpengerIKombinasjonATOgMedBesteberegningFødendeKvinne() {
        //Arrange
        var fastsattPrÅr = BigDecimal.valueOf(120000);
        var beregnetDagsats = BigDecimal.valueOf(720);
        var besteberegning = BigDecimal.valueOf(240000);
        var inntektsgrunnlag = lagInntektsgrunnlag(beregnetDagsats, skjæringstidspunkt, 0.5);
        var beregningsgrunnlag = Beregningsgrunnlag.builder(settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag,
            List.of(AktivitetStatus.DP, AktivitetStatus.ATFL), Collections.singletonList(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(skjæringstidspunkt.minusYears(1), "12345"))))
            .medYtelsesSpesifiktGrunnlag(new ForeldrepengerGrunnlag(true)).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        var dp = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        BeregningsgrunnlagPrStatus.builder(dp).medFastsattAvSaksbehandler(true).medBesteberegningPrÅr(besteberegning).build();
        var atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        atfl.getArbeidsforhold().forEach(af -> BeregningsgrunnlagPrArbeidsforhold.builder(af)
            .medFastsattAvSaksbehandler(true)
            .medBeregnetPrÅr(fastsattPrÅr)
            .build());

        //Act
        var regelResultat = new RegelForeslåBeregningsgrunnlag(grunnlag).evaluerRegel(grunnlag);

        //Assert
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP);
        assertThat(bgps.getBeregnetPrÅr()).isEqualByComparingTo(besteberegning);
        assertThat(bgps.getBruttoPrÅr()).isEqualByComparingTo(besteberegning);
        assertThat(bgps.getÅrsbeløpFraTilstøtendeYtelse()).isEqualByComparingTo(besteberegning);
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.DP).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
        assertThat(bgps.getOrginalDagsatsFraTilstøtendeYtelse()).isEqualTo(beregnetDagsats.longValue());

        bgps = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        assertThat(bgps.getBeregnetPrÅr()).isEqualByComparingTo(fastsattPrÅr);
        assertThat(bgps.getBruttoPrÅr()).isEqualByComparingTo(fastsattPrÅr);
        assertThat(grunnlag.getBeregningsgrunnlag().getAktivitetStatus(AktivitetStatus.ATFL).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }


    private Inntektsgrunnlag lagInntektsgrunnlag(BigDecimal dagsats, LocalDate skjæringstidspunkt, double utbetalingsgrad) {

        var inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
            .medMåned(skjæringstidspunkt)
            .medInntekt(dagsats)
            .medUtbetalingsfaktor(BigDecimal.valueOf(utbetalingsgrad))
            .build());
        return inntektsgrunnlag;
    }

}
