package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static java.util.stream.Collectors.toList;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlagIPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

public class FastsettSammenligningsgrunnlagFLTest {
    private static final String FUNKSJONELT_TIDSOFFSET = DateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_PERIODE;

    @AfterAll
    public static void after() {
        System.clearProperty(DateUtil.SystemConfiguredClockProvider.PROPERTY_KEY_OFFSET_PERIODE);
        DateUtil.init();
    }

    private void settSimulertNåtidTil(LocalDate dato) {
        Period periode = Period.between(LocalDate.now(), dato);
        System.setProperty(FUNKSJONELT_TIDSOFFSET, periode.toString());
        DateUtil.init();
    }

    @Test
    public void sammenligningsgrunnlagFørFristMedSisteInntektIkkeRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 4, 3));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 2, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 3, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 2, 28));
    }

    @Test
    public void sammenligningsgrunnlagFørFristMedSisteInntektRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 4, 3));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 4, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 3, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 2, 28));
    }

    @Test
    public void sammenligningsgrunnlagFørIHelgFristMedSisteInntektIkkeRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 1, 7));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 2, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 11, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2017, 12, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2018, 11, 30));
    }

    @Test
    public void sammenligningsgrunnlagEtterFristMedSisteInntektIkkeRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 10, 8));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 11, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2018, 10, 1), LocalDate.of(2019, 8, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 9, 30));
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(275000));
    }

    @Test
    public void sammenligningsgrunnlagEtterFristMedSisteInntektRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 10, 8));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 11, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2018, 10, 1), LocalDate.of(2019, 9, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 9, 30));
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
    }

    @Test
    public void sammenligningsgrunnlagBehandlingEtterStp() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 5, 11));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 12, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(200000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 5, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 4, 30));
    }

    @Test
    public void sammenligningsgrunnlagBehandlingLengeEtterStp() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 6, 11));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 12, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        Periode periode2 = Periode.of(LocalDate.of(2019,5,1), LocalDate.of(2019, 6, 30));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode2, BigDecimal.valueOf(333333), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 5, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 4, 30));
    }

    @Test
    public void skalIkkeLageNyttSammenligningsgrunnlagNårAlleredeEksisterer() {
        //Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2018, 10, 10);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 12, 31));
        SammenligningsGrunnlag sg = SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(periode)
            .medRapportertPrÅr(BigDecimal.valueOf(55)).build();
        Beregningsgrunnlag.builder(beregningsgrunnlag).medSammenligningsgrunnlagPrStatus(AktivitetStatus.FL, sg);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus beregningsgrunnlagPrStatus = grunnlag.getBeregningsgrunnlagPrStatus().stream().collect(toList()).get(0);
        BeregningsgrunnlagPrStatus.builder(beregningsgrunnlagPrStatus).medBeregningsperiode(periode).build();

        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag hentetSg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(hentetSg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(55));
        assertThat(hentetSg.getSammenligningsperiode().getFom()).isEqualTo(periode.getFom());
        assertThat(hentetSg.getSammenligningsperiode().getTom()).isEqualTo(periode.getTom());
    }

    @Test
    public void skalReturnereSammenligningsgrunnlagBasertPåPeriodeinntekterForKunFLNårSkalFastsetteSammenligningsgrunnlagForFL() {
        //Arrange
        var månedsinntektAT = 25000L;
        var månedsinntektFL = 1000L;
        settSimulertNåtidTil(LocalDate.of(2019, 1, 7));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 2, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 11, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(månedsinntektAT), AktivitetStatus.AT);
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(månedsinntektFL), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagFL().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.FL);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(12*månedsinntektFL));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2017, 12, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2018, 11, 30));
    }
}

