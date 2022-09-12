package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdNaturalytelseBortfalt.ID)
class BeregnPrArbeidsforholdNaturalytelseBortfalt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.4";
    static final String BESKRIVELSE = "Beregn naturalytelse -> naturalytelseverdi * 12";

    BeregnPrArbeidsforholdNaturalytelseBortfalt() {
        super(ID, BESKRIVELSE);
    }


	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		throw new IllegalStateException("Utviklerquiz: Hvorfor slår denne til?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag, ServiceArgument arg) {
		var arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold) arg.getVerdi();

        LocalDate fom = grunnlag.getSkjæringstidspunkt();
        LocalDate tom = grunnlag.getBeregningsgrunnlagPeriode().getFom();
        Inntektsgrunnlag inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        BigDecimal beløp = inntektsgrunnlag.finnTotaltNaturalytelseBeløpMedOpphørsdatoIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), fom, tom)
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ikke være mulig å havne i denne regelen uten beløp."));
        BigDecimal naturalytelseBortfaltPrÅr = beløp.multiply(BigDecimal.valueOf(12));

        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr)
            .build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("naturalytelseBortfaltPrÅr", naturalytelseBortfaltPrÅr);
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        return beregnet(resultater);
    }
}
