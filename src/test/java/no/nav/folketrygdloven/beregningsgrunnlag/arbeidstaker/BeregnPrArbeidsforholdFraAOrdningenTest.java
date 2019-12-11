package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.opprettBeregningsgrunnlagFraInntektskomponenten;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class BeregnPrArbeidsforholdFraAOrdningenTest {

    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345");

    @Test
    public void skalKasteExceptionNårBeregningperiodeErNull() {
        //Arrange
        Beregningsgrunnlag grunnlag = opprettBeregningsgrunnlagFraInntektskomponenten(SKJÆRINGSTIDSPUNKT, BigDecimal.valueOf(35000), BigDecimal.ZERO, false);
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0)).medBeregningsperiode(null);
        BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        //Act
        Assertions.assertThrows(IllegalStateException.class, () -> {
            new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
        });
    }

    @Test
    public void skalBeregneSnittAvInntekterIBeregningperioden() {
        //Arrange
        Periode beregningsperiode = Periode.of(SKJÆRINGSTIDSPUNKT.minusMonths(3).withDayOfMonth(1), SKJÆRINGSTIDSPUNKT.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()));
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(31452)).medMåned(beregningsperiode.getFom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(48739)).medMåned(beregningsperiode.getFom().plusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(44810)).medMåned(beregningsperiode.getTom()).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());
        //Inntekt utenfor beregningsperioden - skal ikke tas med
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder().medInntekt(BigDecimal.valueOf(999999)).medMåned(beregningsperiode.getFom().minusMonths(1)).medArbeidsgiver(arbeidsforhold).medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING).build());

        Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(LocalDate.now(), inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold arbeidstakerStatus = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidstakerStatus).medBeregningsperiode(beregningsperiode);
        //Act
        Evaluation resultat = new BeregnPrArbeidsforholdFraAOrdningen(arbeidstakerStatus).evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
        assertThat(arbeidstakerStatus.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(500004));
    }

}
