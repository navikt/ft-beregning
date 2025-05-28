package no.nav.folketrygdloven.beregningsgrunnlag.foreslå;

import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.GRUNNBELØP_2017;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppGrunnlagMedEnPeriode;
import static no.nav.folketrygdloven.beregningsgrunnlag.BeregningsgrunnlagScenario.settoppMånedsinntekter;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;

class BeregningsgrunnlagDocTest {

    @Test
    void test_documentation() { // NOSONAR
	    var orgnr = "987";
	    var skjæringstidspunkt = LocalDate.of(2018, Month.JANUARY, 15);
	    var arbeidsforholStartdato = skjæringstidspunkt.minusYears(2);
	    var månedsinntekt = BigDecimal.valueOf(GRUNNBELØP_2017 / 12 / 2);
	    var arbeidsforhold = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsforholStartdato, orgnr);
	    var inntektsgrunnlag = settoppMånedsinntekter(skjæringstidspunkt,
                List.of(månedsinntekt, månedsinntekt, månedsinntekt), Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold);
	    var grunnlag = settoppGrunnlagMedEnPeriode(skjæringstidspunkt, inntektsgrunnlag, List.of(AktivitetStatus.ATFL),
                List.of(arbeidsforhold), List.of(månedsinntekt.multiply(BigDecimal.valueOf(12)))).getBeregningsgrunnlagPerioder().get(0);

	    var beregning = new RegelForeslåBeregningsgrunnlag(grunnlag).getSpecification();

	    var json = EvaluationSerializer.asJson(beregning);
	    assertThat(json).isNotEmpty();
    }
}
