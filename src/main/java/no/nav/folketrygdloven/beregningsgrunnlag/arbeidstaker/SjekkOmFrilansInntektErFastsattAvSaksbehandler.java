package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmFrilansInntektErFastsattAvSaksbehandler.ID)
class SjekkOmFrilansInntektErFastsattAvSaksbehandler extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.9";
    static final String BESKRIVELSE = "Er frilans inntekt fastsatt av saksbehandler?";

    SjekkOmFrilansInntektErFastsattAvSaksbehandler() {
        super(ID, BESKRIVELSE);
    }

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		throw new IllegalStateException("Utviklerquiz: Hvorfor slår denne til?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag, ServiceArgument arg) {
		var arbeidsforhold = (BeregningsgrunnlagPrArbeidsforhold) arg.getVerdi();
        return Boolean.TRUE.equals(arbeidsforhold.getFastsattAvSaksbehandler()) ? ja() : nei();
    }
}
