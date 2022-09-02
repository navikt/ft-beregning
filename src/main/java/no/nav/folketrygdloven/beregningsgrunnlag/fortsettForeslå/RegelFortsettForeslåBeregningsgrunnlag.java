package no.nav.folketrygdloven.beregningsgrunnlag.fortsettForeslå;

import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Beregnet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.DynamicRuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.specification.Specification;


public class RegelFortsettForeslåBeregningsgrunnlag extends DynamicRuleService<BeregningsgrunnlagPeriode> {

    public static final String ID = "BG-FORESLÅ";

    public RegelFortsettForeslåBeregningsgrunnlag(BeregningsgrunnlagPeriode regelmodell) {
        super(regelmodell);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Specification<BeregningsgrunnlagPeriode> getSpecification() {
        Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();

        // Fastsett alle BG per status
        Specification<BeregningsgrunnlagPeriode> foreslåBeregningsgrunnlag;
        foreslåBeregningsgrunnlag =
            rs.beregningsRegel("FP_BR pr status", "Fastsett beregningsgrunnlag pr status", RegelFortsettForeslåBeregningsgrunnlagPrStatus.class, regelmodell, "aktivitetStatus",
		            regelmodell.getAktivitetStatuser().stream().filter(s -> s.getAktivitetStatus().erSelvstendigNæringsdrivende()).collect(Collectors.toList()), new Beregnet());

	    Specification<BeregningsgrunnlagPeriode> skalKjøreFortsettForeslå =
			    rs.beregningHvisRegel(new SkalKjøreFortsettForeslå(), foreslåBeregningsgrunnlag, new Beregnet());

		return skalKjøreFortsettForeslå;

    }
}
