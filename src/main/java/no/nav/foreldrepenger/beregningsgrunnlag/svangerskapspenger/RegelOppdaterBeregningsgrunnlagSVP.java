package no.nav.foreldrepenger.beregningsgrunnlag.svangerskapspenger;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.specification.Specification;

@RuleDocumentation(value = RegelOppdaterBeregningsgrunnlagSVP.ID, specificationReference = "https://confluence.adeo.no/pages/viewpage.action?pageId=329023538")
public class RegelOppdaterBeregningsgrunnlagSVP extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "SVP_BR_X";

    RegelOppdaterBeregningsgrunnlagSVP(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // SVP_BR
        Specification<BeregningsgrunnlagPeriode> reduserBeregningsgrunnlag = rs.beregningsRegel(OppdaterBeregningsgrunnlagIhhtDelvisSøktYtelse.ID,
            OppdaterBeregningsgrunnlagIhhtDelvisSøktYtelse.BESKRIVELSE,
            new OppdaterBeregningsgrunnlagIhhtDelvisSøktYtelse(), new Beregnet());


        return reduserBeregningsgrunnlag;
    }
}
