package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdFraInntektsmelding.ID)
class BeregnPrArbeidsforholdFraInntektsmelding extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.2";
    static final String BESKRIVELSE = "Rapportert inntekt = inntektsmelding sats * 12";

    BeregnPrArbeidsforholdFraInntektsmelding() {
        super(ID, BESKRIVELSE);
    }

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		throw new IllegalStateException("Utviklerquiz: Hvorfor slår denne til?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag, ServiceArgument arg) {
		var arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold) arg.getVerdi();
        BigDecimal beløp = grunnlag.getInntektsgrunnlag().getInntektFraInntektsmelding(arbeidsforhold);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medBeregnetPrÅr(beløp.multiply(BigDecimal.valueOf(12)))
            .build();
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("beregnetPrÅr", arbeidsforhold.getBeregnetPrÅr());
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        return beregnet(resultater);
    }
}
