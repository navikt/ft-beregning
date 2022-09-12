package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.sykepenger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdNaturalytelseBortfaltForSykepenger.ID)
public class BeregnPrArbeidsforholdNaturalytelseBortfaltForSykepenger extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.9";
    static final String BESKRIVELSE = "Beregn bortfalt naturalytelse i arbeidsgiverperioden for sykepenger -> naturalytelseverdi * 12";

    public BeregnPrArbeidsforholdNaturalytelseBortfaltForSykepenger() {
        super(ID, BESKRIVELSE);
    }

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		throw new IllegalStateException("Utviklerquiz: Hvorfor slår denne til?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag, ServiceArgument arg) {
		var arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold) arg.getVerdi();
		Inntektsgrunnlag inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        BigDecimal naturalytelseBortfaltPrÅr = arbeidsforhold.getArbeidsgiverperioder()
            .stream()
            .map(periode -> finnBortfaltNaturalytelsePrPeriode(inntektsgrunnlag, periode, arbeidsforhold))
            .map(naturalYtelseBortfaltPrMnd -> naturalYtelseBortfaltPrMnd.multiply(BigDecimal.valueOf(12)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
            .medNaturalytelseBortfaltPrÅr(naturalytelseBortfaltPrÅr)
            .build();

        Map<String, Object> resultater = new HashMap<>();
        resultater.put("naturalytelseBortfaltPrÅr", naturalytelseBortfaltPrÅr);
        resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
        return beregnet(resultater);
    }

    private BigDecimal finnBortfaltNaturalytelsePrPeriode(Inntektsgrunnlag inntektsgrunnlag, Periode periode, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        return inntektsgrunnlag.finnTotaltNaturalytelseBeløpMedOpphørsdatoIPeriodeForArbeidsforhold(arbeidsforhold.getArbeidsforhold(), periode.getFom(), periode.getTom())
            .orElse(BigDecimal.ZERO);
    }
}
