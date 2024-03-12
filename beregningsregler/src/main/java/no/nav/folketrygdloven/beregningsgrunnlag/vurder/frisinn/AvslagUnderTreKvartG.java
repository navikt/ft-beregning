package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(AvslagUnderTreKvartG.ID)
public class AvslagUnderTreKvartG extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 3.3";
    static final String BESKRIVELSE = "Opprett regelmerknad om avslag under 0.75G";

    public AvslagUnderTreKvartG() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return nei(new BeregningUtfallMerknad(BeregningUtfallÅrsak.AVSLAG_UNDER_TREKVART_G));
    }
}
