package no.nav.folketrygdloven.beregningsgrunnlag.militær;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter.getRegelResultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;

class RegelForeslåBeregningsgrunnlagMilitærTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, 2, 1);
    private static final BigDecimal GRUNNBELØP_2018 = BigDecimal.valueOf(96883);
    private static final BigDecimal ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP = BigDecimal.valueOf(3);
    private static final BigDecimal BELØP_MILITÆR_HAR_KRAV_PÅ_FP = GRUNNBELØP_2018.multiply(ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP);

    @Test
    void skalTesteAtMSBlirSattTil3GNårIkkeFastsattAvSaksbehandler() {
        //Arrange
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.MS));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        var evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        var regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(BELØP_MILITÆR_HAR_KRAV_PÅ_FP);
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    void skalTesteAtMSBlirSattTil3GNårSaksbehandlerHarFastsattMindreEnn3G() {
        //Arrange
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.MS));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).medAntallGMilitærHarKravPå(ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP.intValue()).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS))
            .medBeregnetPrÅr(BigDecimal.valueOf(250_000))
            .medFastsattAvSaksbehandler(true)
            .build();
        //Act
        var evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        var regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(BELØP_MILITÆR_HAR_KRAV_PÅ_FP);
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    void skalTesteAtMSBlirSattTilDetSaksbehandlerHarFastsattNårMerEnn3G() {
        //Arrange
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Collections.singletonList(AktivitetStatus.MS));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS))
            .medBeregnetPrÅr(BigDecimal.valueOf(420_000))
            .medFastsattAvSaksbehandler(true)
            .build();
        //Act
        var evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        var regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(420_000));
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    void skalTesteAtMSFårDifferansenOver0Mellom3GogBruttoPåGrunnlaget() {
        //Arrange
        var snInntekt = BigDecimal.valueOf(76783);
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Arrays.asList(AktivitetStatus.MS, AktivitetStatus.SN));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).medAntallGMilitærHarKravPå(ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP.intValue()).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS))
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN))
            .medBeregnetPrÅr(snInntekt)
            .build();
        //Act
        var evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        var regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        var diffMellom3GOgATFLInntekt = BELØP_MILITÆR_HAR_KRAV_PÅ_FP.subtract(snInntekt);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(diffMellom3GOgATFLInntekt);
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

    @Test
    void skalTesteAtMSIkkeFårPengerNårSamletBruttoOver3G() {
        //Arrange
        var beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            Arrays.asList(AktivitetStatus.MS, AktivitetStatus.SN, AktivitetStatus.DP));
        Beregningsgrunnlag.builder(beregningsgrunnlag).medGrunnbeløp(GRUNNBELØP_2018).medAntallGMilitærHarKravPå(ANTALL_G_MILITÆR_HAR_KRAV_PÅ_FP.intValue()).build();
        var grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS))
            .medBeregnetPrÅr(BigDecimal.ZERO)
            .build();
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN))
            .medBeregnetPrÅr(BigDecimal.valueOf(200_000))
            .build();
        BeregningsgrunnlagPrStatus.builder(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.DP))
            .medBeregnetPrÅr(BigDecimal.valueOf(150_000))
            .build();
        //Act
        var evaluation = new RegelForeslåBeregningsgrunnlagMilitær().evaluer(grunnlag);
        var regelResultat = getRegelResultat(evaluation, "input");

        //Assert
        assertThat(regelResultat.beregningsresultat()).isEqualTo(ResultatBeregningType.BEREGNET);
        assertThat(grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.MS).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(beregningsgrunnlag.getAktivitetStatus(AktivitetStatus.MS).getHjemmel()).isEqualTo(BeregningsgrunnlagHjemmel.F_14_7);
    }

}
