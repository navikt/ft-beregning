package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.foreslå.RegelForeslåBeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBeregningsgrunnlagFRISINN extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORESLÅ-FRISINN";

    public RegelForeslåBeregningsgrunnlagFRISINN(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();
        // Fastsett alle BG per status
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag;
        foreslåBeregningsgrunnlag =
            rs.beregningsRegel("FRISINN pr status", "Fastsett beregningsgrunnlag pr status", RegelForeslåBeregningsgrunnlagPrStatusFRISINN.class, regelmodell, "aktivitetStatus", regelmodell.getAktivitetStatuser(), new Beregnet());



        return foreslåBeregningsgrunnlag;
    }
}
