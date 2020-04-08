package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelVurderBeregningsgrunnlagFRISINN extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 3.1";

    public RegelVurderBeregningsgrunnlagFRISINN(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();
        return rs.beregningHvisRegel(new SjekkBeregningsgrunnlagFLSNMindreEnnFRISINN(), new AvslagUnderTreKvartG(), new Beregnet());
    }
}
