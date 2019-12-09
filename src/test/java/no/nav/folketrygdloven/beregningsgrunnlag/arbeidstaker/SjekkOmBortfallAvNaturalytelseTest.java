package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

public class SjekkOmBortfallAvNaturalytelseTest {

    private final static LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345");

    @Test
    public void skalReturnereNeiNårNaturalytelserHarOpphørtFørSkjæringstidspunkt() {
        //Arrange
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<NaturalYtelse> naturalYtelser = List.of(new NaturalYtelse(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT.minusYears(1), SKJÆRINGSTIDSPUNKT.minusDays(2)));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medInntekt(BigDecimal.TEN)
            .medMåned(SKJÆRINGSTIDSPUNKT)
            .medNaturalYtelser(naturalYtelser)
            .build());
        Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold andel = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        //Act
        Evaluation resultat = new SjekkOmBortfallAvNaturalytelse(andel).evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.NEI);
    }

    @Test
    public void skalReturnereJaNårNaturalytelserHarOpphørtPåSkjæringstidspunkt() {
        //Arrange
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<NaturalYtelse> naturalYtelser = List.of(new NaturalYtelse(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT.minusYears(1), SKJÆRINGSTIDSPUNKT.minusDays(1)));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medInntekt(BigDecimal.TEN)
            .medMåned(SKJÆRINGSTIDSPUNKT)
            .medNaturalYtelser(naturalYtelser)
            .build());
        Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold andel = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        //Act
        Evaluation resultat = new SjekkOmBortfallAvNaturalytelse(andel).evaluate(periode);
        //Assert
        assertThat(resultat.result()).isEqualTo(Resultat.JA);
    }
}
