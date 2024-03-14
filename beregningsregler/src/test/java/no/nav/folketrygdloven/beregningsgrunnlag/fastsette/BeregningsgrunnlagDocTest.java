package no.nav.folketrygdloven.beregningsgrunnlag.fastsette;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.specification.Specification;

class BeregningsgrunnlagDocTest {

    @Test
    void test_documentation() { // NOSONAR
        String orgnr = "4353";
        LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
	    LocalDate arbeidsforholdStartdato = skjæringstidspunkt.minusYears(2);
	    Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholdStartdato, orgnr);
        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt,
                List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
                List.of(arbeidsforhold), Collections.singletonList(månedsinntekt.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);

        Specification<BeregningsgrunnlagPeriode> beregning = new RegelForeslåBeregningsgrunnlag(grunnlag).getSpecification();

	    String json = EvaluationSerializer.asJson(beregning);
	    assertThat(json).isNotEmpty();
    }
}
