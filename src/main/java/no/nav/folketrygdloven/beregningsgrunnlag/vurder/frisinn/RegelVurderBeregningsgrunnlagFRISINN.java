package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelVurderBeregningsgrunnlagFRISINN implements RuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 3.1";

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();
        Specification<BeregningsgrunnlagPeriode> sjekkMindreEnnTreKvartG = rs.beregningHvisRegel(new SjekkBeregningsgrunnlagFLSNMindreEnnFRISINN(), new AvslagUnderTreKvartG(), new Beregnet());
        Specification<BeregningsgrunnlagPeriode> sjekkFrilansUtenInntekt = rs.beregningHvisRegel(new SjekkFrilansUtenInntekt(), new AvslagFrilansUtenInntekt(), sjekkMindreEnnTreKvartG);
        return sjekkFrilansUtenInntekt;
    }
}
