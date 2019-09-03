package no.nav.foreldrepenger.beregningsgrunnlag.svangerskapspenger;


import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

public class RegelOppdaterBeregningsgrunnlagSVPTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();


    @Test
    public void testArbeidsforhold() {
        // Arrange
        Arbeidsforhold arbeidsforhold1 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123");
        Arbeidsforhold arbeidsforhold2 = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("321");
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold1, arbeidsforhold2));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrArbeidsforhold> bgArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold.get(0))
            .medBeregnetPrÅr(BigDecimal.valueOf(200_000))
            .medUtbetalingsprosentSVP(BigDecimal.valueOf(10))
            .build();
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold.get(1))
            .medBeregnetPrÅr(BigDecimal.valueOf(555_555))
            .medUtbetalingsprosentSVP(BigDecimal.valueOf(100))
            .build();
        // Act
        Evaluation evaluation = new RegelOppdaterBeregningsgrunnlagSVP(grunnlag).evaluer(grunnlag);
        // Assert
        String sporing = EvaluationSerializer.asJson(evaluation);

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsforhold.get(0).getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(20_000));
        assertThat(arbeidsforhold.get(1).getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(555_555));
        assertThat(sporing).isNotBlank();
    }

    @Test
    public void testFrilans() {
        // Arrange
        Arbeidsforhold frilans = Arbeidsforhold.frilansArbeidsforhold();
        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(),
            List.of(AktivitetStatus.ATFL), List.of(frilans));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        List<BeregningsgrunnlagPrArbeidsforhold> bgArbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        BeregningsgrunnlagPrArbeidsforhold.builder(bgArbeidsforhold.get(0))
            .medBeregnetPrÅr(BigDecimal.valueOf(123_456))
            .medUtbetalingsprosentSVP(BigDecimal.valueOf(1))
            .build();
        // Act
        Evaluation evaluation = new RegelOppdaterBeregningsgrunnlagSVP(grunnlag).evaluer(grunnlag);
        // Assert
        String sporing = EvaluationSerializer.asJson(evaluation);

        List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold();
        assertThat(arbeidsforhold.get(0).getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(1234.56));
        assertThat(sporing).isNotBlank();
    }

    @Test
    public void testSN() {
        // Arrange
        BigDecimal bruttoPrÅr = BigDecimal.valueOf(240_000);
        BigDecimal utbetalingsprosent = BigDecimal.valueOf(60);

        Beregningsgrunnlag beregningsgrunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, new Inntektsgrunnlag(), List.of(AktivitetStatus.SN));
        BeregningsgrunnlagPeriode grunnlag = beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrStatus andel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BeregningsgrunnlagPrStatus.builder(andel)
            .medBeregnetPrÅr(utbetalingsprosent)
            .medUtbetalingsprosentSVP(bruttoPrÅr)
            .build();
        // Act
        Evaluation evaluation = new RegelOppdaterBeregningsgrunnlagSVP(grunnlag).evaluer(grunnlag);
        // Assert
        String sporing = EvaluationSerializer.asJson(evaluation);

        assertThat(andel.getBruttoPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(144_000));
        assertThat(sporing).isNotBlank();
    }

}
