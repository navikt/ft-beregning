package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektsmelding;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettInntektsmeldingIPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettSammenligningsgrunnlagIPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SammenligningsGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

public class FastsettSammenligningsgrunnlagATTest {

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
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 2, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagAT().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 3, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 2, 28));
    }

    @Test
    public void sammenligningsgrunnlagFørFristMedSisteInntektRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 4, 3));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 5, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2019, 4, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagAT().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 3, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 2, 28));
    }

    @Test
    public void sammenligningsgrunnlagFørIHelgFristMedSisteInntektIkkeRapportert() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 1, 7));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 2, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 11, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.AT);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagAT().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(300000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2017, 12, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2018, 11, 30));
    }

    @Test
    public void skalIkkeLageNyttSammenligningsgrunnlagNårAlleredeEksisterer() {
        //Arrange
        LocalDate skjæringstidspunkt = LocalDate.of(2018, 10, 10);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 12, 31));
        SammenligningsGrunnlag sg = SammenligningsGrunnlag.builder()
            .medSammenligningsperiode(periode)
            .medRapportertPrÅr(BigDecimal.valueOf(55)).build();
        Beregningsgrunnlag.builder(beregningsgrunnlag).medSammenligningsgrunnlagPrStatus(AktivitetStatus.AT, sg);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagAT().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag hentetSg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        assertThat(hentetSg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(55));
        assertThat(hentetSg.getSammenligningsperiode().getFom()).isEqualTo(periode.getFom());
        assertThat(hentetSg.getSammenligningsperiode().getTom()).isEqualTo(periode.getTom());
    }

    @Test
    public void skalReturnereSammenligningsgrunnlagBasertPåPeriodeinntekterForKunATNårSkalFastsetteSammenligningsgrunnlagForAT() {
        //Arrange
        settSimulertNåtidTil(LocalDate.of(2019, 1, 7));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 2, 1);
        Beregningsgrunnlag beregningsgrunnlag = opprettBeregningsgrunnlagFraInntektsmelding(skjæringstidspunkt, BigDecimal.ZERO, BigDecimal.ZERO);
        Periode periode = Periode.of(LocalDate.of(2017, 10, 1), LocalDate.of(2018, 11, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(1000), AktivitetStatus.AT);
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.FL);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagAT().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(12000));
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2017, 12, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2018, 11, 30));
    }

    @Test
    public void skalIkkeLageSammenligningsperiodeIHenholdTilRapporteringsfristNårAlleInntektesmeldingerForeligger() {
        //Arrange
        String orgnrArbeidsforholdNr1 = "987";
        String orgnrArbeidsforholdNr2 = "987654322";
        settSimulertNåtidTil(LocalDate.of(2019, 10, 8));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 11, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Arbeidsforhold arbeidsforhold1 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(orgnrArbeidsforholdNr1).build();
        Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(orgnrArbeidsforholdNr2).build();
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            List.of(arbeidsforhold1, arbeidsforhold2), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2018, 10, 1), LocalDate.of(2019, 8, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.AT);
        opprettInntektsmeldingIPeriode(inntektsgrunnlag, periode, BigDecimal.valueOf(25000), AktivitetStatus.AT, arbeidsforhold1);
        opprettInntektsmeldingIPeriode(inntektsgrunnlag, periode, BigDecimal.valueOf(25000), AktivitetStatus.AT, arbeidsforhold2);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagAT().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 9, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 8, 31));
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(275000));
    }

    @Test
    public void skalLageSammenligningsperiodeIHenholdTilRapporteringsfristNårInntektsmeldingManglerForMinstEttArbeidsforhold() {
        //Arrange
        String orgnrArbeidsforholdNr1 = "987";
        String orgnrArbeidsforholdNr2 = "987654322";
        settSimulertNåtidTil(LocalDate.of(2019, 10, 8));
        LocalDate skjæringstidspunkt = LocalDate.of(2019, 11, 1);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.setInntektRapporteringFristDag(5);
        Arbeidsforhold arbeidsforhold1 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(orgnrArbeidsforholdNr1).build();
        Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.builder().medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).medOrgnr(orgnrArbeidsforholdNr2).build();
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, Collections.singletonList(AktivitetStatus.ATFL),
            List.of(arbeidsforhold1, arbeidsforhold2), Collections.emptyList(), Collections.emptyList());
        Periode periode = Periode.of(LocalDate.of(2018, 10, 1), LocalDate.of(2019, 8, 1));
        opprettSammenligningsgrunnlagIPeriode(beregningsgrunnlag.getInntektsgrunnlag(), periode, BigDecimal.valueOf(25000), AktivitetStatus.AT);
        opprettInntektsmeldingIPeriode(inntektsgrunnlag, periode, BigDecimal.valueOf(25000), AktivitetStatus.AT, arbeidsforhold1);
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        //Act
        new FastsettSammenligningsgrunnlagAT().evaluate(grunnlag);
        //Assert
        SammenligningsGrunnlag sg = grunnlag.getSammenligningsgrunnlagPrStatus(AktivitetStatus.AT);
        assertThat(sg.getSammenligningsperiode().getFom()).isEqualTo(LocalDate.of(2018, 10, 1));
        assertThat(sg.getSammenligningsperiode().getTom()).isEqualTo(LocalDate.of(2019, 9, 30));
        assertThat(sg.getRapportertPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(275000));
    }
}
