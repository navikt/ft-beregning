package no.nav.folketrygdloven.beregningsgrunnlag.ytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkHarSaksbehandlerSattInntektFraYtelseManuelt.ID)
class SjekkHarSaksbehandlerSattInntektFraYtelseManuelt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 30.1.1";
    static final String BESKRIVELSE = "Har saksbehandler fastsatt månedsinntekt manuelt?";
    private BeregningsgrunnlagPrStatus andel;

    SjekkHarSaksbehandlerSattInntektFraYtelseManuelt(BeregningsgrunnlagPrStatus andel) {
        super(ID, BESKRIVELSE);
        this.andel = andel;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return Boolean.TRUE.equals(andel.erFastsattAvSaksbehandler()) ? ja() : nei();
    }
}
