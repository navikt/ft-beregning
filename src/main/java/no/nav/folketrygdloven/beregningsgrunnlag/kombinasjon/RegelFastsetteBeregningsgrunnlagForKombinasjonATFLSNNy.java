package no.nav.folketrygdloven.beregningsgrunnlag.kombinasjon;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.RegelBeregningsgrunnlagATFL;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_2";

    public RegelFastsetteBeregningsgrunnlagForKombinasjonATFLSNNy(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR 21 Fastsett beregningsgrunnlag for arbeidstakerandelen
	    Specification<BeregningsgrunnlagPeriode> beregningsgrunnlagKombinasjon =
            rs.beregningsRegel("FP_BR_14-15-27-28", "Beregn beregningsgrunnlag for arbeidstaker/frilanser)",
                new RegelBeregningsgrunnlagATFL(regelmodell).getSpecification(), new Beregnet());


        return beregningsgrunnlagKombinasjon;
    }
}
