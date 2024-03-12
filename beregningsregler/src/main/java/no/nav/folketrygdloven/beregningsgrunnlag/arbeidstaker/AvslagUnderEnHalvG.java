package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(AvslagUnderEnHalvG.ID)
public class AvslagUnderEnHalvG extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_VK_8_47_5";
    static final String BESKRIVELSE = "Opprett regelmerknad om avslag under 0.5G";

    public AvslagUnderEnHalvG() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return nei(new BeregningUtfallMerknad(BeregningUtfallÅrsak.AVSLAG_UNDER_HALV_G));
    }
}
