package no.nav.folketrygdloven.beregningsgrunnlag.militær;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = RegelForeslåBeregningsgrunnlagMilitær.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=297830750")
public class RegelForeslåBeregningsgrunnlagMilitær implements RuleService<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 32";
    static final String BESKRIVELSE = "Foreslå beregningsgrunnlag for militær og sivilforsvarstjeneste.";

	public RegelForeslåBeregningsgrunnlagMilitær() {
		super();
	}

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // FP_BR 32.6 Foreslå beregningsgrunnlag for status militær og sivilforsvarstjeneste
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlagMS = rs.beregningsRegel(ForeslåBeregningsgrunnlagMS.ID, ForeslåBeregningsgrunnlagMS.BESKRIVELSE,
            new ForeslåBeregningsgrunnlagMS(), new Beregnet());

        return foreslåBeregningsgrunnlagMS;
    }
}
