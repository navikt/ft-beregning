package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDescriptionDigraph;
import no.nav.fpsak.nare.specification.Specification;

public class BeregningsgrunnlagDocTest {

    @Test
    public void test_documentation() { // NOSONAR
        String orgnr = "987";
        LocalDate skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
        BigDecimal månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
        Arbeidsforhold arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr);
        Inntektsgrunnlag inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt,
                List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
        BeregningsgrunnlagPeriode grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
                List.of(arbeidsforhold), List.of(månedsinntekt.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);

        Specification<BeregningsgrunnlagPeriode> beregning = new RegelForeslåBeregningsgrunnlag(grunnlag).getSpecification();

        RuleDescriptionDigraph digraph = new RuleDescriptionDigraph(beregning.ruleDescription());

        @SuppressWarnings("unused")
        String json = digraph.toJson();

//        System.out.println(json);
    }
}
