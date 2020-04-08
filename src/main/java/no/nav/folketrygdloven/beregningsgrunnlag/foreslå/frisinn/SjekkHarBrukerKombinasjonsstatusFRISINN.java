package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkHarBrukerKombinasjonsstatusFRISINN.ID)
class SjekkHarBrukerKombinasjonsstatusFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 14.6";
    static final String BESKRIVELSE = "Har bruker kombinasjonsstatus med SN?";

    SjekkHarBrukerKombinasjonsstatusFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlagPrStatus().stream()
                .anyMatch(bgps -> AktivitetStatus.SN.equals(bgps.getAktivitetStatus())) ? ja() : nei();
    }
}
