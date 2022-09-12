package no.nav.folketrygdloven.beregningsgrunnlag.ytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.ServiceArgument;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkHarSaksbehandlerSattInntektFraYtelseManuelt.ID)
class SjekkHarSaksbehandlerSattInntektFraYtelseManuelt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 30.1.1";
    static final String BESKRIVELSE = "Har saksbehandler fastsatt månedsinntekt manuelt?";

    SjekkHarSaksbehandlerSattInntektFraYtelseManuelt() {
        super(ID, BESKRIVELSE);
    }

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		throw new IllegalStateException("Utviklerquiz: Hvorfor slår denne til?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag, ServiceArgument argument) {
		var andel = (BeregningsgrunnlagPrStatus) argument.verdi();
        return Boolean.TRUE.equals(andel.erFastsattAvSaksbehandler()) ? ja() : nei();
    }
}
