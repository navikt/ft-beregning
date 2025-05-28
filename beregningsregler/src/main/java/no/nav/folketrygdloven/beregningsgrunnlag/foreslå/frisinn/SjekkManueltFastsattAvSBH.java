package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkManueltFastsattAvSBH.ID)
class SjekkManueltFastsattAvSBH extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 2.12";
    static final String BESKRIVELSE = "Har noen av andelene blitt manuelt fastsatt?";

    SjekkManueltFastsattAvSBH() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var atlfAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atlfAndel == null) {
            return nei();
        }
        var finnesArbforSomErManueltFastsatt = atlfAndel.getArbeidsforhold().stream()
            .anyMatch(BeregningsgrunnlagPrArbeidsforhold::getFastsattAvSaksbehandler);
        return finnesArbforSomErManueltFastsatt ? ja() : nei();
    }
}
