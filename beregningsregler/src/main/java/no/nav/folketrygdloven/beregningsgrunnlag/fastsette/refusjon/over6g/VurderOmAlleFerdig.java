package no.nav.folketrygdloven.beregningsgrunnlag.fastsette.refusjon.over6g;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(VurderOmAlleFerdig.ID)
class VurderOmAlleFerdig extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.13.3";
    static final String BESKRIVELSE = "Vurder om refusjonskrav for alle beregningsgrunnlagsandeler kan settes til ferdig avkortet";

    VurderOmAlleFerdig() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var ikkeFerdig = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold()
            .stream()
            .filter(af -> af.getMaksimalRefusjonPrÅr() != null)
            .filter(af -> af.getAvkortetRefusjonPrÅr() == null)
            .findAny();
        return ikkeFerdig.isPresent() ? nei() : ja();
    }
}
