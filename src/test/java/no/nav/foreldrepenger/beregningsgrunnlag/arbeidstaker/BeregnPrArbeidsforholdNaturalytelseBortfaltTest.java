package no.nav.foreldrepenger.beregningsgrunnlag.arbeidstaker;

import static no.nav.foreldrepenger.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

public class BeregnPrArbeidsforholdNaturalytelseBortfaltTest {


    private final static LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("12345");

    @Test
    public void skalSummereNaturalytelserBortfaltPåSkjæringstidspunktet() {
        //Arrange
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        List<NaturalYtelse> naturalYtelser = List.of(
            new NaturalYtelse(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT.minusYears(1), SKJÆRINGSTIDSPUNKT.minusDays(2)), //Tas ikke med
            new NaturalYtelse(BigDecimal.valueOf(5000), SKJÆRINGSTIDSPUNKT.minusYears(1), SKJÆRINGSTIDSPUNKT.minusDays(1)),
            new NaturalYtelse(BigDecimal.valueOf(3333), SKJÆRINGSTIDSPUNKT.minusYears(1), SKJÆRINGSTIDSPUNKT.minusDays(1)),
            new NaturalYtelse(BigDecimal.valueOf(2222), SKJÆRINGSTIDSPUNKT.minusYears(1), SKJÆRINGSTIDSPUNKT.minusDays(1)));
        inntektsgrunnlag.leggTilPeriodeinntekt(Periodeinntekt.builder()
            .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSMELDING)
            .medArbeidsgiver(arbeidsforhold)
            .medInntekt(BigDecimal.valueOf(30000))
            .medMåned(SKJÆRINGSTIDSPUNKT)
            .medNaturalYtelser(naturalYtelser)
            .build());
        Beregningsgrunnlag grunnlag = settoppGrunnlagMedEnPeriode(SKJÆRINGSTIDSPUNKT, inntektsgrunnlag, List.of(AktivitetStatus.ATFL), List.of(arbeidsforhold));
        BeregningsgrunnlagPeriode periode = grunnlag.getBeregningsgrunnlagPerioder().get(0);
        BeregningsgrunnlagPrArbeidsforhold andel = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().get(0);
        //Act
        new BeregnPrArbeidsforholdNaturalytelseBortfalt(andel).evaluate(periode);
        //Assert
        assertThat(andel.getNaturalytelseBortfaltPrÅr()).isPresent();
        assertThat(andel.getNaturalytelseBortfaltPrÅr().get()).isEqualByComparingTo(BigDecimal.valueOf(126_660));//NOSONAR
    }

}